# Tobacconist — Forge 1.19.2

Standalone backport of the Forge 1.20.1 **4.2.0-beta4** source. Requires **Minecraft 1.19.2, Forge 43.1.25 or newer within 43.x, and Java 17**. Install the mod on both client and server.

The port retains tobacco cultivation, environmental curing, specialty barrel processing, quality and aging, cutting and blending, traditional smoking, hookahs, novelty pipes, vaping, liquid blending, bottles, recipe cards, storage and vanilla hopper automation.

Create integration is disabled, including when Create 0.5.1 is installed. Create processing, fan assistance, factory logistics and Ponder tutorials are excluded. The Industrial Drying Rack and assembly intermediates remain registered for identifier consistency but have no recipes or creative entries. The Production Monitor supports ordinary hoppers and has a vanilla-material crafting recipe.

Use brewing stands for molasses, Aqua Vitae and essences; heated cauldrons for extraction; wooden racks for curing; extended downward-facing pistons for barrel pressure; and the Liquid Blending Bench for vape liquids. Netherite upgrades use the two-input 1.19.2 smithing table. Cherry and bamboo plank pipe variants are omitted because those woods were added after 1.19.2.

## Optional mods

- Curios **1.19.2-5.1.6.4**: Head and Mouth slots, equipped smoking and accessory rendering.
- JEI **11.8.1.1034**: crafting, brewing and processing recipe displays.
- Patchouli **1.19.2-77**: the updated in-game manual.
- Food-mod flavor tags remain optional; install matching 1.19.2 versions of those mods.

Without Curios, spectacles use the vanilla head slot and smoking works through ordinary item use. None of the optional mods is bundled in the JAR.

## Build

With a Java 17 JDK installed, run `bash gradlew build` on Linux/macOS or `gradlew.bat build` on Windows. The reobfuscated production JAR is written to `build/libs/`.

## Validation

The opt-in audit sources in `validation/java/` are excluded from the production JAR. After accepting Minecraft's EULA in the corresponding isolated audit directory, run:

- `bash gradlew runServer -PportValidation -Pstandalone` for the mod alone.
- `bash gradlew runServer -PportValidation` for Curios, JEI and Patchouli.

Results are written to `run-audit-standalone/port-audit-results.json` or `run-audit-optional/port-audit-results.json`. See the included validation report for the completed checks and their limits.

To test the production JAR against installed Forge servers, build the separate audit mod with `bash gradlew portAuditJar -PportValidation -PproductionAudit`, then use `validation/run_production_matrix.py --help`. The audit mod is for testing only and is not bundled in the Tobacconist JAR. The project compiles against its minimum supported Forge API, 43.1.25.

Earlier release history is preserved in [changelog.md](changelog.md); its Create-related entries describe the newer Minecraft branches.
