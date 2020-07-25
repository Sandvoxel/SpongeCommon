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
package org.spongepowered.vanilla.mixin.core.entity;

import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.world.PlatformITeleporterBridge;
import org.spongepowered.common.entity.EntityUtil;

import javax.annotation.Nullable;

@Mixin(Entity.class)
public abstract class EntityMixin_Vanilla {

    @Shadow public boolean removed;

    @Shadow public abstract World shadow$getEntityWorld();
    @Shadow @Nullable public abstract MinecraftServer shadow$getServer();

    /**
     * @author Zidane
     * @reason Call to EntityUtil to handle dimension changes
     */
    @Nullable
    @Overwrite
    public Entity changeDimension(DimensionType destination) {
        if (this.shadow$getEntityWorld().isRemote || this.removed) {
            return null;
        }

        return EntityUtil.changeDimension((Entity) (Object) this, destination, (PlatformITeleporterBridge) this.shadow$getServer().getWorld(destination).getDefaultTeleporter());
    }
}
