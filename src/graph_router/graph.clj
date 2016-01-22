(ns graph-router.graph
	(:require [schema.core :as s]
			  [graph-router.type :refer :all]))
			
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

(defn f? [x]
	(and (ifn? x) (not (map? x))))

(def recursive-schema
	{:type (s/eq Recursive)
	 :value (s/pred var? "var?")})

(def attribute-schema
	{:type (s/eq Attribute)
	 :value s/Keyword 
	 :fn (s/pred f? "ifn?")})

(def function-schema
	{:type (s/eq Function)
	 :value s/Symbol 
	 :fn (s/pred f? "ifn?")})

(def weave-schema
	{:type (s/eq Weave)
	 :value attribute-schema
	 :functions (s/conditional seq 
			 				   [function-schema])})

(def attribute-list-schema 
	(s/if #(= (:type %) Recursive) 
		  recursive-schema 
		  (s/conditional seq 
				  		 [(s/if #(= (:type %) Attribute) 
				  				attribute-schema 
				  				(s/recursive #'context-list-schema))])))

(def context-schema
	{:type (s/eq Context)
	 :value (s/if #(= (:type %) Weave) 
	 			  weave-schema
	 			  attribute-schema)
	 :attributes attribute-list-schema})

(def context-list-schema
	{:type (s/eq ContextList)
	 :value (s/conditional seq 
	 			  		  [context-schema])})

(def graph-schema
	(s/conditional  #(= (:type %) ContextList)
					context-list-schema 

					#(= (:type %) Context)
					context-schema 

					#(= (:type %) Weave)
					weave-schema

					:else
					attribute-list-schema))

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
		(fn [schema-validator form] 
			(schema-validator (parse form)))))

(def validator 
	(memoize 
		#(s/validator %)))

(defn parse 
	([form schema]
		(validate (validator schema) form))
	([form]
		(cond (vector? form)
			  (parse-vector form)

			  (map? form)
			  (parse-map form)

			  (keyword? form)
			  (parse-keyword form)

			  (var? form)
			  (->ContList Recursive form))))
