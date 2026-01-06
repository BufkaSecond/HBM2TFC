package net.minecraft.client.gui;

import com.gtnewhorizon.gtnhlib.client.event.RenderTooltipEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.awt.Toolkit;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.GuiScreenEvent.ActionPerformedEvent.Post;
import net.minecraftforge.client.event.GuiScreenEvent.ActionPerformedEvent.Pre;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.transformer.meta.MixinMerged;

@SideOnly(Side.CLIENT)
public class GuiScreen extends Gui {
    protected static RenderItem itemRender = new RenderItem();
    public Minecraft mc;
    public int width;
    public int height;
    protected List buttonList = new ArrayList();
    protected List labelList = new ArrayList();
    public boolean allowUserInput;
    public FontRenderer fontRendererObj;
    private GuiButton selectedButton;
    private int eventButton;
    private long lastMouseEvent;
    private int field_146298_h;
    private static final String __OBFID = "CL_00000710";
    @Unique
    @MixinMerged(
        mixin = "com.gtnewhorizon.gtnhlib.mixins.early.MixinGuiScreen",
        priority = 999,
        sessionId = "98d213e4-85ca-4ce4-959f-84858add440a"
    )
    private ItemStack gtnhlib$currentStack;

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        int k;
        for(k = 0; k < this.buttonList.size(); ++k) {
            ((GuiButton)this.buttonList.get(k)).drawButton(this.mc, mouseX, mouseY);
        }

