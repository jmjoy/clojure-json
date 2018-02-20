(ns json.core
  (:require [clojure.data.json :as json]
            [clojure.tools.trace :refer :all]
            [c-style.core :refer :all]
            [clojure.string :as str])
  (:import [java.io StringWriter StringReader PushbackReader]))

(declare from-json)

(defn- format-key [k]
  (c/proc (:let mid ((if (instance? clojure.lang.Named k) name str) k))
          (str \" mid \")))

(defmulti to-json instance?)

(defmethod to-json clojure.lang.IPersistentMap [x]
  (c/proc (:let kvs (for [[k v] x] (str (format-key k) ":" (to-json v))))
          (:let mid (str/join \, kvs))
          (str \{ mid \})))

(defmethod to-json java.util.Collection [x]
  (c/proc (:let vs (map to-json x))
          (:let mid (str/join \, vs))
          (str \[ mid \])))

(defmethod to-json java.lang.Object [x] (if (nil? x) "\"\"" (str \" x \")))

;; (to-json [{:name "民" :age 26}
;;           {:name "明" :age 26}])

(def json-str "[{\"name\":\"jmjoy\",\"age\":18}]")

(defn test-parse [] (json/read-str json-str :key-fn keyword))

;; (json/write-str {:name "明明"} :escape-unicode true)

;; (if-not :condition1
;;   [false "message1"]
;;   (if-not :condition2
;;     [false "message2"]
;;     (if-not :condition3
;;       [false "message3"]
;;       [true "ok"])))

;; (if-not :condition1 [false "message1"])
;; (if-not :condition2 [false "message2"])
;; (if-not :condition3 [false "message3"])

;; (if :condition1 [false "message1"])

;; (-> (fn [] [true "ok"])
;;     #(if-not :condition1 [false "message1"] (% {})))

;; (json/read-str "\"TEST\" 123")

;; (fn [] (let [rd (Push)]))

(defn- decode-integer [reader]
  (let [sb (StringBuilder.)]
    (loop []
      (proc (:let c (.read reader))
            (:let ch (if (pos? c) (char c) \?))
            (case ch
              (\1 \2 \3 \4 \5 \6 \7 \8 \9 \0)
              (do (.append sb ch)
                  (recur))
             ;; else
              (do (.unread reader c)
                  (java.lang.Integer. (.toString sb))))))))

(defn decode-string [reader]
  (let [sb (StringBuilder.)]
    (loop []
      (let [c (.read reader)
            ch (char c)]
        (println "STRING:" ch)
        (case ch
          \"
          (str sb)
          ;; else
          (do
            (.append sb ch)
            (recur)))))))

(defn decode-array [reader]
  (let [arr (atom [])]
    (loop []
      (let [c (.read reader)
            ch (char c)]
        (println "ARRAY:" ch)
        (case ch
          \,
          (recur)

          \]
          @arr

         ;; else
          (do (.unread reader c)
              (swap! arr conj (from-json reader))
              (recur)))))))

(defn decode-object [reader]
  (let [obj (atom {})
        key (atom nil)]
    (loop []
      (let [c (.read reader)
            ch (char c)]
        (println "OBJECT:" ch)
        (case ch
          \:
          (recur)
          \,
          (do (reset! key nil)
              (recur))
          \}
          @obj
          ;; else
          (do (.unread reader c)
              (let [element (from-json reader)]
                (if (not @key) (reset! key element)
                    (swap! obj assoc @key element)))
              (recur)))))))

(defn from-json [x]
  (if (= String (class x))
    (from-json (PushbackReader. (StringReader. x)))
    (let [reader x]
      (loop []
        (proc (:let c (.read reader))
              (when (pos? c)
                (proc (:let ch (char c))
                      (println "MAIN: " ch)
                      (case ch
                        (\1 \2 \3 \4 \5 \6 \7 \8 \9 \0)
                        (do (.unread reader c)
                            (decode-integer reader))

                        \"
                        (decode-string reader)

                        \[
                        (decode-array reader)

                        \{
                        (decode-object reader)

                        ;; else
                        (throw (Exception. (str "Unkonw character " ch)))
                        ;; (do (println "FUCK:" ch) (recur))
                        ))))))))

;; (from-json "123456")

;; (from-json "\" TEST  YOU  \"123")

;; (from-json "[\"TEST YOU\",123,\"fuck\"]")

;; (from-json json-str)

