(defproject jira-reader "v2"
  :description "Reads from a project on a JIRA server and writes the API output for issues to a Kafka topic."
  :url "https://github.com/clingen-data-model/jira-reader"
  :license {:name "EPL-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [cheshire "5.9.0"]
                 [clj-http "3.10.0"]
                 [com.taoensso/timbre "4.10.0"]
                 [com.fzakaria/slf4j-timbre "0.3.14"]
                 [fundingcircle/jackdaw "0.6.9"]
                 [com.google.cloud/google-cloud-storage "1.101.0"]]
  :main ^:skip-aot jira-reader.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :uberjar-name "jira-reader.jar"}})
