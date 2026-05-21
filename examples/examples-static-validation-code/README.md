<picture>
  <source media="(prefers-color-scheme: dark)" srcset="https://www.mgm-tp.com/global-content/cd/logos/a12/app-icons/dark/A12-Dark.svg" />
  <img src="https://www.mgm-tp.com/global-content/cd/logos/a12/app-icons/light/A12-Light.svg" height="200" alt="A12 logo" />
</picture>

# Static Code Validation Example

This example shows how to package generated Java and JavaScript code using the kernel API. Key points:

Refer to https://geta12.com/#/docs to get started with A12 development

---

## License

Parts of the A12 platform are made available under a **dual license**.  
Please check the [LICENSE](../../LICENSE) file for details.

---

## Key Points

1. Generate code from complete runtime models, ensuring all includes and metadata are present.
2. Include code generation for `RelationshipMetaModel.json` (provided on the classpath by DS); otherwise, model imports will fail.
3. You can define any package for generated Java classes, but update the static config to include the package for code lookup.
4. JavaScript files can be generated in any directory, but update the static config to include the directory for code lookup.

---

## Documentation

- Full technical documentation is available at [GetA12.com](https://GetA12.com).
- The website also provides access to the **A12 Discourse Community Forum**.

---

**The mgm A12 Team**

[mgm technology partners GmbH](https://www.mgm-tp.com) • [Imprint](https://www.mgm-tp.com/imprint.html)
