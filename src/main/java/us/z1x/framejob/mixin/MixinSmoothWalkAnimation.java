package us.z1x.framejob.mixin;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
//? if = 1.8.9 {
/*import net.minecraft.client.render.entity.LivingEntityRenderer;
*///?} else {
import net.minecraft.world.entity.WalkAnimationState;
//?}
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import us.z1x.framejob.Framejob;

//? if = 1.8.9 {
/*@Mixin(LivingEntityRenderer.class)
*///?} else {
@Mixin(WalkAnimationState.class)
//?}
public class MixinSmoothWalkAnimation {
    //? if = 1.8.9 {
    /*@Definition(id = "lastWalkAnimationSpeed", field = "Lnet/minecraft/entity/living/LivingEntity;lastWalkAnimationSpeed:F")
    @Expression("?.lastWalkAnimationSpeed + (? - ?.lastWalkAnimationSpeed) * @(?)")
    @ModifyExpressionValue(method = "render", at = @At("MIXINEXTRAS:EXPRESSION"))
    *///?} else {
    @Definition(id = "lerp", method = "Lnet/minecraft/util/Mth;lerp(FFF)F")
    @Definition(id = "speedOld", field = "Lnet/minecraft/world/entity/WalkAnimationState;speedOld:F")
    @Expression("lerp(@(?), this.speedOld, ?)")
    @ModifyExpressionValue(method = "speed(F)F", at = @At("MIXINEXTRAS:EXPRESSION"))
    //?}
    private float framejob$smoothWalkAnimation(float tickDelta) {
        return Framejob.enabled && Framejob.smoothWalkAnimation ? Framejob.exponentialTickDelta(tickDelta, 0.4F) : tickDelta;
    }
}
