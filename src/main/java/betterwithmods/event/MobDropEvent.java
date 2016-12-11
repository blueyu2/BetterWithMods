package betterwithmods.event;

import betterwithmods.BWMBlocks;
import betterwithmods.BWMItems;
import betterwithmods.blocks.BlockAesthetic;
import betterwithmods.config.BWConfig;
import betterwithmods.entity.EntityShearedCreeper;
import betterwithmods.items.ItemMaterial;
import betterwithmods.util.InvUtils;
import betterwithmods.util.player.EntityPlayerExt;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityRabbit;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.*;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MobDropEvent {
    private static final int[] fearLevel = {1600, 1500, 1400, 1300, 1200, 1100, 1000, 900, 800, 700, 600, 500, 400, 300, 200, 100};
    private static final Random rand = new Random();
    public static FakePlayer player;

    //Initializing a static fake player for saws, so spawn isn't flooded with player equipping sounds when mobs hit the saw.
    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load evt) {
        if (evt.getWorld() instanceof WorldServer) {
            player = FakePlayerFactory.getMinecraft((WorldServer) evt.getWorld());
            ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
            sword.addEnchantment(Enchantment.getEnchantmentByLocation("looting"), 2);
            player.setHeldItem(EnumHand.MAIN_HAND, sword);
        }
    }

    //Not sure if this would be needed, but can't be too safe.
    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload evt) {
        if (evt.getWorld() instanceof WorldServer) {
            if (player != null) {
                player.setHeldItem(EnumHand.MAIN_HAND, null);
                player = null;
            }
        }
    }

    @SubscribeEvent
    public void mobDungProduction(LivingEvent.LivingUpdateEvent evt) {
        if (evt.getEntityLiving().getEntityWorld().isRemote)
            return;

        if (!BWConfig.produceDung)
            return;

        if (evt.getEntityLiving() instanceof EntityAnimal) {
            EntityAnimal animal = (EntityAnimal) evt.getEntityLiving();
            if (animal instanceof EntityWolf) {
                if (!animal.getEntityWorld().canSeeSky(animal.getPosition())) {
                    if (animal.getGrowingAge() > 99) {
                        int light = animal.getEntityWorld().getLight(animal.getPosition());
                        if (animal.getGrowingAge() == fearLevel[light]) {
                            evt.getEntityLiving().entityDropItem(new ItemStack(BWMItems.MATERIAL, 1, 5), 0.0F);
                            animal.setGrowingAge(99);
                        }
                    }
                }
            }
            if (!(animal instanceof EntityRabbit)) {
                if (animal.getGrowingAge() == 100) {
                    evt.getEntityLiving().entityDropItem(new ItemStack(BWMItems.MATERIAL, 1, 5), 0.0F);
                } else if (animal.isInLove()) {
                    if (rand.nextInt(1200) == 0) {
                        evt.getEntityLiving().entityDropItem(new ItemStack(BWMItems.MATERIAL, 1, 5), 0.0F);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void mobDiesBySaw(LivingDropsEvent evt) {
        BlockPos pos = evt.getEntityLiving().getPosition().down();
        if (isChoppingBlock(evt.getEntityLiving().getEntityWorld(), pos) || isBattleAxe(evt.getEntityLiving())) {
            if (!(evt.getEntityLiving() instanceof EntityPlayer)) {
                for (EntityItem item : evt.getDrops()) {
                    ItemStack stack = item.getEntityItem();
                    if (stack.getMaxStackSize() != 1 && evt.getEntity().getEntityWorld().rand.nextBoolean())
                        item.setEntityItemStack(new ItemStack(stack.getItem(), stack.stackSize + 1, stack.getItemDamage()));
                }
            }
            if (evt.getEntityLiving() instanceof EntityAgeable)
                addDrop(evt, new ItemStack(BWMItems.MATERIAL, 1, 5));
            int headChance = evt.getEntityLiving().getEntityWorld().rand.nextInt(12);
            if (headChance < 5) {
                if (evt.getEntityLiving() instanceof EntitySkeleton) {
                    EntitySkeleton skeltal = (EntitySkeleton) evt.getEntityLiving();
                    if (skeltal.getSkeletonType() != SkeletonType.STRAY)
                        addDrop(evt, new ItemStack(Items.SKULL, 1, skeltal.getSkeletonType().getId()));
                } else if (evt.getEntityLiving() instanceof EntityZombie)
                    addDrop(evt, new ItemStack(Items.SKULL, 1, 2));
                else if (evt.getEntityLiving() instanceof EntityCreeper)
                    addDrop(evt, new ItemStack(Items.SKULL, 1, 4));
                else if (evt.getEntityLiving() instanceof EntityPlayer) {
                    addDrop(evt, EntityPlayerExt.getPlayerHead((EntityPlayer) evt.getEntityLiving()));
                }
            }
        }
    }

    private boolean isChoppingBlock(World world, BlockPos pos) {
        if (world.getBlockState(pos).getBlock() == BWMBlocks.AESTHETIC) {
            IBlockState state = world.getBlockState(pos);
            return state.getValue(BlockAesthetic.blockType) == BlockAesthetic.EnumType.CHOPBLOCK || state.getValue(BlockAesthetic.blockType) == BlockAesthetic.EnumType.CHOPBLOCKBLOOD;
        }
        return false;
    }

    private boolean isBattleAxe(EntityLivingBase entity) {
        DamageSource source = entity.getLastDamageSource();
        if (source != null && source.getSourceOfDamage() != null) {
            Entity e = source.getSourceOfDamage();
            if (e instanceof EntityLivingBase) {
                ItemStack held = ((EntityLivingBase) e).getHeldItemMainhand();
                if (held != null && held.isItemEqual(new ItemStack(BWMItems.STEEL_BATTLEAXE))) {
                    return true;
                }
            }
        }
        return false;
    }

    @SubscribeEvent
    public void mobDrops(LivingDropsEvent evt) {
        if (BWConfig.armorDrops) {
            if (evt.getEntity() instanceof EntityZombie || evt.getEntity() instanceof EntitySkeleton) {
                EntityMob mob = (EntityMob) evt.getEntity();
                List<ItemStack> drops = new ArrayList<>();
                for (EntityItem item : evt.getDrops()) {
                    if (item.getEntityItem() != null) {
                        drops.add(item.getEntityItem().copy());
                    }
                }
                for (ItemStack item : mob.getEquipmentAndArmor()) {
                    if (item != null) {
                        if (!InvUtils.listContainsArmor(item, drops)) {
                            if (isNonDefaultArmor(mob, item)) {
                                createDamagedItem(evt, item.copy());
                            }
                        }
                    }
                }
            }
        }
        if (!BWConfig.hardcoreGunpowder)
            return;
        if (evt.getEntity() instanceof EntityCreeper || evt.getEntity() instanceof EntityGhast) {
            for (EntityItem item : evt.getDrops()) {
                ItemStack stack = item.getEntityItem();
                if (stack.getItem() == Items.GUNPOWDER) {
                    item.setEntityItemStack(ItemMaterial.getMaterial("niter", stack.stackSize));
                }
            }
        }
    }

    private boolean isNonDefaultArmor(EntityMob mob, ItemStack stack) {
        Item item = stack.getItem();
        if (mob instanceof EntitySkeleton) {
            if (item instanceof ItemBow || item instanceof ItemSword)
                return stack.hasTagCompound();
        } else if (mob instanceof EntityPigZombie) {
            if (item == Items.GOLDEN_SWORD) {
                return stack.hasTagCompound();
            }
        }
        return true;
    }

    private void createDamagedItem(LivingDropsEvent evt, ItemStack stack) {
        if (stack.isItemStackDamageable()) {
            stack.setItemDamage((int) (rand.nextFloat() * stack.getMaxDamage()));
        }
        addDrop(evt, stack);
    }

    public void addDrop(LivingDropsEvent evt, ItemStack drop) {
        EntityItem item = new EntityItem(evt.getEntityLiving().getEntityWorld(), evt.getEntityLiving().posX, evt.getEntityLiving().posY, evt.getEntityLiving().posZ, drop);
        item.setDefaultPickupDelay();
        evt.getDrops().add(item);
    }

    @SubscribeEvent
    public void shearCreeper(PlayerInteractEvent.EntityInteractSpecific e) {
        Entity creeper = e.getTarget();
        if (creeper instanceof EntityCreeper) {
            if (e.getSide().isServer() && creeper.isEntityAlive() && e.getItemStack() != null) {
                if (e.getItemStack().getItem() instanceof ItemShears) {
                    InvUtils.ejectStack(e.getWorld(), creeper.posX, creeper.posY, creeper.posZ, new ItemStack(BWMItems.CREEPER_OYSTER));
                    EntityShearedCreeper shearedCreeper = new EntityShearedCreeper(e.getWorld());
                    creeper.attackEntityFrom(new DamageSource(""), 0);
                    copyEntityInfo(creeper, shearedCreeper);
                    e.getWorld().playSound(null, shearedCreeper.posX, shearedCreeper.posY, shearedCreeper.posZ, SoundEvents.ENTITY_SLIME_JUMP, SoundCategory.HOSTILE, 1, 0.3F);
                    e.getWorld().playSound(null, shearedCreeper.posX, shearedCreeper.posY, shearedCreeper.posZ, SoundEvents.ENTITY_SHEEP_SHEAR, SoundCategory.HOSTILE, 1, 1F);
                    creeper.setDead();
                    e.getWorld().spawnEntity(shearedCreeper);
                }
            }
        }
    }

    public void copyEntityInfo(Entity copyFrom, Entity copyTo) {
        copyTo.setPositionAndRotation(copyFrom.posX, copyFrom.posY, copyFrom.posZ, copyFrom.rotationYaw, copyFrom.rotationPitch);
        copyTo.setRotationYawHead(copyFrom.getRotationYawHead());
    }
}
