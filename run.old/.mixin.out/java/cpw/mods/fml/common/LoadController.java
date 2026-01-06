package cpw.mods.fml.common;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.BiMap;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.util.concurrent.Runnables;
import com.gtnewhorizon.gtnhmixins.GTNHMixins;
import com.gtnewhorizon.gtnhmixins.Reflection;
import cpw.mods.fml.common.LoaderState.ModState;
import cpw.mods.fml.common.ProgressManager.ProgressBar;
import cpw.mods.fml.common.discovery.ASMDataTable;
import cpw.mods.fml.common.discovery.ASMDataTable.ASMData;
import cpw.mods.fml.common.event.FMLEvent;
import cpw.mods.fml.common.event.FMLLoadEvent;
import cpw.mods.fml.common.event.FMLModDisabledEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLStateEvent;
import cpw.mods.fml.common.functions.ArtifactVersionNameFunction;
import cpw.mods.fml.common.versioning.ArtifactVersion;
import io.github.tox1cozz.mixinbooterlegacy.ILateMixinLoader;
import io.github.tox1cozz.mixinbooterlegacy.LateMixin;
import io.github.tox1cozz.mixinbooterlegacy.MixinBooterLegacyPlugin;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Stream;
import net.minecraft.launchwrapper.Launch;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.ThreadContext;
import org.spongepowered.asm.launch.MixinInitialisationError;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigSource;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.transformer.Config;
import org.spongepowered.asm.mixin.transformer.Proxy;
import org.spongepowered.asm.mixin.transformer.meta.MixinMerged;

public class LoadController {
    private Loader loader;
    private EventBus masterChannel;
    private ImmutableMap<String, EventBus> eventChannels;
    private LoaderState state;
    private Multimap<String, ModState> modStates = ArrayListMultimap.create();
    private Multimap<String, Throwable> errors = ArrayListMultimap.create();
    private List<ModContainer> activeModList = Lists.newArrayList();
    private ModContainer activeContainer;
    private BiMap<ModContainer, Object> modObjectList;
    private ListMultimap<String, ModContainer> packageOwners;
    private cpw.mods.fml.common.LoadController.FMLSecurityManager accessibleManager = new cpw.mods.fml.common.LoadController.FMLSecurityManager(this);

    public LoadController(Loader loader) {
        this.loader = loader;
        this.masterChannel = new EventBus("FMLMainChannel");
        this.masterChannel.register(this);
        this.state = LoaderState.NOINIT;
        this.packageOwners = ArrayListMultimap.create();
    }

    void disableMod(ModContainer mod) {
        HashMap<String, EventBus> temporary = Maps.newHashMap(this.eventChannels);
        String modId = mod.getModId();
        EventBus bus = (EventBus)temporary.remove(modId);
        bus.post(new FMLModDisabledEvent());
        if (this.errors.get(modId).isEmpty()) {
            this.eventChannels = ImmutableMap.copyOf(temporary);
            this.modStates.put(modId, ModState.DISABLED);
            this.modObjectList.remove(mod);
            this.activeModList.remove(mod);
        }

    }

    @Subscribe
    public void buildModList(FMLLoadEvent event) {
        Builder<String, EventBus> eventBus = ImmutableMap.builder();
        Iterator var3 = this.loader.getModList().iterator();

        while(var3.hasNext()) {
            ModContainer mod = (ModContainer)var3.next();
            EventBus bus = new EventBus(mod.getModId());
            boolean isActive = mod.registerBus(bus, this);
            if (isActive) {
                this.activeModList.add(mod);
                this.modStates.put(mod.getModId(), ModState.UNLOADED);
                eventBus.put(mod.getModId(), bus);
                FMLCommonHandler.instance().addModToResourcePack(mod);
            } else {
                FMLLog.log(mod.getModId(), Level.WARN, "Mod %s has been disabled through configuration", new Object[]{mod.getModId()});
                this.modStates.put(mod.getModId(), ModState.UNLOADED);
                this.modStates.put(mod.getModId(), ModState.DISABLED);
            }
        }

        this.eventChannels = eventBus.build();
    }

    public void distributeStateMessage(LoaderState state, Object... eventData) {
        this.handler$zza000$beforeConstructing(state, eventData, (CallbackInfo)null);
        this.handler$zzb001$beforeConstructing(state, eventData, (CallbackInfo)null);
        if (state.hasEvent()) {
            this.masterChannel.post(state.getEvent(eventData));
        }

    }

