package net.minecraft.client.renderer.texture;

import com.mitchej123.hodgepodge.client.textures.Mipmaps;
import cpw.mods.fml.client.SplashProgress;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.IntBuffer;
import javax.imageio.ImageIO;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.transformer.meta.MixinMerged;

@SideOnly(Side.CLIENT)
public class TextureUtil {
    private static final Logger logger = LogManager.getLogger();
    private static final IntBuffer dataBuffer = GLAllocation.createDirectIntBuffer(4194304);
    public static final DynamicTexture missingTexture = new DynamicTexture(16, 16);
    public static final int[] missingTextureData;
    private static int field_147958_e;
    private static int field_147956_f;
    private static float field_152779_g;
    private static final int[] field_147957_g;
    private static final String __OBFID = "CL_00001067";

    public static int glGenTextures() {
        return GL11.glGenTextures();
    }

    public static void deleteTexture(int p_147942_0_) {
        GL11.glDeleteTextures(p_147942_0_);
    }

    public static int uploadTextureImage(int p_110987_0_, BufferedImage p_110987_1_) {
        return uploadTextureImageAllocate(p_110987_0_, p_110987_1_, false, false);
    }

    public static void uploadTexture(int p_110988_0_, int[] p_110988_1_, int p_110988_2_, int p_110988_3_) {
        bindTexture(p_110988_0_);
        uploadTextureSub(0, p_110988_1_, p_110988_2_, p_110988_3_, 0, 0, false, false, false);
    }

    private static int func_147944_a(int p_147944_0_, int p_147944_1_, int p_147944_2_, int p_147944_3_, int p_147944_4_) {
        float f = (float)Math.pow((double)((float)(p_147944_0_ >> p_147944_4_ & 255) / 255.0F), 2.2D);
        float f1 = (float)Math.pow((double)((float)(p_147944_1_ >> p_147944_4_ & 255) / 255.0F), 2.2D);
        float f2 = (float)Math.pow((double)((float)(p_147944_2_ >> p_147944_4_ & 255) / 255.0F), 2.2D);
        float f3 = (float)Math.pow((double)((float)(p_147944_3_ >> p_147944_4_ & 255) / 255.0F), 2.2D);
        float f4 = (float)Math.pow((double)(f + f1 + f2 + f3) * 0.25D, 0.45454545454545453D);
        return (int)((double)f4 * 255.0D);
    }

    public static void uploadTextureMipmap(int[][] p_147955_0_, int p_147955_1_, int p_147955_2_, int p_147955_3_, int p_147955_4_, boolean p_147955_5_, boolean p_147955_6_) {
        for(int i1 = 0; i1 < p_147955_0_.length; ++i1) {
            int[] aint1 = p_147955_0_[i1];
            uploadTextureSub(i1, aint1, p_147955_1_ >> i1, p_147955_2_ >> i1, p_147955_3_ >> i1, p_147955_4_ >> i1, p_147955_5_, p_147955_6_, p_147955_0_.length > 1);
        }

    }

    private static void uploadTextureSub(int p_147947_0_, int[] p_147947_1_, int p_147947_2_, int p_147947_3_, int p_147947_4_, int p_147947_5_, boolean p_147947_6_, boolean p_147947_7_, boolean p_147947_8_) {
        int j1 = 4194304 / p_147947_2_;
        func_147954_b(p_147947_6_, p_147947_8_);
        setTextureClamped(p_147947_7_);

        int i2;
        for(int k1 = 0; k1 < p_147947_2_ * p_147947_3_; k1 += p_147947_2_ * i2) {
            int l1 = k1 / p_147947_2_;
            i2 = Math.min(j1, p_147947_3_ - l1);
            int j2 = p_147947_2_ * i2;
            copyToBufferPos(p_147947_1_, k1, j2);
            GL11.glTexSubImage2D(3553, p_147947_0_, p_147947_4_, p_147947_5_ + l1, p_147947_2_, i2, 32993, 33639, dataBuffer);
        }

    }

    public static int uploadTextureImageAllocate(int p_110989_0_, BufferedImage p_110989_1_, boolean p_110989_2_, boolean p_110989_3_) {
        allocateTexture(p_110989_0_, p_110989_1_.getWidth(), p_110989_1_.getHeight());
        return uploadTextureImageSub(p_110989_0_, p_110989_1_, 0, 0, p_110989_2_, p_110989_3_);
    }

    public static void allocateTexture(int p_110991_0_, int p_110991_1_, int p_110991_2_) {
        allocateTextureImpl(p_110991_0_, 0, p_110991_1_, p_110991_2_, 1.0F);
    }

