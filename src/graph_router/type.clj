(ns graph-router.type)

(def Attribute ::Attribute)

(def Context ::Context)

(def ContextList ::ContextList)

(def Function ::Function)

(def Weave ::Weave)

(def Recursive ::Recursive)

(defrecord Attrib [type value fn])
(defrecord Func [type value args])
(defrecord ContList [type value])
(defrecord Cont [type value attributes])
(defrecord Wea [type value functions])