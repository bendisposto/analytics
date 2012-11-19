(ns analytics.views.welcome
  (:require [analytics.views.common :as common]
            [analytics.models.datomic :as d]
            [noir.response :as response])
  (:use [noir.core :only [defpage defpartial]]))

(def tdate (atom nil))
(def current-sha (atom nil))

(defn mk-li [file] [:li (str file)])

(defpartial module-selection [sha]
  [:h3 "Module dependencies"]
  [:select {:onChange (str "javascript: display_graph(this.options[this.selectedIndex].value,'" sha "')")}
   [:option]
   (map (fn [e] [:option {:value e } (str  e)]) (d/all-modules sha))
   ])

(defpage "/" {:keys [sha date] :as request}
  
  (reset! tdate date)
  (reset! current-sha sha)
  
  (let [sha @current-sha] (common/layout
    [:div {:class "sixteen columns"}
     [:h1  {:id "title"} "ProB Sourcecode Analytics"]
     [:hr]
     [:p "This page provides an overview of the ProB Sourcecode repository. Use the Cit commit timeline to select a commit. The sections below will provide details for the selected commit."]
     [:h3 "Git Commits"]
     [:div {:id "tl"}]
     (when sha [:h2 (str "Working with commit " sha " from " @tdate)]) 
     (when sha (module-selection sha))
     [:div {:id "canvas"}]])))
     
(defn mk-ds [[ts msg author sha]]
  {"start" ts
   "description" msg
   "author" author
   "sha" sha
   "title" ""})

(defpage "/dependencies" {:keys [m sha]}
  (response/json {:name m
                  :relying (d/using m sha)
                  :imported (d/used-by m sha)}))

(defpage "/commits" []
  (let [commits (d/commits)
        last-commit-date (ffirst commits)
        r { "dateTimeFormat" "Gregorian"
            "events" (map mk-ds commits)}]
    (response/json (if @tdate (merge r {"date" @tdate}) (merge r {"date" last-commit-date})))))

