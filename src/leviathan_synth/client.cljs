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
  (go (let [response (<! (http/post endpoint {:json-params payload}))]
        (append! (by-id "results") (str "<audio src='/" (:body response) "' autoplay></audio>")))))

(listen! (by-id "send") :click (fn [_] (insert-rendered-audio "/render" {:text (get-sentence)})))
