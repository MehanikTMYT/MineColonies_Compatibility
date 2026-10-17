package steve_gall.minecolonies_compatibility.module.client.emi;

import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

/** Lossless conversion at the EMI/NeoForge boundary. Never invents a fluid or truncates an amount. */
public final class EmiGhostStacks
{
    private EmiGhostStacks() {}

    public static FluidStack fluid(EmiStack stack)
    {
        if (!(stack.getKey() instanceof Fluid fluid) || stack.getAmount() <= 0 || stack.getAmount() > Integer.MAX_VALUE)
        {
            return FluidStack.EMPTY;
        }
        var result = new FluidStack(fluid, (int) stack.getAmount());
        result.applyComponents(stack.getComponentChanges());
        return result;
    }
}
