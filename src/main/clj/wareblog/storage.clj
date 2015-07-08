(ns wareblog.storage
  (:require [com.stuartsierra.component :as component]))

(defrecord Storage [options]
  component/Lifecycle
  (start [this]
    (println ";; Starting the storage")
    this)
  (stop [this]
    (println ";; Stopping the storage")
    this))
    
(defn storage-component [options]
  (map->Storage options))

(defn example-system [options]
  (-> (component/system-map
       :storage (storage-component options))
      (component/system-using
       {:storage []})))
