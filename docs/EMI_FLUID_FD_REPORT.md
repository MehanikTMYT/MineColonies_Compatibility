# EMI Fluid Transfer / FD integration report

Environment: NL, Minecraft 26.1.2, NeoForge 26.1.2.114, Java 25.
Compatibility baseline: `544255a5`. MineColonies baseline: `398a3f3fb3`.

## DONE

- Minimal MineColonies extension: `IGenericRecipe.getFluidInputs()` defaults to an empty list;
  `MineColoniesEmiRecipe` retains its typed source, displays component-aware fluid stacks and
  differentiates fluid recipes by full items/outputs/fluid data, including volume.
- Loss was in `MineColoniesEmiRecipe.fromGeneric`: the wrapper formerly retained only item
  ingredients/outputs. The old JEI bucket handler received `BucketFillingGenericRecipe` directly;
  the new EMI handler now recovers that same typed source.
- Bucket teaching transfer uses the existing `JEIRecipeTransferMessage` protocol and menu validator.
  No new packet protocol, profession, building requirement or category is introduced.
- Ghost fluid payload preserves Data Components and exact volume; nonpositive/overflow values are
  rejected. Storage decoding rejects an unknown fluid ID instead of accepting a registry fallback.
- Container copies preserve components. Parsing snapshots the drained fluid before refilling consumes
  the tank and records the actual drained volume, not tank capacity.
- Existing smithing/stonecutter implementations (byte-for-byte comparison against `544255a5`)
  and research/orchardist/butcher recipe registration
  remain intact. No recipe JSON, production balance, CPE networking or request interfaces changed.
- MineColonies Gradle build passed. Final Compatibility Gradle build, QA source compilation and
  prepared FD compilation passed (51 seconds). Gradle has no conventional test sources here; the
  runtime checks are the opt-in `src/qa` mod, not an assumed JUnit test suite.
- Mixin checker: 38 mixins, 7 Accessors, 4 Invokers, 0 problems.
- Dedicated server reached Done, passed serialization and real water/lava bucket ghost assertions,
  saved all dimensions and stopped normally. Loaded MineColonies, Compatibility, Tweaks, CPE fix-5,
  EMI Compendium, Domum Ornamentum and existing LDT dependencies.

- Xvfb client (with the documented QA-only Tweaks handler workaround) passed model/ghost tests,
  invoked the actual EMI `craft` handler, transported water/lava recipes to the integrated server,
  verified server ghost containers and received recipe responses on the client. Live EMI registry:
  89 categories, unique IDs. Client exited normally and the Gradle run completed successfully.
  This uses a test menu position rather than constructing a staffed colony building.

## READY_FOR_FD

- Prepared cooking/cutting/plating EMI teaching handlers; no duplicate worker categories.
- Updated FD platform imports, recipe lookup, serialization, menu packets, screen extraction and
  cooking/cutting recipe synchronization. Source files remain outside the release source set.
- A real unofficial target FD port was found: `farmersdelight 1.3.3-fix7`.
  `compileFdJava` passed against its JAR. Therefore base FD API is NOT an absent dependency here.
- This label means prepared integration awaiting activation/validation against the chosen FD
  distribution, not a claim that FD itself has been ported or fully tested.
- [API matrix and complete disabled-source audit](FD_API_COMPATIBILITY.md).
- [Activation instructions and lazy optional registration](ENABLE_FD.md).

## BLOCKED

- Cultural Delights: real target API/JAR unavailable locally; required `ModBlocks`,
  `FruitingLeaves`, `ModItems` listed in the API matrix. Remains disabled.
- Fruits Delight: real target API/JAR unavailable locally; required leaves/bush/tree registries
  listed in the API matrix. Remains disabled.
- Unmodified fleet Tweaks JAR blocks normal client startup:
  missing client handlers for `resourcescroll_book_open`, `batch_upgrade_data_load`,
  `batch_repair_data_load`. Initial Xvfb attempt failed before entering the world.
  The isolated QA mod wires these three existing handlers for testing only.
  This workaround is not included in the release JAR and does not modify Claude's tree. Remove the QA workaround
  when testing with a Tweaks version that registers these handlers itself.

