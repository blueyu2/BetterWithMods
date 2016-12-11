package betterwithmods.entity.ai;

import betterwithmods.BWMItems;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

/**
 * Purpose:
 *
 * @author Tyler Marshall
 * @version 11/20/16
 */
public class EndermanAgro extends EntityAINearestAttackableTarget<EntityPlayer> {
    public final EntityEnderman enderman;
    public EntityPlayer player;
    public int aggroTime;
    public int teleportTime;

    public EndermanAgro(EntityEnderman enderman) {
        super(enderman, EntityPlayer.class, false);
        this.enderman = enderman;
    }

    private boolean shouldEndermanAttackPlayer(@Nonnull EntityEnderman enderman, @Nonnull EntityPlayer player) {
        ItemStack stack = player.inventory.armorInventory[3];
        if (stack != null && stack.getItem() == BWMItems.ENDER_SPECTACLES) {
            return false;
        } else {
            return enderman.shouldAttackPlayer(player);
        }
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    @Override
    public boolean shouldExecute() {
        double d0 = this.getTargetDistance();
        this.player = this.enderman.getEntityWorld().getNearestAttackablePlayer(this.enderman.posX, this.enderman.posY, this.enderman.posZ, d0, d0, null, player -> player != null && shouldEndermanAttackPlayer(enderman, player));
        return this.player != null;
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    @Override
    public void startExecuting() {
        this.aggroTime = 5;
        this.teleportTime = 0;
    }

    /**
     * Resets the task
     */
    @Override
    public void resetTask() {
        this.player = null;
        super.resetTask();
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    @Override
    public boolean continueExecuting() {
        if (this.player != null) {
            if (!shouldEndermanAttackPlayer(enderman, player)) {
                return false;
            } else {
                this.enderman.faceEntity(this.player, 10.0F, 10.0F);
                return true;
            }
        } else {
            return this.targetEntity != null && this.targetEntity.isEntityAlive() || super.continueExecuting();
        }
    }

    /**
     * Updates the task
     */
    @Override
    public void updateTask() {

        if (this.player != null) {
            if (--this.aggroTime <= 0) {
                this.targetEntity = this.player;
                this.player = null;
                super.startExecuting();
            }
        } else {
            if (this.targetEntity != null) {
                if (shouldEndermanAttackPlayer(enderman, targetEntity)) {
                    if (this.targetEntity.getDistanceSqToEntity(this.enderman) < 16.0D) {
                        this.enderman.teleportRandomly();
                    }
                    this.teleportTime = 0;
                } else if (this.targetEntity.getDistanceSqToEntity(this.enderman) > 256.0D && this.teleportTime++ >= 30 && this.enderman.teleportToEntity(this.targetEntity)) {
                    this.teleportTime = 0;
                }
            }

            super.updateTask();
        }
    }
}