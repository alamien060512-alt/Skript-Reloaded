# Adding New Syntax

1. Put the class in `src/main/java/ch/njol/skript/reloaded/{effects,expressions,conditions}`. Those folders are loaded
   automatically (see `Skript.java`); a new folder needs adding to that `loadClasses` call.
2. Register patterns in a `static` block, e.g. `Skript.registerExpression(...)`, and add `@Name`, `@Description`,
   `@Example` and `@Since("SkriptReloaded x.y")` for the generated docs.
3. Add a test script under `src/test/skript/tests/syntaxes/`.
4. List the syntax in [Syntax Reference](Syntax-Reference.md) and the README.

Keep existing Skript syntax untouched so old scripts keep working.
