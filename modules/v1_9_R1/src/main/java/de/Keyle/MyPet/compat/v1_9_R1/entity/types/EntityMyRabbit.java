/*
 * This file is part of MyPet
 *
 * Copyright (C) 2011-2016 Keyle
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

package de.Keyle.MyPet.compat.v1_9_R1.entity.types;

import de.Keyle.MyPet.api.Configuration;
import de.Keyle.MyPet.api.entity.ActiveMyPet;
import de.Keyle.MyPet.api.entity.EntitySize;
import de.Keyle.MyPet.api.entity.types.MyRabbit;
import de.Keyle.MyPet.compat.v1_9_R1.entity.EntityMyPet;
import net.minecraft.server.v1_9_R1.*;

@EntitySize(width = 0.6F, height = 0.7F)
public class EntityMyRabbit extends EntityMyPet {
    private static final DataWatcherObject<Boolean> ageWatcher = DataWatcher.a(EntityMyRabbit.class, DataWatcherRegistry.h);
    private static final DataWatcherObject<Integer> variantWatcher = DataWatcher.a(EntityMyRabbit.class, DataWatcherRegistry.b);

    int jumpDelay;

    public EntityMyRabbit(World world, ActiveMyPet myPet) {
        super(world, myPet);
        this.jumpDelay = (this.random.nextInt(20) + 10);
    }

    @Override
    protected String getDeathSound() {
        return "entity.rabbit.death";
    }

    @Override
    protected String getHurtSound() {
        return "entity.rabbit.hurt";
    }

    protected String getLivingSound() {
        return "entity.rabbit.ambient";
    }

    @Override
    public void playPetStepSound() {
        makeSound("entity.rabbit.hop", 1.0F, 1.0F);
    }

    public boolean handlePlayerInteraction(EntityHuman entityhuman, EnumHand enumhand, ItemStack itemStack) {
        if (super.handlePlayerInteraction(entityhuman, enumhand, itemStack)) {
            return true;
        }

        if (getOwner().equals(entityhuman) && itemStack != null && canUseItem()) {
            if (Configuration.MyPet.Rabbit.GROW_UP_ITEM.compare(itemStack) && getMyPet().isBaby() && getOwner().getPlayer().isSneaking()) {
                if (!entityhuman.abilities.canInstantlyBuild) {
                    if (--itemStack.count <= 0) {
                        entityhuman.inventory.setItem(entityhuman.inventory.itemInHandIndex, null);
                    }
                }
                this.getMyPet().setBaby(false);
                return true;
            }
        }
        return false;
    }

    @Override
    public void initDatawatcher() {
        super.initDatawatcher();
        this.datawatcher.register(ageWatcher, false); // is baby
        this.datawatcher.register(variantWatcher, 0); // variant
    }

    @Override
    public void updateVisuals() {
        this.datawatcher.set(ageWatcher, getMyPet().isBaby());
        this.datawatcher.set(variantWatcher, (int) getMyPet().getVariant().getId());
    }

    public void onLivingUpdate() {
        super.onLivingUpdate();

        if (this.onGround && getNavigation().k() != null && jumpDelay-- <= 0) {
            getControllerJump().a();
            jumpDelay = (this.random.nextInt(10) + 10);
            if (getTarget() != null) {
                jumpDelay /= 3;
            }
            this.world.broadcastEntityEffect(this, (byte) 1);
        }
    }

    public MyRabbit getMyPet() {
        return (MyRabbit) myPet;
    }
}