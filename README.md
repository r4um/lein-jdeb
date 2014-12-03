# lein-jdeb

A Leiningen plugin to create debian package from your project's uberjar.
Uses [jdeb](https://github.com/tcurdt/jdeb) to build the debian packages.

##  Leiningen

[![Clojars Project](http://clojars.org/lein-jdeb/latest-version.svg)](http://clojars.org/lein-jdeb)

## Installation

###With Leiningen 2

Add `[lein-jdeb "0.1.2"]` to your project's `:plugins`.

###With Leiningen 1

Add `[lein-jdeb "0.1.2"]` to your project's `:dev-dependencies`.

##  Usage

Add `:deb-maintainer` to your `project.clj` to add the [Maintainer](https://www.debian.org/doc/debian-policy/ch-controlfields.html#s-f-Maintainer) field for the package
and you should be able to build package.

Invoke via:

    $ lein jdeb

Other settings that are available and their defaults
* `:deb-control` Set this to a directory containing the control file. By default this is
   temporarily generated.
* `:deb-architecture` Sets [Architecture](https://www.debian.org/doc/debian-policy/ch-controlfields.html#s-f-Architecture). Set to `all` by default.
* `:deb-priority` Sets [Priority](https://www.debian.org/doc/debian-policy/ch-controlfields.html#s-f-Priority). Set to `optional` by default.
* `:deb-section` Sets [Section](https://www.debian.org/doc/debian-policy/ch-controlfields.html#s-f-Section). Set to `java` by default.
* `:deb-depends` Sets [Depends](). Set to `default-jre | java7-runtime | java6-runtime` by default

## License

Copyright © 2014 Pranay Kanwar

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
