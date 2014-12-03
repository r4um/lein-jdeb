(ns leiningen.deb
  (:require [leiningen.core.main :as lm]
            [leiningen.uberjar :as uj]
            [clojure.java.io :as io]
            [me.raynes.fs :refer [temp-dir file]])
  (:import [org.vafer.jdeb DebMaker Console]
           [org.vafer.jdeb.producers DataProducerFile DataProducerLink])
  (:gen-class))

(def console
  (reify org.vafer.jdeb.Console
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

(defn deb
  "Create debian package from uberjar"
  [project & args]
  (let [package (project :name)
        version (project :version)
        description (project :description)
        homepage (project :url)
        control-dir (project :deb-control-dir)
        maintainer (project :deb-maintainer)
        architecture (project :deb-architecture "all")
        section (project :deb-section "java")
        depends (project
                 :deb-depends
                 "default-jre | java7-runtime | java6-runtime")
        priority (project :deb-priority "optional")
        pkg-name (deb-pkg-name package version)
        uberjar (uj/uberjar project)
        uberjar-name (.getName (io/file uberjar))
        uberjar-pkg-path (.getPath (io/file "usr" "share" "java" uberjar-name))
        uberjar-symlink-name (str package ".jar")
        uberjar-symlink-pkg-path (.getPath (io/file "usr" "share" "java" uberjar-symlink-name))
        df (DataProducerFile. (io/file uberjar) uberjar-pkg-path nil nil nil)
        dl (DataProducerLink. uberjar-symlink-pkg-path uberjar-name true nil nil nil)
        dm (DebMaker. console [df dl] nil)]

    ;; If user specified control dir use that, else create control in temp
    ;; directory with minimum required control fields
    (if control-dir
      (.setControl dm (file control-dir))
      (.setControl dm
                   (create-temp-control
                    architecture maintainer priority version)))
    (.setDeb dm (file pkg-name))
    (.setDepends dm depends)
    (.setDescription dm description)
    (.setHomepage dm homepage)
    (.setPackage dm package)
    (.setSection dm section)
    (.makeDeb dm)))
