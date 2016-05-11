(ns discuss.utils.views
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]))

(defn fa-icon
  "Wrapper for font-awesome icons."
  ([class]
   (dom/i #js {:className (str "fa " class)}))
  ([class f]
   (dom/i #js {:className (str "pointer fa " class)
               :onClick   f})))

(defn logo
  "If no function is provided, show logo as is. Else bind function to onClick-event and add
   pointer-class."
  ([]
   (fa-icon "fa-comments"))
  ([f]
   (fa-icon "fa-comments" f)))

(defn safe-html
  "Creates DOM element with interpreted HTML."
  [string]
  (dom/span #js {:dangerouslySetInnerHTML #js {:__html string}}))

(defn commit-target-value
  "Set local state of view, parse the value of the target of val."
  [key val owner]
  (om/set-state! owner key (.. val -target -value)))

(defn display
  "Toggle display view."
  [show]
  (if show
    #js {}
    #js {:display "none"}))

(defn toggle-show [show] (if show false true))