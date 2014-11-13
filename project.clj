(defproject clothesline "0.2.0-SNAPSHOT"
  :description "A Clojure port of the Erlang project WebMachine, a stateful HTTP service library."
  :dependencies [[org.clojure/clojure "1.2.0"]
                 [org.clojure/clojure-contrib "1.2.0"]
                 [clout "0.3.1"]
                 [ring "0.3.1"]
                 [clj-time "0.2.0-SNAPSHOT"]]
  :dev-dependencies [[clj-stacktrace "0.2.0"]]
  ;:jvm-opts ["-agentlib:jdwp=transport=dt_socket,address=8030,server=y,suspend=n"]
  :aot [clothesline.interop.nodetest
        clothesline.interop.iservice
        clothesline.service.base-service
        clothesline.core
        clothesline.startup
        clothesline.util])
