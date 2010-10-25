(defproject Clothesline "1.0.0-SNAPSHOT"
  :description "FIXME: write"
  :dependencies [[org.clojure/clojure "1.2.0"]
                 [org.clojure/clojure-contrib "1.2.0"]
                 [clout "0.3.1"]
                 [ring "0.3.1"]]
  :dev-dependencies [[swank-clojure "1.3.0-SNAPSHOT"]
                     [clj-stacktrace "0.2.0"]]
  :aot [clothesline.interop.iservice
        clothesline.service.base-service
        clothesline.interop.nodetest])
