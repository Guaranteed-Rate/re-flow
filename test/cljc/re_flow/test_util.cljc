(ns re-flow.test-util
  "Utility functions to integrate clojure.spec.test/check with clojure.test

  These functions taken from:
  https://gist.github.com/Risto-Stevcev/dc628109abd840c7553de1c5d7d55608"
  (:require [clojure.pprint :as pprint]
            [clojure.spec :as s]
            [clojure.spec.test :as stest]
            [clojure.test :refer [is]]))

(defn summarize-results [spec-check]
  (map (comp #(pprint/write % :stream nil) stest/abbrev-result) spec-check))

(defn check [spec-check]
  (is (nil? (-> spec-check first :failure)) (summarize-results spec-check)))
