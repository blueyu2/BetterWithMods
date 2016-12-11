package betterwithmods.blocks.tile;

import betterwithmods.BWMItems;
import betterwithmods.blocks.BlockMechMachines;
import betterwithmods.craft.bulk.CraftingManagerBulk;
import betterwithmods.craft.heat.BWMHeatRegistry;
import betterwithmods.craft.heat.BWMHeatSource;
import betterwithmods.util.DirUtils;
import betterwithmods.util.InvUtils;
import betterwithmods.util.MechanicalUtil;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;

import java.util.List;

import static betterwithmods.blocks.tile.TileEntityFilteredHopper.putDropInInventoryAllSlots;

public abstract class TileEntityCookingPot extends TileEntityVisibleInventory implements IFacing {
    public int cookCounter;
    public int stokedCooldownCounter;
    public int scaledCookCounter;
    public boolean containsValidIngredients;
    public int fireIntensity;
    public int facing;
    private boolean forceValidation;

    public TileEntityCookingPot() {
        this.cookCounter = 0;
        this.containsValidIngredients = false;
        this.forceValidation = true;
        this.scaledCookCounter = 0;
        this.fireIntensity = -1;
        this.occupiedSlots = 0;
        this.facing = 1;
    }

    @Override
    public int getFacing() {
        return facing;
    }

