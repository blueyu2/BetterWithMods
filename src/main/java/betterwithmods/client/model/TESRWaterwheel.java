package betterwithmods.client.model;

import betterwithmods.blocks.BlockMillGenerator;
import betterwithmods.blocks.tile.gen.TileEntityWaterwheel;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

public class TESRWaterwheel extends TileEntitySpecialRenderer<TileEntityWaterwheel> {
    private final ModelWaterwheel waterwheel;

    public TESRWaterwheel() {
        this.waterwheel = new ModelWaterwheel();
    }

    @Override
    public void renderTileEntityAt(TileEntityWaterwheel te, double x, double y, double z,
                                   float partialTicks, int destroyStage) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5D, y + 0.5D, z + 0.5D);
        this.bindTexture(new ResourceLocation("minecraft", "textures/blocks/planks_oak.png"));
        float rotation = (te.getCurrentRotation() + (te.getRunningState() == 0 ? 0 : partialTicks * te.getPrevRotation()));

        IBlockState state = te.getWorld().getBlockState(te.getPos());
        EnumFacing.Axis axis = EnumFacing.Axis.Y;
        if (state.getProperties().containsKey(BlockMillGenerator.AXIS)) {
            axis = state.getValue(BlockMillGenerator.AXIS);
        }

        switch (axis) {
            case X:
                waterwheel.setRotateAngle(waterwheel.axle, 0, 0, (float) Math.toRadians(rotation));
                GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
                break;
            case Z:
                waterwheel.setRotateAngle(waterwheel.axle, 0, 0, -(float) Math.toRadians(rotation));
                break;
            case Y:
            default:
                waterwheel.setRotateAngle(waterwheel.axle, 0, (float) Math.toRadians(rotation), 0);
                GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
                break;
        }

        this.waterwheel.render(0.0625F);
        GlStateManager.popMatrix();
    }

}
