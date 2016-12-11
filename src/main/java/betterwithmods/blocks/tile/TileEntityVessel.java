package betterwithmods.blocks.tile;

import betterwithmods.blocks.BlockMechMachines;
import betterwithmods.util.DirUtils;
import betterwithmods.util.MechanicalUtil;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import java.util.List;

/**
 * Created by blueyu2 on 12/5/16.
 */
public class TileEntityVessel extends TileEntity implements IFacing, ITickable {
    public int xp;
    private final int xpMax = 800;
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
        if (xp < xpMax) {
            flag = captureDroppedXP();
        }
        if (flag) {
            getWorld().scheduleBlockUpdate(pos, this.getBlockType(), this.getBlockType().tickRate(getWorld()), 5);
            this.markDirty();
        }
    }

    private boolean captureDroppedXP() {
        List<EntityXPOrb> orbs = getWorld().getEntitiesWithinAABB(EntityXPOrb.class, new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1D, pos.getY() + 1.5D, pos.getZ() + 1D), EntitySelectors.IS_ALIVE);
        if (orbs.size() > 0) {
            boolean flag = false;
            for (EntityXPOrb orb : orbs) {
                if(xp < xpMax) {
                    flag = true;
                    xp = Math.min(xp + orb.getXpValue(), xpMax);
                    orb.setDead();
                }
            }
            if (flag) {
                this.getWorld().playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.ENTITY_EXPERIENCE_ORB_TOUCH, SoundCategory.PLAYERS, 0.2F, ((getWorld().rand.nextFloat() - getWorld().rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
                return true;
            }
        }
        return false;
    }

    public void ejectXP(EnumFacing facing) {
        BlockPos target = pos.offset(facing);
        IBlockState targetState = getWorld().getBlockState(target);
        boolean ejectIntoWorld = getWorld().isAirBlock(target) || targetState.getBlock().isReplaceable(getWorld(), target) || !targetState.getMaterial().isSolid() || targetState.getBoundingBox(getWorld(), target).maxY < 0.5d;
        if (ejectIntoWorld) {
            this.getWorld().playSound(null, pos, SoundEvents.ENTITY_EXPERIENCE_ORB_TOUCH, SoundCategory.PLAYERS, 0.2F, ((getWorld().rand.nextFloat() - getWorld().rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
            while (xp > 0)
            {
                int eject = EntityXPOrb.getXPSplit(xp);
                xp -= eject;
                this.world.spawnEntity(new EntityXPOrb(this.world, target.getX(), target.getY(), target.getZ(), eject));
            }
        }

    }

    @Override
    public int getFacing() {
        return facing;
    }
}
