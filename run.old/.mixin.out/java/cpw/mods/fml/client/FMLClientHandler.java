package cpw.mods.fml.client;

import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mitchej123.hodgepodge.asm.hooks.fml.FMLClientHandlerHook;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.DummyModContainer;
import cpw.mods.fml.common.DuplicateModsFoundException;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.FMLContainerHolder;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.IFMLSidedHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.LoaderException;
import cpw.mods.fml.common.MetadataCollection;
import cpw.mods.fml.common.MissingModsException;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.ModMetadata;
import cpw.mods.fml.common.ObfuscationReflectionHelper;
import cpw.mods.fml.common.StartupQuery;
import cpw.mods.fml.common.WrongMinecraftVersionException;
import cpw.mods.fml.common.StartupQuery.AbortedException;
import cpw.mods.fml.common.eventhandler.EventBus;
import cpw.mods.fml.common.network.FMLNetworkEvent.CustomPacketRegistrationEvent;
import cpw.mods.fml.common.network.internal.FMLNetworkHandler;
import cpw.mods.fml.common.registry.GameData;
import cpw.mods.fml.common.registry.LanguageRegistry;
import cpw.mods.fml.common.toposort.ModSortingException;
import cpw.mods.fml.relauncher.Side;
import java.io.File;
import java.io.FileInputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSelectWorld;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.ServerListEntryNormal;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.OldServerPinger;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.resources.AbstractResourcePack;
import net.minecraft.client.resources.FallbackResourceManager;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.SimpleReloadableResourceManager;
import net.minecraft.crash.CrashReport;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.ServerStatusResponse;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.storage.SaveFormatOld;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.LWJGLUtil;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.transformer.meta.MixinMerged;

public class FMLClientHandler implements IFMLSidedHandler {
    private static final FMLClientHandler INSTANCE = new FMLClientHandler();
    private Minecraft client;
    private DummyModContainer optifineContainer;
    private boolean guiLoaded;
    private boolean serverIsRunning;
    private MissingModsException modsMissing;
    private ModSortingException modSorting;
    private boolean loading = true;
    private WrongMinecraftVersionException wrongMC;
    private CustomModLoadingErrorDisplayException customError;
    private DuplicateModsFoundException dupesFound;
    private boolean serverShouldBeKilledQuietly;
    private List<IResourcePack> resourcePackList;
    private IReloadableResourceManager resourceManager;
    private Map<String, IResourcePack> resourcePackMap;
    private BiMap<ModContainer, IModGuiFactory> guiFactories;
    private Map<ServerStatusResponse, JsonObject> extraServerListData;
    private Map<ServerData, ExtendedServerListData> serverDataTag;
    private WeakReference<NetHandlerPlayClient> currentPlayClient;
    private static final ResourceLocation iconSheet = new ResourceLocation("fml:textures/gui/icons.png");
    private static final CountDownLatch startupConnectionData = new CountDownLatch(1);
    private CountDownLatch playClientBlock;
    private SetMultimap<String, ResourceLocation> missingTextures = HashMultimap.create();
    private Set<String> badTextureDomains = Sets.newHashSet();
    private Table<String, String, Set<ResourceLocation>> brokenTextures = HashBasedTable.create();
    private static final String ALLOWED_CHARS = "\u00c0\u00c1\u00c2\u00c8\u00ca\u00cb\u00cd\u00d3\u00d4\u00d5\u00da\u00df\u00e3\u00f5\u011f\u0130\u0131\u0152\u0153\u015e\u015f\u0174\u0175\u017e\u0207\u0000\u0000\u0000\u0000\u0000\u0000\u0000 !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u0000\u00c7\u00fc\u00e9\u00e2\u00e4\u00e0\u00e5\u00e7\u00ea\u00eb\u00e8\u00ef\u00ee\u00ec\u00c4\u00c5\u00c9\u00e6\u00c6\u00f4\u00f6\u00f2\u00fb\u00f9\u00ff\u00d6\u00dc\u00f8\u00a3\u00d8\u00d7\u0192\u00e1\u00ed\u00f3\u00fa\u00f1\u00d1\u00aa\u00ba\u00bf\u00ae\u00ac\u00bd\u00bc\u00a1\u00ab\u00bb\u2591\u2592\u2593\u2502\u2524\u2561\u2562\u2556\u2555\u2563\u2551\u2557\u255d\u255c\u255b\u2510\u2514\u2534\u252c\u251c\u2500\u253c\u255e\u255f\u255a\u2554\u2569\u2566\u2560\u2550\u256c\u2567\u2568\u2564\u2565\u2559\u2558\u2552\u2553\u256b\u256a\u2518\u250c\u2588\u2584\u258c\u2590\u2580\u03b1\u03b2\u0393\u03c0\u03a3\u03c3\u03bc\u03c4\u03a6\u0398\u03a9\u03b4\u221e\u2205\u2208\u2229\u2261\u00b1\u2265\u2264\u2320\u2321\u00f7\u2248\u00b0\u2219\u00b7\u221a\u207f\u00b2\u25a0\u0000";

