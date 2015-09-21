[![Build Status](https://travis-ci.org/LockedOn/graph-router.svg?branch=master)](https://travis-ci.org/LockedOn/graph-router)

# ![graph-router](https://github.com/lockedon/graph-router/blob/master/graph-router-logo.png)

A Clojure library for composing and querying graphs!

Graph-router *aims* to provide [GraphQL](https://github.com/facebook/graphql) like query power with the elegance of 
the [Datomic Pull API](http://docs.datomic.com/pull.html) whilst sitting on top 
of a [Falcor](https://github.com/Netflix/falcor) like Router.

Graph-router leverages the fact that Clojure keywords are also functions for 
accessing data in Clojure hash maps. This means that Graph-router will work with 
any data type that allows keywords to access values, this include Datomic entities. 

## Installation

[![Clojars Project](http://clojars.org/lockedon/graph-router/latest-version.svg)](http://clojars.org/lockedon/graph-router)

Graph-router is available on clojars. Once you have added the latest version to your project.clj you are ready to go!

## Getting Started

Requiring graph-router.core provides you with two functions `dispatch` and `with`.

`dispatch` is used to process a query with a graph.

`with` is a utility funciton to help compose your graphs.

```clojure
(ns example.core
	(:require [graph-router.core :refer :all]))
```

__NOTE:__
All future examples assume both `dispatch` and `with` are imported.

### Composing Graph Descriptions

Graphs descriptions are data structures defining the shape of data in Clojure hash maps available for querying.

```clojure
(def data {:Hello "Hello" :World "World"})

(def data-keys [:Hello :World])
```

In the example above two bindings are defined:

`data` is a Clojure data structure accessable via Clojure keywords.

`data-keys` is a description of how to consume the values in `data`.

#### `with`

Graph-router requires at the top level a hash-map of data generators.

`with` is mainly used to define what function to use in place of a keyword in accessing data.

```clojure
(defn generate-data 
	[_] 
	{:Hello "Hello" :World "World"})

(def graph {(with :Root generate-data) [:Hello :World]})
```

In the above example the keyword `:Root` is declared as an alias for `generate-data` that returns the data.

`generate-data` is called once per dispatch. This is very useful as `generate-data` could be used to return data from an external source like a SQL database, datomic, a third party REST API or any other data source you like.

__NOTE:__
When using `with` to swap what function is used to access data, the function used must receive at least one argument. 
Just as a keyword receives one argument when being used as a function to access data in a hash map.

```clojure
(defn generate-data 
	[_] 
	{:Hello "Hello" :World "World"})

(defn get-hello 
	[& args]
	(apply :Hello args))

;; This will produce an identical graph to the above example.
(def graph {(with :Root generate-data) [(with :Hello get-hello) :World]})
``` 

### Putting It All Together

`dispatch` processes a query in relation to a graph. Fortunately the query looks very similar to a graph.

```clojure
(defn generate-data 
	[_] 
	{:Hello "Hello" :World "World"})

(defn say-hi
	[e]
	(str (:Hello e) " " (:World e) "!"))

(def graph {(with :Root generate-data) [(with :Hi say-hi)]})

(def query '{:Root [:Hi :Hello]})

(dispatch graph query) ;; => {:Root {:Hi "Hello World!", :Hello nil}}
```

There is quite a bit going on in the previous example. 

`graph` builds on what we have already seen, exposing only `:Hi` from `:Root`. `:Hi` is an alias for `say-hi` 
which in turn uses `generate-data` being passed in as `e`.  

`query` is a quoted form, defining the shape of the data required.
`query` is requesting `:Hi` and `:Hello` from `:Root`. Given only `:Hi` is exposed from `:Root`, the result of `:Hello` is `nil`.


### Passing Arguments 

Arguments can be passed to keywords, anywhere keywords are in the query.

```clojure
(defn generate-data 
    [_] 
    {:Hello "Hello" :World "World"})

(defn speak
    [e greeting]
    (str greeting " " (:World e) "!"))

(def graph {(with :Root generate-data) [(with :Say speak)]})

(def query '{:Root [(:Say "Hi")]})

(dispatch graph query) ;; => {:Root {:Say "Hi World!"}}
```

In `query` we are passing "Hi" to the function that is aliased for `:Say`.

There is no hard limit to the number of arguments that can be passed.

### Collections

Collections are handled transparently, the graph description and query stay the same, the only thing that changes is the data and result.

```clojure
(defn generate-data 
    [_] 
    [{:Hello "Hello" :World "World"}])

(def graph {(with :Root generate-data) [:Hello :World]})

(def query '{:Root [:Hello]})

(dispatch graph query) ;; => {:Root [{:Hello "Hello"}]}
```

### Nested Data

Graph-router borrows the concept of nesting queries to access nested data.

```clojure
(defn generate-data
    [_]
    {:One {:Two "Deep" :Three "3"}})

(def graph {(with :Root generate-data) [{:One [:Two]}]})

(def query '{:Root [{:One [:Two]}]})

(dispatch graph query) ;; => {:Root {:One {:Two "Deep"}}}
```

In both `graph` and `query` all the same rules are applied to define the nested graph description and query.

### Multiple Roots

The graph description can have multiple roots, however the query can only access one.

```clojure
(defn generate-data 
    [_] 
    {:Hello "Hello" :World "World"})

(defn second-root 
    [_] 
    {:Hi "Hi" :Moon "Moon"})

(def graph {(with :Root generate-data) [:Hello :World]
            (with :Root2 second-root) [:Hi :Moon]})

(def query '{:Root2 [:Hi :Moon]})

(dispatch graph query) ;; => {:Root2 {:Hi "Hi", :Moon "Moon"}}
```

### Recursive Graph Descriptions

Graph-router uses Clojure references to build recusive graph descriptions.

```clojure
(defn generate-data
    [_]
    {:left {:right {:value "Hello"}} :right {:value 2}})

(def b-tree [{:left #'b-tree} {:right #'b-tree} :value])

(def graph {(with :Root generate-data) b-tree})

(def query '{:Root [{:left [{:right [:value]}]}]})

(dispatch graph query) ;; => {:Root {:left {:right {:value "Hello"}}}}
```

This also allows you to do mutual recursion.

```clojure
(defn generate-data
    [_]
    {:A {:B {:A {:value "Hello Word!"}}}})

(declare b)

(def a [{:A #'b} :value])

(def b [{:B a} :value])

(def graph {(with :Root generate-data) a})

(def query '{:Root [{:A [{:B [{:A [:value]}]}]}]})

(dispatch graph query) ;; => {:Root {:A {:B {:A {:value "Hello Word!"}}}}}
```

Notice how `a` and `b` reference each other.

### Weaving Functions

`with` has two jobs; aliasing functions as described earlier and attaching functions that the result of the function can be weaved through.

```clojure
(defn generate-data 
    [_] 
    [{:Hello "Hello" :World "World"} {:Hello "Hi" :World "Moon"}])

(def graph {(with :Root generate-data {'take take}) [:Hello :World]})

(def query '{(->> :Root (take 1)) [:Hello]})

(dispatch graph query) ;; => {:Root [{:Hello "Hello"}]}
```

`with` accepts an additional argument of a hash map with the key being a symbol and the value being the function. In the above example we are declaring the symbol `'take` to use the clojure.core function `take`.

Graph-router borrows the syntax from the Clojure thead last macro `->>`. The weave function has no limit to the number of functions to weave the result through. Each function can take additional arguments as in the example above where `take` is being given the additional argument `1`. 

__NOTE:__ The result is always passed as the last argument to the weave functions.

## TODO

* Add thread first weave.
* Statically analyze the query in relation to the graph to validate that the query is a strict subset of graph.
* Statically analyze the arity of all functions to ensure the correct number of arguments are being passed before the query is processed.
* Better error reporting when query or graph description does not match schema.

## License

Copyright © 2015 LockedOn

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
