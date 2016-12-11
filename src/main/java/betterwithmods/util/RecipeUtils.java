package betterwithmods.util;

import betterwithmods.BWCrafting;
import betterwithmods.BWMod;
import betterwithmods.craft.bulk.*;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.oredict.OreDictionary;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

public final class RecipeUtils {
    private RecipeUtils() {
    }

    public static void removeFurnaceRecipe(ItemStack input) {
        Iterator<Map.Entry<ItemStack, ItemStack>> iter = FurnaceRecipes.instance().getSmeltingList().entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<ItemStack, ItemStack> next = iter.next();
            //for some reason mojang put fucking wildcard for their ore meta
            if (next.getKey().isItemEqual(input) || (next.getKey().getItem() == input.getItem() && next.getKey().getMetadata() == OreDictionary.WILDCARD_VALUE)) {
                iter.remove();
            }
        }
    }

    public static IBlockState getStateFromStack(ItemStack stack) {
        if (stack != null && stack.getItem() instanceof ItemBlock) {
            return ((ItemBlock) stack.getItem()).getBlock().getStateFromMeta(stack.getMetadata());
        }
        return Blocks.AIR.getDefaultState();
    }

    /**
     * Remove all recipes.
     *
     * @param item Item to remove recipes of.
     * @param meta Metavalue.
     *             If {@link OreDictionary#WILDCARD_VALUE} all recipes of the item will be removed.
     */
    public static void removeRecipes(Item item, int meta) {
        removeRecipes(new ItemStack(item, 1, meta));
    }

    /**
     * Remove all recipes.
     *
     * @param block Block to remove recipes of.
     */
    public static void removeRecipes(Block block) {
        removeRecipes(new ItemStack(block));
    }

    /**
     * Remove all recipes.
     *
     * @param stack ItemStack to remove recipes of.
     */
    public static void removeRecipes(ItemStack stack) {
        List<IRecipe> recipeList = CraftingManager.getInstance().getRecipeList();
        final ListIterator<IRecipe> li = recipeList.listIterator();
        boolean found = false;
        while (li.hasNext()) {
            ItemStack output = li.next().getRecipeOutput();
            if (OreDictionary.itemMatches(stack, output, false)) {
                li.remove();
                found = true;
            }
        }
        if (!found)
            BWMod.logger.error("No matching recipe found.");

    }

    public static void gatherCookableFood() {
        Map<ItemStack, ItemStack> furnace = FurnaceRecipes.instance().getSmeltingList();

        for (ItemStack input : furnace.keySet()) {
            if (input != null) {
                if (input.getItem() instanceof ItemFood && input.getItem() != Items.BREAD) {
                    ItemStack output = FurnaceRecipes.instance().getSmeltingResult(input);
                    if (output != null) {
                        BWCrafting.addCauldronRecipe(output.copy(), new ItemStack[]{input.copy()});
                    }
                }
            }
        }
    }

    public static void refreshRecipes() {
        CraftingManagerCauldron.getInstance().refreshRecipes();
        CraftingManagerCauldronStoked.getInstance().refreshRecipes();
        CraftingManagerCrucible.getInstance().refreshRecipes();
        CraftingManagerCrucibleStoked.getInstance().refreshRecipes();
        CraftingManagerMill.getInstance().refreshRecipes();
    }
}