    public void beginMinecraftLoading(Minecraft minecraft, List resourcePackList, IReloadableResourceManager resourceManager) {
        this.detectOptifine();
        SplashProgress.start();
        this.client = minecraft;
        this.resourcePackList = resourcePackList;
        this.resourceManager = resourceManager;
        this.resourcePackMap = Maps.newHashMap();
        if (minecraft.isDemo()) {
            FMLLog.severe("DEMO MODE DETECTED, FML will not work. Finishing now.", new Object[0]);
            this.haltGame("FML will not run in demo mode", new RuntimeException());
        } else {
            FMLCommonHandler.instance().beginLoading(this);

            label120: {
                try {
                    Loader.instance().loadMods();
                    break label120;
                } catch (WrongMinecraftVersionException var20) {
                    this.wrongMC = var20;
                    break label120;
                } catch (DuplicateModsFoundException var21) {
                    this.dupesFound = var21;
                    break label120;
                } catch (MissingModsException var22) {
                    this.modsMissing = var22;
                    break label120;
                } catch (ModSortingException var23) {
                    this.modSorting = var23;
                    break label120;
                } catch (CustomModLoadingErrorDisplayException var24) {
                    FMLLog.log(Level.ERROR, var24, "A custom exception was thrown by a mod, the game will now halt", new Object[0]);
                    this.customError = var24;
                    break label120;
                } catch (LoaderException var25) {
                    this.haltGame("There was a severe problem during mod loading that has caused the game to fail", var25);
                } finally {
                    this.client.refreshResources();
                }

                return;
            }

            try {
                Loader.instance().preinitializeMods();
            } catch (CustomModLoadingErrorDisplayException var18) {
                FMLLog.log(Level.ERROR, var18, "A custom exception was thrown by a mod, the game will now halt", new Object[0]);
                this.customError = var18;
            } catch (LoaderException var19) {
                this.haltGame("There was a severe problem during mod loading that has caused the game to fail", var19);
                return;
            }

            Map<String, Map<String, String>> sharedModList = (Map)Launch.blackboard.get("modList");
            if (sharedModList == null) {
                sharedModList = Maps.newHashMap();
                Launch.blackboard.put("modList", sharedModList);
            }

            Iterator var5 = Loader.instance().getActiveModList().iterator();

            while(var5.hasNext()) {
                ModContainer mc = (ModContainer)var5.next();
                Map<String, String> sharedModDescriptor = mc.getSharedModDescriptor();
                if (sharedModDescriptor != null) {
                    String sharedModId = "fml:" + mc.getModId();
                    ((Map)sharedModList).put(sharedModId, sharedModDescriptor);
                }
            }

        }
    }

