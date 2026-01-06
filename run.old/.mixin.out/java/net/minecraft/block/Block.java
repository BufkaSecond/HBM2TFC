package net.minecraft.block;

import com.mitchej123.hodgepodge.mixins.interfaces.BlockExt_FixXray;
import cpw.mods.fml.common.registry.FMLControlledNamespacedRegistry;
import cpw.mods.fml.common.registry.GameData;
import cpw.mods.fml.common.registry.RegistryDelegate;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import net.minecraft.block.Block.1;
import net.minecraft.block.Block.2;
import net.minecraft.block.Block.3;
import net.minecraft.block.Block.4;
import net.minecraft.block.Block.5;
import net.minecraft.block.Block.6;
import net.minecraft.block.BlockPressurePlate.Sensitivity;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.RegistryNamespaced;
import net.minecraft.util.StatCollector;
import net.minecraft.util.Vec3;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderEnd;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.common.util.RotationHelper;
import net.minecraftforge.event.ForgeEventFactory;
import org.spongepowered.asm.mixin.transformer.meta.MixinMerged;

public class Block implements BlockExt_FixXray {
    public static final RegistryNamespaced blockRegistry = GameData.getBlockRegistry();
    private CreativeTabs displayOnCreativeTab;
    protected String textureName;
    public static final net.minecraft.block.Block.SoundType soundTypeStone = new net.minecraft.block.Block.SoundType("stone", 1.0F, 1.0F);
    public static final net.minecraft.block.Block.SoundType soundTypeWood = new net.minecraft.block.Block.SoundType("wood", 1.0F, 1.0F);
    public static final net.minecraft.block.Block.SoundType soundTypeGravel = new net.minecraft.block.Block.SoundType("gravel", 1.0F, 1.0F);
    public static final net.minecraft.block.Block.SoundType soundTypeGrass = new net.minecraft.block.Block.SoundType("grass", 1.0F, 1.0F);
    public static final net.minecraft.block.Block.SoundType soundTypePiston = new net.minecraft.block.Block.SoundType("stone", 1.0F, 1.0F);
    public static final net.minecraft.block.Block.SoundType soundTypeMetal = new net.minecraft.block.Block.SoundType("stone", 1.0F, 1.5F);
    public static final net.minecraft.block.Block.SoundType soundTypeGlass = new 1("stone", 1.0F, 1.0F);
    public static final net.minecraft.block.Block.SoundType soundTypeCloth = new net.minecraft.block.Block.SoundType("cloth", 1.0F, 1.0F);
    public static final net.minecraft.block.Block.SoundType soundTypeSand = new net.minecraft.block.Block.SoundType("sand", 1.0F, 1.0F);
    public static final net.minecraft.block.Block.SoundType soundTypeSnow = new net.minecraft.block.Block.SoundType("snow", 1.0F, 1.0F);
    public static final net.minecraft.block.Block.SoundType soundTypeLadder = new 2("ladder", 1.0F, 1.0F);
    public static final net.minecraft.block.Block.SoundType soundTypeAnvil = new 3("anvil", 0.3F, 1.0F);
    protected boolean opaque;
    protected int lightOpacity;
    protected boolean canBlockGrass;
    protected int lightValue;
    protected boolean useNeighborBrightness;
    protected float blockHardness;
    protected float blockResistance;
    protected boolean blockConstructorCalled = true;
    protected boolean enableStats = true;
    protected boolean needsRandomTick;
    protected boolean isBlockContainer;
    protected double minX;
    protected double minY;
    protected double minZ;
    protected double maxX;
    protected double maxY;
    protected double maxZ;
    public net.minecraft.block.Block.SoundType stepSound;
    public float blockParticleGravity;
    protected final Material blockMaterial;
    public float slipperiness;
    private String unlocalizedName;
    @SideOnly(Side.CLIENT)
    protected IIcon blockIcon;
    private static final String __OBFID = "CL_00000199";
    public final RegistryDelegate<Block> delegate;
    protected ThreadLocal<EntityPlayer> harvesters;
    private ThreadLocal<Integer> silk_check_meta;
    private boolean isTileProvider;
    private String[] harvestTool;
    private int[] harvestLevel;
    protected ThreadLocal<Boolean> captureDrops;
    protected ThreadLocal<List<ItemStack>> capturedDrops;

    public static int getIdFromBlock(Block blockIn) {
        return blockRegistry.getIDForObject(blockIn);
    }

    public static Block getBlockById(int id) {
        Block ret = (Block)blockRegistry.getObjectById(id);
        return ret == null ? Blocks.air : ret;
    }

    public static Block getBlockFromItem(Item itemIn) {
        return getBlockById(Item.getIdFromItem(itemIn));
    }

    public static Block getBlockFromName(String name) {
        if (blockRegistry.containsKey(name)) {
            return (Block)blockRegistry.getObject(name);
        } else {
            try {
                return (Block)blockRegistry.getObjectById(Integer.parseInt(name));
            } catch (NumberFormatException var2) {
                return null;
            }
        }
    }

    public boolean func_149730_j() {
        return this.opaque;
    }

    public int getLightOpacity() {
        return this.lightOpacity;
    }

    @SideOnly(Side.CLIENT)
    public boolean getCanBlockGrass() {
        return this.canBlockGrass;
    }

    public int getLightValue() {
        return this.lightValue;
    }

    public boolean getUseNeighborBrightness() {
        return this.useNeighborBrightness;
    }

    public Material getMaterial() {
        return this.blockMaterial;
    }

    public MapColor getMapColor(int meta) {
        return this.getMaterial().getMaterialMapColor();
    }

