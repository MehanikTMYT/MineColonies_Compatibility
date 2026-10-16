package steve_gall.minecolonies_compatibility.module.client.emi;

import java.util.ArrayList;
import java.util.List;

import com.minecolonies.api.blocks.ModBlocks;
import steve_gall.minecolonies_tweaks.core.common.item.ItemSerializationHelper;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiPlayerInventory;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.recipe.handler.EmiCraftContext;
import dev.emi.emi.api.recipe.handler.EmiRecipeHandler;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.fluids.FluidStack;
import steve_gall.minecolonies_compatibility.api.client.IFluidGhostScreen;
import steve_gall.minecolonies_compatibility.api.client.IItemGhostScreen;
import steve_gall.minecolonies_compatibility.api.client.jei.GhostTarget;
import steve_gall.minecolonies_compatibility.api.client.jei.ItemGhostSlotTarget;
import steve_gall.minecolonies_compatibility.api.common.butcher.ButcherableIconCache;
import steve_gall.minecolonies_compatibility.api.common.butcher.CustomizedButcherable;
import steve_gall.minecolonies_compatibility.api.common.inventory.IItemGhostSlot;
import steve_gall.minecolonies_compatibility.api.common.plant.CustomizedFruit;
import steve_gall.minecolonies_compatibility.api.common.plant.FruitIconCache;
import steve_gall.minecolonies_compatibility.core.common.MineColoniesCompatibility;
import steve_gall.minecolonies_compatibility.core.common.crafting.RecipeLookup;
import steve_gall.minecolonies_compatibility.core.common.init.ModItems;
import steve_gall.minecolonies_compatibility.core.common.init.ModMenuTypes;
import steve_gall.minecolonies_compatibility.core.common.inventory.SmithingTeachMenu;
import steve_gall.minecolonies_compatibility.core.common.inventory.StonecutterTeachMenu;
import steve_gall.minecolonies_compatibility.core.common.network.message.JEIRecipeTransferMessage;
import steve_gall.minecolonies_compatibility.core.common.util.NBTUtils2;

/**
 * EMI integration (fleet 26.1.2; replaces the JEI plugin kept in src/disabled):
 * <ul>
 * <li>categories: research (university), orchardist fruits, butcher products;</li>
 * <li>"+" transfer of vanilla smithing / stonecutting recipes into the teaching menus;</li>
 * <li>drag and drop of items / fluids onto ghost slots of the teaching screens.</li>
 * </ul>
 * Recipes of the custom crafting types (smithing, stonecutting, bucket filling workers) are shown by MineColonies' own
 * EMI plugin, which lists every crafting module. JEI's recipe click areas have no EMI equivalent.
 */
@EmiEntrypoint
public class CompatibilityEmiPlugin implements EmiPlugin
{
	public static final EmiRecipeCategory RESEARCH = category("research", EmiStack.of(ModBlocks.blockHutUniversity), Component.translatable("com.minecolonies.coremod.research.research"));
	public static final EmiRecipeCategory ORCHARDIST = category("orchardist_fruit", EmiStack.of(ModBlocks.blockHutLumberjack), Component.translatable("com.minecolonies_compatibility.job.orchardist"));
	public static final EmiRecipeCategory BUTCHER = category("butcher_butcherable", EmiStack.of(ModItems.BUTCHERABLE_ICON.get()), Component.translatable("com.minecolonies_compatibility.job.butcher"));

	private static EmiRecipeCategory category(String path, EmiStack icon, Component name)
	{
		return new EmiRecipeCategory(MineColoniesCompatibility.rl(path), icon)
		{
			@Override
			public Component getName()
			{
				return name;
			}
		};
	}

