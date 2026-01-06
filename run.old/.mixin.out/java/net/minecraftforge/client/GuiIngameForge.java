package net.minecraftforge.client;

import cpw.mods.fml.common.FMLCommonHandler;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.GuiPlayerInfo;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.Direction;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.FoodStats;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StringUtils;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Chat;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Post;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Text;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.transformer.meta.MixinMerged;

public class GuiIngameForge extends GuiIngame {
    private static final ResourceLocation WIDGITS = new ResourceLocation("textures/gui/widgets.png");
    private static final int WHITE = 16777215;
    public static boolean renderHelmet = true;
    public static boolean renderPortal = true;
    public static boolean renderHotbar = true;
    public static boolean renderCrosshairs = true;
    public static boolean renderBossHealth = true;
    public static boolean renderHealth = true;
    public static boolean renderArmor = true;
    public static boolean renderFood = true;
    public static boolean renderHealthMount = true;
    public static boolean renderAir = true;
    public static boolean renderExperiance = true;
    public static boolean renderJumpBar = true;
    public static boolean renderObjective = true;
    public static int left_height = 39;
    public static int right_height = 39;
    private ScaledResolution res = null;
    private FontRenderer fontrenderer = null;
    private RenderGameOverlayEvent eventParent;
    private static final String MC_VERSION = "1.7.10";

    public GuiIngameForge(Minecraft mc) {
        super(mc);
    }

    public void renderGameOverlay(float partialTicks, boolean hasScreen, int mouseX, int mouseY) {
        this.res = new ScaledResolution(this.mc, this.mc.displayWidth, this.mc.displayHeight);
        this.eventParent = new RenderGameOverlayEvent(partialTicks, this.res, mouseX, mouseY);
        int width = this.res.getScaledWidth();
        int height = this.res.getScaledHeight();
        renderHealthMount = this.mc.thePlayer.ridingEntity instanceof EntityLivingBase;
        renderFood = this.mc.thePlayer.ridingEntity == null;
        renderJumpBar = this.mc.thePlayer.isRidingHorse();
        right_height = 39;
        left_height = 39;
        if (!this.pre(ElementType.ALL)) {
            this.fontrenderer = this.mc.fontRenderer;
            this.mc.entityRenderer.setupOverlayRendering();
            GL11.glEnable(3042);
            if (Minecraft.isFancyGraphicsEnabled()) {
                this.renderVignette(this.mc.thePlayer.getBrightness(partialTicks), width, height);
            } else {
                OpenGlHelper.glBlendFunc(770, 771, 1, 0);
            }

            if (renderHelmet) {
                this.renderHelmet(this.res, partialTicks, hasScreen, mouseX, mouseY);
            }

            if (renderPortal && !this.mc.thePlayer.isPotionActive(Potion.confusion)) {
                this.renderPortal(width, height, partialTicks);
            }

            if (!this.mc.playerController.enableEverythingIsScrewedUpMode()) {
                GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                this.zLevel = -90.0F;
                this.rand.setSeed((long)(this.updateCounter * 312871));
                if (renderCrosshairs) {
                    this.renderCrosshairs(width, height);
                }

                if (renderBossHealth) {
                    this.renderBossHealth();
                }

                if (this.mc.playerController.shouldDrawHUD()) {
                    if (renderHealth) {
                        this.renderHealth(width, height);
                    }

                    if (renderArmor) {
                        this.renderArmor(width, height);
                    }

                    if (renderFood) {
                        this.renderFood(width, height);
                    }

                    if (renderHealthMount) {
                        this.renderHealthMount(width, height);
                    }

                    if (renderAir) {
                        this.renderAir(width, height);
                    }
                }

                if (renderHotbar) {
                    this.renderHotbar(width, height, partialTicks);
                }
            }

            if (renderJumpBar) {
                this.renderJumpBar(width, height);
            } else if (renderExperiance) {
                this.renderExperience(width, height);
            }

            this.renderSleepFade(width, height);
            this.renderToolHightlight(width, height);
            this.renderHUDText(width, height);
            this.renderRecordOverlay(width, height, partialTicks);
            ScoreObjective objective = this.mc.theWorld.getScoreboard().func_96539_a(1);
            if (renderObjective && objective != null) {
                this.func_96136_a(objective, height, width, this.fontrenderer);
            }

            GL11.glEnable(3042);
            OpenGlHelper.glBlendFunc(770, 771, 1, 0);
            GL11.glDisable(3008);
            this.renderChat(width, height);
            this.renderPlayerList(width, height);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            GL11.glDisable(2896);
            GL11.glEnable(3008);
            this.post(ElementType.ALL);
        }
    }

