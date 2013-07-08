(ns middler-clojure.core
  (:use org.httpkit.server)
  (:use clojure.walk)
  (:require [org.httpkit.client :as http])
  (:gen-class))

(defn sanitize-headers [m]
  (-> (stringify-keys m)
      (dissoc "content-length")
      (dissoc "content-encoding")
      (dissoc "host")))

(defn app [{:keys [request-method headers body] :as req}]
  (println (str "Pre-sanitization"))
  (println headers)
  (println (str "Post-sanitization"))
  (println (sanitize-headers headers))
  (let [uri (:uri req)]
    (with-channel req channel
      (http/request {:url uri :method request-method :headers (sanitize-headers headers) :body body}
                    (fn [res]
                      (let [response-headers (sanitize-headers (:headers res))]
                        (send! channel (assoc res :headers response-headers))))))))

(defn -main
  [& args]
  (run-server app {:port 8080})) ; Ring server