    public static void allocateTextureImpl(int p_147946_0_, int p_147946_1_, int p_147946_2_, int p_147946_3_, float p_147946_4_) {
        Class var5 = SplashProgress.class;
        synchronized(SplashProgress.class) {
            deleteTexture(p_147946_0_);
            bindTexture(p_147946_0_);
        }

        if (OpenGlHelper.anisotropicFilteringSupported) {
            GL11.glTexParameterf(3553, 34046, p_147946_4_);
        }

        if (p_147946_1_ > 0) {
            GL11.glTexParameteri(3553, 33085, p_147946_1_);
            GL11.glTexParameterf(3553, 33082, 0.0F);
            GL11.glTexParameterf(3553, 33083, (float)p_147946_1_);
            GL11.glTexParameterf(3553, 34049, 0.0F);
        }

        for(int i1 = 0; i1 <= p_147946_1_; ++i1) {
            GL11.glTexImage2D(3553, i1, 6408, p_147946_2_ >> i1, p_147946_3_ >> i1, 0, 32993, 33639, (IntBuffer)null);
        }

    }

    public static int uploadTextureImageSub(int p_110995_0_, BufferedImage p_110995_1_, int p_110995_2_, int p_110995_3_, boolean p_110995_4_, boolean p_110995_5_) {
        bindTexture(p_110995_0_);
        uploadTextureImageSubImpl(p_110995_1_, p_110995_2_, p_110995_3_, p_110995_4_, p_110995_5_);
        return p_110995_0_;
    }

    private static void uploadTextureImageSubImpl(BufferedImage p_110993_0_, int p_110993_1_, int p_110993_2_, boolean p_110993_3_, boolean p_110993_4_) {
        int k = p_110993_0_.getWidth();
        int l = p_110993_0_.getHeight();
        int i1 = 4194304 / k;
        int[] aint = new int[i1 * k];
        setTextureBlurred(p_110993_3_);
        setTextureClamped(p_110993_4_);

        for(int j1 = 0; j1 < k * l; j1 += k * i1) {
            int k1 = j1 / k;
            int l1 = Math.min(i1, l - k1);
            int i2 = k * l1;
            p_110993_0_.getRGB(0, k1, k, l1, aint, 0, k);
            copyToBuffer(aint, i2);
            GL11.glTexSubImage2D(3553, 0, p_110993_1_, p_110993_2_ + k1, k, l1, 32993, 33639, dataBuffer);
        }

    }

    private static void setTextureClamped(boolean p_110997_0_) {
        if (p_110997_0_) {
            GL11.glTexParameteri(3553, 10242, 10496);
            GL11.glTexParameteri(3553, 10243, 10496);
        } else {
            GL11.glTexParameteri(3553, 10242, 10497);
            GL11.glTexParameteri(3553, 10243, 10497);
        }

    }

    private static void setTextureBlurred(boolean p_147951_0_) {
        func_147954_b(p_147951_0_, false);
    }

    public static void func_152777_a(boolean p_152777_0_, boolean p_152777_1_, float p_152777_2_) {
        field_147958_e = GL11.glGetTexParameteri(3553, 10241);
        field_147956_f = GL11.glGetTexParameteri(3553, 10240);
        field_152779_g = GL11.glGetTexParameterf(3553, 34046);
        func_147954_b(p_152777_0_, p_152777_1_);
        func_152778_a(p_152777_2_);
    }

    public static void func_147945_b() {
        if (field_147958_e >= 0 && field_147956_f >= 0 && field_152779_g >= 0.0F) {
            func_147952_b(field_147958_e, field_147956_f);
            func_152778_a(field_152779_g);
            field_152779_g = -1.0F;
            field_147958_e = -1;
            field_147956_f = -1;
        }

    }

    private static void func_147952_b(int p_147952_0_, int p_147952_1_) {
        GL11.glTexParameteri(3553, 10241, p_147952_0_);
        GL11.glTexParameteri(3553, 10240, p_147952_1_);
    }

    private static void func_152778_a(float p_152778_0_) {
        GL11.glTexParameterf(3553, 34046, p_152778_0_);
    }

    private static void func_147954_b(boolean p_147954_0_, boolean p_147954_1_) {
        if (p_147954_0_) {
            GL11.glTexParameteri(3553, 10241, p_147954_1_ ? 9987 : 9729);
            GL11.glTexParameteri(3553, 10240, 9729);
        } else {
            GL11.glTexParameteri(3553, 10241, p_147954_1_ ? 9986 : 9728);
            GL11.glTexParameteri(3553, 10240, 9728);
        }

    }

    private static void copyToBuffer(int[] p_110990_0_, int p_110990_1_) {
        copyToBufferPos(p_110990_0_, 0, p_110990_1_);
    }

