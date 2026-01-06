package net.minecraft.client;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Queues;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;
import com.gtnewhorizon.gtnhlib.GTNHLib;
import com.mitchej123.hodgepodge.client.ClientTicker;
import com.mitchej123.hodgepodge.config.TweaksConfig;
import com.mitchej123.hodgepodge.mixins.interfaces.KeyBindingExt;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.SplashProgress;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.ProgressManager;
import cpw.mods.fml.common.StartupQuery;
import cpw.mods.fml.common.ProgressManager.ProgressBar;
import cpw.mods.fml.common.asm.transformers.TerminalTransformer.ExitVisitor;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.util.concurrent.GenericFutureListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.Proxy;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import javax.imageio.ImageIO;
import me.eigenraven.lwjgl3ify.core.Config;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft.1;
import net.minecraft.client.Minecraft.10;
import net.minecraft.client.Minecraft.11;
import net.minecraft.client.Minecraft.12;
import net.minecraft.client.Minecraft.13;
import net.minecraft.client.Minecraft.14;
import net.minecraft.client.Minecraft.15;
import net.minecraft.client.Minecraft.16;
import net.minecraft.client.Minecraft.2;
import net.minecraft.client.Minecraft.3;
import net.minecraft.client.Minecraft.4;
import net.minecraft.client.Minecraft.5;
import net.minecraft.client.Minecraft.6;
import net.minecraft.client.Minecraft.7;
import net.minecraft.client.Minecraft.8;
import net.minecraft.client.Minecraft.9;
import net.minecraft.client.audio.MusicTicker;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.audio.MusicTicker.MusicType;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiControls;
import net.minecraft.client.gui.GuiGameOver;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiMemoryErrorScreen;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSleepMP;
import net.minecraft.client.gui.GuiWinGame;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.achievement.GuiAchievement;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.gui.stream.GuiStreamUnavailable;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerLoginClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.DefaultResourcePack;
import net.minecraft.client.resources.FoliageColorReloadListener;
import net.minecraft.client.resources.GrassColorReloadListener;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.LanguageManager;
import net.minecraft.client.resources.ResourceIndex;
import net.minecraft.client.resources.ResourcePackRepository;
import net.minecraft.client.resources.SimpleReloadableResourceManager;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.client.resources.ResourcePackRepository.Entry;
import net.minecraft.client.resources.data.AnimationMetadataSection;
import net.minecraft.client.resources.data.AnimationMetadataSectionSerializer;
import net.minecraft.client.resources.data.FontMetadataSection;
import net.minecraft.client.resources.data.FontMetadataSectionSerializer;
import net.minecraft.client.resources.data.IMetadataSerializer;
import net.minecraft.client.resources.data.LanguageMetadataSection;
import net.minecraft.client.resources.data.LanguageMetadataSectionSerializer;
import net.minecraft.client.resources.data.PackMetadataSection;
import net.minecraft.client.resources.data.PackMetadataSectionSerializer;
import net.minecraft.client.resources.data.TextureMetadataSection;
import net.minecraft.client.resources.data.TextureMetadataSectionSerializer;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.settings.GameSettings.Options;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.client.stream.IStream;
import net.minecraft.client.stream.NullStream;
import net.minecraft.client.stream.TwitchStream;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.BossStatus;
import net.minecraft.entity.player.EntityPlayer.EnumChatVisibility;
import net.minecraft.init.Bootstrap;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.EnumConnectionState;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.network.login.client.C00PacketLoginStart;
import net.minecraft.network.play.client.C16PacketClientStatus;
import net.minecraft.network.play.client.C16PacketClientStatus.EnumState;
import net.minecraft.profiler.IPlayerUsage;
import net.minecraft.profiler.PlayerUsageSnooper;
import net.minecraft.profiler.Profiler;
import net.minecraft.profiler.Profiler.Result;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.stats.AchievementList;
import net.minecraft.stats.StatFileWriter;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MinecraftError;
import net.minecraft.util.MouseHelper;
import net.minecraft.util.MovementInputFromOptions;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.ReportedException;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.ScreenShotHelper;
import net.minecraft.util.Session;
import net.minecraft.util.Timer;
import net.minecraft.util.Util;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.util.Util.EnumOS;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderEnd;
import net.minecraft.world.WorldProviderHell;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.chunk.storage.AnvilSaveConverter;
import net.minecraft.world.storage.ISaveFormat;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;
import net.minecraftforge.event.world.WorldEvent.Unload;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Vector3i;
import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.ContextCapabilities;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.opengl.OpenGLException;
import org.lwjgl.util.glu.GLU;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.transformer.meta.MixinMerged;

@SideOnly(Side.CLIENT)
public class Minecraft implements IPlayerUsage {
    private static final Logger logger = LogManager.getLogger();
    private static final ResourceLocation locationMojangPng = new ResourceLocation("textures/gui/title/mojang.png");
    public static final boolean isRunningOnMac;
    public static byte[] memoryReserve;
    private static final List macDisplayModes;
    private final File fileResourcepacks;
    private final Multimap field_152356_J;
    private ServerData currentServerData;
    public TextureManager renderEngine;
    private static Minecraft theMinecraft;
    public PlayerControllerMP playerController;
    private boolean fullscreen;
    private boolean hasCrashed;
    private CrashReport crashReporter;
    public int displayWidth;
    public int displayHeight;
    private Timer timer = new Timer(20.0F);
    private PlayerUsageSnooper usageSnooper = new PlayerUsageSnooper("client", this, MinecraftServer.getSystemTimeMillis());
    public WorldClient theWorld;
    public RenderGlobal renderGlobal;
    public EntityClientPlayerMP thePlayer;
    public EntityLivingBase renderViewEntity;
    public Entity pointedEntity;
    public EffectRenderer effectRenderer;
    private final Session session;
    private boolean isGamePaused;
    public FontRenderer fontRenderer;
    public FontRenderer standardGalacticFontRenderer;
    public GuiScreen currentScreen;
    public LoadingScreenRenderer loadingScreen;
    public EntityRenderer entityRenderer;
    private int leftClickCounter;
    private int tempDisplayWidth;
    private int tempDisplayHeight;
    private IntegratedServer theIntegratedServer;
    public GuiAchievement guiAchievement;
    public GuiIngame ingameGUI;
    public boolean skipRenderWorld;
    public MovingObjectPosition objectMouseOver;
    public GameSettings gameSettings;
    public MouseHelper mouseHelper;
    public final File mcDataDir;
    public final File fileAssets;
    private final String launchedVersion;
    private final Proxy proxy;
    private ISaveFormat saveLoader;
    private static int debugFPS;
    private int rightClickDelayTimer;
    private boolean refreshTexturePacksScheduled;
    private String serverName;
    private int serverPort;
    public boolean inGameHasFocus;
    long systemTime = getSystemTime();
    private int joinPlayerCounter;
    private final boolean jvm64bit;
    private final boolean isDemo;
    private NetworkManager myNetworkManager;
    private boolean integratedServerIsRunning;
    public final Profiler mcProfiler = new Profiler();
    private long field_83002_am = -1L;
    private IReloadableResourceManager mcResourceManager;
    private final IMetadataSerializer metadataSerializer_ = new IMetadataSerializer();
    public List defaultResourcePacks = Lists.newArrayList();
    public DefaultResourcePack mcDefaultResourcePack;
    private ResourcePackRepository mcResourcePackRepository;
    private LanguageManager mcLanguageManager;
    private IStream field_152353_at;
    private Framebuffer framebufferMc;
    private TextureMap textureMapBlocks;
    private SoundHandler mcSoundHandler;
    private MusicTicker mcMusicTicker;
    private ResourceLocation field_152354_ay;
    private final MinecraftSessionService field_152355_az;
    private SkinManager field_152350_aA;
    private final Queue field_152351_aB = Queues.newArrayDeque();
    private final Thread field_152352_aC = Thread.currentThread();
    public volatile boolean running = true;
    public String debug = "";
    long debugUpdateTime = getSystemTime();
    int fpsCounter;
    long prevFrameTime = -1L;
    private String debugProfilerName = "root";
    private static final String __OBFID = "CL_00000631";
    private static int max_texture_size;
    @Unique
    @MixinMerged(
        mixin = "com.mitchej123.hodgepodge.mixins.early.minecraft.MixinMinecraft_FastBlockPlacing",
        priority = 1000,
        sessionId = "98d213e4-85ca-4ce4-959f-84858add440a"
    )
    private final Vector3i currentPosition = new Vector3i(0, 0, 0);
    @Unique
    @MixinMerged(
        mixin = "com.mitchej123.hodgepodge.mixins.early.minecraft.MixinMinecraft_FastBlockPlacing",
        priority = 1000,
        sessionId = "98d213e4-85ca-4ce4-959f-84858add440a"
    )
    private final Vector3i comparePosition = new Vector3i(0, 0, 0);
    @Unique
    @MixinMerged(
        mixin = "com.mitchej123.hodgepodge.mixins.early.minecraft.MixinMinecraft_FastBlockPlacing",
        priority = 1000,
        sessionId = "98d213e4-85ca-4ce4-959f-84858add440a"
    )
    private final Vector3i lastPosition = new Vector3i(0, 0, 0);
    @Unique
    @MixinMerged(
        mixin = "com.mitchej123.hodgepodge.mixins.early.minecraft.MixinMinecraft_FastBlockPlacing",
        priority = 1000,
        sessionId = "98d213e4-85ca-4ce4-959f-84858add440a"
    )
    private ForgeDirection lastSide;

    public Minecraft(Session sessionIn, int displayWidth, int displayHeight, boolean fullscreen, boolean isDemo, File dataDir, File assetsDir, File resourcePackDir, Proxy proxy, String version, Multimap twitchDetails, String assetsJsonVersion) {
        theMinecraft = this;
        this.mcDataDir = dataDir;
        this.fileAssets = assetsDir;
        this.fileResourcepacks = resourcePackDir;
        this.launchedVersion = version;
        this.field_152356_J = twitchDetails;
        this.mcDefaultResourcePack = new DefaultResourcePack((new ResourceIndex(assetsDir, assetsJsonVersion)).func_152782_a());
        this.addDefaultResourcePack();
        this.proxy = proxy == null ? Proxy.NO_PROXY : proxy;
        this.field_152355_az = (new YggdrasilAuthenticationService(proxy, UUID.randomUUID().toString())).createMinecraftSessionService();
        this.startTimerHackThread();
        this.session = sessionIn;
        logger.info("Setting user: " + sessionIn.getUsername());
        this.isDemo = isDemo;
        this.displayWidth = displayWidth;
        this.displayHeight = displayHeight;
        this.tempDisplayWidth = displayWidth;
        this.tempDisplayHeight = displayHeight;
        this.fullscreen = fullscreen;
        this.jvm64bit = isJvm64bit();
        ImageIO.setUseCache(false);
        Bootstrap.func_151354_b();
    }

    private static boolean isJvm64bit() {
        String[] astring = new String[]{"sun.arch.data.model", "com.ibm.vm.bitmode", "os.arch"};
        String[] astring1 = astring;
        int i = astring.length;

        for(int j = 0; j < i; ++j) {
            String s = astring1[j];
            String s1 = System.getProperty(s);
            if (s1 != null && s1.contains("64")) {
                return true;
            }
        }

        return false;
    }

    public Framebuffer getFramebuffer() {
        return this.framebufferMc;
    }

    private void startTimerHackThread() {
        Thread thread = new 1(this, "Timer hack thread");
        thread.setDaemon(true);
        thread.start();
    }

    public void crashed(CrashReport crash) {
        this.hasCrashed = true;
        this.crashReporter = crash;
    }

