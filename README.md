# playground-http
Reproduces an issue regarding https connection reuse (keep-alive) when using jackson unmarshaller, chunked transfer encoding and RestTemplate in default configuration.

## Running
* `./runServer.sh`
    * starts a small springboot app that simulates the server
    * issue is not related to springboot, occured in production on WAS8+J9
* `./runClient.sh`
    * calls service using curl for low level trace
    * calls service using `RestTemplate` in default config
    * calls service using `RestTemplate` with jackson's `AUTO_CLOSE_SOURCE`-Feature disabled

## Analysis
Observe the difference in log between the two client runs:
```
FINEST: Looking for HttpClient for URL https://localhost:8443/api and proxy value of DIRECT
FINEST: Creating new HttpsClient with url:https://localhost:8443/api and proxy:DIRECT with connect timeout:-1
```
```
FINEST: Looking for HttpClient for URL https://localhost:8443/api and proxy value of DIRECT
FINEST: KeepAlive stream retrieved from the cache, sun.net.www.protocol.https.HttpsClient(https://localhost:8443/api)
```

* When the `AUTO_CLOSE_SOURCE`-Feature is enabled, jackson closes the `HttpInputStream` in `com.fasterxml.jackson.core.json.UTF8StreamJsonParser._closeInput()` 
when the json doc is fully read. 
* The stream is not at EOF yet, though. This leads to a call to `sun.net.www.http.ChunkedInputStream.hurry()` which aims to read the remaining bytes. 
* This does not succeed fully, because in `sun.net.www.http.ChunkedInputStream.readAheadNonBlocking()` `in.available()` may not return all remaining bytes. 
* This can lead to a `ChunkedInputStream` that is not in state `STATE_DONE`. That will lead to it being closed, instead of kept alive.

## Problems
* JDK-HttpClient `hurry()` may leave unread bytes in the stream, which is probably okay, as close() was called prematurely.
* Jackson bz default calls `close()` prematurely, having not read all input.
* Spring provides Jackson in its default configuration on top of JDK-HttpClient.