    public void transition(LoaderState desiredState, boolean forceState) {
        LoaderState oldState = this.state;
        this.state = this.state.transition(!this.errors.isEmpty());
        if (this.state != desiredState && !forceState) {
            Throwable toThrow = null;
            FMLLog.severe("Fatal errors were detected during the transition from %s to %s. Loading cannot continue", new Object[]{oldState, desiredState});
            StringBuilder sb = new StringBuilder();
            this.printModStates(sb);
            FMLLog.severe("%s", new Object[]{sb.toString()});
            if (this.errors.size() > 0) {
                FMLLog.severe("The following problems were captured during this phase", new Object[0]);
                Iterator var6 = this.errors.entries().iterator();

                while(var6.hasNext()) {
                    Entry<String, Throwable> error = (Entry)var6.next();
                    FMLLog.log(Level.ERROR, (Throwable)error.getValue(), "Caught exception from %s", new Object[]{error.getKey()});
                    if (error.getValue() instanceof IFMLHandledException) {
                        toThrow = (Throwable)error.getValue();
                    } else if (toThrow == null) {
                        toThrow = (Throwable)error.getValue();
                    }
                }

                if (toThrow != null && toThrow instanceof RuntimeException) {
                    throw (RuntimeException)toThrow;
                } else {
                    throw new LoaderException(toThrow);
                }
            } else {
                FMLLog.severe("The ForgeModLoader state engine has become corrupted. Probably, a state was missed by and invalid modification to a base classForgeModLoader depends on. This is a critical error and not recoverable. Investigate any modifications to base classes outside ofForgeModLoader, especially Optifine, to see if there are fixes available.", new Object[0]);
                throw new RuntimeException("The ForgeModLoader state engine is invalid");
            }
        } else {
            if (this.state != desiredState && forceState) {
                FMLLog.info("The state engine was in incorrect state %s and forced into state %s. Errors may have been discarded.", new Object[]{this.state, desiredState});
                this.forceState(desiredState);
            }

        }
    }

    public ModContainer activeContainer() {
        return this.activeContainer != null ? this.activeContainer : this.findActiveContainerFromStack();
    }

    @Subscribe
    public void propogateStateMessage(FMLEvent stateEvent) {
        if (stateEvent instanceof FMLPreInitializationEvent) {
            this.modObjectList = this.buildModObjectList();
        }

        ProgressBar bar = ProgressManager.push(stateEvent.description(), this.activeModList.size(), true);
        Iterator var3 = this.activeModList.iterator();

        while(var3.hasNext()) {
            ModContainer mc = (ModContainer)var3.next();
            bar.step(mc.getName());
            this.sendEventToModContainer(stateEvent, mc);
        }

        ProgressManager.pop(bar);
    }

    private void sendEventToModContainer(FMLEvent stateEvent, ModContainer mc) {
        String modId = mc.getModId();
        Collection<String> requirements = Collections2.transform(mc.getRequirements(), new ArtifactVersionNameFunction());
        Iterator var5 = mc.getDependencies().iterator();

        ArtifactVersion av;
        do {
            if (!var5.hasNext()) {
                this.activeContainer = mc;
                stateEvent.applyModContainer(this.activeContainer());
                ThreadContext.put("mod", modId);
                FMLLog.log(modId, Level.TRACE, "Sending event %s to mod %s", new Object[]{stateEvent.getEventType(), modId});
                ((EventBus)this.eventChannels.get(modId)).post(stateEvent);
                FMLLog.log(modId, Level.TRACE, "Sent event %s to mod %s", new Object[]{stateEvent.getEventType(), modId});
                ThreadContext.remove("mod");
                this.activeContainer = null;
                if (stateEvent instanceof FMLStateEvent) {
                    if (!this.errors.containsKey(modId)) {
                        this.modStates.put(modId, ((FMLStateEvent)stateEvent).getModState());
                    } else {
                        this.modStates.put(modId, ModState.ERRORED);
                    }
                }

                return;
            }

            av = (ArtifactVersion)var5.next();
        } while(av.getLabel() == null || !requirements.contains(av.getLabel()) || !this.modStates.containsEntry(av.getLabel(), ModState.ERRORED));

        FMLLog.log(modId, Level.ERROR, "Skipping event %s and marking errored mod %s since required dependency %s has errored", new Object[]{stateEvent.getEventType(), modId, av.getLabel()});
        this.modStates.put(modId, ModState.ERRORED);
    }

