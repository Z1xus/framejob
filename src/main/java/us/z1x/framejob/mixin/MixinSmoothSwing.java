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
    @Unique
    private float framejob$smoothing = 0.5F;
    @Unique
    private float framejob$normalStep;
    @Unique
    private float framejob$lastStep;

    @Inject(method = "tick", at = @At("RETURN"))
    private void framejob$advance(CallbackInfo ci) {
        this.framejob$lag = this.framejob$lagAt(1);
        float smoothing = Framejob.swingSmoothing;
        this.framejob$smoothing = smoothing >= 0.25F && smoothing <= 3 ? smoothing : 0.5F;
        //? if = 1.8.9 {
        /*float old = this.lastAttackAnimationProgress;
        float current = this.attackAnimationProgress;
        *///?} elif < 26.3 {
        /*float old = this.oAttackAnim;
        float current = this.attackAnim;
        *///?} else {
        float old = this.oldAnimation;
        float current = this.animation;
        //?}
        float step = current - old;
        if (Float.isNaN(step)) step = 0;
        if (step < 0) step++;
        boolean start = step > 0 && this.framejob$lastStep == 0;
        if (current > 0 && step > 0) this.framejob$normalStep = step;
        this.framejob$speed = Framejob.smoothWholeSwing || start ? step : Math.max(0, step - this.framejob$normalStep);
        this.framejob$lastStep = step;
    }

    //? if >= 26.3 {
    @Inject(
            method = "start",
            at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/LivingEntity$SwingState;animation:F", opcode = Opcodes.PUTFIELD)
    )
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
        float trail = this.framejob$speed * this.framejob$smoothing;
        return (this.framejob$lag + trail) * (float) Math.exp(-tickDelta / this.framejob$smoothing) - trail;
    }
}
