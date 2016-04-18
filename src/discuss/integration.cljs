(ns discuss.integration
  "Listen for mouse clicks, get references and highlight them in the article."
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [goog.events :as events]
            [om.core :as om]
            [cljs.core.async :refer [put! chan <!]]
            [clojure.string :as string]
            [discuss.extensions]
            [discuss.communication :as com]
            [discuss.lib :as lib]))

;;; Listener for mouse clicks
(defn listen
  "Helper function for mouse-click events."
  [el type]
  (let [out (chan)]
    (events/listen el type
                   (fn [e] (put! out e)))
    out))

;; http://www.thesoftwaresimpleton.com/blog/2014/12/30/core-dot-async-dot-mouse-dot-down/
(let [clicks (listen (.getElementById js/document "discuss-text") "click")]
  (go (while true
        (<! clicks)
        (lib/save-selected-text))))

(defn minify-doms
  "Removes dom-elements, which can never be used as a reference."
  [doms]
  (remove #(or (= "SCRIPT" (.-nodeName %))
               (= "path"   (.-nodeName %))
               (= "svg"    (.-nodeName %))
               (= "circle" (.-nodeName %))
               (= "HTML"   (.-nodeName %))
               (= "HEAD"   (.-nodeName %))
               (= "META"   (.-nodeName %))
               (= "BUTTON" (.-nodeName %)))
          doms))

(defn get-parent
  "Assuming that the last occurence of my reference is the closest parent of it,
   this function will return it."
  [doms ref]
  (last
    (filter
      identity
      (map (fn [dom]
             (when (lib/substring? ref (.-innerHTML dom))
               dom))
           doms))))


;;; Integrate references and highlight them in the article
(defn convert-reference
  "Find parent of reference, split it into parts and wrap the original reference for highlighting and interaction."
  [ref]
  (let [ref-text  (:text ref)
        ref-url   (:url ref)
        doms-raw  (.getElementsByTagName js/document "*")
        doms      (minify-doms doms-raw)
        parent    (get-parent doms ref-text)
        dom-parts (string/split (.-innerHTML parent) (re-pattern ref-text))]
    (when parent
      (om/root discuss.views/reference-view {:text     ref-text
                                             :url      ref-url
                                             :dom-pre  (first dom-parts)
                                             :dom-post (last dom-parts)}
               {:target parent}))))

(defn process-references
  "Receives references through the API and prepares them for the next steps."
  [refs]
  (doall (map #(convert-reference %) refs)))


;;; Interaction with integratet references
(defn click-reference
  ""
  [text url]
  (com/ajax-get url)
  (discuss.sidebar/show!)
  )