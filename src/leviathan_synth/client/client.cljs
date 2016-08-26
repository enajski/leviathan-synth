(ns leviathan-synth.client
  (:require [domina :refer [by-id add-class! remove-class!]]
            [domina.css :refer [sel]]
            [domina.events :refer [listen!]]
            [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]
            [reagent.core :as r :refer [atom]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(defn log [mess]
  (.log js/console mess))

(defn get-sentence []
  (.-value (by-id "word-input")))

(def keycodes [49   50  51  52
               113 119 101 114
               97  115 100 102
               122 120  99 118])

(def keybindings ["1" "2" "3" "4"
                  "q" "w" "e" "r"
                  "a" "s" "d" "f"
                  "z" "x" "c" "v"])

(def keycode->sample (zipmap keycodes keybindings))

(defn dummy-samples [size]
  (into []
        (for [i (range size)]
          {:id (nth keybindings i)})))

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
  [:td.sampler-button {:id (str "sample-button" (:id sample))
                       :on-click #(trigger-audio (:id sample))}
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
   [:tbody
    (for [sample-row (partition 4 (:samples @app-state))]
      ^{:key (:id (first sample-row))} [SamplerRow sample-row])]])

(defn TextInput [app-state]
  [:div#input-box
   [:input {:type "text"
            :id "word-input"
            :placeholder "Enter sentence"
            :on-key-up (fn [e]
                         (let [keycode (.-keyCode e)]
                           (when (= 13 keycode)
                             (insert-rendered-audio "/render" {:text (get-sentence)} app-state))))}]
   [:button {:id "send"
             :on-click #(insert-rendered-audio "/render" {:text (get-sentence)} app-state)}
   "Add sample"]])

(defn Sidebar [app-state]
  [:div#sidebar
   [TextInput app-state]
   [AvailableWords app-state]])

(defn MainComponent [app-state]
  [:div#mpc
   [Sidebar app-state]
   [Sampler app-state]])

(r/render [MainComponent app-state] (js/document.getElementById "main"))

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

(defn trigger-audio [sample-key]
  (let [audio (by-id (str "audio" sample-key))
        sampler-button (by-id (str "sample-button" sample-key))]
    (when audio
      (set! (.-currentTime audio) 0)
      (.play audio))
    (when sampler-button
      (add-class! sampler-button "active")
      (js/setTimeout #(remove-class! sampler-button "active") 300))))

(listen! (sel "body") :keypress (fn [e]
                                  (let [keycode (:charCode e)]
                                    (when (and (contains? keycode->sample keycode)
                                               (not= "word-input" (.-id (:target e))))
                                      (trigger-audio (get keycode->sample keycode))))))