    private void detectOptifine() {
        try {
            Class<?> optifineConfig = Class.forName("Config", false, Loader.instance().getModClassLoader());
            String optifineVersion = (String)optifineConfig.getField("VERSION").get((Object)null);
            Map<String, Object> dummyOptifineMeta = ImmutableMap.builder().put("name", "Optifine").put("version", optifineVersion).build();
            ModMetadata optifineMetadata = MetadataCollection.from(this.getClass().getResourceAsStream("optifinemod.info"), "optifine").getMetadataForId("optifine", dummyOptifineMeta);
            this.optifineContainer = new DummyModContainer(optifineMetadata);
            FMLLog.info("Forge Mod Loader has detected optifine %s, enabling compatibility features", new Object[]{this.optifineContainer.getVersion()});
        } catch (Exception var5) {
            this.optifineContainer = null;
        }

    }

    public void haltGame(String message, Throwable t) {
        SplashProgress.finish();
        this.client.displayCrashReport(new CrashReport(message, t));
        throw Throwables.propagate(t);
    }

    public void finishMinecraftLoading() {
        if (this.modsMissing == null && this.wrongMC == null && this.customError == null && this.dupesFound == null && this.modSorting == null) {
            try {
                Loader.instance().initializeMods();
            } catch (CustomModLoadingErrorDisplayException var8) {
                FMLLog.log(Level.ERROR, var8, "A custom exception was thrown by a mod, the game will now halt", new Object[0]);
                this.customError = var8;
                SplashProgress.finish();
                return;
            } catch (LoaderException var9) {
                this.haltGame("There was a severe problem during mod loading that has caused the game to fail", var9);
                return;
            }

            this.client.refreshResources();
            RenderingRegistry.instance().loadEntityRenderers(RenderManager.instance.entityRenderMap);
            this.guiFactories = HashBiMap.create();
            Iterator var1 = Loader.instance().getActiveModList().iterator();

            while(var1.hasNext()) {
                ModContainer mc = (ModContainer)var1.next();
                String className = mc.getGuiClassName();
                if (!Strings.isNullOrEmpty(className)) {
                    try {
                        Class<?> clazz = Class.forName(className, true, Loader.instance().getModClassLoader());
                        Class<? extends IModGuiFactory> guiClassFactory = clazz.asSubclass(IModGuiFactory.class);
                        IModGuiFactory guiFactory = (IModGuiFactory)guiClassFactory.newInstance();
                        guiFactory.initialize(this.client);
                        this.guiFactories.put(mc, guiFactory);
                    } catch (Exception var7) {
                        FMLLog.log(Level.ERROR, var7, "A critical error occurred instantiating the gui factory for mod %s", new Object[]{mc.getModId()});
                    }
                }
            }

            this.loading = false;
            this.client.gameSettings.loadOptions();
        } else {
            SplashProgress.finish();
        }
    }

    public void extendModList() {
        Map<String, Map<String, String>> modList = (Map)Launch.blackboard.get("modList");
        if (modList != null) {
            Iterator var2 = modList.entrySet().iterator();

            while(var2.hasNext()) {
                Entry<String, Map<String, String>> modEntry = (Entry)var2.next();
                String sharedModId = (String)modEntry.getKey();
                String system = sharedModId.split(":")[0];
                if (!"fml".equals(system)) {
                    Map<String, String> mod = (Map)modEntry.getValue();
                    String modSystem = (String)mod.get("modsystem");
                    String modId = (String)mod.get("id");
                    String modVersion = (String)mod.get("version");
                    String modName = (String)mod.get("name");
                    String modURL = (String)mod.get("url");
                    String modAuthors = (String)mod.get("authors");
                    String var13 = (String)mod.get("description");
                }
            }
        }

    }

    public void onInitializationComplete() {
        if (this.wrongMC != null) {
            this.showGuiScreen(new GuiWrongMinecraft(this.wrongMC));
        } else if (this.modsMissing != null) {
            this.showGuiScreen(new GuiModsMissing(this.modsMissing));
        } else if (this.dupesFound != null) {
            this.showGuiScreen(new GuiDupesFound(this.dupesFound));
        } else if (this.modSorting != null) {
            this.showGuiScreen(new GuiSortingProblem(this.modSorting));
        } else if (this.customError != null) {
            this.showGuiScreen(new GuiCustomModLoadingErrorScreen(this.customError));
        } else {
            Loader.instance().loadingComplete();
            SplashProgress.finish();
        }

        this.logMissingTextureErrors();
    }

