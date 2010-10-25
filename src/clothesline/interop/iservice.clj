(ns clothesline.interop.iservice
  (:import clothesline.interop.nodetest.TestResult))


(gen-interface
  :name clothesline.interop.IService
  :methods [
            [resourceExists
             [clojure.lang.APersistentMap clojure.lang.APersistentMap] clothesline.interop.nodetest.TestResult]
            [serviceAvailable
             [clojure.lang.APersistentMap clojure.lang.APersistentMap] clothesline.interop.nodetest.TestResult]
            [authorized
             [clojure.lang.APersistentMap clojure.lang.APersistentMap] clothesline.interop.nodetest.TestResult]
            [forbidden
             [clojure.lang.APersistentMap clojure.lang.APersistentMap] clothesline.interop.nodetest.TestResult]
            [allowMissingPost
             [clojure.lang.APersistentMap clojure.lang.APersistentMap] clothesline.interop.nodetest.TestResult]
            [malformedRequest
             [clojure.lang.APersistentMap clojure.lang.APersistentMap] clothesline.interop.nodetest.TestResult]
            [uriTooLong
             [clojure.lang.APersistentMap clojure.lang.APersistentMap] clothesline.interop.nodetest.TestResult]
            [knownContentType
             [clojure.lang.APersistentMap clojure.lang.APersistentMap] clothesline.interop.nodetest.TestResult]
            [validContentHeaders
             [clojure.lang.APersistentMap clojure.lang.APersistentMap] clothesline.interop.nodetest.TestResult]
            [options
             [clojure.lang.APersistentMap clojure.lang.APersistentMap] clothesline.interop.nodetest.TestResult]
            [allowedMethods
             [clojure.lang.APersistentMap clojure.lang.APersistentMap] clothesline.interop.nodetest.TestResult]
            [deleteResource
             [clojure.lang.APersistentMap clojure.lang.APersistentMap] clothesline.interop.nodetest.TestResult]
            [deleteCompleted
             [clojure.lang.APersistentMap clojure.lang.APersistentMap] clothesline.interop.nodetest.TestResult]
            [postIsCreate
             [clojure.lang.APersistentMap clojure.lang.APersistentMap] clothesline.interop.nodetest.TestResult]
            [createPath
             [clojure.lang.APersistentMap clojure.lang.APersistentMap] clothesline.interop.nodetest.TestResult]
            [processPost
             [clojure.lang.APersistentMap clojure.lang.APersistentMap] clothesline.interop.nodetest.TestResult]
            [contentTypesProvided
             [clojure.lang.APersistentMap clojure.lang.APersistentMap] clothesline.interop.nodetest.TestResult]
            [contentTypesAccepted
             [clojure.lang.APersistentMap clojure.lang.APersistentMap] clothesline.interop.nodetest.TestResult]
            [charsetsProvided
             [clojure.lang.APersistentMap clojure.lang.APersistentMap] clothesline.interop.nodetest.TestResult]
            [encodingsProvided
             [clojure.lang.APersistentMap clojure.lang.APersistentMap] clothesline.interop.nodetest.TestResult]
            [variances
             [clojure.lang.APersistentMap clojure.lang.APersistentMap] clothesline.interop.nodetest.TestResult]
            [conflict
             [clojure.lang.APersistentMap clojure.lang.APersistentMap] clothesline.interop.nodetest.TestResult]
            [multipleChoices
             [clojure.lang.APersistentMap clojure.lang.APersistentMap] clothesline.interop.nodetest.TestResult]
            [previouslyExisted
             [clojure.lang.APersistentMap clojure.lang.APersistentMap] clothesline.interop.nodetest.TestResult]
            [movedPermantently
             [clojure.lang.APersistentMap clojure.lang.APersistentMap] clothesline.interop.nodetest.TestResult]
            [lastModified
             [clojure.lang.APersistentMap clojure.lang.APersistentMap] clothesline.interop.nodetest.TestResult]
            [expires
             [clojure.lang.APersistentMap clojure.lang.APersistentMap] clothesline.interop.nodetest.TestResult]
            [generateETag
             [clojure.lang.APersistentMap clojure.lang.APersistentMap] clothesline.interop.nodetest.TestResult]
            [finishRquest
             [clojure.lang.APersistentMap clojure.lang.APersistentMap] clothesline.interop.nodetest.TestResult]])