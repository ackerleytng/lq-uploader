(ns lq-uploader.uploader
  (:require [clj-ssh.ssh :refer [ssh-agent add-identity
                                 jump-session with-connection
                                 the-session ssh scp-to]]))

(defn- cleanup-host-info
  [host-info]
  (map (fn [item]
         (-> (select-keys item [:hostname :username :port])
             ;; Ugh. Limited deployments only, hence quick and dirty solution
             (assoc :strict-host-key-checking :no))) host-info))

(defn scp-file!
  "scp a file to remote through a bunch of hosts (specified in host-info)
  Example:
    (uploader/scp-file! host-info \"/Users/ackerleytng/gcloud/moooo\" \"/home/user/\")
  "
  [host-info src-path dst-path]
  (let [agent (ssh-agent {:use-system-ssh-agent false
                          :known-hosts-path "known_hosts"})]
    (doseq [host host-info]
      (add-identity agent {:private-key-path (:private-key-path host)}))
    (let [session (jump-session agent
                                (cleanup-host-info host-info)
                                {})]
      (with-connection session
        (scp-to (the-session session)
                src-path
                dst-path)))))

(defn exec-command!
  "ssh to remote through a bunch of hosts (specified in host-info) and execute a cmd
  Example:
    (uploader/exec-command! tmp \"rm -f moooo\")
  "
  [host-info cmd]
  (let [agent (ssh-agent {:use-system-ssh-agent false
                          :known-hosts-path "known_hosts"})]
    (doseq [host host-info]
      (add-identity agent {:private-key-path (:private-key-path host)}))
    (let [session (jump-session agent
                                (cleanup-host-info host-info)
                                {})]
      (with-connection session
        (ssh (the-session session) {:in cmd})))))