    public ImmutableBiMap<ModContainer, Object> buildModObjectList() {
        com.google.common.collect.ImmutableBiMap.Builder<ModContainer, Object> builder = ImmutableBiMap.builder();
        Iterator var2 = this.activeModList.iterator();

        while(var2.hasNext()) {
            ModContainer mc = (ModContainer)var2.next();
            if (!mc.isImmutable() && mc.getMod() != null) {
                builder.put(mc, mc.getMod());
                List<String> packages = mc.getOwnedPackages();
                Iterator var5 = packages.iterator();

                while(var5.hasNext()) {
                    String pkg = (String)var5.next();
                    this.packageOwners.put(pkg, mc);
                }
            }

            if (mc.getMod() == null && !mc.isImmutable() && this.state != LoaderState.CONSTRUCTING) {
                FMLLog.severe("There is a severe problem with %s - it appears not to have constructed correctly", new Object[]{mc.getModId()});
                if (this.state != LoaderState.CONSTRUCTING) {
                    this.errorOccurred(mc, new RuntimeException());
                }
            }
        }

        return builder.build();
    }

    public void errorOccurred(ModContainer modContainer, Throwable exception) {
        if (exception instanceof InvocationTargetException) {
            this.errors.put(modContainer.getModId(), ((InvocationTargetException)exception).getCause());
        } else {
            this.errors.put(modContainer.getModId(), exception);
        }

    }

    public void printModStates(StringBuilder ret) {
        ret.append("\n\tStates:");
        ModState[] var2 = ModState.values();
        int var3 = var2.length;

        ModState state;
        for(int var4 = 0; var4 < var3; ++var4) {
            state = var2[var4];
            ret.append(" '").append(state.getMarker()).append("' = ").append(state.toString());
        }

        Iterator var6 = this.loader.getModList().iterator();

        while(var6.hasNext()) {
            ModContainer mc = (ModContainer)var6.next();
            ret.append("\n\t");
            Iterator var8 = this.modStates.get(mc.getModId()).iterator();

            while(var8.hasNext()) {
                state = (ModState)var8.next();
                ret.append(state.getMarker());
            }

            ret.append("\t").append(mc.getModId()).append("{").append(mc.getVersion()).append("} [").append(mc.getName()).append("] (").append(mc.getSource().getName()).append(") ");
        }

    }

    public List<ModContainer> getActiveModList() {
        return this.activeModList;
    }

    public ModState getModState(ModContainer selectedMod) {
        return (ModState)Iterables.getLast(this.modStates.get(selectedMod.getModId()), ModState.AVAILABLE);
    }

    public void distributeStateMessage(Class<?> customEvent) {
        try {
            this.masterChannel.post(customEvent.newInstance());
        } catch (Exception var3) {
            FMLLog.log(Level.ERROR, var3, "An unexpected exception", new Object[0]);
            throw new LoaderException(var3);
        }
    }

    public BiMap<ModContainer, Object> getModObjectList() {
        if (this.modObjectList == null) {
            FMLLog.severe("Detected an attempt by a mod %s to perform game activity during mod construction. This is a serious programming error.", new Object[]{this.activeContainer});
            return this.buildModObjectList();
        } else {
            return ImmutableBiMap.copyOf(this.modObjectList);
        }
    }

    public boolean isInState(LoaderState state) {
        return this.state == state;
    }

    boolean hasReachedState(LoaderState state) {
        return this.state.ordinal() >= state.ordinal() && this.state != LoaderState.ERRORED;
    }

    void forceState(LoaderState newState) {
        this.state = newState;
    }

    private ModContainer findActiveContainerFromStack() {
        Class[] var1 = this.getCallingStack();
        int var2 = var1.length;

        for(int var3 = 0; var3 < var2; ++var3) {
            Class<?> c = var1[var3];
            int idx = c.getName().lastIndexOf(46);
            if (idx != -1) {
                String pkg = c.getName().substring(0, idx);
                if (this.packageOwners.containsKey(pkg)) {
                    return (ModContainer)this.packageOwners.get(pkg).get(0);
                }
            }
        }

        return null;
    }

    Class<?>[] getCallingStack() {
        return this.accessibleManager.getStackClasses();
    }

    LoaderState getState() {
        return this.state;
    }

