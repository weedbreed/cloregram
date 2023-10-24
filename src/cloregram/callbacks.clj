(ns cloregram.callbacks
  (:require [dialog.logger :as log]
            [cloregram.db :as db]
            [datomic.api :as d]
            [clojure.edn :as edn]))

(defn ^java.util.UUID create
  ([user ^clojure.lang.Symbol f] (create user f nil))
  ([user ^clojure.lang.Symbol f args]
   (let [uuid (java.util.UUID/randomUUID)
         args (pr-str args)]
     (d/transact (db/conn) [{:callback/uuid uuid
                             :callback/function f
                             :callback/args args
                             :callback/user [:user/id (:user/id user)]}])
     (log/debugf "Created Callback %s for User %s: %s %s" uuid user f args)
     uuid)))

(defn- load-callback
  [user uuid]
  (let [callback (d/pull (db/db) '[* {:callback/user [*]}] [:callback/uuid uuid])]
    (log/debug (format "Loaded callback %s: %s" uuid callback))
    (when (not= (:id user) (-> callback :callback/user :id))
      (throw (ex-info "Wrong User in loaded Callback!" {:user user :callback callback})))
    callback))

(defn call
  [user ^java.util.UUID uuid]
  (let [callback (load-callback user uuid)
        func (:callback/function callback)
        args (-> callback :callback/args edn/read-string)
        args (cond-> args
               (vector? args) (conj user)
               (map? args) (-> (merge {:user user}) vector))]
    (log/debugf "Calling %s with args %s" func args)
    (apply (resolve func) args)))
