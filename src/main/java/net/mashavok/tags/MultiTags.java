package net.mashavok.tags;


import net.minecraft.block.Block;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public class MultiTags {
    public static void init() {
        Blocks.init();
        Fluids.init();
    }
    public static class Blocks {
        private static void init() {}
        public static final TagKey<Block> STONE = TagKey.of(RegistryKeys.BLOCK, new Identifier("minecraft", "stone"));
        public static final TagKey<Block> COBBLESTONE = TagKey.of(RegistryKeys.BLOCK, new Identifier("minecraft", "cobblestone"));
    }
    public static class Fluids {
        private static void init() {}
    }
}
