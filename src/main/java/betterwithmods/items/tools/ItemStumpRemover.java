package betterwithmods.items.tools;

import betterwithmods.blocks.BlockStump;
import betterwithmods.client.BWCreativeTabs;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * @author Koward
 */
public class ItemStumpRemover extends Item {
    public ItemStumpRemover() {
        super();
        this.setCreativeTab(BWCreativeTabs.BWTAB);
        this.setMaxDamage(0);
        this.setHasSubtypes(false);
        this.maxStackSize = 16;
    }

    @Override
    public EnumActionResult onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (playerIn != null && !playerIn.canPlayerEdit(pos, facing, stack)) {
            return EnumActionResult.FAIL;
        } else if (stack.stackSize == 0) {
            return EnumActionResult.FAIL;
        } else {
            IBlockState state = worldIn.getBlockState(pos);
            if (state.getBlock() instanceof BlockStump) {
                if (!worldIn.isRemote) {
                    worldIn.destroyBlock(pos, true);
                    //TODO custom sound
                }

                --stack.stackSize;
                return EnumActionResult.SUCCESS;
            } else {
                return EnumActionResult.FAIL;
            }
        }
    }
}
