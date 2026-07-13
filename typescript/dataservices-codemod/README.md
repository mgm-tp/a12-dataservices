# Data Services Codemod

A command-line tool for automating code migrations when upgrading A12 Data Services.

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
