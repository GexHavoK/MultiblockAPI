package net.mashavok.block.entity;


import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.shape.VoxelShape;

public interface BoundingBoxEntity {
    Box INFINITE_EXTENT_AABB = new Box(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);

    default BlockEntity self() {
        return (BlockEntity) this;
    }

    default Box getRenderBoundingBox() {
        Box box = getInfiniteBoundingBox();
        BlockPos pos = self().getPos();
        try {
            VoxelShape collisionShape = self().getCachedState().getCollisionShape(self().getWorld(), pos);
            if (!collisionShape.isEmpty()) {
                box = collisionShape.getBoundingBox().offset(pos);
            }
        } catch (Exception e) {
            box = new Box(pos.add(-1, 0, -1), pos.add(1, 1, 1));
        }

        return box;
    }

    default Box getInfiniteBoundingBox() {
        return INFINITE_EXTENT_AABB;
    }
}