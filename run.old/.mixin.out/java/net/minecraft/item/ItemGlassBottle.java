package net.minecraft.item;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.transformer.meta.MixinMerged;

public class ItemGlassBottle extends Item {
    private static final String __OBFID = "CL_00001776";

    public ItemGlassBottle() {
        this.setCreativeTab(CreativeTabs.tabBrewing);
    }

    @SideOnly(Side.CLIENT)
    public IIcon getIconFromDamage(int p_77617_1_) {
        return Items.potionitem.getIconFromDamage(0);
    }

    public ItemStack onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer player) {
        MovingObjectPosition movingobjectposition = this.getMovingObjectPositionFromPlayer(worldIn, player, true);
        if (movingobjectposition == null) {
            return itemStackIn;
        } else {
            if (movingobjectposition.typeOfHit == MovingObjectType.BLOCK) {
                int i = movingobjectposition.blockX;
                int j = movingobjectposition.blockY;
                int k = movingobjectposition.blockZ;
                if (!worldIn.canMineBlock(player, i, j, k)) {
                    return itemStackIn;
                }

                if (!player.canPlayerEdit(i, j, k, movingobjectposition.sideHit, itemStackIn)) {
                    return itemStackIn;
                }

                if (this.modifyExpressionValue$zii000$hodgepodge$checkWaterBlock(worldIn.getBlock(i, j, k)).getMaterial() == Material.water) {
                    --itemStackIn.stackSize;
                    if (itemStackIn.stackSize <= 0) {
                        return new ItemStack(Items.potionitem);
                    }

                    if (!player.inventory.addItemStackToInventory(new ItemStack(Items.potionitem))) {
                        player.dropPlayerItemWithRandomChoice(new ItemStack(Items.potionitem, 1, 0), false);
                    }
                }
            }

            return itemStackIn;
        }
    }

    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister register) {
    }

    @MixinMerged(
        mixin = "com.mitchej123.hodgepodge.mixins.early.minecraft.MixinItemGlassBottle",
        priority = 1000,
        sessionId = "98d213e4-85ca-4ce4-959f-84858add440a"
    )
    private Block modifyExpressionValue$zii000$hodgepodge$checkWaterBlock(Block original) {
        return original == Blocks.water ? original : Blocks.cobblestone;
    }
}
