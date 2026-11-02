package us.z1x.framejob.mixin;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import us.z1x.framejob.Framejob;

@Mixin(Camera.class)
public class MixinSmoothSneakCamera {
    @Definition(id = "lerp", method = "Lnet/minecraft/util/Mth;lerp(FFF)F")
    @Definition(id = "eyeHeightOld", field = "Lnet/minecraft/client/Camera;eyeHeightOld:F")
    @Expression("lerp(@(?), this.eyeHeightOld, ?)")
    //? if < 26.1 {
    /*@ModifyExpressionValue(method = "setup", at = @At("MIXINEXTRAS:EXPRESSION"))
    *///?} else {
    @ModifyExpressionValue(method = "alignWithEntity", at = @At("MIXINEXTRAS:EXPRESSION"))
    //?}
    private float framejob$smoothSneakCamera(float tickDelta) {
        return Framejob.enabled && Framejob.smoothSneakCamera ? Framejob.exponentialTickDelta(tickDelta) : tickDelta;
    }
}
