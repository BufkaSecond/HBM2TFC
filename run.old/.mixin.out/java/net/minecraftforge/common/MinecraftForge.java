package net.minecraftforge.common;

import com.google.common.collect.ObjectArrays;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.eventhandler.EventBus;
import cpw.mods.fml.relauncher.Side;
import java.util.concurrent.Callable;
import net.minecraft.crash.CrashReport;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ForgeHooks.SeedEntry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.oredict.OreDictionary;
import org.spongepowered.asm.mixin.transformer.meta.MixinMerged;

public class MinecraftForge {
    public static final EventBus EVENT_BUS = new EventBus();
    public static final EventBus TERRAIN_GEN_BUS = new EventBus();
    public static final EventBus ORE_GEN_BUS = new EventBus();
    public static final String MC_VERSION = "1.7.10";
    static final ForgeInternalHandler INTERNAL_HANDLER = new ForgeInternalHandler();

    public static void addGrassSeed(ItemStack seed, int weight) {
        ForgeHooks.seedList.add(new SeedEntry(seed, weight));
    }

    public static void initialize() {
        FMLLog.info("MinecraftForge v%s Initialized", new Object[]{ForgeVersion.getVersion()});
        OreDictionary.getOreName(0);
        new CrashReport("ThisIsFake", new Exception("Not real"));
        String[] handlers = new String[]{"net.minecraft.world.World$1", "net.minecraft.world.World$2", "net.minecraft.world.World$3", "net.minecraft.world.World$4", "net.minecraft.world.chunk.Chunk$1", "net.minecraft.crash.CrashReportCategory$1", "net.minecraft.crash.CrashReportCategory$2", "net.minecraft.crash.CrashReportCategory$3", "net.minecraft.entity.Entity$1", "net.minecraft.entity.Entity$2", "net.minecraft.entity.EntityTracker$1", "net.minecraft.world.gen.layer.GenLayer$1", "net.minecraft.world.gen.layer.GenLayer$2", "net.minecraft.entity.player.InventoryPlayer$1", "net.minecraft.world.gen.structure.MapGenStructure$1", "net.minecraft.world.gen.structure.MapGenStructure$2", "net.minecraft.world.gen.structure.MapGenStructure$3", "net.minecraft.server.MinecraftServer$3", "net.minecraft.server.MinecraftServer$4", "net.minecraft.server.MinecraftServer$5", "net.minecraft.nbt.NBTTagCompound$1", "net.minecraft.nbt.NBTTagCompound$2", "net.minecraft.network.NetHandlerPlayServer$2", "net.minecraft.network.NetworkSystem$3", "net.minecraft.tileentity.TileEntity$1", "net.minecraft.tileentity.TileEntity$2", "net.minecraft.tileentity.TileEntity$3", "net.minecraft.world.storage.WorldInfo$1", "net.minecraft.world.storage.WorldInfo$2", "net.minecraft.world.storage.WorldInfo$3", "net.minecraft.world.storage.WorldInfo$4", "net.minecraft.world.storage.WorldInfo$5", "net.minecraft.world.storage.WorldInfo$6", "net.minecraft.world.storage.WorldInfo$7", "net.minecraft.world.storage.WorldInfo$8", "net.minecraft.world.storage.WorldInfo$9"};
        String[] client = new String[]{"net.minecraft.client.Minecraft$3", "net.minecraft.client.Minecraft$4", "net.minecraft.client.Minecraft$5", "net.minecraft.client.Minecraft$6", "net.minecraft.client.Minecraft$7", "net.minecraft.client.Minecraft$8", "net.minecraft.client.Minecraft$9", "net.minecraft.client.Minecraft$10", "net.minecraft.client.Minecraft$11", "net.minecraft.client.Minecraft$12", "net.minecraft.client.Minecraft$13", "net.minecraft.client.Minecraft$14", "net.minecraft.client.Minecraft$15", "net.minecraft.client.multiplayer.WorldClient$1", "net.minecraft.client.multiplayer.WorldClient$2", "net.minecraft.client.multiplayer.WorldClient$3", "net.minecraft.client.multiplayer.WorldClient$4", constant$zdg000$hodgepodge$EffectRenderer$1("net.minecraft.client.particle,EffectRenderer$1"), constant$zdg000$hodgepodge$EffectRenderer$2("net.minecraft.client.particle,EffectRenderer$2"), constant$zdg000$hodgepodge$EffectRenderer$3("net.minecraft.client.particle,EffectRenderer$3"), constant$zdg000$hodgepodge$EffectRenderer$4("net.minecraft.client.particle,EffectRenderer$4"), "net.minecraft.client.renderer.EntityRenderer$1", "net.minecraft.client.renderer.EntityRenderer$2", "net.minecraft.client.renderer.EntityRenderer$3", "net.minecraft.server.integrated.IntegratedServer$1", "net.minecraft.server.integrated.IntegratedServer$2", "net.minecraft.client.renderer.RenderGlobal$1", "net.minecraft.client.renderer.entity.RenderItem$1", "net.minecraft.client.renderer.entity.RenderItem$2", "net.minecraft.client.renderer.entity.RenderItem$3", "net.minecraft.client.renderer.entity.RenderItem$4", "net.minecraft.client.renderer.texture.TextureAtlasSprite$1", "net.minecraft.client.renderer.texture.TextureManager$1", "net.minecraft.client.renderer.texture.TextureMap$1", "net.minecraft.client.renderer.texture.TextureMap$2", "net.minecraft.client.renderer.texture.TextureMap$3"};
        String[] server = new String[]{"net.minecraft.server.dedicated.DedicatedServer$3", "net.minecraft.server.dedicated.DedicatedServer$4"};
        if (FMLCommonHandler.instance().getSide() == Side.CLIENT) {
            handlers = (String[])ObjectArrays.concat(handlers, client, String.class);
        } else {
            handlers = (String[])ObjectArrays.concat(handlers, server, String.class);
        }

        String[] var4 = handlers;
        int var5 = handlers.length;

        for(int var6 = 0; var6 < var5; ++var6) {
            String s = var4[var6];

            try {
                Class cls = Class.forName(s, false, MinecraftForge.class.getClassLoader());
                if (cls != null && !Callable.class.isAssignableFrom(cls)) {
                }
            } catch (Exception var9) {
            }
        }

        UsernameCache.load();
        FluidRegistry.validateFluidRegistry();
    }

