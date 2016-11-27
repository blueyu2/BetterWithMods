package betterwithmods.integration.minetweaker;

import betterwithmods.asm.tweaks.TileEntityFurnaceTweaks;
import betterwithmods.asm.tweaks.TileEntityFurnaceTweaks.*;
import betterwithmods.integration.minetweaker.utils.BaseListAddition;
import betterwithmods.integration.minetweaker.utils.BaseListRemoval;
import betterwithmods.integration.minetweaker.utils.LogHelper;
import betterwithmods.integration.minetweaker.utils.StackHelper;
import minetweaker.MineTweakerAPI;
import minetweaker.api.item.IIngredient;
import minetweaker.api.item.IItemStack;
import minetweaker.api.oredict.IOreDictEntry;
import net.minecraft.item.ItemStack;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.ArrayList;
import java.util.List;

import static betterwithmods.integration.minetweaker.utils.InputHelper.toIItemStack;

/**
 * Created by blueyu2 on 11/27/16.
 */
@ZenClass("mods.betterwithmods.Furnace")
public class Furnace {
    private final static List<FurnaceTime> cookTimes = TileEntityFurnaceTweaks.getCookTimes();
    private final static List<FurnaceTime> burnTimes = TileEntityFurnaceTweaks.getBurnTimes();

    @ZenMethod
    public static void addCookTime(IItemStack stack, int time) {
        MineTweakerAPI.apply(new Furnace.Add(cookTimes, new FurnaceTime(stack, time)));
    }

    @ZenMethod
    public static void addBurnTime(IItemStack stack, int time) {
        MineTweakerAPI.apply(new Furnace.Add(burnTimes, new FurnaceTime(stack, time)));
    }

    @ZenMethod
    public static void removeCookTime(IIngredient stack) {
        removeTime(cookTimes, stack);
    }

    @ZenMethod
    public static void removeBurnTime(IIngredient stack) {
        removeTime(burnTimes, stack);
    }

    private static void removeTime(List<FurnaceTime> times, IIngredient stack) {
        List<FurnaceTime> toRemove = new ArrayList<>();
        for (FurnaceTime time : times) {
            if(time.getObject() instanceof ItemStack && stack instanceof IItemStack){
                if (StackHelper.matches(stack, toIItemStack((ItemStack)time.getObject()))) {
                    toRemove.add(time);
                }
            }
            else if (time.getObject() instanceof String && stack instanceof IOreDictEntry) {
                if((time.getObject()).equals(((IOreDictEntry)stack).getName()))
                    toRemove.add(time);
            }
        }
        if (!toRemove.isEmpty()) {
            MineTweakerAPI.apply(new Remove(times, toRemove));
        } else {
            LogHelper.logWarning(String.format("No %s Recipe found for %s. Command ignored!", "steelAnvil", stack.toString()));
        }
    }

    public static class Add extends BaseListAddition<FurnaceTime> {

        protected Add(List<FurnaceTime> list, FurnaceTime time) {
            super("bwFurnace", list);
            list.add(time);
        }

        @Override
        protected String getRecipeInfo(FurnaceTime time) {
            return LogHelper.getStackDescription(time.getObject());
        }
    }

    public static class Remove extends BaseListRemoval<FurnaceTime> {

        protected Remove(List<FurnaceTime> list, List<FurnaceTime> toRemove) {
            super("bwFurnace", list, toRemove);
        }

        @Override
        protected String getRecipeInfo(FurnaceTime time) {
            return LogHelper.getStackDescription(time.getObject());
        }
    }
}
