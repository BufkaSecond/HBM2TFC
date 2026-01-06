package net.minecraft.block;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.Random;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.IIcon;
import net.minecraft.world.ColorizerGrass;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.transformer.meta.MixinMerged;

public class BlockGrass extends Block implements IGrowable {
    private static final Logger logger = LogManager.getLogger();
    @SideOnly(Side.CLIENT)
    private IIcon field_149991_b;
    @SideOnly(Side.CLIENT)
    private IIcon field_149993_M;
    @SideOnly(Side.CLIENT)
    private IIcon field_149994_N;
    private static final String __OBFID = "CL_00000251";

    protected BlockGrass() {
        super(Material.grass);
        this.setTickRandomly(true);
        this.setCreativeTab(CreativeTabs.tabBlock);
    }

    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        return side == 1 ? this.field_149991_b : (side == 0 ? Blocks.dirt.getBlockTextureFromSide(side) : this.blockIcon);
    }

    public Item getItemDropped(int meta, Random random, int fortune) {
        return Blocks.dirt.getItemDropped(0, random, fortune);
    }

    public boolean func_149851_a(World worldIn, int x, int y, int z, boolean isClient) {
        return true;
    }

    public boolean func_149852_a(World worldIn, Random random, int x, int y, int z) {
        return true;
    }

    @SideOnly(Side.CLIENT)
    public IIcon getIcon(IBlockAccess worldIn, int x, int y, int z, int side) {
        if (side == 1) {
            return this.field_149991_b;
        } else if (side == 0) {
            return Blocks.dirt.getBlockTextureFromSide(side);
        } else {
            Material material = worldIn.getBlock(x, y + 1, z).getMaterial();
            return material != Material.snow && material != Material.craftedSnow ? this.blockIcon : this.field_149993_M;
        }
    }

    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister reg) {
        this.blockIcon = reg.registerIcon(this.getTextureName() + "_side");
        this.field_149991_b = reg.registerIcon(this.getTextureName() + "_top");
        this.field_149993_M = reg.registerIcon(this.getTextureName() + "_side_snowed");
        this.field_149994_N = reg.registerIcon(this.getTextureName() + "_side_overlay");
    }

    @SideOnly(Side.CLIENT)
    public int getBlockColor() {
        double d0 = 0.5D;
        double d1 = 1.0D;
        return ColorizerGrass.getGrassColor(d0, d1);
    }

    @SideOnly(Side.CLIENT)
    public int getRenderColor(int meta) {
        return this.getBlockColor();
    }

    @SideOnly(Side.CLIENT)
    public int colorMultiplier(IBlockAccess worldIn, int x, int y, int z) {
        int l = 0;
        int i1 = 0;
        int j1 = 0;

        for(int k1 = -1; k1 <= 1; ++k1) {
            for(int l1 = -1; l1 <= 1; ++l1) {
                int i2 = worldIn.getBiomeGenForCoords(x + l1, z + k1).getBiomeGrassColor(x + l1, y, z + k1);
                l += (i2 & 16711680) >> 16;
                i1 += (i2 & '\uff00') >> 8;
                j1 += i2 & 255;
            }
        }

        return (l / 9 & 255) << 16 | (i1 / 9 & 255) << 8 | j1 / 9 & 255;
    }

    @SideOnly(Side.CLIENT)
    public static IIcon getIconSideOverlay() {
        return Blocks.grass.field_149994_N;
    }

    public void func_149853_b(World worldIn, Random random, int x, int y, int z) {
        label34:
        for(int l = 0; l < 128; ++l) {
            int i1 = x;
            int j1 = y + 1;
            int k1 = z;

            for(int l1 = 0; l1 < l / 16; ++l1) {
                i1 += random.nextInt(3) - 1;
                j1 += (random.nextInt(3) - 1) * random.nextInt(3) / 2;
                k1 += random.nextInt(3) - 1;
                if (worldIn.getBlock(i1, j1 - 1, k1) != Blocks.grass || worldIn.getBlock(i1, j1, k1).isNormalCube()) {
                    continue label34;
                }
            }

            if (worldIn.getBlock(i1, j1, k1).blockMaterial == Material.air) {
                if (random.nextInt(8) != 0) {
                    if (Blocks.tallgrass.canBlockStay(worldIn, i1, j1, k1)) {
                        worldIn.setBlock(i1, j1, k1, Blocks.tallgrass, 1, 3);
                    }
                } else {
                    worldIn.getBiomeGenForCoords(i1, k1).plantFlower(worldIn, random, i1, j1, k1);
                }
            }
        }

    }

    @Overwrite
    @MixinMerged(
        mixin = "com.mitchej123.hodgepodge.mixins.early.minecraft.MixinBlockGrass",
        priority = 1000,
        sessionId = "98d213e4-85ca-4ce4-959f-84858add440a"
    )
    public void updateTick(World worldIn, int x, int y, int z, Random random) {
        if (!worldIn.isRemote) {
            int blockLightValue = worldIn.getBlockLightValue(x, y + 1, z);
            if (blockLightValue < 4 && worldIn.getBlockLightOpacity(x, y + 1, z) > 2) {
                worldIn.setBlock(x, y, z, Blocks.dirt);
            } else if (blockLightValue >= 9) {
                for(int i = 0; i < 4; ++i) {
                    int targetX = x + random.nextInt(3) - 1;
                    int targetY = y + random.nextInt(5) - 3;
                    int targetZ = z + random.nextInt(3) - 1;
                    if ((targetX != x || targetZ != z || targetY != y && targetY != y - 1) && worldIn.blockExists(targetX, targetY, targetZ) && worldIn.getBlock(targetX, targetY, targetZ) == Blocks.dirt && worldIn.getBlockMetadata(targetX, targetY, targetZ) == 0 && worldIn.getBlockLightValue(targetX, targetY + 1, targetZ) >= 4 && worldIn.getBlockLightOpacity(targetX, targetY + 1, targetZ) <= 2) {
                        worldIn.setBlock(targetX, targetY, targetZ, Blocks.grass);
                    }
                }
            }

        }
    }
}
