(ns leviathan-synth.web
  (:require [compojure.core :refer [defroutes GET]]
            [compojure.route :refer [files resources]]))

(defn index [req]
  {:status  200
   :headers {"Content-Type" "text/html"}
   :body    "Hello from Compojure!"})

(defroutes app
  (GET "/" [] index)
  (resources "/")
  (files "/target" {:root "target"}))
