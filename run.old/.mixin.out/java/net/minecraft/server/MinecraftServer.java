package net.minecraft.server;

import com.google.common.base.Charsets;
import com.gtnewhorizon.gtnhlib.util.ServerThreadUtil;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.LoaderState;
import cpw.mods.fml.common.StartupQuery;
import cpw.mods.fml.common.StartupQuery.AbortedException;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.base64.Base64;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.Proxy;
import java.security.KeyPair;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import javax.imageio.ImageIO;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandManager;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.crash.CrashReport;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetworkSystem;
import net.minecraft.network.ServerStatusResponse;
import net.minecraft.network.ServerStatusResponse.MinecraftProtocolVersionIdentifier;
import net.minecraft.network.ServerStatusResponse.PlayerCountData;
import net.minecraft.network.play.server.S03PacketTimeUpdate;
import net.minecraft.profiler.IPlayerUsage;
import net.minecraft.profiler.PlayerUsageSnooper;
import net.minecraft.profiler.Profiler;
import net.minecraft.server.MinecraftServer.1;
import net.minecraft.server.MinecraftServer.2;
import net.minecraft.server.MinecraftServer.3;
import net.minecraft.server.MinecraftServer.4;
import net.minecraft.server.MinecraftServer.5;
import net.minecraft.server.gui.IUpdatePlayerListBox;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ReportedException;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.MinecraftException;
import net.minecraft.world.World;
import net.minecraft.world.WorldManager;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldServerMulti;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.world.WorldSettings.GameType;
import net.minecraft.world.chunk.storage.AnvilSaveConverter;
import net.minecraft.world.demo.DemoWorldServer;
import net.minecraft.world.storage.ISaveFormat;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.chunkio.ChunkIOExecutor;
import net.minecraftforge.event.world.WorldEvent.Load;
import net.minecraftforge.event.world.WorldEvent.Unload;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.transformer.meta.MixinMerged;

public abstract class MinecraftServer implements ICommandSender, Runnable, IPlayerUsage {
    private static final Logger logger = LogManager.getLogger();
    public static final File field_152367_a = new File("usercache.json");
    private static MinecraftServer mcServer;
    private final ISaveFormat anvilConverterForAnvilFile;
    private final PlayerUsageSnooper usageSnooper = new PlayerUsageSnooper("server", this, getSystemTimeMillis());
    private final File anvilFile;
    private final List tickables = new ArrayList();
    private final ICommandManager commandManager;
    @Final
    public final Profiler theProfiler = new Profiler();
    private final NetworkSystem field_147144_o;
    private final ServerStatusResponse field_147147_p = new ServerStatusResponse();
    private final Random field_147146_q = new Random();
    private int serverPort = -1;
    public WorldServer[] worldServers = new WorldServer[0];
    private ServerConfigurationManager serverConfigManager;
    private boolean serverRunning = true;
    private boolean serverStopped;
    private int tickCounter;
    protected final Proxy serverProxy;
    public String currentTask;
    public int percentDone;
    private boolean onlineMode;
    private boolean canSpawnAnimals;
    private boolean canSpawnNPCs;
    private boolean pvpEnabled;
    private boolean allowFlight;
    private String motd;
    private int buildLimit;
    private int field_143008_E = 0;
    public final long[] tickTimeArray = new long[100];
    public Hashtable<Integer, long[]> worldTickTimes = new Hashtable();
    private KeyPair serverKeyPair;
    private String serverOwner;
    private String folderName;
    @SideOnly(Side.CLIENT)
    private String worldName;
    private boolean isDemo;
    private boolean enableBonusChest;
    private boolean worldIsBeingDeleted;
    private String field_147141_M = "";
    private boolean serverIsRunning;
    private long timeOfLastWarning;
    private String userMessage;
    private boolean startProfiling;
    private boolean isGamemodeForced;
    private final YggdrasilAuthenticationService field_152364_T;
    private final MinecraftSessionService field_147143_S;
    private long field_147142_T = 0L;
    private final GameProfileRepository field_152365_W;
    private final PlayerProfileCache field_152366_X;
    private static final String __OBFID = "CL_00001462";