    public Minecraft getClient() {
        return this.client;
    }

    public static FMLClientHandler instance() {
        return INSTANCE;
    }

    public void displayGuiScreen(EntityPlayer player, GuiScreen gui) {
        if (this.client.thePlayer == player && gui != null) {
            this.client.displayGuiScreen(gui);
        }

    }

    public void addSpecialModEntries(ArrayList<ModContainer> mods) {
        if (this.optifineContainer != null) {
            mods.add(this.optifineContainer);
        }

    }

    public List<String> getAdditionalBrandingInformation() {
        return (List)(this.optifineContainer != null ? Arrays.asList(String.format("Optifine %s", this.optifineContainer.getVersion())) : ImmutableList.of());
    }

    public Side getSide() {
        return Side.CLIENT;
    }

    public boolean hasOptifine() {
        return this.optifineContainer != null;
    }

    public void showGuiScreen(Object clientGuiElement) {
        GuiScreen gui = (GuiScreen)clientGuiElement;
        this.client.displayGuiScreen(gui);
    }

    public void queryUser(StartupQuery query) throws InterruptedException {
        if (query.getResult() == null) {
            this.client.displayGuiScreen(new GuiNotification(query));
        } else {
            this.client.displayGuiScreen(new GuiConfirmation(query));
        }

        if (query.isSynchronous()) {
            while(this.client.currentScreen instanceof GuiNotification) {
                if (Thread.interrupted()) {
                    throw new InterruptedException();
                }

                this.client.loadingScreen.resetProgresAndWorkingMessage("");
                Thread.sleep(50L);
            }

            this.client.loadingScreen.resetProgresAndWorkingMessage("");
        }

    }

    public boolean handleLoadingScreen(ScaledResolution scaledResolution) {
        if (this.client.currentScreen instanceof GuiNotification) {
            int width = scaledResolution.getScaledWidth();
            int height = scaledResolution.getScaledHeight();
            int mouseX = Mouse.getX() * width / this.client.displayWidth;
            int mouseZ = height - Mouse.getY() * height / this.client.displayHeight - 1;
            this.client.currentScreen.drawScreen(mouseX, mouseZ, 0.0F);
            this.client.currentScreen.handleInput();
            return true;
        } else {
            return false;
        }
    }

    public WorldClient getWorldClient() {
        return this.client.theWorld;
    }

    public EntityClientPlayerMP getClientPlayerEntity() {
        return this.client.thePlayer;
    }

    public void beginServerLoading(MinecraftServer server) {
        this.serverShouldBeKilledQuietly = false;
    }

    public void finishServerLoading() {
    }

    public File getSavesDirectory() {
        return ((SaveFormatOld)this.client.getSaveLoader()).savesDirectory;
    }

    public MinecraftServer getServer() {
        return this.client.getIntegratedServer();
    }

    public void displayMissingMods(Object modMissingPacket) {
    }

    public boolean isLoading() {
        return this.loading;
    }

    public boolean shouldServerShouldBeKilledQuietly() {
        return this.serverShouldBeKilledQuietly;
    }

    public boolean isGUIOpen(Class<? extends GuiScreen> gui) {
        return this.client.currentScreen != null && this.client.currentScreen.getClass().equals(gui);
    }

