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

### Composing Graph Descriptions

Graphs descriptions are data structures defining the shape of data in Clojure hash maps available for querying.

```clojure
(def data {:Hello "Hello" :World "World"})

(def data-keys [:Hello :World])
```

In the example above two bindings are defined:

`data` is a Clojure Hash map.

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

### Putting it all together

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


## License

Copyright © 2015 LockedOn

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
