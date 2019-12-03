(ns jira-reader.jira
  (:require  [clj-http.client :as http]
             [cheshire.core :as json]
             [clojure.java.io :as io]
             [taoensso.timbre :as log]
             [clojure.string :as s]))

(def max-results 50)

(defn seq->jql 
  "Render a seq in a form usable in a JQL collection"
  [query-sequence]
  (s/join ", " (map #(str "\"" % "\"") query-sequence)))

(defn issues-from [start-time start config]
  (let [query-str (str "project = " (:jira-project config) " AND type IN ("
                       (seq->jql (:jira-types config))  ") AND status != Open AND updated > '" start-time "' ORDER BY updated DESC")
        _ (log/info query-str)
        url (:jira-host config)
        req  {:query-params 
              {:jql query-str
               :startAt start
               :maxResults max-results}
              :content-type "application/json"
              :basic-auth [(:jira-user config)
                           (:jira-password config)]}
        result (http/get url req)
        result-body (-> result :body json/parse-string)
        remaining (- (result-body "total") (+ start max-results))]
    (log/info "Retrieving messages from " start ", " (result-body "total") " total.")
    (if (> remaining 0)
      (lazy-seq
       (cons (result-body "issues")
             (issues-from start-time (+ start max-results) config)))
      (list (result-body "issues")))))

(defn issues
  "Return a lazy seq of blocks of raw issues, retrieved from JIRA"
  [start-time config] (issues-from start-time 0 config))

(defn write-jira-output
  "Write records from JIRA to data/jira-output
  curation type should be one of :gene or :region"
  [output-dir config]
  (println "Writing data to " output-dir)
  (let [issue-batches (issues "2010-01-01" config)]
    (doseq [batch issue-batches
            issue batch]
      (with-open [w (io/writer (str output-dir (get issue "key") ".json"))]
        (json/generate-stream issue w {:pretty true}))))
  (println "Completed writing jira output"))
