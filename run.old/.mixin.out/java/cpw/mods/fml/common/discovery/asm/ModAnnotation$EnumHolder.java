package cpw.mods.fml.common.discovery.asm;

import com.gtnewhorizon.gtnhlib.mixins.early.fml.EnumHolderAccessor;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.transformer.meta.MixinMerged;

public class ModAnnotation$EnumHolder implements EnumHolderAccessor {
    private String desc;
    private String value;
    // $FF: synthetic field
    final ModAnnotation this$0;

    public ModAnnotation$EnumHolder(ModAnnotation this$0, String desc, String value) {
        this.this$0 = this$0;
        this.desc = desc;
        this.value = value;
    }

    // $FF: synthetic method
    @Accessor(
        target = "value:Ljava/lang/String;"
    )
    @MixinMerged(
        mixin = "com.gtnewhorizon.gtnhlib.mixins.early.fml.EnumHolderAccessor",
        priority = 1000,
        sessionId = "98d213e4-85ca-4ce4-959f-84858add440a"
    )
    public String getValue() {
        return this.value;
    }
}
