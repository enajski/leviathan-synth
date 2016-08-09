(ns leviathan-synth.client
  (:require [domina :refer [by-id append!]]
            [domina.events :refer [listen!]]
            [cljs-http.client :as http]
            [cljs.core.async :refer [<!]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(defn log [mess]
  (.log js/console mess))

(defn get-sentence []
  (.-value (by-id "words")))

(defn insert-rendered-audio [endpoint payload]
  (go (let [response (<! (http/post endpoint {:json-params payload}))
            html (str "<p>" (:text payload) "</p>"
                      "<audio src='/" (:body response) "'  autoplay controls='controls'></audio>")]
        (append! (by-id "results") html))))

(defn display-available-words []
  (go (let [response (<! (http/get "/words"))
            words (-> response
                      :body
                      JSON/parse
                      .-words)
            html (str "<p>" words "</p>")]
        (append! (by-id "available-words") html))))

(display-available-words)

(listen! (by-id "send") :click (fn [_] (insert-rendered-audio "/render" {:text (get-sentence)})))