    public static void registerBlocks() {
        blockRegistry.addObject(0, "air", (new BlockAir()).setBlockName("air"));
        blockRegistry.addObject(1, "stone", (new BlockStone()).setHardness(1.5F).setResistance(10.0F).setStepSound(soundTypePiston).setBlockName("stone").setBlockTextureName("stone"));
        blockRegistry.addObject(2, "grass", (new BlockGrass()).setHardness(0.6F).setStepSound(soundTypeGrass).setBlockName("grass").setBlockTextureName("grass"));
        blockRegistry.addObject(3, "dirt", (new BlockDirt()).setHardness(0.5F).setStepSound(soundTypeGravel).setBlockName("dirt").setBlockTextureName("dirt"));
        Block block = (new Block(Material.rock)).setHardness(2.0F).setResistance(10.0F).setStepSound(soundTypePiston).setBlockName("stonebrick").setCreativeTab(CreativeTabs.tabBlock).setBlockTextureName("cobblestone");
        blockRegistry.addObject(4, "cobblestone", block);
        Block block1 = (new BlockWood()).setHardness(2.0F).setResistance(5.0F).setStepSound(soundTypeWood).setBlockName("wood").setBlockTextureName("planks");
        blockRegistry.addObject(5, "planks", block1);
        blockRegistry.addObject(6, "sapling", (new BlockSapling()).setHardness(0.0F).setStepSound(soundTypeGrass).setBlockName("sapling").setBlockTextureName("sapling"));
        blockRegistry.addObject(7, "bedrock", (new Block(Material.rock)).setBlockUnbreakable().setResistance(6000000.0F).setStepSound(soundTypePiston).setBlockName("bedrock").disableStats().setCreativeTab(CreativeTabs.tabBlock).setBlockTextureName("bedrock"));
        blockRegistry.addObject(8, "flowing_water", (new BlockDynamicLiquid(Material.water)).setHardness(100.0F).setLightOpacity(3).setBlockName("water").disableStats().setBlockTextureName("water_flow"));
        blockRegistry.addObject(9, "water", (new BlockStaticLiquid(Material.water)).setHardness(100.0F).setLightOpacity(3).setBlockName("water").disableStats().setBlockTextureName("water_still"));
        blockRegistry.addObject(10, "flowing_lava", (new BlockDynamicLiquid(Material.lava)).setHardness(100.0F).setLightLevel(1.0F).setBlockName("lava").disableStats().setBlockTextureName("lava_flow"));
        blockRegistry.addObject(11, "lava", (new BlockStaticLiquid(Material.lava)).setHardness(100.0F).setLightLevel(1.0F).setBlockName("lava").disableStats().setBlockTextureName("lava_still"));
        blockRegistry.addObject(12, "sand", (new BlockSand()).setHardness(0.5F).setStepSound(soundTypeSand).setBlockName("sand").setBlockTextureName("sand"));
        blockRegistry.addObject(13, "gravel", (new BlockGravel()).setHardness(0.6F).setStepSound(soundTypeGravel).setBlockName("gravel").setBlockTextureName("gravel"));
        blockRegistry.addObject(14, "gold_ore", (new BlockOre()).setHardness(3.0F).setResistance(5.0F).setStepSound(soundTypePiston).setBlockName("oreGold").setBlockTextureName("gold_ore"));
        blockRegistry.addObject(15, "iron_ore", (new BlockOre()).setHardness(3.0F).setResistance(5.0F).setStepSound(soundTypePiston).setBlockName("oreIron").setBlockTextureName("iron_ore"));
        blockRegistry.addObject(16, "coal_ore", (new BlockOre()).setHardness(3.0F).setResistance(5.0F).setStepSound(soundTypePiston).setBlockName("oreCoal").setBlockTextureName("coal_ore"));
        blockRegistry.addObject(17, "log", (new BlockOldLog()).setBlockName("log").setBlockTextureName("log"));
        blockRegistry.addObject(18, "leaves", (new BlockOldLeaf()).setBlockName("leaves").setBlockTextureName("leaves"));
        blockRegistry.addObject(19, "sponge", (new BlockSponge()).setHardness(0.6F).setStepSound(soundTypeGrass).setBlockName("sponge").setBlockTextureName("sponge"));
        blockRegistry.addObject(20, "glass", (new BlockGlass(Material.glass, false)).setHardness(0.3F).setStepSound(soundTypeGlass).setBlockName("glass").setBlockTextureName("glass"));
        blockRegistry.addObject(21, "lapis_ore", (new BlockOre()).setHardness(3.0F).setResistance(5.0F).setStepSound(soundTypePiston).setBlockName("oreLapis").setBlockTextureName("lapis_ore"));
        blockRegistry.addObject(22, "lapis_block", (new BlockCompressed(MapColor.lapisColor)).setHardness(3.0F).setResistance(5.0F).setStepSound(soundTypePiston).setBlockName("blockLapis").setCreativeTab(CreativeTabs.tabBlock).setBlockTextureName("lapis_block"));
        blockRegistry.addObject(23, "dispenser", (new BlockDispenser()).setHardness(3.5F).setStepSound(soundTypePiston).setBlockName("dispenser").setBlockTextureName("dispenser"));
        Block block2 = (new BlockSandStone()).setStepSound(soundTypePiston).setHardness(0.8F).setBlockName("sandStone").setBlockTextureName("sandstone");
        blockRegistry.addObject(24, "sandstone", block2);
        blockRegistry.addObject(25, "noteblock", (new BlockNote()).setHardness(0.8F).setBlockName("musicBlock").setBlockTextureName("noteblock"));
        blockRegistry.addObject(26, "bed", (new BlockBed()).setHardness(0.2F).setBlockName("bed").disableStats().setBlockTextureName("bed"));
        blockRegistry.addObject(27, "golden_rail", (new BlockRailPowered()).setHardness(0.7F).setStepSound(soundTypeMetal).setBlockName("goldenRail").setBlockTextureName("rail_golden"));
        blockRegistry.addObject(28, "detector_rail", (new BlockRailDetector()).setHardness(0.7F).setStepSound(soundTypeMetal).setBlockName("detectorRail").setBlockTextureName("rail_detector"));
        blockRegistry.addObject(29, "sticky_piston", (new BlockPistonBase(true)).setBlockName("pistonStickyBase"));
        blockRegistry.addObject(30, "web", (new BlockWeb()).setLightOpacity(1).setHardness(4.0F).setBlockName("web").setBlockTextureName("web"));
        blockRegistry.addObject(31, "tallgrass", (new BlockTallGrass()).setHardness(0.0F).setStepSound(soundTypeGrass).setBlockName("tallgrass"));
        blockRegistry.addObject(32, "deadbush", (new BlockDeadBush()).setHardness(0.0F).setStepSound(soundTypeGrass).setBlockName("deadbush").setBlockTextureName("deadbush"));
        blockRegistry.addObject(33, "piston", (new BlockPistonBase(false)).setBlockName("pistonBase"));
        blockRegistry.addObject(34, "piston_head", new BlockPistonExtension());
        blockRegistry.addObject(35, "wool", (new BlockColored(Material.cloth)).setHardness(0.8F).setStepSound(soundTypeCloth).setBlockName("cloth").setBlockTextureName("wool_colored"));
        blockRegistry.addObject(36, "piston_extension", new BlockPistonMoving());
        blockRegistry.addObject(37, "yellow_flower", (new BlockFlower(0)).setHardness(0.0F).setStepSound(soundTypeGrass).setBlockName("flower1").setBlockTextureName("flower_dandelion"));
        blockRegistry.addObject(38, "red_flower", (new BlockFlower(1)).setHardness(0.0F).setStepSound(soundTypeGrass).setBlockName("flower2").setBlockTextureName("flower_rose"));
        blockRegistry.addObject(39, "brown_mushroom", (new BlockMushroom()).setHardness(0.0F).setStepSound(soundTypeGrass).setLightLevel(0.125F).setBlockName("mushroom").setBlockTextureName("mushroom_brown"));
        blockRegistry.addObject(40, "red_mushroom", (new BlockMushroom()).setHardness(0.0F).setStepSound(soundTypeGrass).setBlockName("mushroom").setBlockTextureName("mushroom_red"));
        blockRegistry.addObject(41, "gold_block", (new BlockCompressed(MapColor.goldColor)).setHardness(3.0F).setResistance(10.0F).setStepSound(soundTypeMetal).setBlockName("blockGold").setBlockTextureName("gold_block"));
        blockRegistry.addObject(42, "iron_block", (new BlockCompressed(MapColor.ironColor)).setHardness(5.0F).setResistance(10.0F).setStepSound(soundTypeMetal).setBlockName("blockIron").setBlockTextureName("iron_block"));
        blockRegistry.addObject(43, "double_stone_slab", (new BlockStoneSlab(true)).setHardness(2.0F).setResistance(10.0F).setStepSound(soundTypePiston).setBlockName("stoneSlab"));
        blockRegistry.addObject(44, "stone_slab", (new BlockStoneSlab(false)).setHardness(2.0F).setResistance(10.0F).setStepSound(soundTypePiston).setBlockName("stoneSlab"));
        Block block3 = (new Block(Material.rock)).setHardness(2.0F).setResistance(10.0F).setStepSound(soundTypePiston).setBlockName("brick").setCreativeTab(CreativeTabs.tabBlock).setBlockTextureName("brick");
        blockRegistry.addObject(45, "brick_block", block3);
        blockRegistry.addObject(46, "tnt", (new BlockTNT()).setHardness(0.0F).setStepSound(soundTypeGrass).setBlockName("tnt").setBlockTextureName("tnt"));
        blockRegistry.addObject(47, "bookshelf", (new BlockBookshelf()).setHardness(1.5F).setStepSound(soundTypeWood).setBlockName("bookshelf").setBlockTextureName("bookshelf"));
        blockRegistry.addObject(48, "mossy_cobblestone", (new Block(Material.rock)).setHardness(2.0F).setResistance(10.0F).setStepSound(soundTypePiston).setBlockName("stoneMoss").setCreativeTab(CreativeTabs.tabBlock).setBlockTextureName("cobblestone_mossy"));
        blockRegistry.addObject(49, "obsidian", (new BlockObsidian()).setHardness(50.0F).setResistance(2000.0F).setStepSound(soundTypePiston).setBlockName("obsidian").setBlockTextureName("obsidian"));
        blockRegistry.addObject(50, "torch", (new BlockTorch()).setHardness(0.0F).setLightLevel(0.9375F).setStepSound(soundTypeWood).setBlockName("torch").setBlockTextureName("torch_on"));
        blockRegistry.addObject(51, "fire", (new BlockFire()).setHardness(0.0F).setLightLevel(1.0F).setStepSound(soundTypeWood).setBlockName("fire").disableStats().setBlockTextureName("fire"));
        blockRegistry.addObject(52, "mob_spawner", (new BlockMobSpawner()).setHardness(5.0F).setStepSound(soundTypeMetal).setBlockName("mobSpawner").disableStats().setBlockTextureName("mob_spawner"));
        blockRegistry.addObject(53, "oak_stairs", (new BlockStairs(block1, 0)).setBlockName("stairsWood"));
        blockRegistry.addObject(54, "chest", (new BlockChest(0)).setHardness(2.5F).setStepSound(soundTypeWood).setBlockName("chest"));
        blockRegistry.addObject(55, "redstone_wire", (new BlockRedstoneWire()).setHardness(0.0F).setStepSound(soundTypeStone).setBlockName("redstoneDust").disableStats().setBlockTextureName("redstone_dust"));
        blockRegistry.addObject(56, "diamond_ore", (new BlockOre()).setHardness(3.0F).setResistance(5.0F).setStepSound(soundTypePiston).setBlockName("oreDiamond").setBlockTextureName("diamond_ore"));
        blockRegistry.addObject(57, "diamond_block", (new BlockCompressed(MapColor.diamondColor)).setHardness(5.0F).setResistance(10.0F).setStepSound(soundTypeMetal).setBlockName("blockDiamond").setBlockTextureName("diamond_block"));
        blockRegistry.addObject(58, "crafting_table", (new BlockWorkbench()).setHardness(2.5F).setStepSound(soundTypeWood).setBlockName("workbench").setBlockTextureName("crafting_table"));
        blockRegistry.addObject(59, "wheat", (new BlockCrops()).setBlockName("crops").setBlockTextureName("wheat"));
        Block block4 = (new BlockFarmland()).setHardness(0.6F).setStepSound(soundTypeGravel).setBlockName("farmland").setBlockTextureName("farmland");
        blockRegistry.addObject(60, "farmland", block4);
        blockRegistry.addObject(61, "furnace", (new BlockFurnace(false)).setHardness(3.5F).setStepSound(soundTypePiston).setBlockName("furnace").setCreativeTab(CreativeTabs.tabDecorations));
        blockRegistry.addObject(62, "lit_furnace", (new BlockFurnace(true)).setHardness(3.5F).setStepSound(soundTypePiston).setLightLevel(0.875F).setBlockName("furnace"));
        blockRegistry.addObject(63, "standing_sign", (new BlockSign(TileEntitySign.class, true)).setHardness(1.0F).setStepSound(soundTypeWood).setBlockName("sign").disableStats());
        blockRegistry.addObject(64, "wooden_door", (new BlockDoor(Material.wood)).setHardness(3.0F).setStepSound(soundTypeWood).setBlockName("doorWood").disableStats().setBlockTextureName("door_wood"));
        blockRegistry.addObject(65, "ladder", (new BlockLadder()).setHardness(0.4F).setStepSound(soundTypeLadder).setBlockName("ladder").setBlockTextureName("ladder"));
        blockRegistry.addObject(66, "rail", (new BlockRail()).setHardness(0.7F).setStepSound(soundTypeMetal).setBlockName("rail").setBlockTextureName("rail_normal"));
        blockRegistry.addObject(67, "stone_stairs", (new BlockStairs(block, 0)).setBlockName("stairsStone"));
        blockRegistry.addObject(68, "wall_sign", (new BlockSign(TileEntitySign.class, false)).setHardness(1.0F).setStepSound(soundTypeWood).setBlockName("sign").disableStats());
        blockRegistry.addObject(69, "lever", (new BlockLever()).setHardness(0.5F).setStepSound(soundTypeWood).setBlockName("lever").setBlockTextureName("lever"));
        blockRegistry.addObject(70, "stone_pressure_plate", (new BlockPressurePlate("stone", Material.rock, Sensitivity.mobs)).setHardness(0.5F).setStepSound(soundTypePiston).setBlockName("pressurePlate"));
        blockRegistry.addObject(71, "iron_door", (new BlockDoor(Material.iron)).setHardness(5.0F).setStepSound(soundTypeMetal).setBlockName("doorIron").disableStats().setBlockTextureName("door_iron"));
        blockRegistry.addObject(72, "wooden_pressure_plate", (new BlockPressurePlate("planks_oak", Material.wood, Sensitivity.everything)).setHardness(0.5F).setStepSound(soundTypeWood).setBlockName("pressurePlate"));
        blockRegistry.addObject(73, "redstone_ore", (new BlockRedstoneOre(false)).setHardness(3.0F).setResistance(5.0F).setStepSound(soundTypePiston).setBlockName("oreRedstone").setCreativeTab(CreativeTabs.tabBlock).setBlockTextureName("redstone_ore"));
        blockRegistry.addObject(74, "lit_redstone_ore", (new BlockRedstoneOre(true)).setLightLevel(0.625F).setHardness(3.0F).setResistance(5.0F).setStepSound(soundTypePiston).setBlockName("oreRedstone").setBlockTextureName("redstone_ore"));
        blockRegistry.addObject(75, "unlit_redstone_torch", (new BlockRedstoneTorch(false)).setHardness(0.0F).setStepSound(soundTypeWood).setBlockName("notGate").setBlockTextureName("redstone_torch_off"));
        blockRegistry.addObject(76, "redstone_torch", (new BlockRedstoneTorch(true)).setHardness(0.0F).setLightLevel(0.5F).setStepSound(soundTypeWood).setBlockName("notGate").setCreativeTab(CreativeTabs.tabRedstone).setBlockTextureName("redstone_torch_on"));
        blockRegistry.addObject(77, "stone_button", (new BlockButtonStone()).setHardness(0.5F).setStepSound(soundTypePiston).setBlockName("button"));
        blockRegistry.addObject(78, "snow_layer", (new BlockSnow()).setHardness(0.1F).setStepSound(soundTypeSnow).setBlockName("snow").setLightOpacity(0).setBlockTextureName("snow"));
        blockRegistry.addObject(79, "ice", (new BlockIce()).setHardness(0.5F).setLightOpacity(3).setStepSound(soundTypeGlass).setBlockName("ice").setBlockTextureName("ice"));
        blockRegistry.addObject(80, "snow", (new BlockSnowBlock()).setHardness(0.2F).setStepSound(soundTypeSnow).setBlockName("snow").setBlockTextureName("snow"));
        blockRegistry.addObject(81, "cactus", (new BlockCactus()).setHardness(0.4F).setStepSound(soundTypeCloth).setBlockName("cactus").setBlockTextureName("cactus"));
        blockRegistry.addObject(82, "clay", (new BlockClay()).setHardness(0.6F).setStepSound(soundTypeGravel).setBlockName("clay").setBlockTextureName("clay"));
        blockRegistry.addObject(83, "reeds", (new BlockReed()).setHardness(0.0F).setStepSound(soundTypeGrass).setBlockName("reeds").disableStats().setBlockTextureName("reeds"));
        blockRegistry.addObject(84, "jukebox", (new BlockJukebox()).setHardness(2.0F).setResistance(10.0F).setStepSound(soundTypePiston).setBlockName("jukebox").setBlockTextureName("jukebox"));
        blockRegistry.addObject(85, "fence", (new BlockFence("planks_oak", Material.wood)).setHardness(2.0F).setResistance(5.0F).setStepSound(soundTypeWood).setBlockName("fence"));
        Block block5 = (new BlockPumpkin(false)).setHardness(1.0F).setStepSound(soundTypeWood).setBlockName("pumpkin").setBlockTextureName("pumpkin");
        blockRegistry.addObject(86, "pumpkin", block5);
        blockRegistry.addObject(87, "netherrack", (new BlockNetherrack()).setHardness(0.4F).setStepSound(soundTypePiston).setBlockName("hellrock").setBlockTextureName("netherrack"));
        blockRegistry.addObject(88, "soul_sand", (new BlockSoulSand()).setHardness(0.5F).setStepSound(soundTypeSand).setBlockName("hellsand").setBlockTextureName("soul_sand"));
        blockRegistry.addObject(89, "glowstone", (new BlockGlowstone(Material.glass)).setHardness(0.3F).setStepSound(soundTypeGlass).setLightLevel(1.0F).setBlockName("lightgem").setBlockTextureName("glowstone"));
        blockRegistry.addObject(90, "portal", (new BlockPortal()).setHardness(-1.0F).setStepSound(soundTypeGlass).setLightLevel(0.75F).setBlockName("portal").setBlockTextureName("portal"));
        blockRegistry.addObject(91, "lit_pumpkin", (new BlockPumpkin(true)).setHardness(1.0F).setStepSound(soundTypeWood).setLightLevel(1.0F).setBlockName("litpumpkin").setBlockTextureName("pumpkin"));
        blockRegistry.addObject(92, "cake", (new BlockCake()).setHardness(0.5F).setStepSound(soundTypeCloth).setBlockName("cake").disableStats().setBlockTextureName("cake"));
        blockRegistry.addObject(93, "unpowered_repeater", (new BlockRedstoneRepeater(false)).setHardness(0.0F).setStepSound(soundTypeWood).setBlockName("diode").disableStats().setBlockTextureName("repeater_off"));
        blockRegistry.addObject(94, "powered_repeater", (new BlockRedstoneRepeater(true)).setHardness(0.0F).setLightLevel(0.625F).setStepSound(soundTypeWood).setBlockName("diode").disableStats().setBlockTextureName("repeater_on"));
        blockRegistry.addObject(95, "stained_glass", (new BlockStainedGlass(Material.glass)).setHardness(0.3F).setStepSound(soundTypeGlass).setBlockName("stainedGlass").setBlockTextureName("glass"));
        blockRegistry.addObject(96, "trapdoor", (new BlockTrapDoor(Material.wood)).setHardness(3.0F).setStepSound(soundTypeWood).setBlockName("trapdoor").disableStats().setBlockTextureName("trapdoor"));
        blockRegistry.addObject(97, "monster_egg", (new BlockSilverfish()).setHardness(0.75F).setBlockName("monsterStoneEgg"));
        Block block6 = (new BlockStoneBrick()).setHardness(1.5F).setResistance(10.0F).setStepSound(soundTypePiston).setBlockName("stonebricksmooth").setBlockTextureName("stonebrick");
        blockRegistry.addObject(98, "stonebrick", block6);
        blockRegistry.addObject(99, "brown_mushroom_block", (new BlockHugeMushroom(Material.wood, 0)).setHardness(0.2F).setStepSound(soundTypeWood).setBlockName("mushroom").setBlockTextureName("mushroom_block"));
        blockRegistry.addObject(100, "red_mushroom_block", (new BlockHugeMushroom(Material.wood, 1)).setHardness(0.2F).setStepSound(soundTypeWood).setBlockName("mushroom").setBlockTextureName("mushroom_block"));
        blockRegistry.addObject(101, "iron_bars", (new BlockPane("iron_bars", "iron_bars", Material.iron, true)).setHardness(5.0F).setResistance(10.0F).setStepSound(soundTypeMetal).setBlockName("fenceIron"));
        blockRegistry.addObject(102, "glass_pane", (new BlockPane("glass", "glass_pane_top", Material.glass, false)).setHardness(0.3F).setStepSound(soundTypeGlass).setBlockName("thinGlass"));
        Block block7 = (new BlockMelon()).setHardness(1.0F).setStepSound(soundTypeWood).setBlockName("melon").setBlockTextureName("melon");
        blockRegistry.addObject(103, "melon_block", block7);
        blockRegistry.addObject(104, "pumpkin_stem", (new BlockStem(block5)).setHardness(0.0F).setStepSound(soundTypeWood).setBlockName("pumpkinStem").setBlockTextureName("pumpkin_stem"));
        blockRegistry.addObject(105, "melon_stem", (new BlockStem(block7)).setHardness(0.0F).setStepSound(soundTypeWood).setBlockName("pumpkinStem").setBlockTextureName("melon_stem"));
        blockRegistry.addObject(106, "vine", (new BlockVine()).setHardness(0.2F).setStepSound(soundTypeGrass).setBlockName("vine").setBlockTextureName("vine"));
        blockRegistry.addObject(107, "fence_gate", (new BlockFenceGate()).setHardness(2.0F).setResistance(5.0F).setStepSound(soundTypeWood).setBlockName("fenceGate"));
        blockRegistry.addObject(108, "brick_stairs", (new BlockStairs(block3, 0)).setBlockName("stairsBrick"));
        blockRegistry.addObject(109, "stone_brick_stairs", (new BlockStairs(block6, 0)).setBlockName("stairsStoneBrickSmooth"));
        blockRegistry.addObject(110, "mycelium", (new BlockMycelium()).setHardness(0.6F).setStepSound(soundTypeGrass).setBlockName("mycel").setBlockTextureName("mycelium"));
        blockRegistry.addObject(111, "waterlily", (new BlockLilyPad()).setHardness(0.0F).setStepSound(soundTypeGrass).setBlockName("waterlily").setBlockTextureName("waterlily"));
        Block block8 = (new Block(Material.rock)).setHardness(2.0F).setResistance(10.0F).setStepSound(soundTypePiston).setBlockName("netherBrick").setCreativeTab(CreativeTabs.tabBlock).setBlockTextureName("nether_brick");
        blockRegistry.addObject(112, "nether_brick", block8);
        blockRegistry.addObject(113, "nether_brick_fence", (new BlockFence("nether_brick", Material.rock)).setHardness(2.0F).setResistance(10.0F).setStepSound(soundTypePiston).setBlockName("netherFence"));
        blockRegistry.addObject(114, "nether_brick_stairs", (new BlockStairs(block8, 0)).setBlockName("stairsNetherBrick"));
        blockRegistry.addObject(115, "nether_wart", (new BlockNetherWart()).setBlockName("netherStalk").setBlockTextureName("nether_wart"));
        blockRegistry.addObject(116, "enchanting_table", (new BlockEnchantmentTable()).setHardness(5.0F).setResistance(2000.0F).setBlockName("enchantmentTable").setBlockTextureName("enchanting_table"));
        blockRegistry.addObject(117, "brewing_stand", (new BlockBrewingStand()).setHardness(0.5F).setLightLevel(0.125F).setBlockName("brewingStand").setBlockTextureName("brewing_stand"));
        blockRegistry.addObject(118, "cauldron", (new BlockCauldron()).setHardness(2.0F).setBlockName("cauldron").setBlockTextureName("cauldron"));
        blockRegistry.addObject(119, "end_portal", (new BlockEndPortal(Material.portal)).setHardness(-1.0F).setResistance(6000000.0F));
        blockRegistry.addObject(120, "end_portal_frame", (new BlockEndPortalFrame()).setStepSound(soundTypeGlass).setLightLevel(0.125F).setHardness(-1.0F).setBlockName("endPortalFrame").setResistance(6000000.0F).setCreativeTab(CreativeTabs.tabDecorations).setBlockTextureName("endframe"));
        blockRegistry.addObject(121, "end_stone", (new Block(Material.rock)).setHardness(3.0F).setResistance(15.0F).setStepSound(soundTypePiston).setBlockName("whiteStone").setCreativeTab(CreativeTabs.tabBlock).setBlockTextureName("end_stone"));
        blockRegistry.addObject(122, "dragon_egg", (new BlockDragonEgg()).setHardness(3.0F).setResistance(15.0F).setStepSound(soundTypePiston).setLightLevel(0.125F).setBlockName("dragonEgg").setBlockTextureName("dragon_egg"));
        blockRegistry.addObject(123, "redstone_lamp", (new BlockRedstoneLight(false)).setHardness(0.3F).setStepSound(soundTypeGlass).setBlockName("redstoneLight").setCreativeTab(CreativeTabs.tabRedstone).setBlockTextureName("redstone_lamp_off"));
        blockRegistry.addObject(124, "lit_redstone_lamp", (new BlockRedstoneLight(true)).setHardness(0.3F).setStepSound(soundTypeGlass).setBlockName("redstoneLight").setBlockTextureName("redstone_lamp_on"));
        blockRegistry.addObject(125, "double_wooden_slab", (new BlockWoodSlab(true)).setHardness(2.0F).setResistance(5.0F).setStepSound(soundTypeWood).setBlockName("woodSlab"));
        blockRegistry.addObject(126, "wooden_slab", (new BlockWoodSlab(false)).setHardness(2.0F).setResistance(5.0F).setStepSound(soundTypeWood).setBlockName("woodSlab"));
        blockRegistry.addObject(127, "cocoa", (new BlockCocoa()).setHardness(0.2F).setResistance(5.0F).setStepSound(soundTypeWood).setBlockName("cocoa").setBlockTextureName("cocoa"));
        blockRegistry.addObject(128, "sandstone_stairs", (new BlockStairs(block2, 0)).setBlockName("stairsSandStone"));
        blockRegistry.addObject(129, "emerald_ore", (new BlockOre()).setHardness(3.0F).setResistance(5.0F).setStepSound(soundTypePiston).setBlockName("oreEmerald").setBlockTextureName("emerald_ore"));
        blockRegistry.addObject(130, "ender_chest", (new BlockEnderChest()).setHardness(22.5F).setResistance(1000.0F).setStepSound(soundTypePiston).setBlockName("enderChest").setLightLevel(0.5F));
        blockRegistry.addObject(131, "tripwire_hook", (new BlockTripWireHook()).setBlockName("tripWireSource").setBlockTextureName("trip_wire_source"));
        blockRegistry.addObject(132, "tripwire", (new BlockTripWire()).setBlockName("tripWire").setBlockTextureName("trip_wire"));
        blockRegistry.addObject(133, "emerald_block", (new BlockCompressed(MapColor.emeraldColor)).setHardness(5.0F).setResistance(10.0F).setStepSound(soundTypeMetal).setBlockName("blockEmerald").setBlockTextureName("emerald_block"));
        blockRegistry.addObject(134, "spruce_stairs", (new BlockStairs(block1, 1)).setBlockName("stairsWoodSpruce"));
        blockRegistry.addObject(135, "birch_stairs", (new BlockStairs(block1, 2)).setBlockName("stairsWoodBirch"));
        blockRegistry.addObject(136, "jungle_stairs", (new BlockStairs(block1, 3)).setBlockName("stairsWoodJungle"));
        blockRegistry.addObject(137, "command_block", (new BlockCommandBlock()).setBlockUnbreakable().setResistance(6000000.0F).setBlockName("commandBlock").setBlockTextureName("command_block"));
        blockRegistry.addObject(138, "beacon", (new BlockBeacon()).setBlockName("beacon").setLightLevel(1.0F).setBlockTextureName("beacon"));
        blockRegistry.addObject(139, "cobblestone_wall", (new BlockWall(block)).setBlockName("cobbleWall"));
        blockRegistry.addObject(140, "flower_pot", (new BlockFlowerPot()).setHardness(0.0F).setStepSound(soundTypeStone).setBlockName("flowerPot").setBlockTextureName("flower_pot"));
        blockRegistry.addObject(141, "carrots", (new BlockCarrot()).setBlockName("carrots").setBlockTextureName("carrots"));
        blockRegistry.addObject(142, "potatoes", (new BlockPotato()).setBlockName("potatoes").setBlockTextureName("potatoes"));
        blockRegistry.addObject(143, "wooden_button", (new BlockButtonWood()).setHardness(0.5F).setStepSound(soundTypeWood).setBlockName("button"));
        blockRegistry.addObject(144, "skull", (new BlockSkull()).setHardness(1.0F).setStepSound(soundTypePiston).setBlockName("skull").setBlockTextureName("skull"));
        blockRegistry.addObject(145, "anvil", (new BlockAnvil()).setHardness(5.0F).setStepSound(soundTypeAnvil).setResistance(2000.0F).setBlockName("anvil"));
        blockRegistry.addObject(146, "trapped_chest", (new BlockChest(1)).setHardness(2.5F).setStepSound(soundTypeWood).setBlockName("chestTrap"));
        blockRegistry.addObject(147, "light_weighted_pressure_plate", (new BlockPressurePlateWeighted("gold_block", Material.iron, 15)).setHardness(0.5F).setStepSound(soundTypeWood).setBlockName("weightedPlate_light"));
        blockRegistry.addObject(148, "heavy_weighted_pressure_plate", (new BlockPressurePlateWeighted("iron_block", Material.iron, 150)).setHardness(0.5F).setStepSound(soundTypeWood).setBlockName("weightedPlate_heavy"));
        blockRegistry.addObject(149, "unpowered_comparator", (new BlockRedstoneComparator(false)).setHardness(0.0F).setStepSound(soundTypeWood).setBlockName("comparator").disableStats().setBlockTextureName("comparator_off"));
        blockRegistry.addObject(150, "powered_comparator", (new BlockRedstoneComparator(true)).setHardness(0.0F).setLightLevel(0.625F).setStepSound(soundTypeWood).setBlockName("comparator").disableStats().setBlockTextureName("comparator_on"));
        blockRegistry.addObject(151, "daylight_detector", (new BlockDaylightDetector()).setHardness(0.2F).setStepSound(soundTypeWood).setBlockName("daylightDetector").setBlockTextureName("daylight_detector"));
        blockRegistry.addObject(152, "redstone_block", (new BlockCompressedPowered(MapColor.tntColor)).setHardness(5.0F).setResistance(10.0F).setStepSound(soundTypeMetal).setBlockName("blockRedstone").setBlockTextureName("redstone_block"));
        blockRegistry.addObject(153, "quartz_ore", (new BlockOre()).setHardness(3.0F).setResistance(5.0F).setStepSound(soundTypePiston).setBlockName("netherquartz").setBlockTextureName("quartz_ore"));
        blockRegistry.addObject(154, "hopper", (new BlockHopper()).setHardness(3.0F).setResistance(8.0F).setStepSound(soundTypeWood).setBlockName("hopper").setBlockTextureName("hopper"));
        Block block9 = (new BlockQuartz()).setStepSound(soundTypePiston).setHardness(0.8F).setBlockName("quartzBlock").setBlockTextureName("quartz_block");
        blockRegistry.addObject(155, "quartz_block", block9);
        blockRegistry.addObject(156, "quartz_stairs", (new BlockStairs(block9, 0)).setBlockName("stairsQuartz"));
        blockRegistry.addObject(157, "activator_rail", (new BlockRailPowered()).setHardness(0.7F).setStepSound(soundTypeMetal).setBlockName("activatorRail").setBlockTextureName("rail_activator"));
        blockRegistry.addObject(158, "dropper", (new BlockDropper()).setHardness(3.5F).setStepSound(soundTypePiston).setBlockName("dropper").setBlockTextureName("dropper"));
        blockRegistry.addObject(159, "stained_hardened_clay", (new BlockColored(Material.rock)).setHardness(1.25F).setResistance(7.0F).setStepSound(soundTypePiston).setBlockName("clayHardenedStained").setBlockTextureName("hardened_clay_stained"));
        blockRegistry.addObject(160, "stained_glass_pane", (new BlockStainedGlassPane()).setHardness(0.3F).setStepSound(soundTypeGlass).setBlockName("thinStainedGlass").setBlockTextureName("glass"));
        blockRegistry.addObject(161, "leaves2", (new BlockNewLeaf()).setBlockName("leaves").setBlockTextureName("leaves"));
        blockRegistry.addObject(162, "log2", (new BlockNewLog()).setBlockName("log").setBlockTextureName("log"));
        blockRegistry.addObject(163, "acacia_stairs", (new BlockStairs(block1, 4)).setBlockName("stairsWoodAcacia"));
        blockRegistry.addObject(164, "dark_oak_stairs", (new BlockStairs(block1, 5)).setBlockName("stairsWoodDarkOak"));
        blockRegistry.addObject(170, "hay_block", (new BlockHay()).setHardness(0.5F).setStepSound(soundTypeGrass).setBlockName("hayBlock").setCreativeTab(CreativeTabs.tabBlock).setBlockTextureName("hay_block"));
        blockRegistry.addObject(171, "carpet", (new BlockCarpet()).setHardness(0.1F).setStepSound(soundTypeCloth).setBlockName("woolCarpet").setLightOpacity(0));
        blockRegistry.addObject(172, "hardened_clay", (new BlockHardenedClay()).setHardness(1.25F).setResistance(7.0F).setStepSound(soundTypePiston).setBlockName("clayHardened").setBlockTextureName("hardened_clay"));
        blockRegistry.addObject(173, "coal_block", (new Block(Material.rock)).setHardness(5.0F).setResistance(10.0F).setStepSound(soundTypePiston).setBlockName("blockCoal").setCreativeTab(CreativeTabs.tabBlock).setBlockTextureName("coal_block"));
        blockRegistry.addObject(174, "packed_ice", (new BlockPackedIce()).setHardness(0.5F).setStepSound(soundTypeGlass).setBlockName("icePacked").setBlockTextureName("ice_packed"));
        blockRegistry.addObject(175, "double_plant", new BlockDoublePlant());
        Iterator iterator = blockRegistry.iterator();

        while(true) {
            while(iterator.hasNext()) {
                Block block10 = (Block)iterator.next();
                if (block10.blockMaterial == Material.air) {
                    block10.useNeighborBrightness = false;
                } else {
                    boolean flag = false;
                    boolean flag1 = block10.getRenderType() == 10;
                    boolean flag2 = block10 instanceof BlockSlab;
                    boolean flag3 = block10 == block4;
                    boolean flag4 = block10.canBlockGrass;
                    boolean flag5 = block10.lightOpacity == 0;
                    if (flag1 || flag2 || flag3 || flag4 || flag5) {
                        flag = true;
                    }

                    block10.useNeighborBrightness = flag;
                }
            }

            return;
        }
    }

