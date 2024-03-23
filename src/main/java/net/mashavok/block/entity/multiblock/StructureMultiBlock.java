package net.mashavok.block.entity.multiblock;

import com.google.common.collect.ImmutableList;
import net.mashavok.block.entity.MultiBlockEntity;
import net.mashavok.multiblock.IMasterLogic;
import net.mashavok.multiblock.IServentLogic;
import net.mashavok.tags.TagUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public abstract class StructureMultiBlock<T extends MultiBlockEntity & IMasterLogic> extends MultiBlockCuboid<StructureMultiBlock.StructureData> {
    private static final String TAG_INSIDE_CHECK = "insideCheck";
    protected final T parent;

    public StructureMultiBlock(T parent, boolean hasFloor, boolean hasFrame, boolean hasCeiling, int maxHeight, int innerLimit) {
        super(hasFloor, hasFrame, hasCeiling, maxHeight, innerLimit);
        this.parent = parent;
    }
    @Override
    public StructureData create(BlockPos min, BlockPos max, Set<BlockPos> extraPos) {
        return new StructureData(min, max, extraPos, hasFloor, hasFrame, hasCeiling);
    }
    public StructureData createClient(BlockPos min, BlockPos max) {
        return new StructureData(min, max, Collections.emptySet(), hasFloor, hasFrame, hasCeiling);
    }
    @Override
    public StructureData detectMultiblock(World world, BlockPos master, Direction facing) {
        return super.detectMultiblock(world, master, facing);
    }
    protected boolean isValidServent(World world, BlockPos pos) {
        BlockEntity te = world.getBlockEntity(pos);

        if (te instanceof IServentLogic) {
            return ((IServentLogic) te).isValidMaster(parent);
        }
        return true;
    }
    public boolean canExpand(StructureData data, World world) {
        BlockPos min = data.getMinPos();
        BlockPos max = data.getMaxPos();
        BlockPos from, to;
        if (hasFloor) {
            to = max.up();
            from = new BlockPos(min.getX(), to.getY(), min.getZ());
        } else {
            from = min.down();
            to = new BlockPos(max.getX(), from.getY(), max.getZ());
        }
        MultiblockResult result = detectLayer(world, from, to, pos -> {
        });
        lastResult(result);
        return result.success;
    }
    private void lastResult(MultiblockResult result) {

    }
    protected abstract boolean isValidBlock(Block block);
    protected abstract boolean isValidFloor(Block block);
    protected abstract boolean isValidWall(Block block);

    @Override
    protected boolean isValidBlock(World world, BlockPos pos, CuboidSide side, boolean isFrame) {
        if (pos.equals(parent.getMasterBlock())) {
            return true;
        }
        if (!isValidServent(world, pos)) {
            return false;
        }
        BlockState state = world.getBlockState(pos);
        if (side == CuboidSide.FLOOR && !isFrame) {
            return isValidFloor(state.getBlock());
        }
        return isValidWall(state.getBlock());
    }
    @Override
    public boolean shouldUpdate(World world, MultiblockStructureData structure, BlockPos pos, BlockState state) {
        if (structure.withinBounds(pos)) {
            if (structure.contains(pos)) {
                return !isValidBlock(state.getBlock());
            }
            return structure.isInside(pos) && !state.isAir();
        }
        return structure.isDirectlyAbove(pos) && isValidWall(state.getBlock());
    }
    public static class StructureData extends MultiblockStructureData {
        private BlockPos insideCheck;
        protected StructureData(BlockPos minPos, BlockPos maxPos, Set<BlockPos> extraPositions, boolean hasFloor, boolean hasFrame, boolean hasCeiling) {
            super(minPos, maxPos, extraPositions, hasFloor, hasFrame, hasCeiling);
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
        public NbtCompound writeToTag() {
            NbtCompound nbt = super.writeToTag;
            if (insideCheck != null) {
                nbt.put(TAG_INSIDE_CHECK, TagUtil.writePos(insideCheck));
            }
            return nbt;
        }
    }
}
