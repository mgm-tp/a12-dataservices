# Development Guide

This document provides guidelines for developing and maintaining the Data Services Codemod tool.

## Project Structure

```
dataservices-codemod/
├── src/
│   ├── cli.ts                          # CLI entry point
│   ├── recipes/                        # Codemod recipes
│   │   └── prefer-top-level-imports.ts # Import migration recipe
│   └── __tests__/                      # Test files
│       └── prefer-top-level-imports.test.ts
├── lib/                                # Compiled output (generated)
├── build.gradle                        # Gradle build configuration
├── package.json                        # NPM package configuration
├── tsconfig.json                       # TypeScript configuration
├── eslint.config.js                    # ESLint configuration
├── prettier.config.js                  # Prettier configuration
└── vitest.config.ts                    # Vitest test configuration
```

## Prerequisites

- Node.js 22+
- npm 10+
- Gradle (for integration with the monorepo build)

## Getting Started

### Initial Setup

Install dependencies:

```bash
npm install
```

### Development Commands

| Command | Description |
|---------|-------------|
| `npm run compile` | Compile TypeScript to JavaScript |
| `npm run start` | Watch mode - recompile on changes |
| `npm run test` | Run tests with Vitest |
| `npm run lint` | Run ESLint checks |
| `npm run lint:fix` | Auto-fix ESLint issues |
| `npm run format` | Check Prettier formatting |
| `npm run format:fix` | Auto-fix Prettier formatting |
| `npm run clean` | Remove build artifacts |
| `npm run initialize` | Run lint and format checks |

## Architecture

### Core Dependencies

- **@com.mgmtp.a12.devtools/codemod**: Core codemod framework providing CLI infrastructure and AST manipulation utilities
- **ts-morph** (via codemod framework): TypeScript AST manipulation library

### Creating a New Recipe

Recipes are self-contained transformation modules. To create a new recipe:

1. **Create the recipe file** in `src/recipes/`:

```typescript
import {
    type Recipe,
    // Import utilities as needed
} from "@com.mgmtp.a12.devtools/codemod";

export const myNewRecipe: Recipe = {
    metadata: {
        id: "my-new-recipe",
        description: "Description of what this recipe does",
        supportedVersions: "^38.2.0"  // Semver range of supported versions
    },

    execute(project): void {
        const sourceFiles = project.getSourceFiles();

        for (const sourceFile of sourceFiles) {
            // Apply transformations to each source file
        }
    }
};
```

2. **Register the recipe** in `src/cli.ts`:

```typescript
import { myNewRecipe } from "./recipes/my-new-recipe.js";

createCodemodCLI({
    name: "data-services-codemod",
    description: "Codemod tooling for assisting migrations of A12 Data Services",
    recipes: [
        preferTopLevelImportsRecipe,
        myNewRecipe  // Add new recipe here
    ]
});
```

3. **Add tests** in `src/__tests__/`:

```typescript
import { expect, it } from "vitest";
import { testRecipe } from "@com.mgmtp.a12.devtools/codemod";
import { myNewRecipe } from "../recipes/my-new-recipe.js";

it("should transform code correctly", async () => {
    await expect(
        testRecipe(
            myNewRecipe,
            `// Input code here`
        )
    ).resolves.toMatchInlineSnapshot(`
        "// Expected output here"
    `);
});
```

## Testing

### Running Tests

```bash
npm run test
```

### Writing Tests

Tests use Vitest with inline snapshots for expected output:

```typescript
it("should handle edge case", async () => {
    await expect(
        testRecipe(recipe, inputCode)
    ).resolves.toMatchInlineSnapshot(`"expected output"`);
});
```

To update snapshots after intentional changes:

```bash
npm run test -- --update
```

## Troubleshooting

### Common Issues

**Module resolution errors**

Ensure you're using `.js` extensions in imports (required for ESM):

```typescript
// Correct
import { recipe } from "./recipes/my-recipe.js";

// Incorrect
import { recipe } from "./recipes/my-recipe";
```

**TypeScript compilation errors**

Run `npm run compile` to see detailed error messages. The project uses strict TypeScript settings.

**Test failures after updates**

If snapshots are outdated, review the changes and update:

```bash
npx vitest --update
```

## Resources

- [ts-morph Documentation](https://ts-morph.com/)
- A12 Devtools Repository
- [AST Explorer](https://astexplorer.net/) - Useful for understanding TypeScript AST structure