    public ScaledResolution getResolution() {
        return this.res;
    }

    protected void renderHotbar(int width, int height, float partialTicks) {
        if (!this.pre(ElementType.HOTBAR)) {
            this.mc.mcProfiler.startSection("actionBar");
            GL11.glEnable(3042);
            GL11.glBlendFunc(770, 771);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            this.mc.renderEngine.bindTexture(WIDGITS);
            InventoryPlayer inv = this.mc.thePlayer.inventory;
            this.drawTexturedModalRect(width / 2 - 91, height - 22, 0, 0, 182, 22);
            this.drawTexturedModalRect(width / 2 - 91 - 1 + inv.currentItem * 20, height - 22 - 1, 0, 22, 24, 22);
            GL11.glDisable(3042);
            GL11.glEnable(32826);
            RenderHelper.enableGUIStandardItemLighting();

            for(int i = 0; i < 9; ++i) {
                int x = width / 2 - 90 + i * 20 + 2;
                int z = height - 16 - 3;
                this.renderInventorySlot(i, x, z, partialTicks);
            }

            RenderHelper.disableStandardItemLighting();
            GL11.glDisable(32826);
            this.mc.mcProfiler.endSection();
            this.post(ElementType.HOTBAR);
        }
    }

    protected void renderCrosshairs(int width, int height) {
        CallbackInfo callbackInfo3 = new CallbackInfo("renderCrosshairs", true);
        this.handler$zfi000$hodgepodge$hideCrosshairThirdPerson(width, height, callbackInfo3);
        if (!callbackInfo3.isCancelled()) {
            if (!this.pre(ElementType.CROSSHAIRS)) {
                this.bind(Gui.icons);
                GL11.glEnable(3042);
                OpenGlHelper.glBlendFunc(775, 769, 1, 0);
                this.drawTexturedModalRect(width / 2 - 7, height / 2 - 7, 0, 0, 16, 16);
                OpenGlHelper.glBlendFunc(770, 771, 1, 0);
                GL11.glDisable(3042);
                this.post(ElementType.CROSSHAIRS);
            }
        }
    }

    protected void renderBossHealth() {
        if (!this.pre(ElementType.BOSSHEALTH)) {
            this.mc.mcProfiler.startSection("bossHealth");
            GL11.glEnable(3042);
            super.renderBossHealth();
            GL11.glDisable(3042);
            this.mc.mcProfiler.endSection();
            this.post(ElementType.BOSSHEALTH);
        }
    }

    private void renderHelmet(ScaledResolution res, float partialTicks, boolean hasScreen, int mouseX, int mouseY) {
        if (!this.pre(ElementType.HELMET)) {
            ItemStack itemstack = this.mc.thePlayer.inventory.armorItemInSlot(3);
            if (this.mc.gameSettings.thirdPersonView == 0 && itemstack != null && itemstack.getItem() != null) {
                if (itemstack.getItem() == Item.getItemFromBlock(Blocks.pumpkin)) {
                    this.renderPumpkinBlur(res.getScaledWidth(), res.getScaledHeight());
                } else {
                    itemstack.getItem().renderHelmetOverlay(itemstack, this.mc.thePlayer, res, partialTicks, hasScreen, mouseX, mouseY);
                }
            }

            this.post(ElementType.HELMET);
        }
    }

