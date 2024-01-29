(ns walk
  (:require [com.rpl.specter :as sp]
            [clojure.walk :as walk]))

(defmacro qbench [& body]
  `(time
    (dotimes [_# 1000]
      ~@body)))

(def data  {:x [{}
                {:x 1
                 :y nil
                 :z [nil
                     {:nil nil
                      :y 11}]}
                nil]
            :y nil})
(qbench
 (walk/prewalk (fn [x]
                 (if (map? x)
                   (into {} (remove (fn [[_k v]] (nil? v)) x))
                   x))
               data))

(qbench
 (sp/setval [(sp/recursive-path
              [] p
              (sp/cond-path
               map?  [sp/MAP-VALS (sp/stay-then-continue p)]
               coll? [sp/ALL p]))
             nil?]
            sp/NONE
            data))

(def compilet-path
  (sp/comp-paths [(sp/recursive-path
                   [] p
                   (sp/cond-path
                    map?  [sp/MAP-VALS (sp/stay-then-continue p)]
                    coll? [sp/ALL p]))
                  nil?]))

(qbench
 (sp/compiled-setval compilet-path
                     sp/NONE
                     data))
