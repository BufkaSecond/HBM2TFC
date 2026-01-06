package cpw.mods.fml.common.network.internal;

import com.llamalad7.mixinextras.sugar.impl.ref.generated.LocalBooleanRefImpl;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.internal.FMLMessage.OpenGui;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.world.World;
import org.apache.logging.log4j.Level;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.transformer.meta.MixinMerged;

public class OpenGuiHandler extends SimpleChannelInboundHandler<OpenGui> {
    protected void channelRead0(ChannelHandlerContext var1, OpenGui msg) throws Exception {
        LocalBooleanRefImpl sharedRef5 = new LocalBooleanRefImpl();
        sharedRef5.init(false);
        EntityPlayer player = FMLClientHandler.instance().getClient().thePlayer;
        int injectorAllocatedLocal12 = msg.z;
        int injectorAllocatedLocal11 = msg.y;
        int injectorAllocatedLocal10 = msg.x;
        World injectorAllocatedLocal9 = player.worldObj;
        int injectorAllocatedLocal8 = msg.modGuiId;
        Object injectorAllocatedLocal7 = msg.modId;
        this.redirect$zfj000$hodgepodge$openGui(player, injectorAllocatedLocal7, injectorAllocatedLocal8, injectorAllocatedLocal9, injectorAllocatedLocal10, injectorAllocatedLocal11, injectorAllocatedLocal12, sharedRef5);
        Container var10000 = player.openContainer;
        int var10001 = msg.windowId;
        CallbackInfo callbackInfo4 = new CallbackInfo("channelRead0", true);
        this.handler$zfj000$hodgepodge$dontSetWindowId(callbackInfo4, sharedRef5);
        if (!callbackInfo4.isCancelled()) {
            var10000.windowId = var10001;
        }
    }

    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        FMLLog.log(Level.ERROR, cause, "OpenGuiHandler exception", new Object[0]);
        super.exceptionCaught(ctx, cause);
    }

    // $FF: synthetic method
    // $FF: bridge method
    protected void channelRead0(ChannelHandlerContext var1, Object var2) throws Exception {
        this.channelRead0(var1, (OpenGui)var2);
    }

    @MixinMerged(
        mixin = "com.mitchej123.hodgepodge.mixins.early.fml.MixinOpenGuiHandler",
        priority = 1000,
        sessionId = "98d213e4-85ca-4ce4-959f-84858add440a"
    )
    public void redirect$zfj000$hodgepodge$openGui(EntityPlayer player, Object mod, int modGuiId, World world, int x, int y, int z, LocalBooleanRef openGuiSuccess) {
        ModContainer mc = FMLCommonHandler.instance().findContainerFor(mod);
        Object guiContainer = NetworkRegistry.INSTANCE.getLocalGuiContainer(mc, player, modGuiId, world, x, y, z);
        if (guiContainer != null) {
            FMLCommonHandler.instance().showGuiScreen(guiContainer);
            openGuiSuccess.set(true);
        } else {
            openGuiSuccess.set(false);
        }
    }

    @MixinMerged(
        mixin = "com.mitchej123.hodgepodge.mixins.early.fml.MixinOpenGuiHandler",
        priority = 1000,
        sessionId = "98d213e4-85ca-4ce4-959f-84858add440a"
    )
    public void handler$zfj000$hodgepodge$dontSetWindowId(CallbackInfo ci, LocalBooleanRef openGuiSuccess) {
        if (!openGuiSuccess.get()) {
            ci.cancel();
        }

    }
}
