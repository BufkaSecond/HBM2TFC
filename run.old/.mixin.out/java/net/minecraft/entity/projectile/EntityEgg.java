package net.minecraft.entity.projectile;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import org.spongepowered.asm.mixin.transformer.meta.MixinMerged;
import org.spongepowered.asm.synthetic.args.Args.1;

public class EntityEgg extends EntityThrowable {
    private static final String __OBFID = "CL_00001724";

    public EntityEgg(World p_i1779_1_) {
        super(p_i1779_1_);
    }

    public EntityEgg(World p_i1780_1_, EntityLivingBase p_i1780_2_) {
        super(p_i1780_1_, p_i1780_2_);
    }

    public EntityEgg(World p_i1781_1_, double p_i1781_2_, double p_i1781_4_, double p_i1781_6_) {
        super(p_i1781_1_, p_i1781_2_, p_i1781_4_, p_i1781_6_);
    }

    protected void onImpact(MovingObjectPosition p_70184_1_) {
        if (p_70184_1_.entityHit != null) {
            p_70184_1_.entityHit.attackEntityFrom(DamageSource.causeThrownDamage(this, this.getThrower()), 0.0F);
        }

        if (!this.worldObj.isRemote && this.rand.nextInt(8) == 0) {
            byte b0 = 1;
            if (this.rand.nextInt(32) == 0) {
                b0 = 4;
            }

            for(int i = 0; i < b0; ++i) {
                EntityChicken entitychicken = new EntityChicken(this.worldObj);
                entitychicken.setGrowingAge(-24000);
                entitychicken.setLocationAndAngles(this.posX, this.posY, this.posZ, this.rotationYaw, 0.0F);
                this.worldObj.spawnEntityInWorld(entitychicken);
            }
        }

        for(int j = 0; j < 8; ++j) {
            World var10000 = this.worldObj;
            1 var10001 = 1.of("snowballpoof", this.posX, this.posY, this.posZ, 0.0D, 0.0D, 0.0D);
            this.args$zin000$hodgepodge$replaceSpawnParticleArgs(var10001);
            var10000.spawnParticle(var10001.$0(), var10001.$1(), var10001.$2(), var10001.$3(), var10001.$4(), var10001.$5(), var10001.$6());
        }

        if (!this.worldObj.isRemote) {
            this.setDead();
        }

    }

    @MixinMerged(
        mixin = "com.mitchej123.hodgepodge.mixins.early.minecraft.MixinEntityEgg",
        priority = 1000,
        sessionId = "98d213e4-85ca-4ce4-959f-84858add440a"
    )
    private void args$zin000$hodgepodge$replaceSpawnParticleArgs(Args args) {
        args.set(0, "iconcrack_" + Item.getIdFromItem(Items.egg));
        args.set(4, ((double)this.rand.nextFloat() - 0.5D) * 0.08D);
        args.set(5, ((double)this.rand.nextFloat() - 0.5D) * 0.08D);
        args.set(6, ((double)this.rand.nextFloat() - 0.5D) * 0.08D);
    }
}
