(ns leviathan-synth.web
  (:require [compojure.core :refer [defroutes GET POST]]
            [compojure.route :refer [files resources]]
            [leviathan-synth.core :refer [md5 save-sentence-to-wav show-default-index-words]]
            [clojure.data.json :as json]))

(defn text-to-wave [text]
  (println (str "Rendering text: " text))
  (let [hash (md5 text)
        result (save-sentence-to-wav text (str "resources/public/" hash ".wav"))]
    (println result)
    (str hash ".wav")))

(defroutes app
  (POST "/render" {params :body}
    (let [body (json/read-str (slurp params))]
      (text-to-wave (body "text"))))
  (GET "/words" []
    (json/write-str {:words (show-default-index-words)}))
  (resources "/")
  (files "/target" {:root "target"}))
