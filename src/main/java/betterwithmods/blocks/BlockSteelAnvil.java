package betterwithmods.blocks;

import betterwithmods.BWMod;
import betterwithmods.blocks.tile.TileEntitySteelAnvil;
import betterwithmods.client.BWCreativeTabs;
import betterwithmods.util.DirUtils;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/**
 * Created by blueyu2 on 11/21/16.
 */
public class BlockSteelAnvil extends BlockContainer {

    private static final AxisAlignedBB NS_AABB = new AxisAlignedBB(0.25F, 0.0F, 0.0F, 0.75F, 1.0F, 1.0F);
    private static final AxisAlignedBB EW_AABB = new AxisAlignedBB(0.0F, 0.0F, 0.25F, 1.0F, 1.0F, 0.75F);

    public BlockSteelAnvil() {
        super(Material.IRON);
        this.setCreativeTab(BWCreativeTabs.BWTAB);
        this.setHardness(5.0F);
        this.setResistance(2000.0F);
        this.setHarvestLevel("pickaxe", 1);
        this.setSoundType(SoundType.ANVIL);
        this.setDefaultState(this.blockState.getBaseState().withProperty(DirUtils.HORIZONTAL, EnumFacing.NORTH));
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing side, float flX, float flY, float flZ, int meta, EntityLivingBase living, ItemStack stack) {
        IBlockState state = super.getStateForPlacement(world, pos, side, flX, flY, flZ, meta, living, stack);
        if (side.ordinal() < 2)
            side = DirUtils.convertEntityOrientationToFlatFacing(living, side);
        return state.withProperty(DirUtils.HORIZONTAL, side);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        switch (state.getValue(DirUtils.HORIZONTAL)) {
            case NORTH:
            case SOUTH:
                return NS_AABB;
            case EAST:
            case WEST:
                return EW_AABB;
            default:
                return FULL_BLOCK_AABB;
        }
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(DirUtils.HORIZONTAL).getHorizontalIndex();
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(DirUtils.HORIZONTAL, EnumFacing.getHorizontal(meta));
    }

    @Override
    public BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, DirUtils.HORIZONTAL);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (world.isRemote)
            return true;
        else
            player.openGui(BWMod.instance, 0, world, pos.getX(), pos.getY(), pos.getZ());
        return true;
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntitySteelAnvil();
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        TileEntitySteelAnvil anvil = (TileEntitySteelAnvil) worldIn.getTileEntity(pos);
        InventoryHelper.dropInventoryItems(worldIn, pos, anvil);
        super.breakBlock(worldIn, pos, state);
    }
}
