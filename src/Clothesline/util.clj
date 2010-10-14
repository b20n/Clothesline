(ns clothesline.util)

(defn get-with-key [map key]
  (when-let [v (get map key)]
    [key v]))

(defn assoc-if
  ([col key value test]
      (if test
        (assoc col key value)
        col))
  ([col key value] (assoc-if col key value value)))