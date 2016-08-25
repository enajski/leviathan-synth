(ns leviathan-synth.client
  (:require [domina :refer [by-id append!]]
            [domina.css :refer [sel]]
            [domina.events :refer [listen!]]
            [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]
            [reagent.core :as r :refer [atom]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(defn log [mess]
  (.log js/console mess))

(defn get-sentence []
  (.-value (by-id "words")))

(def render-index (atom 0))

(defonce available-words (r/atom []))

(defn AvailableWord [word]
  [:li word])

(defn AvailableWords [available-words-state]
  [:div
   [:h3 "Available words:"]
   [:ul (for [word (sort @available-words-state)]
        ^{:key word} [AvailableWord word])]])

(defn TextInput []
  [:div
   [:input {:type "text" :id "words"}]
   [:button {:id "send"
             :on-click #(insert-rendered-audio "/render" {:text (get-sentence)})}
   "Send"]])

(defn MainComponent []
  [:div
   [TextInput]
   [AvailableWords available-words]])

(r/render [MainComponent] (js/document.getElementById "react"))

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

(defn get-available-words []
  (go (let [response (<! (http/get "/words"))
            words (-> response
                      :body
                      JSON/parse
                      .-words)]
        (reset! available-words words))))

(get-available-words)

(def ascii-numbers (range 49 57))
(defn keycode->number [keycode] (- keycode (dec (first ascii-numbers))))

(listen! (sel "body") :keypress (fn [e]
                                  (let [keycode (:keyCode e)]
                                    (when (some #{keycode} ascii-numbers)
                                      (let [audio (by-id (str "audio" (keycode->number keycode)))]
                                        (set! (.-currentTime audio) 0)
                                        (.play audio))))))
