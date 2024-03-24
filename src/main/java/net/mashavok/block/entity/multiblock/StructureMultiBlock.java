package net.mashavok.block.entity.multiblock;

import net.mashavok.block.entity.MultiBlockEntity;
import net.mashavok.multiblock.IMasterLogic;
import net.mashavok.multiblock.IServentLogic;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;


public abstract class StructureMultiBlock<T extends MultiBlockEntity & IMasterLogic> extends MultiBlockCuboid<StructureData> {
    private static final String TAG_TANKS = "tanks";

    /**
     * Parent structure instance
     */
    protected final T parent;
    /**
     * List to check if a tank is found between valid block checks
     */
    protected final List<BlockPos> tanks = new ArrayList<>();

    public StructureMultiBlock(T parent, boolean hasFloor, boolean hasFrame, boolean hasCeiling, int maxHeight, int innerLimit) {
        super(hasFloor, hasFrame, hasCeiling, maxHeight, innerLimit);
        this.parent = parent;
    }

    @Override
    public StructureData create(BlockPos min, BlockPos max, Set<BlockPos> extraPos) {
        return new StructureData(min, max, extraPos, hasFloor, hasFrame, hasCeiling);
    }
    public StructureData createClient(BlockPos min, BlockPos max, List<BlockPos> tanks) {
        return new StructureData(min, max, Collections.emptySet(), hasFloor, hasFrame, hasCeiling);
    }

    @Override
    public StructureData detectMultiblock(World world, BlockPos master, Direction facing) {
        // clear tanks from last check before calling
        tanks.clear();
        return super.detectMultiblock(world, master, facing);
    }

    /**
     * Reads the structure data from Tag
     *
     * @param nbt Tag tag
     * @return Structure data, or null if invalid
     */
    @Override
    public StructureData readFromTag(NbtCompound nbt) {
        // add all tanks from Tag, will be picked up in the create call
        tanks.clear();
        tanks.addAll(readPosList(nbt, TAG_TANKS));
        return super.readFromTag(nbt);
    }
    protected boolean isValidSlave(World world, BlockPos pos) {
        BlockEntity te = world.getBlockEntity(pos);

        // slave-blocks are only allowed if they already belong to this smeltery
        if (te instanceof IServentLogic) {
            return ((IServentLogic) te).isValidMaster(parent);
        }

        return true;
    }
    public boolean canExpand(StructureData data, World world) {
        // note that if the structure has neither a floor nor ceiling, this will only expand upwards
        // I really doubt we ever will want a structure with neither... if we did they can override this logic
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
        // want two positions one layer above the structure
        MultiblockResult result = detectLayer(world, from, to, pos -> {
        });
        setLastResult(result);
        return result.success;
    }
    protected abstract boolean isValidBlock(Block block);
    protected abstract boolean isValidFloor(Block block);

    protected abstract boolean isValidWall(Block block);

    @Override
    protected boolean isValidBlock(World world, BlockPos pos, CuboidSide side, boolean isFrame) {
        // controller always is valid
        if (pos.equals(parent.getPos())) {
            return true;
        }
        if (!isValidSlave(world, pos)) {
            return false;
        }

        // floor has a smaller list
        BlockState state = world.getBlockState(pos);
        // treat frame blocks as walls, its more natural
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
}