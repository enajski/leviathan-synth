(ns user
  (:require [reloaded.repl :refer [system reset stop]]
            [leviathan-synth.system]))

(reloaded.repl/set-init! #'leviathan-synth.system/create-system)
