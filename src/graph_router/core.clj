(ns graph-router.core 
	(:require [graph-router.dispatch :as dispatch]
			  [graph-router.type :refer :all]
			  [graph-router.graph :as gr])
	(:gen-class))

(def dispatch dispatch/dispatch)

(defn- process-with 
	"Expand `with` form."
	[k f m]
	(let [k' (if (nil? f)
				k
				[Attribute k f])]
		(if (nil? m)
			k' 
			[Weave k' m])))

(defmacro named-fn 
	"Convert (fn []) functions into a (defn symbol []) to allow reflection on arity count.
	Don't pass through #() funcitons."
	[f]
	(let [symbol-fn (and (symbol? f) (ifn? f))
		  list-fn (and (list? f) (= 'fn (first f)))]
		(or (symbol? symbol-fn) 
		  	(if list-fn (list 'defn (symbol (str (java.util.UUID/randomUUID))) (rest f))))))

(defn- arg-length 
	"Takes a vecor of args and returns minimum required arity."
	[args]
	(let [amp-index (first (keep-indexed #(if (= %2 '&) %1) args))
		  args' (if amp-index 
		  			(subvec args 0 amp-index)
		  			args)]
		  [(if amp-index >= =) (count args')]))

(defn- correct-args 
	"Check if arglists is the correct arity."
	[arglists arity]
	(->> arglists
		(map arg-length)
		(reduce (fn [ok [pred len]]
					(or ok (pred arity len))) false)))

(defmacro with
	"Alias a keyword with another function and or register weave functions.
	See https://github.com/LockedOn/graph-router for more information."
	[k f & [m & _]]
	(let [f' (if (map? f) m f)
		  m' (if (map? f) f m)
		  f# (macroexpand `(named-fn ~f'))]
		  (process-with k f# m')))
