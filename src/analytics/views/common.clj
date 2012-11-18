(ns analytics.views.common
  (:use [noir.core :only [defpartial]]
        [hiccup.page :only [include-css include-js html5]]))

(defpartial layout [& content]
            (html5
              [:head
               [:title "ProB Sourcecode Analytics"]
              (map (comp include-css #(str "/css/" % ".css")) ["base" "skeleton" "layout" "analytics"])]
              (map (comp include-js #(str "/js/" % ".js")) ["jquery-1.7.2.min"] )
              (include-js "http://static.simile.mit.edu/timeline/api-2.3.0/timeline-api.js?bundle=true")
              (include-js "/js/timeline.js")
              [:body {:onLoad "onLoad()"  :onResize "onResize()"}
               [:div {:class "container"}
                content
                (include-js "/coffee-js/client.js")
                ]]))
