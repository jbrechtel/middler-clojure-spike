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

(defn assoc-query-string-part [m qs-part]
  (let [k (first (clojure.string/split qs-part #"="))
        v (second (clojure.string/split qs-part #"="))]
    (assoc m k v)))


(defn parse-query-string [qs]
  (if (= nil qs)
    {}
    (let [decoded (java.net.URLDecoder/decode qs "UTF-8")]
      (reduce assoc-query-string-part {} (clojure.string/split decoded #"&")))))

(defn app [{:keys [uri request-method headers body query-string] :as req}]
  (println (str "parsed querystring"))
  (println (parse-query-string query-string))
  (with-channel req channel
    (http/request {:url uri :method request-method :headers (sanitize-headers headers) :body body :query-params (parse-query-string query-string)}
                  (fn [res]
                    (let [response-headers (sanitize-headers (:headers res))]
                      (send! channel (assoc res :headers response-headers)))))))

(defn -main
  [& args]
  (run-server app {:port 8080})) ; Ring server
