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
package org.spongepowered.common.mixin.inventory.api.carrier;

import net.minecraft.inventory.DoubleSidedInventory;
import net.minecraft.inventory.IInventory;
import org.spongepowered.api.block.entity.BlockEntity;
import org.spongepowered.api.item.inventory.Carrier;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.MultiBlockCarrier;
import org.spongepowered.api.item.inventory.type.CarriedInventory;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.inventory.DefaultSingleBlockCarrier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Mixin(DoubleSidedInventory.class)
public abstract class DoubleSidedInventoryMixin_API implements MultiBlockCarrier {

    @Shadow @Final private IInventory field_70477_b;
    @Shadow @Final private IInventory field_70478_c;

    @Override
    public List<Location> getLocations() {
        final List<Location> list = new ArrayList<>();
        if (this.field_70477_b instanceof BlockEntity) {
            list.add(((BlockEntity) this.field_70477_b).getLocation());
        }
        if (this.field_70478_c instanceof BlockEntity) {
            list.add(((BlockEntity) this.field_70478_c).getLocation());
        }
        return Collections.unmodifiableList(list);
    }

    @Override
    public CarriedInventory<? extends Carrier> getInventory() {
        return (CarriedInventory<? extends Carrier>) this;
    }

    @Override
    public Location getLocation() {
        return this.getLocations().get(0);
    }

    @Override
    public Optional<Inventory> getInventory(final Location at) {
        if (this.getLocations().contains(at)) {
            return Optional.of(this.getInventory());
        }
        return Optional.empty();
    }

    @Override
    public Optional<Inventory> getInventory(final Location at, final Direction from) {
        return this.getInventory(at);
    }

    @Override
    public Inventory getInventory(final Direction from) {
        return DefaultSingleBlockCarrier.getInventory(from, this);
    }
}