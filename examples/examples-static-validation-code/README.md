# Static Code Validation Example

This example shows how to package generated Java and JavaScript code using the kernel API. Key points:

1. Generate code from complete runtime models, ensuring all includes and metadata are present.
2. Include code generation for `RelationshipMetaModel.json` (provided on the classpath by DS); otherwise, model imports will fail.
3. You can define any package for generated Java classes, but update the static config to include the package for code lookup.
4. JavaScript files can be generated in any directory, but update the static config to include the directory for code lookup.