(ns clothesline.service.iservice)
(gen-interface
  :name clothesline.service.IService
  :methods [
            [resourceExists
             [clothesline.service.IService clojure.lang.APersistentMap clojure.lang.APersistentMap] boolean]
            [serviceAvailable
             [clothesline.service.IService clojure.lang.APersistentMap clojure.lang.APersistentMap] boolean]
            [authorized
             [clothesline.service.IService clojure.lang.APersistentMap clojure.lang.APersistentMap] boolean]
            [forbidden
             [clothesline.service.IService clojure.lang.APersistentMap clojure.lang.APersistentMap] boolean]
            [allowMissingPost
             [clothesline.service.IService clojure.lang.APersistentMap clojure.lang.APersistentMap] boolean]
            [malformedRequest
             [clothesline.service.IService clojure.lang.APersistentMap clojure.lang.APersistentMap] boolean]
            [uriTooLong
             [clothesline.service.IService clojure.lang.APersistentMap clojure.lang.APersistentMap] boolean]
            [knownContentType
             [clothesline.service.IService clojure.lang.APersistentMap clojure.lang.APersistentMap] boolean]
            [validContentHeaders
             [clothesline.service.IService clojure.lang.APersistentMap clojure.lang.APersistentMap] boolean]
            [options
             [clothesline.service.IService clojure.lang.APersistentMap clojure.lang.APersistentMap] clojure.lang.APersistentMap]
            [allowedMethods
             [clothesline.service.IService clojure.lang.APersistentMap clojure.lang.APersistentMap] clojure.lang.APersistentMap]
            [deleteResource
             [clothesline.service.IService clojure.lang.APersistentMap clojure.lang.APersistentMap] boolean]
            [deleteCompleted
             [clothesline.service.IService clojure.lang.APersistentMap clojure.lang.APersistentMap] boolean]
            [postIsCreate
             [clothesline.service.IService clojure.lang.APersistentMap clojure.lang.APersistentMap] boolean]
            [createPath
             [clothesline.service.IService clojure.lang.APersistentMap clojure.lang.APersistentMap] String]
            [processPost
             [clothesline.service.IService clojure.lang.APersistentMap clojure.lang.APersistentMap] boolean]
            [contentTypesProvided
             [clothesline.service.IService clojure.lang.APersistentMap clojure.lang.APersistentMap] clojure.lang.APersistentMap]
            [contentTypesAccepted
             [clothesline.service.IService clojure.lang.APersistentMap clojure.lang.APersistentMap] clojure.lang.APersistentMap]
            [charsetsProvided
             [clothesline.service.IService clojure.lang.APersistentMap clojure.lang.APersistentMap] clojure.lang.APersistentMap]
            [encodingsProvided
             [clothesline.service.IService clojure.lang.APersistentMap clojure.lang.APersistentMap] clojure.lang.APersistentMap]
            [variances
             [clothesline.service.IService clojure.lang.APersistentMap clojure.lang.APersistentMap] clojure.lang.APersistentMap]
            [conflict
             [clothesline.service.IService clojure.lang.APersistentMap clojure.lang.APersistentMap] boolean]
            [multipleChoices
             [clothesline.service.IService clojure.lang.APersistentMap clojure.lang.APersistentMap] boolean]
            [previouslyExisted
             [clothesline.service.IService clojure.lang.APersistentMap clojure.lang.APersistentMap] boolean]
            [movedPermantently
             [clothesline.service.IService clojure.lang.APersistentMap clojure.lang.APersistentMap] boolean]
            [lastModified
             [clothesline.service.IService clojure.lang.APersistentMap clojure.lang.APersistentMap] java.util.Date]
            [expires
             [clothesline.service.IService clojure.lang.APersistentMap clojure.lang.APersistentMap] java.util.Date]
            [generateETag
             [clothesline.service.IService clojure.lang.APersistentMap clojure.lang.APersistentMap] String]
            [finishRquest
             [clothesline.service.IService clojure.lang.APersistentMap clojure.lang.APersistentMap] boolean]])