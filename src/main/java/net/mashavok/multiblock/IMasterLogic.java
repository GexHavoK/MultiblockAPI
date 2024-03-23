package net.mashavok.multiblock;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;

public interface IMasterLogic {
    private BlockEntity self(){
        return (BlockEntity) this;
    }
    default BlockState getMasterBlock(){
        return self().getCachedState();
    }
    default BlockPos getMasterPos() {
        return self().getPos();
    }

    void notify(ServentTileEntity serventTileEntity, BlockPos pos, BlockState state);
}
