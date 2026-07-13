# Data Services Extending Server Examples

In this example module, you can find a way to run fully our module `dataservices-server-app`.

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
* `dataservices-example-attachments_audit`: Example on how to use the {@link AttachmentBeforeCreateEvent} to store additional information when an attachment is created. See `AttachmentAuditService` for details.
* `dataservices-example-attachments_mime_type_custom` : Mimetype overwrite example. Replaces a configured mime-type with a replacement mime-type after DS probing. See `CustomZipTypeListener` for more details.
* `dataservices-example-attachments_encryption_async`: Shows how to encrypt/decrypt attachments asynchronously using `ContentBeforeCreateEvent` and `ContentAfterRequestEvent`. See `AttachmentEncryptionAsyncListeners` for more details.
* `dataservices-example-attachments_encryption_sync`: Example demonstrating how to encrypt and decrypt attachment content using synchronous event listeners. See `AttachmentEncryptionSyncListeners` for more details.
* `dataservices-example-attachments_virus_scan` : Enable virus scan for attachments with [Clam AV].
* `dataservices-example-attachments_probes_mime_type`: Profile provides configuration to set up Data Services for probing attachments mime type before persisting to Content Store.
* `dataservices-example-attachments_with_public_type_models`: Profile provides configuration to set up list of models which their attachment will be persisted as public type.
* `dataservices-example-authorization_uaa`: Setting up additional ABAC rules and import some documents to prove that the ABAC rules are applied correctly (see tests in `AbacIT.java`)
* `dataservices-example-authorization_black_box`: Enable to showcase black-box authorization use case where documents are removed from the result set after the query is resolved. This authorization breaks paging, but it is the only solution for the project that cannot specify their authorization rule using constraints in a query.
* `dataservices-example-custom_type`: Executing RPC scripts to import required documents.
* `dataservices-example-uaa`: Additional UAA configuration.
* `dataservices-example-db_persister`: Configure attachment storage with database, this profile has effect on embedded mode Content Store, in standalone mode Content Store the property should be set on Content Store server.
* `dataservices-example-fs_persister`: Configure attachment storage with file system, this profile has effect on embedded mode Content Store, in standalone mode Content Store the property should be set on Content Store server.
* `dataservices-example-shared`: Shared configuration between configurations. For example, import of all models required by examples.
* `dataservices-example-documents_serialization`: Enables custom document (de)serialization configuration (pretty-print JSON, transient field handling, etc.).
* `dataservices-example-extension_jackson`: Enables custom Jackson `ObjectMapper` configuration by registering a `JacksonModule` bean with custom serializers/deserializers. See `ExampleJacksonConfiguration` for details.
* `dataservices-example-documents_static_code`: Enables use of statically generated validation code instead of dynamic generation from document models.
* `dataservices-example-documents_sequence_generator`: Enables custom database sequence based document ID generator.
* `dataservices-example-documents_external_enumeration`: Enables external enumeration loader example for BusinessPartner documents.
* `dataservices-example-documents_storage_ram`: Registers in-memory document repository example for BusinessPartner model.
* `dataservices-example-documents_metadata`: Enables custom document metadata. This example removes all fields except `docRef` and `modelReference`, and introduces two new fields (`metadataVersion` and `linkAssignment`) in the `extensions` group.
* `dataservices-example-documents_extension_model`: Enables model migration listeners (Base64 encode/decode of model content) for document models.
* `dataservices-example-documents_extension_document`: Enables document validation extension (address validation for ContactModel) listeners.
* `dataservices-example-documents_encryption`: Enables document content encryption/decryption listeners (Base64 encoding around persistence operations).
* `dataservices-example-business_partner_tax_authority_registration_status_projection`: Enables new query projection `businessPartnerTaxAuthorityRegistrationStatus` which enriches the query result by mocked response from 3rd party server. For more info see: `BusinessPartnerTaxAuthorityRegistrationStatus`
* `application-dataservices-example-business_partner_tax_authority_registration_status_no_op_projection.properties` : Replaces the functionality of the `businessPartnerTaxAuthorityRegistrationStatus` introduced by `dataservices-example-business_partner_tax_authority_registration_status_projection` with a no-op projection that does nothing in preprocess and postprocess. This profile is useful to showcase how to replace any existing projection with a custom implementation.

> **NOTE**: For your own additional profiles, we recommend to name it with prefix to avoid overlapping built-in Data Services profiles

## Aggregated Profiles
Aggregated profiles is the combination of project profiles and built-in profiles from Data Services which could be found at [application.properties] file.

* `dataservices-example-common`: The common profile that is shared between aggregated profiles.
* `dataservices-example-attachments_env`: The profile which includes `dataservices-example-attachments`
* `dataservices-example-authorization_env`: The profile which includes `dataservices-example-authorization`
* `dataservices-example-authorization-uaa_env`: The profile which includes `dataservices-example-authorization_uaa`
* `dataservices-example-custom-type_env`: The profile which includes `dataservices-example-custom_type`

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
