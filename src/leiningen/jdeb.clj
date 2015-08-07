(ns leiningen.jdeb
  (:require [leiningen.core.main :as lm]
            [clojure.java.io :as io]
            [me.raynes.fs :refer [temp-dir file]])
  (:import [org.vafer.jdeb DebMaker Console]
           [org.vafer.jdeb.mapping Mapper PermMapper NullMapper]
           [org.vafer.jdeb.producers DataProducerFile DataProducerDirectory DataProducerPathTemplate])
  (:gen-class))

(def console
  (reify Console
    (info [_ m]
      (lm/info m))
    (warn [_ m]
      (lm/warn m))
    (debug [_ m]
      (lm/debug m))))

(defn create-temp-control
  "Create control file in temporary directory with minimum fields"
  [a m p v]
  (let [td (temp-dir "lein-deb")]
    (with-open [f (io/writer (file td "control"))]
      (.write f (str "Architecture: " a))
      (.newLine f)
      (.write f (str "Maintainer: " m))
      (.newLine f)
      (.write f (str "Priority: " p))
      (.newLine f)
      (.write f (str "Version: " v))
      (.newLine f)
      (.flush f))
    td))

(defn deb-pkg-name
  "Build debian package name"
  [p v]
  (str p "_" v ".deb"))

(defmulti mapper :type)

(defmethod mapper :perm [m]
  (PermMapper. (:uid m -1) (:gid m -1) (:user m) (:group m) (:filemode m "") (:dirmode m "") (:strip m 0) (:prefix m)))

(defmethod mapper :null [_]
  NullMapper/INSTANCE)

(defmethod mapper :default [_] nil)

(defmulti process-data :type)

(defmethod process-data :file [data]
  (DataProducerFile. (io/file (:src data)) (:dst data)
                     (into-array String (:includes data)) (into-array String (:excludes data))
                     (into-array Mapper [(mapper (:mapper data))])))

(defmethod process-data :directory [data]
  (DataProducerDirectory. (io/file (:src data)) (into-array String (:includes data)) (into-array String (:excludes data))
                          (into-array Mapper [(mapper (:mapper data))])))

(defmethod process-data :template [data]
  (DataProducerPathTemplate. (into-array String (:paths data))
                             (into-array String (:includes data)) (into-array String (:excludes data))
                             (into-array Mapper [(mapper (:mapper data))])))

(defn process-data-set [data-set]
  (let [confs (atom [])
        producers (mapv (fn [data]
                          (let [producer (process-data data)]
                            (when (:conffile data)
                              (swap! confs conj producer))
                            producer))
                        data-set)]
    [producers @confs]))

(defn jdeb
  "Create debian package from project.clj configuration"
  [project & args]
  (let [conf (project :jdeb)
        package (project :name)
        version (project :version)
        description (project :description)
        homepage (project :url)
        control-dir (:deb-control-dir conf)
        maintainer (:deb-maintainer conf)
        architecture (:deb-architecture conf "all")
        section (:deb-section conf "java")
        depends (:deb-depends conf)
        priority (:deb-priority conf "optional")
        pkg-name (deb-pkg-name package version)
        [producers confs] (process-data-set (:data-set conf))
        dm (DebMaker. console producers confs)]
    ;; If user specified control dir use that, else create control in temp
    ;; directory with minimum required control fields
    (if control-dir
      (.setControl dm (file control-dir))
      (.setControl dm
                   (create-temp-control
                    architecture maintainer priority version)))
    (.setDeb dm (file pkg-name))
    (if depends
      (.setDepends dm depends))
    (.setDescription dm description)
    (.setHomepage dm homepage)
    (.setPackage dm package)
    (.setSection dm section)
    (.makeDeb dm)))