    public void addModAsResource(ModContainer container) {
        LanguageRegistry.instance().loadLanguagesFor(container, Side.CLIENT);
        Class<?> resourcePackType = container.getCustomResourcePackClass();
        if (resourcePackType != null) {
            try {
                IResourcePack pack = (IResourcePack)resourcePackType.getConstructor(ModContainer.class).newInstance(container);
                this.resourcePackList.add(pack);
                this.resourcePackMap.put(container.getModId(), pack);
            } catch (NoSuchMethodException var4) {
                FMLLog.log(Level.ERROR, "The container %s (type %s) returned an invalid class for it's resource pack.", new Object[]{container.getName(), container.getClass().getName()});
                return;
            } catch (Exception var5) {
                FMLLog.log(Level.ERROR, var5, "An unexpected exception occurred constructing the custom resource pack for %s", new Object[]{container.getName()});
                throw Throwables.propagate(var5);
            }
        }

    }

    public IResourcePack getResourcePackFor(String modId) {
        return (IResourcePack)this.resourcePackMap.get(modId);
    }

    public String getCurrentLanguage() {
        return this.client.getLanguageManager().getCurrentLanguage().getLanguageCode();
    }

    public void serverStopped() {
        MinecraftServer server = this.getServer();
        if (server != null && !server.serverIsInRunLoop()) {
            ObfuscationReflectionHelper.setPrivateValue(MinecraftServer.class, server, true, new String[]{"field_71296_Q", "serverIsRunning"});
        }

    }

    public INetHandler getClientPlayHandler() {
        return this.currentPlayClient == null ? null : (INetHandler)this.currentPlayClient.get();
    }

    public NetworkManager getClientToServerNetworkManager() {
        return this.client.getNetHandler() != null ? this.client.getNetHandler().getNetworkManager() : null;
    }

    public void handleClientWorldClosing(WorldClient world) {
        NetworkManager client = this.getClientToServerNetworkManager();
        if (client != null && !client.isLocalChannel()) {
            GameData.revertToFrozen();
        }

    }

    public void startIntegratedServer(String id, String name, WorldSettings settings) {
        this.playClientBlock = new CountDownLatch(1);
    }

    public File getSavesDir() {
        return new File(this.client.mcDataDir, "saves");
    }

    public void tryLoadExistingWorld(GuiSelectWorld selectWorldGUI, String dirName, String saveName) {
        File dir = new File(this.getSavesDir(), dirName);

        NBTTagCompound leveldat;
        try {
            leveldat = CompressedStreamTools.readCompressed(new FileInputStream(new File(dir, "level.dat")));
        } catch (Exception var10) {
            try {
                leveldat = CompressedStreamTools.readCompressed(new FileInputStream(new File(dir, "level.dat_old")));
            } catch (Exception var9) {
                FMLLog.warning("There appears to be a problem loading the save %s, both level files are unreadable.", new Object[]{dirName});
                return;
            }
        }

        NBTTagCompound fmlData = leveldat.getCompoundTag("FML");
        if (fmlData.hasKey("ModItemData")) {
            this.showGuiScreen(new GuiOldSaveLoadConfirm(dirName, saveName, selectWorldGUI));
        } else {
            try {
                this.client.launchIntegratedServer(dirName, saveName, (WorldSettings)null);
            } catch (AbortedException var8) {
            }
        }

    }

    public void showInGameModOptions(GuiIngameMenu guiIngameMenu) {
        this.showGuiScreen(new GuiIngameModOptions(guiIngameMenu));
    }

    public IModGuiFactory getGuiFactoryFor(ModContainer selectedMod) {
        return (IModGuiFactory)this.guiFactories.get(selectedMod);
    }

    public void setupServerList() {
        this.extraServerListData = Collections.synchronizedMap(Maps.newHashMap());
        this.serverDataTag = Collections.synchronizedMap(Maps.newHashMap());
    }

    public void captureAdditionalData(ServerStatusResponse serverstatusresponse, JsonObject jsonobject) {
        if (jsonobject.has("modinfo")) {
            JsonObject fmlData = jsonobject.get("modinfo").getAsJsonObject();
            this.extraServerListData.put(serverstatusresponse, fmlData);
        }

    }

