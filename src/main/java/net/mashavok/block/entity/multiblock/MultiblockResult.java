package net.mashavok.block.entity.multiblock;

import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import org.jetbrains.annotations.Nullable;

public final class MultiblockResult {
    public static final MultiblockResult SUCCESS = new MultiblockResult(true, null, Text.empty());
    public final boolean success;
    @Nullable
    public final BlockPos pos;
    public final Text message;

    public MultiblockResult(boolean success, @Nullable BlockPos pos, Text message) {
        this.success = success;
        this.pos = pos;
        this.message = message;
    }

    public static MultiblockResult error(@Nullable BlockPos pos, Text error) {
        return new MultiblockResult(false, pos, error);
    }
    public static MultiblockResult error(@Nullable BlockPos pos, String key, Object... params) {
        return error(pos,Text.translatable(key, params));
    }
}