package com.myname.mymodid.mixins.late;


import com.dunk.tfc.ItemSetup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value= ItemSetup.class, remap=false)
public class WeaponFix {

}
