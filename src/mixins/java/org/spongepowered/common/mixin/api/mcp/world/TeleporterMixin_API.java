/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.common.mixin.api.mcp.world;

import net.minecraft.block.pattern.BlockPattern;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Teleporter;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.cause.entity.teleport.TeleportTypes;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.world.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.api.world.teleport.PortalManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.phase.entity.EntityPhase;
import org.spongepowered.common.event.tracking.phase.entity.TeleportContext;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.math.vector.Vector3d;

import javax.annotation.Nullable;

@Mixin(Teleporter.class)
public abstract class TeleporterMixin_API implements PortalManager {

    @Shadow @Final private net.minecraft.world.server.ServerWorld world;
    @Shadow public abstract boolean shadow$placeInPortal(net.minecraft.entity.Entity p_222268_1_, float p_222268_2_);

    @Shadow @Nullable public abstract BlockPattern.PortalInfo placeInExistingPortal(BlockPos p_222272_1_,
            Vec3d p_222272_2_, Direction directionIn, double p_222272_4_, double p_222272_6_,
            boolean p_222272_8_);

    @Override
    public ServerWorld getWorld() {
        return (ServerWorld) this.world;
    }

    // Welcome to the circus folks, have your ticket ready! You'll need to be at least 5ft 10" and bat shit insane to touch the code below
    @Override
    public boolean teleport(final Entity entity, final ServerLocation location) {
        // I am aware this looks odd but this is much better than a bridge method that would break teleporter logic
        // that assumes the entity is actually already at the portal before actually putting the portal there..
        final net.minecraft.entity.Entity mEntity = (net.minecraft.entity.Entity) entity;
        final Vector3d currentPosition = entity.getLocation().getPosition();

        boolean result;

        try (final TeleportContext context = EntityPhase.State.TELEPORT.createPhaseContext(PhaseTracker.SERVER).buildAndSwitch()) {

            try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
                frame.addContext(EventContextKeys.TELEPORT_TYPE, TeleportTypes.PLUGIN);

                // We assume the mod teleporter code will call placeInExistingPortal as it is public and should be assumed anyone
                // would call this
                final BlockPattern.PortalInfo portalInfo =
                        this.placeInExistingPortal(VecHelper.toBlockPos(currentPosition), mEntity.getMotion(), mEntity.getTeleportDirection(),
                                mEntity.getLastPortalVec().x, mEntity.getLastPortalVec().y, mEntity instanceof PlayerEntity);

                if (portalInfo == null) {
                    return false;
                }

                final MoveEntityEvent.Teleport.Portal event = SpongeEventFactory.createMoveEntityEventTeleportPortal(frame.getCurrentCause(),
                        entity, currentPosition, VecHelper.toVector3d(portalInfo.pos), (ServerWorld) entity.getWorld(),
                        location.getWorld(), true, this);
                result = !SpongeCommon.postEvent(event);

                if (!result) {
                    return result;
                }

                if (!event.getKeepsVelocity()) {
                    mEntity.setMotion(0, 0, 0);
                }

                mEntity.posX = event.getToPosition().getX();
                mEntity.posY = event.getToPosition().getY();
                mEntity.posZ = event.getToPosition().getZ();

                result = this.shadow$placeInPortal(mEntity, mEntity.rotationYaw);

                if (!result) {
                    mEntity.posX = event.getFromPosition().getX();
                    mEntity.posY = event.getFromPosition().getY();
                    mEntity.posZ = event.getFromPosition().getZ();
                }

                return result;
            }
        }
    }
}
