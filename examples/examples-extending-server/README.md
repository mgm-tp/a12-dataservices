<picture>
  <source media="(prefers-color-scheme: dark)" srcset="https://www.mgm-tp.com/global-content/cd/logos/a12/app-icons/dark/A12-Dark.svg" />
  <img src="https://www.mgm-tp.com/global-content/cd/logos/a12/app-icons/light/A12-Light.svg" height="200" alt="A12 logo" />
</picture>

# Data Services Extending Server Examples

In this example module, you can find a way to run fully our module `dataservices-server-app`.

Refer to https://geta12.com/#/docs to get started with A12 development

---

## License

Parts of the A12 platform are made available under a **dual license**.
Please check the [LICENSE](../../LICENSE) file for details.

---

## How to run

To check the running example, execute:

```shell
./gradlew bootRun
```

You can start these example extension by running main method of the application class `ExampleExtendingServerApplication` from `examples-extending-server` module.

If you want to enable virus scan for attachments with the profile `dataservices-example-virus_scan`, you have to run a [Clam AV] server.
```shell
./gradlew startClamAVContainer
```
to stop the [Clam AV] server.
```shell
./gradlew stopClamAVContainer
```

> **NOTE**: You need to specify `mgmtp.a12.uaa.authentication.client.rest.uaa-base.url` in case you want to run server with standalone content store.
>

## Example Project Profiles

There are profiles which configures the behavior of example application differently that could be found at [config](src/main/resources/config) folder.

* `dataservices-example-attachments`: Configure attachments and content store behavior.
* `dataservices-example-attachments-audit`: Example on how to use the {@link AttachmentBeforeCreateEvent} to store additional information when an attachment is created. See `AttachmentAuditService` for details.
* `dataservices-example-attachments-mime-type-custom` : Mimetype overwrite example. Replaces a configured mime-type with a replacement mime-type after DS probing. See `CustomZipTypeListener` for more details.
* `dataservices-example-attachments-encryption-async`: Shows how to encrypt/decrypt attachments asynchronously using `ContentBeforeCreateEvent` and `ContentAfterRequestEvent`. See `AttachmentEncryptionAsyncListeners` for more details.
* `dataservices-example-attachments-encryption-sync`: Example demonstrating how to encrypt and decrypt attachment content using synchronous event listeners. See `AttachmentEncryptionSyncListeners` for more details.
* `dataservices-example-attachments-virus_scan` : Enable virus scan for attachments with [Clam AV].
* `dataservices-example-attachments_probes_mime_type`: Profile provides configuration to set up Data Services for probing attachments mime type before persisting to Content Store.
* `dataservices-example-attachments_with_public_type_models`: Profile provides configuration to set up list of models which their attachment will be persisted as public type.
* `dataservices-example-abac`: Setting up additional ABAC rules and import some documents to prove that the ABAC rules are applied correctly (see tests in `AbacIT.java`)
* `dataservices-example-custom_type`: Executing RPC scripts to import required documents.
* `dataservices-example-uaa`: Additional UAA configuration.
* `dataservices-example-db-persister`: Configure attachment storage with database, this profile effect on embedded mode Content Store, in standalone mode Content Store the property should be set on Content Store server.
* `dataservices-example-fs-persister`: Configure attachment storage with file system, this profile effect on embedded mode Content Store, in standalone mode Content Store the property should be set on Content Store server.
* `dataservices-example-shared`: Shared configuration between configurations. For example, import of all models required by examples.
* `dataservices-example-import_models`: Import all [models](src/main/resources/model)

* `dataservices-example-documents-serialization`: Enables custom document (de)serialization configuration (pretty-print JSON, transient field handling, etc.).
* `dataservices-example-documents-static-code`: Enables use of statically generated validation code instead of dynamic generation from document models.
* `dataservices-example-documents-sequence-generator`: Enables custom database sequence based document ID generator.
* `dataservices-example-documents-external-enumeration`: Enables external enumeration loader example for BusinessPartner documents.
* `dataservices-example-documents-storage-ram`: Registers in-memory document repository example for BusinessPartner model.
* `dataservices-example-documents-metadata`: Enables custom document metadata. This example removes all fields except `docRef` and `modelReference`, and introduces two new fields (`metadataVersion` and `linkAssignment`) in the `extensions` group.
* `dataservices-example-documents-extension-model`: Enables model migration listeners (Base64 encode/decode of model content) for document models.
* `dataservices-example-documents-extension-document`: Enables document validation extension (address validation for ContactModel) listeners.
* `dataservices-example-documents-encryption`: Enables document content encryption/decryption listeners (Base64 encoding around persistence operations).

