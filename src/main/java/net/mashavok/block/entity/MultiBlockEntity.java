package net.mashavok.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class MultiBlockEntity extends BlockEntity {

    public MultiBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }
    public boolean isClient(){
        return this.getWorld() !=null && this.getWorld().isClient;
    }
    @SuppressWarnings("deprecation")
    public void setChangedFast(){
        if (world != null){
            if(world.isChunkLoaded(pos)){
                world.getWorldChunk(pos).setNeedsSaving(true);
            }
        }
    }
    protected boolean shouldSyncOnUpdate(){
        return false;
    }
    @Nullable
    public BlockEntityUpdateS2CPacket getUpdatePacket(){
        return shouldSyncOnUpdate() ? BlockEntityUpdateS2CPacket.create(this) : null;
    }
    protected void saveSynced(NbtCompound nbt){}
    public NbtCompound getUpdateTag(){
        NbtCompound nbt = new NbtCompound();
        saveSynced(nbt);
        return nbt;
    }
    public void writenbt(NbtCompound nbt){
        super.writeNbt(nbt);
        saveSynced(nbt);
    }
}