    @Override
    public SimpleItemStackHandler createItemStackHandler() {
        return new SimpleItemStackHandler(this, true, 27);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        this.fireIntensity = tag.hasKey("fireIntensity") ? tag.getInteger("fireIntensity") : -1;
        this.facing = tag.hasKey("facing") ? tag.getInteger("facing") : 1;
        validateInventory();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        NBTTagCompound t = super.writeToNBT(tag);
        t.setInteger("fireIntensity", this.fireIntensity);
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
            if (this.getWorld() != null && stateChanged) {
                this.getWorld().notifyBlockUpdate(this.pos, state, state, 3);
            }
            if (this.fireIntensity != getFireIntensity()) {
                validateFireIntensity();
                this.forceValidation = true;
            }

            if (!block.isMechanicalOn(this.getWorld(), this.pos)) {
                this.facing = 1;
                entityCollision();
                if (this.fireIntensity > 0) {
                    if (this.forceValidation) {
                        validateContents();
                        this.forceValidation = false;
                    }

                    if (this.fireIntensity > 4) {
                        if (this.stokedCooldownCounter < 1)
                            this.cookCounter = 0;
                        this.stokedCooldownCounter = 20;

                        performStokedFireUpdate(getCurrentFireIntensity());
                    } else if (this.stokedCooldownCounter > 0) {
                        this.stokedCooldownCounter -= 1;

                        if (this.stokedCooldownCounter < 1) {
                            this.cookCounter = 0;
                        }
                    } else if (this.stokedCooldownCounter == 0 && this.fireIntensity > 0 && this.fireIntensity < 5)
                        performNormalFireUpdate(getCurrentFireIntensity());
                } else
                    this.cookCounter = 0;
            } else {
                this.cookCounter = 0;
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
                if (power != EnumFacing.UP && filledSlots() > 0) {
                    ejectInventory(dumpToward);
                }

            }
        }
        validateInventory();
        this.scaledCookCounter = this.cookCounter * 1000 / 4350;
    }

    private void entityCollision() {
        boolean flag = false;
        if (!isFull()) {
            flag = captureDroppedItems();
        }
        if (flag) {
            getWorld().scheduleBlockUpdate(pos, this.getBlockType(), this.getBlockType().tickRate(getWorld()), 5);//this.getWorld().markBlockForUpdate(this.getPos());
            this.markDirty();
        }
    }

    private boolean isFull() {
        for (int i = 0; i < this.inventory.getSlots(); i++) {
            ItemStack itemstack = this.inventory.getStackInSlot(i);
            if (itemstack == null || itemstack.stackSize != itemstack.getMaxStackSize()) {
                return false;
            }
        }
        return true;
    }

    public List<EntityItem> getCaptureItems(World worldIn, BlockPos pos) {
        return worldIn.getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1D, pos.getY() + 1.5D, pos.getZ() + 1D), EntitySelectors.IS_ALIVE);
    }

    private boolean captureDroppedItems() {
        List<EntityItem> items = this.getCaptureItems(getWorld(), getPos());
        if (items.size() > 0) {
            boolean flag = false;
            for (EntityItem item : items) {
                flag = putDropInInventoryAllSlots(inventory, item) || flag;
                //this.getWorld().playSound((EntityPlayer)null, pos.getX(), pos.getY(),pos.getZ(), SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.2F, ((getWorld().rand.nextFloat() - getWorld().rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
            }
            if (flag) {
                this.getWorld().playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.2F, ((getWorld().rand.nextFloat() - getWorld().rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
                if (this.validateInventory()) {
                    getWorld().scheduleBlockUpdate(pos, this.getBlockType(), this.getBlockType().tickRate(getWorld()), 5);
                }
                return true;
            }
        }
        return false;
    }

    public void ejectInventory(EnumFacing facing) {


        int index = InvUtils.getFirstOccupiedStackNotOfItem(inventory, Items.BRICK);
        if (index >= 0 && index < inventory.getSlots()) {
            ItemStack stack = inventory.getStackInSlot(index);
            int ejectStackSize = 8;
            if (8 > stack.stackSize) {
                ejectStackSize = stack.stackSize;
            }
            BlockPos target = pos.offset(facing);
            ItemStack eject = new ItemStack(stack.getItem(), ejectStackSize, stack.getItemDamage());
            InvUtils.copyTags(eject, stack);
            IBlockState targetState = getWorld().getBlockState(target);
            boolean ejectIntoWorld = getWorld().isAirBlock(target) || targetState.getBlock().isReplaceable(getWorld(), target) || !targetState.getMaterial().isSolid() || targetState.getBoundingBox(getWorld(), target).maxY < 0.5d;
            if (ejectIntoWorld) {
                this.getWorld().playSound(null, pos, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.BLOCKS, 0.2F, ((getWorld().rand.nextFloat() - getWorld().rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
                ejectStack(getWorld(), target, facing, eject);
                inventory.extractItem(index, ejectStackSize, false);
            }
        }
    }

    public void ejectStack(World world, BlockPos pos, EnumFacing facing, ItemStack stack) {
        Vec3i vec = new BlockPos(0, 0, 0).offset(facing);
        EntityItem item = new EntityItem(world, pos.getX() + 0.5F - (vec.getX() / 4d), pos.getY() + 0.25D, pos.getZ() + 0.5D - (vec.getZ() / 4d), stack);
        float velocity = 0.05F;
        item.motionX = (double) (vec.getX() * velocity);
        item.motionY = vec.getY() * velocity * 0.1;
        item.motionZ = (double) (vec.getZ() * velocity);
        item.setDefaultPickupDelay();
        world.spawnEntity(item);
    }

    @Override
    public void markDirty() {
        super.markDirty();
        this.forceValidation = true;
        if (this.getWorld() != null) {
            validateInventory();
        }
    }


    public boolean isUseableByPlayer(EntityPlayer player) {
        return this.getWorld().getTileEntity(this.pos) == this && player.getDistanceSq(this.pos.getX() + 0.5D, this.pos.getY() + 0.5D, this.pos.getZ() + 0.5D) <= 64.0D;
    }

    public abstract void validateContents();

    protected abstract CraftingManagerBulk getCraftingManager(boolean stoked);

    public int getCurrentFireIntensity() {
        int fireFactor = 0;

        if (this.fireIntensity > 0) {
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    int yPos = -1;
                    BlockPos target = pos.add(x, yPos, z);
                    Block block = this.getWorld().getBlockState(target).getBlock();
                    int meta = this.getWorld().getBlockState(target).getBlock().damageDropped(this.getWorld().getBlockState(target));
                    if (BWMHeatRegistry.get(block, meta) != null)
                        fireFactor += BWMHeatRegistry.get(block, meta).value;
                }
            }
            if (fireFactor < 5)
                fireFactor = 5;
        }

        return fireFactor;
    }

    private void performNormalFireUpdate(int fireIntensity) {
        if (this.containsValidIngredients) {
            this.cookCounter += fireIntensity;

            if (this.cookCounter >= 4350) {
                attemptToCookNormal();
                this.cookCounter = 0;
            }
        } else
            this.cookCounter = 0;
    }

    private void performStokedFireUpdate(int fireIntensity) {
        if (this.containsValidIngredients) {
            this.cookCounter += fireIntensity;

            if (this.cookCounter >= 4350) {
                if (containsExplosives())
                    explode();
                else
                    attemptToCookStoked();
                this.cookCounter = 0;
            }
        } else
            this.cookCounter = 0;
    }

    protected boolean containsExplosives() {
        return containsItem(BWMItems.MATERIAL, 16) || containsItem(Item.getItemFromBlock(Blocks.TNT)) || containsItem(Items.GUNPOWDER) || containsItem(BWMItems.MATERIAL, 29);
    }

    private boolean containsItem(Item item) {
        return containsItem(item, OreDictionary.WILDCARD_VALUE);
    }

    private boolean containsItem(Item item, int meta) {
        return InvUtils.getFirstOccupiedStackOfItem(inventory, item, meta) > -1;
    }

    private void explode() {
        int hellfire = InvUtils.countItemsInInventory(inventory, BWMItems.MATERIAL, 16);
        float expSize = hellfire * 10.0F / 64.0F;
        expSize += InvUtils.countItemsInInventory(inventory, Items.GUNPOWDER) * 10.0F / 64.0F;
        expSize += InvUtils.countItemsInInventory(inventory, BWMItems.MATERIAL, 29) * 10.0F / 64.0F;
        if (InvUtils.countItemsInInventory(inventory, Item.getItemFromBlock(Blocks.TNT)) > 0) {
            if (expSize < 4.0F)
                expSize = 4.0F;
            expSize += InvUtils.countItemsInInventory(inventory, Item.getItemFromBlock(Blocks.TNT));
        }

        if (expSize < 2.0F)
            expSize = 2.0F;
        else if (expSize > 10.0F)
            expSize = 10.0F;

        InvUtils.clearInventory(inventory);
        getWorld().setBlockToAir(pos);
        getWorld().createExplosion(null, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, expSize, true);
    }

    protected boolean attemptToCookNormal() {
        return attemptToCookWithManager(getCraftingManager(false));
    }

    protected boolean attemptToCookStoked() {
        return attemptToCookWithManager(getCraftingManager(true));
    }

    private boolean attemptToCookWithManager(CraftingManagerBulk man) {
        if (man != null) {
            if (man.getCraftingResult(inventory) != null) {
                ItemStack[] output = man.craftItem(inventory);

                if (output != null) {
                    for (ItemStack out : output) {
                        if (out != null) {
                            ItemStack stack = out.copy();
                            if (!InvUtils.addItemStackToInv(inventory, stack))
                                InvUtils.ejectStackWithOffset(this.getWorld(), this.pos.up(), stack);
                        }
                    }
                    return true;
                }
            }
        }
        return false;
    }

    public int getCookingProgressScaled(int scale) {
        return this.scaledCookCounter * scale / 1000;
    }

    public boolean isCooking() {
        return this.scaledCookCounter > 0;
    }

    private boolean validateInventory() {
        boolean stateChanged = false;
        byte currentSlots = (byte) InvUtils.getOccupiedStacks(inventory);
        if (currentSlots != this.occupiedSlots) {
            this.occupiedSlots = currentSlots;
            stateChanged = true;
        }
        if (getWorld() != null && stateChanged) {
            IBlockState state = getWorld().getBlockState(pos);
            getWorld().notifyBlockUpdate(pos, state, state, 3);
        }
        return stateChanged;
    }

    public int getFireIntensity() {
        BlockPos down = pos.down();
        Block block = this.getWorld().getBlockState(down).getBlock();
        int meta = block.damageDropped(this.getWorld().getBlockState(down));
        BWMHeatSource source = BWMHeatRegistry.get(block, meta);
        if (source != null)
            return source.value;
        return -1;
    }

    private void validateFireIntensity() {
        BlockPos down = pos.down();
        Block block = this.getWorld().getBlockState(down).getBlock();
        int meta = block.damageDropped(this.getWorld().getBlockState(down));
        BWMHeatSource source = BWMHeatRegistry.get(block, meta);
        if (source != null)
            fireIntensity = source.value;
        else
            fireIntensity = -1;
    }

    @Override
    public int getMaxVisibleSlots() {
        return 27;
    }
}
