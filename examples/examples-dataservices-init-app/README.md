# Data Services Init App Example

In this example module, you can find a way to extend our module `dataservices-server-init-app` by adding migration tasks to it.

## How to run
To check the running example, execute:

`./gradlew bootRun`

or you can use the main method of the application class `ExampleInitApplication`.

By default, these profiles are enabled in [application.properties]:
* `dataservices-embedded_contentstore`
* `dataservices-external_postgres`
* `dataservices-uaa`

The application initializes data with migration steps in an embedded PostgreSQL database and is terminated after initialization.

If you want to store initialized data in an external database such as postgres, you have to enable the profile `dataservices-external_postgres`
and configure the data source properly. For this example module, you could configure the data source within the [application-example_local_db.properties] then enable it in [application.properties]

```properties
spring.profiles.active=dataservices-embedded_contentstore,dataservices-uaa,dataservices-external_postgres,dataservices-init-example-local_db
```

## Migration

### Database migration
Database migration is done using Liquibase. You can see all defined changesets in the `dataservices-core/src/main/resources/database/project_model.xml`

### Migration Steps

After database migrations from Liquibase, the database schema is available. Now Data Services performs customized
migration step. You could find examples in [migrations](src/main/java/com/mgmtp/a12/examples/migrations) folder.

```java
// Mark this class as a migration step. Data Services will scan and execute this class.
@MigrationStep(version="34.2.0", name="MigrationStepOne", description="This is an example of a migration step")
@Slf4j
@Order(1) // Control the order of the step.
public class MigrationStepOne {

	// The executed code.
	@MigrationTask(name = "migrationOne")
	public void migrationOne() {
		log.info("Running the first migration of the version 1.0.0 according to the Order defined - using the annotation @MigrationTask here");
	}
}
```


<!--- References--->
[application.properties]: src/main/resources/config/application.properties
[application-dataservices-init-example_db.properties]: src/main/resources/application-dataservices-init-example-local_db.properties
