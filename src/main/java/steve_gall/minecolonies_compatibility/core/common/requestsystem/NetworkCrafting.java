package steve_gall.minecolonies_compatibility.core.common.requestsystem;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.network.chat.ComponentSerialization;
import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import steve_gall.minecolonies_compatibility.core.common.MineColoniesCompatibility;
import steve_gall.minecolonies_tweaks.api.common.requestsystem.IRequestableObject;
import steve_gall.minecolonies_tweaks.core.common.item.ItemSerializationHelper;

public class NetworkCrafting implements IRequestableObject
{
	private static final Component EMPTY = Component.empty();

	public static final Identifier ID = MineColoniesCompatibility.rl("network_crafting");
	public static final Component DISPLAY_STRING = Component.translatable("minecolonies_compatibility.request.network_crafting");
	public static final Identifier ICON = MineColoniesCompatibility.rl("textures/gui/citizen/network_crafting.png");

	public static NetworkCrafting deserialize(@NotNull HolderLookup.Provider provider, @NotNull IFactoryController controller, @NotNull CompoundTag tag)
	{
		var crafting = new NetworkCrafting();
		crafting.cycles = tag.getIntOr("cycles", 0);
		crafting.item = ItemSerializationHelper.deserializeTag(provider, tag.getCompoundOrEmpty("item"));
		crafting.view = ItemSerializationHelper.deserializeTag(provider, tag.getCompoundOrEmpty("view"));
		crafting.text = textFromJson(provider, tag.getStringOr("text", ""));
		return crafting;
	}

	/** fleet 26.1.2: Component.Serializer is gone; same JSON text format through the component codec. */
	private static Component textFromJson(HolderLookup.Provider provider, String json)
	{
		if (json.isEmpty())
		{
			return Component.empty();
		}

		return ComponentSerialization.CODEC.parse(provider.createSerializationContext(JsonOps.INSTANCE), JsonParser.parseString(json)).result().orElse(Component.empty());
	}

	public static void serialize(@NotNull HolderLookup.Provider provider, @NotNull IFactoryController controller, @NotNull CompoundTag tag, @NotNull NetworkCrafting request)
	{
		tag.putInt("cycles", request.cycles);
		tag.put("item", ItemSerializationHelper.serializeTag(provider, request.item));
		tag.put("view", ItemSerializationHelper.serializeTag(provider, request.view));
		tag.putString("text", ComponentSerialization.CODEC.encodeStart(provider.createSerializationContext(JsonOps.INSTANCE), request.text).result().map(Object::toString).orElse("\"\""));
	}

	private int cycles;
	private ItemStack item = ItemStack.EMPTY;
	private ItemStack view = ItemStack.EMPTY;
	private Component text = EMPTY;

	public NetworkCrafting()
	{

	}

	@Override
	public @NotNull Identifier getId()
	{
		return ID;
	}

	@Override
	public @NotNull Component getShortDisplayString()
	{
		return Component.translatable("%s * %s", this.cycles, DISPLAY_STRING);
	}

	@Override
	public @NotNull Component getLongDisplayString()
	{
		if (!this.view.isEmpty())
		{
			return Component.translatable("%s * %s - %s: %s", this.cycles, DISPLAY_STRING, this.view.getHoverName(), this.getText());
		}
		else
		{
			return DISPLAY_STRING;
		}

	}

	@Override
	public @NotNull List<ItemStack> getDisplayStacks()
	{
		if (!this.item.isEmpty())
		{
			return Collections.singletonList(this.item);
		}
		else
		{
			return Collections.emptyList();
		}

	}

	@Override
	public @NotNull List<MutableComponent> getResolverToolTip(@NotNull IColonyView colony)
	{
		return IRequestableObject.super.getResolverToolTip(colony);
	}

	@Override
	public @NotNull Identifier getDisplayIcon()
	{
		return ICON;
	}

	public void setCycles(int cycles)
	{
		this.cycles = cycles;
	}

	public int getCycles()
	{
		return this.cycles;
	}

	public void setItem(ItemStack item)
	{
		this.item = item;
	}

	public ItemStack getItem()
	{
		return this.item;
	}

	public void setView(ItemStack view)
	{
		this.view = view;
	}

	public ItemStack getView()
	{
		return this.view;
	}

	public void setText(Component text)
	{
		this.text = text;
	}

	public Component getText()
	{
		return text;
	}

}