    public void bindServerListData(ServerData data, ServerStatusResponse originalResponse) {
        if (this.extraServerListData.containsKey(originalResponse)) {
            JsonObject jsonData = (JsonObject)this.extraServerListData.get(originalResponse);
            String type = jsonData.get("type").getAsString();
            JsonArray modDataArray = jsonData.get("modList").getAsJsonArray();
            boolean moddedClientAllowed = jsonData.has("clientModsAllowed") ? jsonData.get("clientModsAllowed").getAsBoolean() : true;
            Builder<String, String> modListBldr = ImmutableMap.builder();
            Iterator var8 = modDataArray.iterator();

            while(var8.hasNext()) {
                JsonElement obj = (JsonElement)var8.next();
                JsonObject modObj = obj.getAsJsonObject();
                modListBldr.put(modObj.get("modid").getAsString(), modObj.get("version").getAsString());
            }

            Map<String, String> modListMap = modListBldr.build();
            this.serverDataTag.put(data, new ExtendedServerListData(type, FMLNetworkHandler.checkModList(modListMap, Side.SERVER) == null, modListMap, !moddedClientAllowed));
        } else {
            String serverDescription = data.serverMOTD;
            boolean moddedClientAllowed = true;
            if (!Strings.isNullOrEmpty(serverDescription)) {
                moddedClientAllowed = !serverDescription.endsWith(":NOFML\u00a7r");
            }

            this.serverDataTag.put(data, new ExtendedServerListData("VANILLA", false, ImmutableMap.of(), !moddedClientAllowed));
        }

        startupConnectionData.countDown();
    }

    public String enhanceServerListEntry(ServerListEntryNormal serverListEntry, ServerData serverEntry, int x, int width, int y, int relativeMouseX, int relativeMouseY) {
        boolean blocked = false;
        if (!this.serverDataTag.containsKey(serverEntry)) {
            return null;
        } else {
            ExtendedServerListData extendedData = (ExtendedServerListData)this.serverDataTag.get(serverEntry);
            String tooltip;
            byte idx;
            if ("FML".equals(extendedData.type) && extendedData.isCompatible) {
                idx = 0;
                tooltip = String.format("Compatible FML modded server\n%d mods present", extendedData.modData.size());
            } else if ("FML".equals(extendedData.type) && !extendedData.isCompatible) {
                idx = 16;
                tooltip = String.format("Incompatible FML modded server\n%d mods present", extendedData.modData.size());
            } else if ("BUKKIT".equals(extendedData.type)) {
                idx = 32;
                tooltip = String.format("Bukkit modded server");
            } else if ("VANILLA".equals(extendedData.type)) {
                idx = 48;
                tooltip = String.format("Vanilla server");
            } else {
                idx = 64;
                tooltip = String.format("Unknown server data");
            }

            blocked = extendedData.isBlocked;
            this.client.getTextureManager().bindTexture(iconSheet);
            Gui.func_146110_a(x + width - 18, y + 10, 0.0F, (float)idx, 16, 16, 256.0F, 256.0F);
            if (blocked) {
                Gui.func_146110_a(x + width - 18, y + 10, 0.0F, 80.0F, 16, 16, 256.0F, 256.0F);
            }

            return relativeMouseX > width - 15 && relativeMouseX < width && relativeMouseY > 10 && relativeMouseY < 26 ? tooltip : null;
        }
    }

    public String fixDescription(String description) {
        return description.endsWith(":NOFML\u00a7r") ? description.substring(0, description.length() - 8) + "\u00a7r" : description;
    }

    public void connectToServerAtStartup(String host, int port) {
        this.setupServerList();
        OldServerPinger osp = new OldServerPinger();
        ServerData serverData = new ServerData("Command Line", host + ":" + port);

        try {
            osp.func_147224_a(serverData);
            startupConnectionData.await(30L, TimeUnit.SECONDS);
        } catch (Exception var6) {
            this.showGuiScreen(new GuiConnecting(new GuiMainMenu(), this.client, host, port));
            return;
        }

        this.connectToServer(new GuiMainMenu(), serverData);
    }

