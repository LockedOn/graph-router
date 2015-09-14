(ns graph-router.dispatch
	(:require [schema.core :as s]
			  [graph-router.type :refer :all]
			  [graph-router.graph :as gr]
			  [graph-router.query :as qu]))

(declare step)

(defn- context-attribute 
	[attrib]
	(if (= Attribute (:type attrib))
		attrib 
		(let [{value :value} attrib
			  {:keys [type] :or {:type nil}} value]
			(cond (= Attribute type)
				  value

				  (= Weave type)
				  (recur value)))))

(defn- get-context 
	[context-list attrib-key]
	(first (filter (fn [context]
				(= attrib-key (:value (context-attribute context))))
			(:value context-list))))

(defn- same-attribute? 
	[{av :value at :type} {bv :value bt :type}]
	(and (= Attribute at bt)
		 (= av bv)))

(defn- same-type?
	[a b]
	(let [ca (context-attribute a)
	      cb (context-attribute b)]
		(if (and (some? ca) 
				 (some? cb))
		    (same-attribute? ca cb))))

(defn- find-match 
	[attrib-list needle]
	(->> attrib-list
		(reduce 
			(fn [ret attrib]
				(cond ret
					  ret 

					  (= ContextList (:type attrib))
					  (let [match (find-match (:value attrib) needle)]
					  	(if match 
					  		match 
					  		ret))

					  (same-type? attrib needle)
					  attrib

					  :else 
					  ret))
				nil)))

(defn- find-function
	[function-list sym]
	(->> function-list
		(filter (fn [f]
			(= (:value f) (:value sym))))
		(first)
		:fn))

(defn- process-attribs-fn [g-list q-list]
	(fn [e]
		(->> q-list
			(map (fn [attrib]
			 (let [k (:value (context-attribute attrib))
			 	   match (find-match g-list attrib)
			 	   result (if match
					   		 (step match attrib e))]
				{k result})))
			(reduce merge))))

(defmulti ^:private step 
	(fn [graph _ _] 
		(:type graph)))

(defmethod step Weave
	[graph query entities]
	(let [fns (:functions graph)]
		(reduce
			(fn [res f]
				(let [args (concat (:args f) [res])]
					(apply (find-function fns f) args)))
		 	entities (:functions query))))

(defmethod step Attribute
	[graph query entity]
	(let [f (:fn graph)
		  args (:args query)]
		(apply f (cons entity args))))

(defmethod step Recursive
	[graph query entity]
	(gr/parse (deref (:value graph)) gr/attribute-list-schema))

(defmethod step Context
	[graph query entity]
	(let [es (step (context-attribute graph) 
				  (context-attribute query) 
				  entity)
		  g-attrib (:attributes graph)
		  g-list (if (= Recursive (:type g-attrib)) 
		  			 (step g-attrib query entity) 
		  			 g-attrib)
		  process (process-attribs-fn g-list (:attributes query))
		  gvalue (:value graph)
		  qvalue (:value query)
		  ; will be able to change to follow the query only once analyzer is written
		  es (if (= Weave (:type gvalue) (:type qvalue))
		  		 (step gvalue qvalue es)
		  		 es)]
		(if ((some-fn sequential? set?) es)
			(vec (map process es))
			(process es))))

(defmethod step ContextList
	[graph query entity]
	(let [attrib-key (:value (context-attribute query))]
		{attrib-key (step (get-context graph attrib-key) query entity)}))

(defn dispatch 
	[graph query & [entity]]
	(step (gr/parse graph gr/context-list-schema)
		  (qu/parse query qu/context-schema)
		  entity))
