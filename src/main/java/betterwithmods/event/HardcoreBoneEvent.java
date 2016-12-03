package betterwithmods.event;

import betterwithmods.BWMBlocks;
import betterwithmods.config.BWConfig;
import net.minecraft.block.BlockFarmland;
import net.minecraft.block.BlockGrass;
import net.minecraft.block.BlockSapling;
import net.minecraft.block.IGrowable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.BonemealEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Created by blueyu2 on 12/2/16.
 */
public class HardcoreBoneEvent {
    @SubscribeEvent
    public void applyBonemeal(BonemealEvent event) {
        if(!BWConfig.hardcoreBone)
            return;

        World world = event.getWorld();
        BlockPos pos = event.getPos();
        IBlockState state = event.getBlock();

        if(state.getBlock() instanceof IGrowable && !(state.getBlock() instanceof BlockGrass)) {
            BlockPos farmPos = pos.down();
            if(world.getBlockState(farmPos).getBlock() == Blocks.FARMLAND) {
                fertilizeFarmland(world, farmPos, world.getBlockState(farmPos));
                event.setResult(Event.Result.ALLOW);
            }
            else
                event.setCanceled(true);
        }
        else if(state.getBlock() == Blocks.FARMLAND) {
            fertilizeFarmland(world, pos, state);
            event.setResult(Event.Result.ALLOW);
        }
        else if(state.getBlock() instanceof BlockSapling)
            event.setCanceled(true);
    }

    private void fertilizeFarmland(World world, BlockPos pos, IBlockState farmState){
        world.setBlockState(pos, BWMBlocks.FERTILE_FARMLAND.getDefaultState().withProperty(BlockFarmland.MOISTURE, farmState.getValue(BlockFarmland.MOISTURE)));
    }
}
