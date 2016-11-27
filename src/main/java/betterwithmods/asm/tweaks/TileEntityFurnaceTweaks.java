package betterwithmods.asm.tweaks;

import betterwithmods.config.BWConfig;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.*;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by blueyu2 on 11/27/16.
 */
public class TileEntityFurnaceTweaks {

    private static List<FurnaceTime> cookTimes = new ArrayList<>();
    private static List<FurnaceTime> burnTimes = new ArrayList<>();

    public static void registerCookTime(ItemStack stack, int time) {
        cookTimes.add(new FurnaceTime(stack, time));
    }

    public static void registerCookTime(String ore, int time) {
        cookTimes.add(new FurnaceTime(ore, time));
    }

    public static List<FurnaceTime> getCookTimes() {
        return cookTimes;
    }

    public static void registerBurnTime(ItemStack stack, int time) {
        burnTimes.add(new FurnaceTime(stack, time));
    }

    public static void registerBurnTime(String ore, int time) {
        burnTimes.add(new FurnaceTime(ore, time));
    }

    public static List<FurnaceTime> getBurnTimes() {
        return burnTimes;
    }

    public static int getCookTime(ItemStack stack){
        if(!BWConfig.hardcoreSmelting)
            return 200;
        return getTime(cookTimes, stack, 200);
    }

    public static int getItemBurnTime(ItemStack stack) {
        if(!BWConfig.hardcoreSmelting)
            return getOriginalItemBurnTime(stack);
        return getTime(burnTimes, stack, getOriginalItemBurnTime(stack));
    }

    private static int getTime(List<FurnaceTime> times, ItemStack stack, int defaultTime) {
        if (stack != null) {
            for (FurnaceTime furnaceTime : times) {
                Object object = furnaceTime.getObject();
                int time = furnaceTime.getTime();
                if (object instanceof ItemStack) {
                    if (((ItemStack)object).isItemEqual(stack))
                        return time;
                }
                else if (object instanceof String) {
                    for (int id : OreDictionary.getOreIDs(stack)) {
                        String oreName = OreDictionary.getOreName(id);
                        if (oreName.equals(object))
                            return time;
                        String partialOre = (String) object;
                        int index = partialOre.indexOf('*');
                        if (index != -1) {
                            if(oreName.startsWith(partialOre.substring(0, index)) || oreName.endsWith(partialOre.substring(index + 1))){
                                return time;
                            }
                        }
                    }
                }
            }
        }
        return defaultTime;
    }

    //Copy of TileEntityFurnace method
    private static int getOriginalItemBurnTime(ItemStack stack)
    {
        if (stack == null)
        {
            return 0;
        }
        else
        {
            Item item = stack.getItem();

            if (item instanceof ItemBlock && Block.getBlockFromItem(item) != Blocks.AIR)
            {
                Block block = Block.getBlockFromItem(item);

                if (block == Blocks.WOODEN_SLAB)
                {
                    return 150;
                }

                if (block.getDefaultState().getMaterial() == Material.WOOD)
                {
                    return 300;
                }

                if (block == Blocks.COAL_BLOCK)
                {
                    return 16000;
                }
            }

            if (item instanceof ItemTool && "WOOD".equals(((ItemTool)item).getToolMaterialName())) return 200;
            if (item instanceof ItemSword && "WOOD".equals(((ItemSword)item).getToolMaterialName())) return 200;
            if (item instanceof ItemHoe && "WOOD".equals(((ItemHoe)item).getMaterialName())) return 200;
            if (item == Items.STICK) return 100;
            if (item == Items.COAL) return 1600;
            if (item == Items.LAVA_BUCKET) return 20000;
            if (item == Item.getItemFromBlock(Blocks.SAPLING)) return 100;
            if (item == Items.BLAZE_ROD) return 2400;
            return net.minecraftforge.fml.common.registry.GameRegistry.getFuelValue(stack);
        }
    }

    public static class FurnaceTime {
        private final Object object;
        private final int time;

        public FurnaceTime(Object object, int time) {
            this.object = object;
            this.time = time;
        }

        public Object getObject() {
            return object;
        }

        public int getTime() {
            return time;
        }
    }
}
