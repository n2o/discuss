(ns discuss.references.lib
  (:require [discuss.utils.common :as lib]
            [om.dom :as dom]
            [om.core :as om]))

(defn save-selected-reference!
  "Saves the currently clicked reference for further processing."
  [ref]
  (lib/update-state-item! :references :selected (fn [_] ref)))

(defn get-selected-reference
  "Returns the currently selected reference."
  []
  (get-in @lib/app-state [:references :selected]))

(defn save-reference-usages!
  "Saves the currently selected statement for further processing."
  [reference]
  (lib/update-state-item! :references :usages (fn [_] reference)))

(defn get-reference-usages
  "Return list of reference usages, which were previously stored in the app-state."
  []
  (get-in @lib/app-state [:references :usages]))


;;;; View Components
(defn- current-reference-component
  "Return DOM element showing which reference is currently selected."
  [_ owner]
  (reify
    om/IRender
    (render [_]
      (om/observe owner (lib/get-cursor :layout))
      (dom/div #js {:className "text-center"
                    :style     #js {:paddingBottom "1em"}}
               (let [ref-title (:text (get-selected-reference))]
                 (dom/em nil "\"" ref-title "\""))))))