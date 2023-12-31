(ns cloregram.users
  (:require [dialog.logger :as log]
            [cloregram.db :as db]
            [datomic.api :as d]
            [cloregram.utils :as utl]))

(defn get-or-create
  [udata]
  (if-let [user (d/pull (db/db) '[:user/id
                                  :user/username
                                  :user/first-name
                                  :user/last-name
                                  :user/language-code
                                  :user/msg-id
                                  :user/handler] [:user/id (:id udata)])]
    (do (log/debug "Loaded User:" user)
        user) ; TODO: Check info and update if needed
    (do (d/transact (db/conn) [(->> {:user/id (:id udata)
                                     :user/username (:username udata)
                                     :user/first-name (:first_name udata)
                                     :user/last-name (:last_name udata)
                                     :user/language-code (:language_code udata)
                                     :user/handler [(symbol (str (:name (utl/get-project-info)) ".handler/common")) nil]}
                                    (filter second) ; 'false' values will be removed!
                                    (into {}))])
        (log/info "Created User by data:" udata)
        (get-or-create udata))))

(defn set-msg-id
  [user msg-id]
  (log/debugf "Setting msg-id=%d for User %s" msg-id (utl/username user))
  (d/transact (db/conn) [{:user/id (:user/id user)
                          :user/msg-id msg-id}]))

(defn set-handler
  [user handler args]
  (log/debugf "Setting handler %s with arguments %s for User %s" handler args user)
  (d/transact (db/conn) [{:user/id (:user/id user)
                          :user/handler [handler args]}]))