    private static void copyToBufferPos(int[] p_110994_0_, int p_110994_1_, int p_110994_2_) {
        int[] aint1 = p_110994_0_;
        if (Minecraft.getMinecraft().gameSettings.anaglyph) {
            aint1 = updateAnaglyph(p_110994_0_);
        }

        dataBuffer.clear();
        dataBuffer.put(aint1, p_110994_1_, p_110994_2_);
        dataBuffer.position(0).limit(p_110994_2_);
    }

    static void bindTexture(int p_94277_0_) {
        GL11.glBindTexture(3553, p_94277_0_);
    }

    public static int[] readImageData(IResourceManager p_110986_0_, ResourceLocation p_110986_1_) throws IOException {
        BufferedImage bufferedimage = ImageIO.read(p_110986_0_.getResource(p_110986_1_).getInputStream());
        int i = bufferedimage.getWidth();
        int j = bufferedimage.getHeight();
        int[] aint = new int[i * j];
        bufferedimage.getRGB(0, 0, i, j, aint, 0, i);
        return aint;
    }

    public static int[] updateAnaglyph(int[] p_110985_0_) {
        int[] aint1 = new int[p_110985_0_.length];

        for(int i = 0; i < p_110985_0_.length; ++i) {
            int j = p_110985_0_[i] >> 24 & 255;
            int k = p_110985_0_[i] >> 16 & 255;
            int l = p_110985_0_[i] >> 8 & 255;
            int i1 = p_110985_0_[i] & 255;
            int j1 = (k * 30 + l * 59 + i1 * 11) / 100;
            int k1 = (k * 30 + l * 70) / 100;
            int l1 = (k * 30 + i1 * 70) / 100;
            aint1[i] = j << 24 | j1 << 16 | k1 << 8 | l1;
        }

        return aint1;
    }

    public static int[] prepareAnisotropicData(int[] p_147948_0_, int p_147948_1_, int p_147948_2_, int p_147948_3_) {
        int l = p_147948_1_ + 2 * p_147948_3_;

        int i1;
        int j1;
        for(i1 = p_147948_2_ - 1; i1 >= 0; --i1) {
            j1 = i1 * p_147948_1_;
            int k1 = p_147948_3_ + (i1 + p_147948_3_) * l;

            int l1;
            for(l1 = 0; l1 < p_147948_3_; l1 += p_147948_1_) {
                int i2 = Math.min(p_147948_1_, p_147948_3_ - l1);
                System.arraycopy(p_147948_0_, j1 + p_147948_1_ - i2, p_147948_0_, k1 - l1 - i2, i2);
            }

            System.arraycopy(p_147948_0_, j1, p_147948_0_, k1, p_147948_1_);

            for(l1 = 0; l1 < p_147948_3_; l1 += p_147948_1_) {
                System.arraycopy(p_147948_0_, j1, p_147948_0_, k1 + p_147948_1_ + l1, Math.min(p_147948_1_, p_147948_3_ - l1));
            }
        }

        for(i1 = 0; i1 < p_147948_3_; i1 += p_147948_2_) {
            j1 = Math.min(p_147948_2_, p_147948_3_ - i1);
            System.arraycopy(p_147948_0_, (p_147948_3_ + p_147948_2_ - j1) * l, p_147948_0_, (p_147948_3_ - i1 - j1) * l, l * j1);
        }

        for(i1 = 0; i1 < p_147948_3_; i1 += p_147948_2_) {
            j1 = Math.min(p_147948_2_, p_147948_3_ - i1);
            System.arraycopy(p_147948_0_, p_147948_3_ * l, p_147948_0_, (p_147948_2_ + p_147948_3_ + i1) * l, l * j1);
        }

        return p_147948_0_;
    }

    public static void func_147953_a(int[] p_147953_0_, int p_147953_1_, int p_147953_2_) {
        int[] aint1 = new int[p_147953_1_];
        int k = p_147953_2_ / 2;

        for(int l = 0; l < k; ++l) {
            System.arraycopy(p_147953_0_, l * p_147953_1_, aint1, 0, p_147953_1_);
            System.arraycopy(p_147953_0_, (p_147953_2_ - 1 - l) * p_147953_1_, p_147953_0_, l * p_147953_1_, p_147953_1_);
            System.arraycopy(aint1, 0, p_147953_0_, (p_147953_2_ - 1 - l) * p_147953_1_, p_147953_1_);
        }

    }

