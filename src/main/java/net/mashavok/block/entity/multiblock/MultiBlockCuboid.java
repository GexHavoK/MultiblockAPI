package net.mashavok.block.entity.multiblock;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import net.mashavok.MultiBlockAPI;
import net.mashavok.tags.TagUtil;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

public abstract class MultiBlockCuboid<T extends MultiblockStructureData> {
    protected static final MultiblockResult NO_ATTEMPT = MultiblockResult.error(null, MultiBlockAPI.makeTranslation("multiblock", "generic.no_attempt"));
    protected static final MultiblockResult NOT_LOADED = MultiblockResult.error(null, MultiBlockAPI.makeTranslation("multiblock", "generic.not_loaded"));
    protected static final MultiblockResult TOO_HIGH = MultiblockResult.error(null, MultiBlockAPI.makeTranslation("multiblock", "generic.too_high"));
    protected static final Text INVALID_INNER_BLOCK = MultiBlockAPI.makeTranslation("multiblock", "generic.invalid_inner_block");
    protected static final String TOO_LARGE = MultiBlockAPI.makeTranslationKey("multiblock", "generic.too_large");
    protected static final Text INVALID_FLOOR_BLOCK = MultiBlockAPI.makeTranslation("multiblock", "generic.invalid_floor_block");
    protected static final Text INVALID_CEILING_BLOCK = MultiBlockAPI.makeTranslation("multiblock", "generic.invalid_floor_block");
    protected static final Text INVALID_WALL_BLOCK = MultiBlockAPI.makeTranslation("multiblock", "generic.invalid_wall_block");
    protected static final Text INVALID_FLOOR_FRAME = MultiBlockAPI.makeTranslation("multiblock", "generic.invalid_floor_frame");
    protected static final Text INVALID_CEILING_FRAME = MultiBlockAPI.makeTranslation("multiblock", "generic.invalid_ceiling_frame");
    protected static final Text INVALID_WALL_FRAME = MultiBlockAPI.makeTranslation("multiblock", "generic.invalid_wall_frame");

    private static final int NORTH = Direction.NORTH.getHorizontal();
    private static final int EAST = Direction.EAST.getHorizontal();
    private static final int SOUTH = Direction.SOUTH.getHorizontal();
    private static final int WEST = Direction.WEST.getHorizontal();
    protected final boolean hasFloor;
    protected final boolean hasFrame;
    protected final boolean hasCeiling;

    private final int maxHeight;
    private final int innerLimit;

    public MultiblockResult lastResult = NO_ATTEMPT;

