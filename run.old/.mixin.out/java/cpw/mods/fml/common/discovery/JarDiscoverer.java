package cpw.mods.fml.common.discovery;

import com.google.common.collect.Lists;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.LoaderException;
import cpw.mods.fml.common.MetadataCollection;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.ModContainerFactory;
import cpw.mods.fml.common.discovery.asm.ASMModParser;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.zip.ZipEntry;
import org.apache.logging.log4j.Level;
import org.spongepowered.asm.mixin.transformer.meta.MixinMerged;

public class JarDiscoverer implements ITypeDiscoverer {
    public List<ModContainer> discover(ModCandidate candidate, ASMDataTable table) {
        List<ModContainer> foundMods = Lists.newArrayList();
        FMLLog.fine("Examining file %s for potential mods", new Object[]{candidate.getModContainer().getName()});
        JarFile jar = null;

        ArrayList var5;
        try {
            jar = new JarFile(candidate.getModContainer());
            if (jar.getManifest() == null || jar.getManifest().getMainAttributes().get("FMLCorePlugin") == null && jar.getManifest().getMainAttributes().get("TweakClass") == null) {
                ZipEntry modInfo = jar.getEntry("mcmod.info");
                MetadataCollection mc = null;
                if (modInfo != null) {
                    FMLLog.finer("Located mcmod.info file in file %s", new Object[]{candidate.getModContainer().getName()});
                    mc = MetadataCollection.from(jar.getInputStream(modInfo), candidate.getModContainer().getName());
                } else {
                    FMLLog.fine("The mod container %s appears to be missing an mcmod.info file", new Object[]{candidate.getModContainer().getName()});
                    mc = MetadataCollection.from((InputStream)null, "");
                }

                Iterator var7 = Collections.list(jar.entries()).iterator();

                while(true) {
                    ZipEntry ze;
                    do {
                        if (!var7.hasNext()) {
                            return foundMods;
                        }

                        ze = (ZipEntry)var7.next();
                    } while(ze.getName() != null && this.redirect$zzd000$lwjgl3ify$getZipEntryName(ze).startsWith("__MACOSX"));

                    Matcher match = classFile.matcher(ze.getName());
                    if (match.matches()) {
                        ASMModParser modParser;
                        try {
                            modParser = new ASMModParser(jar.getInputStream(ze));
                            candidate.addClassEntry(ze.getName());
                        } catch (LoaderException var23) {
                            FMLLog.log(Level.ERROR, var23, "There was a problem reading the entry %s in the jar %s - probably a corrupt zip", new Object[]{ze.getName(), candidate.getModContainer().getPath()});
                            jar.close();
                            throw var23;
                        }

                        modParser.validate();
                        modParser.sendToTable(table, candidate);
                        ModContainer container = ModContainerFactory.instance().build(modParser, candidate.getModContainer(), candidate);
                        if (container != null) {
                            table.addContainer(container);
                            foundMods.add(container);
                            container.bindMetadata(mc);
                        }
                    }
                }
            }

            FMLLog.finer("Ignoring coremod or tweak system %s", new Object[]{candidate.getModContainer()});
            var5 = foundMods;
        } catch (Exception var24) {
            FMLLog.log(Level.WARN, var24, "Zip file %s failed to read properly, it will be ignored", new Object[]{candidate.getModContainer().getName()});
            return foundMods;
        } finally {
            if (jar != null) {
                try {
                    jar.close();
                } catch (Exception var22) {
                }
            }

        }

        return var5;
    }

    @MixinMerged(
        mixin = "me.eigenraven.lwjgl3ify.mixins.early.fml.JarDiscoverer",
        priority = 1000,
        sessionId = "98d213e4-85ca-4ce4-959f-84858add440a"
    )
    public String redirect$zzd000$lwjgl3ify$getZipEntryName(ZipEntry ze) {
        String name = ze.getName();
        if (name == null) {
            return null;
        } else {
            return !name.contains("module-info.class") && !name.startsWith("META-INF/versions/") && !name.contains("org/openjdk/nashorn") && !name.contains("jakarta/servlet/") ? name : "__MACOSX_ignoreme";
        }
    }
}
