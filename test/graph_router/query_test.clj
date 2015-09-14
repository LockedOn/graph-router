(ns graph-router.query-test
  (:require [clojure.test :refer :all]
            [graph-router.query :refer :all]))

(deftest ok-tests
  (testing "Basic Query"
    (is (parse '{(:Artist 17592186047077) [:artist/name]} query-schema)))

  (testing "Basic Query - No Args on Context"
    (is (parse '{:Artist [:artist/name]} query-schema)))

  (testing "Basic Query - Args on Attribute"
    (is (parse '{:Artist [(:artist/name 999)]} query-schema)))

  (testing "Complex Query - got everything!"
    (is (parse '{(->> (:Artists 123) (drop 10) (take 10)) 
    				  [(->> :artist/name (d)) (:artist/format "yyyy-MM-dd") {:artist/country 
    				  											   [:country/code]}]} query-schema))))

(deftest failing-tests
  (testing "Empty Args List"
    (is (thrown? Exception (parse '[] attribute-schema))))

  (testing "Symbol in Arg List"
    (is (thrown? Exception (parse '[symbol] attribute-schema))))

  (testing "Function in Arg List"
    (is (thrown? Exception (parse [take] attribute-schema))))

  (testing "nil is not an arglist"
    (is (thrown? Exception (parse nil attribute-schema))))

  (testing "map is not an arglist"
    (is (thrown? Exception (parse '{:ok world} attribute-schema))))

  (testing "Weave missing functions"
    (is (thrown? Exception (parse '(->> :Hello) weave-schema))))

  (testing "Weave Invalid functions"
    (is (thrown? Exception (parse '(->> :Hello (:wow 1)) weave-schema))))

  (testing "Weave missing Attribute"
    (is (thrown? Exception (parse '(->> ) weave-schema)))))