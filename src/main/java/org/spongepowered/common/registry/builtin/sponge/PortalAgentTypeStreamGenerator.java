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
package org.spongepowered.common.registry.builtin.sponge;

import net.minecraft.world.Teleporter;
import net.minecraft.world.dimension.EndDimension;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.world.teleport.PortalAgentType;
import org.spongepowered.common.bridge.world.PlatformITeleporterBridge;
import org.spongepowered.common.world.SpongePortalAgentType;

import java.util.stream.Stream;

public final class PortalAgentTypeStreamGenerator {

    private static final PortalAgentType THE_END = new SpongePortalAgentType(ResourceKey.minecraft("default_the_end"), (Class<PlatformITeleporterBridge>) (Object) Teleporter.class);
    private static final PortalAgentType THE_NETHER = new SpongePortalAgentType(ResourceKey.minecraft("default_the_nether"), (Class<PlatformITeleporterBridge>) (Object) Teleporter.class);

    private PortalAgentTypeStreamGenerator() {
    }

    // TODO Minecraft 1.14 - Stop gap to get Vanilla portals working, does need a lot of thought...
    public static PortalAgentType find(final ServerWorld world) {
        if (world.dimension instanceof EndDimension) {
            return PortalAgentTypeStreamGenerator.THE_END;
        }

        return PortalAgentTypeStreamGenerator.THE_NETHER;
    }

    public static Stream<PortalAgentType> stream() {
        return Stream.of(
                PortalAgentTypeStreamGenerator.THE_END,
                PortalAgentTypeStreamGenerator.THE_NETHER
        );
    }
}