    static {
        missingTextureData = missingTexture.getTextureData();
        field_147958_e = -1;
        field_147956_f = -1;
        field_152779_g = -1.0F;
        int var0 = -16777216;
        int var1 = -524040;
        int[] var2 = new int[]{-524040, -524040, -524040, -524040, -524040, -524040, -524040, -524040};
        int[] var3 = new int[]{-16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216};
        int var4 = var2.length;

        for(int var5 = 0; var5 < 16; ++var5) {
            System.arraycopy(var5 < var4 ? var2 : var3, 0, missingTextureData, 16 * var5, var4);
            System.arraycopy(var5 < var4 ? var3 : var2, 0, missingTextureData, 16 * var5 + var4, var4);
        }

        missingTexture.updateDynamicTexture();
        field_147957_g = new int[4];
    }

    @Overwrite
    @MixinMerged(
        mixin = "com.mitchej123.hodgepodge.mixins.early.minecraft.textures.client.MixinTextureUtil",
        priority = 1000,
        sessionId = "98d213e4-85ca-4ce4-959f-84858add440a"
    )
    public static int[][] generateMipmapData(int levels, int size, int[][] texture) {
        int[][] mipmaps = new int[levels + 1][];
        mipmaps[0] = texture[0];
        if (levels > 0) {
            boolean transparent = false;

            int level;
            for(level = 0; level < texture[0].length; ++level) {
                if (texture[0][level] >> 24 == 0) {
                    transparent = true;
                    break;
                }
            }

            for(level = 1; level <= levels; ++level) {
                if (texture[level] != null) {
                    mipmaps[level] = texture[level];
                } else {
                    int[] prevLevel = mipmaps[level - 1];
                    int width = size >> level;
                    if (width <= 0) {
                        mipmaps[level] = prevLevel;
                    } else {
                        int[] mipmap = new int[prevLevel.length >> 2];
                        int height = mipmap.length / width;
                        int prevWidth = width << 1;

                        for(int x = 0; x < width; ++x) {
                            for(int y = 0; y < height; ++y) {
                                int prevPos = 2 * (x + y * prevWidth);
                                mipmap[x + y * width] = func_147943_a(prevLevel[prevPos], prevLevel[prevPos + 1], prevLevel[prevPos + prevWidth], prevLevel[prevPos + 1 + prevWidth], transparent);
                            }
                        }

                        mipmaps[level] = mipmap;
                    }
                }
            }
        }

        return mipmaps;
    }

    @Overwrite
    @MixinMerged(
        mixin = "com.mitchej123.hodgepodge.mixins.early.minecraft.textures.client.MixinTextureUtil",
        priority = 1000,
        sessionId = "98d213e4-85ca-4ce4-959f-84858add440a"
    )
    private static int func_147943_a(int one, int two, int three, int four, boolean alpha) {
        int n;
        if (!alpha) {
            n = Mipmaps.getColorComponent(one, two, three, four, 24);
            int r = Mipmaps.getColorComponent(one, two, three, four, 16);
            int g = Mipmaps.getColorComponent(one, two, three, four, 8);
            int b = Mipmaps.getColorComponent(one, two, three, four, 0);
            return n << 24 | r << 16 | g << 8 | b;
        } else {
            n = 0;
            float a = 0.0F;
            float r = 0.0F;
            float g = 0.0F;
            float b = 0.0F;
            if (one >> 24 != 0) {
                a += Mipmaps.get(one >> 24);
                r += Mipmaps.get(one >> 16);
                g += Mipmaps.get(one >> 8);
                b += Mipmaps.get(one >> 0);
                ++n;
            }

            if (two >> 24 != 0) {
                a += Mipmaps.get(two >> 24);
                r += Mipmaps.get(two >> 16);
                g += Mipmaps.get(two >> 8);
                b += Mipmaps.get(two >> 0);
                ++n;
            }

            if (three >> 24 != 0) {
                a += Mipmaps.get(three >> 24);
                r += Mipmaps.get(three >> 16);
                g += Mipmaps.get(three >> 8);
                b += Mipmaps.get(three >> 0);
                ++n;
            }

            if (four >> 24 != 0) {
                a += Mipmaps.get(four >> 24);
                r += Mipmaps.get(four >> 16);
                g += Mipmaps.get(four >> 8);
                b += Mipmaps.get(four >> 0);
                ++n;
            }

            a /= 4.0F;
            if (n != 0) {
                r /= (float)n;
                g /= (float)n;
                b /= (float)n;
            }

            int ia = (int)(Math.pow((double)a, 0.45454545454545453D) * 255.0D);
            int ir = (int)(Math.pow((double)r, 0.45454545454545453D) * 255.0D);
            int ig = (int)(Math.pow((double)g, 0.45454545454545453D) * 255.0D);
            int ib = (int)(Math.pow((double)b, 0.45454545454545453D) * 255.0D);
            return ia << 24 | ir << 16 | ig << 8 | ib;
        }
    }
}
