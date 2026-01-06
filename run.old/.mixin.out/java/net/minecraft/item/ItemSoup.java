package net.minecraft.item;

import com.llamalad7.mixinextras.sugar.SugarBridge;
import com.llamalad7.mixinextras.sugar.impl.ref.generated.LocalRefImpl;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.transformer.meta.MixinMerged;

public class ItemSoup extends ItemFood {
    private static final String __OBFID = "CL_00001778";

    public ItemSoup(int p_i45330_1_) {
        super(p_i45330_1_, false);
        this.setMaxStackSize(1);
    }

    public ItemStack onEaten(ItemStack p_77654_1_, World p_77654_2_, EntityPlayer p_77654_3_) {
        this.handler$zdn000$hodgepodge$fixStackDeletion(p_77654_1_, p_77654_2_, p_77654_3_, (CallbackInfoReturnable)null);
        super.onEaten(p_77654_1_, p_77654_2_, p_77654_3_);
        Item var10001 = Items.bowl;
        LocalRefImpl ref4 = new LocalRefImpl();
        ref4.init(p_77654_1_);
        ItemStack var10000 = this.redirect$zdn000$hodgepodge$returnStew$mixinextras$bridge$4(var10001, ref4);
        p_77654_1_ = (ItemStack)ref4.dispose();
        if (var10000 == null) {
            throw new NullPointerException("@Redirect constructor handler net/minecraft/item/ItemSoup::hodgepodge$returnStew returned null for net.minecraft.item.ItemStack");
        } else {
            return var10000;
        }
    }

    @MixinMerged(
        mixin = "com.mitchej123.hodgepodge.mixins.early.minecraft.MixinItemSoup",
        priority = 1000,
        sessionId = "98d213e4-85ca-4ce4-959f-84858add440a"
    )
    private void handler$zdn000$hodgepodge$fixStackDeletion(ItemStack p_77654_1_, World p_77654_2_, EntityPlayer p_77654_3_, CallbackInfoReturnable<ItemStack> cir) {
        ItemStack emptyBowl = new ItemStack(Items.bowl);
        if (!p_77654_3_.inventory.addItemStackToInventory(emptyBowl)) {
            p_77654_3_.dropPlayerItemWithRandomChoice(emptyBowl, true);
        }

    }

    @MixinMerged(
        mixin = "com.mitchej123.hodgepodge.mixins.early.minecraft.MixinItemSoup",
        priority = 1000,
        sessionId = "98d213e4-85ca-4ce4-959f-84858add440a"
    )
    private ItemStack redirect$zdn000$hodgepodge$returnStew(Item p_i1879_1_, ItemStack p_77654_1_) {
        return p_77654_1_;
    }

    @MixinMerged(
        mixin = "com.mitchej123.hodgepodge.mixins.early.minecraft.MixinItemSoup",
        priority = 1000,
        sessionId = "98d213e4-85ca-4ce4-959f-84858add440a"
    )
    @SugarBridge
    private ItemStack redirect$zdn000$hodgepodge$returnStew$mixinextras$bridge$4(Item var1, LocalRef var2) {
        return this.redirect$zdn000$hodgepodge$returnStew(var1, (ItemStack)var2.get());
    }
}
