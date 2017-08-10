(ns re-flow.test-runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [re-flow.core-test]
            [re-flow.db-test]
            [re-flow.flow-test]
            [re-flow.interceptors-test]))

(enable-console-print!)

(doo-tests 're-flow.core-test
           're-flow.db-test
           're-flow.flow-test
           're-flow.interceptors-test)