    @MixinMerged(
        mixin = "io.github.tox1cozz.mixinbooterlegacy.loader.mixin.LoadControllerMixin",
        priority = 1000,
        sessionId = "98d213e4-85ca-4ce4-959f-84858add440a"
    )
    private void handler$zza000$beforeConstructing(LoaderState state, Object[] eventData, CallbackInfo ci) throws Throwable {
        if (state == LoaderState.CONSTRUCTING) {
            ModClassLoader modClassLoader = (ModClassLoader)eventData[0];
            ASMDataTable asmDataTable = (ASMDataTable)eventData[1];
            MixinBooterLegacyPlugin.LOGGER.info("Instantiating all ILateMixinLoader implemented classes...");
            Iterator var6 = asmDataTable.getAll(LateMixin.class.getName()).iterator();

            Class mixinTransformerClass;
            while(var6.hasNext()) {
                ASMData asmData = (ASMData)var6.next();
                modClassLoader.addFile(asmData.getCandidate().getModContainer());
                mixinTransformerClass = Class.forName(asmData.getClassName().replace('/', '.'));
                MixinBooterLegacyPlugin.LOGGER.info("Instantiating {} for its mixins.", new Object[]{mixinTransformerClass});
                if (!ILateMixinLoader.class.isAssignableFrom(mixinTransformerClass)) {
                    throw new MixinInitialisationError(String.format("The class %s has the LateMixin annotation, but does not implement the ILateMixinLoader interface.", mixinTransformerClass.getName()));
                }

                ILateMixinLoader loader = (ILateMixinLoader)mixinTransformerClass.newInstance();
                Iterator var10 = loader.getMixinConfigs().iterator();

                while(var10.hasNext()) {
                    String mixinConfig = (String)var10.next();
                    if (loader.shouldMixinConfigQueue(mixinConfig)) {
                        MixinBooterLegacyPlugin.LOGGER.info("Adding {} mixin configuration.", new Object[]{mixinConfig});
                        Mixins.addConfiguration(mixinConfig);
                        loader.onMixinConfigQueued(mixinConfig);
                    }
                }
            }

            ((Runnable)Launch.blackboard.getOrDefault("unimixins.mixinModidDecorator.refresh", Runnables.doNothing())).run();
            var6 = this.loader.getActiveModList().iterator();

            while(var6.hasNext()) {
                ModContainer container = (ModContainer)var6.next();
                modClassLoader.addFile(container.getSource());
            }

            Field transformerField = Proxy.class.getDeclaredField("transformer");
            transformerField.setAccessible(true);
            Stream var10001 = Launch.classLoader.getTransformers().stream();
            Proxy.class.getClass();
            Object transformer = transformerField.get(var10001.filter(Proxy.class::isInstance).findFirst().get());
            mixinTransformerClass = Class.forName("org.spongepowered.asm.mixin.transformer.MixinTransformer");
            Field processorField = mixinTransformerClass.getDeclaredField("processor");
            processorField.setAccessible(true);
            Object processor = processorField.get(transformer);
            Class mixinProcessorClass = Class.forName("org.spongepowered.asm.mixin.transformer.MixinProcessor");
            Method selectConfigsMethod = mixinProcessorClass.getDeclaredMethod("selectConfigs", MixinEnvironment.class);
            selectConfigsMethod.setAccessible(true);
            MixinEnvironment env = MixinEnvironment.getCurrentEnvironment();
            selectConfigsMethod.invoke(processor, env);

            try {
                Method prepareConfigsMethod = mixinProcessorClass.getDeclaredMethod("prepareConfigs", MixinEnvironment.class);
                prepareConfigsMethod.setAccessible(true);
                prepareConfigsMethod.invoke(processor, env);
            } catch (NoSuchMethodException var19) {
                Class extensionsClass = Class.forName("org.spongepowered.asm.mixin.transformer.ext.Extensions");
                Method prepareConfigsMethod = mixinProcessorClass.getDeclaredMethod("prepareConfigs", MixinEnvironment.class, extensionsClass);
                prepareConfigsMethod.setAccessible(true);
                Field extensionsField = mixinProcessorClass.getDeclaredField("extensions");
                extensionsField.setAccessible(true);
                Object extensions = extensionsField.get(processor);
                prepareConfigsMethod.invoke(processor, env, extensions);
            }

        }
    }