    public void displayCrashReport(CrashReport crashReportIn) {
        File file1 = new File(getMinecraft().mcDataDir, "crash-reports");
        File file2 = new File(file1, "crash-" + (new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss")).format(new Date()) + "-client.txt");
        System.out.println(crashReportIn.getCompleteReport());
        byte retVal;
        if (crashReportIn.getFile() != null) {
            System.out.println("#@!@# Game crashed! Crash report saved to: #@!@# " + crashReportIn.getFile());
            retVal = -1;
        } else if (crashReportIn.saveToFile(file2)) {
            System.out.println("#@!@# Game crashed! Crash report saved to: #@!@# " + file2.getAbsolutePath());
            retVal = -1;
        } else {
            System.out.println("#@?@# Game crashed! Crash report could not be saved. #@?@#");
            retVal = -2;
        }

        FMLCommonHandler.instance().handleExit(retVal);
    }

    public void setServer(String serverHostname, int serverPort) {
        this.serverName = serverHostname;
        this.serverPort = serverPort;
    }

    private void startGame() throws LWJGLException {
        this.gameSettings = new GameSettings(this, this.mcDataDir);
        if (this.gameSettings.overrideHeight > 0 && this.gameSettings.overrideWidth > 0) {
            this.displayWidth = this.gameSettings.overrideWidth;
            this.displayHeight = this.gameSettings.overrideHeight;
        }

        if (this.fullscreen) {
            Display.setFullscreen(true);
            this.displayWidth = Display.getDisplayMode().getWidth();
            this.displayHeight = Display.getDisplayMode().getHeight();
            if (this.displayWidth <= 0) {
                this.displayWidth = 1;
            }

            if (this.displayHeight <= 0) {
                this.displayHeight = 1;
            }
        } else {
            Display.setDisplayMode(new DisplayMode(this.displayWidth, this.displayHeight));
        }

        Display.setResizable(true);
        Display.setTitle("Minecraft 1.7.10");
        logger.info("LWJGL Version: " + Sys.getVersion());
        EnumOS enumos = Util.getOSType();
        if (enumos != EnumOS.OSX) {
            try {
                InputStream inputstream = this.mcDefaultResourcePack.func_152780_c(new ResourceLocation("icons/icon_16x16.png"));
                InputStream inputstream1 = this.mcDefaultResourcePack.func_152780_c(new ResourceLocation("icons/icon_32x32.png"));
                if (inputstream != null && inputstream1 != null) {
                    Display.setIcon(new ByteBuffer[]{this.func_152340_a(inputstream), this.func_152340_a(inputstream1)});
                }
            } catch (IOException var8) {
                logger.error("Couldn't set icon", var8);
            }
        }

        try {
            ForgeHooksClient.createDisplay();
        } catch (LWJGLException var7) {
            logger.error("Couldn't set pixel format", var7);

            try {
                Thread.sleep(1000L);
            } catch (InterruptedException var6) {
            }

            if (this.fullscreen) {
                this.updateDisplayMode();
            }

            Display.create();
        }

        OpenGlHelper.initializeTextures();

        try {
            this.field_152353_at = new TwitchStream(this, (String)Iterables.getFirst(this.field_152356_J.get("twitch_access_token"), (Object)null));
        } catch (Throwable var5) {
            this.field_152353_at = new NullStream(var5);
            logger.error("Couldn't initialize twitch stream");
        }

        this.framebufferMc = new Framebuffer(this.displayWidth, this.displayHeight, true);
        this.framebufferMc.setFramebufferColor(0.0F, 0.0F, 0.0F, 0.0F);
        this.guiAchievement = new GuiAchievement(this);
        this.metadataSerializer_.registerMetadataSectionType(new TextureMetadataSectionSerializer(), TextureMetadataSection.class);
        this.metadataSerializer_.registerMetadataSectionType(new FontMetadataSectionSerializer(), FontMetadataSection.class);
        this.metadataSerializer_.registerMetadataSectionType(new AnimationMetadataSectionSerializer(), AnimationMetadataSection.class);
        this.metadataSerializer_.registerMetadataSectionType(new PackMetadataSectionSerializer(), PackMetadataSection.class);
        this.metadataSerializer_.registerMetadataSectionType(new LanguageMetadataSectionSerializer(), LanguageMetadataSection.class);
        this.saveLoader = new AnvilSaveConverter(new File(this.mcDataDir, "saves"));
        this.mcResourcePackRepository = new ResourcePackRepository(this.fileResourcepacks, new File(this.mcDataDir, "server-resource-packs"), this.mcDefaultResourcePack, this.metadataSerializer_, this.gameSettings);
        this.mcResourceManager = new SimpleReloadableResourceManager(this.metadataSerializer_);
        this.mcLanguageManager = new LanguageManager(this.metadataSerializer_, this.gameSettings.language);
        this.mcResourceManager.registerReloadListener(this.mcLanguageManager);
        FMLClientHandler.instance().beginMinecraftLoading(this, this.defaultResourcePacks, this.mcResourceManager);
        this.renderEngine = new TextureManager(this.mcResourceManager);
        this.mcResourceManager.registerReloadListener(this.renderEngine);
        this.field_152350_aA = new SkinManager(this.renderEngine, new File(this.fileAssets, "skins"), this.field_152355_az);
        SplashProgress.drawVanillaScreen();
        this.mcSoundHandler = new SoundHandler(this.mcResourceManager, this.gameSettings);
        this.mcResourceManager.registerReloadListener(this.mcSoundHandler);
        this.mcMusicTicker = new MusicTicker(this);
        this.fontRenderer = new FontRenderer(this.gameSettings, new ResourceLocation("textures/font/ascii.png"), this.renderEngine, false);
        if (this.gameSettings.language != null) {
            this.fontRenderer.setUnicodeFlag(this.func_152349_b());
            this.fontRenderer.setBidiFlag(this.mcLanguageManager.isCurrentLanguageBidirectional());
        }

        this.standardGalacticFontRenderer = new FontRenderer(this.gameSettings, new ResourceLocation("textures/font/ascii_sga.png"), this.renderEngine, false);
        this.mcResourceManager.registerReloadListener(this.fontRenderer);
        this.mcResourceManager.registerReloadListener(this.standardGalacticFontRenderer);
        this.mcResourceManager.registerReloadListener(new GrassColorReloadListener());
        this.mcResourceManager.registerReloadListener(new FoliageColorReloadListener());
        ProgressBar bar = ProgressManager.push("Rendering Setup", 9, true);
        bar.step("Loading Render Manager");
        RenderManager.instance.itemRenderer = new ItemRenderer(this);
        bar.step("Loading Entity Renderer");
        this.entityRenderer = new EntityRenderer(this, this.mcResourceManager);
        this.mcResourceManager.registerReloadListener(this.entityRenderer);
        AchievementList.openInventory.setStatStringFormatter(new 2(this));
        bar.step("Loading GL properties");
        this.mouseHelper = new MouseHelper();
        this.checkGLError("Pre startup");
        GL11.glEnable(3553);
        GL11.glShadeModel(7425);
        GL11.glClearDepth(1.0D);
        GL11.glEnable(2929);
        GL11.glDepthFunc(515);
        GL11.glEnable(3008);
        GL11.glAlphaFunc(516, 0.1F);
        GL11.glCullFace(1029);
        GL11.glMatrixMode(5889);
        GL11.glLoadIdentity();
        GL11.glMatrixMode(5888);
        this.checkGLError("Startup");
        bar.step("Render Global instance");
        this.renderGlobal = new RenderGlobal(this);
        bar.step("Building Blocks Texture");
        this.textureMapBlocks = new TextureMap(0, "textures/blocks", true);
        bar.step("Anisotropy and Mipmaps");
        this.textureMapBlocks.setAnisotropicFiltering(this.gameSettings.anisotropicFiltering);
        this.textureMapBlocks.setMipmapLevels(this.gameSettings.mipmapLevels);
        bar.step("Loading Blocks Texture");
        this.renderEngine.loadTextureMap(TextureMap.locationBlocksTexture, this.textureMapBlocks);
        bar.step("Loading Items Texture");
        this.renderEngine.loadTextureMap(TextureMap.locationItemsTexture, new TextureMap(1, "textures/items", true));
        bar.step("Viewport");
        GL11.glViewport(0, 0, this.displayWidth, this.displayHeight);
        this.effectRenderer = new EffectRenderer(this.theWorld, this.renderEngine);
        ProgressManager.pop(bar);
        FMLClientHandler.instance().finishMinecraftLoading();
        this.checkGLError("Post startup");
        this.ingameGUI = new GuiIngameForge(this);
        if (this.serverName != null) {
            FMLClientHandler.instance().connectToServerAtStartup(this.serverName, this.serverPort);
        } else {
            this.displayGuiScreen(new GuiMainMenu());
        }

        SplashProgress.clearVanillaResources(this.renderEngine, this.field_152354_ay);
        this.field_152354_ay = null;
        this.loadingScreen = new LoadingScreenRenderer(this);
        FMLClientHandler.instance().onInitializationComplete();
        if (this.gameSettings.fullScreen && !this.fullscreen) {
            this.toggleFullscreen();
        }

        try {
            Display.setVSyncEnabled(this.gameSettings.enableVsync);
        } catch (OpenGLException var4) {
            this.gameSettings.enableVsync = false;
            this.gameSettings.saveOptions();
        }

        this.handler$zzc000$mymodid$example$sayHello((CallbackInfo)null);
    }

    public boolean func_152349_b() {
        return this.mcLanguageManager.isCurrentLocaleUnicode() || this.gameSettings.forceUnicodeFont;
    }

    public void refreshResources() {
        ArrayList arraylist = Lists.newArrayList(this.defaultResourcePacks);
        Iterator iterator = this.mcResourcePackRepository.getRepositoryEntries().iterator();

        while(iterator.hasNext()) {
            Entry entry = (Entry)iterator.next();
            arraylist.add(entry.getResourcePack());
        }

        if (this.mcResourcePackRepository.func_148530_e() != null) {
            arraylist.add(this.mcResourcePackRepository.func_148530_e());
        }

        try {
            this.mcResourceManager.reloadResources(arraylist);
        } catch (RuntimeException var4) {
            logger.info("Caught error stitching, removing all assigned resourcepacks", var4);
            arraylist.clear();
            arraylist.addAll(this.defaultResourcePacks);
            this.mcResourcePackRepository.func_148527_a(Collections.emptyList());
            this.mcResourceManager.reloadResources(arraylist);
            this.gameSettings.resourcePacks.clear();
            this.gameSettings.saveOptions();
        }

        this.mcLanguageManager.parseLanguageMetadata(arraylist);
        if (this.renderGlobal != null) {
            this.renderGlobal.loadRenderers();
        }

    }

    private void addDefaultResourcePack() {
        this.defaultResourcePacks.add(this.mcDefaultResourcePack);
    }

    private ByteBuffer func_152340_a(InputStream imageStream) throws IOException {
        BufferedImage bufferedimage = ImageIO.read(imageStream);
        int[] aint = bufferedimage.getRGB(0, 0, bufferedimage.getWidth(), bufferedimage.getHeight(), (int[])null, 0, bufferedimage.getWidth());
        ByteBuffer bytebuffer = ByteBuffer.allocate(4 * aint.length);
        int[] aint1 = aint;
        int i = aint.length;

        for(int j = 0; j < i; ++j) {
            int k = aint1[j];
            bytebuffer.putInt(k << 8 | k >> 24 & 255);
        }

        bytebuffer.flip();
        return bytebuffer;
    }

    private void updateDisplayMode() throws LWJGLException {
        HashSet hashset = new HashSet();
        Collections.addAll(hashset, Display.getAvailableDisplayModes());
        DisplayMode displaymode = Display.getDesktopDisplayMode();
        if (!hashset.contains(displaymode) && Util.getOSType() == EnumOS.OSX) {
            Iterator iterator = macDisplayModes.iterator();

            label49:
            while(true) {
                while(true) {
                    DisplayMode displaymode1;
                    boolean flag;
                    Iterator iterator1;
                    DisplayMode displaymode2;
                    do {
                        if (!iterator.hasNext()) {
                            break label49;
                        }

                        displaymode1 = (DisplayMode)iterator.next();
                        flag = true;
                        iterator1 = hashset.iterator();

                        while(iterator1.hasNext()) {
                            displaymode2 = (DisplayMode)iterator1.next();
                            if (displaymode2.getBitsPerPixel() == 32 && displaymode2.getWidth() == displaymode1.getWidth() && displaymode2.getHeight() == displaymode1.getHeight()) {
                                flag = false;
                                break;
                            }
                        }
                    } while(flag);

                    iterator1 = hashset.iterator();

                    while(iterator1.hasNext()) {
                        displaymode2 = (DisplayMode)iterator1.next();
                        if (displaymode2.getBitsPerPixel() == 32 && displaymode2.getWidth() == displaymode1.getWidth() / 2 && displaymode2.getHeight() == displaymode1.getHeight() / 2) {
                            displaymode = displaymode2;
                            break;
                        }
                    }
                }
            }
        }

        Display.setDisplayMode(displaymode);
        this.displayWidth = displaymode.getWidth();
        this.displayHeight = displaymode.getHeight();
    }

    public void loadScreen() throws LWJGLException {
        ScaledResolution scaledresolution = new ScaledResolution(this, this.displayWidth, this.displayHeight);
        int i = scaledresolution.getScaleFactor();
        Framebuffer framebuffer = new Framebuffer(scaledresolution.getScaledWidth() * i, scaledresolution.getScaledHeight() * i, true);
        framebuffer.bindFramebuffer(false);
        GL11.glMatrixMode(5889);
        GL11.glLoadIdentity();
        GL11.glOrtho(0.0D, (double)scaledresolution.getScaledWidth(), (double)scaledresolution.getScaledHeight(), 0.0D, 1000.0D, 3000.0D);
        GL11.glMatrixMode(5888);
        GL11.glLoadIdentity();
        GL11.glTranslatef(0.0F, 0.0F, -2000.0F);
        GL11.glDisable(2896);
        GL11.glDisable(2912);
        GL11.glDisable(2929);
        GL11.glEnable(3553);

        try {
            this.field_152354_ay = this.renderEngine.getDynamicTextureLocation("logo", new DynamicTexture(ImageIO.read(this.mcDefaultResourcePack.getInputStream(locationMojangPng))));
            this.renderEngine.bindTexture(this.field_152354_ay);
        } catch (IOException var7) {
            logger.error("Unable to load logo: " + locationMojangPng, var7);
        }

        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.setColorOpaque_I(16777215);
        tessellator.addVertexWithUV(0.0D, (double)this.displayHeight, 0.0D, 0.0D, 0.0D);
        tessellator.addVertexWithUV((double)this.displayWidth, (double)this.displayHeight, 0.0D, 0.0D, 0.0D);
        tessellator.addVertexWithUV((double)this.displayWidth, 0.0D, 0.0D, 0.0D, 0.0D);
        tessellator.addVertexWithUV(0.0D, 0.0D, 0.0D, 0.0D, 0.0D);
        tessellator.draw();
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        tessellator.setColorOpaque_I(16777215);
        short short1 = 256;
        short short2 = 256;
        this.scaledTessellator((scaledresolution.getScaledWidth() - short1) / 2, (scaledresolution.getScaledHeight() - short2) / 2, 0, 0, short1, short2);
        GL11.glDisable(2896);
        GL11.glDisable(2912);
        framebuffer.unbindFramebuffer();
        framebuffer.framebufferRender(scaledresolution.getScaledWidth() * i, scaledresolution.getScaledHeight() * i);
        GL11.glEnable(3008);
        GL11.glAlphaFunc(516, 0.1F);
        GL11.glFlush();
        this.func_147120_f();
    }

    public void scaledTessellator(int width, int height, int width2, int height2, int stdTextureWidth, int stdTextureHeight) {
        float f = 0.00390625F;
        float f1 = 0.00390625F;
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV((double)(width + 0), (double)(height + stdTextureHeight), 0.0D, (double)((float)(width2 + 0) * f), (double)((float)(height2 + stdTextureHeight) * f1));
        tessellator.addVertexWithUV((double)(width + stdTextureWidth), (double)(height + stdTextureHeight), 0.0D, (double)((float)(width2 + stdTextureWidth) * f), (double)((float)(height2 + stdTextureHeight) * f1));
        tessellator.addVertexWithUV((double)(width + stdTextureWidth), (double)(height + 0), 0.0D, (double)((float)(width2 + stdTextureWidth) * f), (double)((float)(height2 + 0) * f1));
        tessellator.addVertexWithUV((double)(width + 0), (double)(height + 0), 0.0D, (double)((float)(width2 + 0) * f), (double)((float)(height2 + 0) * f1));
        tessellator.draw();
    }

    public ISaveFormat getSaveLoader() {
        return this.saveLoader;
    }

    public void displayGuiScreen(GuiScreen guiScreenIn) {
        if (guiScreenIn == null && this.theWorld == null) {
            guiScreenIn = new GuiMainMenu();
        } else if (guiScreenIn == null && this.thePlayer.getHealth() <= 0.0F) {
            guiScreenIn = new GuiGameOver();
        }

        GuiScreen old = this.currentScreen;
        GuiOpenEvent event = new GuiOpenEvent((GuiScreen)guiScreenIn);
        if (!MinecraftForge.EVENT_BUS.post(event)) {
            GuiScreen guiScreenIn = event.gui;
            if (old != null && guiScreenIn != old) {
                old.onGuiClosed();
            }

            if (guiScreenIn instanceof GuiMainMenu) {
                this.gameSettings.showDebugInfo = false;
                this.ingameGUI.getChatGUI().clearChatMessages();
            }

            this.currentScreen = guiScreenIn;
            if (guiScreenIn != null) {
                this.setIngameNotInFocus();
                ScaledResolution scaledresolution = new ScaledResolution(this, this.displayWidth, this.displayHeight);
                int i = scaledresolution.getScaledWidth();
                int j = scaledresolution.getScaledHeight();
                guiScreenIn.setWorldAndResolution(this, i, j);
                this.skipRenderWorld = false;
            } else {
                SoundHandler injectorAllocatedLocal7 = this.mcSoundHandler;
                if (this.wrapWithCondition$zhb000$hodgepodge$fixDuplicateSounds(injectorAllocatedLocal7, old)) {
                    injectorAllocatedLocal7.resumeSounds();
                }

                this.setIngameFocus();
            }

        }
    }

    private void checkGLError(String message) {
        int i = GL11.glGetError();
        if (i != 0) {
            String s1 = GLU.gluErrorString(i);
            logger.error("########## GL ERROR ##########");
            logger.error("@ " + message);
            logger.error(i + ": " + s1);
        }

    }

    public void shutdownMinecraftApplet() {
        try {
            this.field_152353_at.func_152923_i();
            logger.info("Stopping!");

            try {
                this.loadWorld((WorldClient)null);
            } catch (Throwable var7) {
            }

            try {
                GLAllocation.deleteTexturesAndDisplayLists();
            } catch (Throwable var6) {
            }

            this.mcSoundHandler.unloadSounds();
        } finally {
            Display.destroy();
            if (!this.hasCrashed) {
                ExitVisitor.systemExitCalled(0);
            }

        }

        System.gc();
    }

    public void run() {
        this.running = true;

        CrashReport crashreport;
        try {
            this.startGame();
        } catch (Throwable var11) {
            crashreport = CrashReport.makeCrashReport(var11, "Initializing game");
            crashreport.makeCategory("Initialization");
            this.displayCrashReport(this.addGraphicsAndWorldToCrashReport(crashreport));
            return;
        }

        while(true) {
            try {
                if (!this.running) {
                    break;
                }

                if (!this.hasCrashed || this.crashReporter == null) {
                    try {
                        this.runGameLoop();
                    } catch (OutOfMemoryError var10) {
                        this.freeMemory();
                        this.displayGuiScreen(new GuiMemoryErrorScreen());
                        System.gc();
                    }
                    continue;
                }

                this.displayCrashReport(this.crashReporter);
            } catch (MinecraftError var12) {
                break;
            } catch (ReportedException var13) {
                this.addGraphicsAndWorldToCrashReport(var13.getCrashReport());
                this.freeMemory();
                logger.fatal("Reported exception thrown!", var13);
                this.displayCrashReport(var13.getCrashReport());
                break;
            } catch (Throwable var14) {
                crashreport = this.addGraphicsAndWorldToCrashReport(new CrashReport("Unexpected error", var14));
                this.freeMemory();
                logger.fatal("Unreported exception thrown!", var14);
                this.displayCrashReport(crashreport);
                break;
            } finally {
                this.shutdownMinecraftApplet();
            }

            return;
        }

    }

    private void runGameLoop() {
        this.mcProfiler.startSection("root");
        if (Display.isCreated() && Display.isCloseRequested()) {
            this.shutdown();
        }

        if (this.isGamePaused && this.theWorld != null) {
            float f = this.timer.renderPartialTicks;
            this.timer.updateTimer();
            this.timer.renderPartialTicks = f;
        } else {
            this.timer.updateTimer();
        }

        if ((this.theWorld == null || this.currentScreen == null) && this.refreshTexturePacksScheduled) {
            this.refreshTexturePacksScheduled = false;
            this.refreshResources();
        }

        long j = System.nanoTime();
        this.mcProfiler.startSection("tick");

        for(int i = 0; i < this.timer.elapsedTicks; ++i) {
            this.runTick();
        }

        this.mcProfiler.endStartSection("preRenderErrors");
        long k = System.nanoTime() - j;
        this.checkGLError("Pre render");
        RenderBlocks.fancyGrass = this.gameSettings.fancyGraphics;
        this.mcProfiler.endStartSection("sound");
        this.mcSoundHandler.setListener(this.thePlayer, this.timer.renderPartialTicks);
        this.mcProfiler.endSection();
        this.mcProfiler.startSection("render");
        GL11.glPushMatrix();
        GL11.glClear(16640);
        this.framebufferMc.bindFramebuffer(true);
        this.mcProfiler.startSection("display");
        GL11.glEnable(3553);
        if (this.thePlayer != null && this.thePlayer.isEntityInsideOpaqueBlock()) {
            this.gameSettings.thirdPersonView = 0;
        }

        this.mcProfiler.endSection();
        if (!this.skipRenderWorld) {
            FMLCommonHandler.instance().onRenderTickStart(this.timer.renderPartialTicks);
            this.mcProfiler.endStartSection("gameRenderer");
            this.entityRenderer.updateCameraAndRender(this.timer.renderPartialTicks);
            this.mcProfiler.endSection();
            FMLCommonHandler.instance().onRenderTickEnd(this.timer.renderPartialTicks);
        }

        GL11.glFlush();
        this.mcProfiler.endSection();
        if (!this.redirect$zek000$hodgepodge$fixUnfocusedFullscreen() && this.fullscreen) {
            this.toggleFullscreen();
        }

        if (this.gameSettings.showDebugInfo && this.gameSettings.showDebugProfilerChart) {
            if (!this.mcProfiler.profilingEnabled) {
                this.mcProfiler.clearProfiling();
            }

            this.mcProfiler.profilingEnabled = true;
            this.displayDebugInfo(k);
        } else {
            this.mcProfiler.profilingEnabled = false;
            this.prevFrameTime = System.nanoTime();
        }

        this.guiAchievement.func_146254_a();
        this.framebufferMc.unbindFramebuffer();
        GL11.glPopMatrix();
        GL11.glPushMatrix();
        this.framebufferMc.framebufferRender(this.displayWidth, this.displayHeight);
        GL11.glPopMatrix();
        GL11.glPushMatrix();
        this.entityRenderer.func_152430_c(this.timer.renderPartialTicks);
        GL11.glPopMatrix();
        this.mcProfiler.startSection("root");
        this.func_147120_f();
        Thread.yield();
        this.mcProfiler.startSection("stream");
        this.mcProfiler.startSection("update");
        this.field_152353_at.func_152935_j();
        this.mcProfiler.endStartSection("submit");
        this.field_152353_at.func_152922_k();
        this.mcProfiler.endSection();
        this.mcProfiler.endSection();
        this.checkGLError("Post render");
        ++this.fpsCounter;
        this.isGamePaused = this.isSingleplayer() && this.currentScreen != null && this.currentScreen.doesGuiPauseGame() && !this.theIntegratedServer.getPublic();

        while(getSystemTime() >= this.debugUpdateTime + 1000L) {
            debugFPS = this.fpsCounter;
            this.debug = debugFPS + " fps, " + WorldRenderer.chunksUpdated + " chunk updates";
            WorldRenderer.chunksUpdated = 0;
            this.debugUpdateTime += 1000L;
            this.fpsCounter = 0;
            this.usageSnooper.addMemoryStatsToSnooper();
            if (!this.usageSnooper.isSnooperRunning()) {
                this.usageSnooper.startSnooper();
            }
        }

        this.mcProfiler.endSection();
        if (this.isFramerateLimitBelowMax()) {
            Display.sync(this.getLimitFramerate());
        }

    }

    public void func_147120_f() {
        Display.update();
        if (!this.fullscreen && Display.wasResized()) {
            int i = this.displayWidth;
            int j = this.displayHeight;
            this.displayWidth = Display.getWidth();
            this.displayHeight = Display.getHeight();
            if (this.displayWidth != i || this.displayHeight != j) {
                if (this.displayWidth <= 0) {
                    this.displayWidth = 1;
                }

                if (this.displayHeight <= 0) {
                    this.displayHeight = 1;
                }

                this.resize(this.displayWidth, this.displayHeight);
            }
        }

    }

    public int getLimitFramerate() {
        return this.theWorld == null && this.currentScreen != null ? 30 : this.gameSettings.limitFramerate;
    }

    public boolean isFramerateLimitBelowMax() {
        return (float)this.getLimitFramerate() < Options.FRAMERATE_LIMIT.getValueMax();
    }

    public void freeMemory() {
        try {
            memoryReserve = new byte[0];
            this.renderGlobal.deleteAllDisplayLists();
        } catch (Throwable var4) {
        }

        try {
            System.gc();
        } catch (Throwable var3) {
        }

        try {
            System.gc();
            this.loadWorld((WorldClient)null);
        } catch (Throwable var2) {
        }

        System.gc();
    }

    private void updateDebugProfilerName(int keyCount) {
        List list = this.mcProfiler.getProfilingData(this.debugProfilerName);
        if (list != null && !list.isEmpty()) {
            Result result = (Result)list.remove(0);
            if (keyCount == 0) {
                if (result.field_76331_c.length() > 0) {
                    int j = this.debugProfilerName.lastIndexOf(".");
                    if (j >= 0) {
                        this.debugProfilerName = this.debugProfilerName.substring(0, j);
                    }
                }
            } else {
                --keyCount;
                if (keyCount < list.size() && !((Result)list.get(keyCount)).field_76331_c.equals("unspecified")) {
                    if (this.debugProfilerName.length() > 0) {
                        this.debugProfilerName = this.debugProfilerName + ".";
                    }

                    this.debugProfilerName = this.debugProfilerName + ((Result)list.get(keyCount)).field_76331_c;
                }
            }
        }

    }

    private void displayDebugInfo(long elapsedTicksTime) {
        if (this.mcProfiler.profilingEnabled) {
            List list = this.mcProfiler.getProfilingData(this.debugProfilerName);
            Result result = (Result)list.remove(0);
            GL11.glClear(256);
            GL11.glMatrixMode(5889);
            GL11.glEnable(2903);
            GL11.glLoadIdentity();
            GL11.glOrtho(0.0D, (double)this.displayWidth, (double)this.displayHeight, 0.0D, 1000.0D, 3000.0D);
            GL11.glMatrixMode(5888);
            GL11.glLoadIdentity();
            GL11.glTranslatef(0.0F, 0.0F, -2000.0F);
            GL11.glLineWidth(1.0F);
            GL11.glDisable(3553);
            Tessellator tessellator = Tessellator.instance;
            short short1 = 160;
            int j = this.displayWidth - short1 - 10;
            int k = this.displayHeight - short1 * 2;
            GL11.glEnable(3042);
            tessellator.startDrawingQuads();
            tessellator.setColorRGBA_I(0, 200);
            tessellator.addVertex((double)((float)j - (float)short1 * 1.1F), (double)((float)k - (float)short1 * 0.6F - 16.0F), 0.0D);
            tessellator.addVertex((double)((float)j - (float)short1 * 1.1F), (double)(k + short1 * 2), 0.0D);
            tessellator.addVertex((double)((float)j + (float)short1 * 1.1F), (double)(k + short1 * 2), 0.0D);
            tessellator.addVertex((double)((float)j + (float)short1 * 1.1F), (double)((float)k - (float)short1 * 0.6F - 16.0F), 0.0D);
            tessellator.draw();
            GL11.glDisable(3042);
            double d0 = 0.0D;

            int i1;
            int k1;
            for(int l = 0; l < list.size(); ++l) {
                Result result1 = (Result)list.get(l);
                i1 = MathHelper.floor_double(result1.field_76332_a / 4.0D) + 1;
                tessellator.startDrawing(6);
                tessellator.setColorOpaque_I(result1.func_76329_a());
                tessellator.addVertex((double)j, (double)k, 0.0D);

                float f;
                float f1;
                float f2;
                for(k1 = i1; k1 >= 0; --k1) {
                    f = (float)((d0 + result1.field_76332_a * (double)k1 / (double)i1) * 3.141592653589793D * 2.0D / 100.0D);
                    f1 = MathHelper.sin(f) * (float)short1;
                    f2 = MathHelper.cos(f) * (float)short1 * 0.5F;
                    tessellator.addVertex((double)((float)j + f1), (double)((float)k - f2), 0.0D);
                }

                tessellator.draw();
                tessellator.startDrawing(5);
                tessellator.setColorOpaque_I((result1.func_76329_a() & 16711422) >> 1);

                for(k1 = i1; k1 >= 0; --k1) {
                    f = (float)((d0 + result1.field_76332_a * (double)k1 / (double)i1) * 3.141592653589793D * 2.0D / 100.0D);
                    f1 = MathHelper.sin(f) * (float)short1;
                    f2 = MathHelper.cos(f) * (float)short1 * 0.5F;
                    tessellator.addVertex((double)((float)j + f1), (double)((float)k - f2), 0.0D);
                    tessellator.addVertex((double)((float)j + f1), (double)((float)k - f2 + 10.0F), 0.0D);
                }

                tessellator.draw();
                d0 += result1.field_76332_a;
            }

            DecimalFormat decimalformat = new DecimalFormat("##0.00");
            GL11.glEnable(3553);
            String s = "";
            if (!result.field_76331_c.equals("unspecified")) {
                s = s + "[0] ";
            }

            if (result.field_76331_c.length() == 0) {
                s = s + "ROOT ";
            } else {
                s = s + result.field_76331_c + " ";
            }

            i1 = 16777215;
            this.fontRenderer.drawStringWithShadow(s, j - short1, k - short1 / 2 - 16, i1);
            this.fontRenderer.drawStringWithShadow(s = decimalformat.format(result.field_76330_b) + "%", j + short1 - this.fontRenderer.getStringWidth(s), k - short1 / 2 - 16, i1);

            for(k1 = 0; k1 < list.size(); ++k1) {
                Result result2 = (Result)list.get(k1);
                String s1 = "";
                if (result2.field_76331_c.equals("unspecified")) {
                    s1 = s1 + "[?] ";
                } else {
                    s1 = s1 + "[" + (k1 + 1) + "] ";
                }

                s1 = s1 + result2.field_76331_c;
                FontRenderer var10000 = this.fontRenderer;
                int var10002 = j - short1;
                int var10003 = k + short1 / 2 + k1 * 8 + 20;
                int injectorAllocatedLocal22 = result2.func_76329_a();
                int injectorAllocatedLocal21 = var10003;
                int injectorAllocatedLocal20 = var10002;
                FontRenderer injectorAllocatedLocal18 = var10000;
                this.redirect$zdk000$hodgepodge$drawLongString(injectorAllocatedLocal18, s1, injectorAllocatedLocal20, injectorAllocatedLocal21, injectorAllocatedLocal22);
                this.fontRenderer.drawStringWithShadow(s1 = decimalformat.format(result2.field_76332_a) + "%", j + short1 - 50 - this.fontRenderer.getStringWidth(s1), k + short1 / 2 + k1 * 8 + 20, result2.func_76329_a());
                this.fontRenderer.drawStringWithShadow(s1 = decimalformat.format(result2.field_76330_b) + "%", j + short1 - this.fontRenderer.getStringWidth(s1), k + short1 / 2 + k1 * 8 + 20, result2.func_76329_a());
            }
        }

    }

    public void shutdown() {
        this.running = false;
    }

    public void setIngameFocus() {
        if (Display.isActive() && !this.inGameHasFocus) {
            this.inGameHasFocus = true;
            this.handler$zfl000$hodgepodge$updateKeysStates((CallbackInfo)null);
            this.mouseHelper.grabMouseCursor();
            this.displayGuiScreen((GuiScreen)null);
            this.leftClickCounter = 10000;
        }

    }

    public void setIngameNotInFocus() {
        if (this.inGameHasFocus) {
            KeyBinding.unPressAllKeys();
            this.inGameHasFocus = false;
            this.mouseHelper.ungrabMouseCursor();
        }

    }

    public void displayInGameMenu() {
        if (this.currentScreen == null) {
            this.displayGuiScreen(new GuiIngameMenu());
            if (this.isSingleplayer() && !this.theIntegratedServer.getPublic()) {
                this.mcSoundHandler.pauseSounds();
            }
        }

    }

    private void func_147115_a(boolean leftClick) {
        if (!leftClick) {
            this.leftClickCounter = 0;
        }

        if (this.leftClickCounter <= 0) {
            if (leftClick && this.objectMouseOver != null && this.objectMouseOver.typeOfHit == MovingObjectType.BLOCK) {
                int i = this.objectMouseOver.blockX;
                int j = this.objectMouseOver.blockY;
                int k = this.objectMouseOver.blockZ;
                if (this.theWorld.getBlock(i, j, k).getMaterial() != Material.air) {
                    this.playerController.onPlayerDamageBlock(i, j, k, this.objectMouseOver.sideHit);
                    if (this.thePlayer.isCurrentToolAdventureModeExempt(i, j, k)) {
                        this.effectRenderer.addBlockHitEffects(i, j, k, this.objectMouseOver);
                        this.thePlayer.swingItem();
                    }
                }
            } else {
                this.playerController.resetBlockRemoving();
            }
        }

    }

    private void func_147116_af() {
        if (this.leftClickCounter <= 0) {
            this.thePlayer.swingItem();
            if (this.objectMouseOver == null) {
                logger.error("Null returned as 'hitResult', this shouldn't happen!");
                if (this.playerController.isNotCreative()) {
                    this.leftClickCounter = 10;
                }
            } else {
                switch(net.minecraft.client.Minecraft.SwitchMovingObjectType.field_152390_a[this.objectMouseOver.typeOfHit.ordinal()]) {
                case 1:
                    this.playerController.attackEntity(this.thePlayer, this.objectMouseOver.entityHit);
                    break;
                case 2:
                    int i = this.objectMouseOver.blockX;
                    int j = this.objectMouseOver.blockY;
                    int k = this.objectMouseOver.blockZ;
                    if (this.theWorld.getBlock(i, j, k).getMaterial() == Material.air) {
                        if (this.playerController.isNotCreative()) {
                            this.leftClickCounter = 10;
                        }
                    } else {
                        this.playerController.clickBlock(i, j, k, this.objectMouseOver.sideHit);
                    }
                }
            }
        }

    }

    private void func_147121_ag() {
        this.rightClickDelayTimer = 4;
        boolean flag = true;
        ItemStack itemstack = this.thePlayer.inventory.getCurrentItem();
        if (this.objectMouseOver == null) {
            logger.warn("Null returned as 'hitResult', this shouldn't happen!");
        } else {
            switch(net.minecraft.client.Minecraft.SwitchMovingObjectType.field_152390_a[this.objectMouseOver.typeOfHit.ordinal()]) {
            case 1:
                if (this.playerController.interactWithEntitySendPacket(this.thePlayer, this.objectMouseOver.entityHit)) {
                    flag = false;
                }
                break;
            case 2:
                int i = this.objectMouseOver.blockX;
                int j = this.objectMouseOver.blockY;
                int k = this.objectMouseOver.blockZ;
                if (!this.theWorld.getBlock(i, j, k).isAir(this.theWorld, i, j, k)) {
                    int l = itemstack != null ? itemstack.stackSize : 0;
                    boolean result = !ForgeEventFactory.onPlayerInteract(this.thePlayer, Action.RIGHT_CLICK_BLOCK, i, j, k, this.objectMouseOver.sideHit, this.theWorld).isCanceled();
                    if (result && this.playerController.onPlayerRightClick(this.thePlayer, this.theWorld, itemstack, i, j, k, this.objectMouseOver.sideHit, this.objectMouseOver.hitVec)) {
                        flag = false;
                        this.thePlayer.swingItem();
                    }

                    if (itemstack == null) {
                        return;
                    }

                    if (itemstack.stackSize == 0) {
                        this.thePlayer.inventory.mainInventory[this.thePlayer.inventory.currentItem] = null;
                    } else if (itemstack.stackSize != l || this.playerController.isInCreativeMode()) {
                        this.entityRenderer.itemRenderer.resetEquippedProgress();
                    }
                }
            }
        }

        if (flag) {
            ItemStack itemstack1 = this.thePlayer.inventory.getCurrentItem();
            boolean result = !ForgeEventFactory.onPlayerInteract(this.thePlayer, Action.RIGHT_CLICK_AIR, 0, 0, 0, -1, this.theWorld).isCanceled();
            if (result && itemstack1 != null && this.playerController.sendUseItem(this.thePlayer, this.theWorld, itemstack1)) {
                this.entityRenderer.itemRenderer.resetEquippedProgress2();
            }
        }

    }

    public void toggleFullscreen() {
        CallbackInfo callbackInfo2 = new CallbackInfo("toggleFullscreen", true);
        this.handler$zzh000$lwjgl3ify$toggleFullscreen(callbackInfo2);
        if (!callbackInfo2.isCancelled()) {
            try {
                this.fullscreen = !this.fullscreen;
                if (this.fullscreen) {
                    this.updateDisplayMode();
                    this.displayWidth = Display.getDisplayMode().getWidth();
                    this.displayHeight = Display.getDisplayMode().getHeight();
                    if (this.displayWidth <= 0) {
                        this.displayWidth = 1;
                    }

                    if (this.displayHeight <= 0) {
                        this.displayHeight = 1;
                    }
                } else {
                    Display.setDisplayMode(new DisplayMode(this.tempDisplayWidth, this.tempDisplayHeight));
                    this.displayWidth = this.tempDisplayWidth;
                    this.displayHeight = this.tempDisplayHeight;
                    if (this.displayWidth <= 0) {
                        this.displayWidth = 1;
                    }

                    if (this.displayHeight <= 0) {
                        this.displayHeight = 1;
                    }
                }

                if (this.currentScreen != null) {
                    this.resize(this.displayWidth, this.displayHeight);
                } else {
                    this.updateFramebufferSize();
                }

                Display.setFullscreen(this.fullscreen);
                this.handler$zej000$hodgepodge$fixFullscreenResizable((CallbackInfo)null);
                Display.setVSyncEnabled(this.gameSettings.enableVsync);
                this.func_147120_f();
            } catch (Exception var3) {
                logger.error("Couldn't toggle fullscreen", var3);
            }

        }
    }

    public void resize(int width, int height) {
        this.displayWidth = width <= 0 ? 1 : width;
        this.displayHeight = height <= 0 ? 1 : height;
        if (this.currentScreen != null) {
            ScaledResolution scaledresolution = new ScaledResolution(this, width, height);
            int k = scaledresolution.getScaledWidth();
            int l = scaledresolution.getScaledHeight();
            this.currentScreen.setWorldAndResolution(this, k, l);
        }

        this.loadingScreen = new LoadingScreenRenderer(this);
        this.updateFramebufferSize();
    }

    private void updateFramebufferSize() {
        this.framebufferMc.createBindFramebuffer(this.displayWidth, this.displayHeight);
        if (this.entityRenderer != null) {
            this.entityRenderer.updateShaderGroupSize(this.displayWidth, this.displayHeight);
        }

    }

    public void runTick() {
        this.mcProfiler.startSection("scheduledExecutables");
        Queue queue = this.field_152351_aB;
        synchronized(this.field_152351_aB) {
            while(!this.field_152351_aB.isEmpty()) {
                ((FutureTask)this.field_152351_aB.poll()).run();
            }
        }

        this.mcProfiler.endSection();
        if (this.rightClickDelayTimer > 0) {
            --this.rightClickDelayTimer;
        }

        FMLCommonHandler.instance().onPreClientTick();
        this.handler$zgn000$hodgepodge$func_147121_ag((CallbackInfo)null);
        this.mcProfiler.startSection("gui");
        if (!this.isGamePaused) {
            this.ingameGUI.updateTick();
        }

        this.mcProfiler.endStartSection("pick");
        this.entityRenderer.getMouseOver(1.0F);
        this.mcProfiler.endStartSection("gameMode");
        if (!this.isGamePaused && this.theWorld != null) {
            this.playerController.updateController();
        }

        this.mcProfiler.endStartSection("textures");
        if (!this.isGamePaused) {
            this.renderEngine.tick();
        }

        if (this.currentScreen == null && this.thePlayer != null) {
            if (this.thePlayer.getHealth() <= 0.0F) {
                this.displayGuiScreen((GuiScreen)null);
            } else if (this.thePlayer.isPlayerSleeping() && this.theWorld != null) {
                this.displayGuiScreen(new GuiSleepMP());
            }
        } else if (this.currentScreen != null && this.currentScreen instanceof GuiSleepMP && !this.thePlayer.isPlayerSleeping()) {
            this.displayGuiScreen((GuiScreen)null);
        }

        if (this.currentScreen != null) {
            this.leftClickCounter = 10000;
        }

        CrashReport crashreport;
        CrashReportCategory crashreportcategory;
        if (this.currentScreen != null) {
            try {
                this.currentScreen.handleInput();
            } catch (Throwable var11) {
                crashreport = CrashReport.makeCrashReport(var11, "Updating screen events");
                crashreportcategory = crashreport.makeCategory("Affected screen");
                crashreportcategory.addCrashSectionCallable("Screen name", new 3(this));
                throw new ReportedException(crashreport);
            }

            if (this.currentScreen != null) {
                try {
                    this.currentScreen.updateScreen();
                } catch (Throwable var10) {
                    crashreport = CrashReport.makeCrashReport(var10, "Ticking screen");
                    crashreportcategory = crashreport.makeCategory("Affected screen");
                    crashreportcategory.addCrashSectionCallable("Screen name", new 4(this));
                    throw new ReportedException(crashreport);
                }
            }
        }

        if (this.currentScreen == null || this.currentScreen.allowUserInput) {
            this.mcProfiler.endStartSection("mouse");

            int j;
            while(Mouse.next()) {
                if (!ForgeHooksClient.postMouseEvent()) {
                    j = Mouse.getEventButton();
                    KeyBinding.setKeyBindState(j - 100, Mouse.getEventButtonState());
                    if (Mouse.getEventButtonState()) {
                        KeyBinding.onTick(j - 100);
                    }

                    long k = getSystemTime() - this.systemTime;
                    if (k <= 200L) {
                        int i = Mouse.getEventDWheel();
                        if (i != 0) {
                            this.thePlayer.inventory.changeCurrentItem(i);
                            if (this.gameSettings.noclip) {
                                if (i > 0) {
                                    i = 1;
                                }

                                if (i < 0) {
                                    i = -1;
                                }

                                GameSettings var10000 = this.gameSettings;
                                var10000.noclipRate += (float)i * 0.25F;
                            }
                        }

                        if (this.currentScreen == null) {
                            if (!this.inGameHasFocus && Mouse.getEventButtonState()) {
                                this.setIngameFocus();
                            }
                        } else if (this.currentScreen != null) {
                            this.currentScreen.handleMouseInput();
                        }
                    }

                    FMLCommonHandler.instance().fireMouseInput();
                }
            }

            if (this.leftClickCounter > 0) {
                --this.leftClickCounter;
            }

            this.mcProfiler.endStartSection("keyboard");

            boolean flag;
            for(; Keyboard.next(); FMLCommonHandler.instance().fireKeyInput()) {
                int var15 = Keyboard.getEventKey();
                boolean injectorAllocatedLocal9 = Keyboard.getEventKeyState();
                int injectorAllocatedLocal8 = var15;
                this.redirect$zzg000$lwjgl3ify$noKeybindUpdateHere(injectorAllocatedLocal8, injectorAllocatedLocal9);
                if (Keyboard.getEventKeyState()) {
                    injectorAllocatedLocal8 = Keyboard.getEventKey();
                    this.redirect$zzg000$lwjgl3ify$noKeybindTickHere(injectorAllocatedLocal8);
                }

                if (this.field_83002_am > 0L) {
                    if (getSystemTime() - this.field_83002_am >= 6000L) {
                        throw new ReportedException(new CrashReport("Manually triggered debug crash", new Throwable()));
                    }

                    if (!Keyboard.isKeyDown(46) || !Keyboard.isKeyDown(61)) {
                        this.field_83002_am = -1L;
                    }
                } else if (Keyboard.isKeyDown(46) && Keyboard.isKeyDown(61)) {
                    this.field_83002_am = getSystemTime();
                }

                this.func_152348_aa();
                if (Keyboard.getEventKeyState()) {
                    if (Keyboard.getEventKey() == 62 && this.entityRenderer != null) {
                        this.entityRenderer.deactivateShader();
                    }

                    if (this.currentScreen != null) {
                        this.currentScreen.handleKeyboardInput();
                    } else {
                        if (Keyboard.getEventKey() == 1) {
                            this.displayInGameMenu();
                        }

                        if (Keyboard.getEventKey() == 31 && Keyboard.isKeyDown(61)) {
                            this.refreshResources();
                        }

                        if (Keyboard.getEventKey() == 20 && Keyboard.isKeyDown(61)) {
                            this.refreshResources();
                        }

                        if (Keyboard.getEventKey() == 33 && Keyboard.isKeyDown(61)) {
                            flag = Keyboard.isKeyDown(42) | Keyboard.isKeyDown(54);
                            this.gameSettings.setOptionValue(Options.RENDER_DISTANCE, flag ? -1 : 1);
                        }

                        if (Keyboard.getEventKey() == 30 && Keyboard.isKeyDown(61)) {
                            this.renderGlobal.loadRenderers();
                            this.handler$zen000$hodgepodge$printDebugChatMsgChunkReload((CallbackInfo)null);
                        }

                        if (Keyboard.getEventKey() == 35 && Keyboard.isKeyDown(61)) {
                            this.gameSettings.advancedItemTooltips = !this.gameSettings.advancedItemTooltips;
                            this.handler$zen000$hodgepodge$printDebugChatMsgTooltips((CallbackInfo)null);
                            this.gameSettings.saveOptions();
                        }

                        if (Keyboard.getEventKey() == 48 && Keyboard.isKeyDown(61)) {
                            RenderManager.debugBoundingBox = !RenderManager.debugBoundingBox;
                            this.handler$zen000$hodgepodge$printDebugChatMsgHitbox((CallbackInfo)null);
                        }

                        if (Keyboard.getEventKey() == 25 && Keyboard.isKeyDown(61)) {
                            this.gameSettings.pauseOnLostFocus = !this.gameSettings.pauseOnLostFocus;
                            this.handler$zen000$hodgepodge$printDebugChatMsgPauseLostFocus((CallbackInfo)null);
                            this.gameSettings.saveOptions();
                        }

                        if (Keyboard.getEventKey() == 59) {
                            this.gameSettings.hideGUI = !this.gameSettings.hideGUI;
                        }

                        if (Keyboard.getEventKey() == 61) {
                            this.gameSettings.showDebugInfo = !this.gameSettings.showDebugInfo;
                            this.gameSettings.showDebugProfilerChart = GuiScreen.isShiftKeyDown();
                        }

                        if (this.gameSettings.keyBindTogglePerspective.isPressed()) {
                            ++this.gameSettings.thirdPersonView;
                            if (this.gameSettings.thirdPersonView > 2) {
                                this.gameSettings.thirdPersonView = 0;
                            }
                        }

                        if (this.gameSettings.keyBindSmoothCamera.isPressed()) {
                            this.gameSettings.smoothCamera = !this.gameSettings.smoothCamera;
                        }
                    }

                    if (this.gameSettings.showDebugInfo && this.gameSettings.showDebugProfilerChart) {
                        if (Keyboard.getEventKey() == 11) {
                            this.updateDebugProfilerName(0);
                        }

                        for(j = 0; j < 9; ++j) {
                            if (Keyboard.getEventKey() == 2 + j) {
                                this.updateDebugProfilerName(j + 1);
                            }
                        }
                    }
                }
            }

            for(j = 0; j < 9; ++j) {
                if (this.gameSettings.keyBindsHotbar[j].isPressed()) {
                    this.thePlayer.inventory.currentItem = j;
                }
            }

            flag = this.gameSettings.chatVisibility != EnumChatVisibility.HIDDEN;

            while(this.gameSettings.keyBindInventory.isPressed()) {
                if (this.playerController.func_110738_j()) {
                    this.thePlayer.func_110322_i();
                } else {
                    this.getNetHandler().addToSendQueue(new C16PacketClientStatus(EnumState.OPEN_INVENTORY_ACHIEVEMENT));
                    this.displayGuiScreen(new GuiInventory(this.thePlayer));
                }
            }

            while(this.gameSettings.keyBindDrop.isPressed()) {
                this.thePlayer.dropOneItem(GuiScreen.isCtrlKeyDown());
            }

            while(this.gameSettings.keyBindChat.isPressed() && flag) {
                this.displayGuiScreen(new GuiChat());
            }

            if (this.currentScreen == null && this.gameSettings.keyBindCommand.isPressed() && flag) {
                this.displayGuiScreen(new GuiChat("/"));
            }

            if (this.thePlayer.isUsingItem()) {
                if (!this.gameSettings.keyBindUseItem.getIsKeyPressed()) {
                    this.playerController.onStoppedUsingItem(this.thePlayer);
                }

                while(this.gameSettings.keyBindAttack.isPressed()) {
                }

                while(true) {
                    if (!this.gameSettings.keyBindUseItem.isPressed()) {
                        while(this.gameSettings.keyBindPickBlock.isPressed()) {
                        }
                        break;
                    }
                }
            } else {
                while(this.gameSettings.keyBindAttack.isPressed()) {
                    this.func_147116_af();
                }

                while(this.gameSettings.keyBindUseItem.isPressed()) {
                    this.func_147121_ag();
                }

                while(this.gameSettings.keyBindPickBlock.isPressed()) {
                    this.func_147112_ai();
                }
            }

            if (this.gameSettings.keyBindUseItem.getIsKeyPressed() && this.rightClickDelayTimer == 0 && !this.thePlayer.isUsingItem()) {
                this.func_147121_ag();
            }

            this.func_147115_a(this.currentScreen == null && this.gameSettings.keyBindAttack.getIsKeyPressed() && this.inGameHasFocus);
        }

        if (this.theWorld != null) {
            if (this.thePlayer != null) {
                ++this.joinPlayerCounter;
                if (this.joinPlayerCounter == 30) {
                    this.joinPlayerCounter = 0;
                    this.theWorld.joinEntityInSurroundings(this.thePlayer);
                }
            }

            this.mcProfiler.endStartSection("gameRenderer");
            if (!this.isGamePaused) {
                this.entityRenderer.updateRenderer();
            }

            this.mcProfiler.endStartSection("levelRenderer");
            if (!this.isGamePaused) {
                this.renderGlobal.updateClouds();
            }

            this.mcProfiler.endStartSection("level");
            if (!this.isGamePaused) {
                if (this.theWorld.lastLightningBolt > 0) {
                    --this.theWorld.lastLightningBolt;
                }

                this.theWorld.updateEntities();
            }
        }

        if (!this.isGamePaused) {
            this.mcMusicTicker.update();
            this.mcSoundHandler.update();
        }

        if (this.theWorld != null) {
            if (!this.isGamePaused) {
                this.theWorld.setAllowedSpawnTypes(this.theWorld.difficultySetting != EnumDifficulty.PEACEFUL, true);

                try {
                    this.theWorld.tick();
                } catch (Throwable var12) {
                    crashreport = CrashReport.makeCrashReport(var12, "Exception in world tick");
                    if (this.theWorld == null) {
                        crashreportcategory = crashreport.makeCategory("Affected level");
                        crashreportcategory.addCrashSection("Problem", "Level is null!");
                    } else {
                        this.theWorld.addWorldInfoToCrashReport(crashreport);
                    }

                    throw new ReportedException(crashreport);
                }
            }

            this.mcProfiler.endStartSection("animateTick");
            if (!this.isGamePaused && this.theWorld != null) {
                this.theWorld.doVoidFogParticles(MathHelper.floor_double(this.thePlayer.posX), MathHelper.floor_double(this.thePlayer.posY), MathHelper.floor_double(this.thePlayer.posZ));
            }

            this.mcProfiler.endStartSection("particles");
            if (!this.isGamePaused) {
                this.effectRenderer.updateEffects();
            }
        } else if (this.myNetworkManager != null) {
            this.mcProfiler.endStartSection("pendingConnection");
            this.myNetworkManager.processReceivedPackets();
        }

        FMLCommonHandler.instance().onPostClientTick();
        this.mcProfiler.endSection();
        this.systemTime = getSystemTime();
    }

    public void launchIntegratedServer(String folderName, String worldName, WorldSettings worldSettingsIn) {
        FMLClientHandler.instance().startIntegratedServer(folderName, worldName, worldSettingsIn);
        this.loadWorld((WorldClient)null);
        System.gc();
        ISaveHandler isavehandler = this.saveLoader.getSaveLoader(folderName, false);
        WorldInfo worldinfo = isavehandler.loadWorldInfo();
        if (worldinfo == null && worldSettingsIn != null) {
            worldinfo = new WorldInfo(worldSettingsIn, folderName);
            isavehandler.saveWorldInfo(worldinfo);
        }

        if (worldSettingsIn == null) {
            worldSettingsIn = new WorldSettings(worldinfo);
        }

        try {
            this.theIntegratedServer = new IntegratedServer(this, folderName, worldName, worldSettingsIn);
            this.theIntegratedServer.startServerThread();
            this.integratedServerIsRunning = true;
        } catch (Throwable var10) {
            CrashReport crashreport = CrashReport.makeCrashReport(var10, "Starting integrated server");
            CrashReportCategory crashreportcategory = crashreport.makeCategory("Starting integrated server");
            crashreportcategory.addCrashSection("Level ID", folderName);
            crashreportcategory.addCrashSection("Level Name", worldName);
            throw new ReportedException(crashreport);
        }

        this.loadingScreen.displayProgressMessage(I18n.format("menu.loadingLevel", new Object[0]));

        while(!this.theIntegratedServer.serverIsInRunLoop()) {
            if (!StartupQuery.check()) {
                this.loadWorld((WorldClient)null);
                this.displayGuiScreen((GuiScreen)null);
                return;
            }

            String s2 = this.theIntegratedServer.getUserMessage();
            if (s2 != null) {
                this.loadingScreen.resetProgresAndWorkingMessage(I18n.format(s2, new Object[0]));
            } else {
                this.loadingScreen.resetProgresAndWorkingMessage("");
            }

            try {
                Thread.sleep(200L);
            } catch (InterruptedException var9) {
            }
        }

        this.displayGuiScreen((GuiScreen)null);
        SocketAddress socketaddress = this.theIntegratedServer.func_147137_ag().addLocalEndpoint();
        NetworkManager networkmanager = NetworkManager.provideLocalClient(socketaddress);
        networkmanager.setNetHandler(new NetHandlerLoginClient(networkmanager, this, (GuiScreen)null));
        networkmanager.scheduleOutboundPacket(new C00Handshake(5, socketaddress.toString(), 0, EnumConnectionState.LOGIN), new GenericFutureListener[0]);
        networkmanager.scheduleOutboundPacket(new C00PacketLoginStart(this.getSession().func_148256_e()), new GenericFutureListener[0]);
        this.myNetworkManager = networkmanager;
    }

    public void loadWorld(WorldClient worldClientIn) {
        this.loadWorld(worldClientIn, "");
    }

    public void loadWorld(WorldClient worldClientIn, String loadingMessage) {
        if (this.theWorld != null) {
            MinecraftForge.EVENT_BUS.post(new Unload(this.theWorld));
        }

        if (worldClientIn == null) {
            NetHandlerPlayClient nethandlerplayclient = this.getNetHandler();
            if (nethandlerplayclient != null) {
                nethandlerplayclient.cleanup();
            }

            if (this.theIntegratedServer != null) {
                this.theIntegratedServer.initiateShutdown();
                if (this.loadingScreen != null) {
                    this.loadingScreen.resetProgresAndWorkingMessage(I18n.format("forge.client.shutdown.internal", new Object[0]));
                }

                while(!this.theIntegratedServer.isServerStopped()) {
                    try {
                        Thread.sleep(10L);
                    } catch (InterruptedException var5) {
                    }
                }
            }

            this.theIntegratedServer = null;
            this.guiAchievement.func_146257_b();
            this.entityRenderer.getMapItemRenderer().func_148249_a();
        }

        this.renderViewEntity = null;
        this.myNetworkManager = null;
        if (this.loadingScreen != null) {
            this.loadingScreen.resetProgressAndMessage(loadingMessage);
            this.loadingScreen.resetProgresAndWorkingMessage("");
        }

        if (worldClientIn == null && this.theWorld != null) {
            if (this.mcResourcePackRepository.func_148530_e() != null) {
                this.scheduleResourcesRefresh();
            }

            this.mcResourcePackRepository.func_148529_f();
            this.setServerData((ServerData)null);
            this.integratedServerIsRunning = false;
            FMLClientHandler.instance().handleClientWorldClosing(this.theWorld);
        }

        this.mcSoundHandler.stopSounds();
        this.theWorld = worldClientIn;
        this.handler$zel000$hodgepodge$fixRenderersWorldLeak(worldClientIn, loadingMessage, (CallbackInfo)null);
        if (worldClientIn != null) {
            if (this.renderGlobal != null) {
                this.renderGlobal.setWorldAndLoadRenderers(worldClientIn);
            }

            if (this.effectRenderer != null) {
                this.effectRenderer.clearEffects(worldClientIn);
            }

            if (this.thePlayer == null) {
                this.thePlayer = this.playerController.func_147493_a(worldClientIn, new StatFileWriter());
                this.playerController.flipPlayer(this.thePlayer);
            }

            this.thePlayer.preparePlayerToSpawn();
            worldClientIn.spawnEntityInWorld(this.thePlayer);
            this.thePlayer.movementInput = new MovementInputFromOptions(this.gameSettings);
            this.playerController.setPlayerCapabilities(this.thePlayer);
            this.renderViewEntity = this.thePlayer;
        } else {
            this.saveLoader.flushCache();
            this.thePlayer = null;
        }

        System.gc();
        this.systemTime = 0L;
    }

    public String debugInfoRenders() {
        return this.renderGlobal.getDebugInfoRenders();
    }

    public String getEntityDebug() {
        return this.renderGlobal.getDebugInfoEntities();
    }

    public String getWorldProviderName() {
        return this.theWorld.getProviderName();
    }

    public String debugInfoEntities() {
        return "P: " + this.effectRenderer.getStatistics() + ". T: " + this.theWorld.getDebugLoadedEntities();
    }

    public void setDimensionAndSpawnPlayer(int dimension) {
        this.theWorld.setSpawnLocation();
        this.theWorld.removeAllEntities();
        int j = 0;
        String s = null;
        if (this.thePlayer != null) {
            j = this.thePlayer.getEntityId();
            this.theWorld.removeEntity(this.thePlayer);
            s = this.thePlayer.func_142021_k();
        }

        this.renderViewEntity = null;
        this.thePlayer = this.playerController.func_147493_a(this.theWorld, this.thePlayer == null ? new StatFileWriter() : this.thePlayer.getStatFileWriter());
        this.thePlayer.dimension = dimension;
        this.renderViewEntity = this.thePlayer;
        this.thePlayer.preparePlayerToSpawn();
        this.thePlayer.func_142020_c(s);
        this.theWorld.spawnEntityInWorld(this.thePlayer);
        this.playerController.flipPlayer(this.thePlayer);
        this.thePlayer.movementInput = new MovementInputFromOptions(this.gameSettings);
        this.thePlayer.setEntityId(j);
        this.playerController.setPlayerCapabilities(this.thePlayer);
        if (this.currentScreen instanceof GuiGameOver) {
            this.displayGuiScreen((GuiScreen)null);
        }

    }

    public final boolean isDemo() {
        return this.isDemo;
    }

    public NetHandlerPlayClient getNetHandler() {
        return this.thePlayer != null ? this.thePlayer.sendQueue : null;
    }

    public static boolean isGuiEnabled() {
        return theMinecraft == null || !theMinecraft.gameSettings.hideGUI;
    }

    public static boolean isFancyGraphicsEnabled() {
        return theMinecraft != null && theMinecraft.gameSettings.fancyGraphics;
    }

    public static boolean isAmbientOcclusionEnabled() {
        return theMinecraft != null && theMinecraft.gameSettings.ambientOcclusion != 0;
    }

    private void func_147112_ai() {
        if (this.objectMouseOver != null) {
            boolean flag = this.thePlayer.capabilities.isCreativeMode;
            if (!ForgeHooks.onPickBlock(this.objectMouseOver, this.thePlayer, this.theWorld)) {
                return;
            }

            if (flag) {
                int j = this.thePlayer.inventoryContainer.inventorySlots.size() - 9 + this.thePlayer.inventory.currentItem;
                this.playerController.sendSlotPacket(this.thePlayer.inventory.getStackInSlot(this.thePlayer.inventory.currentItem), j);
            }
        }

    }

    public CrashReport addGraphicsAndWorldToCrashReport(CrashReport theCrash) {
        theCrash.getCategory().addCrashSectionCallable("Launched Version", new 5(this));
        theCrash.getCategory().addCrashSectionCallable("LWJGL", new 6(this));
        theCrash.getCategory().addCrashSectionCallable("OpenGL", new 7(this));
        theCrash.getCategory().addCrashSectionCallable("GL Caps", new 8(this));
        theCrash.getCategory().addCrashSectionCallable("Is Modded", new 9(this));
        theCrash.getCategory().addCrashSectionCallable("Type", new 10(this));
        theCrash.getCategory().addCrashSectionCallable("Resource Packs", new 11(this));
        theCrash.getCategory().addCrashSectionCallable("Current Language", new 12(this));
        theCrash.getCategory().addCrashSectionCallable("Profiler Position", new 13(this));
        theCrash.getCategory().addCrashSectionCallable("Vec3 Pool Size", new 14(this));
        theCrash.getCategory().addCrashSectionCallable("Anisotropic Filtering", new 15(this));
        if (this.theWorld != null) {
            this.theWorld.addWorldInfoToCrashReport(theCrash);
        }

        return theCrash;
    }

    public static Minecraft getMinecraft() {
        return theMinecraft;
    }

    public void scheduleResourcesRefresh() {
        this.refreshTexturePacksScheduled = true;
    }

    public void addServerStatsToSnooper(PlayerUsageSnooper playerSnooper) {
        playerSnooper.func_152768_a("fps", debugFPS);
        playerSnooper.func_152768_a("vsync_enabled", this.gameSettings.enableVsync);
        playerSnooper.func_152768_a("display_frequency", Display.getDisplayMode().getFrequency());
        playerSnooper.func_152768_a("display_type", this.fullscreen ? "fullscreen" : "windowed");
        playerSnooper.func_152768_a("run_time", (MinecraftServer.getSystemTimeMillis() - playerSnooper.getMinecraftStartTimeMillis()) / 60L * 1000L);
        playerSnooper.func_152768_a("resource_packs", this.mcResourcePackRepository.getRepositoryEntries().size());
        int i = 0;
        Iterator iterator = this.mcResourcePackRepository.getRepositoryEntries().iterator();

        while(iterator.hasNext()) {
            Entry entry = (Entry)iterator.next();
            playerSnooper.func_152768_a("resource_pack[" + i++ + "]", entry.getResourcePackName());
        }

        if (this.theIntegratedServer != null && this.theIntegratedServer.getPlayerUsageSnooper() != null) {
            playerSnooper.func_152768_a("snooper_partner", this.theIntegratedServer.getPlayerUsageSnooper().getUniqueID());
        }

    }

    public void addServerTypeToSnooper(PlayerUsageSnooper playerSnooper) {
        playerSnooper.func_152767_b("opengl_version", GL11.glGetString(7938));
        playerSnooper.func_152767_b("opengl_vendor", GL11.glGetString(7936));
        playerSnooper.func_152767_b("client_brand", ClientBrandRetriever.getClientModName());
        playerSnooper.func_152767_b("launched_version", this.launchedVersion);
        ContextCapabilities contextcapabilities = GLContext.getCapabilities();
        playerSnooper.func_152767_b("gl_caps[ARB_arrays_of_arrays]", contextcapabilities.GL_ARB_arrays_of_arrays);
        playerSnooper.func_152767_b("gl_caps[ARB_base_instance]", contextcapabilities.GL_ARB_base_instance);
        playerSnooper.func_152767_b("gl_caps[ARB_blend_func_extended]", contextcapabilities.GL_ARB_blend_func_extended);
        playerSnooper.func_152767_b("gl_caps[ARB_clear_buffer_object]", contextcapabilities.GL_ARB_clear_buffer_object);
        playerSnooper.func_152767_b("gl_caps[ARB_color_buffer_float]", contextcapabilities.GL_ARB_color_buffer_float);
        playerSnooper.func_152767_b("gl_caps[ARB_compatibility]", contextcapabilities.GL_ARB_compatibility);
        playerSnooper.func_152767_b("gl_caps[ARB_compressed_texture_pixel_storage]", contextcapabilities.GL_ARB_compressed_texture_pixel_storage);
        playerSnooper.func_152767_b("gl_caps[ARB_compute_shader]", contextcapabilities.GL_ARB_compute_shader);
        playerSnooper.func_152767_b("gl_caps[ARB_copy_buffer]", contextcapabilities.GL_ARB_copy_buffer);
        playerSnooper.func_152767_b("gl_caps[ARB_copy_image]", contextcapabilities.GL_ARB_copy_image);
        playerSnooper.func_152767_b("gl_caps[ARB_depth_buffer_float]", contextcapabilities.GL_ARB_depth_buffer_float);
        playerSnooper.func_152767_b("gl_caps[ARB_compute_shader]", contextcapabilities.GL_ARB_compute_shader);
        playerSnooper.func_152767_b("gl_caps[ARB_copy_buffer]", contextcapabilities.GL_ARB_copy_buffer);
        playerSnooper.func_152767_b("gl_caps[ARB_copy_image]", contextcapabilities.GL_ARB_copy_image);
        playerSnooper.func_152767_b("gl_caps[ARB_depth_buffer_float]", contextcapabilities.GL_ARB_depth_buffer_float);
        playerSnooper.func_152767_b("gl_caps[ARB_depth_clamp]", contextcapabilities.GL_ARB_depth_clamp);
        playerSnooper.func_152767_b("gl_caps[ARB_depth_texture]", contextcapabilities.GL_ARB_depth_texture);
        playerSnooper.func_152767_b("gl_caps[ARB_draw_buffers]", contextcapabilities.GL_ARB_draw_buffers);
        playerSnooper.func_152767_b("gl_caps[ARB_draw_buffers_blend]", contextcapabilities.GL_ARB_draw_buffers_blend);
        playerSnooper.func_152767_b("gl_caps[ARB_draw_elements_base_vertex]", contextcapabilities.GL_ARB_draw_elements_base_vertex);
        playerSnooper.func_152767_b("gl_caps[ARB_draw_indirect]", contextcapabilities.GL_ARB_draw_indirect);
        playerSnooper.func_152767_b("gl_caps[ARB_draw_instanced]", contextcapabilities.GL_ARB_draw_instanced);
        playerSnooper.func_152767_b("gl_caps[ARB_explicit_attrib_location]", contextcapabilities.GL_ARB_explicit_attrib_location);
        playerSnooper.func_152767_b("gl_caps[ARB_explicit_uniform_location]", contextcapabilities.GL_ARB_explicit_uniform_location);
        playerSnooper.func_152767_b("gl_caps[ARB_fragment_layer_viewport]", contextcapabilities.GL_ARB_fragment_layer_viewport);
        playerSnooper.func_152767_b("gl_caps[ARB_fragment_program]", contextcapabilities.GL_ARB_fragment_program);
        playerSnooper.func_152767_b("gl_caps[ARB_fragment_shader]", contextcapabilities.GL_ARB_fragment_shader);
        playerSnooper.func_152767_b("gl_caps[ARB_fragment_program_shadow]", contextcapabilities.GL_ARB_fragment_program_shadow);
        playerSnooper.func_152767_b("gl_caps[ARB_framebuffer_object]", contextcapabilities.GL_ARB_framebuffer_object);
        playerSnooper.func_152767_b("gl_caps[ARB_framebuffer_sRGB]", contextcapabilities.GL_ARB_framebuffer_sRGB);
        playerSnooper.func_152767_b("gl_caps[ARB_geometry_shader4]", contextcapabilities.GL_ARB_geometry_shader4);
        playerSnooper.func_152767_b("gl_caps[ARB_gpu_shader5]", contextcapabilities.GL_ARB_gpu_shader5);
        playerSnooper.func_152767_b("gl_caps[ARB_half_float_pixel]", contextcapabilities.GL_ARB_half_float_pixel);
        playerSnooper.func_152767_b("gl_caps[ARB_half_float_vertex]", contextcapabilities.GL_ARB_half_float_vertex);
        playerSnooper.func_152767_b("gl_caps[ARB_instanced_arrays]", contextcapabilities.GL_ARB_instanced_arrays);
        playerSnooper.func_152767_b("gl_caps[ARB_map_buffer_alignment]", contextcapabilities.GL_ARB_map_buffer_alignment);
        playerSnooper.func_152767_b("gl_caps[ARB_map_buffer_range]", contextcapabilities.GL_ARB_map_buffer_range);
        playerSnooper.func_152767_b("gl_caps[ARB_multisample]", contextcapabilities.GL_ARB_multisample);
        playerSnooper.func_152767_b("gl_caps[ARB_multitexture]", contextcapabilities.GL_ARB_multitexture);
        playerSnooper.func_152767_b("gl_caps[ARB_occlusion_query2]", contextcapabilities.GL_ARB_occlusion_query2);
        playerSnooper.func_152767_b("gl_caps[ARB_pixel_buffer_object]", contextcapabilities.GL_ARB_pixel_buffer_object);
        playerSnooper.func_152767_b("gl_caps[ARB_seamless_cube_map]", contextcapabilities.GL_ARB_seamless_cube_map);
        playerSnooper.func_152767_b("gl_caps[ARB_shader_objects]", contextcapabilities.GL_ARB_shader_objects);
        playerSnooper.func_152767_b("gl_caps[ARB_shader_stencil_export]", contextcapabilities.GL_ARB_shader_stencil_export);
        playerSnooper.func_152767_b("gl_caps[ARB_shader_texture_lod]", contextcapabilities.GL_ARB_shader_texture_lod);
        playerSnooper.func_152767_b("gl_caps[ARB_shadow]", contextcapabilities.GL_ARB_shadow);
        playerSnooper.func_152767_b("gl_caps[ARB_shadow_ambient]", contextcapabilities.GL_ARB_shadow_ambient);
        playerSnooper.func_152767_b("gl_caps[ARB_stencil_texturing]", contextcapabilities.GL_ARB_stencil_texturing);
        playerSnooper.func_152767_b("gl_caps[ARB_sync]", contextcapabilities.GL_ARB_sync);
        playerSnooper.func_152767_b("gl_caps[ARB_tessellation_shader]", contextcapabilities.GL_ARB_tessellation_shader);
        playerSnooper.func_152767_b("gl_caps[ARB_texture_border_clamp]", contextcapabilities.GL_ARB_texture_border_clamp);
        playerSnooper.func_152767_b("gl_caps[ARB_texture_buffer_object]", contextcapabilities.GL_ARB_texture_buffer_object);
        playerSnooper.func_152767_b("gl_caps[ARB_texture_cube_map]", contextcapabilities.GL_ARB_texture_cube_map);
        playerSnooper.func_152767_b("gl_caps[ARB_texture_cube_map_array]", contextcapabilities.GL_ARB_texture_cube_map_array);
        playerSnooper.func_152767_b("gl_caps[ARB_texture_non_power_of_two]", contextcapabilities.GL_ARB_texture_non_power_of_two);
        playerSnooper.func_152767_b("gl_caps[ARB_uniform_buffer_object]", contextcapabilities.GL_ARB_uniform_buffer_object);
        playerSnooper.func_152767_b("gl_caps[ARB_vertex_blend]", contextcapabilities.GL_ARB_vertex_blend);
        playerSnooper.func_152767_b("gl_caps[ARB_vertex_buffer_object]", contextcapabilities.GL_ARB_vertex_buffer_object);
        playerSnooper.func_152767_b("gl_caps[ARB_vertex_program]", contextcapabilities.GL_ARB_vertex_program);
        playerSnooper.func_152767_b("gl_caps[ARB_vertex_shader]", contextcapabilities.GL_ARB_vertex_shader);
        playerSnooper.func_152767_b("gl_caps[EXT_bindable_uniform]", contextcapabilities.GL_EXT_bindable_uniform);
        playerSnooper.func_152767_b("gl_caps[EXT_blend_equation_separate]", contextcapabilities.GL_EXT_blend_equation_separate);
        playerSnooper.func_152767_b("gl_caps[EXT_blend_func_separate]", contextcapabilities.GL_EXT_blend_func_separate);
        playerSnooper.func_152767_b("gl_caps[EXT_blend_minmax]", contextcapabilities.GL_EXT_blend_minmax);
        playerSnooper.func_152767_b("gl_caps[EXT_blend_subtract]", contextcapabilities.GL_EXT_blend_subtract);
        playerSnooper.func_152767_b("gl_caps[EXT_draw_instanced]", contextcapabilities.GL_EXT_draw_instanced);
        playerSnooper.func_152767_b("gl_caps[EXT_framebuffer_multisample]", contextcapabilities.GL_EXT_framebuffer_multisample);
        playerSnooper.func_152767_b("gl_caps[EXT_framebuffer_object]", contextcapabilities.GL_EXT_framebuffer_object);
        playerSnooper.func_152767_b("gl_caps[EXT_framebuffer_sRGB]", contextcapabilities.GL_EXT_framebuffer_sRGB);
        playerSnooper.func_152767_b("gl_caps[EXT_geometry_shader4]", contextcapabilities.GL_EXT_geometry_shader4);
        playerSnooper.func_152767_b("gl_caps[EXT_gpu_program_parameters]", contextcapabilities.GL_EXT_gpu_program_parameters);
        playerSnooper.func_152767_b("gl_caps[EXT_gpu_shader4]", contextcapabilities.GL_EXT_gpu_shader4);
        playerSnooper.func_152767_b("gl_caps[EXT_multi_draw_arrays]", contextcapabilities.GL_EXT_multi_draw_arrays);
        playerSnooper.func_152767_b("gl_caps[EXT_packed_depth_stencil]", contextcapabilities.GL_EXT_packed_depth_stencil);
        playerSnooper.func_152767_b("gl_caps[EXT_paletted_texture]", contextcapabilities.GL_EXT_paletted_texture);
        playerSnooper.func_152767_b("gl_caps[EXT_rescale_normal]", contextcapabilities.GL_EXT_rescale_normal);
        playerSnooper.func_152767_b("gl_caps[EXT_separate_shader_objects]", contextcapabilities.GL_EXT_separate_shader_objects);
        playerSnooper.func_152767_b("gl_caps[EXT_shader_image_load_store]", contextcapabilities.GL_EXT_shader_image_load_store);
        playerSnooper.func_152767_b("gl_caps[EXT_shadow_funcs]", contextcapabilities.GL_EXT_shadow_funcs);
        playerSnooper.func_152767_b("gl_caps[EXT_shared_texture_palette]", contextcapabilities.GL_EXT_shared_texture_palette);
        playerSnooper.func_152767_b("gl_caps[EXT_stencil_clear_tag]", contextcapabilities.GL_EXT_stencil_clear_tag);
        playerSnooper.func_152767_b("gl_caps[EXT_stencil_two_side]", contextcapabilities.GL_EXT_stencil_two_side);
        playerSnooper.func_152767_b("gl_caps[EXT_stencil_wrap]", contextcapabilities.GL_EXT_stencil_wrap);
        playerSnooper.func_152767_b("gl_caps[EXT_texture_3d]", contextcapabilities.GL_EXT_texture_3d);
        playerSnooper.func_152767_b("gl_caps[EXT_texture_array]", contextcapabilities.GL_EXT_texture_array);
        playerSnooper.func_152767_b("gl_caps[EXT_texture_buffer_object]", contextcapabilities.GL_EXT_texture_buffer_object);
        playerSnooper.func_152767_b("gl_caps[EXT_texture_filter_anisotropic]", contextcapabilities.GL_EXT_texture_filter_anisotropic);
        playerSnooper.func_152767_b("gl_caps[EXT_texture_integer]", contextcapabilities.GL_EXT_texture_integer);
        playerSnooper.func_152767_b("gl_caps[EXT_texture_lod_bias]", contextcapabilities.GL_EXT_texture_lod_bias);
        playerSnooper.func_152767_b("gl_caps[EXT_texture_sRGB]", contextcapabilities.GL_EXT_texture_sRGB);
        playerSnooper.func_152767_b("gl_caps[EXT_vertex_shader]", contextcapabilities.GL_EXT_vertex_shader);
        playerSnooper.func_152767_b("gl_caps[EXT_vertex_weighting]", contextcapabilities.GL_EXT_vertex_weighting);
        playerSnooper.func_152767_b("gl_caps[gl_max_vertex_uniforms]", GL11.glGetInteger(35658));
        GL11.glGetError();
        playerSnooper.func_152767_b("gl_caps[gl_max_fragment_uniforms]", GL11.glGetInteger(35657));
        GL11.glGetError();
        playerSnooper.func_152767_b("gl_caps[gl_max_vertex_attribs]", GL11.glGetInteger(34921));
        GL11.glGetError();
        playerSnooper.func_152767_b("gl_caps[gl_max_vertex_texture_image_units]", GL11.glGetInteger(35660));
        GL11.glGetError();
        playerSnooper.func_152767_b("gl_caps[gl_max_texture_image_units]", GL11.glGetInteger(34930));
        GL11.glGetError();
        playerSnooper.func_152767_b("gl_caps[gl_max_texture_image_units]", GL11.glGetInteger(35071));
        GL11.glGetError();
        playerSnooper.func_152767_b("gl_max_texture_size", getGLMaximumTextureSize());
    }

    public static int getGLMaximumTextureSize() {
        if (max_texture_size != -1) {
            return max_texture_size;
        } else {
            for(int i = 16384; i > 0; i >>= 1) {
                GL11.glTexImage2D(32868, 0, 6408, i, i, 0, 6408, 5121, (ByteBuffer)null);
                int j = GL11.glGetTexLevelParameteri(32868, 0, 4096);
                if (j != 0) {
                    max_texture_size = i;
                    return i;
                }
            }

            return -1;
        }
    }

    public boolean isSnooperEnabled() {
        return this.gameSettings.snooperEnabled;
    }

    public void setServerData(ServerData serverDataIn) {
        this.currentServerData = serverDataIn;
    }

    public ServerData func_147104_D() {
        return this.currentServerData;
    }

    public boolean isIntegratedServerRunning() {
        return this.integratedServerIsRunning;
    }

    public boolean isSingleplayer() {
        return this.integratedServerIsRunning && this.theIntegratedServer != null;
    }

    public IntegratedServer getIntegratedServer() {
        return this.theIntegratedServer;
    }

    public static void stopIntegratedServer() {
        if (theMinecraft != null) {
            IntegratedServer integratedserver = theMinecraft.getIntegratedServer();
            if (integratedserver != null) {
                integratedserver.stopServer();
            }
        }

    }

    public PlayerUsageSnooper getPlayerUsageSnooper() {
        return this.usageSnooper;
    }

    public static long getSystemTime() {
        return Sys.getTime() * 1000L / Sys.getTimerResolution();
    }

    public boolean isFullScreen() {
        return this.fullscreen;
    }

    public Session getSession() {
        return this.session;
    }

    public Multimap func_152341_N() {
        return this.field_152356_J;
    }

    public Proxy getProxy() {
        return this.proxy;
    }

    public TextureManager getTextureManager() {
        return this.renderEngine;
    }

    public IResourceManager getResourceManager() {
        return this.mcResourceManager;
    }

    public ResourcePackRepository getResourcePackRepository() {
        return this.mcResourcePackRepository;
    }

    public LanguageManager getLanguageManager() {
        return this.mcLanguageManager;
    }

    public TextureMap getTextureMapBlocks() {
        return this.textureMapBlocks;
    }

    public boolean isJava64bit() {
        return this.jvm64bit;
    }

    public boolean isGamePaused() {
        return this.isGamePaused;
    }

    public SoundHandler getSoundHandler() {
        return this.mcSoundHandler;
    }

    public MusicType func_147109_W() {
        return this.currentScreen instanceof GuiWinGame ? MusicType.CREDITS : (this.thePlayer != null ? (this.thePlayer.worldObj.provider instanceof WorldProviderHell ? MusicType.NETHER : (this.thePlayer.worldObj.provider instanceof WorldProviderEnd ? (BossStatus.bossName != null && BossStatus.statusBarTime > 0 ? MusicType.END_BOSS : MusicType.END) : (this.thePlayer.capabilities.isCreativeMode && this.thePlayer.capabilities.allowFlying ? MusicType.CREATIVE : MusicType.GAME))) : MusicType.MENU);
    }

    public IStream func_152346_Z() {
        return this.field_152353_at;
    }

    public void func_152348_aa() {
        int i = Keyboard.getEventKey();
        if (i != 0 && !Keyboard.isRepeatEvent() && (!(this.currentScreen instanceof GuiControls) || ((GuiControls)this.currentScreen).field_152177_g <= getSystemTime() - 20L)) {
            if (Keyboard.getEventKeyState()) {
                if (i == this.gameSettings.field_152396_an.getKeyCode()) {
                    if (this.func_152346_Z().func_152934_n()) {
                        this.func_152346_Z().func_152914_u();
                    } else if (this.func_152346_Z().func_152924_m()) {
                        this.displayGuiScreen(new GuiYesNo(new 16(this), I18n.format("stream.confirm_start", new Object[0]), "", 0));
                    } else if (this.func_152346_Z().func_152928_D() && this.func_152346_Z().func_152936_l()) {
                        if (this.theWorld != null) {
                            this.ingameGUI.getChatGUI().printChatMessage(new ChatComponentText("Not ready to start streaming yet!"));
                        }
                    } else {
                        GuiStreamUnavailable.func_152321_a(this.currentScreen);
                    }
                } else if (i == this.gameSettings.field_152397_ao.getKeyCode()) {
                    if (this.func_152346_Z().func_152934_n()) {
                        if (this.func_152346_Z().func_152919_o()) {
                            this.func_152346_Z().func_152933_r();
                        } else {
                            this.func_152346_Z().func_152916_q();
                        }
                    }
                } else if (i == this.gameSettings.field_152398_ap.getKeyCode()) {
                    if (this.func_152346_Z().func_152934_n()) {
                        this.func_152346_Z().func_152931_p();
                    }
                } else if (i == this.gameSettings.field_152399_aq.getKeyCode()) {
                    this.field_152353_at.func_152910_a(true);
                } else if (i == this.gameSettings.field_152395_am.getKeyCode()) {
                    this.toggleFullscreen();
                } else if (i == this.gameSettings.keyBindScreenshot.getKeyCode()) {
                    this.ingameGUI.getChatGUI().printChatMessage(ScreenShotHelper.saveScreenshot(this.mcDataDir, this.displayWidth, this.displayHeight, this.framebufferMc));
                }
            } else if (i == this.gameSettings.field_152399_aq.getKeyCode()) {
                this.field_152353_at.func_152910_a(false);
            }
        }

    }

    public ListenableFuture func_152343_a(Callable callableToSchedule) {
        Validate.notNull(callableToSchedule);
        if (!this.func_152345_ab()) {
            ListenableFutureTask listenablefuturetask = ListenableFutureTask.create(callableToSchedule);
            Queue queue = this.field_152351_aB;
            synchronized(this.field_152351_aB) {
                this.field_152351_aB.add(listenablefuturetask);
                return listenablefuturetask;
            }
        } else {
            try {
                return Futures.immediateFuture(callableToSchedule.call());
            } catch (Exception var7) {
                return Futures.immediateFailedCheckedFuture(var7);
            }
        }
    }

    public ListenableFuture func_152344_a(Runnable runnableToSchedule) {
        Validate.notNull(runnableToSchedule);
        return this.func_152343_a(Executors.callable(runnableToSchedule));
    }

    public boolean func_152345_ab() {
        return Thread.currentThread() == this.field_152352_aC;
    }

    public MinecraftSessionService func_152347_ac() {
        return this.field_152355_az;
    }

    public SkinManager func_152342_ad() {
        return this.field_152350_aA;
    }

    // $FF: synthetic method
    static String access$000(Minecraft x0) {
        return x0.launchedVersion;
    }

    // $FF: synthetic method
    static LanguageManager access$100(Minecraft x0) {
        return x0.mcLanguageManager;
    }

    static {
        isRunningOnMac = Util.getOSType() == EnumOS.OSX;
        memoryReserve = new byte[10485760];
        macDisplayModes = Lists.newArrayList(new DisplayMode[]{new DisplayMode(2560, 1600), new DisplayMode(2880, 1800)});
        max_texture_size = -1;
    }

    @MixinMerged(
        mixin = "com.myname.mymodid.mixins.early.MixinMinecraft_Example",
        priority = 1000,
        sessionId = "98d213e4-85ca-4ce4-959f-84858add440a"
    )
    private void handler$zzc000$mymodid$example$sayHello(CallbackInfo ci) {
        System.out.println("Example mod says Hello from within Minecraft.startGame()!");
    }

    @MixinMerged(
        mixin = "me.eigenraven.lwjgl3ify.mixins.early.game.MixinMinecraftKeyBinding",
        priority = 1000,
        sessionId = "98d213e4-85ca-4ce4-959f-84858add440a"
    )
    private void redirect$zzg000$lwjgl3ify$noKeybindUpdateHere(int eventKey, boolean eventKeyState) {
    }

    @MixinMerged(
        mixin = "me.eigenraven.lwjgl3ify.mixins.early.game.MixinMinecraftKeyBinding",
        priority = 1000,
        sessionId = "98d213e4-85ca-4ce4-959f-84858add440a"
    )
    private void redirect$zzg000$lwjgl3ify$noKeybindTickHere(int eventKey) {
    }

    @MixinMerged(
        mixin = "me.eigenraven.lwjgl3ify.mixins.early.game.MixinBorderlessWindow",
        priority = 1000,
        sessionId = "98d213e4-85ca-4ce4-959f-84858add440a"
    )
    public void handler$zzh000$lwjgl3ify$toggleFullscreen(CallbackInfo ci) {
        if (Config.WINDOW_BORDERLESS_REPLACES_FULLSCREEN) {
            ci.cancel();
            org.lwjglx.opengl.Display.toggleBorderless();
        }

    }

    @MixinMerged(
        mixin = "com.mitchej123.hodgepodge.mixins.early.minecraft.profiler.MinecraftMixin",
        priority = 1000,
        sessionId = "98d213e4-85ca-4ce4-959f-84858add440a"
    )
    public int redirect$zdk000$hodgepodge$drawLongString(FontRenderer fontRenderer, String text, int x, int y, int color) {
        int offset = ClientTicker.INSTANCE.getDebugPieTextOffset();
        int length = text.length();
        if (length >= 42) {
            int first = offset % length;
            text = text.substring(first) + " " + text.substring(0, first);
            text = text.substring(0, 43);
        }

        return fontRenderer.drawStringWithShadow(text, x, y, color);
    }

    @MixinMerged(
        mixin = "com.mitchej123.hodgepodge.mixins.early.minecraft.MixinMinecraft_ResizableFullscreen",
        priority = 1000,
        sessionId = "98d213e4-85ca-4ce4-959f-84858add440a"
    )
    private void handler$zej000$hodgepodge$fixFullscreenResizable(CallbackInfo ci) {
        if (!this.fullscreen && Util.getOSType() == EnumOS.WINDOWS) {
            Display.setResizable(false);
            Display.setResizable(true);
        }

    }

    @MixinMerged(
        mixin = "com.mitchej123.hodgepodge.mixins.early.minecraft.MixinMinecraft_UnfocusedFullscreen",
        priority = 1000,
        sessionId = "98d213e4-85ca-4ce4-959f-84858add440a"
    )
    public boolean redirect$zek000$hodgepodge$fixUnfocusedFullscreen() {
        return true;
    }

    @MixinMerged(
        mixin = "com.mitchej123.hodgepodge.mixins.early.minecraft.MixinMinecraft_ClearRenderersWorldLeak",
        priority = 1000,
        sessionId = "98d213e4-85ca-4ce4-959f-84858add440a"
    )
    private void handler$zel000$hodgepodge$fixRenderersWorldLeak(WorldClient worldClient, String loadingMessage, CallbackInfo ci) {
        if (worldClient == null) {
            if (this.renderGlobal != null) {
                this.renderGlobal.setWorldAndLoadRenderers((WorldClient)null);
            }

            if (this.effectRenderer != null) {
                this.effectRenderer.clearEffects((World)null);
            }
        }

    }

    @MixinMerged(
        mixin = "com.mitchej123.hodgepodge.mixins.early.minecraft.MixinMinecraft_ToggleDebugMessage",
        priority = 1000,
        sessionId = "98d213e4-85ca-4ce4-959f-84858add440a"
    )
    public void handler$zen000$hodgepodge$printDebugChatMsgTooltips(CallbackInfo ci) {
        GTNHLib.proxy.addDebugToChat("Advanced Item Tooltips:" + (this.gameSettings.advancedItemTooltips ? EnumChatFormatting.GREEN + " On" : EnumChatFormatting.RED + " Off"));
    }

    @MixinMerged(
        mixin = "com.mitchej123.hodgepodge.mixins.early.minecraft.MixinMinecraft_ToggleDebugMessage",
        priority = 1000,
        sessionId = "98d213e4-85ca-4ce4-959f-84858add440a"
    )
    public void handler$zen000$hodgepodge$printDebugChatMsgHitbox(CallbackInfo ci) {
        GTNHLib.proxy.addDebugToChat("Hitboxes:" + (RenderManager.debugBoundingBox ? EnumChatFormatting.GREEN + " On" : EnumChatFormatting.RED + " Off"));
    }

    @MixinMerged(
        mixin = "com.mitchej123.hodgepodge.mixins.early.minecraft.MixinMinecraft_ToggleDebugMessage",
        priority = 1000,
        sessionId = "98d213e4-85ca-4ce4-959f-84858add440a"
    )
    public void handler$zen000$hodgepodge$printDebugChatMsgPauseLostFocus(CallbackInfo ci) {
        GTNHLib.proxy.addDebugToChat("Pause on lost focus:" + (this.gameSettings.pauseOnLostFocus ? EnumChatFormatting.GREEN + " On" : EnumChatFormatting.RED + " Off"));
    }

    @MixinMerged(
        mixin = "com.mitchej123.hodgepodge.mixins.early.minecraft.MixinMinecraft_ToggleDebugMessage",
        priority = 1000,
        sessionId = "98d213e4-85ca-4ce4-959f-84858add440a"
    )
    public void handler$zen000$hodgepodge$printDebugChatMsgChunkReload(CallbackInfo ci) {
        GTNHLib.proxy.addDebugToChat("Reloading all chunks");
    }

    @MixinMerged(
        mixin = "com.mitchej123.hodgepodge.mixins.early.minecraft.MixinMinecraft_UpdateKeys",
        priority = 1000,
        sessionId = "98d213e4-85ca-4ce4-959f-84858add440a"
    )
    private void handler$zfl000$hodgepodge$updateKeysStates(CallbackInfo ci) {
        ((KeyBindingExt)getMinecraft().gameSettings.keyBindAttack).hodgepodge$updateKeyStates();
    }

    @MixinMerged(
        mixin = "com.mitchej123.hodgepodge.mixins.early.minecraft.MixinMinecraft_FastBlockPlacing",
        priority = 1000,
        sessionId = "98d213e4-85ca-4ce4-959f-84858add440a"
    )
    private void handler$zgn000$hodgepodge$func_147121_ag(CallbackInfo ci) {
        if (TweaksConfig.fastBlockPlacingServerSide) {
            if (TweaksConfig.fastBlockPlacing) {
                if (this.thePlayer != null && !this.thePlayer.isUsingItem()) {
                    if (this.objectMouseOver != null) {
                        if (this.objectMouseOver.typeOfHit == MovingObjectType.BLOCK) {
                            ItemStack itemstack = this.thePlayer.inventory.getCurrentItem();
                            if (itemstack != null) {
                                if (itemstack.getItem() instanceof ItemBlock) {
                                    this.currentPosition.set(this.objectMouseOver.blockX, this.objectMouseOver.blockY, this.objectMouseOver.blockZ);
                                    if (this.rightClickDelayTimer > 0 && !this.currentPosition.equals(this.lastPosition) && !this.currentPosition.equals(this.comparePosition.set(this.lastPosition).add(this.lastSide.offsetX, this.lastSide.offsetY, this.lastSide.offsetZ))) {
                                        this.rightClickDelayTimer = 0;
                                    } else if (this.rightClickDelayTimer == 0 && this.currentPosition.equals(this.lastPosition) && this.lastSide.equals(ForgeDirection.getOrientation(this.objectMouseOver.sideHit))) {
                                        this.rightClickDelayTimer = 4;
                                    }

                                    this.lastPosition.set(this.currentPosition);
                                    this.lastSide = ForgeDirection.getOrientation(this.objectMouseOver.sideHit);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @MixinMerged(
        mixin = "com.mitchej123.hodgepodge.mixins.early.minecraft.MixinMinecraft_FixDuplicateSounds",
        priority = 1000,
        sessionId = "98d213e4-85ca-4ce4-959f-84858add440a"
    )
    private boolean wrapWithCondition$zhb000$hodgepodge$fixDuplicateSounds(SoundHandler instance, GuiScreen old) {
        return old instanceof GuiIngameMenu && this.isSingleplayer() && !this.theIntegratedServer.getPublic();
    }
}
