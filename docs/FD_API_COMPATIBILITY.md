# FD API audit — Minecraft 26.1.2 / NeoForge 26.1.2.114 / Java 25

Scope: integration code only. No FD gameplay, blocks, items, recipes or library implementations are copied.

## Evidence

- Baseline Compatibility: `544255a5`; isolated worktree `compat-emi-fluid-fd`.
- Unofficial FD: [CurseForge file 9115209](https://www.curseforge.com/minecraft/mc-mods/farmers-delight-26-neo-ver/files/9115209).
- Source: [BuaichiZY/FarmersDelight_26_Neo.ver, branch 26.1.2](https://github.com/BuaichiZY/FarmersDelight_26_Neo.ver/tree/26.1.2), commit `8abace75ad0cd4c7aba2e70894fbccf6285b8ed4`.
- Downloaded JAR SHA-256: `6510766997e871852eb1aaffdd665577b7797fe1fdd73995fd1a082b7ef76142`.
- JAR metadata: `farmersdelight 1.3.3-fix7`, Minecraft `[26.1.2]`, NeoForge `[26.1.2.86,)`.
- No Cultural Delights or Fruits Delight JAR found in the fleet pack-libs or Maven Local. Their APIs cannot be compiled or runtime-tested here.

## Upstream releases clarified (2026-10-17)

The addons do have published releases. Their absence from the target environment does not mean
that the projects have no releases:

- [Cultural Delights files](https://www.curseforge.com/minecraft/mc-mods/cultural-delights/files/all)
  lists 0.19.2 for Minecraft 1.21.1 / NeoForge. User supplied
  [file 9208654](https://www.curseforge.com/minecraft/mc-mods/cultural-delights/files/9208654);
  the individual file page was inaccessible to the web reader, so its exact metadata was not
  independently verified.
- [Fruits Delight files](https://www.curseforge.com/minecraft/mc-mods/fruits-delight/files/all?page=1&pageSize=20&showAlphaFiles=hide)
  lists fruitsdelight-1.2.14.jar for Minecraft 1.21.1 / NeoForge.

Neither inspected listing advertises a 26.1.2 release. These older artifacts may inform an API
comparison, but are not target dependencies or evidence of 26.1.2 compilation/runtime compatibility.
Both integrations remain BLOCKED pending real target APIs/JARs; no 1.21.1 JAR was added to the
26.1.2 build or runtime. Porting the addons themselves is outside this task.

## Compatibility matrix

| Component | API used | Adaptation / blocker |
|---|---|---|
| Cooking | CookingPotRecipe, RecipeWrapper, getIngredients(), getResultItem(), getOutputContainer(), getCookTime() | Real target API exists. Use explicit FD ingredients/result, current RecipeMap, synchronize cooking recipes to client. |
| Cutting | CuttingBoardRecipe/Input, ChanceResult, getTool(), getRollableResults() | Real target API exists. Ingredient.items()/IngredientHelper replaces getItems(); RecipeHolder.id().identifier(); retain tool and chance validation. |
| Plating | FeastBlock.getServingsProperty(), getMaxServings(), getServingItem() | Real target API exists. Current component-aware remainder helper; retain original serving/container counts. |
| Crops | TomatoVineBlock, ModBlocks, ModItems | Preserve existing harvest/replant logic. Only platform signatures are adapted. |
| Rice / mushrooms | Rice crop registry, MushroomColonyBlock.COLONY_AGE/getMaxAge | Clone item now accessed through BlockState with includeData argument. |
| Screens | GuiGraphics, renderBg, renderTooltip | GuiGraphicsExtractor, extractBackground, next-frame tooltip, RenderPipelines. |
| Registries/network | DeferredRegister, ModuleMenuTypes, CustomPacketPayload, ModuleMenuOpenMessage | Existing NeoForge deferred/payload architecture retained; Identifier buffer methods and current registry getters. |
| JEI transfer | Three FD handlers and ModulePlugin | Prepared EMI plugin handles cooking/cutting/plating; adds no categories. Legacy JEI sources excluded from fd compilation. |
| Cultural Delights | com.baisylia.culturaldelights.block.ModBlocks, block.custom.FruitingLeaves, item.ModItems | BLOCKED: no target addon JAR. Platform-only preparation; no fake classes. |
| Fruits Delight | dev.xkmc.fruitsdelight.content.block BaseLeavesBlock/PassableLeavesBlock/BaseBushBlock/DoubleFruitBushBlock; init.plants FDTrees/FDBushes | BLOCKED: no target addon JAR. Platform-only preparation; no fake classes. |

## Blocking addon members and remaining platform work

- Cultural Delights: `ModBlocks.FRUITING_AVOCADO_LEAVES`, `AVOCADO_SAPLING`, `AVOCADO_PIT`,
  `ModItems.AVOCADO`, `FruitingLeaves.AGE/MAX_AGE` and their registry-holder signatures must exist
  in a real target addon. `AvocadoFruit` now uses the current `level.getRandom()` accessor.
- Fruits Delight: `FDTrees.values()/DURIAN/getLeaves()/getSapling()/getFruit()`,
  `FDBushes.values()/getBush()/getSeed()/getFruit()`, `BaseBushBlock.AGE/MAX_AGE`,
  `PassableLeavesBlock.STATE/State.FRUITS/State.LEAVES`, and `DoubleFruitBushBlock` inheritance
  need target verification. `FruitBushFruit` now stores its bush as the platform `Block` type (only Block operations are
  required) and uses `level.getRandom()`. These platform edits do not verify addon signatures.
- Neither addon provides a separate JEI teaching plugin in the audited tree: they contribute fruit
  registrations consumed by the existing Compatibility orchardist EMI category. They must not
  register duplicate fruit categories.
- Base FD signatures were checked by compiling the non-JEI disabled FD source set against the
  downloaded JAR. `RecipeWrapper`, `InvWrapper` and `TomatoVineBlock` generate deprecation warnings
  but remain present and are required by this port's actual signatures.

## Dependency and registration boundary

- Required for Compatibility: Minecraft/NeoForge/Java, MineColonies, Tweaks, the existing LDT dependencies.
- FD is optional for Compatibility; mandatory only for the prepared FD source set. `-PfdJar=/absolute/path/to/real.jar` enables compilation, not shipping or registration.
- EMI is client-only and optional. No JEI dependency is needed by the prepared EMI plugin.
- Each addon requires its own real target JAR and its declared runtime dependencies. Installing base FD does not supply Cultural/Fruits Delight classes.
- Keep disabled modules outside the main source set. The prepared EMI entrypoint checks FD presence and bound menu registration before touching handlers.
- No worker categories are registered by the FD adapter. MineColonies remains their owner; research/orchardist/butcher stay owned by Compatibility.

## Source inventory

| Disabled source | External FD/addon imports |
|---|---|
| `src/disabled/java/steve_gall/minecolonies_compatibility/module/client/farmersdelight/CookingTeachScreen.java` | `vectorwing.farmersdelight.common.crafting.CookingPotRecipe` |
| `src/disabled/java/steve_gall/minecolonies_compatibility/module/client/farmersdelight/CuttingTeachScreen.java` | `vectorwing.farmersdelight.common.crafting.CuttingBoardRecipe` |
| `src/disabled/java/steve_gall/minecolonies_compatibility/module/client/farmersdelight/PlatingTeachScreen.java` | Platform / integration classes only |
| `src/disabled/java/steve_gall/minecolonies_compatibility/module/client/farmersdelight/emi/FarmersDelightEmiPlugin.java` | Platform / integration classes only |
| `src/disabled/java/steve_gall/minecolonies_compatibility/module/client/farmersdelight/jei/CookingTeachRecipeTransferHandler.java` | `vectorwing.farmersdelight.common.crafting.CookingPotRecipe`<br>`vectorwing.farmersdelight.integration.jei.FDRecipeTypes` |
| `src/disabled/java/steve_gall/minecolonies_compatibility/module/client/farmersdelight/jei/CuttingTeachRecipeTransferHandler.java` | `vectorwing.farmersdelight.common.crafting.CuttingBoardRecipe`<br>`vectorwing.farmersdelight.common.crafting.CuttingBoardRecipeInput`<br>`vectorwing.farmersdelight.integration.jei.FDRecipeTypes` |
| `src/disabled/java/steve_gall/minecolonies_compatibility/module/client/farmersdelight/jei/ModulePlugin.java` | `vectorwing.farmersdelight.integration.jei.FDRecipeTypes` |
| `src/disabled/java/steve_gall/minecolonies_compatibility/module/client/farmersdelight/jei/PlatingTeachRecipeTransferHandler.java` | Platform / integration classes only |
| `src/disabled/java/steve_gall/minecolonies_compatibility/module/common/culturaldelights/AvocadoFruit.java` | `com.baisylia.culturaldelights.block.ModBlocks`<br>`com.baisylia.culturaldelights.block.custom.FruitingLeaves`<br>`com.baisylia.culturaldelights.item.ModItems` |
| `src/disabled/java/steve_gall/minecolonies_compatibility/module/common/culturaldelights/CulturalDelightsModule.java` | Platform / integration classes only |
| `src/disabled/java/steve_gall/minecolonies_compatibility/module/common/farmersdelight/FarmersDelightModule.java` | `vectorwing.farmersdelight.common.block.MushroomColonyBlock`<br>`vectorwing.farmersdelight.common.registry.ModItems` |
| `src/disabled/java/steve_gall/minecolonies_compatibility/module/common/farmersdelight/MushroomColonyFruit.java` | `vectorwing.farmersdelight.common.block.MushroomColonyBlock` |
| `src/disabled/java/steve_gall/minecolonies_compatibility/module/common/farmersdelight/RiceFruit.java` | `vectorwing.farmersdelight.common.block.RicePaniclesBlock`<br>`vectorwing.farmersdelight.common.registry.ModBlocks`<br>`vectorwing.farmersdelight.common.registry.ModItems` |
| `src/disabled/java/steve_gall/minecolonies_compatibility/module/common/farmersdelight/TomatoCrop.java` | `vectorwing.farmersdelight.common.block.TomatoVineBlock`<br>`vectorwing.farmersdelight.common.registry.ModBlocks`<br>`vectorwing.farmersdelight.common.registry.ModItems` |
| `src/disabled/java/steve_gall/minecolonies_compatibility/module/common/farmersdelight/building/module/CookingCraftingModule.java` | `vectorwing.farmersdelight.common.block.entity.CookingPotBlockEntity`<br>`vectorwing.farmersdelight.common.crafting.CookingPotRecipe` |
| `src/disabled/java/steve_gall/minecolonies_compatibility/module/common/farmersdelight/building/module/CookingCraftingModuleView.java` | Platform / integration classes only |
| `src/disabled/java/steve_gall/minecolonies_compatibility/module/common/farmersdelight/building/module/CuttingCraftingModule.java` | `vectorwing.farmersdelight.common.registry.ModBlocks` |
| `src/disabled/java/steve_gall/minecolonies_compatibility/module/common/farmersdelight/building/module/CuttingCraftingModuleView.java` | Platform / integration classes only |
| `src/disabled/java/steve_gall/minecolonies_compatibility/module/common/farmersdelight/building/module/PlatingCraftingModule.java` | Platform / integration classes only |
| `src/disabled/java/steve_gall/minecolonies_compatibility/module/common/farmersdelight/building/module/PlatingCraftingModuleView.java` | Platform / integration classes only |
| `src/disabled/java/steve_gall/minecolonies_compatibility/module/common/farmersdelight/crafting/CookingGenericRecipe.java` | `vectorwing.farmersdelight.common.block.entity.CookingPotBlockEntity`<br>`vectorwing.farmersdelight.common.crafting.CookingPotRecipe`<br>`vectorwing.farmersdelight.common.registry.ModBlocks` |
| `src/disabled/java/steve_gall/minecolonies_compatibility/module/common/farmersdelight/crafting/CookingRecipeStorage.java` | Platform / integration classes only |
| `src/disabled/java/steve_gall/minecolonies_compatibility/module/common/farmersdelight/crafting/CuttingChanceResult.java` | `vectorwing.farmersdelight.common.crafting.ingredient.ChanceResult` |
| `src/disabled/java/steve_gall/minecolonies_compatibility/module/common/farmersdelight/crafting/CuttingCraftingType.java` | `vectorwing.farmersdelight.common.crafting.CuttingBoardRecipe`<br>`vectorwing.farmersdelight.common.crafting.CuttingBoardRecipeInput`<br>`vectorwing.farmersdelight.common.registry.ModRecipeTypes` |
| `src/disabled/java/steve_gall/minecolonies_compatibility/module/common/farmersdelight/crafting/CuttingGenericRecipe.java` | `vectorwing.farmersdelight.common.crafting.CuttingBoardRecipe`<br>`vectorwing.farmersdelight.common.registry.ModBlocks` |
| `src/disabled/java/steve_gall/minecolonies_compatibility/module/common/farmersdelight/crafting/CuttingRecipeStorage.java` | Platform / integration classes only |
| `src/disabled/java/steve_gall/minecolonies_compatibility/module/common/farmersdelight/crafting/PlatingCraftingType.java` | `vectorwing.farmersdelight.common.block.FeastBlock` |
| `src/disabled/java/steve_gall/minecolonies_compatibility/module/common/farmersdelight/crafting/PlatingGenericRecipe.java` | Platform / integration classes only |
| `src/disabled/java/steve_gall/minecolonies_compatibility/module/common/farmersdelight/crafting/PlatingRecipeStorage.java` | `vectorwing.farmersdelight.common.block.FeastBlock` |
| `src/disabled/java/steve_gall/minecolonies_compatibility/module/common/farmersdelight/init/ModuleBuildingModules.java` | Platform / integration classes only |
| `src/disabled/java/steve_gall/minecolonies_compatibility/module/common/farmersdelight/init/ModuleCraftingTypes.java` | `vectorwing.farmersdelight.common.registry.ModRecipeTypes` |
| `src/disabled/java/steve_gall/minecolonies_compatibility/module/common/farmersdelight/init/ModuleMenuTypes.java` | Platform / integration classes only |
| `src/disabled/java/steve_gall/minecolonies_compatibility/module/common/farmersdelight/menu/CookingTeachMenu.java` | `vectorwing.farmersdelight.common.crafting.CookingPotRecipe`<br>`vectorwing.farmersdelight.common.registry.ModRecipeTypes` |
| `src/disabled/java/steve_gall/minecolonies_compatibility/module/common/farmersdelight/menu/CuttingTeachMenu.java` | `vectorwing.farmersdelight.common.crafting.CuttingBoardRecipe`<br>`vectorwing.farmersdelight.common.crafting.CuttingBoardRecipeInput`<br>`vectorwing.farmersdelight.common.crafting.ingredient.ChanceResult`<br>`vectorwing.farmersdelight.common.registry.ModRecipeTypes` |
| `src/disabled/java/steve_gall/minecolonies_compatibility/module/common/farmersdelight/menu/PlatingTeachMenu.java` | `vectorwing.farmersdelight.common.block.FeastBlock` |
| `src/disabled/java/steve_gall/minecolonies_compatibility/module/common/farmersdelight/network/CookingOpenTeachMessage.java` | Platform / integration classes only |
| `src/disabled/java/steve_gall/minecolonies_compatibility/module/common/farmersdelight/network/CuttingOpenTeachMessage.java` | Platform / integration classes only |
| `src/disabled/java/steve_gall/minecolonies_compatibility/module/common/farmersdelight/network/PlatingOpenTeachMessage.java` | Platform / integration classes only |
| `src/disabled/java/steve_gall/minecolonies_compatibility/module/common/fruitsdelight/DurianBlockFruit.java` | `dev.xkmc.fruitsdelight.content.block.BaseLeavesBlock`<br>`dev.xkmc.fruitsdelight.init.plants.FDTrees` |
| `src/disabled/java/steve_gall/minecolonies_compatibility/module/common/fruitsdelight/FruitBushFruit.java` | `dev.xkmc.fruitsdelight.content.block.BaseBushBlock`<br>`dev.xkmc.fruitsdelight.content.block.DoubleFruitBushBlock`<br>`dev.xkmc.fruitsdelight.init.plants.FDBushes` |
| `src/disabled/java/steve_gall/minecolonies_compatibility/module/common/fruitsdelight/FruitsDelightModule.java` | `dev.xkmc.fruitsdelight.content.block.PassableLeavesBlock`<br>`dev.xkmc.fruitsdelight.init.plants.FDBushes`<br>`dev.xkmc.fruitsdelight.init.plants.FDTrees` |
| `src/disabled/java/steve_gall/minecolonies_compatibility/module/common/fruitsdelight/TreeLeavesFruit.java` | `dev.xkmc.fruitsdelight.content.block.BaseLeavesBlock`<br>`dev.xkmc.fruitsdelight.content.block.PassableLeavesBlock`<br>`dev.xkmc.fruitsdelight.init.plants.FDTrees` |

Adjacent disabled modules such as Neapolitan, Meadow, Vinery and Butchercraft are not base-FD integrations merely because they concern food. They do not import the three FD-family namespaces above and are not activated or ported by this change.
