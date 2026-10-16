package steve_gall.minecolonies_compatibility.core.common.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.IntFunction;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

public class NBTUtils2
{
	public static <T> List<T> deserializeList(@NotNull CompoundTag tag, @NotNull String key, Function<CompoundTag, T> elementFunc)
	{
		return deserializeList(tag, key, ArrayList::new, elementFunc);
	}

	public static <T, COLLECTION extends Collection<T>> COLLECTION deserializeList(@NotNull CompoundTag tag, @NotNull String key, IntFunction<COLLECTION> collectionFunc, Function<CompoundTag, T> elementFunc)
	{
		var listTag = tag.getListOrEmpty(key);
		var collection = collectionFunc.apply(listTag.size());

		for (var i = 0; i < listTag.size(); i++)
		{
			collection.add(elementFunc.apply(listTag.getCompoundOrEmpty(i)));
		}

		return collection;
	}

	public static <T> ListTag serializeCollection(@NotNull CompoundTag tag, @NotNull String key, @NotNull Collection<T> list, @NotNull Function<T, CompoundTag> func)
	{
		var listTag = new ListTag();
		tag.put(key, listTag);

		for (T element : list)
		{
			listTag.add(func.apply(element));
		}

		return listTag;
	}

	public static CompoundTag getOrEmpty(@Nullable CompoundTag tag, @NotNull String key)
	{
		if (tag != null)
		{
			if (tag.contains(key, Tag.TAG_COMPOUND))
			{
				return tag.getCompoundOrEmpty(key);
			}

		}

		return new CompoundTag();
	}

	public static CompoundTag getOrCreate(@Nullable CompoundTag tag, @NotNull String key)
	{
		if (tag != null)
		{
			if (tag.contains(key, Tag.TAG_COMPOUND))
			{
				return tag.getCompoundOrEmpty(key);
			}

			var child = new CompoundTag();
			tag.put(key, child);
			return child;
		}

		return new CompoundTag();
	}

	private NBTUtils2()
	{

	}

}
