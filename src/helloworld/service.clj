(ns helloworld.service
    (:require [io.pedestal.service.http :as bootstrap]
              [io.pedestal.service.http.route :as route]
              [io.pedestal.service.http.body-params :as body-params]
              [io.pedestal.service.http.route.definition :refer [defroutes]]
              [ring.util.response :as ring-resp]))

(defn about-page
  [request]
  (ring-resp/response (format "Clojure %s" (clojure-version))))

(defn home-page
  [request]
  (-> (ring-resp/response "
<html>
  <head>
    <title>Hello World</title>
  </head>
  <body>
      <h1>Hello world</h1>
      <script src=\"//ajax.googleapis.com/ajax/libs/jquery/1.6.1/jquery.js\"></script>
      <script type=text/javascript>
          $(function() {
              $('a#calculate').bind('click', function() {
                $.getJSON('/calculate', {
                  a: $('input[name=\"a\"]').val(),
                  b: $('input[name=\"b\"]').val()
                  }, function(data) {
                  $(\"#result\").text(data.result);
                  });
                return false;
                });
              });
      </script>
      <h1>jQuery Example</h1>
      <p><input type=text size=5 name=a> +
      <input type=text size=5 name=b> =
      <span id=result>?</span>
      <p><a href=# id=calculate>calculate server side</a>
  </body>
</html>")
    (ring-resp/content-type "text/html")))

(defn calculate
  [request]
  (let [a (get-in request [:query-params :a])
        b (get-in request [:query-params :b])]
    (ring-resp/response (str "{\"result\":" (+ (Integer/parseInt a) (Integer/parseInt b)) "}"))))

(defroutes routes
  [[["/" {:get home-page}
     ;; Set default interceptors for /about and any other paths under /
     ^:interceptors [(body-params/body-params)]
     ["/calculate" {:get calculate}]
     ["/about" {:get about-page}]]]])

;; You can use this fn or a per-request fn via io.pedestal.service.http.route/url-for
(def url-for (route/url-for-routes routes))

;; Consumed by helloworld.server/create-server
(def service {:env :prod
              ;; You can bring your own non-default interceptors. Make
              ;; sure you include routing and set it up right for
              ;; dev-mode. If you do, many other keys for configuring
              ;; default interceptors will be ignored.
              ;; :bootstrap/interceptors []
              ::bootstrap/routes routes
              ;; Root for resource interceptor that is available by default.
              ::bootstrap/resource-path "/public"
              ;; Either :jetty or :tomcat (see comments in project.clj
              ;; to enable Tomcat)
              ::bootstrap/type :jetty
              ::bootstrap/port 8080})
