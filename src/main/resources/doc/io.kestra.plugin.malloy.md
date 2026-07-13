# How to use the Malloy plugin

Run Malloy CLI queries from Kestra flows. The single `CLI` task executes `malloy-cli` commands inside a container.

## Tasks

`CLI` runs one or more Malloy CLI commands set in `commands` (for example `malloy-cli run model.malloy`). Author a Malloy script first, then run it: pass the script via `inputFiles` or pull it from [namespace files](https://kestra.io/docs/concepts/namespace-files), and reference it from a command.

## Runtime

`containerImage` selects the image that provides the Malloy CLI. `taskRunner` controls where the container runs and defaults to Docker. Use `beforeCommands` for setup steps that run before the query, and `env` to pass environment variables.

Store credentials for the data sources Malloy connects to in [secrets](https://kestra.io/docs/concepts/secret) and pass them through `env`.