    protected void renderArmor(int width, int height) {
        if (!this.pre(ElementType.ARMOR)) {
            this.mc.mcProfiler.startSection("armor");
            GL11.glEnable(3042);
            int left = width / 2 - 91;
            int top = height - left_height;
            int level = ForgeHooks.getTotalArmorValue(this.mc.thePlayer);

            for(int i = 1; level > 0 && i < 20; i += 2) {
                if (i < level) {
                    this.drawTexturedModalRect(left, top, 34, 9, 9, 9);
                } else if (i == level) {
                    this.drawTexturedModalRect(left, top, 25, 9, 9, 9);
                } else if (i > level) {
                    this.drawTexturedModalRect(left, top, 16, 9, 9, 9);
                }

                left += 8;
            }

            left_height += 10;
            GL11.glDisable(3042);
            this.mc.mcProfiler.endSection();
            this.post(ElementType.ARMOR);
        }
    }

    protected void renderPortal(int width, int height, float partialTicks) {
        if (!this.pre(ElementType.PORTAL)) {
            float f1 = this.mc.thePlayer.prevTimeInPortal + (this.mc.thePlayer.timeInPortal - this.mc.thePlayer.prevTimeInPortal) * partialTicks;
            if (f1 > 0.0F) {
                this.func_130015_b(f1, width, height);
            }

            this.post(ElementType.PORTAL);
        }
    }

    protected void renderAir(int width, int height) {
        if (!this.pre(ElementType.AIR)) {
            this.mc.mcProfiler.startSection("air");
            GL11.glEnable(3042);
            int left = width / 2 + 91;
            int top = height - right_height;
            if (this.mc.thePlayer.isInsideOfMaterial(Material.water)) {
                int air = this.mc.thePlayer.getAir();
                int full = MathHelper.ceiling_double_int((double)(air - 2) * 10.0D / 300.0D);
                int partial = MathHelper.ceiling_double_int((double)air * 10.0D / 300.0D) - full;

                for(int i = 0; i < full + partial; ++i) {
                    this.drawTexturedModalRect(left - i * 8 - 9, top, i < full ? 16 : 25, 18, 9, 9);
                }

                right_height += 10;
            }

            GL11.glDisable(3042);
            this.mc.mcProfiler.endSection();
            this.post(ElementType.AIR);
        }
    }

    public void renderHealth(int width, int height) {
        this.bind(icons);
        if (!this.pre(ElementType.HEALTH)) {
            this.mc.mcProfiler.startSection("health");
            GL11.glEnable(3042);
            boolean highlight = this.mc.thePlayer.hurtResistantTime / 3 % 2 == 1;
            if (this.mc.thePlayer.hurtResistantTime < 10) {
                highlight = false;
            }

            IAttributeInstance attrMaxHealth = this.mc.thePlayer.getEntityAttribute(SharedMonsterAttributes.maxHealth);
            int health = MathHelper.ceiling_float_int(this.mc.thePlayer.getHealth());
            int healthLast = MathHelper.ceiling_float_int(this.mc.thePlayer.prevHealth);
            float healthMax = (float)attrMaxHealth.getAttributeValue();
            float absorb = this.mc.thePlayer.getAbsorptionAmount();
            int healthRows = MathHelper.ceiling_float_int((healthMax + absorb) / 2.0F / 10.0F);
            int rowHeight = Math.max(10 - (healthRows - 2), 3);
            this.rand.setSeed((long)(this.updateCounter * 312871));
            int left = width / 2 - 91;
            int top = height - left_height;
            left_height += healthRows * rowHeight;
            if (rowHeight != 10) {
                left_height += 10 - rowHeight;
            }

            int regen = -1;
            if (this.mc.thePlayer.isPotionActive(Potion.regeneration)) {
                regen = this.updateCounter % 25;
            }

            int TOP = 9 * (this.mc.theWorld.getWorldInfo().isHardcoreModeEnabled() ? 5 : 0);
            int BACKGROUND = highlight ? 25 : 16;
            int MARGIN = 16;
            if (this.mc.thePlayer.isPotionActive(Potion.poison)) {
                MARGIN += 36;
            } else if (this.mc.thePlayer.isPotionActive(Potion.wither)) {
                MARGIN += 72;
            }

            float absorbRemaining = absorb;

            for(int i = MathHelper.ceiling_float_int((healthMax + absorb) / 2.0F) - 1; i >= 0; --i) {
                int row = MathHelper.ceiling_float_int((float)(i + 1) / 10.0F) - 1;
                int x = left + i % 10 * 8;
                int y = top - row * rowHeight;
                if (health <= 4) {
                    y += this.rand.nextInt(2);
                }

                if (i == regen) {
                    y -= 2;
                }

                this.drawTexturedModalRect(x, y, BACKGROUND, TOP, 9, 9);
                if (highlight) {
                    if (i * 2 + 1 < healthLast) {
                        this.drawTexturedModalRect(x, y, MARGIN + 54, TOP, 9, 9);
                    } else if (i * 2 + 1 == healthLast) {
                        this.drawTexturedModalRect(x, y, MARGIN + 63, TOP, 9, 9);
                    }
                }

                if (absorbRemaining <= 0.0F) {
                    if (i * 2 + 1 < health) {
                        this.drawTexturedModalRect(x, y, MARGIN + 36, TOP, 9, 9);
                    } else if (i * 2 + 1 == health) {
                        this.drawTexturedModalRect(x, y, MARGIN + 45, TOP, 9, 9);
                    }
                } else {
                    if (absorbRemaining == absorb && absorb % 2.0F == 1.0F) {
                        this.drawTexturedModalRect(x, y, MARGIN + 153, TOP, 9, 9);
                    } else {
                        this.drawTexturedModalRect(x, y, MARGIN + 144, TOP, 9, 9);
                    }

                    absorbRemaining -= 2.0F;
                }
            }

            GL11.glDisable(3042);
            this.mc.mcProfiler.endSection();
            this.post(ElementType.HEALTH);
        }
    }

