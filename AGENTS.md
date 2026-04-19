# Kestra Malloy Plugin

## What

- Provides plugin components under `io.kestra.plugin.malloy`.
- Includes classes such as `CLI`.

## Why

- What user problem does this solve? Teams need to execute Malloy CLI queries from orchestrated workflows instead of relying on manual console work, ad hoc scripts, or disconnected schedulers.
- Why would a team adopt this plugin in a workflow? It keeps Malloy CLI steps in the same Kestra flow as upstream preparation, approvals, retries, notifications, and downstream systems.
- What operational/business outcome does it enable? It reduces manual handoffs and fragmented tooling while improving reliability, traceability, and delivery speed for processes that depend on Malloy CLI.

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
