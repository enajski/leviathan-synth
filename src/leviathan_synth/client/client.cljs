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

(defn dummy-samples [size]
  (into []
        (for [i (range size)]
          {:id (inc i)})))

(defonce app-state (r/atom {:available-words []
                            :samples (dummy-samples 16)}))

(defn AvailableWord [word]
  [:li word])

(defn AvailableWords [app-state]
  [:div
   [:h3 "Available words:"]
   [:ul (for [word (sort (:available-words @app-state))]
        ^{:key word} [AvailableWord word])]])

(defn SamplerButton [sample]
  [:td.sampler-button
   [:p (:text sample)]
   (when (:source sample)
     [:audio {:id (str "audio" (:id sample))
              :src (:source sample)
              :autoPlay "autoplay"}])
   [:p.key-tip (:id sample)]])

(defn SamplerRow [sample-row]
  [:tr
   (for [sample sample-row]
     ^{:key (:id sample)} [SamplerButton sample])])

(defn Sampler [app-state]
  [:table {:id "sampler"}
   (for [sample-row (partition 4 (:samples @app-state))]
     ^{:key (:id (first sample-row))} [SamplerRow sample-row])])

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
            free-slot (count (filter #(:source %) (:samples @app-state)))
            sample {:id (inc free-slot)
                    :source (:body response)
                    :text (:text payload)}]
        (swap! app-state update-in [:samples] assoc free-slot sample))))

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
