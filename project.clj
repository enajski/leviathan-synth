(defproject leviathan-synth "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0-alpha7"]
                 [org.craigandera/dynne "0.4.1"]
                 [org.clojure/clojurescript "1.9.93"]
                 [http-kit "2.2.0"]
                 [compojure "1.5.1"]
                 [com.stuartsierra/component "0.3.1"]
                 [domina "1.0.3"]
                 [cljs-http "0.1.41"]
                 [org.clojure/data.json "0.2.6"]
                 [reagent "0.6.0-SNAPSHOT"]]
  :main leviathan-synth.system
  :profiles {:dev {:plugins [[lein-cljsbuild "1.1.3"]
                             [lein-figwheel "0.5.4-7"]]
                   :dependencies [[reloaded.repl "0.2.2"]]
                   :source-paths ["dev"]
                   :cljsbuild {:builds [{:id "dev"
                                         :source-paths ["src" "dev"]
                                         :figwheel true
                                         :compiler {:main "leviathan-synth/client"
                                                    :output-to "target/app.js"
                                                    :output-dir "target/out"
                                                    :optimizations :none
                                                    :recompile-dependents true
                                                    :source-map true}}]}}})