    protected Block(Material materialIn) {
        this.delegate = ((FMLControlledNamespacedRegistry)blockRegistry).getDelegate(this, Block.class);
        this.harvesters = new ThreadLocal();
        this.silk_check_meta = new ThreadLocal();
        this.isTileProvider = this instanceof ITileEntityProvider;
        this.harvestTool = new String[16];
        this.harvestLevel = new int[]{-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1};
        this.captureDrops = new 4(this);
        this.capturedDrops = new 5(this);
        this.stepSound = soundTypeStone;
        this.blockParticleGravity = 1.0F;
        this.slipperiness = 0.6F;
        this.blockMaterial = materialIn;
        this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        this.opaque = this.isOpaqueCube();
        this.lightOpacity = this.isOpaqueCube() ? 255 : 0;
        this.canBlockGrass = !materialIn.getCanBlockGrass();
    }

    public Block setStepSound(net.minecraft.block.Block.SoundType sound) {
        this.stepSound = sound;
        return this;
    }

    public Block setLightOpacity(int opacity) {
        this.lightOpacity = opacity;
        return this;
    }

    public Block setLightLevel(float value) {
        this.lightValue = (int)(15.0F * value);
        return this;
    }

    public Block setResistance(float resistance) {
        this.blockResistance = resistance * 3.0F;
        return this;
    }

