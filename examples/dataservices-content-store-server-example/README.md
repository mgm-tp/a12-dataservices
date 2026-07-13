# Data Services Content Store Server Examples

In this example module, you can find a way to run fully our module `dataservices-content-store-server-app`.

## How to Run

To check the running example, execute:

```shell
./gradlew bootRun
```

You can start this example extension by running the main method of the application class `ContentStoreServerExampleApplication` from `dataservices-content-store-server-example` module.

## Content Store Server Example Project Profiles

There are profiles which configure the behavior of example application differently that could be found at [config](src/main/resources/config) folder.

* `contentstore-example-uaa`: Additional UAA configuration with authentication type UAA_ACCESS_TOKEN.
* `contentstore-example-content_external_mime_type`: Profile provides configuration to set up uploading content API accepts external mime type from query parameter, and also makes this query parameter become mandatory.
* `contentstore-example-db_persister`: Configure (embedded) content storage with a database.
* `contentstore-example-fs_persister`: Configure (embedded) content storage with file system.

## Aggregated Profiles
Aggregated profiles is the combination of project profiles and built-in profiles from Content Store which could be found at [application.properties] file.

* `contentstore-example-common`: The common profile that is shared between aggregated profiles.
* `contentstore-example-trust-external-mime-type_env`: The profile which includes `contentstore-example-content_external_mime_type`
* `contentstore-example-http1_env`: The profile which includes `contentstore-http1_only` profile for supporting HTTP1 protocol only.

<!--- References--->
[application.properties]: src/main/resources/config/application.properties
