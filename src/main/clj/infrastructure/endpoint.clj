(ns infrastructure.endpoint
  )

(defprotocol Endpoint
  (get-routes [this])
)
