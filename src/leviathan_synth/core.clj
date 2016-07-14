(ns leviathan-synth.core
  (:require [leviathan-synth.index :as media-library]
            [clojure.java.shell :refer [sh]]
            [clojure.spec :as s]
            [clojure.string :refer [split join trim replace-first]]
            [dynne.sampled-sound :refer [read-sound play append save]]))

(def media-path (atom (or (System/getenv "MEDIAPATH")
                          "/Users/dev/experiments/leviathan-synth/media")))

(def word-index (atom (media-library/build-index @media-path)))

(defn show-words [index] (keys @index))

(def with-spaces (re-pattern " "))

(defn find-match
  "Finds the longest matching string against the index"
  [sentence index]
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

(def read-duration
  "Reference to private read-duration function"
  #'dynne.sampled-sound/read-duration)

(defn seconds->milis
  [seconds]
  (* 1000 seconds))

(defn play-sample
  "Plays sample at path and blocks for its duration"
  [path]
  (let [sound (read-sound path)
        duration (seconds->milis (read-duration path))]
    (do (play sound)
        (Thread/sleep duration))))

(defn play-voice-synth
  "Uses the OS command 'say'"
  [word]
  (sh "say" word))

(defn random-version
  "Fetch a random path for an index entry"
  [word index]
  (rand-nth (get @index word)))

(defn play-sample-or-synth
  "Plays sample if available or falls back to 'say'"
  [word index]
  (do (if (contains? @index word)
        (play-sample (random-version word index))
        (play-voice-synth word))
      word))

(defn speak
  "Says the sentence using available methods"
  [^String sentence]
  (let [index word-index
        words (split-into-indexed sentence index)]
    (map #(play-sample-or-synth % index) words)))

(defn save-sentence-to-wav
  "Saves a sampled sentence to a file"
  [sentence path]
  (let [index word-index
        words (split-into-indexed sentence index)
        samplerate 44100
        combined-sample (->> words
                             (map #(random-version % index))
                             (map read-sound)
                             (reduce append))]
    (save combined-sample path samplerate)))

;; (speak (join with-spaces (show-words word-index)))
;; (save-sentence-to-wav (join with-spaces (show-words word-index)) "output.wav")