	@Override
	public void register(EmiRegistry registry)
	{
		registry.addCategory(RESEARCH);
		registry.addCategory(ORCHARDIST);
		registry.addCategory(BUTCHER);
		registry.addWorkstation(RESEARCH, EmiStack.of(ModBlocks.blockHutUniversity));
		registry.addWorkstation(ORCHARDIST, EmiStack.of(ModBlocks.blockHutLumberjack));
		registry.addWorkstation(BUTCHER, EmiStack.of(ModItems.BUTCHERABLE_ICON.get()));

		registry.addRecipeHandler(ModMenuTypes.SMITHING_TEACH.get(), new SmithingTeaching());
		registry.addRecipeHandler(ModMenuTypes.STONECUTTING_TEACH.get(), new StonecutterTeaching());
		registry.addGenericDragDropHandler(CompatibilityEmiPlugin::dropOnGhostTarget);

		// Research tree and the fruit/butcherable registries are filled by datapack reload: read them late.
		registry.addDeferredRecipes(consumer ->
		{
			for (var research : ResearchCache.getGlobalResearches().toList())
			{
				consumer.accept(new CompatibilityEmiRecipes.Research(RESEARCH, synthetic("research", research.getId()), new ResearchCache(research)));
			}

			for (CustomizedFruit fruit : CustomizedFruit.getRegistry().values())
			{
				consumer.accept(new CompatibilityEmiRecipes.Fruit(ORCHARDIST, synthetic("orchardist", fruit.getId()), new FruitIconCache(fruit)));
			}

			for (CustomizedButcherable butcherable : CustomizedButcherable.getRegistry().values())
			{
				consumer.accept(new CompatibilityEmiRecipes.Butcher(BUTCHER, synthetic("butcher", butcherable.getId()), new ButcherableIconCache(butcherable)));
			}

		});
	}

	/** EMI convention for generated recipes: path starts with '/'. */
	private static Identifier synthetic(String kind, Identifier id)
	{
		return MineColoniesCompatibility.rl("/" + kind + "/" + id.getNamespace() + "/" + id.getPath());
	}

	// ---- drag & drop onto ghost slots (was the JEI GhostIngredientHandler) ----

	private static boolean dropOnGhostTarget(Screen screen, EmiIngredient ingredient, int mouseX, int mouseY)
	{
		if (!(screen instanceof AbstractContainerScreen<?> containerScreen) || ingredient.getEmiStacks().isEmpty())
		{
			return false;
		}

		var emiStack = ingredient.getEmiStacks().get(0);
		var left = containerScreen.getGuiLeft();
		var top = containerScreen.getGuiTop();
		var targets = new ArrayList<GhostTarget<?>>();
		Object payload;

		if (emiStack.getKey() instanceof Fluid fluid)
		{
			payload = new FluidStack(fluid, (int) Math.max(1, emiStack.getAmount()));

			if (screen instanceof IFluidGhostScreen ghostScreen)
			{
				targets.addAll(ghostScreen.getFluidGhostSlots());
			}

		}
		else
		{
			var item = emiStack.getItemStack();

			if (item.isEmpty())
			{
				return false;
			}

			payload = item;
			var slots = containerScreen.getMenu().slots;

			for (var i = 0; i < slots.size(); i++)
			{
				var slot = slots.get(i);

				if (slot instanceof IItemGhostSlot ghostSlot && ghostSlot.canAccept(item))
				{
					targets.add(new ItemGhostSlotTarget(slot, i));
				}

			}

			if (screen instanceof IItemGhostScreen ghostScreen)
			{
				targets.addAll(ghostScreen.getItemGhostSlots());
			}

		}

		for (var target : targets)
		{
			var area = target.getArea();

			if (mouseX >= left + area.getX() && mouseX < left + area.getX() + area.getWidth() && mouseY >= top + area.getY() && mouseY < top + area.getY() + area.getHeight())
			{
				accept(target, payload);
				return true;
			}

		}

		return false;
	}

	@SuppressWarnings("unchecked")
	private static <I> void accept(GhostTarget<I> target, Object payload)
	{
		target.accept((I) payload);
	}

	// ---- "+" transfer into the teaching menus (was the JEI recipe transfer handlers) ----