    @SideOnly(Side.CLIENT)
    public boolean isBlockNormalCube() {
        return this.blockMaterial.blocksMovement() && this.renderAsNormalBlock();
    }

    public boolean isNormalCube() {
        return this.blockMaterial.isOpaque() && this.renderAsNormalBlock() && !this.canProvidePower();
    }

    public boolean renderAsNormalBlock() {
        return true;
    }

    public boolean getBlocksMovement(IBlockAccess worldIn, int x, int y, int z) {
        return !this.blockMaterial.blocksMovement();
    }

    public int getRenderType() {
        return 0;
    }

    public Block setHardness(float hardness) {
        this.blockHardness = hardness;
        if (this.blockResistance < hardness * 5.0F) {
            this.blockResistance = hardness * 5.0F;
        }

        return this;
    }

    public Block setBlockUnbreakable() {
        this.setHardness(-1.0F);
        return this;
    }

    public float getBlockHardness(World worldIn, int x, int y, int z) {
        return this.blockHardness;
    }

    public Block setTickRandomly(boolean shouldTick) {
        this.needsRandomTick = shouldTick;
        return this;
    }

    public boolean getTickRandomly() {
        return this.needsRandomTick;
    }

    /** @deprecated */
    @Deprecated
    public boolean hasTileEntity() {
        return this.hasTileEntity(0);
    }

