(ns leviathan-synth.web
  (:require [compojure.core :refer [defroutes GET POST]]
            [compojure.route :refer [files resources]]
            [leviathan-synth.core :refer [md5 save-sentence-to-wav show-default-index-words]]
            [clojure.data.json :as json]
            [org.httpkit.client :as http]))

(defn text-to-wave [text]
  (println (str "Rendering text: " text))
  (let [hash (md5 text)
        result (save-sentence-to-wav text (str "resources/public/" hash ".wav"))]
    (println result)
    (str hash ".wav")))

(defn get-suggestions [text]
  (println (str "Getting suggestions for:" text))
  (let [sanitised-text (http/url-encode text)
        endpoint (str "http://google.com/complete/search?client=chrome&q=" sanitised-text)
        {:keys [status headers body error] :as resp} @(http/get endpoint)]
    (if error
      (println "Failed, exception: " error)
      body)))

(defroutes app
  (POST "/render" {params :body}
    (let [body (json/read-str (slurp params))]
      (text-to-wave (body "text"))))
  (GET "/words" []
    (json/write-str {:words (show-default-index-words)}))
  (POST "/suggestions" {params :body}
    (let [body (json/read-str (slurp params))]
      (get-suggestions (body "text"))))
  (resources "/")
  (files "/target" {:root "target"}))
