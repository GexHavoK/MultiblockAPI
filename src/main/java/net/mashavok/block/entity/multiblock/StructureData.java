package net.mashavok.block.entity.multiblock;

import net.mashavok.tags.TagUtil;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.Set;


public class StructureData extends MultiblockStructureData {

    private static final String TAG_INSIDE_CHECK = "insideCheck";
    private BlockPos insideCheck;

    public StructureData(BlockPos minPos, BlockPos maxPos, Set<BlockPos> extraPositions, boolean hasFloor, boolean hasFrame, boolean hasCeiling, BlockPos minInside, BlockPos maxInside) {
        super(minPos, maxPos, extraPositions, hasFloor, hasFrame, hasCeiling, minInside, maxInside);
    }

    public StructureData(BlockPos min, BlockPos max, Set<BlockPos> extraPos, boolean hasFloor, boolean hasFrame, boolean hasCeiling) {
        super(min, max, extraPos, hasFloor, hasFrame, hasCeiling);
    }

    private BlockPos getNextInsideCheck(@Nullable BlockPos prev) {
        BlockPos min = getMinInside();
        if (prev == null) {
            return min;
        }
        if (prev.getX() < min.getX() || prev.getY() < min.getY() || prev.getZ() < min.getZ()) {
            return min;
        }
        BlockPos max = getMaxInside();
        if (prev.getZ() >= max.getZ()) {
            if (prev.getX() >= max.getX()) {
                if (prev.getY() >= max.getY()) {
                    return min;
                } else {
                    return new BlockPos(min.getX(), prev.getY() + 1, min.getZ());
                }
            } else {
                return new BlockPos(prev.getX() + 1, prev.getY(), min.getZ());
            }
        } else {
            return prev.add(0, 0, 1);
        }
    }
    private BlockPos getMinInside() {
        return getMinInside();
    }
    public BlockPos getNextInsideCheck() {
        insideCheck = getNextInsideCheck(insideCheck);
        return insideCheck;
    }
    public int getPerimeterCount() {
        BlockPos min = getMinInside();
        BlockPos max = getMaxInside();
        int dx = max.getX() - min.getX();
        int dy = max.getY() - min.getY();
        int dz = max.getZ() - min.getZ();
        return (2 * (dx * dy) + 2 * (dy * dz) + (dx * dz));
    }
    private BlockPos getMaxInside() {
        return getMaxInside();
    }
    @Override
    public NbtCompound writeClientTag() {
        NbtCompound nbt = super.writeClientTag();
        return nbt;
    }
    @Override
    public NbtCompound writeTo() {
        NbtCompound nbt = super.writeToTag();
        if (insideCheck != null) {
            nbt.put(TAG_INSIDE_CHECK, TagUtil.writePos(insideCheck));
        }
        return nbt;
    }
    public BlockPos getMinPos() {
        return getMinPos();
    }
    public BlockPos getMaxPos() {
        return getMaxPos();
    }
}

