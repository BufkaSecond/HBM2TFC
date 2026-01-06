package net.minecraft.block;

import com.gtnewhorizon.gtnhlib.GTNHLib;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperationRuntime;
import com.llamalad7.mixinextras.sugar.SugarBridge;
import com.llamalad7.mixinextras.sugar.impl.ref.generated.LocalRefImpl;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.Iterator;
import java.util.Random;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.EntityPlayer.EnumStatus;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.Direction;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import org.spongepowered.asm.mixin.transformer.meta.MixinMerged;

public class BlockBed extends BlockDirectional {
    public static final int[][] field_149981_a = new int[][]{{0, 1}, {-1, 0}, {0, -1}, {1, 0}};
    @SideOnly(Side.CLIENT)
    private IIcon[] field_149980_b;
    @SideOnly(Side.CLIENT)
    private IIcon[] field_149982_M;
    @SideOnly(Side.CLIENT)
    private IIcon[] field_149983_N;
    private static final String __OBFID = "CL_00000198";

    public BlockBed() {
        super(Material.cloth);
        this.func_149978_e();
    }

    public boolean onBlockActivated(World worldIn, int x, int y, int z, EntityPlayer player, int side, float subX, float subY, float subZ) {
        if (worldIn.isRemote) {
            return true;
        } else {
            int i1 = worldIn.getBlockMetadata(x, y, z);
            if (!isBlockHeadOfBed(i1)) {
                int j1 = getDirection(i1);
                x += field_149981_a[j1][0];
                z += field_149981_a[j1][1];
                if (worldIn.getBlock(x, y, z) != this) {
                    return true;
                }

                i1 = worldIn.getBlockMetadata(x, y, z);
            }

            if (worldIn.provider.canRespawnHere() && worldIn.getBiomeGenForCoords(x, z) != BiomeGenBase.hell) {
                ChatComponentTranslation injectorAllocatedLocal19;
                if (func_149976_c(i1)) {
                    EntityPlayer entityplayer1 = null;
                    Iterator iterator = worldIn.playerEntities.iterator();

                    while(iterator.hasNext()) {
                        EntityPlayer entityplayer2 = (EntityPlayer)iterator.next();
                        if (entityplayer2.isPlayerSleeping()) {
                            ChunkCoordinates chunkcoordinates = entityplayer2.playerLocation;
                            if (chunkcoordinates.posX == x && chunkcoordinates.posY == y && chunkcoordinates.posZ == z) {
                                entityplayer1 = entityplayer2;
                            }
                        }
                    }

                    if (entityplayer1 != null) {
                        injectorAllocatedLocal19 = new ChatComponentTranslation("tile.bed.occupied", new Object[0]);
                        this.redirect$zfn000$hodgepodge$sendMessageAboveHotbar(player, injectorAllocatedLocal19);
                        return true;
                    }

                    func_149979_a(worldIn, x, y, z, false);
                }

                Operation var10005 = (var0) -> {
                    WrapOperationRuntime.checkArgumentCount(var0, 4, "[net.minecraft.entity.player.EntityPlayer, int, int, int]");
                    return ((EntityPlayer)var0[0]).sleepInBedAt((Integer)var0[1], (Integer)var0[2], (Integer)var0[3]);
                };
                LocalRefImpl ref23 = new LocalRefImpl();
                ref23.init(worldIn);
                EnumStatus var10000 = this.wrapOperation$zfo000$hodgepodge$setSpawn$mixinextras$bridge$22(player, x, y, z, var10005, ref23);
                worldIn = (World)ref23.dispose();
                EnumStatus enumstatus = var10000;
                if (enumstatus == EnumStatus.OK) {
                    func_149979_a(worldIn, x, y, z, true);
                    return true;
                } else {
                    if (enumstatus == EnumStatus.NOT_POSSIBLE_NOW) {
                        injectorAllocatedLocal19 = new ChatComponentTranslation("tile.bed.noSleep", new Object[0]);
                        this.redirect$zfn000$hodgepodge$sendMessageAboveHotbar(player, injectorAllocatedLocal19);
                    } else if (enumstatus == EnumStatus.NOT_SAFE) {
                        injectorAllocatedLocal19 = new ChatComponentTranslation("tile.bed.notSafe", new Object[0]);
                        this.redirect$zfn000$hodgepodge$sendMessageAboveHotbar(player, injectorAllocatedLocal19);
                    }

                    return true;
                }
            } else {
                double d2 = (double)x + 0.5D;
                double d0 = (double)y + 0.5D;
                double d1 = (double)z + 0.5D;
                worldIn.setBlockToAir(x, y, z);
                int k1 = getDirection(i1);
                x += field_149981_a[k1][0];
                z += field_149981_a[k1][1];
                if (worldIn.getBlock(x, y, z) == this) {
                    worldIn.setBlockToAir(x, y, z);
                    d2 = (d2 + (double)x + 0.5D) / 2.0D;
                    d0 = (d0 + (double)y + 0.5D) / 2.0D;
                    d1 = (d1 + (double)z + 0.5D) / 2.0D;
                }

                worldIn.newExplosion((Entity)null, (double)((float)x + 0.5F), (double)((float)y + 0.5F), (double)((float)z + 0.5F), 5.0F, true, true);
                return true;
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        if (side == 0) {
            return Blocks.planks.getBlockTextureFromSide(side);
        } else {
            int k = getDirection(meta);
            int l = Direction.bedDirection[k][side];
            int i1 = isBlockHeadOfBed(meta) ? 1 : 0;
            return i1 == 1 && l == 2 || i1 == 0 && l == 3 ? this.field_149980_b[i1] : (l != 5 && l != 4 ? this.field_149983_N[i1] : this.field_149982_M[i1]);
        }
    }

    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister reg) {
        this.field_149983_N = new IIcon[]{reg.registerIcon(this.getTextureName() + "_feet_top"), reg.registerIcon(this.getTextureName() + "_head_top")};
        this.field_149980_b = new IIcon[]{reg.registerIcon(this.getTextureName() + "_feet_end"), reg.registerIcon(this.getTextureName() + "_head_end")};
        this.field_149982_M = new IIcon[]{reg.registerIcon(this.getTextureName() + "_feet_side"), reg.registerIcon(this.getTextureName() + "_head_side")};
    }

    public int getRenderType() {
        return 14;
    }

    public boolean renderAsNormalBlock() {
        return false;
    }

    public boolean isOpaqueCube() {
        return false;
    }

    public void setBlockBoundsBasedOnState(IBlockAccess worldIn, int x, int y, int z) {
        this.func_149978_e();
    }

    public void onNeighborBlockChange(World worldIn, int x, int y, int z, Block neighbor) {
        int l = worldIn.getBlockMetadata(x, y, z);
        int i1 = getDirection(l);
        if (isBlockHeadOfBed(l)) {
            if (worldIn.getBlock(x - field_149981_a[i1][0], y, z - field_149981_a[i1][1]) != this) {
                worldIn.setBlockToAir(x, y, z);
            }
        } else if (worldIn.getBlock(x + field_149981_a[i1][0], y, z + field_149981_a[i1][1]) != this) {
            worldIn.setBlockToAir(x, y, z);
            if (!worldIn.isRemote) {
                this.dropBlockAsItem(worldIn, x, y, z, l, 0);
            }
        }

    }

    public Item getItemDropped(int meta, Random random, int fortune) {
        return isBlockHeadOfBed(meta) ? Item.getItemById(0) : Items.bed;
    }

    private void func_149978_e() {
        this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.5625F, 1.0F);
    }