    public void connectToServer(GuiScreen guiMultiplayer, ServerData serverEntry) {
        ExtendedServerListData extendedData = (ExtendedServerListData)this.serverDataTag.get(serverEntry);
        if (extendedData != null && extendedData.isBlocked) {
            this.showGuiScreen(new GuiAccessDenied(guiMultiplayer, serverEntry));
        } else {
            this.showGuiScreen(new GuiConnecting(guiMultiplayer, this.client, serverEntry));
        }

        this.playClientBlock = new CountDownLatch(1);
    }

    public void connectToRealmsServer(String host, int port) {
        this.playClientBlock = new CountDownLatch(1);
    }

    public void setPlayClient(NetHandlerPlayClient netHandlerPlayClient) {
        if (this.playClientBlock == null) {
            this.playClientBlock = new CountDownLatch(1);
        }

        this.playClientBlock.countDown();
        this.currentPlayClient = new WeakReference(netHandlerPlayClient);
    }

    public void waitForPlayClient() {
        boolean gotIt = false;

        try {
            gotIt = this.playClientBlock.await(5L, TimeUnit.SECONDS);
        } catch (InterruptedException var3) {
        }

        if (!gotIt) {
            throw new RuntimeException("Timeout waiting for client thread to catch up!");
        }
    }

    public void fireNetRegistrationEvent(EventBus bus, NetworkManager manager, Set<String> channelSet, String channel, Side side) {
        if (side == Side.CLIENT) {
            this.waitForPlayClient();
            bus.post(new CustomPacketRegistrationEvent(manager, channelSet, channel, side, NetHandlerPlayClient.class));
        } else {
            bus.post(new CustomPacketRegistrationEvent(manager, channelSet, channel, side, NetHandlerPlayServer.class));
        }

    }

    public boolean shouldAllowPlayerLogins() {
        return true;
    }

    public void allowLogins() {
    }

    public void trackMissingTexture(ResourceLocation resourceLocation) {
        this.badTextureDomains.add(resourceLocation.getResourceDomain());
        this.missingTextures.put(resourceLocation.getResourceDomain(), resourceLocation);
    }

    public void trackBrokenTexture(ResourceLocation resourceLocation, String error) {
        this.badTextureDomains.add(resourceLocation.getResourceDomain());
        Set<ResourceLocation> badType = (Set)this.brokenTextures.get(resourceLocation.getResourceDomain(), error);
        if (badType == null) {
            badType = Sets.newHashSet();
            this.brokenTextures.put(resourceLocation.getResourceDomain(), error, badType);
        }

        ((Set)badType).add(resourceLocation);
    }

