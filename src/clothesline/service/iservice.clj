(ns clothesline.service.iservice)
(gen-interface
  :name clothesline.service.IService
  :methods [
            [resourceExists
             [clojure.lang.APersistentMap clojure.lang.APersistentMap] boolean]
            [serviceAvailable
             [clojure.lang.APersistentMap clojure.lang.APersistentMap] boolean]
            [authorized
             [clojure.lang.APersistentMap clojure.lang.APersistentMap] boolean]
            [forbidden
             [clojure.lang.APersistentMap clojure.lang.APersistentMap] boolean]
            [allowMissingPost
             [clojure.lang.APersistentMap clojure.lang.APersistentMap] boolean]
            [malformedRequest
             [clojure.lang.APersistentMap clojure.lang.APersistentMap] boolean]
            [uriTooLong
             [clojure.lang.APersistentMap clojure.lang.APersistentMap] boolean]
            [knownContentType
             [clojure.lang.APersistentMap clojure.lang.APersistentMap] boolean]
            [validContentHeaders
             [clojure.lang.APersistentMap clojure.lang.APersistentMap] boolean]
            [options
             [clojure.lang.APersistentMap clojure.lang.APersistentMap] clojure.lang.APersistentMap]
            [allowedMethods
             [clojure.lang.APersistentMap clojure.lang.APersistentMap] clojure.lang.APersistentMap]
            [deleteResource
             [clojure.lang.APersistentMap clojure.lang.APersistentMap] boolean]
            [deleteCompleted
             [clojure.lang.APersistentMap clojure.lang.APersistentMap] boolean]
            [postIsCreate
             [clojure.lang.APersistentMap clojure.lang.APersistentMap] boolean]
            [createPath
             [clojure.lang.APersistentMap clojure.lang.APersistentMap] String]
            [processPost
             [clojure.lang.APersistentMap clojure.lang.APersistentMap] boolean]
            [contentTypesProvided
             [clojure.lang.APersistentMap clojure.lang.APersistentMap] clojure.lang.APersistentMap]
            [contentTypesAccepted
             [clojure.lang.APersistentMap clojure.lang.APersistentMap] clojure.lang.APersistentMap]
            [charsetsProvided
             [clojure.lang.APersistentMap clojure.lang.APersistentMap] clojure.lang.APersistentMap]
            [encodingsProvided
             [clojure.lang.APersistentMap clojure.lang.APersistentMap] clojure.lang.APersistentMap]
            [variances
             [clojure.lang.APersistentMap clojure.lang.APersistentMap] clojure.lang.APersistentMap]
            [conflict
             [clojure.lang.APersistentMap clojure.lang.APersistentMap] boolean]
            [multipleChoices
             [clojure.lang.APersistentMap clojure.lang.APersistentMap] boolean]
            [previouslyExisted
             [clojure.lang.APersistentMap clojure.lang.APersistentMap] boolean]
            [movedPermantently
             [clojure.lang.APersistentMap clojure.lang.APersistentMap] boolean]
            [lastModified
             [clojure.lang.APersistentMap clojure.lang.APersistentMap] java.util.Date]
            [expires
             [clojure.lang.APersistentMap clojure.lang.APersistentMap] java.util.Date]
            [generateETag
             [clojure.lang.APersistentMap clojure.lang.APersistentMap] String]
            [finishRquest
             [clojure.lang.APersistentMap clojure.lang.APersistentMap] boolean]])