    public MinecraftServer(File workDir, Proxy proxy) {
        this.field_152366_X = new PlayerProfileCache(this, field_152367_a);
        mcServer = this;
        this.serverProxy = proxy;
        this.anvilFile = workDir;
        this.field_147144_o = new NetworkSystem(this);
        this.commandManager = new ServerCommandManager();
        this.anvilConverterForAnvilFile = new AnvilSaveConverter(workDir);
        this.field_152364_T = new YggdrasilAuthenticationService(proxy, UUID.randomUUID().toString());
        this.field_147143_S = this.field_152364_T.createMinecraftSessionService();
        this.field_152365_W = this.field_152364_T.createProfileRepository();
    }

    protected abstract boolean startServer() throws IOException;

    protected void convertMapIfNeeded(String p_71237_1_) {
        if (this.getActiveAnvilConverter().isOldMapFormat(p_71237_1_)) {
            logger.info("Converting map!");
            this.setUserMessage("menu.convertingLevel");
            this.getActiveAnvilConverter().convertMapFormat(p_71237_1_, new 1(this));
        }

    }

    protected synchronized void setUserMessage(String message) {
        this.userMessage = message;
    }

    @SideOnly(Side.CLIENT)
    public synchronized String getUserMessage() {
        return this.userMessage;
    }

    protected void loadAllWorlds(String p_71247_1_, String p_71247_2_, long p_71247_3_, WorldType p_71247_5_, String p_71247_6_) {
        this.convertMapIfNeeded(p_71247_1_);
        this.setUserMessage("menu.loadingLevel");
        ISaveHandler isavehandler = this.anvilConverterForAnvilFile.getSaveLoader(p_71247_1_, true);
        WorldInfo worldinfo = isavehandler.loadWorldInfo();
        WorldSettings worldsettings;
        if (worldinfo == null) {
            worldsettings = new WorldSettings(p_71247_3_, this.getGameType(), this.canStructuresSpawn(), this.isHardcore(), p_71247_5_);
            worldsettings.func_82750_a(p_71247_6_);
        } else {
            worldsettings = new WorldSettings(worldinfo);
        }

        if (this.enableBonusChest) {
            worldsettings.enableBonusChest();
        }

        WorldServer overWorld = this.isDemo() ? new DemoWorldServer(this, isavehandler, p_71247_2_, 0, this.theProfiler) : new WorldServer(this, isavehandler, p_71247_2_, 0, worldsettings, this.theProfiler);
        Integer[] var11 = DimensionManager.getStaticDimensionIDs();
        int var12 = var11.length;

        for(int var13 = 0; var13 < var12; ++var13) {
            int dim = var11[var13];
            WorldServer world = dim == 0 ? overWorld : new WorldServerMulti(this, isavehandler, p_71247_2_, dim, worldsettings, (WorldServer)overWorld, this.theProfiler);
            ((WorldServer)world).addWorldAccess(new WorldManager(this, (WorldServer)world));
            if (!this.isSinglePlayer()) {
                ((WorldServer)world).getWorldInfo().setGameType(this.getGameType());
            }

            MinecraftForge.EVENT_BUS.post(new Load((World)world));
        }

        this.serverConfigManager.setPlayerManager(new WorldServer[]{(WorldServer)overWorld});
        this.func_147139_a(this.func_147135_j());
        this.initialWorldChunkLoad();
    }

    protected void initialWorldChunkLoad() {
        boolean flag = true;
        boolean flag1 = true;
        boolean flag2 = true;
        boolean flag3 = true;
        int i = 0;
        this.setUserMessage("menu.generatingTerrain");
        byte b0 = 0;
        logger.info("Preparing start region for level " + b0);
        WorldServer worldserver = this.worldServers[b0];
        ChunkCoordinates chunkcoordinates = worldserver.getSpawnPoint();
        long j = getSystemTimeMillis();

        for(int k = -192; k <= 192 && this.isServerRunning(); k += 16) {
            for(int l = -192; l <= 192 && this.isServerRunning(); l += 16) {
                long i1 = getSystemTimeMillis();
                if (i1 - j > 1000L) {
                    this.outputPercentRemaining("Preparing spawn area", i * 100 / 625);
                    j = i1;
                }

                ++i;
                worldserver.theChunkProviderServer.loadChunk(chunkcoordinates.posX + k >> 4, chunkcoordinates.posZ + l >> 4);
            }
        }

        this.clearCurrentTask();
    }

