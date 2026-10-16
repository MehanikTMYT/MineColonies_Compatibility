package steve_gall.minecolonies_compatibility.module.common.ae2.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import java.util.function.Function;

import appeng.api.parts.IPart;
import appeng.api.parts.IPartItem;
import appeng.items.parts.PartItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;
import steve_gall.minecolonies_compatibility.module.common.ae2.CitizenTerminalPart;

public class ModuleParts
{
	public static final DeferredHolder<Item, PartItem<CitizenTerminalPart>> CITIZEN_TERMINAL = createPart("citizen_terminal", CitizenTerminalPart.class, CitizenTerminalPart::new);

	private static <T extends IPart> DeferredHolder<Item, PartItem<T>> createPart(String name, Class<T> partClass, Function<IPartItem<T>, T> factory)
	{
		return item(name, props -> new PartItem<>(props, partClass, factory));
	}

	private static <T extends Item> DeferredHolder<Item, T> item(String name, Function<Item.Properties, T> factory)
	{
		return ModuleItems.REGISTER.register(name, id -> factory.apply(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id))));
	}

	public static void init()
	{

	}

	private ModuleParts()
	{

	}

}
