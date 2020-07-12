package org.spongepowered.inventorytest;

import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.item.inventory.ChangeInventoryEvent;
import org.spongepowered.api.event.item.inventory.container.InteractContainerEvent;
import org.spongepowered.plugin.jvm.Plugin;

@Plugin("inventorytest")
public class InventoryTest {

    @Listener
    public void onClickContainer(InteractContainerEvent event) {
        System.out.println(event);
    }

    @Listener
    public void onInteract(ChangeInventoryEvent event) {
        System.out.println(event);
    }
//
//    public static net.minecraft.inventory.container.Container doStuff(net.minecraft.inventory.container.Container mcContainer, PlayerEntity player) {
//        Container container = ((Container) mcContainer);
//        InventoryAdapter adapter = (InventoryAdapter) container;
//
//        if (container instanceof ChestContainer) {
//            int i = 1;
//            ItemStack stick = ItemStack.of(ItemTypes.STICK);
//            for (org.spongepowered.api.item.inventory.Slot slot : container.slots()) {
//                stick.setQuantity(i++);
//                slot.set(stick.copy());
//            }
//            stick.setQuantity(1);
//            Inventory queriedGrid = container.query(PrimaryPlayerInventory.class).get().asGrid().query(QueryTypes.GRID, Vector2i.from(1, 1),
//                    Vector2i.from(2, 2));
//            queriedGrid.slots().forEach(slot -> {
//                slot.set(stick.copy());
//            });
//            Inventory grids = container.query(QueryTypes.INVENTORY_TYPE, GridInventory.class);
//            container.query(Hotbar.class).get().set(0, ItemStack.of(ItemTypes.CHEST));
//
//            Inventory inv5slots = Inventory.builder().slots(5).completeStructure().build();
//            Inventory inv4GridSlots = Inventory.builder().grid(2, 2).completeStructure().build();
//            inv4GridSlots.offer(ItemStack.of(ItemTypes.DIAMOND), ItemStack.of(ItemTypes.EMERALD), ItemStack.of(ItemTypes.IRON_INGOT), ItemStack.of(ItemTypes.GOLD_INGOT));
//            Inventory inv10Composite = Inventory.builder()
//                    .inventory(inv5slots)
//                    .inventory(inv4GridSlots)
//                    .slots(1)
//                    .completeStructure().build();
//            Inventory inv4GridAgain = inv10Composite.query(GridInventory.class).get();
//
//
//            Optional<ItemStack> itemStack = inv10Composite.peekAt(5);
//            inv4GridAgain.peekAt(0);
//            inv4GridAgain.slots().forEach(slot -> System.out.println(slot.peek()));
//
//            Inventory mixedComposite = Inventory.builder().inventory(grids).slots(1).inventory(container).completeStructure().build();
//        }
//        if (container instanceof DispenserContainer) {
//            final GridInventory inv27Grid = ((PlayerInventory)player.inventory).query(PrimaryPlayerInventory.class).get().getStorage();
//            final Inventory inv27Slots = Inventory.builder().slots(27).completeStructure().build();
//            final Inventory inv27Slots2 = Inventory.builder().slots(27).completeStructure().build();
//            final ViewableInventory doubleMyInventory = ViewableInventory.builder().type(ContainerTypes.GENERIC_9x6.get())
//                    .grid(inv27Slots.slots(), Vector2i.from(9, 3), Vector2i.from(0, 0))
//                    .grid(inv27Slots2.slots(), Vector2i.from(9, 3), Vector2i.from(0, 3))
//                    .completeStructure()
//                    .carrier((Carrier)player)
//                    .build();
//            final Optional<Container> open = doubleMyInventory.asMenu().open((ServerPlayer) player);
//            doubleMyInventory.offer(ItemStack.of(ItemTypes.GOLD_INGOT));
//            doubleMyInventory.offer(ItemStack.of(ItemTypes.IRON_INGOT));
//            return null;
//        }
//
//        return mcContainer;
//
//    }
}
