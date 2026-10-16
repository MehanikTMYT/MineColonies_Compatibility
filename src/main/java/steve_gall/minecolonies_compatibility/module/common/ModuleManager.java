package steve_gall.minecolonies_compatibility.module.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import steve_gall.minecolonies_compatibility.module.common.ae2.AppliedEnergistics2Module;
import steve_gall.minecolonies_compatibility.module.common.create.CreateModule;
import steve_gall.minecolonies_compatibility.module.common.dynamictrees.DynamicTreesModule;
import steve_gall.minecolonies_compatibility.module.common.jade.JadeModule;
import steve_gall.minecolonies_compatibility.module.common.minecraft.MinecraftModule;
import steve_gall.minecolonies_compatibility.module.common.oreberries.OreberriesModule;
import steve_gall.minecolonies_compatibility.module.common.polymorph.PolymorphModule;
import steve_gall.minecolonies_compatibility.module.common.refinedstorage.RefinedStorageModule;
import steve_gall.minecolonies_compatibility.module.common.regions_unexplored.RegionsUnexploredModule;
import steve_gall.minecolonies_compatibility.module.common.reliquary.ReliquaryModule;
import steve_gall.minecolonies_compatibility.module.common.silentgear.SilentGearModule;
import steve_gall.minecolonies_compatibility.module.common.undergarden.UndergardenModule;

public class ModuleManager
{
	private static final List<OptionalModule<?>> _MODULES;
	public static final List<OptionalModule<?>> MODULES;
	private static boolean INITIALIZED;
	private static final List<OptionalModule<?>> _LOADED_MODULES;
	public static final List<OptionalModule<?>> LOADED_MODULES;

	static
	{
		_MODULES = new ArrayList<>();
		MODULES = Collections.unmodifiableList(_MODULES);

		_LOADED_MODULES = new ArrayList<>();
		LOADED_MODULES = Collections.unmodifiableList(_LOADED_MODULES);
	}

	public static final OptionalModule<MinecraftModule> MINECRAFT = register("minecraft", () -> MinecraftModule::new);
	public static final OptionalModule<AppliedEnergistics2Module> AE2 = register("ae2", () -> AppliedEnergistics2Module::new);
	// fleet 26.1.2: "aether" module in src/disabled (mod not released for 26.1.2 NeoForge)
	// fleet 26.1.2: "ars_nouveau" module in src/disabled (mod not released for 26.1.2 NeoForge)
	// fleet 26.1.2: "atmospheric" module in src/disabled (mod not released for 26.1.2 NeoForge)
	// fleet 26.1.2: "cobblemon" module in src/disabled (mod not released for 26.1.2 NeoForge)
	public static final OptionalModule<CreateModule> CREATE = register("create", () -> CreateModule::new);
	// fleet 26.1.2: "croptopia" module in src/disabled (mod not released for 26.1.2 NeoForge)
	// fleet 26.1.2: "culturaldelights" module in src/disabled (mod not released for 26.1.2 NeoForge)
	public static final OptionalModule<DynamicTreesModule> DYNAMICTREES = register("dynamictrees", () -> DynamicTreesModule::new);
	// fleet 26.1.2: "musketmod" module in src/disabled (mod not released for 26.1.2 NeoForge)
	// fleet 26.1.2: "farmersdelight" module in src/disabled (mod not released for 26.1.2 NeoForge)
	// fleet 26.1.2: "butchercraft" module in src/disabled (mod not released for 26.1.2 NeoForge)
	// fleet 26.1.2: "fruitsdelight" module in src/disabled (mod not released for 26.1.2 NeoForge)
	// fleet 26.1.2: "immersiveengineering" module in src/disabled (mod not released for 26.1.2 NeoForge)
	public static final OptionalModule<JadeModule> JADE = register("jade", () -> JadeModule::new);
	// fleet 26.1.2: "meadow" module in src/disabled (mod not released for 26.1.2 NeoForge)
	// fleet 26.1.2: "vinery" module in src/disabled (mod not released for 26.1.2 NeoForge)
	// fleet 26.1.2: "neapolitan" module in src/disabled (mod not released for 26.1.2 NeoForge)
	public static final OptionalModule<OreberriesModule> OREBERRIES = register("oreberriesreplanted", () -> OreberriesModule::new);
	// fleet 26.1.2: "pamhc2trees" module in src/disabled (mod not released for 26.1.2 NeoForge)
	public static final OptionalModule<PolymorphModule> POLYMORPH = register("polymorph_plus", () -> PolymorphModule::new); // fleet 26.1.2: Polymorph+ (mod id polymorph_plus, Polymorph API kept)
	public static final OptionalModule<RefinedStorageModule> REFINEDSTORAGE = register("refinedstorage", () -> RefinedStorageModule::new);
	public static final OptionalModule<RegionsUnexploredModule> REGIONS_UNEXPLORED = register("regions_unexplored", () -> RegionsUnexploredModule::new);
	public static final OptionalModule<ReliquaryModule> RELIQUARY = register("reliquary", () -> ReliquaryModule::new);
	// fleet 26.1.2: "scguns" module in src/disabled (mod not released for 26.1.2 NeoForge)
	public static final OptionalModule<SilentGearModule> SILENTGEAR = register("silentgear", () -> SilentGearModule::new);
	// fleet 26.1.2: "tacz" module in src/disabled (mod not released for 26.1.2 NeoForge)
	public static final OptionalModule<UndergardenModule> UNDERGARDEN = register("undergarden", () -> UndergardenModule::new);

	private static <MODULE extends AbstractModule> OptionalModule<MODULE> register(String modid, Supplier<Supplier<MODULE>> initializer)
	{
		return register(modid, OptionalModule::new, initializer);
	}

	private static <WRAPPER extends OptionalModule<MODULE>, MODULE extends AbstractModule> WRAPPER register(String modid, BiFunction<String, Supplier<Supplier<MODULE>>, WRAPPER> wrapper, Supplier<Supplier<MODULE>> initializer)
	{
		var module = wrapper.apply(modid, initializer);
		_MODULES.add(module);
		return module;
	}

	public static boolean isInitialized()
	{
		return INITIALIZED;
	}

	public static void initialize()
	{
		if (INITIALIZED)
		{
			throw new IllegalCallerException();
		}

		INITIALIZED = true;
		_LOADED_MODULES.clear();

		for (var module : MODULES)
		{
			module.tryLoad();

			if (module.isLoaded())
			{
				_LOADED_MODULES.add(module);
			}

		}

	}

}
