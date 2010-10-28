(ns clothesline.service.base-service
  (:use [clothesline.protocol [test-helpers :only [annotated-return]]])
  (:gen-class
    :name clothesline.service.BaseService
    :implements [clothesline.interop.IService]))


(defn -resourceExists [self request graphdata]
  (annotated-return true))

(defn -serviceAvailable [self request graphdata]
  (annotated-return true))

(defn -authorized [self request graphdata]
  (annotated-return true))

(defn -forbidden [self request graphdata]
  (annotated-return false))

(defn -allowMissingPost [self request graphdata]
  (annotated-return false))

(defn -malformedRequest [self request graphdata]
  (annotated-return false))

(defn -uriTooLong [self request graphdata]
  (annotated-return false))

(defn -knownContentType [self request graphdata]
  (annotated-return true))

(defn -validContentHeaders [self request graphdata]
  (annotated-return true))

(defn -validEntityLength [self request graphdata]
  (annotated-return true))

(defn -options [self request graphdata]
  (annotated-return nil))

(defn -allowedMethods [self request graphdata]
  (annotated-return #{:get :head}))

(defn -deleteResource [self request graphdata]
  (annotated-return false))

(defn -deleteCompleted [self request graphdata]
  (annotated-return false))

(defn -postIsCreate [self request graphdata]
  (annotated-return false))

(defn -createPath [self request graphdata]
  (annotated-return false))

(defn -processPost [self request graphdata]
  (annotated-return false))

(defn -contentTypesProvided [self request graphdata]
  (annotated-return {}))

(defn -contentTypesAccepted [self request graphdata]
  (annotated-return {}))

(defn -charsetsProvided [self request graphdata]
  (annotated-return {}))

(defn -encodingsProvided [self request graphdata]
  (annotated-return {}))

(defn -variances [self request graphdata]
  (annotated-return {}))

(defn -conflict [self request graphdata]
  (annotated-return false))

(defn -multipleChoices [self request graphdata]
  (annotated-return false))

(defn -previouslyExisted [self request graphdata]
  (annotated-return false))

(defn -movedPermantently [self request graphdata]
  (annotated-return false))

(defn -lastModified [self request graphdata]
  (annotated-return nil))

(defn -expires [self request graphdata]
  (annotated-return nil))

(defn -generateETag [self request graphdata]
  (annotated-return nil))

(defn -finishRequest [self request graphdata]
  (annotated-return true))
