<picture>
  <source media="(prefers-color-scheme: dark)" srcset="https://www.mgm-tp.com/global-content/cd/logos/a12/app-icons/dark/A12-Dark.svg" />
  <img src="https://www.mgm-tp.com/global-content/cd/logos/a12/app-icons/light/A12-Light.svg" height="200" alt="A12 logo" />
</picture>

# Data Services Server App

The Data Services Server Application component for A12.

Refer to https://geta12.com/#/docs to get started with A12 development

---

## License

Parts of the A12 platform are made available under a **dual license**.
Please check the [LICENSE](../LICENSE) file for details.

---

## Troubleshoot

1. Springboot devtools cause the application stop during application startup

   We were facing problem with `springboot devtools`, when devtools trying to restart application server it will trigger
   immediate restart which throw `
   org.springframework.boot.devtools.restart.silentexitexceptionhandler$SilentExitException`. If this exception is
   thrown at application startup time and `DS` will exit the whole application because we're assuming no unhandled
   exception occurs when initializing application.

---

## Documentation

- Full technical documentation is available at [GetA12.com](https://GetA12.com).
- The website also provides access to the **A12 Discourse Community Forum**.

---

**The mgm A12 Team**

[mgm technology partners GmbH](https://www.mgm-tp.com) • [Imprint](https://www.mgm-tp.com/imprint.html)
