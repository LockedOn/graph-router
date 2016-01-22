(ns graph-router.query
	(:require [schema.core :as s]
			  [graph-router.type :refer :all]))

(declare parse-weave
		 parse-function
		 parse-keyword
		 parse-map
		 parse
		 attribute-schema
		 function-schema
		 weave-schema
		 attribute-list-schema
		 context-schema
		 query-schema)

(def attribute-schema
	{:type (s/eq Attribute)
	 :value s/Keyword 
	 :args [s/Any]})

(def function-schema
	{:type (s/eq Function)
	 :value s/Symbol
	 :args [s/Any]})

(def weave-schema
	{:type (s/eq Weave)
	 :value attribute-schema
	 :functions (s/conditional seq [function-schema])})

(def attribute-list-schema
	(s/conditional seq [(s/conditional #(= (:type %) Attribute)
									   attribute-schema 

									   #(= (:type %) Weave)
									   weave-schema 

									   #(= (:type %) Context)
									   (s/recursive #'context-schema))]))

(def context-schema
	{:type (s/eq Context)
	 :value (s/if #(= (:type %) Attribute) 
	 			  attribute-schema 
	 			  weave-schema)
	 :attributes attribute-list-schema})

(def query-schema
	(s/conditional  #(= (:type %) Attribute)
					attribute-schema 

					#(= (:type %) Function)
					function-schema 

					#(= (:type %) Weave)
					weave-schema 

					#(= (:type %) Context)
					context-schema

					:else
					attribute-list-schema))

(defn- parse-weave 
	[fun]
	(let [[value & args] (:args fun)]
		(->Wea Weave (parse value) (map parse args))))

(defn- parse-list 
	[form]
	(let [value (first form)
		  fun (->Func Function value (rest form))]

		(cond (= '->> value)
			  (parse-weave fun)

			  (keyword? value)
			  (assoc fun :type Attribute)

			  :else
		  	  fun)))

(defn- parse-keyword 
	[form]
	(->Func Attribute form '()))

(defn- parse-map 
	[form]
	(let [[context & _] (keys form)]
		(->Cont Context (parse context) (parse (get form context)))))

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
		(cond (list? form)
			  (parse-list form)

			  (keyword? form)
			  (parse-keyword form)

			  (vector? form)
			  (map parse form)

			  (map? form)
			  (parse-map form))))
