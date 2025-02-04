package com.meteor.extrabotany.common.entity;

import java.util.List;

import javax.annotation.Nonnull;

import com.gamerforea.eventhelper.util.EventUtils;
import com.meteor.extrabotany.ExtraBotany;
import com.meteor.extrabotany.api.ExtraBotanyAPI;
import com.meteor.extrabotany.api.entity.IBossProjectile;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class EntitySubspaceSpear extends EntityThrowableCopy implements IBossProjectile {

	private static final String TAG_LIVE_TICKS = "liveTicks";
	private static final String TAG_ROTATION = "rotation";
	private static final String TAG_DAMAGE = "damage";
	private static final String TAG_LIFE = "life";
	private static final String TAG_PITCH = "pitch";

	private static final DataParameter<Integer> LIVE_TICKS = EntityDataManager.createKey(EntitySubspaceSpear.class,
			DataSerializers.VARINT);
	private static final DataParameter<Float> ROTATION = EntityDataManager.createKey(EntitySubspaceSpear.class,
			DataSerializers.FLOAT);
	private static final DataParameter<Float> DAMAGE = EntityDataManager.createKey(EntitySubspaceSpear.class,
			DataSerializers.FLOAT);
	private static final DataParameter<Integer> LIFE = EntityDataManager.createKey(EntitySubspaceSpear.class,
			DataSerializers.VARINT);
	private static final DataParameter<Float> PITCH = EntityDataManager.createKey(EntitySubspaceSpear.class,
			DataSerializers.FLOAT);

	public EntitySubspaceSpear(World worldIn) {
		super(worldIn);
	}

	public EntitySubspaceSpear(World world, EntityLivingBase thrower) {
		super(world, thrower);
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		setSize(0F, 0F);
		dataManager.register(LIVE_TICKS, 0);
		dataManager.register(ROTATION, 0F);
		dataManager.register(DAMAGE, 0F);
		dataManager.register(LIFE, 0);
		dataManager.register(PITCH, 0F);
	}

	@Override
	public boolean isImmuneToExplosions() {
		return true;
	}

	@Override
	protected float getGravityVelocity() {
		return 0F;
	}

	@Override
	public void onUpdate() {

		EntityLivingBase thrower = getThrower();
		if (!world.isRemote && (thrower == null || thrower.isDead)) {
			setDead();
			return;
		}
		EntityLivingBase player = thrower;
		if (!world.isRemote) {
			AxisAlignedBB axis = new AxisAlignedBB(posX - 1F, posY - 0.45F, posZ - 1F, lastTickPosX + 1F,
					lastTickPosY + 0.45F, lastTickPosZ + 1F);
			List<EntityLivingBase> entities = world.getEntitiesWithinAABB(EntityLivingBase.class, axis);
			for (EntityLivingBase living : entities) {
				if (living == thrower)
					continue;

				if (ExtraBotany.isTableclothServer && EventUtils.cantAttack((EntityPlayer) thrower, living))
					continue;

				if (living.hurtTime == 0) {
					ExtraBotanyAPI.dealTrueDamage(this.getThrower(), living, getDamage() * 0.4F);
					attackedFrom(living, player, (int) (getDamage() * 1.5F));
				}

			}
		}
		super.onUpdate();

		if (ticksExisted > getLife())
			setDead();
	}

	public static void attackedFrom(EntityLivingBase target, EntityLivingBase player, int i) {
		if (player != null && player instanceof EntityPlayer)
			target.attackEntityFrom(DamageSource.causePlayerDamage((EntityPlayer) player), i);
		else
			target.attackEntityFrom(DamageSource.causeMobDamage(player), i);
	}

	@Override
	protected void onImpact(RayTraceResult pos) {
		EntityLivingBase thrower = getThrower();
		if (pos.entityHit == null || pos.entityHit != thrower) {

		}
	}

	@Override
	public void writeEntityToNBT(@Nonnull NBTTagCompound cmp) {
		super.writeEntityToNBT(cmp);
		cmp.setInteger(TAG_LIVE_TICKS, getLiveTicks());
		cmp.setFloat(TAG_ROTATION, getRotation());
		cmp.setInteger(TAG_LIFE, getLife());
		cmp.setFloat(TAG_DAMAGE, getDamage());
		cmp.setFloat(TAG_PITCH, getPitch());
	}

	@Override
	public void readEntityFromNBT(@Nonnull NBTTagCompound cmp) {
		super.readEntityFromNBT(cmp);
		setLiveTicks(cmp.getInteger(TAG_LIVE_TICKS));
		setRotation(cmp.getFloat(TAG_ROTATION));
		setLife(cmp.getInteger(TAG_LIFE));
		setDamage(cmp.getFloat(TAG_DAMAGE));
		setPitch(cmp.getFloat(TAG_PITCH));
	}

	public int getLiveTicks() {
		return dataManager.get(LIVE_TICKS);
	}

	public void setLiveTicks(int ticks) {
		dataManager.set(LIVE_TICKS, ticks);
	}

	public float getRotation() {
		return dataManager.get(ROTATION);
	}

	public void setRotation(float rot) {
		dataManager.set(ROTATION, rot);
	}

	public float getPitch() {
		return dataManager.get(PITCH);
	}

	public void setPitch(float rot) {
		dataManager.set(PITCH, rot);
	}

	public int getLife() {
		return dataManager.get(LIFE);
	}

	public void setLife(int delay) {
		dataManager.set(LIFE, delay);
	}

	public float getDamage() {
		return dataManager.get(DAMAGE);
	}

	public void setDamage(float delay) {
		dataManager.set(DAMAGE, delay);
	}

	@Override
	public boolean isBoss(Entity p) {
		return this.getLiveTicks() > 0;
	}
}
