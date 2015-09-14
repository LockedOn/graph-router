(ns graph-router.core 
	(:require [graph-router.dispatch :as dispatch]
			  [graph-router.graph :as gr])
	(:gen-class))

(def dispatch dispatch/dispatch)
(def with gr/with)