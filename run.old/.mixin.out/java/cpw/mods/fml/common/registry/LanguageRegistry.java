package cpw.mods.fml.common.registry;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.relauncher.Side;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StringTranslate;
import org.apache.logging.log4j.Level;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.transformer.meta.MixinMerged;

public class LanguageRegistry {
    private static final LanguageRegistry INSTANCE = new LanguageRegistry();
    private Map<String, Properties> modLanguageData = new HashMap();
    private static final Pattern assetENUSLang = Pattern.compile("assets/(.*)/lang/(?:.+/|)([\\w_-]+).lang");
    @MixinMerged(
        mixin = "com.mitchej123.hodgepodge.mixins.early.minecraft.MixinLanguageRegistry",
        priority = 1000,
        sessionId = "98d213e4-85ca-4ce4-959f-84858add440a"
    )
    private Set<String> examinedFiles = new ObjectOpenHashSet();

    public static LanguageRegistry instance() {
        return INSTANCE;
    }

    public String getStringLocalization(String key) {
        return this.getStringLocalization(key, FMLCommonHandler.instance().getCurrentLanguage());
    }

    public String getStringLocalization(String key, String lang) {
        String localizedString = "";
        Properties langPack = (Properties)this.modLanguageData.get(lang);
        if (langPack != null && langPack.getProperty(key) != null) {
            localizedString = langPack.getProperty(key);
        }

        return localizedString;
    }

    /** @deprecated */
    @Deprecated
    public void addStringLocalization(String key, String value) {
        this.addStringLocalization(key, "en_US", value);
    }

    /** @deprecated */
    @Deprecated
    public void addStringLocalization(String key, String lang, String value) {
        Properties langPack = (Properties)this.modLanguageData.get(lang);
        if (langPack == null) {
            langPack = new Properties();
            this.modLanguageData.put(lang, langPack);
        }

        langPack.put(key, value);
    }

    /** @deprecated */
    @Deprecated
    public void addStringLocalization(Properties langPackAdditions) {
        this.addStringLocalization(langPackAdditions, "en_US");
    }

    /** @deprecated */
    @Deprecated
    public void addStringLocalization(Properties langPackAdditions, String lang) {
        Properties langPack = (Properties)this.modLanguageData.get(lang);
        if (langPack == null) {
            langPack = new Properties();
            this.modLanguageData.put(lang, langPack);
        }

        if (langPackAdditions != null) {
            langPack.putAll(langPackAdditions);
        }

    }

    /** @deprecated */
    @Deprecated
    public void addNameForObject(Object objectToName, String lang, String name) {
        String objectName;
        if (objectToName instanceof Item) {
            objectName = ((Item)objectToName).getUnlocalizedName();
        } else if (objectToName instanceof Block) {
            objectName = ((Block)objectToName).getUnlocalizedName();
        } else {
            if (!(objectToName instanceof ItemStack)) {
                throw new IllegalArgumentException(String.format("Illegal object for naming %s", objectToName));
            }

            objectName = ((ItemStack)objectToName).getItem().getUnlocalizedName((ItemStack)objectToName);
        }

        objectName = objectName + ".name";
        this.addStringLocalization(objectName, lang, name);
    }

    /** @deprecated */
    @Deprecated
    public static void addName(Object objectToName, String name) {
        instance().addNameForObject(objectToName, "en_US", name);
    }

    /** @deprecated */
    @Deprecated
    public void mergeLanguageTable(Map field_135032_a, String lang) {
        Properties langPack = (Properties)this.modLanguageData.get(lang);
        if (langPack != null) {
            this.mergeWithoutOverwrite(langPack, field_135032_a);
        }

        Properties usPack = (Properties)this.modLanguageData.get("en_US");
        if (usPack != null) {
            this.mergeWithoutOverwrite(usPack, field_135032_a);
        }

    }

    /** @deprecated */
    @Deprecated
    private <K, V> void mergeWithoutOverwrite(Map<? extends K, ? extends V> from, Map<K, V> to) {
        Iterator var3 = from.entrySet().iterator();

        while(var3.hasNext()) {
            Entry<? extends K, ? extends V> e = (Entry)var3.next();
            if (!to.containsKey(e.getKey())) {
                to.put(e.getKey(), e.getValue());
            }
        }

    }

