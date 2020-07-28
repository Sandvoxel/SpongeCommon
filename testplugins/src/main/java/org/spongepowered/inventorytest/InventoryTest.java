package org.spongepowered.inventorytest;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainComponentSerializer;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.item.inventory.ChangeInventoryEvent;
import org.spongepowered.api.event.item.inventory.EnchantItemEvent;
import org.spongepowered.api.event.item.inventory.TransferInventoryEvent;
import org.spongepowered.api.event.item.inventory.container.ClickContainerEvent;
import org.spongepowered.api.event.item.inventory.container.InteractContainerEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Container;
import org.spongepowered.api.item.inventory.ContainerTypes;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.entity.PrimaryPlayerInventory;
import org.spongepowered.api.item.inventory.menu.InventoryMenu;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.api.item.inventory.type.GridInventory;
import org.spongepowered.api.item.inventory.type.ViewableInventory;
import org.spongepowered.math.vector.Vector2i;
import org.spongepowered.plugin.jvm.Plugin;

import java.util.Optional;

@Plugin("inventorytest")
public class InventoryTest {


    @Listener
    public void onOpen(InteractContainerEvent.Open event) {
        final Optional<Component> component = event.getContainer().get(Keys.DISPLAY_NAME);
        final String title = component.map(c -> PlainComponentSerializer.plain().serialize(c)).orElse("No Title");
        System.out.println(event.getClass().getSimpleName() + " [" + title + "]");
    }

    @Listener
    public void onClickContainer(InteractContainerEvent event) {
        //        System.out.println(event);
        if (event instanceof EnchantItemEvent) {
            System.out.println(
                    event.getClass().getSimpleName() + " [" + ((EnchantItemEvent) event).getOption() + "] S:" + ((EnchantItemEvent) event).getSeed());
        }
        final Optional<Component> component = event.getContainer().get(Keys.DISPLAY_NAME);
        final String title = component.map(c -> PlainComponentSerializer.plain().serialize(c)).orElse("No Title");
        if (title.equals("Foobar")) {
            doFancyStuff(event.getCause().first(Player.class).get());
        }

    }

    @Listener
    public void onInteract(ChangeInventoryEvent event) {

        if (event instanceof ClickContainerEvent) {
            System.out.println(event.getClass().getSimpleName() + " " + ((ClickContainerEvent) event).getContainer().getClass().getSimpleName());
            final Transaction<ItemStackSnapshot> cursor = ((ClickContainerEvent) event).getCursorTransaction();
            System.out.println("Cursor: " + cursor.getOriginal().getType() + "x" + cursor.getOriginal().getQuantity() + "->" +
                    cursor.getFinal().getType() + "x" + cursor.getFinal().getQuantity()
            );
        } else {
            System.out.println(event.getClass().getSimpleName() + " " + event.getInventory().getClass().getSimpleName());
        }
        for (SlotTransaction slotTrans : event.getTransactions()) {
            System.out.println("SlotTr: " + slotTrans.getOriginal().getType() + "x" + slotTrans.getOriginal().getQuantity() + "->" +
                    slotTrans.getFinal().getType() + "x" + slotTrans.getFinal().getQuantity() + "[" + slotTrans.getSlot().get(Keys.SLOT_INDEX).get()
                    + "]");
        }
        //        System.out.println(event);
    }

    @Listener
    public void onTransfer(TransferInventoryEvent event) {
        if (event instanceof TransferInventoryEvent.Post) {
            System.out.println(
                    event.getClass().getSimpleName() + " " + event.getSourceInventory().getClass().getSimpleName() + "=>" + event.getTargetInventory()
                            .getClass().getSimpleName());
            final Integer sourceIdx = ((TransferInventoryEvent.Post) event).getSourceSlot().get(Keys.SLOT_INDEX).get();
            final Integer targetIdx = ((TransferInventoryEvent.Post) event).getTargetSlot().get(Keys.SLOT_INDEX).get();
            final ItemStackSnapshot item = ((TransferInventoryEvent.Post) event).getTransferredItem();
            System.out.println("[" + sourceIdx + "] -> [" + targetIdx + "] " + item.getType() + "x" + item.getQuantity());
        }

        //        System.out.println(event);
    }

    private void doFancyStuff(Player player) {

        final GridInventory inv27Grid = player.getInventory().query(PrimaryPlayerInventory.class).get().getStorage();
        final Inventory inv27Slots = Inventory.builder().slots(27).completeStructure().build();
        final Inventory inv27Slots2 = Inventory.builder().slots(27).completeStructure().build();
        final ViewableInventory doubleMyInventory = ViewableInventory.builder().type(ContainerTypes.GENERIC_9x6.get())
                .grid(inv27Slots.slots(), Vector2i.from(9, 3), Vector2i.from(0, 0))
                .grid(inv27Slots2.slots(), Vector2i.from(9, 3), Vector2i.from(0, 3))
                .completeStructure()
                .carrier(player)
                .build();
        final InventoryMenu menu = doubleMyInventory.asMenu();
        menu.setReadOnly(true);
        doubleMyInventory.set(0, ItemStack.of(ItemTypes.IRON_INGOT));
        doubleMyInventory.set(8, ItemStack.of(ItemTypes.GOLD_INGOT));
        doubleMyInventory.set(45, ItemStack.of(ItemTypes.EMERALD));
        doubleMyInventory.set(53, ItemStack.of(ItemTypes.DIAMOND));
        menu.registerSlotClick((cause, container, slot, slotIndex, clickType) -> {
            final ViewableInventory.Builder.DummyStep builder = ViewableInventory.builder().type(ContainerTypes.GENERIC_9x6.get()).fillDummy();
            ViewableInventory inventory = null;
            switch (slotIndex) {
                case 0:
                case 8:
                case 45:
                case 53:
                    inventory = builder.item(slot.peek().createSnapshot()).completeStructure().build();
            }
            if (inventory != null) {
                menu.setCurrentInventory(inventory);
            }
            return false;
        });
        final Optional<Container> open = menu.open((ServerPlayer) player);
    }


    //
    //    public static net.minecraft.inventory.container.Container doStuff(net.minecraft.inventory.container.Container mcContainer, PlayerEntity
    //    player) {
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
    //            inv4GridSlots.offer(ItemStack.of(ItemTypes.DIAMOND), ItemStack.of(ItemTypes.EMERALD), ItemStack.of(ItemTypes.IRON_INGOT),
    //            ItemStack.of(ItemTypes.GOLD_INGOT));
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