    protected MultiBlockCuboid(boolean hasFloor, boolean hasFrame, boolean hasCeiling, int maxHeight, int innerLimit) {
        this.hasFloor = hasFloor;
        this.hasFrame = hasFrame;
        this.hasCeiling = hasCeiling;
        this.maxHeight = 64;
        this.innerLimit = 14;
    }
    @Nullable
    public T detectMultiblock(World world, BlockPos master, Direction facing) {
        ImmutableSet.Builder<BlockPos> extraBlocks = ImmutableSet.builder();
        BlockPos center = master.offset(facing.getOpposite());
        CuboidSide neededCap = null;
        if (!isInnerBlock(world, center)) {
            if (!hasFrame || (!hasFloor && !hasCeiling)) {
                setLastResult(MultiblockResult.error(center, INVALID_INNER_BLOCK));
                return null;
            }
            if (hasFloor) {
                neededCap = CuboidSide.FLOOR;
                center = center.up();
            } else {
                neededCap = CuboidSide.CEILING;
                center = center.down();
            }
            if (!isInnerBlock(world, center)) {
                setLastResult(MultiblockResult.error(center, INVALID_INNER_BLOCK));
                return null;
            }
        }
        int[] edges = new int[4];
        for (Direction direction : Direction.Type.HORIZONTAL) {
            BlockPos pos = getOuterPos(world, center, direction, innerLimit + 1);
            edges[direction.getHorizontal()] = (pos.getX() - center.getX()) + (pos.getZ() - center.getZ());
        }
        int xd = (edges[SOUTH] - edges[NORTH]) - 1;
        int zd = (edges[EAST] - edges[WEST]) - 1;
        if(xd > innerLimit || zd > innerLimit) {
            setLastResult(MultiblockResult.error(null, TOO_LARGE, xd, zd, innerLimit, innerLimit));
            return null;
        }
        BlockPos from = center.add(edges[WEST], 0, edges[NORTH]);
        BlockPos to = center.add(edges[EAST], 0, edges[SOUTH]);
        Consumer<Collection<BlockPos>> posConsumer = extraBlocks::addAll;
        MultiblockResult result = detectLayer(world, from, to, posConsumer);
        if (!result.success) {
            setLastResult(result);
            return null;
        }
        MultiblockResult layerResult = MultiblockResult.SUCCESS;
        setLastResult(MultiblockResult.SUCCESS);
        int minLayer = -1;
        int remainingHeight = maxHeight - 1;
        if (neededCap != CuboidSide.FLOOR) {
            for (; minLayer > -remainingHeight; minLayer--) {
                layerResult = detectLayer(world, from.up(minLayer), to.up(minLayer), posConsumer);
                if (!layerResult.success) {
                    break;
                }
            }
        }
        remainingHeight += minLayer + 1;
        if (hasFloor) {
            MultiblockResult floorResult = detectCap(world, from.up(minLayer), to.up(minLayer), CuboidSide.FLOOR, posConsumer);
            if (!floorResult.success) {
                setLastResult(floorResult);
                return null;
            }
        } else {
            minLayer++;
            setLastResult(layerResult);
        }
        int maxLayer = 1;
        if (neededCap != CuboidSide.CEILING) {
            for (; maxLayer < remainingHeight; maxLayer++) {
                layerResult = detectLayer(world, from.up(maxLayer), to.up(maxLayer), posConsumer);
                if (!layerResult.success) {
                    break;
                }
            }
        }
        if (hasCeiling) {
            MultiblockResult floorResult = detectCap(world, from.up(maxLayer), to.up(maxLayer), CuboidSide.CEILING, posConsumer);
            if (!floorResult.success) {
                setLastResult(floorResult);
                return null;
            }
        } else {
            maxLayer--;
            setLastResult(layerResult);
        }
        BlockPos minPos = from.up(minLayer);
        BlockPos maxPos = to.up(maxLayer);
        return create(minPos, maxPos, extraBlocks.build());
    }

    private void setLastResult(MultiblockResult error) {
    }

