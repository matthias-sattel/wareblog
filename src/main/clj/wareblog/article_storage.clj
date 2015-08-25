(ns wareblog.article-storage)

(defprotocol Article-Storage

  (get-by-id [this id])

  (get-by-title [this title])

  (get-all [this])

  (import-from-file [this path-to-file])
  
  )
