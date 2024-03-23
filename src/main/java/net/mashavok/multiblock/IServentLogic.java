package net.mashavok.multiblock;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public interface IServentLogic {
    BlockPos getMasterPos();
    void notifyMasterOfChange(BlockPos pos, BlockState state);
    boolean isValidMaster(IMasterLogic master);
    void setPotentialMaster(IMasterLogic master);
    void removeMaster(IMasterLogic master);
}
