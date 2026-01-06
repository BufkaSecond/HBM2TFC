package net.minecraft.client.gui;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.ObjectArrays;
import com.google.common.collect.Sets;
import com.gtnewhorizon.gtnhlib.util.FilesUtil;
import com.mitchej123.hodgepodge.config.TweaksConfig;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import net.minecraft.client.gui.stream.GuiTwitchUserMode;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.event.ClickEvent.Action;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.client.C14PacketTabComplete;
import net.minecraft.stats.Achievement;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatList;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.client.ClientCommandHandler;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.transformer.meta.MixinMerged;
import tv.twitch.chat.ChatUserInfo;

@SideOnly(Side.CLIENT)
public class GuiChat extends GuiScreen implements GuiYesNoCallback {
    private static final Set field_152175_f = Sets.newHashSet(new String[]{"http", "https"});
    private static final Logger logger = LogManager.getLogger();
    private String field_146410_g = "";
    private int sentHistoryCursor = -1;
    private boolean field_146417_i;
    private boolean field_146414_r;
    private int field_146413_s;
    private List field_146412_t = new ArrayList();
    private URI clickedURI;
    protected GuiTextField inputField;
    private String defaultInputFieldText = "";
    private static final String __OBFID = "CL_00000682";

    public GuiChat() {
    }

