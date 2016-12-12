package betterwithmods.client.model;

import betterwithmods.BWMod;
import betterwithmods.blocks.tile.TileEntityVessel;
import betterwithmods.client.model.render.RenderUtils;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.ResourceLocation;

/**
 * Created by blueyu2 on 12/11/16.
 */
public class TESRVessel extends TileEntitySpecialRenderer<TileEntityVessel> {
    private int occupiedXP;

    @Override
    public void renderTileEntityAt(TileEntityVessel tile, double x, double y, double z, float partialTicks, int destroyStage) {
        if (tile != null) {
            if (occupiedXP != tile.xp)
                occupiedXP = tile.xp;
            float fillOffset = 0.75F * occupationMod(tile);
            RenderUtils.renderFill(new ResourceLocation(BWMod.MODID, "blocks/vessel_contents"), tile.getPos(), x, y, z, 0.123D, 0.125D, 0.123D, 0.877D, 0.248D + fillOffset, 0.877D);
        }
    }

    private float occupationMod(TileEntityVessel tile) {
        return (float) occupiedXP / tile.xpMax;
    }
}