	private abstract static class Teaching<T extends AbstractContainerMenu, R extends Recipe<?>> implements EmiRecipeHandler<T>
	{
		private final EmiRecipeCategory category;
		private final Class<R> recipeClass;

		Teaching(EmiRecipeCategory category, Class<R> recipeClass)
		{
			this.category = category;
			this.recipeClass = recipeClass;
		}

		@Override
		public EmiPlayerInventory getInventory(AbstractContainerScreen<T> screen)
		{
			// Teaching never consumes items: ghost slots only.
			return new EmiPlayerInventory(List.of());
		}

		@Override
		public boolean supportsRecipe(EmiRecipe recipe)
		{
			return recipe.getCategory() == this.category && recipe.getId() != null;
		}

		@SuppressWarnings("unchecked")
		protected RecipeHolder<R> holder(EmiRecipe recipe)
		{
			var level = Minecraft.getInstance().level;

			if (level == null || recipe.getId() == null)
			{
				return null;
			}

			var holder = RecipeLookup.byKey(level, recipe.getId()).orElse(null);
			return holder != null && this.recipeClass.isInstance(holder.value()) ? (RecipeHolder<R>) holder : null;
		}

		protected static List<ItemStack> firstStacks(EmiRecipe recipe)
		{
			return recipe.getInputs().stream().map(ingredient -> ingredient.isEmpty() ? ItemStack.EMPTY : ingredient.getEmiStacks().get(0).getItemStack()).toList();
		}

		protected static CompoundTag inputPayload(List<ItemStack> input)
		{
			var tag = new CompoundTag();
			var registries = Minecraft.getInstance().player.registryAccess();
			NBTUtils2.serializeCollection(tag, "input", input, ItemSerializationHelper.serializerTag(registries));
			return tag;
		}

	}

	private static final class SmithingTeaching extends Teaching<SmithingTeachMenu, SmithingRecipe>
	{
		SmithingTeaching()
		{
			super(VanillaEmiRecipeCategories.SMITHING, SmithingRecipe.class);
		}

		@Override
		public boolean canCraft(EmiRecipe recipe, EmiCraftContext<SmithingTeachMenu> context)
		{
			var holder = this.holder(recipe);

			if (holder == null)
			{
				return false;
			}

			var input = firstStacks(recipe);
			var menu = context.getScreenHandler();

			if (input.size() >= 3 && menu.getRecipeError(new SmithingRecipeInput(input.get(0), input.get(1), input.get(2))) != null)
			{
				return false;
			}

			return menu.getRecipeError(holder) == null;
		}

		@Override
		public boolean craft(EmiRecipe recipe, EmiCraftContext<SmithingTeachMenu> context)
		{
			var holder = this.holder(recipe);

			if (holder == null)
			{
				return false;
			}

			ClientPacketDistributor.sendToServer(new JEIRecipeTransferMessage<>(context.getScreenHandler(), holder, inputPayload(firstStacks(recipe))));
			Minecraft.getInstance().setScreen(context.getScreen());
			return true;
		}

	}

	private static final class StonecutterTeaching extends Teaching<StonecutterTeachMenu, StonecutterRecipe>
	{
		StonecutterTeaching()
		{
			super(VanillaEmiRecipeCategories.STONECUTTING, StonecutterRecipe.class);
		}

		@Override
		public boolean canCraft(EmiRecipe recipe, EmiCraftContext<StonecutterTeachMenu> context)
		{
			var holder = this.holder(recipe);
			return holder != null && context.getScreenHandler().getRecipeError(holder) == null;
		}

		@Override
		public boolean craft(EmiRecipe recipe, EmiCraftContext<StonecutterTeachMenu> context)
		{
			var holder = this.holder(recipe);

			if (holder == null)
			{
				return false;
			}

			ClientPacketDistributor.sendToServer(new JEIRecipeTransferMessage<>(context.getScreenHandler(), holder, inputPayload(firstStacks(recipe))));
			Minecraft.getInstance().setScreen(context.getScreen());
			return true;
		}

	}

}