    public abstract boolean canStructuresSpawn();

    public abstract GameType getGameType();

    public abstract EnumDifficulty func_147135_j();

    public abstract boolean isHardcore();

    public abstract int getOpPermissionLevel();

    public abstract boolean func_152363_m();

    protected void outputPercentRemaining(String message, int percent) {
        this.currentTask = message;
        this.percentDone = percent;
        logger.info(message + ": " + percent + "%");
    }

    protected void clearCurrentTask() {
        this.currentTask = null;
        this.percentDone = 0;
    }

    protected void saveAllWorlds(boolean dontLog) {
        if (!this.worldIsBeingDeleted) {
            WorldServer[] aworldserver = this.worldServers;
            if (aworldserver == null) {
                return;
            }

            int i = aworldserver.length;

            for(int j = 0; j < i; ++j) {
                WorldServer worldserver = aworldserver[j];
                if (worldserver != null) {
                    if (!dontLog) {
                        logger.info("Saving chunks for level '" + worldserver.getWorldInfo().getWorldName() + "'/" + worldserver.provider.getDimensionName());
                    }

                    try {
                        worldserver.saveAllChunks(true, (IProgressUpdate)null);
                    } catch (MinecraftException var7) {
                        logger.warn(var7.getMessage());
                    }
                }
            }
        }

    }

    public void stopServer() {
        if (!this.worldIsBeingDeleted && Loader.instance().hasReachedState(LoaderState.SERVER_STARTED) && !this.serverStopped) {
            logger.info("Stopping server");
            if (this.func_147137_ag() != null) {
                this.func_147137_ag().terminateEndpoints();
            }

            if (this.serverConfigManager != null) {
                logger.info("Saving players");
                this.serverConfigManager.saveAllPlayerData();
                this.serverConfigManager.removeAllPlayers();
            }

            if (this.worldServers != null) {
                logger.info("Saving worlds");
                this.saveAllWorlds(false);

                for(int i = 0; i < this.worldServers.length; ++i) {
                    WorldServer worldserver = this.worldServers[i];
                    MinecraftForge.EVENT_BUS.post(new Unload(worldserver));
                    worldserver.flush();
                }

                WorldServer[] tmp = this.worldServers;
                WorldServer[] var7 = tmp;
                int var3 = tmp.length;

                for(int var4 = 0; var4 < var3; ++var4) {
                    WorldServer world = var7[var4];
                    DimensionManager.setWorld(world.provider.dimensionId, (WorldServer)null);
                }
            }

            if (this.usageSnooper.isSnooperRunning()) {
                this.usageSnooper.stopSnooper();
            }
        }

    }

    public boolean isServerRunning() {
        return this.serverRunning;
    }

    public void initiateShutdown() {
        this.serverRunning = false;
    }

