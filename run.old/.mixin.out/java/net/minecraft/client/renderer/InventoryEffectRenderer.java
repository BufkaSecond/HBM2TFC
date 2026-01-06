package net.minecraft.client.renderer;

import com.llamalad7.mixinextras.sugar.impl.ref.generated.LocalIntRefImpl;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import com.mitchej123.hodgepodge.Compat;
import com.mitchej123.hodgepodge.config.TweaksConfig;
import com.mitchej123.hodgepodge.util.RomanNumerals;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.Collection;
import java.util.Iterator;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.Container;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.transformer.meta.MixinMerged;

@SideOnly(Side.CLIENT)
public abstract class InventoryEffectRenderer extends GuiContainer {
    private boolean field_147045_u;
    private static final String __OBFID = "CL_00000755";

    public InventoryEffectRenderer(Container p_i1089_1_) {
        super(p_i1089_1_);
    }

    public void initGui() {
        super.initGui();
        if (!this.mc.thePlayer.getActivePotionEffects().isEmpty()) {
            int var1 = 160 + (this.width - this.xSize - 200) / 2;
            this.redirect$zcg000$hodgepodge$fixPotionOffset(this, var1);
            this.field_147045_u = true;
        }

    }

    private void func_147044_g() {
        LocalIntRefImpl sharedRef14 = new LocalIntRefImpl();
        sharedRef14.init(0);
        int i = this.guiLeft - 124;
        int j = this.guiTop;
        boolean flag = true;
        Collection collection = this.mc.thePlayer.getActivePotionEffects();
        if (!collection.isEmpty()) {
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            GL11.glDisable(2896);
            int k = 33;
            if (collection.size() > 5) {
                k = 132 / (collection.size() - 1);
            }

            for(Iterator iterator = this.mc.thePlayer.getActivePotionEffects().iterator(); iterator.hasNext(); j += k) {
                PotionEffect potioneffect = (PotionEffect)iterator.next();
                Potion potion = Potion.potionTypes[potioneffect.getPotionID()];
                GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                this.mc.getTextureManager().bindTexture(field_147001_a);
                this.drawTexturedModalRect(i, j, 0, 166, 140, 32);
                if (potion.hasStatusIcon()) {
                    int l = potion.getStatusIconIndex();
                    this.drawTexturedModalRect(i + 6, j + 7, 0 + l % 8 * 18, 198 + l / 8 * 18, 18, 18);
                }

                potion.renderInventoryEffect(i, j, potioneffect, this.mc);
                if (potion.shouldRenderInvText(potioneffect)) {
                    String s1 = I18n.format(potion.getName(), new Object[0]);
                    if (this.modifyExpressionValue$zbi000$hodgepodge$skipOriginalCode(potioneffect.getAmplifier(), sharedRef14) == 1) {
                        StringBuilder var10000 = (new StringBuilder()).append(s1).append(" ");
                        Object[] injectorAllocatedLocal12 = new Object[0];
                        String injectorAllocatedLocal11 = "enchantment.level.2";
                        s1 = var10000.append(this.redirect$zbi000$hodgepodge$addRomanNumeral(injectorAllocatedLocal11, injectorAllocatedLocal12, sharedRef14)).toString();
                    } else if (potioneffect.getAmplifier() == 2) {
                        s1 = s1 + " " + I18n.format("enchantment.level.3", new Object[0]);
                    } else if (potioneffect.getAmplifier() == 3) {
                        s1 = s1 + " " + I18n.format("enchantment.level.4", new Object[0]);
                    }

                    this.fontRendererObj.drawStringWithShadow(s1, i + 10 + 18, j + 6, 16777215);
                    String s = Potion.getDurationString(potioneffect);
                    this.fontRendererObj.drawStringWithShadow(s, i + 10 + 18, j + 6 + 10, 8355711);
                }
            }
        }

    }

    @MixinMerged(
        mixin = "com.mitchej123.hodgepodge.mixins.early.minecraft.MixinInventoryEffectRenderer_FixPotionEffectNumerals",
        priority = 1000,
        sessionId = "98d213e4-85ca-4ce4-959f-84858add440a"
    )
    private int modifyExpressionValue$zbi000$hodgepodge$skipOriginalCode(int amplifier, LocalIntRef potionAmplifierLevel) {
        potionAmplifierLevel.set(amplifier);
        return 1;
    }

    @MixinMerged(
        mixin = "com.mitchej123.hodgepodge.mixins.early.minecraft.MixinInventoryEffectRenderer_FixPotionEffectNumerals",
        priority = 1000,
        sessionId = "98d213e4-85ca-4ce4-959f-84858add440a"
    )
    private String redirect$zbi000$hodgepodge$addRomanNumeral(String string, Object[] objects, LocalIntRef potionAmplifierLevel) {
        if (potionAmplifierLevel.get() > 0) {
            if (TweaksConfig.arabicNumbersForEnchantsPotions) {
                return String.valueOf(potionAmplifierLevel.get() + 1);
            } else {
                String translation = I18n.format("enchantment.level." + (potionAmplifierLevel.get() + 1), objects);
                return translation != null && translation.startsWith("enchantment.level.") ? RomanNumerals.toRomanLimited(potionAmplifierLevel.get() + 1, 20) : translation;
            }
        } else {
            return "";
        }
    }

    @MixinMerged(
        mixin = "com.mitchej123.hodgepodge.mixins.early.minecraft.MixinInventoryEffectRenderer_PotionOffset",
        priority = 1000,
        sessionId = "98d213e4-85ca-4ce4-959f-84858add440a"
    )
    public void redirect$zcg000$hodgepodge$fixPotionOffset(InventoryEffectRenderer instance, int value) {
        this.guiLeft = (this.width - this.xSize) / 2;
    }

    @Overwrite
    @MixinMerged(
        mixin = "com.mitchej123.hodgepodge.mixins.early.minecraft.MixinInventoryEffectRenderer_PotionEffectRendering",
        priority = 1000,
        sessionId = "98d213e4-85ca-4ce4-959f-84858add440a"
    )
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        boolean leftPanelHidden = !Compat.isNeiLeftPanelVisible();
        if (leftPanelHidden) {
            super.drawScreen(mouseX, mouseY, partialTicks);
        }

        if (!this.mc.thePlayer.getActivePotionEffects().isEmpty()) {
            this.func_147044_g();
        }

        if (!leftPanelHidden) {
            super.drawScreen(mouseX, mouseY, partialTicks);
        }
    }
}