## QA / limitations

- FD compilation is not FD gameplay QA. Activation, startup with/without FD, cooking with 1–6
  ingredients, cutting tool/chance restrictions, plating containers, recipe reload and actual
  colony execution still need testing with the selected real FD JAR installed.
- Addon tests cannot run without their real target JARs. No stubs or missing-library copies were made.
- Full colony resource delivery from network storage was not exercised: the existing interfaces
  were inspected and the ecosystem passed server startup. Production request behavior remains QA.
- Custom 250/2000 mB containers are not available in this fixture. Those amounts are covered in
  the model/codec tests, not claimed as successful physical bucket recipes.
- The existing bucket teaching menu has ONE filled-container ghost slot. Multiple item inputs are
  tested at the generic EMI model boundary; arbitrary multi-input bucket crafting is not introduced.
- Server log contains absent optional-mod mixin targets (AE2, RS, Reliquary, Oreberries, LootJS),
  missing TACZ items in data and a Farlanders loot parse error. Startup/tests succeeded; this is
  not a clean-log claim. CPE also emits an existing method-overwrite conflict warning.
- Smithing/stonecutting handlers are unchanged and retained by registration checks; full manual
  building-menu interaction for these two professions remains regression QA.

- Client also reports pre-existing model-load errors for the missing
  `domum_ornamentum:materially_textured` loader, untranslated EMI tags and a missing-compost-recipe
  warning. Thus startup/transport success does not certify all Domum rendering or optional modules.

## Test matrix

| Requested case | Coverage |
|---|---|
| One input item | Real EMI craft → packet → server ghost → client recipe response, water and lava |
| Multiple ingredients | Generic EMI model retains two item ingredients plus fluid; not a new bucket-menu feature |
| One fluid type | Water at 250/1000/2000 mB model + storage round trip |
| Different fluids | Water/lava storage round trip and real 1000 mB server menu |
| Fluid container | Real water/lava buckets; custom-name container components in codec/model |
| Different volumes | 250/1000/2000 mB identity/codec/model; physical custom containers remain QA |
| Missing inventory ingredients | Empty-inventory EMI teaching context; ghost teaching must not consume inventory |
| Same items, different fluid | Same item input/output with different fluid/volume must yield distinct stable EMI IDs |
| Unknown fluid/components | Unknown registry ID rejected; fluid/container components survive round trip |

## Reproduction / artifacts

All commands run on NL in the isolated worktrees, with Java 25.
MineColonies commit: `a557a2f3ec`; apply/build it before Compatibility.
Compatibility branch: `codex/emi-fluid-fd-26.1.2`; this report is committed with the changes.
No push was performed.

```sh
# MineColonies worktree
./gradlew build -PopcScript=/root/home/forks/OperaPublicaCreator/gradle/mod.gradle --no-daemon

# Compatibility worktree
./gradlew build compileQaJava compileFdJava writeQaClasspath   -PfluidQa -PfdJar=qa/deps/farmersdelight.jar -PqaRuntimeDir=qa/deps/runtime   -PminecoloniesJar=/root/home/forks/worktrees/minecolonies-emi-fluid/build/libs/minecolonies-1.1.1397-26.1.2.jar   --no-daemon

# Client probe (copied disposable server world at runs/client/saves/FluidQA)
xvfb-run -a -s "-screen 0 1280x720x24" ./gradlew runClient   -PfluidQa -PqaWorld=FluidQA -PqaRuntimeDir=qa/deps/runtime   -PminecoloniesJar=/root/home/forks/worktrees/minecolonies-emi-fluid/build/libs/minecolonies-1.1.1397-26.1.2.jar   --no-daemon
```

