(ns graph-router.graph
	(:require [schema.core :as s]
			  [graph-router.type :refer :all]))

(defn- attribute [k f]
	(if (nil? f)
		k
		[Attribute k f]))

(defn- weave-with [k f]
	(if (nil? f)
		k
		[Weave k f]))

(defn with
	"Alias a keyword with another function and or register weave functions.
	See https://github.com/LockedOn/graph-router for more information."
	([k x]
		(if (map? x)
			(with k nil x)
			(with k x nil)))
	([k f m] 
			(-> k 
				 (attribute f)
				 (weave-with m))))

(declare parse 
		 parse-map 
		 parse-vector 
		 parse-keyword 
		 parse-function
		 attribute-list-schema
		 attribute-schema
		 weave-schema
		 context-schema
		 context-list-schema
		 function-schema
		 recursive-schema
		 graph-schema)

(def recursive-schema
	{:type (s/eq Recursive)
	 :value (s/pred var? "var?")})

(def attribute-schema
	{:type (s/eq Attribute)
	 :value s/Keyword 
	 :fn (s/pred ifn? "ifn?")})

(def function-schema
	{:type (s/eq Function)
	 :value s/Symbol 
	 :fn (s/pred ifn? "ifn?")})

(def weave-schema
	{:type (s/eq Weave)
	 :value attribute-schema
	 :functions (s/both (s/pred seq "seq") [function-schema])})

(def attribute-list-schema 
	(s/either recursive-schema (s/both (s/pred seq "seq") [(s/either attribute-schema (s/recursive #'context-list-schema))])))

(def context-schema
	{:type (s/eq Context)
	 :value (s/either attribute-schema weave-schema)
	 :attributes attribute-list-schema})

(def context-list-schema
	{:type (s/eq ContextList)
	 :value (s/both (s/pred seq "seq") [context-schema])})

(def graph-schema
	(s/either context-list-schema context-schema attribute-list-schema weave-schema))

(defn- parse-vector 
	[form] 
	(let [[t k f] form]
		(cond (= Attribute t)
			  (->Attrib Attribute k f)

			  (= Weave t)
			  (->Wea Weave (parse k) (map parse-function f))

			  :else 
			  (map parse form))))

(defn- parse-keyword 
	[form] 
	(->Attrib Attribute form form))

(defn- parse-function 
	[form]
	(let [[s f] form]
		(->Attrib Function s f)))

(defn- parse-map 
	[form]
	(->ContList ContextList
		 (map (fn [k]
				(->Cont Context (parse k) (parse (get form k)))) 
			(keys form))))

(def validate 
	(memoize 
		(fn [schema form] 
			(s/validate schema (parse form)))))

(defn parse 
	([form schema]
		(validate schema form))
	([form]
		(cond (vector? form)
			  (parse-vector form)

			  (map? form)
			  (parse-map form)

			  (keyword? form)
			  (parse-keyword form)

			  (var? form)
			  (->ContList Recursive form))))
