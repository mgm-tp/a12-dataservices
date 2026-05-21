<picture>
  <source media="(prefers-color-scheme: dark)" srcset="https://www.mgm-tp.com/global-content/cd/logos/a12/app-icons/dark/A12-Dark.svg" />
  <img src="https://www.mgm-tp.com/global-content/cd/logos/a12/app-icons/light/A12-Light.svg" height="200" alt="A12 logo" />
</picture>

# Data Services Content Store Server Examples

In this example module, you can find a way to run fully our module `dataservices-content-store-server-app`.

Refer to https://geta12.com/#/docs to get started with A12 development

---

## License

Parts of the A12 platform are made available under a **dual license**.
Please check the [LICENSE](../../LICENSE) file for details.

---

## How to Run

To check the running example, execute:

```shell
./gradlew bootRun
```

You can start this example extension by running the main method of the application class `ContentStoreServerExampleApplication` from `dataservices-content-store-server-example` module.

## Content Store Server Example Project Profiles

There are profiles which configure the behavior of example application differently that could be found at [config](src/main/resources/config) folder.

* `contentstore-example-uaa`: Additional UAA configuration with authentication type UAA_ACCESS_TOKEN.
* `contentstore-example-content-external-mime-type`: Profile provides configuration to set up uploading content API accepts external mime type from query parameter, and also makes this query parameter become mandatory.
* `contentstore-example-db-persister`: Configure content storage with database.
* `contentstore-example-fs-persister`: Configure content storage with database.

## Aggregated Profiles
Aggregated profiles is the combination of project profiles and built-in profiles from Content Store which could be found at [application.properties] file.

* `contentstore-example-common`: The common profile that is shared between aggregated profiles.
* `contentstore-example-trust-external-mime-type-env`: The profile which includes `contentstore-example-content-external-mime-type`
* `contentstore-example-http1-env`: The profile which includes `contentstore-http1-only` profile for supporting HTTP1 protocol only.

<!--- References--->
[application.properties]: src/main/resources/config/application.properties

---

## Documentation

- Full technical documentation is available at [GetA12.com](https://GetA12.com).
- The website also provides access to the **A12 Discourse Community Forum**.

---

**The mgm A12 Team**

[mgm technology partners GmbH](https://www.mgm-tp.com) • [Imprint](https://www.mgm-tp.com/imprint.html)
