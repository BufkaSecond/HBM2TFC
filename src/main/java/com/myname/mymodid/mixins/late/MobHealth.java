package com.myname.mymodid.mixins.late;


import com.dunk.tfc.Core.TFC_MobData;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value= TFC_MobData.class, remap=false)
public class MobHealth {

    /*
    @Redirect(method = "<clinit>()V", at = @At(value = "INVOKE"))
    private double mixin() {
        // Steve
        float STEVE_DAMAGE = 20f;

        float SHARK_DAMAGE = 300f;

    }

     */

    @ModifyConstant(method = "<clinit>()V", constant = @Constant(intValue = 1500))
    private int changeConstant(int original) {
        return 15;
    }


    @Mutable
    @Shadow @Final
    public static float STEVE_DAMAGE;


    @Mutable
    @Shadow @Final
    public static int WOLF_HEALTH;


    /*
    @Inject(method = "<clinit>()V", at = @At(value = "TAIL"))
    private static void mixin(CallbackInfo ci) {
        WOLF_HEALTH = 1;
        STEVE_DAMAGE = 0.1f;
    }

     */



    /*
    @Inject(method = "<clinit>()V", at = @At(value = "TAIL"))
    private static void mixin(CallbackInfo ci) {
        WOLF_HEALTH = 15;
    }

     */


    /*
    @ModifyVariable(
        method = "<clinit>()V",
        at = @At(value = "RETURN"),
        index = 1
    )
    private double mixin(int WOLF_HEALTH) {
        return 10;
    }

     */


}
