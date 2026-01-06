package net.minecraft.client.entity;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.io.File;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ImageBufferDownload;
import net.minecraft.client.renderer.ThreadDownloadImageData;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.client.resources.SkinManager.SkinAvailableCallback;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StringUtils;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.transformer.meta.MixinMerged;

@SideOnly(Side.CLIENT)
public abstract class AbstractClientPlayer extends EntityPlayer implements SkinAvailableCallback {
    public static final ResourceLocation locationStevePng = new ResourceLocation("textures/entity/steve.png");
    private ResourceLocation locationSkin;
    private ResourceLocation locationCape;
    private static final String __OBFID = "CL_00000935";

    public AbstractClientPlayer(World p_i45074_1_, GameProfile p_i45074_2_) {
        super(p_i45074_1_, p_i45074_2_);
        String s = this.getCommandSenderName();
        if (!s.isEmpty()) {
            SkinManager skinmanager = Minecraft.getMinecraft().func_152342_ad();
            skinmanager.func_152790_a(p_i45074_2_, this, true);
        }

    }

    public boolean func_152122_n() {
        return this.locationCape != null;
    }

    public boolean func_152123_o() {
        return this.locationSkin != null;
    }

    public ResourceLocation getLocationSkin() {
        return this.locationSkin == null ? locationStevePng : this.locationSkin;
    }

    public ResourceLocation getLocationCape() {
        return this.locationCape;
    }

    public static ThreadDownloadImageData getDownloadImageSkin(ResourceLocation resourceLocationIn, String username) {
        TextureManager texturemanager = Minecraft.getMinecraft().getTextureManager();
        Object object = texturemanager.getTexture(resourceLocationIn);
        if (object == null) {
            object = new ThreadDownloadImageData((File)null, String.format(constant$zfp000$hodgepodge$redirectSkinUrl("http://skins.minecraft.net/MinecraftSkins/%s.png"), StringUtils.stripControlCodes(username)), locationStevePng, new ImageBufferDownload());
            texturemanager.loadTexture(resourceLocationIn, (ITextureObject)object);
        }

        return (ThreadDownloadImageData)object;
    }

    public static ResourceLocation getLocationSkin(String username) {
        return new ResourceLocation("skins/" + StringUtils.stripControlCodes(username));
    }

    public void func_152121_a(Type skinPart, ResourceLocation skinLoc) {
        switch(net.minecraft.client.entity.AbstractClientPlayer.SwitchType.field_152630_a[skinPart.ordinal()]) {
        case 1:
            this.locationSkin = skinLoc;
            break;
        case 2:
            this.locationCape = skinLoc;
        }

    }

    @MixinMerged(
        mixin = "com.mitchej123.hodgepodge.mixins.early.minecraft.MixinAbstractClientPlayer",
        priority = 1000,
        sessionId = "98d213e4-85ca-4ce4-959f-84858add440a"
    )
    private static String constant$zfp000$hodgepodge$redirectSkinUrl(String url) {
        return "https://visage.surgeplay.com/skin/%s.png";
    }
}
