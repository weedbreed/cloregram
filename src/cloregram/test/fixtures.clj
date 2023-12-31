(ns cloregram.test.fixtures
  (:require  [clojure.java.io :as io]
             [cloregram.test.infrastructure.core]
             [cloregram.core :refer [run]]
             [cloregram.tasks.update-schema :refer [update-schema]]))

(defn use-test-environment
  [body]
  (run (io/resource "test-config.edn"))
  (body))

(defn setup-schema
  [body]
  (update-schema)
  (body))