    public final void setBlockBounds(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        this.minX = (double)minX;
        this.minY = (double)minY;
        this.minZ = (double)minZ;
        this.maxX = (double)maxX;
        this.maxY = (double)maxY;
        this.maxZ = (double)maxZ;
    }

    @SideOnly(Side.CLIENT)
    public int getMixedBrightnessForBlock(IBlockAccess worldIn, int x, int y, int z) {
        Block block = worldIn.getBlock(x, y, z);
        int l = worldIn.getLightBrightnessForSkyBlocks(x, y, z, block.getLightValue(worldIn, x, y, z));
        if (l == 0 && block instanceof BlockSlab) {
            --y;
            block = worldIn.getBlock(x, y, z);
            return worldIn.getLightBrightnessForSkyBlocks(x, y, z, block.getLightValue(worldIn, x, y, z));
        } else {
            return l;
        }
    }

    @SideOnly(Side.CLIENT)
    public boolean shouldSideBeRendered(IBlockAccess worldIn, int x, int y, int z, int side) {
        return side == 0 && this.minY > 0.0D ? true : (side == 1 && this.maxY < 1.0D ? true : (side == 2 && this.minZ > 0.0D ? true : (side == 3 && this.maxZ < 1.0D ? true : (side == 4 && this.minX > 0.0D ? true : (side == 5 && this.maxX < 1.0D ? true : !worldIn.getBlock(x, y, z).isOpaqueCube())))));
    }

    public boolean isBlockSolid(IBlockAccess worldIn, int x, int y, int z, int side) {
        return worldIn.getBlock(x, y, z).getMaterial().isSolid();
    }

