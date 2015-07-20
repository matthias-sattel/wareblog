(ns wareblog.article-storage)

(defprotocol Article-Storage
  (get-by-id [this id])
  )
