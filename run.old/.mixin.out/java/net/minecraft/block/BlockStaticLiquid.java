package net.minecraft.block;

import java.util.Random;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.transformer.meta.MixinMerged;

public class BlockStaticLiquid extends BlockLiquid {
    private static final String __OBFID = "CL_00000315";

    protected BlockStaticLiquid(Material p_i45429_1_) {
        super(p_i45429_1_);
        this.setTickRandomly(false);
        if (p_i45429_1_ == Material.lava) {
            this.setTickRandomly(true);
        }

    }

    public void onNeighborBlockChange(World worldIn, int x, int y, int z, Block neighbor) {
        super.onNeighborBlockChange(worldIn, x, y, z, neighbor);
        if (worldIn.getBlock(x, y, z) == this) {
            this.setNotStationary(worldIn, x, y, z);
        }

    }

    private void setNotStationary(World p_149818_1_, int p_149818_2_, int p_149818_3_, int p_149818_4_) {
        int l = p_149818_1_.getBlockMetadata(p_149818_2_, p_149818_3_, p_149818_4_);
        p_149818_1_.setBlock(p_149818_2_, p_149818_3_, p_149818_4_, Block.getBlockById(Block.getIdFromBlock(this) - 1), l, 2);
        p_149818_1_.scheduleBlockUpdate(p_149818_2_, p_149818_3_, p_149818_4_, Block.getBlockById(Block.getIdFromBlock(this) - 1), this.tickRate(p_149818_1_));
    }

    public void updateTick(World worldIn, int x, int y, int z, Random random) {
        if (this.blockMaterial == Material.lava) {
            int l = random.nextInt(3);
            int i1 = 0;

            while(true) {
                if (i1 >= l) {
                    if (l == 0) {
                        i1 = x;
                        int k1 = z;

                        for(int j1 = 0; j1 < 3; ++j1) {
                            x = i1 + random.nextInt(3) - 1;
                            z = k1 + random.nextInt(3) - 1;
                            int injectorAllocatedLocal12 = y + 1;
                            if (this.redirect$zih000$hodgepodge$wrapperUpdateTickIsAirBlock(worldIn, x, injectorAllocatedLocal12, z) && this.isFlammable(worldIn, x, y, z)) {
                                worldIn.setBlock(x, y + 1, z, Blocks.fire);
                            }
                        }
                    }
                    break;
                }

                x += random.nextInt(3) - 1;
                ++y;
                z += random.nextInt(3) - 1;
                Block block = this.redirect$zih000$hodgepodge$wrapperUpdateTickGetBlock(worldIn, x, y, z);
                if (block.blockMaterial == Material.air) {
                    if (this.isFlammable(worldIn, x - 1, y, z) || this.isFlammable(worldIn, x + 1, y, z) || this.isFlammable(worldIn, x, y, z - 1) || this.isFlammable(worldIn, x, y, z + 1) || this.isFlammable(worldIn, x, y - 1, z) || this.isFlammable(worldIn, x, y + 1, z)) {
                        worldIn.setBlock(x, y, z, Blocks.fire);
                        return;
                    }
                } else if (block.blockMaterial.blocksMovement()) {
                    return;
                }

                ++i1;
            }
        }

    }

    private boolean isFlammable(World p_149817_1_, int p_149817_2_, int p_149817_3_, int p_149817_4_) {
        return this.redirect$zih000$hodgepodge$wrapperIsFlammableGetBlock(p_149817_1_, p_149817_2_, p_149817_3_, p_149817_4_).getMaterial().getCanBurn();
    }

    @MixinMerged(
        mixin = "com.mitchej123.hodgepodge.mixins.early.minecraft.MixinBlockStaticLiquid",
        priority = 1000,
        sessionId = "98d213e4-85ca-4ce4-959f-84858add440a"
    )
    public Block redirect$zih000$hodgepodge$wrapperUpdateTickGetBlock(World world, int x, int y, int z) {
        return (Block)(!world.blockExists(x, y, z) ? Blocks.grass : world.getBlock(x, y, z));
    }

    @MixinMerged(
        mixin = "com.mitchej123.hodgepodge.mixins.early.minecraft.MixinBlockStaticLiquid",
        priority = 1000,
        sessionId = "98d213e4-85ca-4ce4-959f-84858add440a"
    )
    public boolean redirect$zih000$hodgepodge$wrapperUpdateTickIsAirBlock(World world, int x, int y, int z) {
        return !world.blockExists(x, y, z) ? false : world.isAirBlock(x, y, z);
    }

    @MixinMerged(
        mixin = "com.mitchej123.hodgepodge.mixins.early.minecraft.MixinBlockStaticLiquid",
        priority = 1000,
        sessionId = "98d213e4-85ca-4ce4-959f-84858add440a"
    )
    public Block redirect$zih000$hodgepodge$wrapperIsFlammableGetBlock(World world, int x, int y, int z) {
        return !world.blockExists(x, y, z) ? Blocks.air : world.getBlock(x, y, z);
    }
}
