(ns discuss.communication.main
  "Functions concerning the communication with the remote discussion system."
  (:require [ajax.core :refer [GET POST]]
            [discuss.utils.common :as lib]
            [discuss.references.integration :as rint]
            [discuss.communication.lib :as comlib]
            [discuss.components.search.statements :as search]
            [discuss.utils.logging :as log]))

;; Handler
(defn process-url-handler
  "React on response after sending a new statement. Reset atom and call newly
  received url."
  [response]
  (lib/remove-origin!)
  (search/remove-search-results!)
  (lib/hide-add-form!)
  (rint/request-references)
  (comlib/process-and-set-items-and-bubbles response))

;;;; Discussion-related functions
(defn get-conclusion-id
  "Returns statement id to which the newly added statement is referred to.
   Currently this is stored in the data_statement_uid of the first bubble."
  []
  (let [bubble (first (lib/get-bubbles))]
    (:data_statement_uid bubble)))


;;;; POST functions
(defn post-json
  "Wrapper to prepare a POST request. Sending and receiving JSON."
  ([url body handler headers]
   (let [request-url (comlib/make-url url)]
     (log/info (str "Posting " body " to " request-url))
     (POST request-url
           {:body            (lib/clj->json body)
            :handler         handler
            :error-handler   comlib/error-handler
            :format          :json
            :response-format :json
            :headers         headers
            :keywords?       true})))
  ([url body handler]
   (post-json url body handler {"Content-Type" "application/json"}))
  ([url body]
   (post-json url body process-url-handler {"Content-Type" "application/json"})))

(defn post-statement
  "Takes statement and an optional reference to post it to the backend."
  [statement reference origin]
  (if (seq (lib/get-last-api))
    (let [url (lib/get-last-api)
          headers (merge {"Content-Type" "application/json"} (comlib/token-header))
          body {:reason statement
                :reference reference
                :origin origin}]
      (post-json url body process-url-handler headers))
    (log/error ":api/last-call is empty, cannot post statement to empty URL.")))


;;;; Get things started!
(defn init-with-references!
  "Load discussion and initially get reference to include them in the discussion."
  []
  (rint/request-references)
  (comlib/init!))
