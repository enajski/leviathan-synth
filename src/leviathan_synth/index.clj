(ns leviathan-synth.index
  (:require [clojure.java.io :as io]
            [clojure.spec :as s]))

(defn get-phrase-from-filename
  "Extracts the words preceding the version
   and file extension"
  [filename]
  (->> filename
       (re-seq (re-pattern "^[\\w\\s]*\\b"))
       first))

(s/def ::index (s/map-of string? vector?))

(defn build-index-entry
  [file]
  (let [filename (.getName file)
        k (get-phrase-from-filename filename)
        v [(str file)]]
    (hash-map k v)))

(defn validate!
  [index]
  (if (s/valid? ::index index)
    index
    (throw (ex-info "Invalid input" (s/explain-data ::index index)))))

(defn get-files
  [path]
  (file-seq (io/file path)))

(defn wav?
  [file]
  (re-seq (re-pattern ".wav") (str file)))

(defn build-index
  [path]
  (->> (get-files path)
       (filter #(not (.isDirectory %)))
       (filter wav?) 
       (map build-index-entry)
       (apply merge-with concat)))

