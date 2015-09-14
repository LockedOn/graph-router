(ns graph-router.dispatch-test
  (:require [clojure.test :refer :all]
            [graph-router.core :refer :all]))

(deftest ok-tests
  (testing "Full Graph Query Dispatch" 
    (is (= {:Root {:name "Hello"}} (dispatch {:Root [:name]} 
                                             '{:Root [:name]} 
                                             {:Root {:name "Hello"}}))))

  (testing "Full Graph Query Dispatch - generating root" 
    (is (= {:Root {:name "Hello"}} (dispatch {(with :Root (fn [& _] 
                                                            {:name "Hello"})) [:name]} 
                                             '{:Root [:name]}))))

  (testing "Full Graph Query Dispatch - Pass Args" 
    (is (= {:Root {:name "World"}} (dispatch {(with :Root (fn [_ s] 
                                                            {:name s})) [:name]} 
                                             '{(:Root "World") [:name]})))

    (is (= {:Root {:name "World"}} (dispatch {(with :Root (fn [& _] nil)) [(with :name (fn [_ s] s))]} 
                                             '{:Root [(:name "World")]}))))

  (testing "Full Graph Query Dispatch - collection" 
    (is (= {:Root [{:name "Hello"}]} (dispatch {:Root [:name]} 
                                               '{:Root [:name]} 
                                               {:Root [{:name "Hello"}]}))))

  (testing "Part Graph Query Dispatch - weave" 
    (is (= {:Root [{:name "World"}]} (dispatch {(with :Root (fn [& _] 
                                                            [{:name "Hello"} {:name "World"}]) 
                                                    {'taker take 'droper drop}) [:name]} 
                                             '{(->> :Root (droper 1) (taker 1)) [:name]}))))

  (testing "Part Graph Query Dispatch" 
    (is (= {:Root {:name "Hello"}} (dispatch {:Root [:name :sound]} 
                                             '{:Root [:name]} 
                                             {:Root {:name "Hello" :sound "Bark"}})))))


(deftest failing-tests
  (testing "Invalid Query" 
    (is (thrown? Exception (dispatch {:Root [:name]} 
                                             '{Root [:name]} 
                                             {:Root {:name "Hello"}}))))
  (testing "Invalid Graph" 
    (is (thrown? Exception (dispatch {"hello" [:name]} 
                                             '{:Root [:name]} 
                                             {:Root {:name "Hello"}})))))