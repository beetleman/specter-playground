(ns scratch
  (:require [com.rpl.specter :as sp]))

(defn create-item [name type]
  {:name  name
   :type  type
   :price (+ (rand-int 100)
             10)
   :id    (random-uuid)})

(defn create-order [name items]
  {:name  name
   :it    (random-uuid)
   :items (mapv
           (fn [{:keys [name type]}]
             (create-item name type))
           items)})

(def order-1 (create-order "Order 1" [{:name "pizza"
                                       :type :food}
                                      {:name "salad"
                                       :type :food}
                                      {:name "water"
                                       :type :drink}
                                      {:name "popcorn"
                                       :type :food}]))

(defn drink? [{:keys [type]}]
  (= type :drink))

(defn food? [{:keys [type]}]
  (= type :food))

(defn add-glass-price [{:keys [price] :as item}]
  (-> item
      (assoc :orig/price price
             :price (inc price))
      (update ::operations (fnil conj []) :food-discount)))

(defn food-discount [{:keys [price] :as item}]
  (-> item
      (assoc
       :orig/price price
       :price (* 0.9 price))
      (update ::operations (fnil conj []) :food-discount)))

(def items [:items sp/ALL])

(sp/select [items food? :price] order-1)

(sp/select-any (sp/traversed [items food? :price] +) order-1)

(sp/select-any [items drink?]
               order-1)

(sp/multi-transform [items (sp/multi-path [drink? (sp/terminal add-glass-price)]
                                          [food? (sp/terminal food-discount)])]
                    order-1)

(sp/transform [sp/ALL items food? :price] inc [order-1])

(transduce (sp/traverse-all [items :price])
           +
           [order-1
            order-1
            order-1
            order-1])
