# Adding New Migrations

Each migration from one version to the next consists of the following parts:

* A Relationship model typing file
* A transformation function if necessary
* One or more test models to verify the correct transformation

## Relationship Model Typing

For each relationship model version, a `version-x.y.z` folder should be created in `src/internal/steps/`.
Afterwards, the current (updated) relationship model typing should be copied into another sub folder `src/internal/steps/version-x.y.z`.

## Transformation Function

If a transformation function is necessary, it should be added to the ```src/internal/steps/version-x.y.z/transform.ts``` file and exported default.

Remark: if the model changes in non-breaking ways or just upgrades to an official version,
which means only the `header.modelVersion` field should be updated, so no transformation function is needed
since the `@com.mgmtp.a12.migrationtool/migrationtool-core` will handle this automatically.

### Important Aspects to Keep in Mind

1. After copying the current relationship model typing and adding the optional transformation function, it needs to run ```compile``` script again
   to generate all schema.json files and compile the whole Typescript source files to ```lib``` directory.

2. The file ```src/internal/steps/index.ts``` will be generated on the fly, then compiled to ```lib/internal/steps/index.js``` automatically.
   In case of any compilation error, there will be a generated file ```index.ts``` (and also the temporary files ```schema.json```) in ```src/internal/steps``` folder, which can be used for debugging.
   After all the errors are fixed, re-run ```compile``` script again will delete all the temporary files.

3. Afterwards, only ```start``` script is needed to run to continue watching the changes in the source files.
