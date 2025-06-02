(ns summary
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
                       str/split-lines)
      git-commit-hashes (-> (bp/shell {:out :string
                                       :err nil
                                       :dir "."}
                                      (str "git log --pretty=format:%H -s"
                                           (when-not (str/blank? latest-tag) (str " " latest-tag "..HEAD"))))
                            :out
                            str/split-lines)]
  
  (print (reduce 
          (fn [output message-line] (str output " - " message-line "\n"))
          "# Deployment requires approval\n\nThe following commits will be deployed if approved:\n\n"
          (map (fn [hash msg] 
                        (let [exclamation-mark? (str/starts-with? msg "!")]
                          (str hash " " (when exclamation-mark? "**")
                               msg (when exclamation-mark? "**"))))
                      git-commit-hashes git-messages))))