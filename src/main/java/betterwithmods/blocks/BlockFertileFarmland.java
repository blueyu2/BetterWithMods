package betterwithmods.blocks;

import net.minecraft.block.BlockFarmland;
import net.minecraft.block.IGrowable;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.common.IPlantable;

import java.util.Random;

public class BlockFertileFarmland extends BlockFarmland {
    public BlockFertileFarmland() {
        super();
        this.setHardness(0.6F);
        this.setSoundType(SoundType.GROUND);
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
        super.updateTick(world, pos, state, rand);

        IBlockState above = world.getBlockState(pos.up());
        if (above.getBlock() instanceof IPlantable && canSustainPlant(above, world, pos, EnumFacing.UP, (IPlantable) above.getBlock())) {
            for (int i = 0; i < 2; i++)
                above.getBlock().updateTick(world, pos.up(), above, rand);
            if (isCropDone(world, pos.up(), above))
                world.setBlockState(pos, Blocks.FARMLAND.getDefaultState().withProperty(MOISTURE, this.getMetaFromState(state) & 7));
        }
    }

    private boolean isCropDone(World world, BlockPos pos, IBlockState state) {
        if (state.getBlock() instanceof BlockHemp && state.getValue(BlockHemp.AGE) == 7)
            return true;
        else if (state.getBlock() instanceof IGrowable && !((IGrowable) state.getBlock()).canGrow(world, pos, state, world.isRemote))
            return true;

        return false;
    }

    @Override
    public boolean canSustainPlant(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing direction, IPlantable plantable) {
        return plantable.getPlantType(world, pos.up()) == EnumPlantType.Crop || plantable.getPlantType(world, pos.up()) == EnumPlantType.Plains;
    }
}