    /** @deprecated */
    @Deprecated
    public void loadLocalization(String localizationFile, String lang, boolean isXML) {
        URL urlResource = this.getClass().getResource(localizationFile);
        if (urlResource != null) {
            this.loadLocalization(urlResource, lang, isXML);
        } else {
            ModContainer activeModContainer = Loader.instance().activeModContainer();
            if (activeModContainer != null) {
                FMLLog.log(activeModContainer.getModId(), Level.ERROR, "The language resource %s cannot be located on the classpath. This is a programming error.", new Object[]{localizationFile});
            } else {
                FMLLog.log(Level.ERROR, "The language resource %s cannot be located on the classpath. This is a programming error.", new Object[]{localizationFile});
            }
        }

    }

    /** @deprecated */
    @Deprecated
    public void loadLocalization(URL localizationFile, String lang, boolean isXML) {
        InputStream langStream = null;
        Properties langPack = new Properties();

        try {
            langStream = localizationFile.openStream();
            if (isXML) {
                langPack.loadFromXML(langStream);
            } else {
                langPack.load(new InputStreamReader(langStream, Charsets.UTF_8));
            }

            this.addStringLocalization(langPack, lang);
        } catch (IOException var15) {
            FMLLog.log(Level.ERROR, var15, "Unable to load localization from file %s", new Object[]{localizationFile});
        } finally {
            try {
                if (langStream != null) {
                    langStream.close();
                }
            } catch (IOException var14) {
            }

        }

    }

    public void injectLanguage(String language, HashMap<String, String> parsedLangFile) {
        Properties p = (Properties)this.modLanguageData.get(language);
        if (p == null) {
            p = new Properties();
            this.modLanguageData.put(language, p);
        }

        p.putAll(parsedLangFile);
    }

    public void loadLanguagesFor(ModContainer container, Side side) {
        CallbackInfo callbackInfo5 = new CallbackInfo("loadLanguagesFor", true);
        this.handler$zzk000$hodgepodge$onlyAddLanguagesOnce(container, side, callbackInfo5);
        if (!callbackInfo5.isCancelled()) {
            File source = container.getSource();

            try {
                if (source.isDirectory()) {
                    this.searchDirForLanguages(source, "", side);
                } else {
                    this.searchZipForLanguages(source, side);
                }
            } catch (IOException var6) {
            }

        }
    }

    private void searchZipForLanguages(File source, Side side) throws IOException {
        ZipFile zf = new ZipFile(source);
        List<String> added = Lists.newArrayList();
        Iterator var5 = Collections.list(zf.entries()).iterator();

        while(var5.hasNext()) {
            ZipEntry ze = (ZipEntry)var5.next();
            Matcher matcher = assetENUSLang.matcher(ze.getName());
            if (matcher.matches()) {
                String lang = matcher.group(2);
                added.add(lang);
                instance().injectLanguage(lang, StringTranslate.parseLangFile(zf.getInputStream(ze)));
                if ("en_US".equals(lang) && side == Side.SERVER) {
                    StringTranslate.inject(zf.getInputStream(ze));
                }
            }
        }

        if (added.size() > 0) {
            FMLLog.fine("Found translations in %s [%s]", new Object[]{source.getName(), Joiner.on(", ").join(added)});
        }

        zf.close();
    }

    private void searchDirForLanguages(File source, String path, Side side) throws IOException {
        File[] var4 = source.listFiles();
        int var5 = var4.length;

        for(int var6 = 0; var6 < var5; ++var6) {
            File file = var4[var6];
            String currPath = path + file.getName();
            if (file.isDirectory()) {
                this.searchDirForLanguages(file, currPath + '/', side);
            }

            Matcher matcher = assetENUSLang.matcher(currPath);
            if (matcher.matches()) {
                String lang = matcher.group(2);
                FMLLog.fine("Injecting found translation assets for lang %s at %s into language system", new Object[]{lang, currPath});
                instance().injectLanguage(lang, StringTranslate.parseLangFile(new FileInputStream(file)));
                if ("en_US".equals(lang) && side == Side.SERVER) {
                    StringTranslate.inject(new FileInputStream(file));
                }
            }
        }

    }

    @MixinMerged(
        mixin = "com.mitchej123.hodgepodge.mixins.early.minecraft.MixinLanguageRegistry",
        priority = 1000,
        sessionId = "98d213e4-85ca-4ce4-959f-84858add440a"
    )
    private void handler$zzk000$hodgepodge$onlyAddLanguagesOnce(ModContainer container, Side side, CallbackInfo ci) {
        if (!this.examinedFiles.add(container.getSource().getName())) {
            ci.cancel();
        }

    }
}
