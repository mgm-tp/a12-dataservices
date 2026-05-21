<picture>
  <source media="(prefers-color-scheme: dark)" srcset="https://www.mgm-tp.com/global-content/cd/logos/a12/app-icons/dark/A12-Dark.svg" />
  <img src="https://www.mgm-tp.com/global-content/cd/logos/a12/app-icons/light/A12-Light.svg" height="200" alt="A12 logo" />
</picture>

# Data Services Codemod

A command-line tool for automating code migrations when upgrading A12 Data Services.

Refer to https://geta12.com/#/docs to get started with A12 development

---

## License

Parts of the A12 platform are made available under a **dual license**.
Please check the [LICENSE](../../LICENSE) file for details.

---

## Overview

This codemod CLI helps automate repetitive code transformations required during Data Services version upgrades. It uses AST-based transformations to safely and accurately modify your TypeScript codebase.

## Run

```bash
npx @com.mgmtp.a12.dataservices/dataservices-codemod@latest <recipe-id> <source-directory-containing-tsconfig-file>
```

Or via pnpm:

```bash
pnpm dlx @com.mgmtp.a12.dataservices/dataservices-codemod@latest <recipe-id> <source-directory-containing-tsconfig-file>
```

## Usage

```bash
data-services-codemod <recipe-id> <tsconfig-path>

Codemod tooling for assisting migrations of A12 Data Services

Positionals:
  recipe-id      The ID of the recipe to run                                                [string]
  tsconfig-path  Path to a tsconfig file or a folder containing tsconfig.json (absolute or relative)
                                                                                            [string]

Options:
      --help         Show help                                                             [boolean]
      --version      Show version number                                                   [boolean]
  -l, --list         List all available recipes                           [boolean] [default: false]
  -i, --interactive  Run in interactive mode to select a recipe           [boolean] [default: false]
      --git-check    Check if git working directory is clean before running the recipe
                                                                           [boolean] [default: true]
```

## Available Recipes

### `prefer-top-level-imports`

**Supported versions:** `^38.2.0`

Migrates deep path imports from `@com.mgmtp.a12.dataservices/dataservices-access` to top-level imports.

#### Before

```typescript
import type { SupportedRequest } from "@com.mgmtp.a12.dataservices/dataservices-access/lib/dispatch/ResponseTypings.js";
import type { Attachment } from "@com.mgmtp.a12.dataservices/dataservices-access/lib/Attachment/attachment.js";
```

#### After

```typescript
import { SupportedRequest, Attachment } from "@com.mgmtp.a12.dataservices/dataservices-access";
```

## Example

Run the `prefer-top-level-imports` recipe on your project:

```bash
npx @com.mgmtp.a12.dataservices/dataservices-codemod prefer-top-level-imports ./client
```

---

## Documentation

- Full technical documentation is available at [GetA12.com](https://GetA12.com).
- The website also provides access to the **A12 Discourse Community Forum**.

---

**The mgm A12 Team**

[mgm technology partners GmbH](https://www.mgm-tp.com) • [Imprint](https://www.mgm-tp.com/imprint.html)
