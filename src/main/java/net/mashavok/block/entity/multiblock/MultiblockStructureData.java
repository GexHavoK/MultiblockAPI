package net.mashavok.block.entity.multiblock;

import net.mashavok.block.entity.BlockEntityHelper;
import net.mashavok.block.entity.MultiBlockEntity;
import net.mashavok.multiblock.IMasterLogic;
import net.mashavok.multiblock.IServentLogic;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class MultiblockStructureData {
    public static final String EXTRA_POS = "extra";
    public static final String MIN = "min";
    public static final String MAX = "max";
    private final BlockPos minPos;
    private final BlockPos maxPos;
    protected Set<BlockPos> extra;
    private final boolean hasCeiling, hasFrame, hasFloor;
    private final BlockPos minInside;

    private final BlockPos maxInside;

    public MultiblockStructureData(BlockPos minPos, BlockPos maxPos, Set<BlockPos> extraPositons, boolean hasFloor, boolean hasFrame, boolean hasCeiling, BlockPos minInside, BlockPos maxInside, int innerX, int innerY, int innerZ, Box bounds) {
        this.minPos = minPos;
        this.maxPos = maxPos;
        this.extra = extraPositons;
        this.hasFloor = hasFloor;
        this.hasFrame = hasFrame;
        this.hasCeiling = hasCeiling;
        this.minInside = minInside;
        this.maxInside = maxInside;
    }
    public static boolean isWithin(BlockPos pos, BlockPos min, BlockPos max){
        return pos.getX() >= min.getX() && pos.getY() >= min.getY() && pos.getZ() >= min.getZ()
                && pos.getX() <= max.getX() && pos.getY() <= max.getY() && pos.getZ() <= max.getZ();
    }
    public boolean withinBounds(BlockPos pos) {
        return isWithin(pos, minPos, maxPos);
    }
    public boolean isInside(BlockPos pos) {
        return isWithin(pos, minInside, maxInside);
    }
    public boolean contains(BlockPos pos) {
        return withinBounds(pos) && containsBase(pos);
    }
    private boolean containsBase(BlockPos pos) {
        if (!isInside(pos)) {
            if (hasFrame) {
                return true;
            }
            int edges = 0;
            if (pos.getX() == minPos.getX() || pos.getX() == maxPos.getX()) edges++;
            if (pos.getZ() == minPos.getZ() || pos.getZ() == maxPos.getZ()) edges++;
            if ((hasFloor && pos.getY() == minPos.getY()) ||
                    (hasCeiling && pos.getX() == maxPos.getX())) edges++;
            if (edges < 2) {
                return true;
            }
        }
        return extra.contains(pos);
    }
    public boolean isDirectlyAbove(BlockPos pos) {
        return pos.getX() >= minPos.getX() && pos.getZ() >= minPos.getZ()
                && pos.getX() <= maxPos.getX() && pos.getZ() <= maxPos.getZ();
    }
    public void forEachContained(Consumer<BlockPos.Mutable> consumer) {
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        for (int x = minPos.getX(); x <= maxPos.getX(); x++) {
            for (int y = minPos.getY(); y <= maxPos.getY(); y++) {
                for (int z = minPos.getZ(); z <= maxPos.getZ(); z++) {
                    mutable.set(x, y, z);
                    if (containsBase(mutable)) {
                        consumer.accept(mutable);
                    }
                }
            }
        }
    }
    public <T extends MultiBlockEntity & IMasterLogic> void assignMaster(T master, @Nullable MultiblockStructureData oldStructure) {
        Predicate<BlockPos> shouldUpdate;
        if (oldStructure == null) {
            shouldUpdate = pos -> true;
        } else {
            shouldUpdate = pos -> !oldStructure.contains(pos);
        }

        World world = master.getWorld();
        assert world != null;
        forEachContained(pos -> {
            if (shouldUpdate.test(pos) && world.isChunkLoaded(pos)) {
                BlockEntityHelper.get(IServentLogic.class, world, pos).ifPresent(te -> te.setPotentialMaster(master));
            }
        });
        if (oldStructure != null) {
            oldStructure.forEachContained(pos -> {
                if (!contains(pos) && world.isChunkLoaded(pos)) {
                    BlockEntityHelper.get(IServentLogic.class, world, pos).ifPresent(te -> te.removeMaster(master));
                }
            });
        }
    }
}
