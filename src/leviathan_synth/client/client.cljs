(ns leviathan-synth.client
  (:require [domina :refer [by-id]]
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

(defonce app-state (r/atom {:available-words []
                            :samples []}))

(defn AvailableWord [word]
  [:li word])

(defn AvailableWords [app-state]
  [:div
   [:h3 "Available words:"]
   [:ul (for [word (sort (:available-words @app-state))]
        ^{:key word} [AvailableWord word])]])

(defn SamplerButton [sample]
  [:div
   [:p (:text sample)]
   [:audio {:id (str "audio" (:id sample))
            :src (:source sample)
            :controls "controls"
            :autoPlay "autoplay"}]
   [:p.key-tip "Press " (:id sample) " to trigger"]])

(defn Sampler [app-state]
  [:div {:id "sampler"}
   (for [sample (:samples @app-state)]
     ^{:key (:id sample)} [SamplerButton sample])])

(defn TextInput [app-state]
  [:div
   [:input {:type "text" :id "words"}]
   [:button {:id "send"
             :on-click #(insert-rendered-audio "/render" {:text (get-sentence)} app-state)}
   "Send"]])

(defn MainComponent []
  [:div
   [TextInput app-state]
   [Sampler app-state]
   [AvailableWords app-state]])

(r/render [MainComponent] (js/document.getElementById "main"))

(defn insert-rendered-audio [endpoint payload app-state]
  (go (let [response (<! (http/post endpoint {:json-params payload}))
            sample {:id (inc (count (:samples @app-state)))
                    :source (:body response)
                    :text (:text payload)}]
        (swap! app-state update-in [:samples] conj sample))))

(defn get-available-words []
  (go (let [response (<! (http/get "/words"))
            words (-> response
                      :body
                      JSON/parse
                      .-words)]
        (swap! app-state assoc :available-words words))))

(get-available-words)

(def ascii-numbers (range 49 57))
(defn keycode->number [keycode] (- keycode (dec (first ascii-numbers))))

(listen! (sel "body") :keypress (fn [e]
                                  (let [keycode (:keyCode e)]
                                    (when (some #{keycode} ascii-numbers)
                                      (let [audio (by-id (str "audio" (keycode->number keycode)))]
                                        (set! (.-currentTime audio) 0)
                                        (.play audio))))))
