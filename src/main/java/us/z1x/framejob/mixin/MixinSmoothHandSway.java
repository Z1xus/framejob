package us.z1x.framejob.mixin;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
//? if = 1.8.9 {
/*import net.minecraft.client.render.ItemInHandRenderer;
*///?} elif < 26.3 {
/*import net.minecraft.client.renderer.ItemInHandRenderer;
*///?} else {
import net.minecraft.client.player.FirstPersonHandsAndItems;
//?}
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import us.z1x.framejob.Framejob;

//? if < 26.3 {
/*@Mixin(ItemInHandRenderer.class)
*///?} else {
@Mixin(FirstPersonHandsAndItems.class)
//?}
public class MixinSmoothHandSway {
    //? if = 1.8.9 {
    /*@Definition(id = "lastEasedPitch", field = "Lnet/minecraft/client/entity/living/player/LocalClientPlayerEntity;lastEasedPitch:F")
    @Definition(id = "lastEasedYaw", field = "Lnet/minecraft/client/entity/living/player/LocalClientPlayerEntity;lastEasedYaw:F")
    @Expression({"?.lastEasedPitch + (? - ?.lastEasedPitch) * @(?)", "?.lastEasedYaw + (? - ?.lastEasedYaw) * @(?)"})
    @ModifyExpressionValue(method = "applyHandSway", at = @At("MIXINEXTRAS:EXPRESSION"))
    *///?} else {
    @Definition(id = "lerp", method = "Lnet/minecraft/util/Mth;lerp(FFF)F")
    @Definition(id = "xBobO", field = "Lnet/minecraft/client/player/LocalPlayer;xBobO:F")
    @Definition(id = "yBobO", field = "Lnet/minecraft/client/player/LocalPlayer;yBobO:F")
    @Expression({"lerp(@(?), ?.xBobO, ?)", "lerp(@(?), ?.yBobO, ?)"})
    //? if < 26.2 {
    /*@ModifyExpressionValue(method = "renderHandsWithItems", at = @At("MIXINEXTRAS:EXPRESSION"))
    *///?} elif < 26.3 {
    /*@ModifyExpressionValue(method = "submitHandsWithItems", at = @At("MIXINEXTRAS:EXPRESSION"))
    *///?} else {
    @ModifyExpressionValue(method = "extractRenderState", at = @At("MIXINEXTRAS:EXPRESSION"))
    //?}
    //?}
    private float framejob$smoothHandSway(float tickDelta) {
        return Framejob.enabled && Framejob.smoothHandSway ? Framejob.exponentialTickDelta(tickDelta, 0.5F) : tickDelta;
    }
}
