(ns user
  (:require [json.core :refer :all]
            [c-style.core :as c]
            [clojure.pprint :refer [pprint]])
  (:import [java.io StringReader PushbackReader]))

;; (defn test []
;;   (pprint (seq (.getURLs (java.lang.ClassLoader/getSystemClassLoader))))

;;   (c/proc (:let msg "hello")
;;           (println msg))

;;   (c/if-map [:condition1 [false "message1"]
;;              :condition2 [false "message2"]
;;              :condition3 [false "message3"]]
;;             [true "ok"]))

(defn see-see []
  (with-open [rd (PushbackReader. (StringReader. "hello"))]
    (println (char (.read rd)))
    (println (char (.read rd)))
    (println (char (.read rd)))
    (println (char (.read rd)))
    (println (char (.read rd)))
    (println (char (.read rd)))
    (println (char (.read rd)))))
