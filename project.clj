(defproject lockedon/graph-router "0.1.6"
  :description "A Clojure library for declarative data fetching in derived graphs!"
  :url "http://github.com/LockedOn/graph-router"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[prismatic/schema "0.4.4"]]
  :profiles {:dev {:dependencies [[org.clojure/clojure "1.7.0"]]
				   :main graph-router.core}}
  :deploy-repositories [["releases" :clojars]])
