(ns leviathan-synth.client
  (:require [domina :refer [by-id]]
            [domina.events :refer [listen!]]))

(defn log [mess]
  (.log js/console mess))

(listen! (by-id "words") :click (log "stało się"))
;; (goog.events/listen (dom/getWindow) (.-KEY events/EventType) #(js/alert "asda"))
