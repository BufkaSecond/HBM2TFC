package net.minecraftforge.fluids;

import com.google.common.base.Strings;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mitchej123.hodgepodge.mixins.early.forge.FluidContainerRegistryAccessor;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.registry.RegistryDelegate;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.FluidRegistry.1;
import net.minecraftforge.fluids.FluidRegistry.2;
import org.apache.logging.log4j.Level;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.transformer.meta.MixinMerged;

public abstract class FluidRegistry {
    static int maxID = 0;
    static BiMap<String, Fluid> fluids = HashBiMap.create();
    static BiMap<Fluid, Integer> fluidIDs = HashBiMap.create();
    static BiMap<Integer, String> fluidNames = HashBiMap.create();
    static BiMap<Block, Fluid> fluidBlocks;
    static BiMap<String, Fluid> masterFluidReference = HashBiMap.create();
    static BiMap<String, String> defaultFluidName = HashBiMap.create();
    static Map<Fluid, net.minecraftforge.fluids.FluidRegistry.FluidDelegate> delegates = Maps.newHashMap();
    public static final Fluid WATER;
    public static final Fluid LAVA;
    public static int renderIdFluid;

    private FluidRegistry() {
    }

    public static void initFluidIDs(BiMap<Fluid, Integer> newfluidIDs, Set<String> defaultNames) {
        maxID = newfluidIDs.size();
        loadFluidDefaults(newfluidIDs, defaultNames);
    }

    private static void loadFluidDefaults(BiMap<Fluid, Integer> localFluidIDs, Set<String> defaultNames) {
        if (defaultNames.isEmpty()) {
            defaultNames.addAll(defaultFluidName.values());
        }

        BiMap<String, Fluid> localFluids = HashBiMap.create(fluids);
        Iterator var3 = defaultNames.iterator();

        while(true) {
            while(var3.hasNext()) {
                String defaultName = (String)var3.next();
                Fluid fluid = (Fluid)masterFluidReference.get(defaultName);
                if (fluid == null) {
                    String derivedName = defaultName.split(":", 2)[1];
                    String localDefault = (String)defaultFluidName.get(derivedName);
                    if (localDefault == null) {
                        FMLLog.getLogger().log(Level.ERROR, "The fluid {} (specified as {}) is missing from this instance - it will be removed", new Object[]{derivedName, defaultName});
                        continue;
                    }

                    fluid = (Fluid)masterFluidReference.get(localDefault);
                    FMLLog.getLogger().log(Level.ERROR, "The fluid {} specified as default is not present - it will be reverted to default {}", new Object[]{defaultName, localDefault});
                }

                FMLLog.getLogger().log(Level.DEBUG, "The fluid {} has been selected as the default fluid for {}", new Object[]{defaultName, fluid.getName()});
                Fluid oldFluid = (Fluid)localFluids.put(fluid.getName(), fluid);
                Integer id = (Integer)localFluidIDs.remove(oldFluid);
                localFluidIDs.put(fluid, id);
            }

            BiMap<Integer, String> localFluidNames = HashBiMap.create();
            Iterator var9 = localFluidIDs.entrySet().iterator();

            while(var9.hasNext()) {
                Entry<Fluid, Integer> e = (Entry)var9.next();
                localFluidNames.put(e.getValue(), ((Fluid)e.getKey()).getName());
            }

            fluidIDs = localFluidIDs;
            fluids = localFluids;
            fluidNames = localFluidNames;
            fluidBlocks = null;
            var9 = delegates.values().iterator();

            while(var9.hasNext()) {
                net.minecraftforge.fluids.FluidRegistry.FluidDelegate fd = (net.minecraftforge.fluids.FluidRegistry.FluidDelegate)var9.next();
                fd.rebind();
            }

            handler$zgd000$hodgepodge$afterLoadFluidDefaults(localFluidIDs, defaultNames, (CallbackInfo)null);
            return;
        }
    }

    public static boolean registerFluid(Fluid fluid) {
        masterFluidReference.put(uniqueName(fluid), fluid);
        delegates.put(fluid, new net.minecraftforge.fluids.FluidRegistry.FluidDelegate(fluid, fluid.getName()));
        if (fluids.containsKey(fluid.getName())) {
            return false;
        } else {
            fluids.put(fluid.getName(), fluid);
            ++maxID;
            fluidIDs.put(fluid, maxID);
            fluidNames.put(maxID, fluid.getName());
            defaultFluidName.put(fluid.getName(), uniqueName(fluid));
            MinecraftForge.EVENT_BUS.post(new net.minecraftforge.fluids.FluidRegistry.FluidRegisterEvent(fluid.getName(), maxID));
            return true;
        }
    }

    private static String uniqueName(Fluid fluid) {
        ModContainer activeModContainer = Loader.instance().activeModContainer();
        String activeModContainerName = activeModContainer == null ? "minecraft" : activeModContainer.getModId();
        return activeModContainerName + ":" + fluid.getName();
    }

    public static boolean isFluidDefault(Fluid fluid) {
        return fluids.containsValue(fluid);
    }

    public static boolean isFluidRegistered(Fluid fluid) {
        return fluid != null && fluids.containsKey(fluid.getName());
    }

    public static boolean isFluidRegistered(String fluidName) {
        return fluids.containsKey(fluidName);
    }

    public static Fluid getFluid(String fluidName) {
        return (Fluid)fluids.get(fluidName);
    }

    public static Fluid getFluid(int fluidID) {
        return (Fluid)fluidIDs.inverse().get(fluidID);
    }

    public static int getFluidID(Fluid fluid) {
        return (Integer)fluidIDs.get(fluid);
    }

    public static int getFluidID(String fluidName) {
        return (Integer)fluidIDs.get(getFluid(fluidName));
    }

