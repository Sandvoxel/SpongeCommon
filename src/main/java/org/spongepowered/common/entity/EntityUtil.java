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
package org.spongepowered.common.entity;

import net.minecraft.block.pattern.BlockPattern;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SChangeGameStatePacket;
import net.minecraft.network.play.server.SRespawnPacket;
import net.minecraft.network.play.server.SServerDifficultyPacket;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.dimension.EndDimension;
import net.minecraft.world.dimension.NetherDimension;
import net.minecraft.world.dimension.OverworldDimension;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldInfo;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.util.Transform;
import org.spongepowered.api.world.ServerLocation;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.accessor.entity.LivingEntityAccessor;
import org.spongepowered.common.accessor.entity.player.ServerPlayerEntityAccessor;
import org.spongepowered.common.bridge.CreatorTrackedBridge;
import org.spongepowered.common.bridge.data.VanishableBridge;
import org.spongepowered.common.bridge.entity.ForgeEntityBridge;
import org.spongepowered.common.bridge.world.ForgeITeleporterBridge;
import org.spongepowered.common.bridge.world.dimension.DimensionBridge;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.item.util.ItemStackUtil;
import org.spongepowered.math.vector.Vector3d;

import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

public final class EntityUtil {

    public static final Function<PhaseContext<?>, Supplier<Optional<User>>> ENTITY_CREATOR_FUNCTION = (context) ->
        () -> Stream.<Supplier<Optional<User>>>builder()
            .add(() -> context.getSource(User.class))
            .add(context::getNotifier)
            .add(context::getCreator)
            .build()
            .map(Supplier::get)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .findFirst();

    private EntityUtil() {
    }

    public static Entity changeDimension(final Entity entity, final DimensionType dimensionType, final ForgeITeleporterBridge teleporter) {
        final ServerWorld fromWorld = (ServerWorld) entity.world;
        fromWorld.getProfiler().startSection("changeDimension");
        final DimensionType fromDimensionType = fromWorld.dimension.getType();
        ServerWorld toWorld = entity.getEntityWorld().getServer().getWorld(dimensionType);
        if (toWorld == null) {
            fromWorld.getProfiler().endSection();
            return entity;
        }

        entity.dimension = dimensionType;
        entity.detach();

        entity.world.getProfiler().startSection("reposition");

        final Entity teleportedEntity = teleporter.bridge$placeEntity(entity, fromWorld, toWorld, entity.rotationYaw, spawnInPortal -> {
            Vec3d vec3d = entity.getMotion();
            float f = 0.0F;
            BlockPos blockpos;
            if (fromWorld.dimension instanceof EndDimension && toWorld.dimension instanceof OverworldDimension) {
                blockpos = toWorld.getHeight(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, toWorld.getSpawnPoint());
            } else if (toWorld.dimension instanceof EndDimension) {
                blockpos = toWorld.getSpawnCoordinate();
            } else {
                final double movementFactor = ((DimensionBridge) fromWorld.dimension).bridge$getMovementFactor() / ((DimensionBridge) toWorld).bridge$getMovementFactor();
                double d0 = entity.posX * movementFactor;
                double d1 = entity.posZ * movementFactor;

                if (fromWorld.dimension instanceof OverworldDimension && toWorld.dimension instanceof NetherDimension) {
                    d0 /= 8.0D;
                    d1 /= 8.0D;
                } else if (fromWorld.dimension instanceof NetherDimension && toWorld.dimension instanceof OverworldDimension) {
                    d0 *= 8.0D;
                    d1 *= 8.0D;
                }

                double d3 = Math.min(-2.9999872E7D, toWorld.getWorldBorder().minX() + 16.0D);
                double d4 = Math.min(-2.9999872E7D, toWorld.getWorldBorder().minZ() + 16.0D);
                double d5 = Math.min(2.9999872E7D, toWorld.getWorldBorder().maxX() - 16.0D);
                double d6 = Math.min(2.9999872E7D, toWorld.getWorldBorder().maxZ() - 16.0D);
                d0 = MathHelper.clamp(d0, d3, d5);
                d1 = MathHelper.clamp(d1, d4, d6);
                Vec3d vec3d1 = entity.getLastPortalVec();
                blockpos = new BlockPos(d0, entity.posY, d1);

                if (spawnInPortal) {
                    BlockPattern.PortalInfo blockpattern$portalinfo = toWorld.getDefaultTeleporter()
                            .placeInExistingPortal(blockpos, vec3d, entity.getTeleportDirection(), vec3d1.x, vec3d1.y, entity instanceof PlayerEntity);
                    if (blockpattern$portalinfo == null) {
                        return null;
                    }

                    blockpos = new BlockPos(blockpattern$portalinfo.pos);
                    vec3d = blockpattern$portalinfo.motion;
                    f = (float) blockpattern$portalinfo.rotation;
                }
            }

            fromWorld.getProfiler().endStartSection("reloading");
            final Entity newEntity = entity.getType().create(toWorld);
            if (newEntity != null) {
                newEntity.copyDataFromOld(entity);
                newEntity.moveToBlockPosAndAngles(blockpos, entity.rotationYaw + f, entity.rotationPitch);
                newEntity.setMotion(vec3d);
                toWorld.func_217460_e(newEntity);
            }
            return newEntity;
        });

        // Teleportation failed, bail and let it live to try another day
        if (teleportedEntity == null) {
            return null;
        }

        ((ForgeEntityBridge) entity).bridge$remove(false);
        fromWorld.getProfiler().endSection();
        fromWorld.resetUpdateEntityTick();
        toWorld.resetUpdateEntityTick();
        fromWorld.getProfiler().endSection();

        return teleportedEntity;
    }