    public void renderFood(int width, int height) {
        if (!this.pre(ElementType.FOOD)) {
            this.mc.mcProfiler.startSection("food");
            GL11.glEnable(3042);
            int left = width / 2 + 91;
            int top = height - right_height;
            right_height += 10;
            boolean unused = false;
            FoodStats stats = this.mc.thePlayer.getFoodStats();
            int level = stats.getFoodLevel();
            int levelLast = stats.getPrevFoodLevel();

            for(int i = 0; i < 10; ++i) {
                int idx = i * 2 + 1;
                int x = left - i * 8 - 9;
                int y = top;
                int icon = 16;
                byte backgound = 0;
                if (this.mc.thePlayer.isPotionActive(Potion.hunger)) {
                    icon += 36;
                    backgound = 13;
                }

                if (unused) {
                    backgound = 1;
                }

                if (this.mc.thePlayer.getFoodStats().getSaturationLevel() <= 0.0F && this.updateCounter % (level * 3 + 1) == 0) {
                    y = top + (this.rand.nextInt(3) - 1);
                }

                this.drawTexturedModalRect(x, y, 16 + backgound * 9, 27, 9, 9);
                if (unused) {
                    if (idx < levelLast) {
                        this.drawTexturedModalRect(x, y, icon + 54, 27, 9, 9);
                    } else if (idx == levelLast) {
                        this.drawTexturedModalRect(x, y, icon + 63, 27, 9, 9);
                    }
                }

                if (idx < level) {
                    this.drawTexturedModalRect(x, y, icon + 36, 27, 9, 9);
                } else if (idx == level) {
                    this.drawTexturedModalRect(x, y, icon + 45, 27, 9, 9);
                }
            }

            GL11.glDisable(3042);
            this.mc.mcProfiler.endSection();
            this.post(ElementType.FOOD);
        }
    }

    protected void renderSleepFade(int width, int height) {
        if (this.mc.thePlayer.getSleepTimer() > 0) {
            this.mc.mcProfiler.startSection("sleep");
            GL11.glDisable(2929);
            GL11.glDisable(3008);
            int sleepTime = this.mc.thePlayer.getSleepTimer();
            float opacity = (float)sleepTime / 100.0F;
            if (opacity > 1.0F) {
                opacity = 1.0F - (float)(sleepTime - 100) / 10.0F;
            }

            int color = (int)(220.0F * opacity) << 24 | 1052704;
            drawRect(0, 0, width, height, color);
            GL11.glEnable(3008);
            GL11.glEnable(2929);
            this.mc.mcProfiler.endSection();
        }

    }

