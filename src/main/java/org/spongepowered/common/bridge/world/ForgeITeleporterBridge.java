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
package org.spongepowered.common.bridge.world;

import net.minecraft.entity.Entity;
import net.minecraft.world.Teleporter;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import java.util.function.Function;

/**
 * Compatibility interface to handle Forge binary patching {@link Teleporter} to implement their ITeleporter
 */
public interface ForgeITeleporterBridge {

    ForgeITeleporterBridge NO_PORTAL = new ForgeITeleporterBridge() {
        @Override
        public Entity bridge$placeEntity(Entity entity, ServerWorld currentWorld, ServerWorld destWorld, float yaw,
                Function<Boolean, Entity> repositionEntity) {
            return repositionEntity.apply(false);
        }
    };

    default Entity bridge$placeEntity(Entity entity, ServerWorld currentWorld, ServerWorld destWorld, float yaw,
            Function<Boolean, Entity> repositionEntity) {
        return repositionEntity.apply(true);
    }

    default boolean bridge$isVanilla() {
        return this.getClass().equals(Teleporter.class);
    }
}
