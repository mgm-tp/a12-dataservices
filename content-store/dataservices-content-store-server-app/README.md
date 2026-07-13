# Content Store Server App

## To run server, use:
```
./gradlew :content-store:dataservices-content-store-server-app:bootRun
```
Default, Content Store Server always runs with dataservices, so authentication type is `UAA_ACCESS_TOKEN`.

## To run server with local authentication type, use
```
./gradlew :content-store:dataservices-content-store-server-app:runServer
```

## Example of How to Run This Module
Refer to our `:examples` module, to see `dataservices-content-store-server-example`, this example shows the way how to set up and run Content Store application properly.