    protected BlockPos getOuterPos(World world, BlockPos pos, Direction direction, int limit) {
        for(int i = 0; i < limit && world.isChunkLoaded(pos) && isInnerBlock(world, pos); i++) {
            pos = pos.offset(direction);
        }

        return pos;
    }
    @SuppressWarnings("deprecation")
    protected MultiblockResult detectCap(World world, BlockPos from, BlockPos to, CuboidSide side, Consumer<Collection<BlockPos>> consumer) {
        if (!world.isRegionLoaded(from, to)) {
            return NOT_LOADED;
        }
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        int height = from.getY();
        if (hasFrame) {
            Predicate<BlockPos> frameCheck = pos -> isValidBlock(world, pos, side, true);
            Text frameError = side == CuboidSide.CEILING ? INVALID_CEILING_FRAME : INVALID_FLOOR_FRAME;
            for (int x = from.getX(); x <= to.getX(); x++) {
                if (!frameCheck.test(mutable.set(x, height, from.getZ()))) return MultiblockResult.error(mutable.toImmutable(), frameError);
                if (!frameCheck.test(mutable.set(x, height, to.getZ())))   return MultiblockResult.error(mutable.toImmutable(), frameError);
            }
            for (int z = from.getZ() + 1; z < to.getZ(); z++) {
                if (!frameCheck.test(mutable.set(from.getX(), height, z))) return MultiblockResult.error(mutable.toImmutable(), frameError);
                if (!frameCheck.test(mutable.set(to.getX(), height, z)))   return MultiblockResult.error(mutable.toImmutable(), frameError);
            }
        }
        Text blockError = side == CuboidSide.CEILING ? INVALID_CEILING_BLOCK : INVALID_FLOOR_BLOCK;
        for (int z = from.getZ() + 1; z < to.getZ(); z++) {
            for (int x = from.getX() + 1; x < to.getX(); x++) {
                if (!isValidBlock(world, mutable.set(x, height, z), side, false)) {
                    return MultiblockResult.error(mutable.toImmutable(), blockError);
                }
            }
        }
        return MultiblockResult.SUCCESS;
    }
    @SuppressWarnings("deprecation")
    protected MultiblockResult detectLayer(World world, BlockPos from, BlockPos to, Consumer<Collection<BlockPos>> consumer) {
        // ensure its loaded
        if(!world.isRegionLoaded(from, to)) {
            return NOT_LOADED;
        }
        List<BlockPos> candidates = Lists.newArrayList();
        BlockPos mutable = new BlockPos.Mutable();
        int height = from.getY();
        for (int x = from.getX() + 1; x < to.getX(); x++) {
            for (int z = from.getZ() + 1; z < to.getZ(); z++) {
                mutable.add(x, height, z);
                if (isInnerBlock(world, mutable)) {
                    if (!world.isAir(mutable)) {
                        candidates.add(mutable.toImmutable());
                    }
                } else {
                    return MultiblockResult.error(mutable.toImmutable(), INVALID_INNER_BLOCK);
                }
            }
        }
        if (hasFrame) {
            Predicate<BlockPos> frameCheck = pos -> isValidBlock(world, pos, CuboidSide.WALL, true);
            if (!frameCheck.test(from)) return MultiblockResult.error(from.toImmutable(), INVALID_WALL_FRAME);
            if (!frameCheck.test(mutable.add(from.getX(), height, to.getZ()))) return MultiblockResult.error(mutable.toImmutable(), INVALID_WALL_FRAME);
            if (!frameCheck.test(mutable.add(to.getX(), height, from.getZ()))) return MultiblockResult.error(mutable.toImmutable(), INVALID_WALL_FRAME);
            if (!frameCheck.test(to))   return MultiblockResult.error(to.toImmutable(), INVALID_WALL_FRAME);
        }
        Predicate<BlockPos> wallCheck = pos -> isValidBlock(world, pos, CuboidSide.WALL, false);
        for (int x = from.getX() + 1; x < to.getX(); x++) {
            if (!wallCheck.test(mutable.add(x, height, from.getZ()))) return MultiblockResult.error(mutable.toImmutable(), INVALID_WALL_BLOCK);
            if (!wallCheck.test(mutable.add(x, height, to.getZ()))) return MultiblockResult.error(mutable.toImmutable(), INVALID_WALL_BLOCK);
        }
        for (int z = from.getZ() + 1; z < to.getZ(); z++) {
            if (!wallCheck.test(mutable.add(from.getX(), height, z))) return MultiblockResult.error(mutable.toImmutable(), INVALID_WALL_BLOCK);
            if (!wallCheck.test(mutable.add(to.getX(), height, z))) return MultiblockResult.error(mutable.toImmutable(), INVALID_WALL_BLOCK);
        }
        consumer.accept(candidates);
        return MultiblockResult.SUCCESS;
    }

    protected abstract boolean isValidBlock(World world, BlockPos pos, CuboidSide side, boolean isFrame);

    public boolean isInnerBlock(World world, BlockPos pos) {
        return world.isAir(pos);
    }


    public abstract boolean shouldUpdate(World world, MultiblockStructureData structure, BlockPos pos, BlockState state);
    @Nullable
    public T readFromTag(NbtCompound nbt) {
        BlockPos minPos = TagUtil.readPos(nbt, MultiblockStructureData.MIN);
        BlockPos maxPos = TagUtil.readPos(nbt, MultiblockStructureData.MAX);
        if (minPos == null || maxPos == null) {
            return null;
        }
        // will be empty client side
        Set<BlockPos> extra = ImmutableSet.copyOf(readPosList(nbt, MultiblockStructureData.EXTRA_POS));
        return create(minPos, maxPos, extra);
    }
    public abstract T create(BlockPos min, BlockPos max, Set<BlockPos> extraPos);
    protected static Collection<BlockPos> readPosList(NbtCompound rootTag, String key) {
        List<BlockPos> collection;
        if (rootTag.contains(key, NbtElement.LIST_TYPE)) {
            NbtList list = rootTag.getList(key, NbtElement.COMPOUND_TYPE);
            collection = new ArrayList<>(list.size());
            for (int i = 0; i < list.size(); i++) {
                BlockPos pos = TagUtil.readPos(list.getCompound(i));
                if (pos != null) {
                    collection.add(pos);
                }
            }
        } else {
            collection = Collections.emptyList();
        }
        return collection;
    }
    public enum CuboidSide {
        FLOOR,
        CEILING,
        WALL
    }
}
