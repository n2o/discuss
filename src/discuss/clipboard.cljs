(ns discuss.clipboard
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [discuss.utils.common :as lib]
            [discuss.utils.views :as vlib]))

(def counter (atom 0))

(defn get-stored-selections
  "Return all stored selections."
  []
  (let [selections (get-in @lib/app-state [:clipboard :selections])]
    (if selections selections [])))

(defn add-selection
  "Store current selection in clipboard."
  []
  (let [selections (get-stored-selections)
        current (lib/get-selection)
        with-current (distinct (conj selections current))]
    (lib/update-state-item! :clipboard :selections (fn [_] with-current))))


;;;; Drag n Drop stuff
; http://www.w3schools.com/html/html5_draganddrop.asp

(defn update-reference-drop
  "Use text from clipboard item as reference for own statement."
  [_ev]
  (let [clipboard-item (get-in @lib/app-state [:clipboard :current])]
    (lib/remove-class clipboard-item "bs-callout-info")
    (lib/add-class clipboard-item "bs-callout-success")
    (lib/update-state-item! :user :selection (fn [_] (.. clipboard-item -innerText)))))

(defn allow-drop [ev]
  (println "fn: allow-drop")
  (.preventDefault ev))

(defn drag-event [ev]
  (let [target (.. ev -target)]
    (lib/update-state-item! :clipboard :current (fn [_] target))))


;;;; Views
(defn clipboard-item [data owner]
  (reify
    om/IInitState
    (init-state [_]
      {:selected? false})
    om/IRenderState
    (render-state [_ {:keys [selected?]}]
      (dom/div #js {:id          (swap! counter inc)
                    :className   "bs-callout bs-callout-info"
                    :draggable   true
                    :onDragStart drag-event}
               (dom/div nil data)
               (dom/button #js {:className "btn btn-sm btn-default"
                                :onClick   #(discuss.communication/ajax-get "api/cat-or-dog")
                                :title     "Select this reference for your statement"}
                           (vlib/fa-icon "fa-check"))))))

(defn view [data owner]
  (reify om/IRender
    (render [_]
      (when (pos? (count (get-stored-selections)))
        (dom/div nil
                 (dom/h5 nil "Clipboard")
                 (apply dom/div nil
                        (om/build-all clipboard-item (get-stored-selections))))))))