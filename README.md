# Customs Notification Push Retry

This service provides endpoints for the counting and deleting of flags that block push retries on customs notifications. 

## Endpoints Summary

| Path               | Method   | Description                                                                            |
|--------------------|----------|----------------------------------------------------------------------------------------|
| [/blocked-count](#count-blocked-flags-for-a-given-client-id)     | `GET`    | Returns a count of the number of notifications for the given clientId that are blocked |
| [/blocked-flag](#delete-blocked-flags-for-a-given-client-id)     | `DELETE` | Deletes the flags blocking notifications from being pushed for the given clientId      |


## Endpoints

### Count blocked flags for a given client id

    curl -v -X GET http://customs-notification-push-retry-host/blocked-count \
        -H 'X-Client-ID: AClientId' \
        -H 'Accept: application/vnd.hmrc.1.0+xml' \
        -H 'Authorization: Bearer {TOKEN}'

For successful responses the status is 200 OK along with an xml body specifying the count e.g.
    
    <pushNotificationBlockedCount>5</pushNotificationBlockedCount>

    
### Delete blocked flags for a given client id

    curl -X DELETE http://customs-notification-push-retry-host/blocked-flag \
        -H 'X-Client-ID: AClientId' \
        -H 'Accept: application/vnd.hmrc.1.0+xml' \
        -H 'Authorization: Bearer {TOKEN}'

For successful responses the status is 204 NO CONTENT. For requests where the clientId has no blocked requests then a 404 NOT FOUND is returned.

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").


