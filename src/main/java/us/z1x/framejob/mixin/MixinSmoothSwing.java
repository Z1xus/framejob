package us.z1x.framejob.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
//? if = 1.8.9 {
/*import net.minecraft.entity.living.LivingEntity;
*///?} else {
import net.minecraft.world.entity.LivingEntity;
//?}
//? if >= 26.3 {
import org.objectweb.asm.Opcodes;
//?}
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import us.z1x.framejob.Framejob;

//? if < 26.3 {
/*@Mixin(LivingEntity.class)
*///?} else {
@Mixin(LivingEntity.SwingState.class)
//?}
public class MixinSmoothSwing {
    //? if = 1.8.9 {
    /*@Shadow
    public float lastAttackAnimationProgress;
    @Shadow
    public float attackAnimationProgress;
    *///?} elif < 26.3 {
    /*@Shadow
    public float oAttackAnim;
    @Shadow
    public float attackAnim;
    *///?} else {
    @Shadow
    private float oldAnimation;
    @Shadow
    private float animation;
    //?}

    @Unique
    private float framejob$lag;
    @Unique
    private float framejob$speed;

    @Inject(method = "tick", at = @At("RETURN"))
    private void framejob$advance(CallbackInfo ci) {
        this.framejob$lag = this.framejob$lagAt(1);
        //? if = 1.8.9 {
        /*float speed = this.attackAnimationProgress - this.lastAttackAnimationProgress;
        *///?} elif < 26.3 {
        /*float speed = this.attackAnim - this.oAttackAnim;
        *///?} else {
        float speed = this.animation - this.oldAnimation;
        //?}
        if (Float.isNaN(speed)) speed = 0;
        this.framejob$speed = speed < 0 ? speed + 1 : speed;
    }

    //? if >= 26.3 {
    @Inject(method = "start", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/LivingEntity$SwingState;animation:F", opcode = Opcodes.PUTFIELD))
    private void framejob$reset(CallbackInfo ci) {
        this.framejob$lag = 0;
        this.framejob$speed = 0;
    }
    //?}

    //? if = 1.8.9 {
    /*@ModifyReturnValue(method = "getAttackAnimationProgress", at = @At("RETURN"))
    *///?} elif < 26.3 {
    /*@ModifyReturnValue(method = "getAttackAnim", at = @At("RETURN"))
    *///?} else {
    @ModifyReturnValue(method = "getAnimation", at = @At("RETURN"))
    //?}
    private float framejob$smoothSwing(float progress, float tickDelta) {
        if (!Framejob.enabled || !Framejob.smoothSwings) return progress;
        float shown = progress + this.framejob$lagAt(tickDelta);
        return shown - (float) Math.floor(shown);
    }

    @Unique
    private float framejob$lagAt(float tickDelta) {
        float trail = this.framejob$speed * Framejob.swingSmoothing;
        return (this.framejob$lag + trail) * (float) Math.exp(-tickDelta / Framejob.swingSmoothing) - trail;
    }
}