    public static boolean isBlockHeadOfBed(int meta) {
        return (meta & 8) != 0;
    }

    public static boolean func_149976_c(int meta) {
        return (meta & 4) != 0;
    }

    public static void func_149979_a(World worldIn, int x, int y, int z, boolean occupied) {
        int l = worldIn.getBlockMetadata(x, y, z);
        if (occupied) {
            l |= 4;
        } else {
            l &= -5;
        }

        worldIn.setBlockMetadataWithNotify(x, y, z, l, 4);
    }

    public static ChunkCoordinates func_149977_a(World worldIn, int x, int y, int z, int safeIndex) {
        int i1 = worldIn.getBlockMetadata(x, y, z);
        int j1 = BlockDirectional.getDirection(i1);

        for(int k1 = 0; k1 <= 1; ++k1) {
            int l1 = x - field_149981_a[j1][0] * k1 - 1;
            int i2 = z - field_149981_a[j1][1] * k1 - 1;
            int j2 = l1 + 2;
            int k2 = i2 + 2;

            for(int l2 = l1; l2 <= j2; ++l2) {
                for(int i3 = i2; i3 <= k2; ++i3) {
                    if (World.doesBlockHaveSolidTopSurface(worldIn, l2, y - 1, i3) && !worldIn.getBlock(l2, y, i3).getMaterial().isOpaque() && !worldIn.getBlock(l2, y + 1, i3).getMaterial().isOpaque()) {
                        if (safeIndex <= 0) {
                            return new ChunkCoordinates(l2, y, i3);
                        }

                        --safeIndex;
                    }
                }
            }
        }

        return null;
    }

