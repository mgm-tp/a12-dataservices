<picture>
  <source media="(prefers-color-scheme: dark)" srcset="https://www.mgm-tp.com/global-content/cd/logos/a12/app-icons/dark/A12-Dark.svg" />
  <img src="https://www.mgm-tp.com/global-content/cd/logos/a12/app-icons/light/A12-Light.svg" height="200" alt="A12 logo" />
</picture>

# Content Store Server App

Refer to https://geta12.com/#/docs to get started with A12 development

---

## License

Parts of the A12 platform are made available under a **dual license**.
Please check the [LICENSE](../../LICENSE) file for details.

---

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

---

## Documentation

- Full technical documentation is available at [GetA12.com](https://GetA12.com).
- The website also provides access to the **A12 Discourse Community Forum**.

---

**The mgm A12 Team**

[mgm technology partners GmbH](https://www.mgm-tp.com) • [Imprint](https://www.mgm-tp.com/imprint.html)
