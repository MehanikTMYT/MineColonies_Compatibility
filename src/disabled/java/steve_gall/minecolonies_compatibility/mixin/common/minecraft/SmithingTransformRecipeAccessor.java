package steve_gall.minecolonies_compatibility.mixin.common.minecraft;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;
import steve_gall.minecolonies_compatibility.core.common.crafting.SmithingRecipeAccessor;

@Mixin(value = SmithingTransformRecipe.class, remap = true)
public interface SmithingTransformRecipeAccessor extends SmithingRecipeAccessor
{
	@Override
	@Accessor(value = "template", remap = true)
	Ingredient getTemplate();

	@Override
	@Accessor(value = "base", remap = true)
	Ingredient getBase();

	@Override
	@Accessor(value = "addition", remap = true)
	Ingredient getAddition();
}
