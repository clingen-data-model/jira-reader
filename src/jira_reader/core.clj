(ns jira-reader.core
  (:require [jira-reader.jira :as jira]
            [jackdaw.client :as jc]
            [jackdaw.data :as jd]
            [taoensso.timbre :as log]
            [cheshire.core :as json]
            [clojure.string :as s])
  (:import [org.apache.kafka.clients.producer Producer KafkaProducer ProducerRecord]
           [com.google.cloud.storage Bucket BucketInfo Storage StorageOptions
            BlobId BlobInfo Blob]
           com.google.cloud.storage.Storage$BlobWriteOption
           com.google.cloud.storage.Storage$BlobTargetOption
           com.google.cloud.storage.Storage$BlobSourceOption
           com.google.cloud.storage.Blob$BlobSourceOption
           [java.time.format DateTimeFormatter]
           [java.time LocalDateTime ZonedDateTime ZoneId ZoneOffset OffsetDateTime])
  (:gen-class))


(def app-config {:jira-user (System/getenv "JIRA_READER_USER")
                 :jira-password (System/getenv "JIRA_READER_PASSWORD")
                 :jira-host (System/getenv "JIRA_READER_HOST")
                 :jira-project (System/getenv "JIRA_READER_PROJECT")
                 :jira-types (when-let [types (System/getenv "JIRA_READER_TYPES")]
                               (s/split types #";"))
                 :kafka-host (System/getenv "KAFKA_HOST")
                 :kafka-user (System/getenv "KAFKA_USER")
                 :kafka-password (System/getenv "KAFKA_PASSWORD")
                 :kafka-topic (System/getenv "JIRA_READER_TOPIC")
                 :gcs-bucket (System/getenv "JIRA_READER_BUCKET")
                 :default-start-date (System/getenv "JIRA_READER_DEFAULT_START_DATE")})

(def gc-storage (.getService (StorageOptions/getDefaultInstance)))

(def date-time-format (DateTimeFormatter/ofPattern "yyyy-MM-dd HH:mm"))

(defn- write-timestamp [timestamp opts]
  (let [blob-id (BlobId/of (:gcs-bucket opts) (:jira-project opts))
        blob-info (-> blob-id BlobInfo/newBuilder (.setContentType "text/plain") .build)]
    (.create gc-storage blob-info (.getBytes timestamp) (make-array Storage$BlobTargetOption 0))))

(defn- read-timestamp [opts]
  (let [blob-id (BlobId/of (:gcs-bucket opts) (:jira-project opts))
        blob (.get gc-storage blob-id)]
    (when blob (String. (.getContent blob (make-array Blob$BlobSourceOption 0))))))

(defn kafka-config 
  "Expects, at a minimum, :user and :password in opts. "
  [opts]
  {"ssl.endpoint.identification.algorithm" "https"
   "sasl.mechanism" "PLAIN"
   "request.timeout.ms" "20000"
   "bootstrap.servers" (:kafka-host opts)
   "retry.backoff.ms" "500"
   "security.protocol" "SASL_SSL"
   "key.serializer" "org.apache.kafka.common.serialization.StringSerializer"
   "value.serializer" "org.apache.kafka.common.serialization.StringSerializer"
   "sasl.jaas.config" 
   (str "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"" 
        (:kafka-user opts) "\" password=\"" (:kafka-password opts) "\";")})

(defn send-update-to-exchange [last-polled producer opts]
  (let [new-issue-batches (jira/issues last-polled app-config)]
    (doseq [batch new-issue-batches]
      (doseq [issue batch]
        (let [k (get issue "key")
              v (json/generate-string issue {:pretty true})]
          (jc/send! producer (jd/->ProducerRecord {:topic-name (:kafka-topic opts)} k v)))))))

(defn exchange-update-loop
  "Loop to update data exchange with messages updated after current date and time"
  [opts]
  (while true
    (try
      (with-open [producer (jc/producer (kafka-config opts))]
        (let [last-polled (or (read-timestamp opts) (:default-start-date opts))
              current-time (-> (OffsetDateTime/now ZoneOffset/UTC) (.format date-time-format))]
          (log/info "Querying JIRA from " last-polled)
          (send-update-to-exchange last-polled producer opts)
          (write-timestamp current-time opts)))
      (catch Exception e (log/error e)))
    (Thread/sleep (* 1000 60 5))))

(defn -main
  [& args]
  (exchange-update-loop app-config))
