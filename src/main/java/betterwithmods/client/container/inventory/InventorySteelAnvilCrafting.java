package betterwithmods.client.container.inventory;

import betterwithmods.blocks.tile.TileEntitySteelAnvil;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;

/**
 * Created by blueyu2 on 11/22/16.
 */
public class InventorySteelAnvilCrafting extends InventoryCrafting {
    private TileEntitySteelAnvil anvil;
    private Container container;

    public InventorySteelAnvilCrafting(Container containerIn, TileEntitySteelAnvil anvilIn) {
        super(containerIn, 4, 4);
        anvil = anvilIn;
        container = containerIn;
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        return index >= this.getSizeInventory() ? null : anvil.getStackInSlot(index);
    }

    @Override
    public ItemStack getStackInRowAndColumn(int row, int column) {
        return row >= 0 && row < 4 && column >= 0 && column <= 4 ? this.getStackInSlot(row + column * 4) : null;
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        ItemStack stack = anvil.getStackInSlot(index);
        if (stack != null) {
            ItemStack newStack;
            if (stack.stackSize <= count) {
                newStack = stack.copy();
                anvil.setInventorySlotContents(index, null);
                this.container.onCraftMatrixChanged(this);
                return newStack;
            } else {
                newStack = stack.splitStack(count);
                if (stack.stackSize == 0)
                    anvil.setInventorySlotContents(index, null);
                this.container.onCraftMatrixChanged(this);
                return newStack;
            }
        } else
            return null;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        anvil.setInventorySlotContents(index, stack);
        this.container.onCraftMatrixChanged(this);
    }
}
