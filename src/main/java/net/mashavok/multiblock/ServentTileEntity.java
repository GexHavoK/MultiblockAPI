package net.mashavok.multiblock;

import net.mashavok.TagUtil;
import net.mashavok.block.entity.BlockEntityHelper;
import net.mashavok.block.entity.MultiBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class ServentTileEntity extends MultiBlockEntity implements IServentLogic {
    private static final String MASTER_POS = "masterPos";
    private static final String MASTER_BLOCK = "master_Block";
    @Nullable
    private BlockPos masterPos;
    @Nullable
    private Block masterBlock;
    public ServentTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public boolean hasMaster() {
        return masterPos != null;
    }

    protected void setMaster(@Nullable BlockPos master, @Nullable Block block) {
        masterPos = master;
        masterBlock = block;
        this.setChangedFast();
    }
    protected boolean validateMaster() {
        if (masterPos == null) {
            return false;
        }
        assert world != null;
        if (world.getBlockState(masterPos).getBlock() == masterBlock) {
            return true;
        }
        setMaster(null, null);
        return false;
    }

    @Override
    public boolean isValidMaster(IMasterLogic master) {
        if (validateMaster()) {
            return master.getMasterPos().equals(this.masterPos);
        }
        return true;
    }

    @Override
    public BlockPos getMasterPos() {
        return null;
    }

    @Override
    public void notifyMasterOfChange(BlockPos pos, BlockState state) {
        if (validateMaster()) {
            assert masterPos != null;
            BlockEntityHelper.get(IMasterLogic.class, world, masterPos).ifPresent(te -> te.notify(this, pos, state));
        }
    }

    @Override
    public void setPotentialMaster(IMasterLogic master) {
        BlockPos newMaster = master.getMasterPos();
        if (newMaster.equals(this.masterPos)) {
            masterBlock = master.getMasterBlock().getBlock();
            this.setChangedFast();
        } else if (!validateMaster()) {
            setMaster(newMaster, master.getMasterBlock().getBlock());
        }
    }

    @Override
    public void removeMaster(IMasterLogic master) {
        if (masterPos != null && masterPos.equals(master.getMasterPos())) {
            setMaster(null, null);
        }
    }
    protected void readMaster(NbtCompound tags) {
        BlockPos masterPos = TagUtil.readPos(tags, MASTER_POS);
        Block masterBlock = null;
        if (masterPos != null && tags.contains(MASTER_BLOCK)) {
            Identifier masterBlockName = Identifier.tryParse(tags.getString(MASTER_BLOCK));
            if (masterBlockName != null && Registries.BLOCK.containsId(masterBlockName)) {
                masterBlock = Registries.BLOCK.get(masterBlockName);
            }
        }
        if (masterBlock != null) {
            this.masterPos = masterPos;
            this.masterBlock = masterBlock;
        }
    }
    public void load(NbtCompound tags) {
        super.writenbt(tags);
        readMaster(tags);
    }
    protected NbtCompound writeMaster(NbtCompound tags) {
        if (masterPos != null && masterBlock != null) {
            tags.put(MASTER_POS, TagUtil.writePos(masterPos));
            tags.putString(MASTER_BLOCK, Objects.requireNonNull(Registries.BLOCK.getKey(masterBlock)).toString());
        }
        return tags;
    }

    @Override
    public void writenbt(NbtCompound tags) {
        super.writenbt(tags);
        writeMaster(tags);
    }
}