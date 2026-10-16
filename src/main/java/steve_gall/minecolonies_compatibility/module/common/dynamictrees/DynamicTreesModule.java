package steve_gall.minecolonies_compatibility.module.common.dynamictrees;

import net.minecraft.world.item.crafting.RecipeMap;
import com.dtteam.dynamictrees.tree.species.Species;

import steve_gall.minecolonies_compatibility.api.common.plant.CustomizedFruit;
import steve_gall.minecolonies_compatibility.module.common.AbstractModule;

public class DynamicTreesModule extends AbstractModule
{
	@Override
	protected void onLoad()
	{
		super.onLoad();
	}

	@Override
	protected void onRecipeReloaded(RecipeMap recipes)
	{
		super.onRecipeReloaded(recipes);

		for (var species : Species.REGISTRY)
		{
			var seed = species.getSeed().orElse(null);
			var fruits = species.getFruits();

			if (seed == null || fruits.size() == 0)
			{
				continue;
			}

			CustomizedFruit.registerVolatile(new SpeciesFruit(species, seed, fruits));
		}

	}

}
