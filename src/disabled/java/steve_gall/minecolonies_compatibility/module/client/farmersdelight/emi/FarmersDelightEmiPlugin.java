package steve_gall.minecolonies_compatibility.module.client.farmersdelight.emi;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.function.Function;
import java.util.function.BiFunction;

import com.minecolonies.core.compatibility.emi.MineColoniesEmiRecipe;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiPlayerInventory;
import dev.emi.emi.api.recipe.handler.EmiRecipeHandler;
import dev.emi.emi.api.recipe.handler.EmiCraftContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import steve_gall.minecolonies_compatibility.core.common.crafting.RecipeLookup;
import steve_gall.minecolonies_compatibility.core.common.inventory.TeachRecipeMenu;
import steve_gall.minecolonies_compatibility.core.common.network.message.JEIRecipeTransferMessage;
import steve_gall.minecolonies_compatibility.core.common.util.NBTUtils2;
import steve_gall.minecolonies_tweaks.core.common.item.ItemSerializationHelper;

/**
 * Prepared FD teaching adapter. MineColonies owns worker categories; this plugin only adds handlers.
 * FD implementation classes are reached only after the optional-mod guard.
 */
@EmiEntrypoint
public final class FarmersDelightEmiPlugin implements EmiPlugin {
    @Override
    public void register(EmiRegistry registry) {
        if (ModList.get().isLoaded("farmersdelight")) Registration.register(registry);
    }

    private static final class Registration {
        static void register(EmiRegistry registry) {
            var menus = steve_gall.minecolonies_compatibility.module.common.farmersdelight.init.ModuleMenuTypes.COOKING_TEACH;
            // Also guard the prepared integration being disabled while FD itself is installed.
            if (!menus.isBound()) return;
            registry.addRecipeHandler(menus.get(), new Teaching<>(
                r -> holder(r, vectorwing.farmersdelight.common.crafting.CookingPotRecipe.class),
                (r, h) -> inputs(r, h.value().getIngredients(), false)));
            registry.addRecipeHandler(steve_gall.minecolonies_compatibility.module.common.farmersdelight.init.ModuleMenuTypes.CUTTING_TEACH.get(),
                new Teaching<>(r -> holder(r, vectorwing.farmersdelight.common.crafting.CuttingBoardRecipe.class),
                    (r, h) -> inputs(r, h.value().getIngredients(), true)));
            registry.addRecipeHandler(steve_gall.minecolonies_compatibility.module.common.farmersdelight.init.ModuleMenuTypes.PLATING_TEACH.get(),
                new Teaching<>(r -> r instanceof MineColoniesEmiRecipe worker
                    && worker.getSourceRecipe() instanceof steve_gall.minecolonies_compatibility.module.common.farmersdelight.crafting.PlatingGenericRecipe plating
                        ? plating.getRecipeStorage() : null,
                    (r, storage) -> {
                        if (r.getInputs().isEmpty() || r.getInputs().getFirst().getEmiStacks().isEmpty()) return null;
                        var item = r.getInputs().getFirst().getEmiStacks().getFirst().getItemStack();
                        if (item.isEmpty()) return null;
                        var tag = new CompoundTag();
                        tag.put("input", ItemSerializationHelper.serializeTag(Minecraft.getInstance().player.registryAccess(), item.copy()));
                        return tag;
                    }));
        }

        @SuppressWarnings("unchecked")
        static <R extends net.minecraft.world.item.crafting.Recipe<?>> RecipeHolder<R> holder(EmiRecipe recipe, Class<R> type) {
            var level = Minecraft.getInstance().level;
            var id = recipe instanceof MineColoniesEmiRecipe worker && worker.getSourceRecipe() != null
                ? worker.getSourceRecipe().getRecipeId() : recipe.getId();
            if (level == null || id == null) return null;
            var holder = RecipeLookup.byKey(level, id).orElse(null);
            return holder != null && type.isInstance(holder.value()) ? (RecipeHolder<R>) holder : null;
        }

        static CompoundTag inputs(EmiRecipe recipe, List<Ingredient> expected, boolean single) {
            var selected = new ArrayList<ItemStack>();
            var used = new HashSet<Integer>();
            for (var wanted : expected) {
                ItemStack chosen = ItemStack.EMPTY;
                for (int i = 0; i < recipe.getInputs().size() && chosen.isEmpty(); i++) {
                    if (used.contains(i)) continue;
                    for (var candidate : recipe.getInputs().get(i).getEmiStacks()) {
                        var item = candidate.getItemStack();
                        if (!item.isEmpty() && wanted.test(item)) {
                            chosen = item.copy();
                            used.add(i);
                            break;
                        }
                    }
                }
                if (chosen.isEmpty()) return null;
                selected.add(chosen);
            }
            if (selected.isEmpty() || (single && selected.size() != 1)) return null;
            var tag = new CompoundTag();
            var registries = Minecraft.getInstance().player.registryAccess();
            if (single) tag.put("input", ItemSerializationHelper.serializeTag(registries, selected.getFirst()));
            else NBTUtils2.serializeCollection(tag, "input", selected, ItemSerializationHelper.serializerTag(registries));
            return tag;
        }
    }

    private static final class Teaching<R, I, T extends TeachRecipeMenu<R, I>> implements EmiRecipeHandler<T> {
        private final Function<EmiRecipe, R> resolve;
        private final BiFunction<EmiRecipe, R, CompoundTag> payload;
        Teaching(Function<EmiRecipe, R> resolve, BiFunction<EmiRecipe, R, CompoundTag> payload) {
            this.resolve = resolve;
            this.payload = payload;
        }
        @Override public EmiPlayerInventory getInventory(AbstractContainerScreen<T> screen) {
            return new EmiPlayerInventory(List.of());
        }
        @Override public boolean supportsRecipe(EmiRecipe recipe) { return resolve.apply(recipe) != null; }
        @Override public boolean canCraft(EmiRecipe recipe, EmiCraftContext<T> context) {
            var source = resolve.apply(recipe);
            return source != null && context.getScreenHandler().getRecipeError(source) == null && payload.apply(recipe, source) != null;
        }
        @Override public boolean craft(EmiRecipe recipe, EmiCraftContext<T> context) {
            var source = resolve.apply(recipe);
            if (source == null || context.getScreenHandler().getRecipeError(source) != null) return false;
            var tag = payload.apply(recipe, source);
            if (tag == null) return false;
            ClientPacketDistributor.sendToServer(new JEIRecipeTransferMessage<>(context.getScreenHandler(), source, tag));
            Minecraft.getInstance().setScreen(context.getScreen());
            return true;
        }
    }
}
