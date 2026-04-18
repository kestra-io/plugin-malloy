# Kestra Malloy Plugin

## What

- Provides plugin components under `io.kestra.plugin.malloy`.
- Includes classes such as `CLI`.

## Why

- This plugin integrates Kestra with Malloy CLI.
- It provides tasks that execute Malloy CLI queries.

## How

### Architecture

Single-module plugin. Source packages under `io.kestra.plugin`:

- `malloy`

### Key Plugin Classes

- `io.kestra.plugin.malloy.CLI`

### Project Structure

```
plugin-malloy/
├── src/main/java/io/kestra/plugin/malloy/
├── src/test/java/io/kestra/plugin/malloy/
├── build.gradle
└── README.md
```

## References

- https://kestra.io/docs/plugin-developer-guide
- https://kestra.io/docs/plugin-developer-guide/contribution-guidelines
