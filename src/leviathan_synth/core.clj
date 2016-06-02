(ns leviathan-synth.core
  (:require [clojure.java.io :as io]
            [clojure.java.shell :refer [sh]]))

(defn build-index [path]
  (->> (io/file path)
       (file-seq)
       (filter #(not (.isDirectory %)))
       (filter #(re-seq (re-pattern ".wav") (str %)))
       (map #(hash-map (first (re-seq (re-pattern "^[\\w\\s]*\\b") (.getName %))) [(str %)]))
       (apply merge-with concat)))

(def word-index (atom (build-index "/Users/dev/experiments/leviathan-synth/media")))

(defn speak [^String sentence]
  (let [words (clojure.string/split sentence (re-pattern " "))]
    (map (fn [word]
           (if (contains? @word-index word)
             (sh "afplay" (rand-nth (get @word-index word)))
             (sh "say" word))
           word) words)))
