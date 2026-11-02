package us.z1x.framejob.mixin;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
//? if = 1.8.9 {
/*import net.minecraft.client.render.GameRenderer;
*///?} elif = 1.21.1 {
/*import net.minecraft.client.renderer.GameRenderer;
*///?} else {
import net.minecraft.client.entity.ClientAvatarState;
//?}
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import us.z1x.framejob.Framejob;

//? if <= 1.21.1 {
/*@Mixin(GameRenderer.class)
*///?} else {
@Mixin(ClientAvatarState.class)
//?}
public class MixinSmoothViewBobbing {
    //? if = 1.8.9 {
    /*@Definition(id = "lastBob", field = "Lnet/minecraft/entity/living/player/PlayerEntity;lastBob:F")
    @Expression("?.lastBob + (? - ?.lastBob) * @(?)")
    @ModifyExpressionValue(method = "applyViewBobbing", at = @At("MIXINEXTRAS:EXPRESSION"))
    *///?} elif = 1.21.1 {
    /*@Definition(id = "lerp", method = "Lnet/minecraft/util/Mth;lerp(FFF)F")
    @Definition(id = "oBob", field = "Lnet/minecraft/world/entity/player/Player;oBob:F")
    @Expression("lerp(@(?), ?.oBob, ?)")
    @ModifyExpressionValue(method = "bobView", at = @At("MIXINEXTRAS:EXPRESSION"))
    *///?} else {
    @Definition(id = "lerp", method = "Lnet/minecraft/util/Mth;lerp(FFF)F")
    @Definition(id = "bobO", field = "Lnet/minecraft/client/entity/ClientAvatarState;bobO:F")
    @Expression("lerp(@(?), this.bobO, ?)")
    @ModifyExpressionValue(method = "getInterpolatedBob", at = @At("MIXINEXTRAS:EXPRESSION"))
    //?}
    private float framejob$smoothViewBobbing(float tickDelta) {
        return Framejob.enabled && Framejob.smoothViewBobbing ? Framejob.exponentialTickDelta(tickDelta, 0.4F) : tickDelta;
    }
}