    public GuiChat(String p_i1024_1_) {
        this.defaultInputFieldText = p_i1024_1_;
    }

    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        this.sentHistoryCursor = this.mc.ingameGUI.getChatGUI().getSentMessages().size();
        this.inputField = new GuiTextField(this.fontRendererObj, 4, this.height - 12, this.width - 4, 12);
        this.inputField.setMaxStringLength(this.constant$zco000$hodgepodge$LongerMessages(100));
        this.inputField.setEnableBackgroundDrawing(false);
        this.inputField.setFocused(true);
        this.inputField.setText(this.defaultInputFieldText);
        this.inputField.setCanLoseFocus(false);
    }

    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
        this.mc.ingameGUI.getChatGUI().resetScroll();
    }

    public void updateScreen() {
        this.inputField.updateCursorCounter();
    }

    protected void keyTyped(char typedChar, int keyCode) {
        this.field_146414_r = false;
        if (keyCode == 15) {
            this.func_146404_p_();
        } else {
            this.field_146417_i = false;
        }

        if (keyCode == 1) {
            this.mc.displayGuiScreen((GuiScreen)null);
        } else if (keyCode != 28 && keyCode != 156) {
            if (keyCode == 200) {
                this.getSentHistory(-1);
            } else if (keyCode == 208) {
                this.getSentHistory(1);
            } else if (keyCode == 201) {
                this.mc.ingameGUI.getChatGUI().scroll(this.mc.ingameGUI.getChatGUI().func_146232_i() - 1);
            } else if (keyCode == 209) {
                this.mc.ingameGUI.getChatGUI().scroll(-this.mc.ingameGUI.getChatGUI().func_146232_i() + 1);
            } else {
                this.inputField.textboxKeyTyped(typedChar, keyCode);
            }
        } else {
            String s = this.inputField.getText().trim();
            if (s.length() > 0) {
                this.func_146403_a(s);
            }

            this.mc.displayGuiScreen((GuiScreen)null);
        }

    }

    public void func_146403_a(String p_146403_1_) {
        this.mc.ingameGUI.getChatGUI().addToSentMessages(p_146403_1_);
        if (ClientCommandHandler.instance.executeCommand(this.mc.thePlayer, p_146403_1_) == 0) {
            this.mc.thePlayer.sendChatMessage(p_146403_1_);
        }
    }

    public void handleMouseInput() {
        super.handleMouseInput();
        int i = Mouse.getEventDWheel();
        if (i != 0) {
            if (i > 1) {
                i = 1;
            }

            if (i < -1) {
                i = -1;
            }

            if (!isShiftKeyDown()) {
                i *= 7;
            }

            this.mc.ingameGUI.getChatGUI().scroll(i);
        }

    }

    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0 && this.mc.gameSettings.chatLinks) {
            IChatComponent ichatcomponent = this.mc.ingameGUI.getChatGUI().func_146236_a(Mouse.getX(), Mouse.getY());
            if (ichatcomponent != null) {
                ClickEvent clickevent = ichatcomponent.getChatStyle().getChatClickEvent();
                if (clickevent != null) {
                    if (isShiftKeyDown()) {
                        this.inputField.writeText(ichatcomponent.getUnformattedTextForChat());
                    } else {
                        URI uri;
                        if (clickevent.getAction() == Action.OPEN_URL) {
                            try {
                                uri = new URI(clickevent.getValue());
                                if (!field_152175_f.contains(uri.getScheme().toLowerCase())) {
                                    throw new URISyntaxException(clickevent.getValue(), "Unsupported protocol: " + uri.getScheme().toLowerCase());
                                }

                                if (this.mc.gameSettings.chatLinksPrompt) {
                                    this.clickedURI = uri;
                                    this.mc.displayGuiScreen(new GuiConfirmOpenLink(this, clickevent.getValue(), 0, false));
                                } else {
                                    this.func_146407_a(uri);
                                }
                            } catch (URISyntaxException var8) {
                                logger.error("Can't open url for " + clickevent, var8);
                            }
                        } else if (clickevent.getAction() == Action.OPEN_FILE) {
                            uri = (new File(clickevent.getValue())).toURI();
                            this.func_146407_a(uri);
                        } else if (clickevent.getAction() == Action.SUGGEST_COMMAND) {
                            this.inputField.setText(clickevent.getValue());
                        } else if (clickevent.getAction() == Action.RUN_COMMAND) {
                            this.func_146403_a(clickevent.getValue());
                        } else if (clickevent.getAction() == Action.TWITCH_USER_INFO) {
                            ChatUserInfo chatuserinfo = this.mc.func_152346_Z().func_152926_a(clickevent.getValue());
                            if (chatuserinfo != null) {
                                this.mc.displayGuiScreen(new GuiTwitchUserMode(this.mc.func_152346_Z(), chatuserinfo));
                            } else {
                                logger.error("Tried to handle twitch user but couldn't find them!");
                            }
                        } else {
                            logger.error("Don't know how to handle " + clickevent);
                        }
                    }

                    return;
                }
            }
        }

        this.inputField.mouseClicked(mouseX, mouseY, mouseButton);
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    public void confirmClicked(boolean result, int id) {
        if (id == 0) {
            if (result) {
                this.func_146407_a(this.clickedURI);
            }

            this.clickedURI = null;
            this.mc.displayGuiScreen(this);
        }

    }

    private void func_146407_a(URI p_146407_1_) {
        CallbackInfo callbackInfo4 = new CallbackInfo("func_146407_a", true);
        this.handler$zhn000$hodgepodge$fixFileOpening(p_146407_1_, callbackInfo4);
        if (!callbackInfo4.isCancelled()) {
            try {
                Class oclass = Class.forName("java.awt.Desktop");
                Object object = oclass.getMethod("getDesktop").invoke((Object)null);
                oclass.getMethod("browse", URI.class).invoke(object, p_146407_1_);
            } catch (Throwable var5) {
                logger.error("Couldn't open link", var5);
            }

        }
    }

    public void func_146404_p_() {
        String s1;
        if (this.field_146417_i) {
            this.inputField.deleteFromCursor(this.inputField.func_146197_a(-1, this.inputField.getCursorPosition(), false) - this.inputField.getCursorPosition());
            if (this.field_146413_s >= this.field_146412_t.size()) {
                this.field_146413_s = 0;
            }
        } else {
            int i = this.inputField.func_146197_a(-1, this.inputField.getCursorPosition(), false);
            this.field_146412_t.clear();
            this.field_146413_s = 0;
            String s = this.inputField.getText().substring(i).toLowerCase();
            s1 = this.inputField.getText().substring(0, this.inputField.getCursorPosition());
            this.func_146405_a(s1, s);
            if (this.field_146412_t.isEmpty()) {
                return;
            }

            this.field_146417_i = true;
            this.inputField.deleteFromCursor(i - this.inputField.getCursorPosition());
        }

        if (this.field_146412_t.size() > 1) {
            StringBuilder stringbuilder = new StringBuilder();

            for(Iterator iterator = this.field_146412_t.iterator(); iterator.hasNext(); stringbuilder.append(s1)) {
                s1 = (String)iterator.next();
                if (stringbuilder.length() > 0) {
                    stringbuilder.append(", ");
                }
            }

            this.mc.ingameGUI.getChatGUI().printChatMessageWithOptionalDeletion(new ChatComponentText(stringbuilder.toString()), 1);
        }

        this.inputField.writeText(EnumChatFormatting.getTextWithoutFormattingCodes((String)this.field_146412_t.get(this.field_146413_s++)));
    }

    private void func_146405_a(String p_146405_1_, String p_146405_2_) {
        if (p_146405_1_.length() >= 1) {
            ClientCommandHandler.instance.autoComplete(p_146405_1_, p_146405_2_);
            this.mc.thePlayer.sendQueue.addToSendQueue(new C14PacketTabComplete(p_146405_1_));
            this.field_146414_r = true;
        }

    }

    public void getSentHistory(int p_146402_1_) {
        int j = this.sentHistoryCursor + p_146402_1_;
        int k = this.mc.ingameGUI.getChatGUI().getSentMessages().size();
        if (j < 0) {
            j = 0;
        }

        if (j > k) {
            j = k;
        }

        if (j != this.sentHistoryCursor) {
            if (j == k) {
                this.sentHistoryCursor = k;
                this.inputField.setText(this.field_146410_g);
            } else {
                if (this.sentHistoryCursor == k) {
                    this.field_146410_g = this.inputField.getText();
                }

                this.inputField.setText((String)this.mc.ingameGUI.getChatGUI().getSentMessages().get(j));
                this.sentHistoryCursor = j;
            }
        }

    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawRect(2, this.height - 14, this.width - 2, this.height - 2, Integer.MIN_VALUE);
        this.inputField.drawTextBox();
        IChatComponent ichatcomponent = this.mc.ingameGUI.getChatGUI().func_146236_a(Mouse.getX(), Mouse.getY());
        if (ichatcomponent != null && ichatcomponent.getChatStyle().getChatHoverEvent() != null) {
            HoverEvent hoverevent = ichatcomponent.getChatStyle().getChatHoverEvent();
            if (hoverevent.getAction() == net.minecraft.event.HoverEvent.Action.SHOW_ITEM) {
                ItemStack itemstack = null;

                try {
                    NBTBase nbtbase = JsonToNBT.func_150315_a(hoverevent.getValue().getUnformattedText());
                    if (nbtbase != null && nbtbase instanceof NBTTagCompound) {
                        itemstack = ItemStack.loadItemStackFromNBT((NBTTagCompound)nbtbase);
                    }
                } catch (NBTException var11) {
                }

                if (itemstack != null) {
                    this.renderToolTip(itemstack, mouseX, mouseY);
                } else {
                    this.drawCreativeTabHoveringText(EnumChatFormatting.RED + "Invalid Item!", mouseX, mouseY);
                }
            } else if (hoverevent.getAction() == net.minecraft.event.HoverEvent.Action.SHOW_TEXT) {
                this.func_146283_a(Splitter.on("\n").splitToList(hoverevent.getValue().getFormattedText()), mouseX, mouseY);
            } else if (hoverevent.getAction() == net.minecraft.event.HoverEvent.Action.SHOW_ACHIEVEMENT) {
                StatBase statbase = StatList.func_151177_a(hoverevent.getValue().getUnformattedText());
                if (statbase != null) {
                    IChatComponent ichatcomponent1 = statbase.func_150951_e();
                    ChatComponentTranslation chatcomponenttranslation = new ChatComponentTranslation("stats.tooltip.type." + (statbase.isAchievement() ? "achievement" : "statistic"), new Object[0]);
                    chatcomponenttranslation.getChatStyle().setItalic(true);
                    String s = statbase instanceof Achievement ? ((Achievement)statbase).getDescription() : null;
                    ArrayList arraylist = Lists.newArrayList(new String[]{ichatcomponent1.getFormattedText(), chatcomponenttranslation.getFormattedText()});
                    if (s != null) {
                        arraylist.addAll(this.fontRendererObj.listFormattedStringToWidth(s, 150));
                    }

                    this.func_146283_a(arraylist, mouseX, mouseY);
                } else {
                    this.drawCreativeTabHoveringText(EnumChatFormatting.RED + "Invalid statistic/achievement!", mouseX, mouseY);
                }
            }

            GL11.glDisable(2896);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    public void func_146406_a(String[] p_146406_1_) {
        if (this.field_146414_r) {
            this.field_146417_i = false;
            this.field_146412_t.clear();
            String[] astring1 = p_146406_1_;
            int i = p_146406_1_.length;
            String[] complete = ClientCommandHandler.instance.latestAutoComplete;
            if (complete != null) {
                astring1 = (String[])ObjectArrays.concat(complete, p_146406_1_, String.class);
                i = astring1.length;
            }

            String s2;
            for(int j = 0; j < i; ++j) {
                s2 = astring1[j];
                if (s2.length() > 0) {
                    this.field_146412_t.add(s2);
                }
            }

            String s1 = this.inputField.getText().substring(this.inputField.func_146197_a(-1, this.inputField.getCursorPosition(), false));
            s2 = StringUtils.getCommonPrefix(p_146406_1_);
            if (s2.length() > 0 && !s1.equalsIgnoreCase(s2)) {
                this.inputField.deleteFromCursor(this.inputField.func_146197_a(-1, this.inputField.getCursorPosition(), false) - this.inputField.getCursorPosition());
                this.inputField.writeText(s2);
            } else if (this.field_146412_t.size() > 0) {
                this.field_146417_i = true;
                this.func_146404_p_();
            }
        }

    }

    public boolean doesGuiPauseGame() {
        return false;
    }

    @MixinMerged(
        mixin = "com.mitchej123.hodgepodge.mixins.early.minecraft.MixinGuiChat_LongerMessages",
        priority = 1000,
        sessionId = "98d213e4-85ca-4ce4-959f-84858add440a"
    )
    public int constant$zco000$hodgepodge$LongerMessages(int constant) {
        return TweaksConfig.longerSentMessages ? 256 : constant;
    }

    @MixinMerged(
        mixin = "com.mitchej123.hodgepodge.mixins.early.minecraft.MixinGuiChat_OpenLinks",
        priority = 1000,
        sessionId = "98d213e4-85ca-4ce4-959f-84858add440a"
    )
    private void handler$zhn000$hodgepodge$fixFileOpening(URI uri, CallbackInfo ci) {
        FilesUtil.openUri(uri);
        ci.cancel();
    }
}