Logs (ignored, retained on NL):
- MineColonies: `/tmp/codex-mc-fluid-build.log`.
- Compatibility: `qa/logs/build-final.log` (earlier FD validation: `qa/logs/build-2.log`).
- Mixins: `qa/logs/mixin-check-final.log`.
- Server: `qa/logs/server-2/server.log`, `summary.txt`, `errors.txt`.
- Client initial failure: `qa/logs/client.log`; QA retry: `qa/logs/client-2.log`; final pass: `qa/logs/client-final.log` (2m 31s).

Resolved EMI runtime: `1.1.24-4524a4bf+26.1.2`.
Release artifact: `build/libs/MineColonies_Compatibility-26.1.2-3.58-fleet.1.jar`.
Do not distribute the `-fluid-qa.jar` probe. FD classes are compiled separately and excluded
from the default release. External downloads/runtime fixtures and logs are ignored.

## Changed Java classes

MineColonies:
- `com.minecolonies.api.crafting.IGenericRecipe`
- `com.minecolonies.core.compatibility.emi.MineColoniesEmiRecipe`

Compatibility (including prepared disabled sources and isolated QA):
- `src/disabled/java/steve_gall/minecolonies_compatibility/module/client/farmersdelight/CookingTeachScreen.java`
- `src/disabled/java/steve_gall/minecolonies_compatibility/module/client/farmersdelight/CuttingTeachScreen.java`
- `src/disabled/java/steve_gall/minecolonies_compatibility/module/client/farmersdelight/PlatingTeachScreen.java`
- `src/disabled/java/steve_gall/minecolonies_compatibility/module/client/farmersdelight/emi/FarmersDelightEmiPlugin.java`
- `src/disabled/java/steve_gall/minecolonies_compatibility/module/common/culturaldelights/AvocadoFruit.java`
- `src/disabled/java/steve_gall/minecolonies_compatibility/module/common/farmersdelight/FarmersDelightModule.java`
- `src/disabled/java/steve_gall/minecolonies_compatibility/module/common/farmersdelight/MushroomColonyFruit.java`
- `src/disabled/java/steve_gall/minecolonies_compatibility/module/common/farmersdelight/RiceFruit.java`
- `src/disabled/java/steve_gall/minecolonies_compatibility/module/common/farmersdelight/TomatoCrop.java`
- `src/disabled/java/steve_gall/minecolonies_compatibility/module/common/farmersdelight/building/module/CookingCraftingModule.java`
- `src/disabled/java/steve_gall/minecolonies_compatibility/module/common/farmersdelight/building/module/CookingCraftingModuleView.java`
- `src/disabled/java/steve_gall/minecolonies_compatibility/module/common/farmersdelight/building/module/CuttingCraftingModule.java`
- `src/disabled/java/steve_gall/minecolonies_compatibility/module/common/farmersdelight/building/module/CuttingCraftingModuleView.java`
- `src/disabled/java/steve_gall/minecolonies_compatibility/module/common/farmersdelight/building/module/PlatingCraftingModuleView.java`
- `src/disabled/java/steve_gall/minecolonies_compatibility/module/common/farmersdelight/crafting/CookingGenericRecipe.java`
- `src/disabled/java/steve_gall/minecolonies_compatibility/module/common/farmersdelight/crafting/CookingRecipeStorage.java`
- `src/disabled/java/steve_gall/minecolonies_compatibility/module/common/farmersdelight/crafting/CuttingChanceResult.java`
- `src/disabled/java/steve_gall/minecolonies_compatibility/module/common/farmersdelight/crafting/CuttingCraftingType.java`
- `src/disabled/java/steve_gall/minecolonies_compatibility/module/common/farmersdelight/crafting/CuttingGenericRecipe.java`
- `src/disabled/java/steve_gall/minecolonies_compatibility/module/common/farmersdelight/crafting/CuttingRecipeStorage.java`
- `src/disabled/java/steve_gall/minecolonies_compatibility/module/common/farmersdelight/crafting/PlatingCraftingType.java`
- `src/disabled/java/steve_gall/minecolonies_compatibility/module/common/farmersdelight/crafting/PlatingRecipeStorage.java`
- `src/disabled/java/steve_gall/minecolonies_compatibility/module/common/farmersdelight/menu/CookingTeachMenu.java`
- `src/disabled/java/steve_gall/minecolonies_compatibility/module/common/farmersdelight/menu/CuttingTeachMenu.java`
- `src/disabled/java/steve_gall/minecolonies_compatibility/module/common/farmersdelight/menu/PlatingTeachMenu.java`
- `src/disabled/java/steve_gall/minecolonies_compatibility/module/common/farmersdelight/network/CuttingOpenTeachMessage.java`
- `src/disabled/java/steve_gall/minecolonies_compatibility/module/common/fruitsdelight/DurianBlockFruit.java`
- `src/disabled/java/steve_gall/minecolonies_compatibility/module/common/fruitsdelight/FruitBushFruit.java`
- `src/disabled/java/steve_gall/minecolonies_compatibility/module/common/fruitsdelight/TreeLeavesFruit.java`
- `src/main/java/steve_gall/minecolonies_compatibility/core/common/crafting/BucketFillingCraftingType.java`
- `src/main/java/steve_gall/minecolonies_compatibility/core/common/crafting/BucketFillingGenericRecipe.java`
- `src/main/java/steve_gall/minecolonies_compatibility/core/common/crafting/BucketFillingRecipeStorage.java`
- `src/main/java/steve_gall/minecolonies_compatibility/module/client/emi/CompatibilityEmiPlugin.java`
- `src/main/java/steve_gall/minecolonies_compatibility/module/client/emi/EmiGhostStacks.java`
- `src/qa/java/compatqa/ClientFluidQa.java`
- `src/qa/java/compatqa/FluidQa.java`