    public void run() {
        this.handler$zjh000$gtnhlib$saveServerThreadReference((CallbackInfo)null);

        try {
            if (this.startServer()) {
                FMLCommonHandler.instance().handleServerStarted();
                long i = getSystemTimeMillis();
                long l = 0L;
                this.field_147147_p.func_151315_a(new ChatComponentText(this.motd));
                this.field_147147_p.func_151321_a(new MinecraftProtocolVersionIdentifier("1.7.10", 5));
                this.func_147138_a(this.field_147147_p);

                while(this.serverRunning) {
                    long j = getSystemTimeMillis();
                    long k = j - i;
                    if (k > 2000L && i - this.timeOfLastWarning >= 15000L) {
                        logger.warn("Can't keep up! Did the system time change, or is the server overloaded? Running {}ms behind, skipping {} tick(s)", new Object[]{k, k / 50L});
                        k = 2000L;
                        this.timeOfLastWarning = i;
                    }

                    if (k < 0L) {
                        logger.warn("Time ran backwards! Did the system time change?");
                        k = 0L;
                    }

                    l += k;
                    i = j;
                    if (this.worldServers[0].areAllPlayersAsleep()) {
                        this.tick();
                        l = 0L;
                    } else {
                        while(l > 50L) {
                            l -= 50L;
                            this.tick();
                        }
                    }

                    Thread.sleep(Math.max(1L, 50L - l));
                    this.serverIsRunning = true;
                }

                FMLCommonHandler.instance().handleServerStopping();
                FMLCommonHandler.instance().expectServerStopped();
            } else {
                FMLCommonHandler.instance().expectServerStopped();
                this.finalTick((CrashReport)null);
            }
        } catch (AbortedException var72) {
            FMLCommonHandler.instance().expectServerStopped();
        } catch (Throwable var73) {
            logger.error("Encountered an unexpected exception", var73);
            CrashReport crashreport = null;
            if (var73 instanceof ReportedException) {
                crashreport = this.addServerInfoToCrashReport(((ReportedException)var73).getCrashReport());
            } else {
                crashreport = this.addServerInfoToCrashReport(new CrashReport("Exception in server tick loop", var73));
            }

            File file1 = new File(new File(this.getDataDirectory(), "crash-reports"), "crash-" + (new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss")).format(new Date()) + "-server.txt");
            if (crashreport.saveToFile(file1)) {
                logger.error("This crash report has been saved to: " + file1.getAbsolutePath());
            } else {
                logger.error("We were unable to save this crash report to disk.");
            }

            FMLCommonHandler.instance().expectServerStopped();
            this.finalTick(crashreport);
        } finally {
            try {
                this.stopServer();
                this.serverStopped = true;
            } catch (Throwable var70) {
                logger.error("Exception stopping the server", var70);
            } finally {
                FMLCommonHandler.instance().handleServerStopped();
                this.serverStopped = true;
                this.systemExitNow();
            }

        }

        this.handler$zjh000$gtnhlib$clearServerThreadReference((CallbackInfo)null);
    }

    private void func_147138_a(ServerStatusResponse response) {
        File file1 = this.getFile("server-icon.png");
        if (file1.isFile()) {
            ByteBuf bytebuf = Unpooled.buffer();

            try {
                BufferedImage bufferedimage = ImageIO.read(file1);
                Validate.validState(bufferedimage.getWidth() == 64, "Must be 64 pixels wide", new Object[0]);
                Validate.validState(bufferedimage.getHeight() == 64, "Must be 64 pixels high", new Object[0]);
                ImageIO.write(bufferedimage, "PNG", new ByteBufOutputStream(bytebuf));
                ByteBuf bytebuf1 = Base64.encode(bytebuf);
                response.func_151320_a("data:image/png;base64," + bytebuf1.toString(Charsets.UTF_8));
            } catch (Exception var9) {
                logger.error("Couldn't load server icon", var9);
            } finally {
                bytebuf.release();
            }
        }

    }

    protected File getDataDirectory() {
        return new File(".");
    }

    protected void finalTick(CrashReport report) {
    }

    protected void systemExitNow() {
    }

    public void tick() {
        long i = System.nanoTime();
        FMLCommonHandler.instance().onPreServerTick();
        ++this.tickCounter;
        if (this.startProfiling) {
            this.startProfiling = false;
            this.theProfiler.profilingEnabled = true;
            this.theProfiler.clearProfiling();
        }

        this.theProfiler.startSection("root");
        this.updateTimeLightAndEntities();
        if (i - this.field_147142_T >= 5000000000L) {
            this.field_147142_T = i;
            this.field_147147_p.func_151319_a(new PlayerCountData(this.getMaxPlayers(), this.getCurrentPlayerCount()));
            GameProfile[] agameprofile = new GameProfile[Math.min(this.getCurrentPlayerCount(), 12)];
            int j = MathHelper.getRandomIntegerInRange(this.field_147146_q, 0, this.getCurrentPlayerCount() - agameprofile.length);

            for(int k = 0; k < agameprofile.length; ++k) {
                agameprofile[k] = ((EntityPlayerMP)this.serverConfigManager.playerEntityList.get(j + k)).getGameProfile();
            }

            Collections.shuffle(Arrays.asList(agameprofile));
            this.field_147147_p.func_151318_b().func_151330_a(agameprofile);
        }

        if (this.tickCounter % 900 == 0) {
            this.theProfiler.startSection("save");
            this.serverConfigManager.saveAllPlayerData();
            this.saveAllWorlds(true);
            this.theProfiler.endSection();
        }

        this.theProfiler.startSection("tallying");
        this.tickTimeArray[this.tickCounter % 100] = System.nanoTime() - i;
        this.theProfiler.endSection();
        this.theProfiler.startSection("snooper");
        if (!this.usageSnooper.isSnooperRunning() && this.tickCounter > 100) {
            this.usageSnooper.startSnooper();
        }

        if (this.tickCounter % 6000 == 0) {
            this.usageSnooper.addMemoryStatsToSnooper();
        }

        this.theProfiler.endSection();
        this.theProfiler.endSection();
        FMLCommonHandler.instance().onPostServerTick();
    }

    public void updateTimeLightAndEntities() {
        this.handler$zjh000$gtnhlib$runJobs((CallbackInfo)null);
        this.theProfiler.startSection("levels");
        ChunkIOExecutor.tick();
        Integer[] ids = DimensionManager.getIDs(this.tickCounter % 200 == 0);

        for(int x = 0; x < ids.length; ++x) {
            int id = ids[x];
            long j = System.nanoTime();
            if (id == 0 || this.getAllowNether()) {
                WorldServer worldserver = DimensionManager.getWorld(id);
                this.theProfiler.startSection(worldserver.getWorldInfo().getWorldName());
                this.theProfiler.startSection("pools");
                this.theProfiler.endSection();
                if (this.tickCounter % 20 == 0) {
                    this.theProfiler.startSection("timeSync");
                    this.serverConfigManager.sendPacketToAllPlayersInDimension(new S03PacketTimeUpdate(worldserver.getTotalWorldTime(), worldserver.getWorldTime(), worldserver.getGameRules().getGameRuleBooleanValue("doDaylightCycle")), worldserver.provider.dimensionId);
                    this.theProfiler.endSection();
                }

                this.theProfiler.startSection("tick");
                FMLCommonHandler.instance().onPreWorldTick(worldserver);

                CrashReport crashreport;
                try {
                    worldserver.tick();
                } catch (Throwable var11) {
                    crashreport = CrashReport.makeCrashReport(var11, "Exception ticking world");
                    worldserver.addWorldInfoToCrashReport(crashreport);
                    throw new ReportedException(crashreport);
                }

                try {
                    worldserver.updateEntities();
                } catch (Throwable var10) {
                    crashreport = CrashReport.makeCrashReport(var10, "Exception ticking world entities");
                    worldserver.addWorldInfoToCrashReport(crashreport);
                    throw new ReportedException(crashreport);
                }

                FMLCommonHandler.instance().onPostWorldTick(worldserver);
                this.theProfiler.endSection();
                this.theProfiler.startSection("tracker");
                worldserver.getEntityTracker().updateTrackedEntities();
                this.theProfiler.endSection();
                this.theProfiler.endSection();
            }

            ((long[])this.worldTickTimes.get(id))[this.tickCounter % 100] = System.nanoTime() - j;
        }

        this.theProfiler.endStartSection("dim_unloading");
        DimensionManager.unloadWorlds(this.worldTickTimes);
        this.theProfiler.endStartSection("connection");
        this.func_147137_ag().networkTick();
        this.theProfiler.endStartSection("players");
        this.serverConfigManager.sendPlayerInfoToAllPlayers();
        this.theProfiler.endStartSection("tickables");

        for(int i = 0; i < this.tickables.size(); ++i) {
            ((IUpdatePlayerListBox)this.tickables.get(i)).update();
        }

        this.theProfiler.endSection();
    }

    public boolean getAllowNether() {
        return true;
    }

    public void startServerThread() {
        StartupQuery.reset();
        (new 2(this, "Server thread")).start();
    }

    public File getFile(String fileName) {
        return new File(this.getDataDirectory(), fileName);
    }

    public void logWarning(String msg) {
        logger.warn(msg);
    }

    public WorldServer worldServerForDimension(int dimension) {
        WorldServer ret = DimensionManager.getWorld(dimension);
        if (ret == null) {
            DimensionManager.initDimension(dimension);
            ret = DimensionManager.getWorld(dimension);
        }

        return ret;
    }

    public String getMinecraftVersion() {
        return "1.7.10";
    }

    public int getCurrentPlayerCount() {
        return this.serverConfigManager.getCurrentPlayerCount();
    }

    public int getMaxPlayers() {
        return this.serverConfigManager.getMaxPlayers();
    }

    public String[] getAllUsernames() {
        return this.serverConfigManager.getAllUsernames();
    }

    public GameProfile[] func_152357_F() {
        return this.serverConfigManager.func_152600_g();
    }

    public String getServerModName() {
        return FMLCommonHandler.instance().getModName();
    }

    public CrashReport addServerInfoToCrashReport(CrashReport report) {
        report.getCategory().addCrashSectionCallable("Profiler Position", new 3(this));
        if (this.worldServers != null && this.worldServers.length > 0 && this.worldServers[0] != null) {
            report.getCategory().addCrashSectionCallable("Vec3 Pool Size", new 4(this));
        }

        if (this.serverConfigManager != null) {
            report.getCategory().addCrashSectionCallable("Player Count", new 5(this));
        }

        return report;
    }

    public List getPossibleCompletions(ICommandSender sender, String input) {
        ArrayList arraylist = new ArrayList();
        if (input.startsWith("/")) {
            input = input.substring(1);
            boolean flag = !input.contains(" ");
            List list = this.commandManager.getPossibleCommands(sender, input);
            if (list != null) {
                Iterator iterator = list.iterator();

                while(iterator.hasNext()) {
                    String s3 = (String)iterator.next();
                    if (flag) {
                        arraylist.add("/" + s3);
                    } else {
                        arraylist.add(s3);
                    }
                }
            }

            return arraylist;
        } else {
            String[] astring = input.split(" ", -1);
            String s1 = astring[astring.length - 1];
            String[] astring1 = this.serverConfigManager.getAllUsernames();
            int i = astring1.length;

            for(int j = 0; j < i; ++j) {
                String s2 = astring1[j];
                if (CommandBase.doesStringStartWith(s1, s2)) {
                    arraylist.add(s2);
                }
            }

            return arraylist;
        }
    }

    public static MinecraftServer getServer() {
        return mcServer;
    }

    public String getCommandSenderName() {
        return "Server";
    }

    public void addChatMessage(IChatComponent message) {
        logger.info(message.getUnformattedText());
    }

    public boolean canCommandSenderUseCommand(int permissionLevel, String command) {
        return true;
    }

    public ICommandManager getCommandManager() {
        return this.commandManager;
    }

    public KeyPair getKeyPair() {
        return this.serverKeyPair;
    }

    public String getServerOwner() {
        return this.serverOwner;
    }

    public void setServerOwner(String owner) {
        this.serverOwner = owner;
    }

    public boolean isSinglePlayer() {
        return this.serverOwner != null;
    }

    public String getFolderName() {
        return this.folderName;
    }

    public void setFolderName(String name) {
        this.folderName = name;
    }

    @SideOnly(Side.CLIENT)
    public void setWorldName(String p_71246_1_) {
        this.worldName = p_71246_1_;
    }

    @SideOnly(Side.CLIENT)
    public String getWorldName() {
        return this.worldName;
    }

    public void setKeyPair(KeyPair keyPair) {
        this.serverKeyPair = keyPair;
    }

    public void func_147139_a(EnumDifficulty difficulty) {
        for(int i = 0; i < this.worldServers.length; ++i) {
            WorldServer worldserver = this.worldServers[i];
            if (worldserver != null) {
                if (worldserver.getWorldInfo().isHardcoreModeEnabled()) {
                    worldserver.difficultySetting = EnumDifficulty.HARD;
                    worldserver.setAllowedSpawnTypes(true, true);
                } else if (this.isSinglePlayer()) {
                    worldserver.difficultySetting = difficulty;
                    worldserver.setAllowedSpawnTypes(worldserver.difficultySetting != EnumDifficulty.PEACEFUL, true);
                } else {
                    worldserver.difficultySetting = difficulty;
                    worldserver.setAllowedSpawnTypes(this.allowSpawnMonsters(), this.canSpawnAnimals);
                }
            }
        }

    }

    protected boolean allowSpawnMonsters() {
        return true;
    }

    public boolean isDemo() {
        return this.isDemo;
    }

    public void setDemo(boolean demo) {
        this.isDemo = demo;
    }

    public void canCreateBonusChest(boolean enable) {
        this.enableBonusChest = enable;
    }

    public ISaveFormat getActiveAnvilConverter() {
        return this.anvilConverterForAnvilFile;
    }

    public void deleteWorldAndStopServer() {
        this.worldIsBeingDeleted = true;
        this.getActiveAnvilConverter().flushCache();

        for(int i = 0; i < this.worldServers.length; ++i) {
            WorldServer worldserver = this.worldServers[i];
            if (worldserver != null) {
                MinecraftForge.EVENT_BUS.post(new Unload(worldserver));
                worldserver.flush();
            }
        }

        this.getActiveAnvilConverter().deleteWorldDirectory(this.worldServers[0].getSaveHandler().getWorldDirectoryName());
        this.initiateShutdown();
    }

    public String getTexturePack() {
        return this.field_147141_M;
    }

    public void addServerStatsToSnooper(PlayerUsageSnooper playerSnooper) {
        playerSnooper.func_152768_a("whitelist_enabled", false);
        playerSnooper.func_152768_a("whitelist_count", 0);
        playerSnooper.func_152768_a("players_current", this.getCurrentPlayerCount());
        playerSnooper.func_152768_a("players_max", this.getMaxPlayers());
        playerSnooper.func_152768_a("players_seen", this.serverConfigManager.getAvailablePlayerDat().length);
        playerSnooper.func_152768_a("uses_auth", this.onlineMode);
        playerSnooper.func_152768_a("gui_state", this.getGuiEnabled() ? "enabled" : "disabled");
        playerSnooper.func_152768_a("run_time", (getSystemTimeMillis() - playerSnooper.getMinecraftStartTimeMillis()) / 60L * 1000L);
        playerSnooper.func_152768_a("avg_tick_ms", (int)(MathHelper.average(this.tickTimeArray) * 1.0E-6D));
        int i = 0;

        for(int j = 0; j < this.worldServers.length; ++j) {
            if (this.worldServers[j] != null) {
                WorldServer worldserver = this.worldServers[j];
                WorldInfo worldinfo = worldserver.getWorldInfo();
                playerSnooper.func_152768_a("world[" + i + "][dimension]", worldserver.provider.dimensionId);
                playerSnooper.func_152768_a("world[" + i + "][mode]", worldinfo.getGameType());
                playerSnooper.func_152768_a("world[" + i + "][difficulty]", worldserver.difficultySetting);
                playerSnooper.func_152768_a("world[" + i + "][hardcore]", worldinfo.isHardcoreModeEnabled());
                playerSnooper.func_152768_a("world[" + i + "][generator_name]", worldinfo.getTerrainType().getWorldTypeName());
                playerSnooper.func_152768_a("world[" + i + "][generator_version]", worldinfo.getTerrainType().getGeneratorVersion());
                playerSnooper.func_152768_a("world[" + i + "][height]", this.buildLimit);
                playerSnooper.func_152768_a("world[" + i + "][chunks_loaded]", worldserver.getChunkProvider().getLoadedChunkCount());
                ++i;
            }
        }

        playerSnooper.func_152768_a("worlds", i);
    }

    public void addServerTypeToSnooper(PlayerUsageSnooper playerSnooper) {
        playerSnooper.func_152767_b("singleplayer", this.isSinglePlayer());
        playerSnooper.func_152767_b("server_brand", this.getServerModName());
        playerSnooper.func_152767_b("gui_supported", GraphicsEnvironment.isHeadless() ? "headless" : "supported");
        playerSnooper.func_152767_b("dedicated", this.isDedicatedServer());
    }

    public boolean isSnooperEnabled() {
        return true;
    }

    public abstract boolean isDedicatedServer();

    public boolean isServerInOnlineMode() {
        return this.onlineMode;
    }

    public void setOnlineMode(boolean online) {
        this.onlineMode = online;
    }

    public boolean getCanSpawnAnimals() {
        return this.canSpawnAnimals;
    }

    public void setCanSpawnAnimals(boolean spawnAnimals) {
        this.canSpawnAnimals = spawnAnimals;
    }

    public boolean getCanSpawnNPCs() {
        return this.canSpawnNPCs;
    }

    public void setCanSpawnNPCs(boolean spawnNpcs) {
        this.canSpawnNPCs = spawnNpcs;
    }

    public boolean isPVPEnabled() {
        return this.pvpEnabled;
    }

    public void setAllowPvp(boolean allowPvp) {
        this.pvpEnabled = allowPvp;
    }

    public boolean isFlightAllowed() {
        return this.allowFlight;
    }

    public void setAllowFlight(boolean allow) {
        this.allowFlight = allow;
    }

    public abstract boolean isCommandBlockEnabled();

    public String getMOTD() {
        return this.motd;
    }

    public void setMOTD(String motdIn) {
        this.motd = motdIn;
    }

    public int getBuildLimit() {
        return this.buildLimit;
    }

    public void setBuildLimit(int maxBuildHeight) {
        this.buildLimit = maxBuildHeight;
    }

    public ServerConfigurationManager getConfigurationManager() {
        return this.serverConfigManager;
    }

    public void func_152361_a(ServerConfigurationManager configManager) {
        this.serverConfigManager = configManager;
    }

    public void setGameType(GameType gameMode) {
        for(int i = 0; i < this.worldServers.length; ++i) {
            getServer().worldServers[i].getWorldInfo().setGameType(gameMode);
        }

    }

    public NetworkSystem func_147137_ag() {
        return this.field_147144_o;
    }

    @SideOnly(Side.CLIENT)
    public boolean serverIsInRunLoop() {
        return this.serverIsRunning;
    }

    public boolean getGuiEnabled() {
        return false;
    }

    public abstract String shareToLAN(GameType var1, boolean var2);

    public int getTickCounter() {
        return this.tickCounter;
    }

    public void enableProfiling() {
        this.startProfiling = true;
    }

    @SideOnly(Side.CLIENT)
    public PlayerUsageSnooper getPlayerUsageSnooper() {
        return this.usageSnooper;
    }

    public ChunkCoordinates getPlayerCoordinates() {
        return new ChunkCoordinates(0, 0, 0);
    }

    public World getEntityWorld() {
        return this.worldServers[0];
    }

    public int getSpawnProtectionSize() {
        return 16;
    }

    public boolean isBlockProtected(World inWorld, int x, int y, int z, EntityPlayer player) {
        return false;
    }

    public boolean getForceGamemode() {
        return this.isGamemodeForced;
    }

    public Proxy getServerProxy() {
        return this.serverProxy;
    }

    public static long getSystemTimeMillis() {
        return System.currentTimeMillis();
    }

    public int func_143007_ar() {
        return this.field_143008_E;
    }

    public void func_143006_e(int idleTimeout) {
        this.field_143008_E = idleTimeout;
    }

    public IChatComponent func_145748_c_() {
        return new ChatComponentText(this.getCommandSenderName());
    }

    public boolean func_147136_ar() {
        return true;
    }

    public MinecraftSessionService func_147130_as() {
        return this.field_147143_S;
    }

    public GameProfileRepository func_152359_aw() {
        return this.field_152365_W;
    }

    public PlayerProfileCache func_152358_ax() {
        return this.field_152366_X;
    }

    public ServerStatusResponse func_147134_at() {
        return this.field_147147_p;
    }

    public void func_147132_au() {
        this.field_147142_T = 0L;
    }

    public boolean isServerStopped() {
        return this.serverStopped;
    }

    // $FF: synthetic method
    static Logger access$000() {
        return logger;
    }

    // $FF: synthetic method
    static ServerConfigurationManager access$100(MinecraftServer x0) {
        return x0.serverConfigManager;
    }

    @MixinMerged(
        mixin = "com.gtnewhorizon.gtnhlib.mixins.early.MixinMinecraftServer",
        priority = 1000,
        sessionId = "98d213e4-85ca-4ce4-959f-84858add440a"
    )
    private void handler$zjh000$gtnhlib$runJobs(CallbackInfo ci) {
        this.theProfiler.startSection("jobs");
        ServerThreadUtil.runJobs();
    }

    @MixinMerged(
        mixin = "com.gtnewhorizon.gtnhlib.mixins.early.MixinMinecraftServer",
        priority = 1000,
        sessionId = "98d213e4-85ca-4ce4-959f-84858add440a"
    )
    private void handler$zjh000$gtnhlib$saveServerThreadReference(CallbackInfo ci) {
        ServerThreadUtil.setup(this, Thread.currentThread());
    }

    @MixinMerged(
        mixin = "com.gtnewhorizon.gtnhlib.mixins.early.MixinMinecraftServer",
        priority = 1000,
        sessionId = "98d213e4-85ca-4ce4-959f-84858add440a"
    )
    private void handler$zjh000$gtnhlib$clearServerThreadReference(CallbackInfo ci) {
        ServerThreadUtil.clear();
    }
}
