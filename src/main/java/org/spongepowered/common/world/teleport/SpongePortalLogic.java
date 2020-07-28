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
package org.spongepowered.common.world.teleport;

import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.world.ServerLocation;
import org.spongepowered.api.world.teleport.PortalLogic;
import org.spongepowered.common.SpongeCatalogType;

import java.util.Optional;

public final class SpongePortalLogic extends SpongeCatalogType implements PortalLogic {

    public SpongePortalLogic(final ResourceKey key, final org.spongepowered.common.world.teleport.logic.PortalLogic logic) {
        super(key);
    }

    @Override
    public int getSearchRadius() {
        return 0;
    }

    @Override
    public PortalLogic setSearchRadius(int radius) {
        return null;
    }

    @Override
    public int getCreationRadius() {
        return 0;
    }

    @Override
    public PortalLogic setCreationRadius(int radius) {
        return null;
    }

    @Override
    public Optional<ServerLocation> findOrCreatePortal(ServerLocation targetLocation) {
        return Optional.empty();
    }

    @Override
    public Optional<ServerLocation> findPortal(ServerLocation targetLocation) {
        return Optional.empty();
    }

    @Override
    public Optional<ServerLocation> createPortal(ServerLocation targetLocation) {
        return Optional.empty();
    }
}