Release JAR inspection confirmed absence of `compatqa/`, disabled FD integration classes and
`vectorwing/` classes. Original Compatibility and MineColonies worktrees remained clean at their
baseline commits after final verification.

## Integration (Claude, 2026-10-17)

- Codex branches integrated: MineColonies `port/26.1` = cherry-pick `a557a2f3ec` (`17ccc424c7`) + `a2de8ded5d`
  (fluid recipe id no longer dereferences a null client level); Compatibility `port/26.1.2` = merge `59ce89f2`.
- Root cause of the Tweaks client failure: NeoForge 26.1 `playBidirectional(type, codec, handler)` registers the
  serverbound handler only. Fixed in Tweaks `MessageRegistrar` (`460115a`, both handlers); the QA workaround was
  removed from `ClientFluidQa` (`67bbed82`). Other fleet trees checked: Trade Post and SEMI pass both handlers,
  AE2 registers clientbound handlers through `RegisterClientPayloadHandlersEvent`.
- Re-run on the integrated trees (MineColonies jar rebuilt on FI):
  - `jar compileFdJava -PfdJar=farmersdelight 1.3.3-fix7` — BUILD SUCCESSFUL (FI);
  - release jar: 0 `compatqa/`, 0 FD, 0 `vectorwing/` classes, 13 EMI classes;
  - mixin-check (Accessor/Invoker types): 38 mixins, 0 problems;
  - dedicated server (FI): Done (9.98 s);
  - Xvfb client (NL), unmodified Tweaks: server serialization + real bucket ghost slots PASS, client model and
    ghost slots PASS, real EMI craft → packet → server ghost PASS for water and lava buckets, live EMI categories
    unique (89) PASS. A fresh `runs/client` needs `onboardAccessibility:false` in options.txt (first-launch screen).
  - EMI itself logs an NPE in its recipe-bake worker after the QA disconnect (`EmiPortClient.getEnchantmentRegistry`,
    `client.level` null) — EMI-side shutdown race, no Compatibility/MineColonies frame.
- Cultural Delights is All Rights Reserved (mod_license / TEMPLATE_LICENSE); the 0.19.3 tree in
  `worktrees/cultural-delights-26.1.2` was reconstructed by decompiling the release jar. Not continued, not published
  — needs the author's permission first. Fruits Delight is LGPL-2.1.