    public static String getBrandingVersion() {
        return "Minecraft Forge " + ForgeVersion.getVersion();
    }

    @MixinMerged(
        mixin = "com.mitchej123.hodgepodge.mixins.early.forge.MixinMinecraftForge",
        priority = 1000,
        sessionId = "98d213e4-85ca-4ce4-959f-84858add440a"
    )
    private static String constant$zdg000$hodgepodge$EffectRenderer$1(String original) {
        return "net.minecraft.client.particle.EffectRenderer$1";
    }

    @MixinMerged(
        mixin = "com.mitchej123.hodgepodge.mixins.early.forge.MixinMinecraftForge",
        priority = 1000,
        sessionId = "98d213e4-85ca-4ce4-959f-84858add440a"
    )
    private static String constant$zdg000$hodgepodge$EffectRenderer$2(String original) {
        return "net.minecraft.client.particle.EffectRenderer$2";
    }

    @MixinMerged(
        mixin = "com.mitchej123.hodgepodge.mixins.early.forge.MixinMinecraftForge",
        priority = 1000,
        sessionId = "98d213e4-85ca-4ce4-959f-84858add440a"
    )
    private static String constant$zdg000$hodgepodge$EffectRenderer$3(String original) {
        return "net.minecraft.client.particle.EffectRenderer$3";
    }

    @MixinMerged(
        mixin = "com.mitchej123.hodgepodge.mixins.early.forge.MixinMinecraftForge",
        priority = 1000,
        sessionId = "98d213e4-85ca-4ce4-959f-84858add440a"
    )
    private static String constant$zdg000$hodgepodge$EffectRenderer$4(String original) {
        return "net.minecraft.client.particle.EffectRenderer$4";
    }
}
