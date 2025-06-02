(ns gate
  (:require
   [babashka.process :as bp]
   [clojure.string :as str]))

(let [latest-tag (-> (bp/shell {:out :string
                                :err nil
                                :continue true
                                :dir "."}
                               "git describe --tags --abbrev=0 --match=\"deployed-*\"")
                     :out
                     str/split-lines
                     first)
      git-messages (-> (bp/shell {:out :string
                                  :err nil
                                  :dir "."}
                                 (str "git log --pretty=format:%s -s"
                                      (when-not (str/blank? latest-tag) (str " " latest-tag "..HEAD"))))
                       :out
                       str/split-lines)]
  (if (some #(str/starts-with? % "!") git-messages)
    (print "BLOCKED")
    (print "NOT_BLOCKED")))