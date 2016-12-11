package betterwithmods.event;

import betterwithmods.BWMItems;
import betterwithmods.config.BWConfig;
import betterwithmods.util.InvUtils;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.projectile.EntityEgg;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.event.entity.ThrowableImpactEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Random;

/**
 * Created by blueyu2 on 11/16/16.
 */
public class EggImpactEvent {
    @SubscribeEvent
    public void getRawEgg(ThrowableImpactEvent event) {
        if (event.getEntityThrowable() instanceof EntityEgg && BWConfig.rawEggDrop) {
            event.setCanceled(true);
            RayTraceResult result = event.getRayTraceResult();
            EntityThrowable egg = event.getEntityThrowable();
            Random rand = egg.getEntityWorld().rand;
            if (result.entityHit != null) {
                result.entityHit.attackEntityFrom(DamageSource.causeThrownDamage(egg, egg.getThrower()), 0.0F);
            }

            if (!egg.getEntityWorld().isRemote) {
                if (rand.nextInt(8) == 0) {
                    int i = 1;

                    if (rand.nextInt(32) == 0) {
                        i = 4;
                    }

                    for (int j = 0; j < i; ++j) {
                        EntityChicken entitychicken = new EntityChicken(egg.getEntityWorld());
                        entitychicken.setGrowingAge(-24000);
                        entitychicken.setLocationAndAngles(egg.posX, egg.posY, egg.posZ, egg.rotationYaw, 0.0F);
                        egg.getEntityWorld().spawnEntity(entitychicken);
                    }
                } else {
                    InvUtils.ejectStack(egg.getEntityWorld(), egg.posX, egg.posY, egg.posZ, new ItemStack(BWMItems.RAW_EGG));
                }
            }

            for (int k = 0; k < 8; ++k) {
                egg.getEntityWorld().spawnParticle(EnumParticleTypes.ITEM_CRACK, egg.posX, egg.posY, egg.posZ, ((double) rand.nextFloat() - 0.5D) * 0.08D, ((double) rand.nextFloat() - 0.5D) * 0.08D, ((double) rand.nextFloat() - 0.5D) * 0.08D, Item.getIdFromItem(Items.EGG));
            }

            if (!egg.getEntityWorld().isRemote) {
                egg.setDead();
            }
        }
    }
}