    @SideOnly(Side.CLIENT)
    public IIcon getIcon(IBlockAccess worldIn, int x, int y, int z, int side) {
        return this.getIcon(side, worldIn.getBlockMetadata(x, y, z));
    }

    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        return this.blockIcon;
    }

    public void addCollisionBoxesToList(World worldIn, int x, int y, int z, AxisAlignedBB mask, List list, Entity collider) {
        AxisAlignedBB axisalignedbb1 = this.getCollisionBoundingBoxFromPool(worldIn, x, y, z);
        if (axisalignedbb1 != null && mask.intersectsWith(axisalignedbb1)) {
            list.add(axisalignedbb1);
        }

    }

    public AxisAlignedBB getCollisionBoundingBoxFromPool(World worldIn, int x, int y, int z) {
        return AxisAlignedBB.getBoundingBox((double)x + this.minX, (double)y + this.minY, (double)z + this.minZ, (double)x + this.maxX, (double)y + this.maxY, (double)z + this.maxZ);
    }

    @SideOnly(Side.CLIENT)
    public final IIcon getBlockTextureFromSide(int side) {
        return this.getIcon(side, 0);
    }

    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getSelectedBoundingBoxFromPool(World worldIn, int x, int y, int z) {
        return AxisAlignedBB.getBoundingBox((double)x + this.minX, (double)y + this.minY, (double)z + this.minZ, (double)x + this.maxX, (double)y + this.maxY, (double)z + this.maxZ);
    }

    public boolean isOpaqueCube() {
        return true;
    }

    public boolean canCollideCheck(int meta, boolean includeLiquid) {
        return this.isCollidable();
    }

    public boolean isCollidable() {
        return true;
    }

    public void updateTick(World worldIn, int x, int y, int z, Random random) {
    }

    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(World worldIn, int x, int y, int z, Random random) {
    }

    public void onBlockDestroyedByPlayer(World worldIn, int x, int y, int z, int meta) {
    }

    public void onNeighborBlockChange(World worldIn, int x, int y, int z, Block neighbor) {
    }

    public int tickRate(World worldIn) {
        return 10;
    }

    public void onBlockAdded(World worldIn, int x, int y, int z) {
    }

    public void breakBlock(World worldIn, int x, int y, int z, Block blockBroken, int meta) {
        if (this.hasTileEntity(meta) && !(this instanceof BlockContainer)) {
            worldIn.removeTileEntity(x, y, z);
        }

    }

    public int quantityDropped(Random random) {
        return 1;
    }

    public Item getItemDropped(int meta, Random random, int fortune) {
        return Item.getItemFromBlock(this);
    }

    public float getPlayerRelativeBlockHardness(EntityPlayer player, World worldIn, int x, int y, int z) {
        return ForgeHooks.blockStrength(this, player, worldIn, x, y, z);
    }

    public final void dropBlockAsItem(World worldIn, int x, int y, int z, int meta, int fortune) {
        this.dropBlockAsItemWithChance(worldIn, x, y, z, meta, 1.0F, fortune);
    }

    public void dropBlockAsItemWithChance(World worldIn, int x, int y, int z, int meta, float chance, int fortune) {
        if (!worldIn.isRemote && !worldIn.restoringBlockSnapshots) {
            ArrayList<ItemStack> items = this.getDrops(worldIn, x, y, z, meta, fortune);
            chance = ForgeEventFactory.fireBlockHarvesting(items, worldIn, this, x, y, z, meta, fortune, chance, false, (EntityPlayer)this.harvesters.get());
            Iterator var9 = items.iterator();

            while(var9.hasNext()) {
                ItemStack item = (ItemStack)var9.next();
                if (worldIn.rand.nextFloat() <= chance) {
                    this.dropBlockAsItem(worldIn, x, y, z, item);
                }
            }
        }

    }

    public void dropBlockAsItem(World worldIn, int x, int y, int z, ItemStack itemIn) {
        if (!worldIn.isRemote && worldIn.getGameRules().getGameRuleBooleanValue("doTileDrops") && !worldIn.restoringBlockSnapshots) {
            if ((Boolean)this.captureDrops.get()) {
                ((List)this.capturedDrops.get()).add(itemIn);
                return;
            }

            float f = 0.7F;
            double d0 = (double)(worldIn.rand.nextFloat() * f) + (double)(1.0F - f) * 0.5D;
            double d1 = (double)(worldIn.rand.nextFloat() * f) + (double)(1.0F - f) * 0.5D;
            double d2 = (double)(worldIn.rand.nextFloat() * f) + (double)(1.0F - f) * 0.5D;
            EntityItem entityitem = new EntityItem(worldIn, (double)x + d0, (double)y + d1, (double)z + d2, itemIn);
            entityitem.delayBeforeCanPickup = 10;
            worldIn.spawnEntityInWorld(entityitem);
        }

    }

    public void dropXpOnBlockBreak(World worldIn, int x, int y, int z, int amount) {
        if (!worldIn.isRemote) {
            while(amount > 0) {
                int i1 = EntityXPOrb.getXPSplit(amount);
                amount -= i1;
                worldIn.spawnEntityInWorld(new EntityXPOrb(worldIn, (double)x + 0.5D, (double)y + 0.5D, (double)z + 0.5D, i1));
            }
        }

    }

    public int damageDropped(int meta) {
        return 0;
    }

    public float getExplosionResistance(Entity exploder) {
        return this.blockResistance / 5.0F;
    }

    public MovingObjectPosition collisionRayTrace(World worldIn, int x, int y, int z, Vec3 startVec, Vec3 endVec) {
        this.setBlockBoundsBasedOnState(worldIn, x, y, z);
        startVec = startVec.addVector((double)(-x), (double)(-y), (double)(-z));
        endVec = endVec.addVector((double)(-x), (double)(-y), (double)(-z));
        Vec3 vec32 = startVec.getIntermediateWithXValue(endVec, this.minX);
        Vec3 vec33 = startVec.getIntermediateWithXValue(endVec, this.maxX);
        Vec3 vec34 = startVec.getIntermediateWithYValue(endVec, this.minY);
        Vec3 vec35 = startVec.getIntermediateWithYValue(endVec, this.maxY);
        Vec3 vec36 = startVec.getIntermediateWithZValue(endVec, this.minZ);
        Vec3 vec37 = startVec.getIntermediateWithZValue(endVec, this.maxZ);
        if (!this.isVecInsideYZBounds(vec32)) {
            vec32 = null;
        }

        if (!this.isVecInsideYZBounds(vec33)) {
            vec33 = null;
        }

        if (!this.isVecInsideXZBounds(vec34)) {
            vec34 = null;
        }

        if (!this.isVecInsideXZBounds(vec35)) {
            vec35 = null;
        }

        if (!this.isVecInsideXYBounds(vec36)) {
            vec36 = null;
        }

        if (!this.isVecInsideXYBounds(vec37)) {
            vec37 = null;
        }

        Vec3 vec38 = null;
        if (vec32 != null && (vec38 == null || startVec.squareDistanceTo(vec32) < startVec.squareDistanceTo(vec38))) {
            vec38 = vec32;
        }

        if (vec33 != null && (vec38 == null || startVec.squareDistanceTo(vec33) < startVec.squareDistanceTo(vec38))) {
            vec38 = vec33;
        }

        if (vec34 != null && (vec38 == null || startVec.squareDistanceTo(vec34) < startVec.squareDistanceTo(vec38))) {
            vec38 = vec34;
        }

        if (vec35 != null && (vec38 == null || startVec.squareDistanceTo(vec35) < startVec.squareDistanceTo(vec38))) {
            vec38 = vec35;
        }

        if (vec36 != null && (vec38 == null || startVec.squareDistanceTo(vec36) < startVec.squareDistanceTo(vec38))) {
            vec38 = vec36;
        }

        if (vec37 != null && (vec38 == null || startVec.squareDistanceTo(vec37) < startVec.squareDistanceTo(vec38))) {
            vec38 = vec37;
        }

        if (vec38 == null) {
            return null;
        } else {
            byte b0 = -1;
            if (vec38 == vec32) {
                b0 = 4;
            }

            if (vec38 == vec33) {
                b0 = 5;
            }

            if (vec38 == vec34) {
                b0 = 0;
            }

            if (vec38 == vec35) {
                b0 = 1;
            }

            if (vec38 == vec36) {
                b0 = 2;
            }

            if (vec38 == vec37) {
                b0 = 3;
            }

            return new MovingObjectPosition(x, y, z, b0, vec38.addVector((double)x, (double)y, (double)z));
        }
    }

    private boolean isVecInsideYZBounds(Vec3 point) {
        return point == null ? false : point.yCoord >= this.minY && point.yCoord <= this.maxY && point.zCoord >= this.minZ && point.zCoord <= this.maxZ;
    }

    private boolean isVecInsideXZBounds(Vec3 point) {
        return point == null ? false : point.xCoord >= this.minX && point.xCoord <= this.maxX && point.zCoord >= this.minZ && point.zCoord <= this.maxZ;
    }

    private boolean isVecInsideXYBounds(Vec3 point) {
        return point == null ? false : point.xCoord >= this.minX && point.xCoord <= this.maxX && point.yCoord >= this.minY && point.yCoord <= this.maxY;
    }

    public void onBlockDestroyedByExplosion(World worldIn, int x, int y, int z, Explosion explosionIn) {
    }

    public boolean canReplace(World worldIn, int x, int y, int z, int side, ItemStack itemIn) {
        return this.canPlaceBlockOnSide(worldIn, x, y, z, side);
    }

    @SideOnly(Side.CLIENT)
    public int getRenderBlockPass() {
        return 0;
    }

    public boolean canPlaceBlockOnSide(World worldIn, int x, int y, int z, int side) {
        return this.canPlaceBlockAt(worldIn, x, y, z);
    }

    public boolean canPlaceBlockAt(World worldIn, int x, int y, int z) {
        return worldIn.getBlock(x, y, z).isReplaceable(worldIn, x, y, z);
    }

    public boolean onBlockActivated(World worldIn, int x, int y, int z, EntityPlayer player, int side, float subX, float subY, float subZ) {
        return false;
    }

    public void onEntityWalking(World worldIn, int x, int y, int z, Entity entityIn) {
    }

    public int onBlockPlaced(World worldIn, int x, int y, int z, int side, float subX, float subY, float subZ, int meta) {
        return meta;
    }

    public void onBlockClicked(World worldIn, int x, int y, int z, EntityPlayer player) {
    }

    public void velocityToAddToEntity(World worldIn, int x, int y, int z, Entity entityIn, Vec3 velocity) {
    }

    public void setBlockBoundsBasedOnState(IBlockAccess worldIn, int x, int y, int z) {
    }

    public final double getBlockBoundsMinX() {
        return this.minX;
    }

    public final double getBlockBoundsMaxX() {
        return this.maxX;
    }

    public final double getBlockBoundsMinY() {
        return this.minY;
    }

    public final double getBlockBoundsMaxY() {
        return this.maxY;
    }

    public final double getBlockBoundsMinZ() {
        return this.minZ;
    }

    public final double getBlockBoundsMaxZ() {
        return this.maxZ;
    }

    @SideOnly(Side.CLIENT)
    public int getBlockColor() {
        return 16777215;
    }

    @SideOnly(Side.CLIENT)
    public int getRenderColor(int meta) {
        return 16777215;
    }

    @SideOnly(Side.CLIENT)
    public int colorMultiplier(IBlockAccess worldIn, int x, int y, int z) {
        return 16777215;
    }

    public int isProvidingWeakPower(IBlockAccess worldIn, int x, int y, int z, int side) {
        return 0;
    }

    public boolean canProvidePower() {
        return false;
    }

    public void onEntityCollidedWithBlock(World worldIn, int x, int y, int z, Entity entityIn) {
    }

    public int isProvidingStrongPower(IBlockAccess worldIn, int x, int y, int z, int side) {
        return 0;
    }

    public void setBlockBoundsForItemRender() {
    }

    public void harvestBlock(World worldIn, EntityPlayer player, int x, int y, int z, int meta) {
        player.addStat(StatList.mineBlockStatArray[getIdFromBlock(this)], 1);
        player.addExhaustion(0.025F);
        if (this.canSilkHarvest(worldIn, player, x, y, z, meta) && EnchantmentHelper.getSilkTouchModifier(player)) {
            ArrayList<ItemStack> items = new ArrayList();
            ItemStack itemstack = this.createStackedBlock(meta);
            if (itemstack != null) {
                items.add(itemstack);
            }

            ForgeEventFactory.fireBlockHarvesting(items, worldIn, this, x, y, z, meta, 0, 1.0F, true, player);
            Iterator var9 = items.iterator();

            while(var9.hasNext()) {
                ItemStack is = (ItemStack)var9.next();
                this.dropBlockAsItem(worldIn, x, y, z, is);
            }
        } else {
            this.harvesters.set(player);
            int i1 = EnchantmentHelper.getFortuneModifier(player);
            this.dropBlockAsItem(worldIn, x, y, z, meta, i1);
            this.harvesters.set((Object)null);
        }

    }

    protected boolean canSilkHarvest() {
        Integer meta = (Integer)this.silk_check_meta.get();
        return this.renderAsNormalBlock() && !this.hasTileEntity(meta == null ? 0 : meta);
    }

    protected ItemStack createStackedBlock(int meta) {
        int j = 0;
        Item item = Item.getItemFromBlock(this);
        if (item != null && item.getHasSubtypes()) {
            j = meta;
        }

        return new ItemStack(item, 1, j);
    }

    public int quantityDroppedWithBonus(int maxBonus, Random random) {
        return this.quantityDropped(random);
    }

    public boolean canBlockStay(World worldIn, int x, int y, int z) {
        return true;
    }

    public void onBlockPlacedBy(World worldIn, int x, int y, int z, EntityLivingBase placer, ItemStack itemIn) {
    }

    public void onPostBlockPlaced(World worldIn, int x, int y, int z, int meta) {
    }

    public Block setBlockName(String name) {
        this.unlocalizedName = name;
        return this;
    }

    public String getLocalizedName() {
        return StatCollector.translateToLocal(this.getUnlocalizedName() + ".name");
    }

    public String getUnlocalizedName() {
        return "tile." + this.unlocalizedName;
    }

    public boolean onBlockEventReceived(World worldIn, int x, int y, int z, int eventId, int eventData) {
        return false;
    }

    public boolean getEnableStats() {
        return this.enableStats;
    }

    protected Block disableStats() {
        this.enableStats = false;
        return this;
    }

    public int getMobilityFlag() {
        return this.blockMaterial.getMaterialMobility();
    }

    @SideOnly(Side.CLIENT)
    public float getAmbientOcclusionLightValue() {
        return this.isBlockNormalCube() ? 0.2F : 1.0F;
    }

    public void onFallenUpon(World worldIn, int x, int y, int z, Entity entityIn, float fallDistance) {
    }

    @SideOnly(Side.CLIENT)
    public Item getItem(World worldIn, int x, int y, int z) {
        return Item.getItemFromBlock(this);
    }

    public int getDamageValue(World worldIn, int x, int y, int z) {
        return this.damageDropped(worldIn.getBlockMetadata(x, y, z));
    }

    @SideOnly(Side.CLIENT)
    public void getSubBlocks(Item itemIn, CreativeTabs tab, List list) {
        list.add(new ItemStack(itemIn, 1, 0));
    }

    public Block setCreativeTab(CreativeTabs tab) {
        this.displayOnCreativeTab = tab;
        return this;
    }

    public void onBlockHarvested(World worldIn, int x, int y, int z, int meta, EntityPlayer player) {
    }

    @SideOnly(Side.CLIENT)
    public CreativeTabs getCreativeTabToDisplayOn() {
        return this.displayOnCreativeTab;
    }

    public void onBlockPreDestroy(World worldIn, int x, int y, int z, int meta) {
    }

    public void fillWithRain(World worldIn, int x, int y, int z) {
    }

    @SideOnly(Side.CLIENT)
    public boolean isFlowerPot() {
        return false;
    }

    public boolean func_149698_L() {
        return true;
    }

    public boolean canDropFromExplosion(Explosion explosionIn) {
        return true;
    }

    public boolean isAssociatedBlock(Block other) {
        return this == other;
    }

    public static boolean isEqualTo(Block blockIn, Block other) {
        return blockIn != null && other != null ? (blockIn == other ? true : blockIn.isAssociatedBlock(other)) : false;
    }

    public boolean hasComparatorInputOverride() {
        return false;
    }

    public int getComparatorInputOverride(World worldIn, int x, int y, int z, int side) {
        return 0;
    }

    public Block setBlockTextureName(String textureName) {
        this.textureName = textureName;
        return this;
    }

    @SideOnly(Side.CLIENT)
    protected String getTextureName() {
        return this.textureName == null ? "MISSING_ICON_BLOCK_" + getIdFromBlock(this) + "_" + this.unlocalizedName : this.textureName;
    }

    @SideOnly(Side.CLIENT)
    public IIcon func_149735_b(int side, int meta) {
        return this.getIcon(side, meta);
    }

    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister reg) {
        this.blockIcon = reg.registerIcon(this.getTextureName());
    }

    @SideOnly(Side.CLIENT)
    public String getItemIconName() {
        return null;
    }

    public int getLightValue(IBlockAccess world, int x, int y, int z) {
        Block block = world.getBlock(x, y, z);
        return block != this ? block.getLightValue(world, x, y, z) : this.getLightValue();
    }

    public boolean isLadder(IBlockAccess world, int x, int y, int z, EntityLivingBase entity) {
        return false;
    }

    public boolean isNormalCube(IBlockAccess world, int x, int y, int z) {
        return this.getMaterial().isOpaque() && this.renderAsNormalBlock() && !this.canProvidePower();
    }

    public boolean isSideSolid(IBlockAccess world, int x, int y, int z, ForgeDirection side) {
        int meta = world.getBlockMetadata(x, y, z);
        if (this instanceof BlockSlab) {
            return (meta & 8) == 8 && side == ForgeDirection.UP || this.func_149730_j();
        } else if (this instanceof BlockFarmland) {
            return side != ForgeDirection.DOWN && side != ForgeDirection.UP;
        } else if (this instanceof BlockStairs) {
            boolean flipped = (meta & 4) != 0;
            return (meta & 3) + side.ordinal() == 5 || side == ForgeDirection.UP && flipped;
        } else if (this instanceof BlockSnow) {
            return (meta & 7) == 7;
        } else if (this instanceof BlockHopper && side == ForgeDirection.UP) {
            return true;
        } else {
            return this instanceof BlockCompressedPowered ? true : this.isNormalCube(world, x, y, z);
        }
    }

    public boolean isReplaceable(IBlockAccess world, int x, int y, int z) {
        return this.blockMaterial.isReplaceable();
    }

    public boolean isBurning(IBlockAccess world, int x, int y, int z) {
        return false;
    }

    public boolean isAir(IBlockAccess world, int x, int y, int z) {
        return this.getMaterial() == Material.air;
    }

    public boolean canHarvestBlock(EntityPlayer player, int meta) {
        return ForgeHooks.canHarvestBlock(this, player, meta);
    }

    public boolean removedByPlayer(World world, EntityPlayer player, int x, int y, int z, boolean willHarvest) {
        return this.removedByPlayer(world, player, x, y, z);
    }

    /** @deprecated */
    @Deprecated
    public boolean removedByPlayer(World world, EntityPlayer player, int x, int y, int z) {
        return world.setBlockToAir(x, y, z);
    }

    public int getFlammability(IBlockAccess world, int x, int y, int z, ForgeDirection face) {
        return Blocks.fire.getFlammability(this);
    }

    public boolean isFlammable(IBlockAccess world, int x, int y, int z, ForgeDirection face) {
        return this.getFlammability(world, x, y, z, face) > 0;
    }

    public int getFireSpreadSpeed(IBlockAccess world, int x, int y, int z, ForgeDirection face) {
        return Blocks.fire.getEncouragement(this);
    }

    public boolean isFireSource(World world, int x, int y, int z, ForgeDirection side) {
        if (this == Blocks.netherrack && side == ForgeDirection.UP) {
            return true;
        } else {
            return world.provider instanceof WorldProviderEnd && this == Blocks.bedrock && side == ForgeDirection.UP;
        }
    }

    public boolean hasTileEntity(int metadata) {
        return this.isTileProvider;
    }

    public TileEntity createTileEntity(World world, int metadata) {
        return this.isTileProvider ? ((ITileEntityProvider)this).createNewTileEntity(world, metadata) : null;
    }

    public int quantityDropped(int meta, int fortune, Random random) {
        return this.quantityDroppedWithBonus(fortune, random);
    }

    public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune) {
        ArrayList<ItemStack> ret = new ArrayList();
        int count = this.quantityDropped(metadata, fortune, world.rand);

        for(int i = 0; i < count; ++i) {
            Item item = this.getItemDropped(metadata, world.rand, fortune);
            if (item != null) {
                ret.add(new ItemStack(item, 1, this.damageDropped(metadata)));
            }
        }

        return ret;
    }

    public boolean canSilkHarvest(World world, EntityPlayer player, int x, int y, int z, int metadata) {
        this.silk_check_meta.set(metadata);
        boolean ret = this.canSilkHarvest();
        this.silk_check_meta.set((Object)null);
        return ret;
    }

    public boolean canCreatureSpawn(EnumCreatureType type, IBlockAccess world, int x, int y, int z) {
        int meta = world.getBlockMetadata(x, y, z);
        if (!(this instanceof BlockSlab)) {
            if (this instanceof BlockStairs) {
                return (meta & 4) != 0;
            } else {
                return this.isSideSolid(world, x, y, z, ForgeDirection.UP);
            }
        } else {
            return (meta & 8) == 8 || this.func_149730_j();
        }
    }

    public boolean isBed(IBlockAccess world, int x, int y, int z, EntityLivingBase player) {
        return this == Blocks.bed;
    }

    public ChunkCoordinates getBedSpawnPosition(IBlockAccess world, int x, int y, int z, EntityPlayer player) {
        return world instanceof World ? BlockBed.func_149977_a((World)world, x, y, z, 0) : null;
    }

    public void setBedOccupied(IBlockAccess world, int x, int y, int z, EntityPlayer player, boolean occupied) {
        if (world instanceof World) {
            BlockBed.func_149979_a((World)world, x, y, z, occupied);
        }

    }

    public int getBedDirection(IBlockAccess world, int x, int y, int z) {
        return BlockBed.getDirection(world.getBlockMetadata(x, y, z));
    }

    public boolean isBedFoot(IBlockAccess world, int x, int y, int z) {
        return BlockBed.isBlockHeadOfBed(world.getBlockMetadata(x, y, z));
    }

    public void beginLeavesDecay(World world, int x, int y, int z) {
    }

    public boolean canSustainLeaves(IBlockAccess world, int x, int y, int z) {
        return false;
    }

    public boolean isLeaves(IBlockAccess world, int x, int y, int z) {
        return this.getMaterial() == Material.leaves;
    }

    public boolean canBeReplacedByLeaves(IBlockAccess world, int x, int y, int z) {
        return !this.func_149730_j();
    }

    public boolean isWood(IBlockAccess world, int x, int y, int z) {
        return false;
    }

    public boolean isReplaceableOreGen(World world, int x, int y, int z, Block target) {
        return this == target;
    }

    public float getExplosionResistance(Entity par1Entity, World world, int x, int y, int z, double explosionX, double explosionY, double explosionZ) {
        return this.getExplosionResistance(par1Entity);
    }

    public void onBlockExploded(World world, int x, int y, int z, Explosion explosion) {
        world.setBlockToAir(x, y, z);
        this.onBlockDestroyedByExplosion(world, x, y, z, explosion);
    }

    public boolean canConnectRedstone(IBlockAccess world, int x, int y, int z, int side) {
        return this.canProvidePower() && side != -1;
    }

    public boolean canPlaceTorchOnTop(World world, int x, int y, int z) {
        if (this.isSideSolid(world, x, y, z, ForgeDirection.UP)) {
            return true;
        } else {
            return this == Blocks.fence || this == Blocks.nether_brick_fence || this == Blocks.glass || this == Blocks.cobblestone_wall;
        }
    }

    public boolean canRenderInPass(int pass) {
        return pass == this.getRenderBlockPass();
    }

    public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z, EntityPlayer player) {
        return this.getPickBlock(target, world, x, y, z);
    }

    /** @deprecated */
    @Deprecated
    public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z) {
        Item item = this.getItem(world, x, y, z);
        if (item == null) {
            return null;
        } else {
            Block block = item instanceof ItemBlock && !this.isFlowerPot() ? getBlockFromItem(item) : this;
            return new ItemStack(item, 1, block.getDamageValue(world, x, y, z));
        }
    }

    public boolean isFoliage(IBlockAccess world, int x, int y, int z) {
        return false;
    }

    @SideOnly(Side.CLIENT)
    public boolean addHitEffects(World worldObj, MovingObjectPosition target, EffectRenderer effectRenderer) {
        return false;
    }

    @SideOnly(Side.CLIENT)
    public boolean addDestroyEffects(World world, int x, int y, int z, int meta, EffectRenderer effectRenderer) {
        return false;
    }

    public boolean canSustainPlant(IBlockAccess world, int x, int y, int z, ForgeDirection direction, IPlantable plantable) {
        Block plant = plantable.getPlant(world, x, y + 1, z);
        EnumPlantType plantType = plantable.getPlantType(world, x, y + 1, z);
        if (plant == Blocks.cactus && this == Blocks.cactus) {
            return true;
        } else if (plant == Blocks.reeds && this == Blocks.reeds) {
            return true;
        } else if (plantable instanceof BlockBush && ((BlockBush)plantable).canPlaceBlockOn(this)) {
            return true;
        } else {
            switch(6.$SwitchMap$net$minecraftforge$common$EnumPlantType[plantType.ordinal()]) {
            case 1:
                return this == Blocks.sand;
            case 2:
                return this == Blocks.soul_sand;
            case 3:
                return this == Blocks.farmland;
            case 4:
                return this.isSideSolid(world, x, y, z, ForgeDirection.UP);
            case 5:
                return this == Blocks.grass || this == Blocks.dirt || this == Blocks.farmland;
            case 6:
                return world.getBlock(x, y, z).getMaterial() == Material.water && world.getBlockMetadata(x, y, z) == 0;
            case 7:
                boolean isBeach = this == Blocks.grass || this == Blocks.dirt || this == Blocks.sand;
                boolean hasWater = world.getBlock(x - 1, y, z).getMaterial() == Material.water || world.getBlock(x + 1, y, z).getMaterial() == Material.water || world.getBlock(x, y, z - 1).getMaterial() == Material.water || world.getBlock(x, y, z + 1).getMaterial() == Material.water;
                return isBeach && hasWater;
            default:
                return false;
            }
        }
    }

    public void onPlantGrow(World world, int x, int y, int z, int sourceX, int sourceY, int sourceZ) {
        if (this == Blocks.grass || this == Blocks.farmland) {
            world.setBlock(x, y, z, Blocks.dirt, 0, 2);
        }

    }

    public boolean isFertile(World world, int x, int y, int z) {
        if (this == Blocks.farmland) {
            return world.getBlockMetadata(x, y, z) > 0;
        } else {
            return false;
        }
    }

    public int getLightOpacity(IBlockAccess world, int x, int y, int z) {
        return this.getLightOpacity();
    }

    public boolean canEntityDestroy(IBlockAccess world, int x, int y, int z, Entity entity) {
        if (entity instanceof EntityWither) {
            return this != Blocks.bedrock && this != Blocks.end_portal && this != Blocks.end_portal_frame && this != Blocks.command_block;
        } else if (!(entity instanceof EntityDragon)) {
            return true;
        } else {
            return this != Blocks.obsidian && this != Blocks.end_stone && this != Blocks.bedrock;
        }
    }

    public boolean isBeaconBase(IBlockAccess worldObj, int x, int y, int z, int beaconX, int beaconY, int beaconZ) {
        return this == Blocks.emerald_block || this == Blocks.gold_block || this == Blocks.diamond_block || this == Blocks.iron_block;
    }

    public boolean rotateBlock(World worldObj, int x, int y, int z, ForgeDirection axis) {
        return RotationHelper.rotateVanillaBlock(this, worldObj, x, y, z, axis);
    }

    public ForgeDirection[] getValidRotations(World worldObj, int x, int y, int z) {
        return RotationHelper.getValidVanillaBlockRotations(this);
    }

    public float getEnchantPowerBonus(World world, int x, int y, int z) {
        return this == Blocks.bookshelf ? 1.0F : 0.0F;
    }

    public boolean recolourBlock(World world, int x, int y, int z, ForgeDirection side, int colour) {
        if (this == Blocks.wool) {
            int meta = world.getBlockMetadata(x, y, z);
            if (meta != colour) {
                world.setBlockMetadataWithNotify(x, y, z, colour, 3);
                return true;
            }
        }

        return false;
    }

    public int getExpDrop(IBlockAccess world, int metadata, int fortune) {
        return 0;
    }

    public void onNeighborChange(IBlockAccess world, int x, int y, int z, int tileX, int tileY, int tileZ) {
    }

    public boolean shouldCheckWeakPower(IBlockAccess world, int x, int y, int z, int side) {
        return this.isNormalCube();
    }

    public boolean getWeakChanges(IBlockAccess world, int x, int y, int z) {
        return false;
    }

    public void setHarvestLevel(String toolClass, int level) {
        for(int m = 0; m < 16; ++m) {
            this.setHarvestLevel(toolClass, level, m);
        }

    }

    public void setHarvestLevel(String toolClass, int level, int metadata) {
        this.harvestTool[metadata] = toolClass;
        this.harvestLevel[metadata] = level;
    }

    public String getHarvestTool(int metadata) {
        return this.harvestTool[metadata];
    }

    public int getHarvestLevel(int metadata) {
        return this.harvestLevel[metadata];
    }

    public boolean isToolEffective(String type, int metadata) {
        if (!"pickaxe".equals(type) || this != Blocks.redstone_ore && this != Blocks.lit_redstone_ore && this != Blocks.obsidian) {
            return this.harvestTool[metadata] == null ? false : this.harvestTool[metadata].equals(type);
        } else {
            return false;
        }
    }

    protected List<ItemStack> captureDrops(boolean start) {
        if (start) {
            this.captureDrops.set(true);
            ((List)this.capturedDrops.get()).clear();
            return null;
        } else {
            this.captureDrops.set(false);
            return (List)this.capturedDrops.get();
        }
    }

    @MixinMerged(
        mixin = "com.mitchej123.hodgepodge.mixins.early.minecraft.MixinBlock_FixXray",
        priority = 1000,
        sessionId = "98d213e4-85ca-4ce4-959f-84858add440a"
    )
    public boolean hodgepodge$shouldRayTraceStopOnBlock(World worldIn, int x, int y, int z) {
        return this.getCollisionBoundingBoxFromPool(worldIn, x, y, z) != null;
    }
}
