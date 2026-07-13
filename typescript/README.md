# Testing

To run TS tests in IntelliJ Idea, install IntelliJ Idea `Node.js` plugin, use `Mocha run configuration` in IDE
and add `--require ts-node/register/transpile-only` to `Extra mocha options`. Assure that `Mocha interface` is set to "tdd".

Make sure that `ts-node` module is installed and then tests with debug should work.
