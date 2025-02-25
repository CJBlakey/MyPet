/*
 * This file is part of MyPet
 *
 * Copyright © 2011-2020 Keyle
 * MyPet is licensed under the GNU Lesser General Public License.
 *
 * MyPet is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MyPet is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package de.Keyle.MyPet.compat.v1_17_R1.entity.types;

import java.lang.reflect.InvocationTargetException;

import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;

import de.Keyle.MyPet.api.Configuration;
import de.Keyle.MyPet.api.entity.EntitySize;
import de.Keyle.MyPet.api.entity.MyPet;
import de.Keyle.MyPet.api.entity.types.MyStrider;
import de.Keyle.MyPet.compat.v1_17_R1.CompatManager;
import de.Keyle.MyPet.compat.v1_17_R1.entity.EntityMyPet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

@EntitySize(width = 0.9F, height = 1.7F)
public class EntityMyStrider extends EntityMyPet {

	private static final EntityDataAccessor<Boolean> AGE_WATCHER = SynchedEntityData.defineId(EntityMyStrider.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Integer> BOOST_TICKS_WATCHER = SynchedEntityData.defineId(EntityMyStrider.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Boolean> HAS_RIDER_WATCHER = SynchedEntityData.defineId(EntityMyStrider.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Boolean> SADDLE_WATCHER = SynchedEntityData.defineId(EntityMyStrider.class, EntityDataSerializers.BOOLEAN);

	public EntityMyStrider(Level world, MyPet myPet) {
		super(world, myPet);
	}

	@Override
	protected String getMyPetDeathSound() {
		return "entity.strider.death";
	}

	@Override
	protected String getHurtSound() {
		return "entity.strider.hurt";
	}

	@Override
	protected String getLivingSound() {
		return "entity.strider.ambient";
	}

	@Override
	public InteractionResult handlePlayerInteraction(final Player entityhuman, InteractionHand enumhand, final ItemStack itemStack) {
		if (super.handlePlayerInteraction(entityhuman, enumhand, itemStack).consumesAction()) {
			return InteractionResult.CONSUME;
		}

		if (getOwner().equals(entityhuman) && itemStack != null && canUseItem()) {
			if (itemStack.getItem() == Items.SADDLE && !getMyPet().hasSaddle() && getOwner().getPlayer().isSneaking()) {
				getMyPet().setSaddle(CraftItemStack.asBukkitCopy(itemStack));
				if (itemStack != ItemStack.EMPTY && !entityhuman.getAbilities().instabuild) {
					itemStack.shrink(1);
					if (itemStack.getCount() <= 0) {
						entityhuman.getInventory().setItem(entityhuman.getInventory().selected, ItemStack.EMPTY);
					}
				}
				return InteractionResult.CONSUME;
			} else if (itemStack.getItem() == Items.SHEARS && getMyPet().hasSaddle() && getOwner().getPlayer().isSneaking()) {
				ItemEntity entityitem = new ItemEntity(this.level, this.getX(), this.getY() + 1, this.getZ(), CraftItemStack.asNMSCopy(getMyPet().getSaddle()));
				entityitem.pickupDelay = 10;
				entityitem.setDeltaMovement(entityitem.getDeltaMovement().add(0, this.random.nextFloat() * 0.05F, 0));
				this.level.addFreshEntity(entityitem);

				makeSound("entity.sheep.shear", 1.0F, 1.0F);
				getMyPet().setSaddle(null);
				if (itemStack != ItemStack.EMPTY && !entityhuman.getAbilities().instabuild) {
					try {
						itemStack.hurtAndBreak(1, entityhuman, (entityhuman1) -> entityhuman1.broadcastBreakEvent(enumhand));
					} catch (Error e) {
						// TODO REMOVE
						itemStack.hurtAndBreak(1, entityhuman, (entityhuman1) -> {
							try {
								CompatManager.ENTITY_LIVING_broadcastItemBreak.invoke(entityhuman1, enumhand);
							} catch (IllegalAccessException | InvocationTargetException ex) {
								ex.printStackTrace();
							}
						});
					}
				}

				return InteractionResult.CONSUME;
			} else if (Configuration.MyPet.Strider.GROW_UP_ITEM.compare(itemStack) && getMyPet().isBaby() && getOwner().getPlayer().isSneaking()) {
				if (itemStack != ItemStack.EMPTY && !entityhuman.getAbilities().instabuild) {
					itemStack.shrink(1);
					if (itemStack.getCount() <= 0) {
						entityhuman.getInventory().setItem(entityhuman.getInventory().selected, ItemStack.EMPTY);
					}
				}
				getMyPet().setBaby(false);
				return InteractionResult.CONSUME;
			}
		}
		return InteractionResult.PASS;
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		getEntityData().define(AGE_WATCHER, false);
		getEntityData().define(BOOST_TICKS_WATCHER, 0);
		getEntityData().define(HAS_RIDER_WATCHER, false);
		getEntityData().define(SADDLE_WATCHER, false);
	}

	@Override
	public void updateVisuals() {
		this.getEntityData().set(AGE_WATCHER, getMyPet().isBaby());
		this.getEntityData().set(SADDLE_WATCHER, getMyPet().hasSaddle());
	}

	@Override
	public void playPetStepSound() {
		makeSound("entity.strider.step", 0.15F, 1.0F);
	}

	@Override
	public MyStrider getMyPet() {
		return (MyStrider) myPet;
	}
}
