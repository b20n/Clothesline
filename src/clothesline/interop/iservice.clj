(ns clothesline.interop.iservice
  (:import clothesline.interop.nodetest.TestResult))


(gen-interface
  :name clothesline.interop.IService
  :methods [
            [resourceExists
             [clojure.lang.IPersistentMap clojure.lang.IPersistentMap] clothesline.interop.nodetest.TestResult]
            [serviceAvailable
             [clojure.lang.IPersistentMap clojure.lang.IPersistentMap] clothesline.interop.nodetest.TestResult]
            [authorized
             [clojure.lang.IPersistentMap clojure.lang.IPersistentMap] clothesline.interop.nodetest.TestResult]
            [forbidden
             [clojure.lang.IPersistentMap clojure.lang.IPersistentMap] clothesline.interop.nodetest.TestResult]
            [allowMissingPost
             [clojure.lang.IPersistentMap clojure.lang.IPersistentMap] clothesline.interop.nodetest.TestResult]
            [malformedRequest
             [clojure.lang.IPersistentMap clojure.lang.IPersistentMap] clothesline.interop.nodetest.TestResult]
            [uriTooLong
             [clojure.lang.IPersistentMap clojure.lang.IPersistentMap] clothesline.interop.nodetest.TestResult]
            [knownContentType
             [clojure.lang.IPersistentMap clojure.lang.IPersistentMap] clothesline.interop.nodetest.TestResult]
            [validEntityLength
             [clojure.lang.IPersistentMap clojure.lang.IPersistentMap] clothesline.interop.nodetest.TestResult]
            [validContentHeaders
             [clojure.lang.IPersistentMap clojure.lang.IPersistentMap] clothesline.interop.nodetest.TestResult]
            [options
             [clojure.lang.IPersistentMap clojure.lang.IPersistentMap] clothesline.interop.nodetest.TestResult]
            [allowedMethods
             [clojure.lang.IPersistentMap clojure.lang.IPersistentMap] clothesline.interop.nodetest.TestResult]
            [deleteResource
             [clojure.lang.IPersistentMap clojure.lang.IPersistentMap] clothesline.interop.nodetest.TestResult]
            [deleteCompleted
             [clojure.lang.IPersistentMap clojure.lang.IPersistentMap] clothesline.interop.nodetest.TestResult]
            [postIsCreate
             [clojure.lang.IPersistentMap clojure.lang.IPersistentMap] clothesline.interop.nodetest.TestResult]
            [createPath
             [clojure.lang.IPersistentMap clojure.lang.IPersistentMap] clothesline.interop.nodetest.TestResult]
            [processPost
             [clojure.lang.IPersistentMap clojure.lang.IPersistentMap] clothesline.interop.nodetest.TestResult]
            [contentTypesProvided
             [clojure.lang.IPersistentMap clojure.lang.IPersistentMap] clothesline.interop.nodetest.TestResult]
            [contentTypesAccepted
             [clojure.lang.IPersistentMap clojure.lang.IPersistentMap] clothesline.interop.nodetest.TestResult]
            [charsetsProvided
             [clojure.lang.IPersistentMap clojure.lang.IPersistentMap] clothesline.interop.nodetest.TestResult]
            [encodingsProvided
             [clojure.lang.IPersistentMap clojure.lang.IPersistentMap] clothesline.interop.nodetest.TestResult]
            [variances
             [clojure.lang.IPersistentMap clojure.lang.IPersistentMap] clothesline.interop.nodetest.TestResult]
            [conflict
             [clojure.lang.IPersistentMap clojure.lang.IPersistentMap] clothesline.interop.nodetest.TestResult]
            [multipleChoices
             [clojure.lang.IPersistentMap clojure.lang.IPersistentMap] clothesline.interop.nodetest.TestResult]
            [previouslyExisted
             [clojure.lang.IPersistentMap clojure.lang.IPersistentMap] clothesline.interop.nodetest.TestResult]
            [movedPermanently
             [clojure.lang.IPersistentMap clojure.lang.IPersistentMap] clothesline.interop.nodetest.TestResult]
            [movedTemporarily
             [clojure.lang.IPersistentMap clojure.lang.IPersistentMap] clothesline.interop.nodetest.TestResult]
            [lastModified
             [clojure.lang.IPersistentMap clojure.lang.IPersistentMap] clothesline.interop.nodetest.TestResult]
            [expires
             [clojure.lang.IPersistentMap clojure.lang.IPersistentMap] clothesline.interop.nodetest.TestResult]
            [generateETag
             [clojure.lang.IPersistentMap clojure.lang.IPersistentMap] clothesline.interop.nodetest.TestResult]
            [finishRequest
             [clojure.lang.IPersistentMap clojure.lang.IPersistentMap] clothesline.interop.nodetest.TestResult]])
