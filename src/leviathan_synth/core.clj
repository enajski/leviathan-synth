(ns leviathan-synth.core
  (:require [leviathan-synth.index :as media-library]
            [clojure.java.shell :refer [sh]]
            [clojure.spec :as s]
            [clojure.string :refer [split join trim replace-first]]))

(def media-path (atom (or (System/getenv "MEDIAPATH")
                          "/Users/dev/experiments/leviathan-synth/media")))
(def word-index (atom (media-library/build-index @media-path)))

(defn show-words [] (keys @word-index))

(def with-spaces (re-pattern " "))

(defn find-match [sentence index]
  (loop [words (split sentence with-spaces)]
    (if (empty? words)
      nil
      (let [current-phrase (join with-spaces words)]
        (if (or (contains? @index current-phrase) (= 1 (count words)))
          current-phrase
          (recur (drop-last words)))))))

(defn split-into-indexed
  "Splits the given sentence into the longest possible phrases
   found in the word index"
  [sentence index]
  (loop [phrase sentence
         output []]
    (if (empty? phrase)
      output
      (if-let [match (find-match phrase index)]
        (recur (trim (replace-first phrase match "")) (conj output match))))))

(defn speak [^String sentence]
  (let [words (split-into-indexed sentence word-index)] 
    (map (fn [word]
           (if (contains? @word-index word)
             (sh "afplay" (rand-nth (get @word-index word)))
             (sh "say" word))
           word) words)))

