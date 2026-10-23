# Compatibility

- The plugin id is still `Skript`, so addons with `depend: [Skript]` load normally and the data folder is `plugins/Skript/`.
- No upstream syntax was changed or removed. New syntax lives in the package `ch.njol.skript.reloaded` and only adds patterns.
- If a new pattern clashes with an addon's pattern, Skript uses registration order; report it as an issue.
- Variables, functions, options and commands in your `.sk` files behave exactly as in upstream Skript.
