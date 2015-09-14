(ns graph-router.graph-test
  (:require [clojure.test :refer :all]
            [graph-router.graph :refer :all]))

(deftest ok-tests
  (testing "Basic Graph" 
    (is (parse {:Root [:name]} graph-schema)))

  (testing "Multi Roots Graph" 
    (is (parse {:Root [:name] :Root2 [:attrib]} graph-schema)))

  (testing "With Function Swap" 
    (is (parse {(with :Root (fn [])) [:name]} graph-schema)))

  (testing "With Weave Functions" 
    (is (parse {(with :Root {'drop drop}) [:name]} graph-schema)))

  (testing "With Function Swap and Weave Functions" 
    (is (parse {(with :Root (fn []) {'drop drop}) [:name]} graph-schema)))

  (testing "With Function Swap in the Attribute List" 
    (is (parse {:Root [(with :name (fn []))]} graph-schema)))

  (testing "Nested Graph" 
    (is (parse {:Root [:name {:sub-graph [:sub/name]}]} graph-schema)))

  (testing "Recursive Attributes" 
  	(def recusive-attribs [:wow {:nested #'recusive-attribs}])
    (is (parse {:Root recusive-attribs} graph-schema))))


(deftest failing-tests
  (testing "Empty Attributes" 
    (is (thrown? Exception (parse {:Root []} graph-schema))))

  (testing "With Function Swap and Weave Functions in wrong order" 
    (is (thrown? Exception (parse {(with :Root {'drop drop} (fn [])) [:name]} graph-schema))))

  (testing "Root not a keyword" 
    (is (thrown? Exception (parse {'Root [:node]} graph-schema)))
    (is (thrown? Exception (parse {"wok" [:node]} graph-schema)))
    (is (thrown? Exception (parse {(fn []) [:node]} graph-schema)))
    (is (thrown? Exception (parse {1 [:node]} graph-schema))))

  (testing "Attribute not a keyword" 
    (is (thrown? Exception (parse {:Root ['Root]} graph-schema)))
    (is (thrown? Exception (parse {:Root ["wok"]} graph-schema)))
    (is (thrown? Exception (parse {:Root [(fn [])]} graph-schema)))
    (is (thrown? Exception (parse {:Root [1]} graph-schema))))

  (testing "With Weave Functions in the Attribute List" 
    (is (thrown? Exception (parse {:Root [(with :node {'drop drop})]} graph-schema)))))