    protected void renderExperience(int width, int height) {
        this.bind(icons);
        if (!this.pre(ElementType.EXPERIENCE)) {
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            GL11.glDisable(3042);
            if (this.mc.playerController.gameIsSurvivalOrAdventure()) {
                this.mc.mcProfiler.startSection("expBar");
                int cap = this.mc.thePlayer.xpBarCap();
                int left = width / 2 - 91;
                int color;
                if (cap > 0) {
                    short barWidth = 182;
                    color = (int)(this.mc.thePlayer.experience * (float)(barWidth + 1));
                    int top = height - 32 + 3;
                    this.drawTexturedModalRect(left, top, 0, 64, barWidth, 5);
                    if (color > 0) {
                        this.drawTexturedModalRect(left, top, 0, 69, color, 5);
                    }
                }

                this.mc.mcProfiler.endSection();
                if (this.mc.playerController.gameIsSurvivalOrAdventure() && this.mc.thePlayer.experienceLevel > 0) {
                    this.mc.mcProfiler.startSection("expLevel");
                    boolean flag1 = false;
                    color = flag1 ? 16777215 : 8453920;
                    String text = "" + this.mc.thePlayer.experienceLevel;
                    int x = (width - this.fontrenderer.getStringWidth(text)) / 2;
                    int y = height - 31 - 4;
                    this.fontrenderer.drawString(text, x + 1, y, 0);
                    this.fontrenderer.drawString(text, x - 1, y, 0);
                    this.fontrenderer.drawString(text, x, y + 1, 0);
                    this.fontrenderer.drawString(text, x, y - 1, 0);
                    this.fontrenderer.drawString(text, x, y, color);
                    this.mc.mcProfiler.endSection();
                }
            }

            GL11.glEnable(3042);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            this.post(ElementType.EXPERIENCE);
        }
    }

    protected void renderJumpBar(int width, int height) {
        this.bind(icons);
        if (!this.pre(ElementType.JUMPBAR)) {
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            GL11.glDisable(3042);
            this.mc.mcProfiler.startSection("jumpBar");
            float charge = this.mc.thePlayer.getHorseJumpPower();
            int barWidth = true;
            int x = width / 2 - 91;
            int filled = (int)(charge * 183.0F);
            int top = height - 32 + 3;
            this.drawTexturedModalRect(x, top, 0, 84, 182, 5);
            if (filled > 0) {
                this.drawTexturedModalRect(x, top, 0, 89, filled, 5);
            }

            GL11.glEnable(3042);
            this.mc.mcProfiler.endSection();
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            this.post(ElementType.JUMPBAR);
        }
    }

    protected void renderToolHightlight(int width, int height) {
        if (this.mc.gameSettings.heldItemTooltips) {
            this.mc.mcProfiler.startSection("toolHighlight");
            if (this.remainingHighlightTicks > 0 && this.highlightingItemStack != null) {
                String name = this.highlightingItemStack.getDisplayName();
                int opacity = (int)((float)this.remainingHighlightTicks * 256.0F / 10.0F);
                if (opacity > 255) {
                    opacity = 255;
                }

                if (opacity > 0) {
                    int y = height - 59;
                    if (!this.mc.playerController.shouldDrawHUD()) {
                        y += 14;
                    }

                    GL11.glPushMatrix();
                    GL11.glEnable(3042);
                    OpenGlHelper.glBlendFunc(770, 771, 1, 0);
                    FontRenderer font = this.highlightingItemStack.getItem().getFontRenderer(this.highlightingItemStack);
                    int x;
                    if (font != null) {
                        x = (width - font.getStringWidth(name)) / 2;
                        font.drawStringWithShadow(name, x, y, 16777215 | opacity << 24);
                    } else {
                        x = (width - this.fontrenderer.getStringWidth(name)) / 2;
                        this.fontrenderer.drawStringWithShadow(name, x, y, 16777215 | opacity << 24);
                    }

                    GL11.glDisable(3042);
                    GL11.glPopMatrix();
                }
            }

            this.mc.mcProfiler.endSection();
        }

    }