    public void logMissingTextureErrors() {
        if (!this.missingTextures.isEmpty() || !this.brokenTextures.isEmpty()) {
            Logger logger = LogManager.getLogger("TEXTURE ERRORS");
            logger.error(Strings.repeat("+=", 25));
            logger.error("The following texture errors were found.");
            Map<String, FallbackResourceManager> resManagers = (Map)ObfuscationReflectionHelper.getPrivateValue(SimpleReloadableResourceManager.class, (SimpleReloadableResourceManager)Minecraft.getMinecraft().getResourceManager(), new String[]{"domainResourceManagers", "field_110548_a"});

            for(Iterator var3 = this.missingTextures.keySet().iterator(); var3.hasNext(); logger.error(Strings.repeat("=", 50))) {
                String resourceDomain = (String)var3.next();
                Set<ResourceLocation> missing = this.missingTextures.get(resourceDomain);
                logger.error(Strings.repeat("=", 50));
                logger.error("  DOMAIN {}", new Object[]{resourceDomain});
                logger.error(Strings.repeat("-", 50));
                logger.error("  domain {} is missing {} texture{}", new Object[]{resourceDomain, missing.size(), missing.size() != 1 ? "s" : ""});
                FallbackResourceManager fallbackResourceManager = (FallbackResourceManager)resManagers.get(resourceDomain);
                Iterator var8;
                if (fallbackResourceManager == null) {
                    logger.error("    domain {} is missing a resource manager - it is probably a side-effect of automatic texture processing", new Object[]{resourceDomain});
                } else {
                    List<IResourcePack> resPacks = (List)ObfuscationReflectionHelper.getPrivateValue(FallbackResourceManager.class, fallbackResourceManager, new String[]{"resourcePacks", "field_110540_a"});
                    logger.error("    domain {} has {} location{}:", new Object[]{resourceDomain, resPacks.size(), resPacks.size() != 1 ? "s" : ""});
                    var8 = resPacks.iterator();

                    while(var8.hasNext()) {
                        IResourcePack resPack = (IResourcePack)var8.next();
                        if (resPack instanceof FMLContainerHolder) {
                            FMLContainerHolder containerHolder = (FMLContainerHolder)resPack;
                            ModContainer fmlContainer = containerHolder.getFMLContainer();
                            logger.error("      mod {} resources at {}", new Object[]{fmlContainer.getModId(), fmlContainer.getSource().getPath()});
                        } else if (resPack instanceof AbstractResourcePack) {
                            AbstractResourcePack resourcePack = (AbstractResourcePack)resPack;
                            File resPath = (File)ObfuscationReflectionHelper.getPrivateValue(AbstractResourcePack.class, resourcePack, new String[]{"resourcePackFile", "field_110597_b"});
                            logger.error("      resource pack at path {}", new Object[]{resPath.getPath()});
                        } else {
                            logger.error("      unknown resourcepack type {} : {}", new Object[]{resPack.getClass().getName(), resPack.getPackName()});
                        }
                    }
                }

                logger.error(Strings.repeat("-", 25));
                logger.error("    The missing resources for domain {} are:", new Object[]{resourceDomain});
                Iterator var12 = missing.iterator();

                while(var12.hasNext()) {
                    ResourceLocation rl = (ResourceLocation)var12.next();
                    logger.error("      {}", new Object[]{rl.getResourcePath()});
                }

                logger.error(Strings.repeat("-", 25));
                if (!this.brokenTextures.containsRow(resourceDomain)) {
                    logger.error("    No other errors exist for domain {}", new Object[]{resourceDomain});
                } else {
                    logger.error("    The following other errors were reported for domain {}:", new Object[]{resourceDomain});
                    Map<String, Set<ResourceLocation>> resourceErrs = this.brokenTextures.row(resourceDomain);
                    var8 = resourceErrs.keySet().iterator();

                    while(var8.hasNext()) {
                        String error = (String)var8.next();
                        logger.error(Strings.repeat("-", 25));
                        logger.error("    Problem: {}", new Object[]{error});
                        Iterator var17 = ((Set)resourceErrs.get(error)).iterator();

                        while(var17.hasNext()) {
                            ResourceLocation rl = (ResourceLocation)var17.next();
                            logger.error("      {}", new Object[]{rl.getResourcePath()});
                        }
                    }
                }
            }

            logger.error(Strings.repeat("+=", 25));
            this.handler$zia000$hodgepodge$freeMemory((CallbackInfo)null);
        }
    }

    public void processWindowMessages() {
        if (LWJGLUtil.getPlatform() == 3) {
            if (SplashProgress.mutex.tryAcquire()) {
                Display.processMessages();
                SplashProgress.mutex.release();
            }
        }
    }

    public String stripSpecialChars(String message) {
        return FMLClientHandlerHook.stripSpecialChars(message);
    }

    @MixinMerged(
        mixin = "com.mitchej123.hodgepodge.mixins.early.memory.MixinFMLClientHandler",
        priority = 1000,
        sessionId = "98d213e4-85ca-4ce4-959f-84858add440a"
    )
    private void handler$zia000$hodgepodge$freeMemory(CallbackInfo ci) {
        this.missingTextures = HashMultimap.create();
        this.badTextureDomains = Sets.newHashSet();
        this.brokenTextures = HashBasedTable.create();
    }
}
