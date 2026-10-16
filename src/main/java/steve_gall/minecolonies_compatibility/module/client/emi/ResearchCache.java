package steve_gall.minecolonies_compatibility.module.client.emi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.MinecoloniesAPIProxy;
import com.minecolonies.api.research.IGlobalResearch;
import com.minecolonies.api.research.IResearchRequirement;
import com.minecolonies.api.research.requirements.BuildingAlternatesResearchRequirement;
import com.minecolonies.api.research.requirements.BuildingResearchRequirement;
import com.minecolonies.api.research.requirements.ResearchResearchRequirement;
import com.minecolonies.api.research.util.ResearchConstants;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.Tuple;
import com.minecolonies.api.util.constant.Constants;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

/** Display data of one global research (was the JEI ResearchCategory.ResearchCache). */
public class ResearchCache
{
	public final IGlobalResearch research;
	public final Component header;
	public final Component name;
	public final List<Component> effects;
	public final List<Tuple<IResearchRequirement, List<ItemStack>>> requirements;
	public final List<List<ItemStack>> costs;
	public final List<Component> tooltip;

	public ResearchCache(IGlobalResearch research)
	{
		var branch = MinecoloniesAPIProxy.getInstance().getGlobalResearchTree().getBranchData(research.getBranch());
		var depth = research.getDepth();
		var shownDepth = (depth > Constants.MAX_BUILDING_LEVEL) ? Constants.MAX_BUILDING_LEVEL : depth;
		var level = Component.translatable("com.minecolonies.coremod.gui.research.tier.header", shownDepth, branch.getHoursTime(depth));

		this.research = research;
		this.header = MutableComponent.create(branch.getName()).append(" - ").append(level);
		this.name = MutableComponent.create(research.getName());
		this.effects = research.getEffects().stream().map(effect -> (Component) MutableComponent.create(effect.getName())).toList();
		this.requirements = research.getResearchRequirements().stream().map(requirement -> new Tuple<>(requirement, this.getDisplayItemStacks(requirement).toList())).toList();
		this.costs = research.getCostList().stream().map(cost -> Arrays.asList(cost.getItems())).toList();

		this.tooltip = new ArrayList<>();
		this.tooltip.add(this.header);
		this.tooltip.add(Component.empty().append(this.name).withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.GOLD));
		this.tooltip.addAll(this.effects);
		var style = Style.EMPTY.withColor(ResearchConstants.COLOR_TEXT_UNFULFILLED);

		for (var requirement : research.getResearchRequirements())
		{
			this.tooltip.add(Component.literal(" - ").append(requirement.getDesc()).withStyle(style));
		}

		for (var cost : research.getCostList())
		{
			this.tooltip.add(Component.literal(" - ").append(Component.translatable("com.minecolonies.coremod.research.limit.cost", ItemStackUtils.getTranslatedName(cost))).withStyle(style));
		}

	}

	public static Stream<IGlobalResearch> getGlobalResearches()
	{
		var tree = MinecoloniesAPIProxy.getInstance().getGlobalResearchTree();
		return tree.getBranches().stream().flatMap(branchId -> tree.getPrimaryResearch(branchId).stream()).flatMap(ResearchCache::getGlobalResearches);
	}

	private static Stream<IGlobalResearch> getGlobalResearches(Identifier researchId)
	{
		var research = MinecoloniesAPIProxy.getInstance().getGlobalResearchTree().getResearch(researchId);
		return Stream.concat(Stream.of(research), research.getChildren().stream().flatMap(ResearchCache::getGlobalResearches));
	}

	private Stream<ItemStack> getDisplayItemStacks(IResearchRequirement requirement)
	{
		if (requirement instanceof BuildingAlternatesResearchRequirement alternateBuildingRequirement)
		{
			return alternateBuildingRequirement.getBuildings().stream().map(name -> new ItemStack(this.getBuildingItem(name), alternateBuildingRequirement.getBuildingLevel()));
		}
		else if (requirement instanceof BuildingResearchRequirement buildingRequirement)
		{
			return Stream.of(new ItemStack(this.getBuildingItem(buildingRequirement.getBuilding()), buildingRequirement.getBuildingLevel()));
		}
		else if (requirement instanceof ResearchResearchRequirement)
		{
			return Stream.of(new ItemStack(Items.BOOK, 1));
		}

		return Stream.empty();
	}

	private ItemLike getBuildingItem(Identifier buildingName)
	{
		var building = IMinecoloniesAPI.getInstance().getBuildingRegistry().getValue(buildingName);
		return building != null ? building.getBuildingBlock() : Items.AIR;
	}

}