    public static ServerPlayerEntity changeDimension(final ServerPlayerEntity player, final DimensionType dimensionType,
            final ForgeITeleporterBridge teleporter) {

        ((ServerPlayerEntityAccessor) player).accessor$setInvulnerableDimensionChange(true);
        final ServerWorld fromWorld = (ServerWorld) player.world;
        final DimensionType fromDimensionType = fromWorld.dimension.getType();
        ServerWorld toWorld = player.getServer().getWorld(dimensionType);

        // You're only getting the credits if you beat Vanilla's THE_END..
        if (fromDimensionType == DimensionType.THE_END && toWorld.dimension instanceof OverworldDimension && teleporter.bridge$isVanilla()) {
            player.detach();
            player.getServerWorld().removePlayer(player);
            if (!player.queuedEndExit) {
                player.queuedEndExit = true;
                player.connection.sendPacket(new SChangeGameStatePacket(4, ((ServerPlayerEntityAccessor) player).accessor$getSeenCredits() ? 0.0F : 1.0F));
                ((ServerPlayerEntityAccessor) player).accessor$setSeenCredits(true);
            }

            return player;
        }

        player.dimension = dimensionType;
        WorldInfo worldinfo = toWorld.getWorldInfo();
        // TODO Minecraft 1.14 - Forge needs their dimension data send down at this spot
        player.connection.sendPacket(new SRespawnPacket(dimensionType, worldinfo.getGenerator(), player.interactionManager.getGameType()));
        player.connection.sendPacket(new SServerDifficultyPacket(worldinfo.getDifficulty(), worldinfo.isDifficultyLocked()));
        PlayerList playerlist = player.getServer().getPlayerList();
        playerlist.updatePermissionLevel(player);
        fromWorld.removePlayer(player);
        // invalidate call.
        ((ForgeEntityBridge) player).revive();

        return player;
    }

    public static boolean isEntityDead(final net.minecraft.entity.Entity entity) {
        if (entity instanceof LivingEntity) {
            final LivingEntity base = (LivingEntity) entity;
            return base.getHealth() <= 0 || base.deathTime > 0 || ((LivingEntityAccessor) entity).accessor$getDead();
        }
        return entity.removed;
    }

    public static MoveEntityEvent.Teleport postDisplaceEntityTeleportEvent(final Entity entityIn, final ServerLocation location) {
        final Transform fromTransform = ((org.spongepowered.api.entity.Entity) entityIn).getTransform();
        final Transform toTransform = fromTransform.withPosition(location.getPosition()).withRotation(new Vector3d(entityIn.rotationPitch, entityIn.rotationYaw, 0));

        return postDisplaceEntityTeleportEvent(entityIn, fromTransform, toTransform, (org.spongepowered.api.world.server.ServerWorld) entityIn.world, location.getWorld());
    }

