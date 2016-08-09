(ns leviathan-synth.client
  (:require [domina :refer [by-id append!]]
            [domina.css :refer [sel]]
            [domina.events :refer [listen!]]
            [cljs-http.client :as http]
            [cljs.core.async :refer [<!]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(defn log [mess]
  (.log js/console mess))

(defn get-sentence []
  (.-value (by-id "words")))

(def render-index (atom 0))

(defn insert-rendered-audio [endpoint payload]
  (go (let [response (<! (http/post endpoint {:json-params payload}))
            html (str "<p>" (:text payload) "</p>"
                      "<audio id='audio" (inc @render-index) "' "
                        "src='/" (:body response) "' "
                        "autoplay controls='controls'>"
                      "</audio>"
                      "<div class='key-tip'>Press " (inc @render-index) " to trigger</div>")]
        (swap! render-index inc)
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

(def ascii-numbers (range 49 57))
(defn keycode->number [keycode] (- keycode (dec (first ascii-numbers))))

(listen! (by-id "send") :click (fn [_] (insert-rendered-audio "/render" {:text (get-sentence)})))
(listen! (sel "body") :keypress (fn [e]
                                  (let [keycode (:keyCode e)]
                                    (when (some #{keycode} ascii-numbers)
                                      (.play (by-id (str "audio" (keycode->number keycode))))))))
