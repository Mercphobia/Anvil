---
name: javascript-conventions
description: JavaScript/TypeScript conventions — always load for Node.js and browser JS projects. Covers ESM vs CommonJS, package.json scripts, async/await, and linting.
applies_to: [NODE_JS]
---

# JavaScript/TypeScript Conventions

You are writing JavaScript or TypeScript. These rules apply to every JS/TS file.

## 1. Module System: Prefer ESM

- Use ES modules: `import`/`export` syntax. Avoid `require()` / `module.exports` unless the project already uses CommonJS.
- `package.json` must include `"type": "module"` for `.js` files to use ESM.
- For TypeScript, set `"module": "ESNext"` and `"moduleResolution": "bundler"` in `tsconfig.json`.
- Use `.mjs` extension for ESM and `.cjs` for CommonJS when both coexist.
- Top-level `await` is available in ESM modules (Node 14.8+).

```json
// package.json
{
  "type": "module",
  "scripts": {
    "start": "node src/index.js"
  }
}
```

```javascript
// Good: ESM import
import { readFile } from 'node:fs/promises';
export const data = await readFile('input.txt', 'utf-8');
```

## 2. Package.json Scripts

- Define `start`, `build`, `test`, `lint`, and `dev` scripts.
- Scripts must be OS-agnostic: never use `&&` for sequential commands (prefer `npm-run-all` or separate script entries).
- CI scripts (`lint`, `test`, `build`) must exit non-zero on failure.
- Use `npm run <script>` or `yarn <script>`; never the bare `node` command for project entrypoints.

```json
{
  "scripts": {
    "dev": "tsx watch src/index.ts",
    "build": "tsc --noEmit && vite build",
    "start": "node dist/index.js",
    "test": "vitest run",
    "lint": "eslint . && prettier --check .",
    "format": "prettier --write ."
  }
}
```

## 3. Async/Await — Always

- Never use raw promise `.then()` / `.catch()` chains. Always `async`/`await`.
- Every `await` that can reject must be wrapped in try/catch or have a `.catch()` at the call site.
- Use `Promise.all()` for parallel independent work; `Promise.allSettled()` when partial failure is acceptable.
- Do not `await` in a loop when the calls are independent — collect promises and `await Promise.all()`.

```javascript
// Bad: sequential awaits in a loop
for (const url of urls) {
  const data = await fetch(url); // each fetch waits for the previous
}

// Good: parallel
const results = await Promise.all(urls.map(url => fetch(url)));
```

## 4. Error Handling

- Throw `Error` instances, never strings or raw objects.
- Attach context to errors: `throw new Error('Failed to parse config', { cause: e })`.
- Always handle rejected promises: dangling promises without `.catch()` are bugs.
- In Express/HTTP handlers, use a centralized error middleware; never let exceptions crash the server.

```javascript
// Good: contextual errors with cause chaining
try {
  const config = JSON.parse(raw);
} catch (e) {
  throw new Error(`Invalid config at ${path}`, { cause: e });
}
```

## 5. TypeScript Specifics

- Enable strict mode in tsconfig: `"strict": true`.
- Prefer `interface` for object shapes; `type` for unions, intersections, and mapped types.
- Avoid `any` — use `unknown` and narrow with type guards.
- Export types explicitly with `export type { ... }` when the import side uses `import type`.
- Never use `as` casts to bypass type errors — fix the types or use a type guard.

```typescript
// Good: type guards instead of as
function isUser(obj: unknown): obj is User {
  return typeof obj === 'object' && obj !== null && 'id' in obj && 'name' in obj;
}
```

## 6. Linting and Formatting

- ESLint for code quality; Prettier for formatting.
- Use a shared config: `@typescript-eslint/recommended-type-checked` for TypeScript.
- `.prettierrc`: single quotes, trailing commas, 100-character print width, semicolons.
- Run linting in CI; block merges with unresolved errors.
- Add `.eslintignore` and `.prettierignore` for build output and node_modules.

```json
// .prettierrc
{
  "singleQuote": true,
  "trailingComma": "all",
  "printWidth": 100,
  "semi": true
}
```

## 7. File Naming and Structure

- Source in `src/`, build output in `dist/`, tests co-located or in `__tests__/`.
- kebab-case for files and directories: `user-service.ts`, `format-currency.ts`.
- One export per file is the default; barrel files (`index.ts`) re-export public API.
- `node_modules/`, `dist/`, `.env` go in `.gitignore`.

```
project/
├── src/
│   ├── index.ts           # entrypoint
│   ├── routes/
│   │   └── users.ts
│   ├── services/
│   │   └── user-service.ts
│   └── utils/
│       └── format.ts
├── tests/
├── package.json
├── tsconfig.json
└── .prettierrc
```

## 8. Environment Variables

- Use `process.env` with a validation layer — never read `process.env.X` raw in business logic.
- Load from `.env` with `dotenv` only in development; in production the platform injects them.
- `.env` must be in `.gitignore`; provide `.env.example` with dummy values for documentation.
- Validate required vars at startup: fail fast with a clear error message, not a cryptic `undefined` crash deep in code.

```javascript
// config.js
import 'dotenv/config';

const required = ['DATABASE_URL', 'JWT_SECRET'];
for (const key of required) {
  if (!process.env[key]) {
    throw new Error(`Missing required env var: ${key}`);
  }
}

export const config = {
  databaseUrl: process.env.DATABASE_URL!,
  jwtSecret: process.env.JWT_SECRET!,
  port: parseInt(process.env.PORT || '3000', 10),
};
```

## 9. Testing

- Vitest for unit/integration tests (or Jest for legacy projects).
- Test files: `*.test.ts` or `*.spec.ts`, co-located or in a `__tests__/` directory.
- Use `describe`/`it` blocks; one assertion concept per `it`.
- Mock at the boundary: mock network calls and filesystem, never mock the code under test.
- Run tests with `--coverage` in CI; aim for >80% branch coverage.

```javascript
import { describe, it, expect } from 'vitest';
import { divide } from '../src/utils/math.js';

describe('divide', () => {
  it('returns quotient of two positive numbers', () => {
    expect(divide(10, 2)).toBe(5);
  });

  it('throws when dividing by zero', () => {
    expect(() => divide(1, 0)).toThrow('Cannot divide by zero');
  });
});
```

## 10. Security

- Sanitize all user input — never trust query params, request bodies, or headers.
- Use parameterized queries for databases; never string-interpolate user data into SQL.
- Set security headers: `helmet` middleware for Express.
- Validate and sanitize with a schema library (zod, joi, yup).
- Never commit secrets; use environment variables.