    /** @deprecated */
    @Deprecated
    public static String getFluidName(int fluidID) {
        return (String)fluidNames.get(fluidID);
    }

    public static String getFluidName(Fluid fluid) {
        return (String)fluids.inverse().get(fluid);
    }

    public static String getFluidName(FluidStack stack) {
        return getFluidName(stack.getFluid());
    }

    public static FluidStack getFluidStack(String fluidName, int amount) {
        return !fluids.containsKey(fluidName) ? null : new FluidStack(getFluid(fluidName), amount);
    }

    public static Map<String, Fluid> getRegisteredFluids() {
        return ImmutableMap.copyOf(fluids);
    }

    /** @deprecated */
    @Deprecated
    public static Map<String, Integer> getRegisteredFluidIDs() {
        return ImmutableMap.copyOf(fluidNames.inverse());
    }

    public static Map<Fluid, Integer> getRegisteredFluidIDsByFluid() {
        return ImmutableMap.copyOf(fluidIDs);
    }

    public static Fluid lookupFluidForBlock(Block block) {
        if (fluidBlocks == null) {
            BiMap<Block, Fluid> tmp = HashBiMap.create();
            Iterator var2 = fluids.values().iterator();

            while(var2.hasNext()) {
                Fluid fluid = (Fluid)var2.next();
                if (fluid.canBePlacedInWorld() && fluid.getBlock() != null) {
                    tmp.put(fluid.getBlock(), fluid);
                }
            }

            fluidBlocks = tmp;
        }

        return (Fluid)fluidBlocks.get(block);
    }

    public static int getMaxID() {
        return maxID;
    }

    public static String getDefaultFluidName(Fluid key) {
        String name = (String)masterFluidReference.inverse().get(key);
        if (Strings.isNullOrEmpty(name)) {
            FMLLog.getLogger().log(Level.ERROR, "The fluid registry is corrupted. A fluid {} {} is not properly registered. The mod that registered this is broken", new Object[]{key.getClass().getName(), key.getName()});
            throw new IllegalStateException("The fluid registry is corrupted");
        } else {
            return name;
        }
    }

    public static void loadFluidDefaults(NBTTagCompound tag) {
        Set<String> defaults = Sets.newHashSet();
        if (tag.hasKey("DefaultFluidList", 9)) {
            FMLLog.getLogger().log(Level.DEBUG, "Loading persistent fluid defaults from world");
            NBTTagList tl = tag.getTagList("DefaultFluidList", 8);

            for(int i = 0; i < tl.tagCount(); ++i) {
                defaults.add(tl.getStringTagAt(i));
            }
        } else {
            FMLLog.getLogger().log(Level.DEBUG, "World is missing persistent fluid defaults - using local defaults");
        }

        loadFluidDefaults(HashBiMap.create(fluidIDs), defaults);
    }

    public static void writeDefaultFluidList(NBTTagCompound forgeData) {
        NBTTagList tagList = new NBTTagList();
        Iterator var2 = fluids.entrySet().iterator();

        while(var2.hasNext()) {
            Entry<String, Fluid> def = (Entry)var2.next();
            tagList.appendTag(new NBTTagString(getDefaultFluidName((Fluid)def.getValue())));
        }

        forgeData.setTag("DefaultFluidList", tagList);
    }

    public static void validateFluidRegistry() {
        Set<Fluid> illegalFluids = Sets.newHashSet();
        Iterator var1 = fluids.values().iterator();

        Fluid f;
        while(var1.hasNext()) {
            f = (Fluid)var1.next();
            if (!masterFluidReference.containsValue(f)) {
                illegalFluids.add(f);
            }
        }

        if (!illegalFluids.isEmpty()) {
            FMLLog.getLogger().log(Level.FATAL, "The fluid registry is corrupted. Something has inserted a fluid without registering it");
            FMLLog.getLogger().log(Level.FATAL, "There is {} unregistered fluids", new Object[]{illegalFluids.size()});
            var1 = illegalFluids.iterator();

            while(var1.hasNext()) {
                f = (Fluid)var1.next();
                FMLLog.getLogger().log(Level.FATAL, "  Fluid name : {}, type: {}", new Object[]{f.getName(), f.getClass().getName()});
            }

            FMLLog.getLogger().log(Level.FATAL, "The mods that own these fluids need to register them properly");
            throw new IllegalStateException("The fluid map contains fluids unknown to the master fluid registry");
        }
    }

    static RegistryDelegate<Fluid> makeDelegate(Fluid fl) {
        return (RegistryDelegate)delegates.get(fl);
    }

    static {
        WATER = (new 1("water")).setBlock(Blocks.water).setUnlocalizedName(Blocks.water.getUnlocalizedName());
        LAVA = (new 2("lava")).setBlock(Blocks.lava).setLuminosity(15).setDensity(3000).setViscosity(6000).setTemperature(1300).setUnlocalizedName(Blocks.lava.getUnlocalizedName());
        renderIdFluid = -1;
        registerFluid(WATER);
        registerFluid(LAVA);
    }

    @MixinMerged(
        mixin = "com.mitchej123.hodgepodge.mixins.early.forge.MixinFluidRegistry",
        priority = 1000,
        sessionId = "98d213e4-85ca-4ce4-959f-84858add440a"
    )
    private static void handler$zgd000$hodgepodge$afterLoadFluidDefaults(BiMap<Fluid, Integer> localFluidIDs, Set<String> defaultNames, CallbackInfo ci) {
        Map filledContainerMap = FluidContainerRegistryAccessor.getFilledContainerMap();
        Map copiedFilledContainerMap = new HashMap(filledContainerMap);
        filledContainerMap.clear();
        filledContainerMap.putAll(copiedFilledContainerMap);
    }
}
