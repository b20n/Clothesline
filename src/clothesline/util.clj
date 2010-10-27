(ns clothesline.util)

(defn get-with-key [map key]
  (when-let [v (get map key)]
    [key v]))

(defn take-until [pred col]
  (take-while #(not (pred %)) col))

(defn assoc-if
  ([col key value test]
      (if test
        (assoc col key value)
        col))
  ([col key value] (assoc-if col key value value)))

(defn map-keys
  "Produces a new map where all keys have been transformed into f(k)."
  ([f col _] ; Preserving order is currently ignored
     (reduce (fn [v [key val]] (assoc v (f key) val))
             {}
             col))
  ([f col] (map-keys f col false)))
