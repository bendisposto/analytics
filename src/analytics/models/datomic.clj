(ns analytics.models.datomic
  (:require [datomic.api :as d])
  (:import java.text.SimpleDateFormat))


(def uri "datomic:free://localhost:4334/prob")
(def conn (d/connect uri))
(def db (d/db conn))

(defn fix-string [s] 
  (let [ws (.trim s)
        ws2 (or (second (re-matches  #".*\/(.*)\.pl" ws))
                (second (re-matches  #".*\/(.*)" ws)) ws)]
    (.replaceAll ws2 "\\." "_")))

(defn fix [[a b]] [(fix-string a) (fix-string b)])

(defn commits []
  (let [c (sort-by first (d/q '[:find ?committed-at ?message ?committer ?sha
                                :where
                                [?e :git/type :commit]
                                [?e :git/sha ?sha]
                                [?e :commit/message ?message]
                                [?e :commit/committedAt ?committed-at]
                                [?e :commit/committer ?m]
                                [?m :email/address ?committer]] db))]
    (reverse c)))

(def formatter (SimpleDateFormat. "dd.MM.yyyy HH:mm:ss"))

(defn format-timestamp [instant]
  (.format formatter instant))


(def rules
 '[[(node-files ?n ?f) [?n :node/object ?f] [?f :git/type :blob]]
   [(node-files ?n ?f) [?n :node/object ?t] [?t :git/type :tree] 
                       [?t :tree/nodes ?n2] (node-files ?n2 ?f)]
   [(object-nodes ?o ?n) [?n :node/object ?o]]
   [(object-nodes ?o ?n) [?n2 :node/object ?o] [?t :tree/nodes ?n2] (object-nodes ?t ?n)]
   [(commit-files ?c ?f) [?c :commit/tree ?root] (node-files ?root ?f)]
   [(commit-codeqs ?c ?cq) (commit-files ?c ?f) [?cq :codeq/file ?f]]
   [(file-commits ?f ?c) (object-nodes ?f ?n) [?c :commit/tree ?n]]
   [(codeq-commits ?cq ?c) [?cq :codeq/file ?f] (file-commits ?f ?c)]])

(defn files-for-commit [sha]
  (filter #(.endsWith (first %) ".pl") (d/q '[:find ?x :in $ % ?sha :where
                                              [?c :git/sha ?sha]
                                              (commit-files ?c ?b)
                                              [?f :node/object ?b]
                                              [?f :node/paths ?p]
                                              [?p :file/name ?x]] db rules sha)))


(def deps1 (d/q '[:find ?m1 ?m2 :where
                      [?m1 :module/use_module ?m2]] db))

(def deps2 (d/q '[:find ?m1 ?m2 :where
                      [?m1 :module/use_predicate ?m2]] db))


(defn codeqs-for-commit [sha type] (d/q '[:find ?codeq :in $ % ?sha ?type :where
                                          [?c :git/sha ?sha]
                                          (commit-codeqs ?c ?codeq)
                                          [?codeq :prolog/type ?type]] db rules sha type))

(defn filter-dependencies [sha deps]
  (let [codeqs (codeqs-for-commit sha :type/module)]
    (filter  (fn [[f t]] (.contains codeqs [f]))  deps)))

(defn resolve-module [attribute codeq]
  (fix-string (ffirst (d/q '[:find ?r :in $ ?m ?a :where 
                  [?m ?a ?mx]
                  [?mx :code/name ?r]] db codeq attribute))))

(defn resolve-modulename-from-module [[f t]]
  (let [res (partial resolve-module :module/name)] [(res f) (res t)]))

(defn resolve-modulename-from-predicate [[f t]]
  (let [rf (partial resolve-module :module/name)
        rs (partial resolve-module :predicate/module)] [(rf f) (rs t)]))


(defn d1 [sha] (map resolve-modulename-from-module (filter-dependencies sha deps1)))
(defn d2 [sha] (map resolve-modulename-from-predicate (filter-dependencies sha deps2)))

(def deps (memoize (fn  [sha] (into #{} (concat (d1 sha) (d2 sha))))))

(defn used-by [w sha] (into #{} (map second (filter (fn [[f _]] (= w f)) (deps sha)))))
(defn using [w sha] (into #{} (map first (filter (fn [[_ t]] (= w t)) (deps sha)))))
(defn all-modules [sha] (sort (into #{}  (map (fn [[e]] (resolve-module :module/name e)) (codeqs-for-commit sha :type/module))))) 
