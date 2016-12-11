package betterwithmods.client.model;

import betterwithmods.blocks.BlockMillGenerator;
import betterwithmods.blocks.tile.gen.TileEntityWindmillHorizontal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

public class TESRWindmill extends TileEntitySpecialRenderer<TileEntityWindmillHorizontal> {
    private final ModelWindmillShafts shafts = new ModelWindmillShafts();
    private final ModelWindmillSail sail = new ModelWindmillSail();

    @Override
    public void renderTileEntityAt(TileEntityWindmillHorizontal te, double x, double y, double z,
                                   float partialTicks, int destroyStage) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5D, y + 0.5D, z + 0.5D);

        float rotation = (te.getCurrentRotation() + (te.getRunningState() == 0 ? 0 : partialTicks * te.getPrevRotation()));

        IBlockState state = te.getWorld().getBlockState(te.getPos());
        EnumFacing.Axis axis = EnumFacing.Axis.Y;
        if (state.getProperties().containsKey(BlockMillGenerator.AXIS)) {
            axis = state.getValue(BlockMillGenerator.AXIS);
        }
        switch (axis) {
            case X:
                shafts.setRotateAngle(shafts.axle, 0, 0, -(float) Math.toRadians(rotation));
                sail.setRotateAngleForSails(0, 0, -(float) Math.toRadians(rotation));
                GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
                break;
            case Z:
                shafts.setRotateAngle(shafts.axle, 0, 0, -(float) Math.toRadians(rotation));
                sail.setRotateAngleForSails(0, 0, -(float) Math.toRadians(rotation));
                GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
                break;
            case Y:
            default:
                GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
                break;
        }

        this.bindTexture(new ResourceLocation("minecraft", "textures/blocks/planks_oak.png"));
        this.shafts.render(0.0625F);
        this.bindTexture(new ResourceLocation("minecraft", "textures/blocks/wool_colored_white.png"));
        this.sail.render(0.0625F, te);
        GlStateManager.popMatrix();
    }

}
