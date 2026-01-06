package net.minecraft.block;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemLead;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.transformer.meta.MixinMerged;

public class BlockFence extends Block {
    private final String field_149827_a;
    private static final String __OBFID = "CL_00000242";

    public BlockFence(String p_i45406_1_, Material p_i45406_2_) {
        super(p_i45406_2_);
        this.field_149827_a = p_i45406_1_;
        this.setCreativeTab(CreativeTabs.tabDecorations);
    }

    public void addCollisionBoxesToList(World worldIn, int x, int y, int z, AxisAlignedBB mask, List list, Entity collider) {
        boolean flag = this.canConnectFenceTo(worldIn, x, y, z - 1);
        boolean flag1 = this.canConnectFenceTo(worldIn, x, y, z + 1);
        boolean flag2 = this.canConnectFenceTo(worldIn, x - 1, y, z);
        boolean flag3 = this.canConnectFenceTo(worldIn, x + 1, y, z);
        float f = 0.375F;
        float f1 = 0.625F;
        float f2 = 0.375F;
        float f3 = 0.625F;
        if (flag) {
            f2 = 0.0F;
        }

        if (flag1) {
            f3 = 1.0F;
        }

        if (flag || flag1) {
            this.setBlockBounds(f, 0.0F, f2, f1, 1.5F, f3);
            super.addCollisionBoxesToList(worldIn, x, y, z, mask, list, collider);
        }

        f2 = 0.375F;
        f3 = 0.625F;
        if (flag2) {
            f = 0.0F;
        }

        if (flag3) {
            f1 = 1.0F;
        }

        if (flag2 || flag3 || !flag && !flag1) {
            this.setBlockBounds(f, 0.0F, f2, f1, 1.5F, f3);
            super.addCollisionBoxesToList(worldIn, x, y, z, mask, list, collider);
        }

        if (flag) {
            f2 = 0.0F;
        }

        if (flag1) {
            f3 = 1.0F;
        }

        this.setBlockBounds(f, 0.0F, f2, f1, 1.0F, f3);
    }

    public void setBlockBoundsBasedOnState(IBlockAccess worldIn, int x, int y, int z) {
        boolean flag = this.canConnectFenceTo(worldIn, x, y, z - 1);
        boolean flag1 = this.canConnectFenceTo(worldIn, x, y, z + 1);
        boolean flag2 = this.canConnectFenceTo(worldIn, x - 1, y, z);
        boolean flag3 = this.canConnectFenceTo(worldIn, x + 1, y, z);
        float f = 0.375F;
        float f1 = 0.625F;
        float f2 = 0.375F;
        float f3 = 0.625F;
        if (flag) {
            f2 = 0.0F;
        }

        if (flag1) {
            f3 = 1.0F;
        }

        if (flag2) {
            f = 0.0F;
        }

        if (flag3) {
            f1 = 1.0F;
        }

        this.setBlockBounds(f, 0.0F, f2, f1, 1.0F, f3);
    }

    public boolean isOpaqueCube() {
        return false;
    }

    public boolean renderAsNormalBlock() {
        return false;
    }

    public boolean getBlocksMovement(IBlockAccess worldIn, int x, int y, int z) {
        return false;
    }

    public int getRenderType() {
        return 11;
    }

    public static boolean func_149825_a(Block p_149825_0_) {
        return p_149825_0_ == Blocks.fence || p_149825_0_ == Blocks.nether_brick_fence;
    }

    @SideOnly(Side.CLIENT)
    public boolean shouldSideBeRendered(IBlockAccess worldIn, int x, int y, int z, int side) {
        return true;
    }

    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister reg) {
        this.blockIcon = reg.registerIcon(this.field_149827_a);
    }

    public boolean onBlockActivated(World worldIn, int x, int y, int z, EntityPlayer player, int side, float subX, float subY, float subZ) {
        return worldIn.isRemote ? true : ItemLead.func_150909_a(player, worldIn, x, y, z);
    }

    @Overwrite
    @MixinMerged(
        mixin = "com.mitchej123.hodgepodge.mixins.early.minecraft.MixinBlockFence",
        priority = 1000,
        sessionId = "98d213e4-85ca-4ce4-959f-84858add440a"
    )
    public boolean canConnectFenceTo(IBlockAccess world, int x, int y, int z) {
        Block block = world.getBlock(x, y, z);
        return block == this || block instanceof BlockFence && block.getMaterial() == this.getMaterial() || block instanceof BlockFenceGate || block.getMaterial().isOpaque() && block.renderAsNormalBlock() && block.getMaterial() != Material.gourd;
    }
}
