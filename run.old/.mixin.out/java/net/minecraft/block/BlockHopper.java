package net.minecraft.block;

import com.google.common.collect.ImmutableList;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityHopper;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Facing;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.transformer.meta.MixinMerged;

public class BlockHopper extends BlockContainer {
    private final Random field_149922_a = new Random();
    @SideOnly(Side.CLIENT)
    private IIcon field_149921_b;
    @SideOnly(Side.CLIENT)
    private IIcon field_149923_M;
    @SideOnly(Side.CLIENT)
    private IIcon field_149924_N;
    private static final String __OBFID = "CL_00000257";
    @Unique
    @MixinMerged(
        mixin = "com.mitchej123.hodgepodge.mixins.early.minecraft.MixinBlockHopper",
        priority = 1000,
        sessionId = "98d213e4-85ca-4ce4-959f-84858add440a"
    )
    private static final EnumMap<EnumFacing, List<AxisAlignedBB>> BOUNDS;

    public BlockHopper() {
        super(Material.iron);
        this.setCreativeTab(CreativeTabs.tabRedstone);
        this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
    }

    public void setBlockBoundsBasedOnState(IBlockAccess worldIn, int x, int y, int z) {
        this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
    }

    public void addCollisionBoxesToList(World worldIn, int x, int y, int z, AxisAlignedBB mask, List list, Entity collider) {
        this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.625F, 1.0F);
        super.addCollisionBoxesToList(worldIn, x, y, z, mask, list, collider);
        float f = 0.125F;
        this.setBlockBounds(0.0F, 0.0F, 0.0F, f, 1.0F, 1.0F);
        super.addCollisionBoxesToList(worldIn, x, y, z, mask, list, collider);
        this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, f);
        super.addCollisionBoxesToList(worldIn, x, y, z, mask, list, collider);
        this.setBlockBounds(1.0F - f, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        super.addCollisionBoxesToList(worldIn, x, y, z, mask, list, collider);
        this.setBlockBounds(0.0F, 0.0F, 1.0F - f, 1.0F, 1.0F, 1.0F);
        super.addCollisionBoxesToList(worldIn, x, y, z, mask, list, collider);
        this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
    }

    public int onBlockPlaced(World worldIn, int x, int y, int z, int side, float subX, float subY, float subZ, int meta) {
        int j1 = Facing.oppositeSide[side];
        if (j1 == 1) {
            j1 = 0;
        }

        return j1;
    }

    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntityHopper();
    }

    public void onBlockPlacedBy(World worldIn, int x, int y, int z, EntityLivingBase placer, ItemStack itemIn) {
        super.onBlockPlacedBy(worldIn, x, y, z, placer, itemIn);
        if (itemIn.hasDisplayName()) {
            TileEntityHopper tileentityhopper = func_149920_e(worldIn, x, y, z);
            tileentityhopper.func_145886_a(itemIn.getDisplayName());
        }

    }

    public void onBlockAdded(World worldIn, int x, int y, int z) {
        super.onBlockAdded(worldIn, x, y, z);
        this.func_149919_e(worldIn, x, y, z);
    }

    public boolean onBlockActivated(World worldIn, int x, int y, int z, EntityPlayer player, int side, float subX, float subY, float subZ) {
        if (worldIn.isRemote) {
            return true;
        } else {
            TileEntityHopper tileentityhopper = func_149920_e(worldIn, x, y, z);
            if (tileentityhopper != null) {
                player.func_146093_a(tileentityhopper);
            }

            return true;
        }
    }

    public void onNeighborBlockChange(World worldIn, int x, int y, int z, Block neighbor) {
        this.func_149919_e(worldIn, x, y, z);
    }

    private void func_149919_e(World p_149919_1_, int p_149919_2_, int p_149919_3_, int p_149919_4_) {
        int l = p_149919_1_.getBlockMetadata(p_149919_2_, p_149919_3_, p_149919_4_);
        int i1 = getDirectionFromMetadata(l);
        boolean flag = !p_149919_1_.isBlockIndirectlyGettingPowered(p_149919_2_, p_149919_3_, p_149919_4_);
        boolean flag1 = func_149917_c(l);
        if (flag != flag1) {
            p_149919_1_.setBlockMetadataWithNotify(p_149919_2_, p_149919_3_, p_149919_4_, i1 | (flag ? 0 : 8), 4);
        }

    }

    public void breakBlock(World worldIn, int x, int y, int z, Block blockBroken, int meta) {
        TileEntityHopper tileentityhopper = (TileEntityHopper)worldIn.getTileEntity(x, y, z);
        if (tileentityhopper != null) {
            for(int i1 = 0; i1 < tileentityhopper.getSizeInventory(); ++i1) {
                ItemStack itemstack = tileentityhopper.getStackInSlot(i1);
                if (itemstack != null) {
                    float f = this.field_149922_a.nextFloat() * 0.8F + 0.1F;
                    float f1 = this.field_149922_a.nextFloat() * 0.8F + 0.1F;
                    float f2 = this.field_149922_a.nextFloat() * 0.8F + 0.1F;

                    while(itemstack.stackSize > 0) {
                        int j1 = this.field_149922_a.nextInt(21) + 10;
                        if (j1 > itemstack.stackSize) {
                            j1 = itemstack.stackSize;
                        }

                        itemstack.stackSize -= j1;
                        EntityItem entityitem = new EntityItem(worldIn, (double)((float)x + f), (double)((float)y + f1), (double)((float)z + f2), new ItemStack(itemstack.getItem(), j1, itemstack.getItemDamage()));
                        if (itemstack.hasTagCompound()) {
                            entityitem.getEntityItem().setTagCompound((NBTTagCompound)itemstack.getTagCompound().copy());
                        }

                        float f3 = 0.05F;
                        entityitem.motionX = (double)((float)this.field_149922_a.nextGaussian() * f3);
                        entityitem.motionY = (double)((float)this.field_149922_a.nextGaussian() * f3 + 0.2F);
                        entityitem.motionZ = (double)((float)this.field_149922_a.nextGaussian() * f3);
                        worldIn.spawnEntityInWorld(entityitem);
                    }
                }
            }

            worldIn.func_147453_f(x, y, z, blockBroken);
        }

        super.breakBlock(worldIn, x, y, z, blockBroken, meta);
    }

    public int getRenderType() {
        return 38;
    }

    public boolean renderAsNormalBlock() {
        return false;
    }

    public boolean isOpaqueCube() {
        return false;
    }

    @SideOnly(Side.CLIENT)
    public boolean shouldSideBeRendered(IBlockAccess worldIn, int x, int y, int z, int side) {
        return true;
    }

    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        return side == 1 ? this.field_149923_M : this.field_149921_b;
    }

    public static int getDirectionFromMetadata(int p_149918_0_) {
        return p_149918_0_ & 7;
    }

    public static boolean func_149917_c(int p_149917_0_) {
        return (p_149917_0_ & 8) != 8;
    }

    public boolean hasComparatorInputOverride() {
        return true;
    }

    public int getComparatorInputOverride(World worldIn, int x, int y, int z, int side) {
        return Container.calcRedstoneFromInventory(func_149920_e(worldIn, x, y, z));
    }

    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister reg) {
        this.field_149921_b = reg.registerIcon("hopper_outside");
        this.field_149923_M = reg.registerIcon("hopper_top");
        this.field_149924_N = reg.registerIcon("hopper_inside");
    }

    @SideOnly(Side.CLIENT)
    public static IIcon getHopperIcon(String p_149916_0_) {
        return p_149916_0_.equals("hopper_outside") ? Blocks.hopper.field_149921_b : (p_149916_0_.equals("hopper_inside") ? Blocks.hopper.field_149924_N : null);
    }

    public static TileEntityHopper func_149920_e(IBlockAccess p_149920_0_, int p_149920_1_, int p_149920_2_, int p_149920_3_) {
        return (TileEntityHopper)p_149920_0_.getTileEntity(p_149920_1_, p_149920_2_, p_149920_3_);
    }

    @SideOnly(Side.CLIENT)
    public String getItemIconName() {
        return "hopper";
    }

    @Unique
    @MixinMerged(
        mixin = "com.mitchej123.hodgepodge.mixins.early.minecraft.MixinBlockHopper",
        priority = 1000,
        sessionId = "98d213e4-85ca-4ce4-959f-84858add440a"
    )
    private static AxisAlignedBB makeAABB(int fromX, int fromY, int fromZ, int toX, int toY, int toZ) {
        return AxisAlignedBB.getBoundingBox((double)((float)fromX / 16.0F), (double)((float)fromY / 16.0F), (double)((float)fromZ / 16.0F), (double)((float)toX / 16.0F), (double)((float)toY / 16.0F), (double)((float)toZ / 16.0F));
    }

    @Unique
    @MixinMerged(
        mixin = "com.mitchej123.hodgepodge.mixins.early.minecraft.MixinBlockHopper",
        priority = 1000,
        sessionId = "98d213e4-85ca-4ce4-959f-84858add440a"
    )
    private static MovingObjectPosition rayTrace(Vec3 pos, Vec3 start, Vec3 end, AxisAlignedBB boundingBox) {
        Vec3 vec3d = start.addVector(-pos.xCoord, -pos.yCoord, -pos.zCoord);
        Vec3 vec3d1 = end.addVector(-pos.xCoord, -pos.yCoord, -pos.zCoord);
        MovingObjectPosition raytraceresult = boundingBox.calculateIntercept(vec3d, vec3d1);
        if (raytraceresult == null) {
            return null;
        } else {
            Vec3 res = raytraceresult.hitVec.addVector(pos.xCoord, pos.yCoord, pos.zCoord);
            return new MovingObjectPosition((int)res.xCoord, (int)res.yCoord, (int)res.zCoord, raytraceresult.sideHit, pos);
        }
    }

    @MixinMerged(
        mixin = "com.mitchej123.hodgepodge.mixins.early.minecraft.MixinBlockHopper",
        priority = 1000,
        sessionId = "98d213e4-85ca-4ce4-959f-84858add440a"
    )
    public MovingObjectPosition collisionRayTrace(World world, int x, int y, int z, Vec3 start, Vec3 end) {
        Vec3 pos = Vec3.createVectorHelper((double)x, (double)y, (double)z);
        EnumFacing facing = EnumFacing.values()[getDirectionFromMetadata(world.getBlockMetadata(x, y, z))];
        List list = (List)BOUNDS.get(facing);
        if (list == null) {
            return super.collisionRayTrace(world, x, y, z, start, end);
        } else {
            return list.stream().map((bb) -> {
                return rayTrace(pos, start, end, bb);
            }).anyMatch(Objects::nonNull) ? super.collisionRayTrace(world, x, y, z, start, end) : null;
        }
    }

    @MixinMerged(
        mixin = "com.mitchej123.hodgepodge.mixins.early.minecraft.MixinBlockHopper"
    )
    static {
        List commonBounds = ImmutableList.of(makeAABB(0, 10, 0, 16, 16, 16), makeAABB(4, 4, 4, 12, 10, 12));
        BOUNDS = (EnumMap)Stream.of(EnumFacing.values()).filter((t) -> {
            return t != EnumFacing.UP;
        }).collect(Collectors.toMap((a) -> {
            return a;
        }, (a) -> {
            return new ArrayList(commonBounds);
        }, (u, v) -> {
            throw new IllegalStateException();
        }, () -> {
            return new EnumMap(EnumFacing.class);
        }));
        ((List)BOUNDS.get(EnumFacing.DOWN)).add(makeAABB(6, 0, 6, 10, 4, 10));
        ((List)BOUNDS.get(EnumFacing.NORTH)).add(makeAABB(6, 4, 0, 10, 8, 4));
        ((List)BOUNDS.get(EnumFacing.SOUTH)).add(makeAABB(6, 4, 12, 10, 8, 16));
        ((List)BOUNDS.get(EnumFacing.WEST)).add(makeAABB(12, 4, 6, 16, 8, 10));
        ((List)BOUNDS.get(EnumFacing.EAST)).add(makeAABB(0, 4, 6, 4, 8, 10));
    }
}
