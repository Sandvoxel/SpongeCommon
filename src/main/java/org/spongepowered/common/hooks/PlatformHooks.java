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
package org.spongepowered.common.hooks;

import com.google.common.base.Preconditions;

/**
 * Stores the various hook classes that are designed to be overwritten by a specific platform,
 * such as SpongeForge for Forge, that allows that platform to interject their specific logic for the
 * specific hook in question
 *
 * With that said, these should only be used if an appropriate bridge interface is simply not possible
 * as those would mesh, better, with our mixin strategy.
 *
 * Especially useful when we must overwrite Vanilla methods that contained code added by another platform that
 * we need to ensure we honor for compatibility purposes.
 *
 * It is by design and completely intended for ALL the hooks interfaces to be default implemented to either no-op
 * or return a value. The default values are to be considered Vanilla's hooks.
 *
 * Lastly, these should never be considered public API and we reserve the right to change the method signatures of the
 * hooks at any time for any reason we see fit (or outright remove a hook). We provide these as-is out of the spirit
 * of wanting to work, in tandem, with another platform.
 */
public final class PlatformHooks {

    private static final PlatformHooks instance = new PlatformHooks();
    private DimensionHooks dimensionHooks = new DimensionHooks() {};
    private EventHooks eventHooks = new EventHooks() {};
    private PacketHooks packetHooks = new PacketHooks() {};

    public static PlatformHooks getInstance() {
        return PlatformHooks.instance;
    }

    public DimensionHooks getDimensionHooks() {
        return this.dimensionHooks;
    }

    public void setDimensionHooks(final DimensionHooks dimensionHooks) {
        this.dimensionHooks = Preconditions.checkNotNull(dimensionHooks);
    }

    public EventHooks getEventHooks() {
        return this.eventHooks;
    }

    public void setEventHooks(final EventHooks eventHooks) {
        this.eventHooks = Preconditions.checkNotNull(eventHooks);
    }

    public PacketHooks getPacketHooks() {
        return this.packetHooks;
    }

    public void setEventHooks(final PacketHooks packetHooks) {
        this.packetHooks = Preconditions.checkNotNull(packetHooks);
    }
}