    protected void renderHUDText(int width, int height) {
        this.mc.mcProfiler.startSection("forgeHudText");
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        ArrayList<String> left = new ArrayList();
        ArrayList<String> right = new ArrayList();
        long max;
        if (this.mc.isDemo()) {
            max = this.mc.theWorld.getTotalWorldTime();
            if (max >= 120500L) {
                right.add(I18n.format("demo.demoExpired", new Object[0]));
            } else {
                right.add(I18n.format("demo.remainingTime", new Object[]{StringUtils.ticksToElapsedTime((int)(120500L - max))}));
            }
        }

        if (this.mc.gameSettings.showDebugInfo && !this.pre(ElementType.DEBUG)) {
            this.mc.mcProfiler.startSection("debug");
            GL11.glPushMatrix();
            left.add("Minecraft 1.7.10 (" + this.mc.debug + ")");
            left.add(this.mc.debugInfoRenders());
            left.add(this.mc.getEntityDebug());
            left.add(this.mc.debugInfoEntities());
            left.add(this.mc.getWorldProviderName());
            left.add((Object)null);
            max = Runtime.getRuntime().maxMemory();
            long total = Runtime.getRuntime().totalMemory();
            long free = Runtime.getRuntime().freeMemory();
            long used = total - free;
            right.add("Used memory: " + used * 100L / max + "% (" + used / 1024L / 1024L + "MB) of " + max / 1024L / 1024L + "MB");
            right.add("Allocated memory: " + total * 100L / max + "% (" + total / 1024L / 1024L + "MB)");
            int x = MathHelper.floor_double(this.mc.thePlayer.posX);
            int y = MathHelper.floor_double(this.mc.thePlayer.posY);
            int z = MathHelper.floor_double(this.mc.thePlayer.posZ);
            float yaw = this.mc.thePlayer.rotationYaw;
            int heading = MathHelper.floor_double((double)(this.mc.thePlayer.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
            left.add(String.format("x: %.5f (%d) // c: %d (%d)", this.mc.thePlayer.posX, x, x >> 4, x & 15));
            left.add(String.format("y: %.3f (feet pos, %.3f eyes pos)", this.mc.thePlayer.boundingBox.minY, this.mc.thePlayer.posY));
            left.add(String.format("z: %.5f (%d) // c: %d (%d)", this.mc.thePlayer.posZ, z, z >> 4, z & 15));
            left.add(String.format("f: %d (%s) / %f", heading, Direction.directions[heading], MathHelper.wrapAngleTo180_float(yaw)));
            if (this.mc.theWorld != null && this.mc.theWorld.blockExists(x, y, z)) {
                Chunk chunk = this.mc.theWorld.getChunkFromBlockCoords(x, z);
                left.add(String.format("lc: %d b: %s bl: %d sl: %d rl: %d", chunk.getTopFilledSegment() + 15, chunk.getBiomeGenForWorldCoords(x & 15, z & 15, this.mc.theWorld.getWorldChunkManager()).biomeName, chunk.getSavedLightValue(EnumSkyBlock.Block, x & 15, y, z & 15), chunk.getSavedLightValue(EnumSkyBlock.Sky, x & 15, y, z & 15), chunk.getBlockLightValue(x & 15, y, z & 15, 0)));
            } else {
                left.add((Object)null);
            }

            left.add(String.format("ws: %.3f, fs: %.3f, g: %b, fl: %d", this.mc.thePlayer.capabilities.getWalkSpeed(), this.mc.thePlayer.capabilities.getFlySpeed(), this.mc.thePlayer.onGround, this.mc.theWorld.getHeightValue(x, z)));
            if (this.mc.entityRenderer != null && this.mc.entityRenderer.isShaderActive()) {
                left.add(String.format("shader: %s", this.mc.entityRenderer.getShaderGroup().getShaderGroupName()));
            }

            right.add((Object)null);
            Iterator var22 = FMLCommonHandler.instance().getBrandings(false).iterator();

            while(var22.hasNext()) {
                String brand = (String)var22.next();
                right.add(brand);
            }

            GL11.glPopMatrix();
            this.mc.mcProfiler.endSection();
            this.post(ElementType.DEBUG);
        }

        Text event = new Text(this.eventParent, left, right);
        if (!MinecraftForge.EVENT_BUS.post(event)) {
            int x;
            String msg;
            for(x = 0; x < left.size(); ++x) {
                msg = (String)left.get(x);
                if (msg != null) {
                    this.fontrenderer.drawStringWithShadow(msg, 2, 2 + x * 10, 16777215);
                }
            }

            for(x = 0; x < right.size(); ++x) {
                msg = (String)right.get(x);
                if (msg != null) {
                    int w = this.fontrenderer.getStringWidth(msg);
                    this.fontrenderer.drawStringWithShadow(msg, width - w - 10, 2 + x * 10, 16777215);
                }
            }
        }

        this.mc.mcProfiler.endSection();
        this.post(ElementType.TEXT);
    }

    protected void renderRecordOverlay(int width, int height, float partialTicks) {
        if (this.recordPlayingUpFor > 0) {
            this.mc.mcProfiler.startSection("overlayMessage");
            float hue = (float)this.recordPlayingUpFor - partialTicks;
            int opacity = (int)(hue * 256.0F / 20.0F);
            if (opacity > 255) {
                opacity = 255;
            }

            if (opacity > 0) {
                GL11.glPushMatrix();
                GL11.glTranslatef((float)(width / 2), (float)(height - 48), 0.0F);
                GL11.glEnable(3042);
                OpenGlHelper.glBlendFunc(770, 771, 1, 0);
                int color = this.recordIsPlaying ? Color.HSBtoRGB(hue / 50.0F, 0.7F, 0.6F) & 16777215 : 16777215;
                this.fontrenderer.drawString(this.recordPlaying, -this.fontrenderer.getStringWidth(this.recordPlaying) / 2, -4, color | opacity << 24);
                GL11.glDisable(3042);
                GL11.glPopMatrix();
            }

            this.mc.mcProfiler.endSection();
        }

    }

    protected void renderChat(int width, int height) {
        this.mc.mcProfiler.startSection("chat");
        Chat event = new Chat(this.eventParent, 0, height - 48);
        if (!MinecraftForge.EVENT_BUS.post(event)) {
            GL11.glPushMatrix();
            GL11.glTranslatef((float)event.posX, (float)event.posY, 0.0F);
            this.persistantChatGUI.drawChat(this.updateCounter);
            GL11.glPopMatrix();
            this.post(ElementType.CHAT);
            this.mc.mcProfiler.endSection();
        }
    }

    protected void renderPlayerList(int width, int height) {
        ScoreObjective scoreobjective = this.mc.theWorld.getScoreboard().func_96539_a(0);
        NetHandlerPlayClient handler = this.mc.thePlayer.sendQueue;
        if (this.mc.gameSettings.keyBindPlayerList.getIsKeyPressed() && (!this.mc.isIntegratedServerRunning() || handler.playerInfoList.size() > 1 || scoreobjective != null)) {
            if (this.pre(ElementType.PLAYER_LIST)) {
                return;
            }

            this.mc.mcProfiler.startSection("playerList");
            List<GuiPlayerInfo> players = handler.playerInfoList;
            int maxPlayers = handler.currentServerMaxPlayers;
            int rows = maxPlayers;
            int columns = true;

            int columns;
            for(columns = 1; rows > 20; rows = (maxPlayers + columns - 1) / columns) {
                ++columns;
            }

            int columnWidth = 300 / columns;
            if (columnWidth > 150) {
                columnWidth = 150;
            }

            int left = (width - columns * columnWidth) / 2;
            byte border = 10;
            drawRect(left - 1, border - 1, left + columnWidth * columns, border + 9 * rows, Integer.MIN_VALUE);

            for(int i = 0; i < maxPlayers; ++i) {
                int xPos = left + i % columns * columnWidth;
                int yPos = border + i / columns * 9;
                drawRect(xPos, yPos, xPos + columnWidth - 1, yPos + 8, 553648127);
                GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                GL11.glEnable(3008);
                if (i < players.size()) {
                    GuiPlayerInfo player = (GuiPlayerInfo)players.get(i);
                    ScorePlayerTeam team = this.mc.theWorld.getScoreboard().getPlayersTeam(player.name);
                    String displayName = ScorePlayerTeam.formatPlayerName(team, player.name);
                    this.fontrenderer.drawStringWithShadow(displayName, xPos, yPos, 16777215);
                    int ping;
                    if (scoreobjective != null) {
                        int endX = xPos + this.fontrenderer.getStringWidth(displayName) + 5;
                        ping = xPos + columnWidth - 12 - 5;
                        if (ping - endX > 5) {
                            Score score = scoreobjective.getScoreboard().func_96529_a(player.name, scoreobjective);
                            String scoreDisplay = EnumChatFormatting.YELLOW + "" + score.getScorePoints();
                            this.fontrenderer.drawStringWithShadow(scoreDisplay, ping - this.fontrenderer.getStringWidth(scoreDisplay), yPos, 16777215);
                        }
                    }

                    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                    this.mc.getTextureManager().bindTexture(Gui.icons);
                    int pingIndex = 4;
                    ping = player.responseTime;
                    if (ping < 0) {
                        pingIndex = 5;
                    } else if (ping < 150) {
                        pingIndex = 0;
                    } else if (ping < 300) {
                        pingIndex = 1;
                    } else if (ping < 600) {
                        pingIndex = 2;
                    } else if (ping < 1000) {
                        pingIndex = 3;
                    }

                    this.zLevel += 100.0F;
                    this.drawTexturedModalRect(xPos + columnWidth - 12, yPos, 0, 176 + pingIndex * 8, 10, 8);
                    this.zLevel -= 100.0F;
                }
            }

            this.post(ElementType.PLAYER_LIST);
        }

    }

    protected void renderHealthMount(int width, int height) {
        Entity tmp = this.mc.thePlayer.ridingEntity;
        if (tmp instanceof EntityLivingBase) {
            this.bind(icons);
            if (!this.pre(ElementType.HEALTHMOUNT)) {
                boolean unused = false;
                int left_align = width / 2 + 91;
                this.mc.mcProfiler.endStartSection("mountHealth");
                GL11.glEnable(3042);
                EntityLivingBase mount = (EntityLivingBase)tmp;
                int health = (int)Math.ceil((double)mount.getHealth());
                float healthMax = mount.getMaxHealth();
                int hearts = (int)(healthMax + 0.5F) / 2;
                if (hearts > 30) {
                    hearts = 30;
                }

                int MARGIN = true;
                int BACKGROUND = 52 + (unused ? 1 : 0);
                int HALF = true;
                int FULL = true;

                for(int heart = 0; hearts > 0; heart += 20) {
                    int top = height - right_height;
                    int rowCount = Math.min(hearts, 10);
                    hearts -= rowCount;

                    for(int i = 0; i < rowCount; ++i) {
                        int x = left_align - i * 8 - 9;
                        this.drawTexturedModalRect(x, top, BACKGROUND, 9, 9, 9);
                        if (i * 2 + 1 + heart < health) {
                            this.drawTexturedModalRect(x, top, 88, 9, 9, 9);
                        } else if (i * 2 + 1 + heart == health) {
                            this.drawTexturedModalRect(x, top, 97, 9, 9, 9);
                        }
                    }

                    right_height += 10;
                }

                GL11.glDisable(3042);
                this.post(ElementType.HEALTHMOUNT);
            }
        }
    }

    private boolean pre(ElementType type) {
        return MinecraftForge.EVENT_BUS.post(new Pre(this.eventParent, type));
    }

    private void post(ElementType type) {
        MinecraftForge.EVENT_BUS.post(new Post(this.eventParent, type));
    }

    private void bind(ResourceLocation res) {
        this.mc.getTextureManager().bindTexture(res);
    }

    @MixinMerged(
        mixin = "com.mitchej123.hodgepodge.mixins.early.forge.MixinGuiIngameForge_CrosshairThirdPerson",
        priority = 1000,
        sessionId = "98d213e4-85ca-4ce4-959f-84858add440a"
    )
    public void handler$zfi000$hodgepodge$hideCrosshairThirdPerson(int width, int height, CallbackInfo ci) {
        if (this.mc.gameSettings.thirdPersonView != 0) {
            ci.cancel();
        }

    }
}
