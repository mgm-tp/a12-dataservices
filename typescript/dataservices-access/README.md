# Data Services Access library.

Library provides utility functions to create URLs for Data Services endpoints and nominal interfaces for description of requests and responses of those endpoints.

## How to use

- If not done already, run
  - `npm install --legacy-peer-deps`
- Compile TypeScript
  - `npm run compile`
- Run Tests
  - `npm run test`
- Run Tests with Integration
  - `npm run test:integration`


## IntelliJ IDEA setup to run tests

1. Set the CDE's node and typescript: ![Node setup](node_setup.png) ![TS setup](ts_setup.png)
2. Add `--require ts-node/register/transpile-only` to mocha options: ![run configuration](run_config.png)
