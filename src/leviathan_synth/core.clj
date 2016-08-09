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

(defn show-default-index-words [] (show-words word-index))

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

(defn md5
  "Hashes the string with MD5"
  [s]
  (->> (-> (java.security.MessageDigest/getInstance "md5")
           (.digest (.getBytes s "UTF-8")))
       (map #(format "%02x" %))
       (apply str)))

(defn render-synth-to-file
  "Renders the output to 'say' to a file"
  [phrase]
  (let [path (str "/tmp/" (md5 phrase) ".wav")
        result (sh "say" "-o" path "--data-format=LEF32@44100" "--channels=2" phrase)]
    (if (zero? (:exit result))
      path
      (:err result))))

(defn get-sound-path
  [index word]
  (if (contains? @index word)
    (random-version word index)
    (render-synth-to-file word)))

(defn save-sentence-to-wav
  "Saves a sampled sentence to a file"
  [sentence path]
  (println (str "Saving sentence: " sentence " to path " path))
  (let [index word-index
        words (split-into-indexed sentence index)
        samplerate 44100
        combined-sample (->> words
                             (map #(get-sound-path index %))
                             (map read-sound)
                             (reduce append))]
    (save combined-sample path samplerate)
    path))

(defn wav->mp4a
  [input-file-path output-folder]
  (let [output-path (str output-folder (md5 input-file-path) ".mp4a")
        result (sh "afconvert" "-f" "mp4f" "-d" "aac" input-file-path output-path)]
    (if (zero? (:exit result))
      output-path
      (:err result))))

;; (speak (join with-spaces (show-words word-index)))
;; (save-sentence-to-wav (join with-spaces (show-words word-index)) "output.wav")
