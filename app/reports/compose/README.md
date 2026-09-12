# Compose compiler stability reports

`app-debug.stability` and `app-release.stability` are snapshots of the Compose
compiler's stability/skippability output for `:app`, one per build variant.

Regenerate after touching composables or their parameter types:

```
./gradlew :app:compileDebugKotlin :app:compileReleaseKotlin -PcomposeReports=true --rerun
```

Raw output lands in `app/build/compose_compiler/` (`app-classes.txt`,
`app-composables.txt`, `app-composables.csv`, `<variant>/app-module.json`);
the `.stability` files here combine those plus a summary header.
