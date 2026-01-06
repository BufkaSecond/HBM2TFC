package com.gtnewhorizon.gtnhlib.mixins.early.fml;

import cpw.mods.fml.common.discovery.asm.ModAnnotation.EnumHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(
    value = {EnumHolder.class},
    remap = false
)
public interface EnumHolderAccessor {
    @Accessor
    String getValue();
}
