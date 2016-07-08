(ns leviathan-synth.index
  (:require [clojure.java.io :as io]))

(defn build-index-entry [file]
  (let [k (first (re-seq (re-pattern "^[\\w\\s]*\\b") (.getName file)))
        v [(str file)]]
    (hash-map k v)))

(defn build-index [path]
  (->> (io/file path)
       (file-seq)
       (filter #(not (.isDirectory %)))
       (filter #(re-seq (re-pattern ".wav") (str %)))
       (map build-index-entry)
       (apply merge-with concat)))

