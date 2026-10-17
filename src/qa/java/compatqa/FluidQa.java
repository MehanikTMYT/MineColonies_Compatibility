package compatqa;

import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import steve_gall.minecolonies_compatibility.core.common.crafting.BucketFillingRecipeStorage;

@Mod("compat_fluid_qa")
public final class FluidQa {
    public FluidQa(net.neoforged.bus.api.IEventBus modBus) {
        NeoForge.EVENT_BUS.addListener(this::started);
        if (net.neoforged.fml.loading.FMLEnvironment.getDist().isClient()) {
            ClientFluidQa.register(modBus);
        }
    }

    private void started(ServerStartedEvent event) {
        roundTrips(event.getServer().registryAccess());
        var player = net.neoforged.neoforge.common.util.FakePlayerFactory.getMinecraft(event.getServer().overworld());
        for (var item : new net.minecraft.world.item.Item[] {Items.WATER_BUCKET, Items.LAVA_BUCKET}) {
            var filled = new ItemStack(item);
            var recipe = steve_gall.minecolonies_compatibility.core.common.crafting.BucketFillingCraftingType.parse(filled);
            check(recipe != null && recipe.getFluidAmount() == 1000, "real container parse");
            var buffer = new net.minecraft.network.FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
            new steve_gall.minecolonies_tweaks.api.common.building.module.ModulePos(
                new steve_gall.minecolonies_tweaks.api.common.building.BuildingPos(net.minecraft.world.level.Level.OVERWORLD, 1, net.minecraft.core.BlockPos.ZERO),
                "bucket_filling").serializeBuffer(buffer);
            var menu = new steve_gall.minecolonies_compatibility.core.common.inventory.BucketFillingTeachMenu(1, player.getInventory(), buffer);
            buffer.release();
            menu.onRecipeTransfer(recipe, new CompoundTag());
            check(ItemStack.matches(menu.getInputContainer().getItem(0), filled), "server ghost slot");
            check(recipe.equals(menu.getRecipe()), "server recipe validation preserves fluid");
        }
        System.out.println("FLUID_QA: server serialization and real bucket ghost slots PASS");
    }

    public static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    static void roundTrips(HolderLookup.Provider registries) {
        for (var fluid : new net.minecraft.world.level.material.Fluid[] {Fluids.WATER, Fluids.LAVA}) {
            for (int amount : new int[] {250, 1000, 2000}) {
                var empty = new ItemStack(Items.BUCKET);
                empty.set(DataComponents.CUSTOM_NAME, Component.literal("container data"));
                var filled = new ItemStack(Items.WATER_BUCKET);
                filled.set(DataComponents.CUSTOM_NAME, Component.literal("filled container data"));
                var patch = DataComponentPatch.builder().set(DataComponents.CUSTOM_NAME, Component.literal("fluid data")).build();
                var recipe = new BucketFillingRecipeStorage(empty, fluid, amount, patch, filled);
                var tag = new CompoundTag();
                BucketFillingRecipeStorage.serialize(registries, null, tag, recipe);
                var restored = BucketFillingRecipeStorage.deserialize(registries, null, tag);
                check(recipe.equals(restored), "storage round trip " + fluid + " / " + amount);
                check(restored.getFluidStack().getAmount() == amount, "amount");
                check(restored.getFluidStack().getComponentsPatch().equals(patch), "fluid components");
                check(ItemStack.matches(restored.getFilledBucket(), filled), "container components");
                empty.setCount(9);
                check(recipe.getEmptyBucket().getCount() == 1, "defensive container copy");
                tag.putString(BucketFillingRecipeStorage.TAG_FLUID, "compat_fluid_qa:unknown");
                boolean rejected = false;
                try { BucketFillingRecipeStorage.deserialize(registries, null, tag); }
                catch (IllegalArgumentException expected) { rejected = true; }
                check(rejected, "unknown fluid must be rejected");
            }
        }
    }
}