        for(k = 0; k < this.labelList.size(); ++k) {
            ((GuiLabel)this.labelList.get(k)).func_146159_a(this.mc, mouseX, mouseY);
        }

    }

    protected void keyTyped(char typedChar, int keyCode) {
        if (keyCode == 1) {
            this.mc.displayGuiScreen((GuiScreen)null);
            this.mc.setIngameFocus();
        }

    }

    public static String getClipboardString() {
        try {
            Transferable transferable = Toolkit.getDefaultToolkit().getSystemClipboard().getContents((Object)null);
            if (transferable != null && transferable.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                return (String)transferable.getTransferData(DataFlavor.stringFlavor);
            }
        } catch (Exception var1) {
        }

        return "";
    }

    public static void setClipboardString(String copyText) {
        try {
            StringSelection stringselection = new StringSelection(copyText);
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringselection, (ClipboardOwner)null);
        } catch (Exception var2) {
        }

    }

    protected void renderToolTip(ItemStack itemIn, int x, int y) {
        this.handler$zjf000$gtnhlib$preRenderToolTip(itemIn, x, y, (CallbackInfo)null);
        List list = itemIn.getTooltip(this.mc.thePlayer, this.mc.gameSettings.advancedItemTooltips);

        for(int k = 0; k < list.size(); ++k) {
            if (k == 0) {
                list.set(k, itemIn.getRarity().rarityColor + (String)list.get(k));
            } else {
                list.set(k, EnumChatFormatting.GRAY + (String)list.get(k));
            }
        }

        FontRenderer font = itemIn.getItem().getFontRenderer(itemIn);
        this.drawHoveringText(list, x, y, font == null ? this.fontRendererObj : font);
        this.handler$zjf000$gtnhlib$postRenderToolTip((CallbackInfo)null);
    }

    protected void drawCreativeTabHoveringText(String tabName, int mouseX, int mouseY) {
        this.func_146283_a(Arrays.asList(tabName), mouseX, mouseY);
    }

    protected void func_146283_a(List textLines, int x, int y) {
        this.drawHoveringText(textLines, x, y, this.fontRendererObj);
    }

    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0) {
            for(int l = 0; l < this.buttonList.size(); ++l) {
                GuiButton guibutton = (GuiButton)this.buttonList.get(l);
                if (guibutton.mousePressed(this.mc, mouseX, mouseY)) {
                    Pre event = new Pre(this, guibutton, this.buttonList);
                    if (MinecraftForge.EVENT_BUS.post(event)) {
                        break;
                    }

                    this.selectedButton = event.button;
                    event.button.func_146113_a(this.mc.getSoundHandler());
                    this.actionPerformed(event.button);
                    if (this.equals(this.mc.currentScreen)) {
                        MinecraftForge.EVENT_BUS.post(new Post(this, event.button, this.buttonList));
                    }
                }
            }
        }

    }

    protected void mouseMovedOrUp(int mouseX, int mouseY, int state) {
        if (this.selectedButton != null && state == 0) {
            this.selectedButton.mouseReleased(mouseX, mouseY);
            this.selectedButton = null;
        }

    }

    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
    }

    protected void actionPerformed(GuiButton button) {
    }

    public void setWorldAndResolution(Minecraft mc, int width, int height) {
        this.mc = mc;
        this.fontRendererObj = mc.fontRenderer;
        this.width = width;
        this.height = height;
        if (!MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.GuiScreenEvent.InitGuiEvent.Pre(this, this.buttonList))) {
            this.buttonList.clear();
            this.initGui();
        }

        MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.GuiScreenEvent.InitGuiEvent.Post(this, this.buttonList));
    }

    public void initGui() {
    }

    public void handleInput() {
        if (Mouse.isCreated()) {
            while(Mouse.next()) {
                this.handleMouseInput();
            }
        }

        if (Keyboard.isCreated()) {
            while(Keyboard.next()) {
                this.handleKeyboardInput();
            }
        }

    }

    public void handleMouseInput() {
        int i = Mouse.getEventX() * this.width / this.mc.displayWidth;
        int j = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
        int k = Mouse.getEventButton();
        if (Mouse.getEventButtonState()) {
            if (this.mc.gameSettings.touchscreen && this.field_146298_h++ > 0) {
                return;
            }

            this.eventButton = k;
            this.lastMouseEvent = Minecraft.getSystemTime();
            this.mouseClicked(i, j, this.eventButton);
        } else if (k != -1) {
            if (this.mc.gameSettings.touchscreen && --this.field_146298_h > 0) {
                return;
            }

            this.eventButton = -1;
            this.mouseMovedOrUp(i, j, k);
        } else if (this.eventButton != -1 && this.lastMouseEvent > 0L) {
            long l = Minecraft.getSystemTime() - this.lastMouseEvent;
            this.mouseClickMove(i, j, this.eventButton, l);
        }

    }

    public void handleKeyboardInput() {
        CallbackInfo callbackInfo1 = new CallbackInfo("handleKeyboardInput", true);
        this.handler$zzf000$lwjgl3ify$handleKeyboardInput(callbackInfo1);
        if (!callbackInfo1.isCancelled()) {
            if (Keyboard.getEventKeyState()) {
                this.keyTyped(Keyboard.getEventCharacter(), Keyboard.getEventKey());
            }

            this.mc.func_152348_aa();
        }
    }

    public void updateScreen() {
    }

    public void onGuiClosed() {
    }

    public void drawDefaultBackground() {
        this.drawWorldBackground(0);
    }

    public void drawWorldBackground(int tint) {
        if (this.mc.theWorld != null) {
            this.drawGradientRect(0, 0, this.width, this.height, -1072689136, -804253680);
        } else {
            this.drawBackground(tint);
        }

    }

    public void drawBackground(int tint) {
        GL11.glDisable(2896);
        GL11.glDisable(2912);
        Tessellator tessellator = Tessellator.instance;
        this.mc.getTextureManager().bindTexture(optionsBackground);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        float f = 32.0F;
        tessellator.startDrawingQuads();
        tessellator.setColorOpaque_I(4210752);
        tessellator.addVertexWithUV(0.0D, (double)this.height, 0.0D, 0.0D, (double)((float)this.height / f + (float)tint));
        tessellator.addVertexWithUV((double)this.width, (double)this.height, 0.0D, (double)((float)this.width / f), (double)((float)this.height / f + (float)tint));
        tessellator.addVertexWithUV((double)this.width, 0.0D, 0.0D, (double)((float)this.width / f), (double)tint);
        tessellator.addVertexWithUV(0.0D, 0.0D, 0.0D, 0.0D, (double)tint);
        tessellator.draw();
    }

    public boolean doesGuiPauseGame() {
        return true;
    }

    public void confirmClicked(boolean result, int id) {
    }

    public static boolean isCtrlKeyDown() {
        return Minecraft.isRunningOnMac ? Keyboard.isKeyDown(219) || Keyboard.isKeyDown(220) : Keyboard.isKeyDown(29) || Keyboard.isKeyDown(157);
    }

    public static boolean isShiftKeyDown() {
        return Keyboard.isKeyDown(42) || Keyboard.isKeyDown(54);
    }

    @MixinMerged(
        mixin = "com.gtnewhorizon.gtnhlib.mixins.early.MixinGuiScreen",
        priority = 999,
        sessionId = "98d213e4-85ca-4ce4-959f-84858add440a"
    )
    private void handler$zjf000$gtnhlib$preRenderToolTip(ItemStack itemIn, int x, int y, CallbackInfo ci) {
        this.gtnhlib$currentStack = itemIn;
    }

    @MixinMerged(
        mixin = "com.gtnewhorizon.gtnhlib.mixins.early.MixinGuiScreen",
        priority = 999,
        sessionId = "98d213e4-85ca-4ce4-959f-84858add440a"
    )
    private void handler$zjf000$gtnhlib$postRenderToolTip(CallbackInfo ci) {
        this.gtnhlib$currentStack = null;
    }

    @Overwrite(
        remap = false
    )
    @MixinMerged(
        mixin = "com.gtnewhorizon.gtnhlib.mixins.early.MixinGuiScreen",
        priority = 999,
        sessionId = "98d213e4-85ca-4ce4-959f-84858add440a"
    )
    protected void drawHoveringText(List<String> textLines, int mouseX, int mouseY, FontRenderer font) {
        if (!textLines.isEmpty()) {
            RenderTooltipEvent event = new RenderTooltipEvent(this.gtnhlib$currentStack, this, -267386864, -267386864, 1347420415, 1344798847, mouseX, mouseY, font);
            if (this.gtnhlib$currentStack != null) {
                MinecraftForge.EVENT_BUS.post(event);
                if (event.isCanceled()) {
                    return;
                }
            }

            GL11.glDisable(32826);
            RenderHelper.disableStandardItemLighting();
            GL11.glDisable(2896);
            GL11.glDisable(2929);
            if (event.alternativeRenderer != null) {
                this.zLevel = 300.0F;
                itemRender.zLevel = 300.0F;
                event.alternativeRenderer.accept(textLines);
                this.zLevel = 0.0F;
                itemRender.zLevel = 0.0F;
            } else {
                mouseX = event.x;
                mouseY = event.y;
                font = event.font;
                int width = 0;
                Iterator var7 = textLines.iterator();

                int height;
                while(var7.hasNext()) {
                    String s = (String)var7.next();
                    height = font.getStringWidth(s);
                    if (height > width) {
                        width = height;
                    }
                }

                int x = mouseX + 12;
                int y = mouseY - 12;
                height = 8;
                if (textLines.size() > 1) {
                    height += 2 + (textLines.size() - 1) * 10;
                }

                if (x + width > this.width) {
                    x -= 28 + width;
                }

                if (y + height + 6 > this.height) {
                    y = this.height - height - 6;
                }

                this.zLevel = 300.0F;
                itemRender.zLevel = 300.0F;
                int backgroundStart = event.backgroundStart;
                int backgroundEnd = event.backgroundEnd;
                int borderStart = event.borderStart;
                int borderEnd = event.borderEnd;
                this.drawGradientRect(x - 3, y - 4, x + width + 3, y - 3, backgroundStart, backgroundStart);
                this.drawGradientRect(x - 3, y + height + 3, x + width + 3, y + height + 4, backgroundEnd, backgroundEnd);
                this.drawGradientRect(x - 3, y - 3, x + width + 3, y + height + 3, backgroundStart, backgroundEnd);
                this.drawGradientRect(x - 4, y - 3, x - 3, y + height + 3, backgroundStart, backgroundEnd);
                this.drawGradientRect(x + width + 3, y - 3, x + width + 4, y + height + 3, backgroundStart, backgroundEnd);
                this.drawGradientRect(x - 3, y - 2, x - 2, y + height + 2, borderStart, borderEnd);
                this.drawGradientRect(x + width + 2, y - 2, x + width + 3, y + height + 2, borderStart, borderEnd);
                this.drawGradientRect(x - 3, y - 3, x + width + 3, y - 2, borderStart, borderStart);
                this.drawGradientRect(x - 3, y + height + 2, x + width + 3, y + height + 3, borderEnd, borderEnd);

                for(int i = 0; i < textLines.size(); ++i) {
                    String s = (String)textLines.get(i);
                    font.drawStringWithShadow(s, x, y, -1);
                    if (i == 0) {
                        y += 2;
                    }

                    y += 10;
                }

                this.zLevel = 0.0F;
                itemRender.zLevel = 0.0F;
            }

            GL11.glEnable(2896);
            GL11.glEnable(2929);
            RenderHelper.enableStandardItemLighting();
            GL11.glEnable(32826);
        }

    }

    @MixinMerged(
        mixin = "me.eigenraven.lwjgl3ify.mixins.early.game.MixinGuiScreenKeyTypeInput",
        priority = 1000,
        sessionId = "98d213e4-85ca-4ce4-959f-84858add440a"
    )
    private void handler$zzf000$lwjgl3ify$handleKeyboardInput(CallbackInfo ci) {
        if (Keyboard.getEventKeyState()) {
            int codepoint = org.lwjglx.input.Keyboard.lwjgl3ify$getEventCodePoint();
            int keycode = Keyboard.getEventKey();
            char[] chars = Character.toChars(codepoint);
            char[] var5 = chars;
            int var6 = chars.length;

            for(int var7 = 0; var7 < var6; ++var7) {
                char c = var5[var7];
                this.keyTyped(c, keycode);
            }
        }

        Minecraft.getMinecraft().func_152348_aa();
        ci.cancel();
    }
}
