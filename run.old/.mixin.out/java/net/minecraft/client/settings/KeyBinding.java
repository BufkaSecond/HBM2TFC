package net.minecraft.client.settings;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.mitchej123.hodgepodge.mixins.interfaces.KeyBindingExt;
import com.mitchej123.hodgepodge.util.FastUtilIntHashMap;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.IntHashMap;
import org.lwjgl.input.Keyboard;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.transformer.meta.MixinMerged;

@SideOnly(Side.CLIENT)
public class KeyBinding implements Comparable, KeyBindingExt {
    @Final
    public static final List keybindArray = new ArrayList();
    public static final IntHashMap hash = new FastUtilIntHashMap();
    public static final Set keybindSet = new HashSet();
    private final String keyDescription;
    private final int keyCodeDefault;
    private final String keyCategory;
    private int keyCode;
    public boolean pressed;
    public int pressTime;
    private static final String __OBFID = "CL_00000628";
    @Unique
    @MixinMerged(
        mixin = "com.mitchej123.hodgepodge.mixins.early.minecraft.MixinKeyBinding",
        priority = 1000,
        sessionId = "98d213e4-85ca-4ce4-959f-84858add440a"
    )
    private static final Multimap<Integer, KeyBinding> hodgepodge$KEYBIND_MULTIMAP = ArrayListMultimap.create();

    public static void unPressAllKeys() {
        Iterator iterator = keybindArray.iterator();

        while(iterator.hasNext()) {
            KeyBinding keybinding = (KeyBinding)iterator.next();
            keybinding.unpressKey();
        }

    }

    public static void resetKeyBindingArrayAndHash() {
        hash.clearMap();
        Iterator iterator = keybindArray.iterator();

        while(iterator.hasNext()) {
            KeyBinding keybinding = (KeyBinding)iterator.next();
            hash.addKey(keybinding.keyCode, keybinding);
        }

        handler$zfk000$hodgepodge$populateKeybindMatcherArray((CallbackInfo)null);
    }

    public static Set getKeybinds() {
        return keybindSet;
    }

    public KeyBinding(String description, int keyCode, String category) {
        this.keyDescription = description;
        this.keyCode = keyCode;
        this.keyCodeDefault = keyCode;
        this.keyCategory = category;
        keybindArray.add(this);
        hash.addKey(keyCode, this);
        keybindSet.add(category);
        this.handler$zfk000$hodgepodge$addMyselfInConstructor(description, keyCode, category, (CallbackInfo)null);
    }

    public boolean getIsKeyPressed() {
        return this.pressed;
    }

    public String getKeyCategory() {
        return this.keyCategory;
    }

    public boolean isPressed() {
        if (this.pressTime == 0) {
            return false;
        } else {
            --this.pressTime;
            return true;
        }
    }

    private void unpressKey() {
        this.pressTime = 0;
        this.pressed = false;
    }

    public String getKeyDescription() {
        return this.keyDescription;
    }

    public int getKeyCodeDefault() {
        return this.keyCodeDefault;
    }

    public int getKeyCode() {
        return this.keyCode;
    }

    public void setKeyCode(int keyCode) {
        this.keyCode = keyCode;
    }

    public int compareTo(KeyBinding p_compareTo_1_) {
        int i = I18n.format(this.keyCategory, new Object[0]).compareTo(I18n.format(p_compareTo_1_.keyCategory, new Object[0]));
        if (i == 0) {
            i = I18n.format(this.keyDescription, new Object[0]).compareTo(I18n.format(p_compareTo_1_.keyDescription, new Object[0]));
        }

        return i;
    }

    public int compareTo(Object p_compareTo_1_) {
        return this.compareTo((KeyBinding)p_compareTo_1_);
    }

    @Overwrite
    @MixinMerged(
        mixin = "com.mitchej123.hodgepodge.mixins.early.minecraft.MixinKeyBinding",
        priority = 1000,
        sessionId = "98d213e4-85ca-4ce4-959f-84858add440a"
    )
    public static void onTick(int keyCode) {
        if (keyCode != 0) {
            Iterator var1 = hodgepodge$KEYBIND_MULTIMAP.get(keyCode).iterator();

            while(var1.hasNext()) {
                KeyBinding bind = (KeyBinding)var1.next();
                if (bind != null) {
                    ++((KeyBinding)bind).pressTime;
                }
            }
        }

    }

    @Overwrite
    @MixinMerged(
        mixin = "com.mitchej123.hodgepodge.mixins.early.minecraft.MixinKeyBinding",
        priority = 1000,
        sessionId = "98d213e4-85ca-4ce4-959f-84858add440a"
    )
    public static void setKeyBindState(int keyCode, boolean pressed) {
        if (keyCode != 0) {
            Iterator var2 = hodgepodge$KEYBIND_MULTIMAP.get(keyCode).iterator();

            while(var2.hasNext()) {
                KeyBinding bind = (KeyBinding)var2.next();
                if (bind != null) {
                    ((KeyBinding)bind).pressed = pressed;
                }
            }
        }

    }

    @MixinMerged(
        mixin = "com.mitchej123.hodgepodge.mixins.early.minecraft.MixinKeyBinding",
        priority = 1000,
        sessionId = "98d213e4-85ca-4ce4-959f-84858add440a"
    )
    private static void handler$zfk000$hodgepodge$populateKeybindMatcherArray(CallbackInfo ci) {
        hodgepodge$KEYBIND_MULTIMAP.clear();
        Iterator var1 = keybindArray.iterator();

        while(var1.hasNext()) {
            KeyBinding binding = (KeyBinding)var1.next();
            if (binding != null && binding.getKeyCode() != 0) {
                hodgepodge$KEYBIND_MULTIMAP.put(binding.getKeyCode(), binding);
            }
        }

    }

    @MixinMerged(
        mixin = "com.mitchej123.hodgepodge.mixins.early.minecraft.MixinKeyBinding",
        priority = 1000,
        sessionId = "98d213e4-85ca-4ce4-959f-84858add440a"
    )
    private void handler$zfk000$hodgepodge$addMyselfInConstructor(String description, int keyCode, String category, CallbackInfo ci) {
        hodgepodge$KEYBIND_MULTIMAP.put(keyCode, this);
    }

    @MixinMerged(
        mixin = "com.mitchej123.hodgepodge.mixins.early.minecraft.MixinKeyBinding",
        priority = 1000,
        sessionId = "98d213e4-85ca-4ce4-959f-84858add440a"
    )
    public void hodgepodge$updateKeyStates() {
        Iterator var1 = hodgepodge$KEYBIND_MULTIMAP.values().iterator();

        while(var1.hasNext()) {
            KeyBinding keyBinding = (KeyBinding)var1.next();

            try {
                int keyCode = keyBinding.getKeyCode();
                setKeyBindState(keyCode, keyCode < 256 && Keyboard.isKeyDown(keyCode));
            } catch (IndexOutOfBoundsException var4) {
            }
        }

    }
}
