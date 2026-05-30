package edu.montana.notch.neoforge;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Holds the mod's creative-mode-tab registry. Item lookups happen inside the supplier lambdas, so
 * they are deferred until the tab is built rather than run during class initialization.
 */
public final class NotchCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Notch.MODID);

    // Registry path names must be lowercase [a-z0-9_.-]; "Notch" here threw a ResourceLocationException.
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> NOTCH_TAB =
            CREATIVE_MODE_TABS.register("notch_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.notch")) // language key for the tab title
                    .withTabsBefore(CreativeModeTabs.COMBAT)
                    .icon(() -> NotchItems.WAND_ITEM.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(NotchItems.WAND_ITEM.get());
                        output.accept(NotchItems.PROGRAMMING_TABLE_ITEM.get());
                    }).build());

    private NotchCreativeModeTabs() {
    }

    public static void register(IEventBus modEventBus) {
        CREATIVE_MODE_TABS.register(modEventBus);
    }
}
