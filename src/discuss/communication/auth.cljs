(ns discuss.communication.auth
  (:require [ajax.core :refer [POST]]
            [clojure.string :refer [split]]
            [discuss.communication.lib :as comlib]
            [discuss.config :as config]
            [discuss.translations :refer [translate] :rename {translate t}]
            [discuss.utils.common :as lib]))

(defn- success-login
  "Callback function when login was successful. Set attributes of user."
  [response]
  (let [res (lib/process-response response)
        nickname (first (split (:token res) "-"))
        token (:token res)]
    (lib/update-state-map! :user {:nickname   nickname
                                  :token      token
                                  :logged-in? true})
    (comlib/ajax-get-and-change-view (lib/get-last-api) :discussion)))

(defn- wrong-login
  "Callback function for invalid credentials."
  [_]
  (lib/error-msg! (t :errors :login))
  (lib/loading? false))

(defn ajax-login
  "Get cleaned data and send ajax request."
  [nickname password]
  (let [url (:login config/api)]
    (POST (comlib/make-url url)
          {:body            (lib/clj->json {:nickname nickname
                                            :password password})
           :handler         success-login
           :error-handler   wrong-login
           :response-format :json
           :headers         {"Content-Type" "application/json"}
           :keywords?       true})))

(defn login
  "Use login form data, validate it and send ajax request."
  [nickname password]
  (when (and (pos? (count nickname))
             (pos? (count password)))
    (ajax-login nickname password)))

(defn logout
  "Reset user credentials."
  []
  (lib/update-state-map! :user {:nickname   ""
                                :token      ""
                                :logged-in? false}))