    public void dropBlockAsItemWithChance(World worldIn, int x, int y, int z, int meta, float chance, int fortune) {
        if (!isBlockHeadOfBed(meta)) {
            super.dropBlockAsItemWithChance(worldIn, x, y, z, meta, chance, 0);
        }

    }

    public int getMobilityFlag() {
        return 1;
    }

    @SideOnly(Side.CLIENT)
    public Item getItem(World worldIn, int x, int y, int z) {
        return Items.bed;
    }

    public void onBlockHarvested(World worldIn, int x, int y, int z, int meta, EntityPlayer player) {
        if (player.capabilities.isCreativeMode && isBlockHeadOfBed(meta)) {
            int i1 = getDirection(meta);
            x -= field_149981_a[i1][0];
            z -= field_149981_a[i1][1];
            if (worldIn.getBlock(x, y, z) == this) {
                worldIn.setBlockToAir(x, y, z);
            }
        }

    }

    @MixinMerged(
        mixin = "com.mitchej123.hodgepodge.mixins.early.minecraft.MixinBlockBed",
        priority = 1000,
        sessionId = "98d213e4-85ca-4ce4-959f-84858add440a"
    )
    public void redirect$zfn000$hodgepodge$sendMessageAboveHotbar(EntityPlayer player, IChatComponent chatComponent) {
        if (player instanceof EntityPlayerMP) {
            EntityPlayerMP entityPlayerMP = (EntityPlayerMP)player;
            GTNHLib.proxy.sendMessageAboveHotbar(entityPlayerMP, chatComponent.setChatStyle((new ChatStyle()).setColor(EnumChatFormatting.WHITE)), 60, true, true);
        }
    }

    @MixinMerged(
        mixin = "com.mitchej123.hodgepodge.mixins.early.minecraft.MixinBlockBed_AlwaysSetsSpawn",
        priority = 1000,
        sessionId = "98d213e4-85ca-4ce4-959f-84858add440a"
    )
    public EnumStatus wrapOperation$zfo000$hodgepodge$setSpawn(EntityPlayer instance, int x, int y, int z, Operation<EnumStatus> original, World worldIn) {
        if (instance instanceof EntityPlayerMP) {
            EntityPlayerMP entityPlayerMP = (EntityPlayerMP)instance;
            ChunkCoordinates bedPos = entityPlayerMP.getBedLocation(worldIn.provider.dimensionId);
            ChunkCoordinates newBedPos = new ChunkCoordinates(x, y, z);
            if (worldIn.isDaytime() && !newBedPos.equals(bedPos)) {
                entityPlayerMP.setSpawnChunk(newBedPos, false);
                entityPlayerMP.addChatComponentMessage(new ChatComponentTranslation("hodgepodge.bed_respawn.msg", new Object[0]));
                return EnumStatus.OTHER_PROBLEM;
            }
        }

        return (EnumStatus)original.call(new Object[]{instance, x, y, z});
    }

    @MixinMerged(
        mixin = "com.mitchej123.hodgepodge.mixins.early.minecraft.MixinBlockBed_AlwaysSetsSpawn",
        priority = 1000,
        sessionId = "98d213e4-85ca-4ce4-959f-84858add440a"
    )
    @SugarBridge
    public EnumStatus wrapOperation$zfo000$hodgepodge$setSpawn$mixinextras$bridge$22(EntityPlayer var1, int var2, int var3, int var4, Operation<EnumStatus> var5, World var6) {
        return this.wrapOperation$zfo000$hodgepodge$setSpawn(var1, var2, var3, var4, var5, (World)var6.get());
    }
}
