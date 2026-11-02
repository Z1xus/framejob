package us.z1x.framejob.mixin;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
//? if = 1.8.9 {
/*import net.minecraft.client.render.GameRenderer;
*///?} elif < 26.1 {
/*import net.minecraft.client.renderer.GameRenderer;
*///?} else {
import net.minecraft.client.Camera;
//?}
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import us.z1x.framejob.Framejob;

//? if < 26.1 {
/*@Mixin(GameRenderer.class)
*///?} else {
@Mixin(Camera.class)
//?}
public class MixinSmoothFov {
    //? if = 1.8.9 {
    /*@Definition(id = "lastFov", field = "Lnet/minecraft/client/render/GameRenderer;lastFov:F")
    @Expression("this.lastFov + (? - this.lastFov) * @(?)")
    @ModifyExpressionValue(method = "getFov", at = @At("MIXINEXTRAS:EXPRESSION"))
    *///?} elif = 1.21.1 {
    /*@Definition(id = "lerp", method = "Lnet/minecraft/util/Mth;lerp(FFF)F")
    @Definition(id = "oldFov", field = "Lnet/minecraft/client/renderer/GameRenderer;oldFov:F")
    @Expression("lerp(@(?), this.oldFov, ?)")
    @ModifyExpressionValue(method = "getFov", at = @At("MIXINEXTRAS:EXPRESSION"))
    *///?} elif < 26.1 {
    /*@Definition(id = "lerp", method = "Lnet/minecraft/util/Mth;lerp(FFF)F")
    @Definition(id = "oldFovModifier", field = "Lnet/minecraft/client/renderer/GameRenderer;oldFovModifier:F")
    @Expression("lerp(@(?), this.oldFovModifier, ?)")
    @ModifyExpressionValue(method = "getFov", at = @At("MIXINEXTRAS:EXPRESSION"))
    *///?} else {
    @Definition(id = "lerp", method = "Lnet/minecraft/util/Mth;lerp(FFF)F")
    @Definition(id = "oldFovModifier", field = "Lnet/minecraft/client/Camera;oldFovModifier:F")
    @Expression("lerp(@(?), this.oldFovModifier, ?)")
    @ModifyExpressionValue(method = "calculateFov", at = @At("MIXINEXTRAS:EXPRESSION"))
    //?}
    private float framejob$smoothFov(float tickDelta) {
        return Framejob.enabled && Framejob.smoothFov ? Framejob.exponentialTickDelta(tickDelta, 0.5F) : tickDelta;
    }
}