    public static MoveEntityEvent.Teleport postDisplaceEntityTeleportEvent(final Entity entityIn, final double posX, final double posY, final double posZ, final float yaw, final float pitch) {
        final org.spongepowered.api.world.server.ServerWorld world = (org.spongepowered.api.world.server.ServerWorld) entityIn.world;
        final Transform fromTransform = ((org.spongepowered.api.entity.Entity) entityIn).getTransform();
        final Transform toTransform = fromTransform.withPosition(new Vector3d(posX, posY, posZ)).withRotation(new Vector3d(pitch, yaw, 0));
        return postDisplaceEntityTeleportEvent(entityIn, fromTransform, toTransform, world, world);
    }

    public static MoveEntityEvent.Teleport postDisplaceEntityTeleportEvent(
        final Entity entityIn, final Transform fromTransform, final Transform toTransform, org.spongepowered.api.world.server.ServerWorld fromWorld, org.spongepowered.api.world.server.ServerWorld toWorld) {

        // Use origin world to get correct cause
        final CauseStackManager causeStackManager = PhaseTracker.getCauseStackManager();
        try (final CauseStackManager.StackFrame frame = causeStackManager.pushCauseFrame()) {
            frame.pushCause(entityIn);

            final MoveEntityEvent.Teleport event = SpongeEventFactory.createMoveEntityEventTeleport(
                causeStackManager.getCurrentCause(),
                fromTransform.getPosition(), toTransform.getPosition(), fromWorld, toWorld, (org.spongepowered.api.entity.Entity) entityIn, false);
            SpongeCommon.postEvent(event);
            return event;
        }
    }

    public static boolean processEntitySpawnsFromEvent(final SpawnEntityEvent event, final Supplier<Optional<User>> entityCreatorSupplier) {
        boolean spawnedAny = false;
        for (final org.spongepowered.api.entity.Entity entity : event.getEntities()) {
            // Here is where we need to handle the custom items potentially having custom entities
            spawnedAny = processEntitySpawn(entity, entityCreatorSupplier);
        }
        return spawnedAny;
    }

    public static boolean processEntitySpawnsFromEvent(final PhaseContext<?> context, final SpawnEntityEvent destruct) {
        return processEntitySpawnsFromEvent(destruct, ENTITY_CREATOR_FUNCTION.apply(context));
    }

    @SuppressWarnings("ConstantConditions")
    public static boolean processEntitySpawn(final org.spongepowered.api.entity.Entity entity, final Supplier<Optional<User>> supplier) {
        final Entity minecraftEntity = (Entity) entity;
        if (minecraftEntity instanceof ItemEntity) {
            final ItemStack item = ((ItemEntity) minecraftEntity).getItem();
            if (!item.isEmpty()) {
                final Optional<Entity> customEntityItem = Optional.ofNullable(SpongeImplHooks.getCustomEntityIfItem(minecraftEntity));
                if (customEntityItem.isPresent()) {
                    // Bypass spawning the entity item, since it is established that the custom entity is spawned.
                    final Entity entityToSpawn = customEntityItem.get();
                    supplier.get()
                        .ifPresent(spawned -> {
                            if (entityToSpawn instanceof CreatorTrackedBridge) {
                                ((CreatorTrackedBridge) entityToSpawn).tracked$setCreatorReference(spawned);
                            }
                        });
                    if (entityToSpawn.removed) {
                        entityToSpawn.removed = false;
                    }
                    // Since forge already has a new event thrown for the entity, we don't need to throw
                    // the event anymore as sponge plugins getting the event after forge mods will
                    // have the modified entity list for entities, so no need to re-capture the entities.
                    entityToSpawn.world.addEntity(entityToSpawn);
                    return true;
                }
            }
        }

        supplier.get()
            .ifPresent(spawned -> {
                if (entity instanceof CreatorTrackedBridge) {
                    ((CreatorTrackedBridge) entity).tracked$setCreatorReference(spawned);
                }
            });
        // Allowed to call force spawn directly since we've applied creator and custom item logic already
        ((net.minecraft.world.World) entity.getWorld()).addEntity((Entity) entity);
        return true;
    }


    private static Vec3d getPositionEyes(final Entity entity, final float partialTicks)
    {
        if (partialTicks == 1.0F)
        {
            return new Vec3d(entity.posX, entity.posY + entity.getEyeHeight(), entity.posZ);
        }

        final double interpX = entity.prevPosX + (entity.posX - entity.prevPosX) * partialTicks;
        final double interpY = entity.prevPosY + (entity.posY - entity.prevPosY) * partialTicks + entity.getEyeHeight();
        final double interpZ = entity.prevPosZ + (entity.posZ - entity.prevPosZ) * partialTicks;
        return new Vec3d(interpX, interpY, interpZ);
    }

