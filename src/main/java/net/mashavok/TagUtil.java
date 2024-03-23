package net.mashavok;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public final class TagUtil {

    @Deprecated
    public static NbtCompound writePos(BlockPos pos) {
        NbtCompound tag = new NbtCompound();
        tag.putInt("x", pos.getX());
        tag.putInt("y", pos.getY());
        tag.putInt("z", pos.getZ());
        return tag;
    }

    @Deprecated
    @Nullable
    public static BlockPos readPos(NbtCompound tag) {
        if (tag.contains("x", NbtElement.NUMBER_TYPE) && tag.contains("y", NbtElement.NUMBER_TYPE) && tag.contains("z", NbtElement.NUMBER_TYPE)) {
            return new BlockPos(tag.getInt("x"), tag.getInt("y"), tag.getInt("z"));
        }
        return null;
    }

    @Nullable
    public static BlockPos readPos(NbtCompound parent, String key) {
        if (parent.contains(key, NbtElement.COMPOUND_TYPE)) {
            return readPos(parent.getCompound(key));
        }
        return null;
    }


    public static boolean isNumeric(NbtElement tag) {
        byte type = tag.getType();
        return type == NbtElement.BYTE_TYPE || type == NbtElement.SHORT_TYPE || type == NbtElement.INT_TYPE || type == NbtElement.LONG_TYPE || type == NbtElement.FLOAT_TYPE || type == NbtElement.DOUBLE_TYPE;
    }
}