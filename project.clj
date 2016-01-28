(defproject lockedon/graph-router "0.1.7"
  :description "A Clojure library for declarative data fetching in derived graphs!"
  :url "http://github.com/LockedOn/graph-router"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[prismatic/schema "1.0.4"]]
  :profiles {:dev {:dependencies [[org.clojure/clojure "1.8.0"]]
  				   :plugins [[com.jakemccrary/lein-test-refresh "0.10.0"]]
				   :main graph-router.core}}
  :deploy-repositories [["releases" :clojars]])
