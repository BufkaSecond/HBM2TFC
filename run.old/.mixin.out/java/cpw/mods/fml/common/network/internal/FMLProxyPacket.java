package cpw.mods.fml.common.network.internal;

import com.google.common.collect.ConcurrentHashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;
import com.google.common.collect.UnmodifiableIterator;
import com.google.common.collect.Multiset.Entry;
import com.llamalad7.mixinextras.sugar.impl.ref.generated.LocalRefImpl;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mitchej123.hodgepodge.Common;
import com.mitchej123.hodgepodge.mixins.hooks.NetworkDispatcherFallbackLookup;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.network.FMLNetworkException;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.handshake.NetworkDispatcher;
import cpw.mods.fml.relauncher.Side;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import java.io.IOException;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.C17PacketCustomPayload;
import net.minecraft.network.play.server.S3FPacketCustomPayload;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.helpers.Integers;
import org.spongepowered.asm.mixin.transformer.meta.MixinMerged;

public class FMLProxyPacket extends Packet {
    final String channel;
    private Side target;
    private final ByteBuf payload;
    private INetHandler netHandler;
    private NetworkDispatcher dispatcher;
    private static Multiset<String> badPackets = ConcurrentHashMultiset.create();
    private static int packetCountWarning = Integers.parseInt(System.getProperty("fml.badPacketCounter", "100"), 100);

    private FMLProxyPacket(byte[] payload, String channel) {
        this(Unpooled.wrappedBuffer(payload), channel);
    }

    public FMLProxyPacket(S3FPacketCustomPayload original) {
        this(original.func_149168_d(), original.func_149169_c());
        this.target = Side.CLIENT;
    }

    public FMLProxyPacket(C17PacketCustomPayload original) {
        this(original.func_149558_e(), original.func_149559_c());
        this.target = Side.SERVER;
    }

    public FMLProxyPacket(ByteBuf payload, String channel) {
        this.channel = channel;
        this.payload = payload;
    }

    public void readPacketData(PacketBuffer packetbuffer) throws IOException {
    }

    public void writePacketData(PacketBuffer packetbuffer) throws IOException {
    }

    public void processPacket(INetHandler inethandler) {
        LocalRefImpl sharedRef9 = new LocalRefImpl();
        sharedRef9.init((Object)null);
        this.netHandler = inethandler;
        EmbeddedChannel internalChannel = NetworkRegistry.INSTANCE.getChannel(this.channel, this.target);
        if (internalChannel != null) {
            internalChannel.attr(NetworkRegistry.NET_HANDLER).set(this.netHandler);

            NetworkDispatcher injectorAllocatedLocal6;
            String injectorAllocatedLocal7;
            try {
                if (internalChannel.writeInbound(new Object[]{this})) {
                    badPackets.add(this.channel);
                    if (badPackets.size() % packetCountWarning == 0) {
                        FMLLog.severe("Detected ongoing potential memory leak. %d packets have leaked. Top offenders", new Object[]{badPackets.size()});
                        int i = 0;
                        UnmodifiableIterator var4 = Multisets.copyHighestCountFirst(badPackets).entrySet().iterator();

                        while(var4.hasNext()) {
                            Entry<String> s = (Entry)var4.next();
                            if (i++ > 10) {
                                break;
                            }

                            FMLLog.severe("\t %s : %d", new Object[]{s.getElement(), s.getCount()});
                        }
                    }
                }

                internalChannel.inboundMessages().clear();
            } catch (FMLNetworkException var10) {
                FMLLog.log(Level.ERROR, var10, "There was a network exception handling a packet on channel %s", new Object[]{this.channel});
                NetworkDispatcher var12 = this.dispatcher;
                injectorAllocatedLocal7 = var10.getMessage();
                injectorAllocatedLocal6 = var12;
                this.redirect$zec000$hodgepodge$rejectHandshakeNullSafe(injectorAllocatedLocal6, injectorAllocatedLocal7, sharedRef9);
            } catch (Throwable var11) {
                Level var10000 = Level.ERROR;
                Object[] injectorAllocatedLocal8 = new Object[]{this.channel};
                injectorAllocatedLocal7 = "There was a critical exception handling a packet on channel %s";
                FMLLog.log(var10000, this.modify$zec000$hodgepodge$captureThrowable(var11, sharedRef9), injectorAllocatedLocal7, injectorAllocatedLocal8);
                injectorAllocatedLocal7 = "A fatal error has occured, this connection is terminated";
                injectorAllocatedLocal6 = this.dispatcher;
                this.redirect$zec000$hodgepodge$rejectHandshakeNullSafe(injectorAllocatedLocal6, injectorAllocatedLocal7, sharedRef9);
            }
        }

    }

    public String channel() {
        return this.channel;
    }

    public ByteBuf payload() {
        return this.payload;
    }

    public INetHandler handler() {
        return this.netHandler;
    }

    public Packet toC17Packet() {
        return new C17PacketCustomPayload(this.channel, this.payload.array());
    }

    public Packet toS3FPacket() {
        return new S3FPacketCustomPayload(this.channel, this.payload.array());
    }

    public void setTarget(Side target) {
        this.target = target;
    }

    public void setDispatcher(NetworkDispatcher networkDispatcher) {
        this.dispatcher = networkDispatcher;
    }

    public NetworkManager getOrigin() {
        return this.dispatcher != null ? this.dispatcher.manager : null;
    }

    public NetworkDispatcher getDispatcher() {
        return this.dispatcher;
    }

    public Side getTarget() {
        return this.target;
    }

    @MixinMerged(
        mixin = "com.mitchej123.hodgepodge.mixins.early.fml.MixinFMLProxyPacket",
        priority = 1000,
        sessionId = "98d213e4-85ca-4ce4-959f-84858add440a"
    )
    public Throwable modify$zec000$hodgepodge$captureThrowable(Throwable e, LocalRef<Throwable> throwableStorage) {
        throwableStorage.set(e);
        return e;
    }

    @MixinMerged(
        mixin = "com.mitchej123.hodgepodge.mixins.early.fml.MixinFMLProxyPacket",
        priority = 1000,
        sessionId = "98d213e4-85ca-4ce4-959f-84858add440a"
    )
    public void redirect$zec000$hodgepodge$rejectHandshakeNullSafe(NetworkDispatcher dispatcher, String message, LocalRef<Throwable> throwableStorage) {
        if (FMLCommonHandler.instance().getSide().isClient() && "A fatal error has occured, this connection is terminated".equals(message)) {
            Throwable t = (Throwable)throwableStorage.get();
            message = "A fatal error has occurred during the network handshake, this connection is terminated. Check the log for details. The exception caught was: " + t;
        }

        if (dispatcher == null) {
            dispatcher = NetworkDispatcherFallbackLookup.getFallbackDispatcher(this.target);
        }

        if (dispatcher == null) {
            Common.log.warn("NetworkDispatcher is null, skipping rejectHandshake to avoid NPE");
            Common.log.warn("The message would have been: {}", new Object[]{message});
        } else {
            dispatcher.rejectHandshake(message);
        }

    }
}
