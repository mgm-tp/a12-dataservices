# JSON Schema Generator

This module provides a CLI tool for generating JSON Schema definitions from the operator and topology classes of the A12 query module. The generated schema is intended for internal use and should not be used by customer projects.

## Features

- Scans the query module for concrete implementations of operators, aggregation functions, and projections.
- Automatically generates a JSON Schema for the `QueryRoot` class.
- Outputs the schema to a configurable file path.
- Supports customization of base package and schema `$id`.

## Usage

### Build

Ensure you have Java and Gradle installed. Build the project using:

```shell
 gradlew build
```

### Build Output
The build process will generate two JAR files in the `build/libs` directory:
- `dataservices-json-schema-generator.jar`: The CLI tool for generating JSON Schema.
- `dataservices-json-schema-schema.jar`: A JAR containing the JSON schema.

### Run

Execute the CLI tool to generate the schema:

```shell
java -jar build/libs/dataservices-json-schema-generator.jar [options]
```

#### Options

- `-o`, `--schema-output`
  Output path for the generated schema (default: `build/schema/query-root-dataservices-vanila.json`)

- `-p`, `--base-package`
  Base package to scan for implementations (default: `com.mgmtp.a12.dataservices`)

- `--schema-id`
  Value for the `$id` field in the generated schema (default: `https://mgm-tp.com/a12/dataservices/schema/query-root-dataservices-vanila.json`)

### Example

```shell
java -jar build/libs/dataservices-json-schema-generator.jar -o build/schema/query-schema.json
```
