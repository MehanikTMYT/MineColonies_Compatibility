package steve_gall.minecolonies_compatibility.module.common.ewewukeks_musketmod;

import ewewukek.musketmod.GunItem;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class DummyGun extends GunItem
{
	private GunItem parent;
	private float damage;

	public DummyGun()
	{
		super(new Item.Properties());
	}

	public GunItem getParent()
	{
		return this.parent;
	}

	public void setParent(GunItem parent)
	{
		this.parent = parent;
	}

	@Override
	public float bulletStdDev()
	{
		return this.parent.bulletStdDev();
	}

	@Override
	public float bulletSpeed()
	{
		return this.parent.bulletSpeed();
	}

	@Override
	public float damage()
	{
		return this.damage;
	}

	public void setDamage(float damage)
	{
		this.damage = damage;
	}

	@Override
	public SoundEvent fireSound(ItemStack item)
	{
		return this.parent.fireSound(item);
	}

	@Override
	public int pelletCount()
	{
		return this.parent.pelletCount();
	}

	@Override
	public boolean twoHanded()
	{
		return this.parent.twoHanded();
	}

	@Override
	public float bulletDropReduction()
	{
		return this.parent.bulletDropReduction();
	}

	@Override
	public int hitDurabilityDamage()
	{
		return this.parent.hitDurabilityDamage();
	}

}
