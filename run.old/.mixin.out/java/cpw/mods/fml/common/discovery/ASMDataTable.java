package cpw.mods.fml.common.discovery;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import cpw.mods.fml.common.ModContainer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.transformer.meta.MixinMerged;

public class ASMDataTable {
    private SetMultimap<String, cpw.mods.fml.common.discovery.ASMDataTable.ASMData> globalAnnotationData = HashMultimap.create();
    private Map<ModContainer, SetMultimap<String, cpw.mods.fml.common.discovery.ASMDataTable.ASMData>> containerAnnotationData;
    private List<ModContainer> containers = Lists.newArrayList();
    private SetMultimap<String, ModCandidate> packageMap = HashMultimap.create();

    public Set<cpw.mods.fml.common.discovery.ASMDataTable.ASMData> getAll(String annotation) {
        return this.globalAnnotationData.get(annotation);
    }

    public void addASMData(ModCandidate candidate, String annotation, String className, String objectName, Map<String, Object> annotationInfo) {
        this.globalAnnotationData.put(annotation, new cpw.mods.fml.common.discovery.ASMDataTable.ASMData(candidate, annotation, className, objectName, annotationInfo));
    }

    public void addContainer(ModContainer container) {
        this.containers.add(container);
    }

    public void registerPackage(ModCandidate modCandidate, String pkg) {
        this.packageMap.put(pkg, modCandidate);
    }

    public Set<ModCandidate> getCandidatesFor(String pkg) {
        return this.packageMap.get(pkg);
    }

    @Overwrite(
        remap = false
    )
    @MixinMerged(
        mixin = "com.mitchej123.hodgepodge.mixins.early.fml.MixinASMDataTable",
        priority = 1000,
        sessionId = "98d213e4-85ca-4ce4-959f-84858add440a"
    )
    public SetMultimap<String, cpw.mods.fml.common.discovery.ASMDataTable.ASMData> getAnnotationsFor(ModContainer container) {
        if (this.containerAnnotationData == null) {
            Map mapBuilder = new HashMap();
            Multimap containersMap = Multimaps.index(this.containers, ModContainer::getSource);
            Iterator var4 = this.globalAnnotationData.entries().iterator();

            while(var4.hasNext()) {
                Entry entry = (Entry)var4.next();
                Iterator var6 = containersMap.get(((cpw.mods.fml.common.discovery.ASMDataTable.ASMData)entry.getValue()).getCandidate().getModContainer()).iterator();

                while(var6.hasNext()) {
                    ModContainer modContainer = (ModContainer)var6.next();
                    ((SetMultimap)mapBuilder.computeIfAbsent(modContainer, (map) -> {
                        return HashMultimap.create();
                    })).put((String)entry.getKey(), (cpw.mods.fml.common.discovery.ASMDataTable.ASMData)entry.getValue());
                }
            }

            this.containerAnnotationData = mapBuilder;
        }

        return (SetMultimap)this.containerAnnotationData.get(container);
    }
}
