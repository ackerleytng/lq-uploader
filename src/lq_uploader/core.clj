(ns lq-uploader.core
  (:require [clojure.edn :as edn]
            [clojure.spec.alpha :as s]
            [lq-uploader.uploader :as uploader]
            [seesaw.core :refer [config! pack! show! frame border-panel
                                 scrollable listbox selection alert]]
            [seesaw.dnd :as dnd]
            [clojure.string :as str])
  (:import [java.io FileNotFoundException])
  (:gen-class))

(defn load-host-info
  "Given a filename, load & return host info"
  [filename]
  (edn/read-string (slurp filename)))

;; ----------------------------
;; host-info validation

;; https://stackoverflow.com/questions/6949667/what-are-the-real-rules-for-linux-usernames-on-centos-6-and-rhel-6
(defonce username-regex #"[a-z_][a-z0-9_]{0,30}")

(s/def ::hostname string?)
(s/def ::port int?)
(s/def ::username (s/and string? #(re-matches username-regex %)))
(s/def ::private-key-path (s/and string?))

(s/def ::host-info-item (s/keys :req-un [::hostname ::port ::username ::private-key-path]))
(s/def ::host-info (s/and seqable? #(every? (fn [item] (s/valid? ::host-info-item item)) %)))

(defn validate-host-info
  [host-info]
  (or (s/valid? ::host-info host-info)
      (throw (Exception.
              (str/join "\n"
                        (conj (map #(s/explain-str ::host-info-item %) host-info)
                              (s/explain-str ::host-info host-info)
                              "Error"))))))

;; ----------------------------
;; doing the upload

(defonce remote-image-path "/var/www/html/image/catalog/covers/")
(def host-info (atom nil))

(defn- do-upload!
  ([file] (do-upload! file false))
  ([file retry]
   (let [full-path (.getAbsolutePath file)
         basename (.getName file)]
     (try
       (uploader/scp-file! @host-info full-path remote-image-path)
       (catch com.jcraft.jsch.JSchException e
         (let [msg (.getMessage e)]
           (when (and (str/includes? msg "known_hosts")
                      (str/includes? msg "FileNotFoundException"))
             (spit "known_hosts" "")
             (if retry
               (throw e)
               (do-upload! file true))))))
     (alert (str "Done uploading " basename "!")))))

;; ----------------------------
;; gui stuff

(defn- file-target []
  (listbox
   :model []
   :drag-enabled? true
   :drop-mode :insert
   :transfer-handler
   (dnd/default-transfer-handler
    :import [dnd/file-list-flavor (fn [{:keys [target data]}]
                                    ;; data is always List<java.io.File>
                                    (doseq [file data]
                                      (do-upload! file)))]
    :export {
             :actions (constantly :copy)
             :start   (fn [c]
                        (let [file (selection c)]
                          [dnd/file-list-flavor [file]]))
             ;; No :finish needed
             })))

(defn- main-frame []
  (frame
   :title "lq-uploader"
   :content (border-panel :border "Drop file here to upload"
                          :center (scrollable (file-target)))))

;; ----------------------------
;; main stuff

(defn- load-default-config! []
  (let [info (load-host-info "config.edn")]
    (when (validate-host-info info)
      (reset! host-info info)
      true)))

(defn -main
  [& args]
  (and (try
         (load-default-config!)
         (catch Exception e
           (alert (.getMessage e))))
       (-> (main-frame)
           (config! :on-close :exit)
           pack!
           show!)))
