package net.mashavok.block.entity;



import net.mashavok.MultiBlockAPI;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;


import java.util.Optional;



@SuppressWarnings("WeakerAccess")
public class BlockEntityHelper {
    public static <T> Optional<T> get(Class<T> clazz, @Nullable BlockView world, BlockPos pos){
        return get(clazz, world, pos, false);
    }
    public static <T> Optional<T> get(Class<T> clazz, @Nullable net.minecraft.world.BlockView world, BlockPos pos, boolean logWrongType) {
        if (!isBlockLoaded(world, pos)) {
            return Optional.empty();
        }
        BlockEntity tile = world.getBlockEntity(pos);
        if (tile == null) {
            return Optional.empty();
        }

        if (clazz.isInstance(tile)) {
            return Optional.of(clazz.cast(tile));
        } else if (logWrongType) {
            MultiBlockAPI.LOGGER.warn("Unexpected TileEntity class at {}, expected {}, but found: {}", pos, clazz, tile.getClass());
        }

        return Optional.empty();
    }
    @SuppressWarnings("deprecation")
    public static boolean isBlockLoaded(@Nullable BlockView world, BlockPos pos) {
        if (world == null) {
            return false;
        }
        if (world instanceof WorldView) {
            return ((WorldView) world).isChunkLoaded(pos);
        }
        return true;
    }

    /** Handles the unchecked cast for a block entity ticker */
    @SuppressWarnings("unchecked")
    @Nullable
    public static <HAVE extends BlockEntity, RET extends BlockEntity> BlockEntityTicker<RET> castTicker(BlockEntityType<RET> expected, BlockEntityType<HAVE> have, BlockEntityTicker<? super HAVE> ticker) {
        return have == expected ? (BlockEntityTicker<RET>)ticker : null;
    }

    /** Handles the unchecked cast for a block entity ticker */
    @Nullable
    public static <HAVE extends BlockEntity, RET extends BlockEntity> BlockEntityTicker<RET> serverTicker(World world, BlockEntityType<RET> expected, BlockEntityType<HAVE> have, BlockEntityTicker<? super HAVE> ticker) {
        return world.isClient ? null : castTicker(expected, have, ticker);
    }
}