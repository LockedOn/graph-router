(defproject lockedon/graph-router "0.1.4"
  :description "A Clojure library for composing and quering graphs!"
  :url "http://github.com/LockedOn/graph-router"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
    			 [prismatic/schema "1.0.1"]]
  :deploy-repositories [["releases" :clojars]]
  :main graph-router.core)