    @MixinMerged(
        mixin = "com.gtnewhorizon.gtnhmixins.mixins.LateMixinOrchestrationMixin",
        priority = 1000,
        sessionId = "98d213e4-85ca-4ce4-959f-84858add440a"
    )
    private void handler$zzb001$beforeConstructing(LoaderState state, Object[] eventData, CallbackInfo ci) throws Throwable {
        if (state == LoaderState.CONSTRUCTING) {
            GTNHMixins.log("Searching ASMData for ILateMixinLoaders");
            ModClassLoader modClassLoader = (ModClassLoader)eventData[0];
            ASMDataTable asmDataTable = (ASMDataTable)eventData[1];
            Loader loader = Loader.instance();
            Set loadedModsTemp = new HashSet();
            loadedModsTemp.addAll(loader.getIndexedModList().keySet());
            loadedModsTemp.addAll(getLiteLoaderMods());
            Set loadedMods = Collections.unmodifiableSet(loadedModsTemp);
            GTNHMixins.log("LoadedMods {}", new Object[]{loadedMods.toString()});
            Iterator var9 = asmDataTable.getAll(com.gtnewhorizon.gtnhmixins.LateMixin.class.getName()).iterator();

            while(true) {
                while(var9.hasNext()) {
                    ASMData asmData = (ASMData)var9.next();
                    modClassLoader.addFile(asmData.getCandidate().getModContainer());
                    String mixinClassName = asmData.getClassName().replace('/', '.');
                    Class lateMixinClass = Class.forName(mixinClassName);
                    if (!com.gtnewhorizon.gtnhmixins.ILateMixinLoader.class.isAssignableFrom(lateMixinClass)) {
                        GTNHMixins.LOGGER.error("Class {} has the @LateMixin annotation, but does not implement the ILateMixinLoader interface!", new Object[]{mixinClassName});
                    } else {
                        com.gtnewhorizon.gtnhmixins.ILateMixinLoader lateLoader = (com.gtnewhorizon.gtnhmixins.ILateMixinLoader)lateMixinClass.newInstance();
                        GTNHMixins.log("Loading mixins from ILateMixinLoader [{}]", new Object[]{lateLoader.getClass().getName()});
                        String mixinConfig = lateLoader.getMixinConfig();
                        Config config = Config.create(mixinConfig, (IMixinConfigSource)null);
                        Object o = Reflection.mixinClassesField.get(Reflection.configField.get(config));
                        Object mixins;
                        if (o instanceof List) {
                            mixins = (List)o;
                        } else {
                            mixins = new ArrayList();
                            Reflection.mixinClassesField.set(Reflection.configField.get(config), mixins);
                        }

                        ((List)mixins).addAll(lateLoader.getMixins(loadedMods));
                        Iterator var18 = ((List)mixins).iterator();

                        while(var18.hasNext()) {
                            String mixin = (String)var18.next();
                            GTNHMixins.log("Loading [{}] {}", new Object[]{mixinConfig, mixin});
                        }

                        Reflection.registerConfigurationMethod.invoke((Object)null, config);
                    }
                }

                ((Runnable)Launch.blackboard.getOrDefault("unimixins.mixinModidDecorator.refresh", Runnables.doNothing())).run();
                var9 = loader.getActiveModList().iterator();

                while(var9.hasNext()) {
                    ModContainer container = (ModContainer)var9.next();
                    modClassLoader.addFile(container.getSource());
                }

                Reflection.setDelegatedTransformersField((Object)null);
                Field transformerField = Proxy.class.getDeclaredField("transformer");
                transformerField.setAccessible(true);
                Stream var10001 = Launch.classLoader.getTransformers().stream();
                Proxy.class.getClass();
                Object transformer = transformerField.get(var10001.filter(Proxy.class::isInstance).findFirst().get());
                MixinEnvironment env = MixinEnvironment.getCurrentEnvironment();
                Reflection.invokeSelectConfigs(transformer, env);
                Reflection.invokePrepareConfigs(transformer, env);
                return;
            }
        }
    }

    @MixinMerged(
        mixin = "com.gtnewhorizon.gtnhmixins.mixins.LateMixinOrchestrationMixin",
        priority = 1000,
        sessionId = "98d213e4-85ca-4ce4-959f-84858add440a"
    )
    private static Set<String> getLiteLoaderMods() {
        HashSet mods = new HashSet();

        try {
            Class LiteLoaderTweaker = Class.forName("com.mumfrey.liteloader.launch.LiteLoaderTweaker");
            Method hasValidMetaData = Class.forName("com.mumfrey.liteloader.interfaces.LoadableMod").getMethod("hasValidMetaData");
            Object instance = FieldUtils.readDeclaredStaticField(LiteLoaderTweaker, "instance", true);
            Object bootstrap = FieldUtils.readDeclaredField(instance, "bootstrap", true);
            Object enumerator = FieldUtils.readDeclaredField(bootstrap, "enumerator", true);
            Map enabledContainers = (Map)FieldUtils.readDeclaredField(enumerator, "enabledContainers", true);
            GTNHMixins.log("LiteLoader present, adding its mods to the list");
            Iterator var7 = enabledContainers.entrySet().iterator();

            while(var7.hasNext()) {
                Entry e = (Entry)var7.next();
                if ((Boolean)hasValidMetaData.invoke(e.getValue())) {
                    mods.add(e.getKey());
                }
            }
        } catch (ClassNotFoundException var9) {
            GTNHMixins.log("LiteLoader not present");
        } catch (Exception var10) {
            GTNHMixins.LOGGER.error("Failed to get LiteLoader mods.", var10);
        }

        return mods;
    }
}
