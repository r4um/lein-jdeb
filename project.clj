(defproject lein-jdeb "1.0.0"
  :description "Create debian packages from leiningen project (based on jdeb)"
  :url "https://github.com/stackoverflow/lein-jdeb"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.vafer/jdeb "1.4"]
                 [me.raynes/fs "1.4.6"]]
  :eval-in-leiningen true)
