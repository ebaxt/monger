(ns monger.test.equality-test
  (:import org.bson.types.ObjectId)
  (:require [monger.core :as mg]
            [monger.collection :as mc]
            [clojure.test :refer :all]))

(let [conn (mg/connect)
      db   (mg/get-db conn "monger-test")
      coll "docs"]
  (defn purge-collections
    [f]
    (mc/purge-many db [coll])
    (f)
    (mc/purge-many db [coll]))

  (use-fixtures :each purge-collections)

  (deftest equalitytest
    (let [compound-id {:_id (ObjectId. "55dead3fcb6883719f31e408") :_version 1}]

      (mc/insert db coll {:_id compound-id :name "test"})

      (let [{id-from-db :_id} (mc/find-one-as-map db coll {:_id compound-id})]

        ;; the ids are equal
        (is (= id-from-db compound-id))

        ;; this passes
        (is (= (mc/find-one-as-map db coll {:_id compound-id})
               (mc/find-one-as-map db coll {"_id._id" (:_id id-from-db)
                                            "_id._version" (:_version id-from-db)})))

        ;; this does not, but did in Clojure 1.6
        (is (= (mc/find-one-as-map db coll {:_id id-from-db})
               (mc/find-one-as-map db coll {:_id compound-id})))))))

