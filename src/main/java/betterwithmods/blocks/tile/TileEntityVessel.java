package betterwithmods.blocks.tile;

import betterwithmods.blocks.BlockMechMachines;
import betterwithmods.util.DirUtils;
import betterwithmods.util.MechanicalUtil;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;

/**
 * Created by blueyu2 on 12/5/16.
 */
public class TileEntityVessel extends TileEntity implements IFacing, ITickable {
    public int xp;
    public int facing;

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        this.xp = tag.hasKey("xp") ? tag.getInteger("xp") : 0;
        this.facing = tag.hasKey("facing") ? tag.getInteger("facing") : 1;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        NBTTagCompound t = super.writeToNBT(tag);
        t.setInteger("xp", this.xp);
        t.setInteger("facing", facing);
        return t;
    }

    @Override
    public void update() {
        if (this.getWorld().isRemote)
            return;

        if (this.getWorld().getBlockState(this.pos).getBlock() instanceof BlockMechMachines) {
            BlockMechMachines block = (BlockMechMachines) this.getWorld().getBlockState(this.pos).getBlock();
            if (block.isCurrentStateValid(getWorld(), pos)) {
                getWorld().scheduleBlockUpdate(pos, block, block.tickRate(getWorld()), 5);
            }
            IBlockState state = this.getWorld().getBlockState(this.pos);
            boolean stateChanged = state.getValue(DirUtils.TILTING) != EnumFacing.getFront(facing);
            if (stateChanged) {
                this.getWorld().notifyBlockUpdate(this.pos, state, state, 3);
            }
            if (!block.isMechanicalOn(this.getWorld(), this.pos)) {
                this.facing = 1;
                entityCollision();
            } else {
                EnumFacing power = EnumFacing.UP;
                if (getWorld().getBlockState(pos).getValue(BlockMechMachines.ISACTIVE)) {
                    for (EnumFacing f : EnumFacing.HORIZONTALS) {
                        if (power != EnumFacing.UP) {
                            MechanicalUtil.destoryHorizontalAxles(getWorld(), getPos().offset(f));
                        }
                        if (MechanicalUtil.isBlockPoweredByAxleOnSide(getWorld(), pos, f) || MechanicalUtil.isPoweredByCrankOnSide(getWorld(), pos, f)) {
                            power = f;
                        }
                    }
                }
                facing = power.getIndex();
                EnumFacing dumpToward = DirUtils.rotateFacingAroundY(power, false);
                if (power != EnumFacing.UP && xp > 0) {
                    ejectXP(dumpToward);
                }

            }
        }
    }

    private void entityCollision() {
        boolean flag = false;
        if (xp <= 0) {
            flag = captureDroppedItems();
        }
        if (flag) {
            getWorld().scheduleBlockUpdate(pos, this.getBlockType(), this.getBlockType().tickRate(getWorld()), 5);
            this.markDirty();
        }
    }

    @Override
    public int getFacing() {
        return facing;
    }
}