* `dataservices-example-black-box-authorization`: Enable to showcase black-box authorization use case where documents are removed from the result set after the query is resolved. This authorization breaks paging, but it is the only solution for the project that cannot specify their authorization rule using constraints in a query.
* `dataservices-example-business-partner-tax-authority-registration-status-projection`: Enables new query projection `businessPartnerTaxAuthorityRegistrationStatus` which enriches the query result by mocked response from 3rd party server. For more info see: `BusinessPartnerTaxAuthorityRegistrationStatus`
* `application-dataservices-example-business-partner-tax-authority-registration-status-no-op-projection.properties` : Replaces the functionality of the `businessPartnerTaxAuthorityRegistrationStatus` introduced by `dataservices-example-business-partner-tax-authority-registration-status-projection` with a no-op projection that does nothing in preprocess and postprocess. This profile is useful to showcase how to replace any existing projection with a custom implementation.

* `dataservices-example-attachments-audit`
* `dataservices-example-attachments-custom-mime-type`
* `dataservices-example-attachments-encryption-async`
* `dataservices-example-attachments-encryption-sync`
* `dataservices-example-attachments-mime-type-custom`
* `dataservices-example-attachments-virus_scan`

> **NOTE**: For your own additional profiles, we recommend to name it with prefix to avoid overlapping built-in Data Services profiles

## Aggregated Profiles
Aggregated profiles is the combination of project profiles and built-in profiles from Data Services which could be found at [application.properties] file.

* `dataservices-example-common`: The common profile that is shared between aggregated profiles.
* `dataservices-example-attachments_env`: The profile which includes `dataservices-example-attachments`
* `dataservices-example-authorization_env`: The profile which includes `dataservices-example-authorization`
* `dataservices-example-abac_env`: The profile which includes `dataservices-example-abac`
* `dataservices-example-custom_type-env`: The profile which includes `dataservices-example-custom_type`
* `dataservices-example-probes_attachment_mime_type_env`: The profile which includes `dataservices-example-attachments_probes_mime_type`
* `dataservices-example-http1-env`: The profile which includes `dataservices-http1-only` profile for supporting HTTP1 protocol only.
* `dataservices-example-attachments-thumbnail-graphic2d_env`: The profile which includes `dataservices-example-attachments-thumbnail-graphic2d` profile for supporting thumbnail generation by using GRAPHIC2D library.
* `dataservices-example-attachments-thumbnail-thumbnailator-conserve-memory_env`: The profile which includes `dataservices-example-attachments-thumbnail-thumbnailator` profile for supporting thumbnail generation by using THUMBNAILATOR library with in-memory temporary files and also enables work around conserve memory for THUMBNAILATOR.
* `dataservices-example-attachments-thumbnail-disk-cache_env`: The profile which includes `dataservices-example-attachments-thumbnail-disk-cache` profile for supporting thumbnail generation by using THUMBNAILATOR library with temporary files stored in file system.

> **NOTE**: By default, the `dataservices-example-attachments_env` profile is included for the example application. Please replace it with your expected aggregated profiles in [application.properties]
> ```properties
> spring.profiles.include=dataservices-example-attachments_env
>```


## Extensions
Within the example, there are extensions:

* Provide attachment extensions. [source code](src/main/java/com/mgmtp/a12/examples/attachment)
  * Attachment encrypt and decryption by listening to `AttachmentAfterLoadEvent` and `AttachmentBeforeCreateEvent` events.
  * Implementation of `IDirtyAttachmentCleanupCondition` interface for deleting dirty attachment.
  * Mime type detector with `CustomZipTypeDetector``
  * Virus scanning with [Clam AV] for attachments.
* Provide custom user authorization. [source code](src/main/java/com/mgmtp/a12/examples/authorization)
* Provide document encryption listener. [source code](src/main/java/com/mgmtp/a12/examples/document/encryption)
* Provide document metadata extension. [source code](src/main/java/com/mgmtp/a12/examples/document/metadata)
* Provide custom operation implementation. [source code](src/main/java/com/mgmtp/a12/examples/operation)
* Provide extended listener on link event. [source code](src/main/java/com/mgmtp/a12/examples/relationship)
* Provide extra Spring JPA Entity & Repository. [source code](src/main/java/com/mgmtp/a12/examples/extra)
* Custom document incremental ID generator using database sequence [source code](src/main/java/com/mgmtp/a12/examples/document/sequenceGenerator/SequenceIdGenerator.java)

<!--- References--->
[application.properties]: src/main/resources/config/application.properties
[Clam AV]: https://docs.clamav.net

---

## Documentation

- Full technical documentation is available at [GetA12.com](https://GetA12.com).
- The website also provides access to the **A12 Discourse Community Forum**.

---

**The mgm A12 Team**

[mgm technology partners GmbH](https://www.mgm-tp.com) • [Imprint](https://www.mgm-tp.com/imprint.html)
