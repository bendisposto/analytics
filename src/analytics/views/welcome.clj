(ns analytics.views.welcome
  (:require [analytics.views.common :as common]
            [noir.content.getting-started])
  (:use [noir.core :only [defpage]]))

(defpage "/" []
  (common/layout
   [:div {:class "sixteen columns"}
    [:h1  {:id "title"} "ProB Sourcecode Analytics"]
    [:hr]
    [:div {:id "tl"}]
    [:div {:id "commit-selector"}]]
   ))


(defpage "/commits" []
  "{ 
        \"dateTimeFormat\": \"Gregorian\",
        \"events\": [
          {\"start\": \"Sat May 20 2010 00:00:00 GMT-0600\",
           \"description\": \"Commit message\",
           \"title\": \"#1234567890\"
          }
        ]
      }")
