(ns jira-reader.kafka
  (:require [taoensso.timbre :as log]
            [clojure.java.io :as io])
  (:import java.util.Properties
           [org.apache.kafka.clients.producer KafkaProducer Producer ProducerRecord
            ProducerConfig]
           org.apache.kafka.common.config.SaslConfigs))

(defn- build-properties [config-fname]
  (with-open [config (io/reader config-fname)]
    (doto (Properties.)
      (.putAll {"sasl.jaas.config" 
                (str "org.apache.kafka.common.security.plain.PlainLoginModule required username=\""
                     (System/getenv "CCLOUD_USER") 
                     "\" password=\""
                     (System/getenv "CCLOUD_PASSWORD")
                     "\";")
                "bootstrap.servers" (System/getenv "DATA_EXCHANGE_HOST")})
      (.load config))))

(defn producer []
  (-> "ccloud.properties" io/resource build-properties KafkaProducer.))

