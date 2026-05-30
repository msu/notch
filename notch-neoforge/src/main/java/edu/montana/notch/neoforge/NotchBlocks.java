package edu.montana.notch.neoforge;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Holds the mod's block registry. This class owns its own {@link DeferredRegister}, so it has no
 * static dependency back on {@link Notch} — that is what keeps registration free of the circular
 * class-initialization cycle.
 */
public final class NotchBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Notch.MODID);

    // A workstation built from an iron-block body with a crafting surface inset into the top.
    public static final DeferredBlock<Block> PROGRAMMING_TABLE =
            BLOCKS.registerSimpleBlock("programming_table", p -> p
                    .mapColor(MapColor.METAL)
                    .sound(SoundType.METAL)
                    .strength(5.0F, 6.0F)
                    .requiresCorrectToolForDrops());

    private NotchBlocks() {
    }

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
    }
}
