package compatqa;

import com.minecolonies.core.compatibility.emi.MineColoniesEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import steve_gall.minecolonies_compatibility.core.common.crafting.BucketFillingGenericRecipe;
import steve_gall.minecolonies_compatibility.core.common.crafting.BucketFillingRecipeStorage;
import steve_gall.minecolonies_compatibility.core.common.inventory.BucketFillingTeachMenu;
import steve_gall.minecolonies_tweaks.api.common.building.BuildingPos;
import steve_gall.minecolonies_tweaks.api.common.building.module.ModulePos;
import java.util.HashSet;
import java.util.List;

final class ClientFluidQa {
    private static boolean done;
    private static boolean finished;
    static void register(net.neoforged.bus.api.IEventBus modBus) {
        NeoForge.EVENT_BUS.addListener(ClientFluidQa::tick);
        // (Tweaks 460115a registers the clientbound handlers of its bidirectional messages itself.)
    }
    private static int elapsed;
    private static int networkCase;
    private static java.util.concurrent.CompletableFuture<BucketFillingTeachMenu> pendingMenu;
    private static java.util.concurrent.CompletableFuture<Boolean> pendingCheck;
    private static BucketFillingTeachMenu clientMenu;
    private static BucketFillingTeachMenu serverMenu;
    private static dev.emi.emi.api.recipe.handler.EmiRecipeHandler<BucketFillingTeachMenu> bucketHandler;
    private static ItemStack expected;
    private static BucketFillingTeachMenu menu(net.minecraft.world.entity.player.Inventory inventory) {
        var buffer = new FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
        new ModulePos(new BuildingPos(Level.OVERWORLD, 1, BlockPos.ZERO), "bucket_filling").serializeBuffer(buffer);
        try { return new BucketFillingTeachMenu(77, inventory, buffer); }
        finally { buffer.release(); }
    }
    private static void networkTick() {
        var mc = Minecraft.getInstance();
        if (++elapsed > 2400) throw new AssertionError("network/EMI QA timeout");
        if (pendingMenu != null) {
            if (!pendingMenu.isDone()) return;
            serverMenu = pendingMenu.join();
            pendingMenu = null;
            clientMenu = menu(mc.player.getInventory());
            mc.player.containerMenu = clientMenu;
            var screen = new steve_gall.minecolonies_compatibility.core.client.gui.BucketFillingTeachScreen(
                clientMenu, mc.player.getInventory(), Component.literal("QA"));
            expected = new ItemStack(networkCase == 0 ? Items.WATER_BUCKET : Items.LAVA_BUCKET);
            var storage = steve_gall.minecolonies_compatibility.core.common.crafting.BucketFillingCraftingType.parse(expected);
            var source = new BucketFillingGenericRecipe(storage.getEmptyBucket(), storage.getFluid(), storage.getFluidAmount(),
                storage.getDataComponentPatch(), storage.getFilledBucket());
            var category = new EmiRecipeCategory(Identifier.parse("compat_fluid_qa:network"), EmiStack.of(Items.BUCKET));
            var context = new dev.emi.emi.api.recipe.handler.EmiCraftContext<>(screen,
                new dev.emi.emi.api.recipe.EmiPlayerInventory(List.of()), dev.emi.emi.api.recipe.handler.EmiCraftContext.Type.FILL_BUTTON);
            FluidQa.check(bucketHandler.craft(MineColoniesEmiRecipe.fromGeneric(category, source), context), "actual EMI craft");
            // Dummy module position is sufficient for transport, but is not a real colony GUI.
            mc.setScreen(null);
            return;
        }
        if (serverMenu != null) {
            if (pendingCheck == null) {
                pendingCheck = mc.getSingleplayerServer().submit(() ->
                    ItemStack.matches(serverMenu.getInputContainer().getItem(0), expected)
                    && serverMenu.getRecipe() != null
                    && serverMenu.getRecipe().getFluid() == (networkCase == 0 ? Fluids.WATER : Fluids.LAVA));
            } else if (pendingCheck.isDone()) {
                if (pendingCheck.join() && clientMenu.getRecipe() != null) {
                    System.out.println("FLUID_QA: actual EMI packet and server ghost PASS " + expected);
                    networkCase++;
                    serverMenu = null;
                }
                pendingCheck = null;
            }
            return;
        }
        if (networkCase < 2) {
            var uuid = mc.player.getUUID();
            pendingMenu = mc.getSingleplayerServer().submit(() -> {
                var player = mc.getSingleplayerServer().getPlayerList().getPlayer(uuid);
                var menu = menu(player.getInventory());
                player.containerMenu = menu;
                return menu;
            });
            return;
        }
        var categories = dev.emi.emi.api.EmiApi.getRecipeManager().getCategories();
        if (categories.isEmpty() || dev.emi.emi.api.EmiApi.getRecipeManager().getRecipes().isEmpty()) return;
        var ids = new HashSet<Identifier>();
        for (var category : categories) FluidQa.check(ids.add(category.getId()), "duplicate live EMI category");
        FluidQa.check(ids.contains(Identifier.parse("minecolonies_compatibility:research")), "live research category");
        System.out.println("FLUID_QA: live EMI categories unique PASS (" + ids.size() + ")");
        mc.player.containerMenu = mc.player.inventoryMenu;
        finished = true;
        mc.stop();
    }
    private static void tick(ClientTickEvent.Post event) {
        var mc = Minecraft.getInstance();
        if (finished || mc.level == null || mc.player == null) return;
        if (done) { networkTick(); return; }
        done = true;
        FluidQa.roundTrips(mc.level.registryAccess());
        var category = new EmiRecipeCategory(Identifier.parse("compat_fluid_qa:worker"), EmiStack.of(Items.BUCKET));
        var ids = new HashSet<Identifier>();
        var categories = new HashSet<Identifier>();
        var handlers = new java.util.HashMap<Object, Object>();
        var registry = (dev.emi.emi.api.EmiRegistry) java.lang.reflect.Proxy.newProxyInstance(
            ClientFluidQa.class.getClassLoader(), new Class<?>[] {dev.emi.emi.api.EmiRegistry.class},
            (proxy, method, args) -> {
                if (method.getName().equals("addCategory")) FluidQa.check(categories.add(((EmiRecipeCategory) args[0]).getId()), "duplicate category");
                if (method.getName().equals("addRecipeHandler")) handlers.put(args[0], args[1]);
                return null;
            });
        new steve_gall.minecolonies_compatibility.module.client.emi.CompatibilityEmiPlugin().register(registry);
        FluidQa.check(categories.size() == 3, "existing research/orchardist/butcher categories only");
        FluidQa.check(handlers.size() == 3, "smithing, stonecutter, bucket handlers retained");
        @SuppressWarnings("unchecked")
        var handler = (dev.emi.emi.api.recipe.handler.EmiRecipeHandler<BucketFillingTeachMenu>) handlers.get(
            steve_gall.minecolonies_compatibility.core.common.init.ModMenuTypes.BUCKET_FILLING_TEACH.get());
        bucketHandler = handler;
        for (var fluid : new net.minecraft.world.level.material.Fluid[] {Fluids.WATER, Fluids.LAVA}) {
            for (int amount : new int[] {250, 1000, 2000}) {
                var filled = new ItemStack(Items.WATER_BUCKET);
                filled.set(DataComponents.CUSTOM_NAME, Component.literal("preserve container"));
                var patch = DataComponentPatch.builder().set(DataComponents.CUSTOM_NAME, Component.literal("preserve fluid")).build();
                var source = new BucketFillingGenericRecipe(new ItemStack(Items.BUCKET), fluid, amount, patch, filled);
                var recipe = MineColoniesEmiRecipe.fromGeneric(category, source);
                FluidQa.check(recipe.getSourceRecipe() == source, "typed source");
                FluidQa.check(recipe.getInputs().size() == 2, "item + fluid input");
                var displayed = recipe.getInputs().get(1).getEmiStacks().getFirst();
                FluidQa.check(displayed.getKey() == fluid && displayed.getAmount() == amount, "EMI exact fluid");
                FluidQa.check(displayed.getComponentChanges().equals(patch), "EMI fluid components");
                var ghostFluid = steve_gall.minecolonies_compatibility.module.client.emi.EmiGhostStacks.fluid(displayed);
                FluidQa.check(ghostFluid.getFluid() == fluid && ghostFluid.getAmount() == amount
                    && ghostFluid.getComponentsPatch().equals(patch), "fluid ghost payload");
                FluidQa.check(steve_gall.minecolonies_compatibility.module.client.emi.EmiGhostStacks.fluid(
                    EmiStack.of(fluid, (long) Integer.MAX_VALUE + 1)).isEmpty(), "overflow rejected");
                FluidQa.check(ids.add(recipe.getId()), "distinct fluid/volume IDs");
                FluidQa.check(recipe.getId().equals(MineColoniesEmiRecipe.fromGeneric(category, source).getId()), "stable ID");
                var buffer = new FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
                new ModulePos(new BuildingPos(Level.OVERWORLD, 1, BlockPos.ZERO), "bucket_filling").serializeBuffer(buffer);
                var menu = new BucketFillingTeachMenu(1, mc.player.getInventory(), buffer);
                buffer.release();
                var screen = new steve_gall.minecolonies_compatibility.core.client.gui.BucketFillingTeachScreen(menu, mc.player.getInventory(), Component.literal("QA"));
                var context = new dev.emi.emi.api.recipe.handler.EmiCraftContext<>(screen,
                    new dev.emi.emi.api.recipe.EmiPlayerInventory(List.of()), dev.emi.emi.api.recipe.handler.EmiCraftContext.Type.FILL_BUTTON);
                FluidQa.check(handler.supportsRecipe(recipe) && handler.canCraft(recipe, context), "transfer allowed with no ingredients");
                menu.onRecipeTransfer(new BucketFillingRecipeStorage(source.getEmptyBucket(), fluid, amount, patch, filled), new net.minecraft.nbt.CompoundTag());
                FluidQa.check(ItemStack.matches(menu.getInputContainer().getItem(0), filled), "exact ghost container");
            }
        }
        var multi = new BucketFillingGenericRecipe(new ItemStack(Items.BUCKET), Fluids.WATER, 1000, DataComponentPatch.EMPTY, new ItemStack(Items.WATER_BUCKET)) {
            @Override public List<List<ItemStack>> getInputs() {
                return List.of(List.of(new ItemStack(Items.BUCKET)), List.of(new ItemStack(Items.DIAMOND, 3)));
            }
        };
        FluidQa.check(MineColoniesEmiRecipe.fromGeneric(category, multi).getInputs().size() == 3, "multiple item inputs preserved");
        System.out.println("FLUID_QA: client model and ghost slots PASS");
    }
}
