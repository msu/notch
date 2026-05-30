package edu.montana.notch.neoforge;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;

/**
 * Holds the mod's item registry. Depends on {@link NotchBlocks} (for the block item) but never on
 * {@link Notch}, so the dependency graph stays acyclic: Blocks -> Items -> CreativeModeTabs.
 */
public final class NotchItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Notch.MODID);

    public static final DeferredItem<@NotNull Item> WAND_ITEM = ITEMS.registerSimpleItem("wand", p ->
            p
                    .stacksTo(1)
                    .component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true)
    );

    public static final DeferredItem<BlockItem> PROGRAMMING_TABLE_ITEM =
            ITEMS.registerSimpleBlockItem("programming_table", NotchBlocks.PROGRAMMING_TABLE);

    private NotchItems() {
    }

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