    /**
     * A simple redirected static util method for {@link Entity#entityDropItem(ItemStack, float)}.
     * What this does is ensures that any possibly required wrapping of captured drops is performed.
     * Likewise, it ensures that the phase state is set up appropriately.
     *
     * @param entity The entity dropping the item
     * @param itemStack The itemstack to spawn
     * @param offsetY The offset y coordinate
     * @return The item entity
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Nullable
    public static ItemEntity entityOnDropItem(final Entity entity, final ItemStack itemStack, final float offsetY, final double xPos, final double zPos) {
        if (itemStack.isEmpty()) {
            // Sanity check, just like vanilla
            return null;
        }
        // Now the real fun begins.
        final ItemStack item;
        final double posX = xPos;
        final double posY = entity.posY + offsetY;
        final double posZ = zPos;

        // FIRST we want to throw the DropItemEvent.PRE
        final ItemStackSnapshot snapshot = ItemStackUtil.snapshotOf(itemStack);
        final List<ItemStackSnapshot> original = new ArrayList<>();
        original.add(snapshot);

        // Gather phase states to determine whether we're merging or capturing later
        final PhaseContext<?> phaseContext = PhaseTracker.getInstance().getPhaseContext();
        final IPhaseState<?> currentState = phaseContext.state;

        // We want to frame ourselves here, because of the two events we have to throw, first for the drop item event, then the constructentityevent.
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            // Perform the event throws first, if they return false, return null
            item = SpongeCommonEventFactory.throwDropItemAndConstructEvent(entity, posX, posY, posZ, snapshot, original, frame);

            if (item == null || item.isEmpty()) {
                return null;
            }


            // This is where we could perform item pre merging, and cancel before we create a new entity.
            // For now, we aren't performing pre merging.

            final ItemEntity entityitem = new ItemEntity(entity.world, posX, posY, posZ, item);
            entityitem.setDefaultPickupDelay();

            // FIFTH - Capture the entity maybe?
            if (((IPhaseState) currentState).spawnItemOrCapture(phaseContext, entity, entityitem)) {
                return entityitem;
            }
            // FINALLY - Spawn the entity in the world if all else didn't fail
            EntityUtil.processEntitySpawn((org.spongepowered.api.entity.Entity) entityitem, Optional::empty);
            return entityitem;
        }
    }


    /**
     * This is used to create the "dropping" motion for items caused by players. This
     * specifically was being used (and should be the correct math) to drop from the
     * player, when we do item stack captures preventing entity items being created.
     *
     * @param dropAround True if it's being "dropped around the player like dying"
     * @param player The player to drop around from
     * @param random The random instance
     * @return The motion vector
     */
    @SuppressWarnings("unused")
    private static Vector3d createDropMotion(final boolean dropAround, final PlayerEntity player, final Random random) {
        double x;
        double y;
        double z;
        if (dropAround) {
            final float f = random.nextFloat() * 0.5F;
            final float f1 = random.nextFloat() * ((float) Math.PI * 2F);
            x = -MathHelper.sin(f1) * f;
            z = MathHelper.cos(f1) * f;
            y = 0.20000000298023224D;
        } else {
            float f2 = 0.3F;
            x = -MathHelper.sin(player.rotationYaw * 0.017453292F) * MathHelper.cos(player.rotationPitch * 0.017453292F) * f2;
            z = MathHelper.cos(player.rotationYaw * 0.017453292F) * MathHelper.cos(player.rotationPitch * 0.017453292F) * f2;
            y = - MathHelper.sin(player.rotationPitch * 0.017453292F) * f2 + 0.1F;
            final float f3 = random.nextFloat() * ((float) Math.PI * 2F);
            f2 = 0.02F * random.nextFloat();
            x += Math.cos(f3) * f2;
            y += (random.nextFloat() - random.nextFloat()) * 0.1F;
            z += Math.sin(f3) * f2;
        }
        return new Vector3d(x, y, z);
    }


    public static boolean isUntargetable(Entity from, Entity target) {
        if (((VanishableBridge) target).bridge$isVanished() && ((VanishableBridge) target).bridge$isUntargetable()) {
            return true;
        }
        // Temporary fix for https://bugs.mojang.com/browse/MC-149563
        if (from.world != target.world) {
            return true;
        }
        return false;
    }

}
