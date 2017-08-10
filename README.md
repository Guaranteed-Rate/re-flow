![re-flow logo](/images/re-flow.png)

re-flow is a library that adds tools for building and executing workflows in
re-frame applications.


## Installation

There are a few different environments that support re-flow, and each requires a
slightly different configuration.

### ClojureScript only

If you intend to use re-flow in a ClojureScript application without interacting
with flows using Clojure, include the following dependency in your project file.

```
[guaranteed-rate/re-flow "0.7.0"]
```

### Clojure 1.8

If you intend to use Clojure 1.8 to generate or manipulate flows, then you will
need to include the following dependencies. re-flow depends heavily upon spec
and uses [clojure-future-spec](https://github.com/tonsky/clojure-future-spec) to
provide those features.

```
[clojure-future-spec "1.9.0-alpha15"]
[guaranteed-rate/re-flow "0.7.0"]
```

The version required is derived from the version of ClojureScript used by
re-frame. Note that `alpha15` pre-dates the namespace split. New versions of
re-flow will be released as the version of ClojureScript required for re-frame
is updated.


### Clojure 1.9

If you intend to use Clojure 1.9 to generate or manipulate flows, then you will
need to include the following dependencies. This version of Clojure 1.9 contains
the same structure of spec namespaces as the version of ClojureScript required
by re-frame.

```
[org.clojure/clojure "1.9.0-alpha15"]
[guaranteed-rate/re-flow "0.7.0"]
```

The version required is derived from the version of ClojureScript used by
re-frame. Note that `alpha15` pre-dates the namespace split. New versions of
re-flow will be released as the version of ClojureScript required for re-frame
is updated.

### Clojure with Visualization

In order to build GraphViz-based visualizations of flows, you will need to
include the following in your project dependencies in addition to the
dependencies described in the previous sections.

```
[aysylu/loom "1.0.0"]
```


## Documentation

[API Docs](https://guaranteed-rate.github.io/re-flow/)

[Documentation](/doc)

[Examples](/examples)


## Usage

The following is a short but complete example of a re-frame application using
re-flow. This is the code used for the [ping-pong example](/examples/ping-pong),
but the example contains much more documentation. Be sure to check out the
[other examples](/examples).


```clojure
(ns ping-pong.core
  (:require [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            [re-flow.core :as re-flow]))

;; Define a flow, which is just a vector of states and an option map containing
;; a value indicating which state is the start state.
(def ping-pong-flow
  (re-flow/flow [{:name :ping
                  :transition {:re-flow.transition/default :pong}}
                 {:name :pong
                  :transition {:re-flow.transition/default :ping}}]
                {:start :ping}))

(defn main-panel []
  ;; Subscribe to a flow's state. re-flow provides functions that wrap re-frame
  ;; functions like re-frame.core/subscribe for convenience, but you are free to
  ;; use the re-frame functions if you wish.
  (let [state (re-flow/sub-flow-state)]
    (fn []
      [:div
       [:p (:name @state)]

       ;; When the button is clicked, transition to the next state (no
       ;; transition data is provided in this case).
       [:button {:on-click #(re-flow/transition)} "Transition!"]])))

(defn mount-root []
  (re-frame/clear-subscription-cache!)
  (reagent/render [main-panel]
                  (.getElementById js/document "app")))

(defn ^:export init []
  ;; Dispatch an event to start the flow.
  (re-flow/start ping-pong-flow)
  (mount-root))
```

## License

Copyright © 2017 Guaranteed Rate

Distributed under the [MIT license](LICENSE)