# Custom Range Operator Example

This package demonstrates how to implement custom range operators for custom field types using the SearchCustomizer infrastructure.

### Query Side

1. **TaxNumberRangeOperator** (`dataservices-domain` module)
   - Extends `NumericRangeOperator<Long>`
   - Provides query constraint for numeric range queries on TaxID fields
   - Operator name: `tax_number_range`

2. **TaxNumberRangeOperatorSqlGenerator** (`dataservices-core` module)
   - Extends `NumericRangeOperatorSqlGenerator<Long, TaxNumberRangeOperator>`
   - Generates SQL: `WHERE field_name = ? AND number_value BETWEEN ? AND ?`
   - Queries the indexed `number_value` column

### Indexing Side (SearchCustomizer Infrastructure)

3. **SearchCustomizer Interface** (`dataservices-domain` module)
   - `SearchCustomizer` interface with 3 methods for customizing indexing
   - Context interfaces: `SearchDataContext`, `DocumentFieldContext`, `ModelFieldsContext`

4. **SearchCustomizerRegistry** (`dataservices-core` module)
   - Auto-discovers all `SearchCustomizer` Spring beans
   - Zero-overhead design: `hasCustomizers()` check before processing
   - Integrated into `DocumentSearchIndexBehaviour` for indexing

5. **TaxIDSearchCustomizer** (this module)
   - Extracts numeric component from TaxID values (format: "US12345678")
   - Indexes 8-digit numeric part to `number_value` column
   - Source identifier: `tax_id_customizer`

6. **Database Migration** (`dataservices-core` module)
   - Liquibase changeset A12S-6471 in version 39.0.0
   - Adds `source` column to `document_fields` table

### Configuration

- Property-based activation: `com.mgmtp.a12.examples.custom-operator.enabled=true`
- `RenamedTaxIDCustomFieldTypeFactory` and `TaxIDSearchCustomizer` use `@ConditionalOnProperty`

### Integration Tests

- `TaxNumberRangeOperatorIT` with 5 test cases (all passing)
- Test documents: PersonTaxNumber_A/B/C.json with TaxIDs US10000000, US50000000, US90000000

## Usage

Query documents by TaxID numeric range:

```json
{
    "operator": "tax_number_range",
    "field": "/Person/TaxIDCustomFieldType",
    "from": "US10000000",
    "to": "US50000000"
}
```

### Query Examples

**Range Query - Between Values:**
```json
{
    "operator": "tax_number_range",
    "field": "/Person/TaxIDCustomFieldType",
    "from": "US10000000",
    "to": "US50000000"
}
```
Returns documents with TaxIDs US10000000 and US50000000.

**Range Query - Above Value:**
```json
{
    "operator": "tax_number_range",
    "field": "/Person/TaxIDCustomFieldType",
    "from": "US50000001"
}
```
Returns documents with TaxID US90000000.

**Range Query - Below Value:**
```json
{
    "operator": "tax_number_range",
    "field": "/Person/TaxIDCustomFieldType",
    "to": "US49999999"
}
```
Returns documents with TaxID US10000000.

## Testing

Run integration tests:

```bash
./gradlew :examples:examples-extending-server:test --tests TaxNumberRangeOperatorIT
```

## Architecture

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           Document Indexing Flow                            │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  Document                                                                   │
│     │                                                                       │
│     ▼                                                                       │
│  DocumentSearchIndexBehaviour                                               │
│     │                                                                       │
│     ├─── Core Indexing ──► document_fields (source='core')                  │
│     │                                                                       │
│     └─── SearchCustomizerRegistry.hasCustomizers() ──► if true:             │
│              │                                                              │
│              ▼                                                              │
│          TaxIDSearchCustomizer.customizeDocumentFields()                    │
│              │                                                              │
│              ▼                                                              │
│          Extract numeric: "US50000000" → 50000000                           │
│              │                                                              │
│              ▼                                                              │
│          document_fields (source='tax_id_customizer', number_value=50000000)│
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│                              Query Flow                                     │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  Query: { "operator": "tax_number_range", "from": 10000000, "to": 50000000 }│
│     │                                                                       │
│     ▼                                                                       │
│  TaxNumberRangeOperator (constraint)                                        │
│     │                                                                       │
│     ▼                                                                       │
│  TaxNumberRangeOperatorSqlGenerator                                         │
│     │                                                                       │
│     ▼                                                                       │
│  SQL: SELECT ... WHERE field_name = ? AND number_value BETWEEN ? AND ?      │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

## TaxID Format

- Pattern: `([A-Z]{2})(\d{8})`
- Example: `US50000000`
  - Group 1: Identifier (`TAX`)
  - Group 2: Numeric part (`50000000`) → indexed to `number_value`

## Files

| File | Module | Description |
|------|--------|-------------|
| `TaxNumberRangeOperator.java` | dataservices-domain | Query constraint |
| `TaxNumberRangeOperatorSqlGenerator.java` | dataservices-core | SQL generation |
| `SearchCustomizer.java` | dataservices-domain | Customization interface |
| `DocumentFieldContext.java` | dataservices-domain | Context for field customization |
| `SearchCustomizerRegistry.java` | dataservices-core | Registry with auto-discovery |
| `TaxIDSearchCustomizer.java` | examples-extending-server | Example implementation |
| `TaxNumberRangeOperatorIT.java` | examples-extending-server | Integration tests |

## References

- Technical Analysis: `~/Downloads/index (62).pdf` (pages 24-31)
- JIRA Ticket: A12S-6471
- Documentation: `src/docs/asciidoc/query/custom_range_operators.adoc`
- QFT Test Spec: `qa/regression_tests/src/main/qftest/dataservices/extension/03ext_Custom_Operator_TODO.md`
