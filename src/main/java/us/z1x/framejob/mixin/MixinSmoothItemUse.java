package us.z1x.framejob.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.ItemInHandRenderer;
import net.minecraft.client.render.platform.GlStateManager;
import net.minecraft.client.resource.model.ModelTransformations;
import net.minecraft.entity.living.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.UseAction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import us.z1x.framejob.Framejob;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

@Mixin(ItemInHandRenderer.class)
public abstract class MixinSmoothItemUse {
    @Unique
    private static final float FRAMEJOB_BLOCK_RATE = 0.95F;
    @Unique
    private static final int FRAMEJOB_MODELVIEW_MATRIX = 0x0BA6;
    @Unique
    private static final FloatBuffer FRAMEJOB_MATRIX = ByteBuffer.allocateDirect(64).order(ByteOrder.nativeOrder()).asFloatBuffer();

    @Shadow
    @Final
    private Minecraft minecraft;
    @Shadow
    private ItemStack itemInHand;
    @Shadow
    private float handHeight;
    @Shadow
    private float lastHandHeight;

    @Unique
    private float framejob$heldHeight;
    @Unique
    private boolean framejob$dropped;
    @Unique
    private float framejob$blocking;
    @Unique
    private float framejob$lastBlocking;

    @Shadow
    protected abstract void applyArmSwing(float swing);

    @Shadow
    protected abstract void applyFirstPersonTransform(float equip, float swing);

    @Shadow
    protected abstract void applySwordBlocking();

    @Inject(method = {"onItemUsed", "onBlockUsed"}, at = @At("HEAD"))
    private void framejob$holdHeight(CallbackInfo ci) {
        this.framejob$heldHeight = this.handHeight;
    }

    @Inject(method = {"onItemUsed", "onBlockUsed"}, at = @At("TAIL"))
    private void framejob$deferDrop(CallbackInfo ci) {
        if (!Framejob.enabled || !Framejob.smoothItemUse) return;
        this.handHeight = this.framejob$heldHeight;
        this.framejob$dropped = true;
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void framejob$advance(CallbackInfo ci) {
        if (this.framejob$dropped) {
            this.handHeight = 0;
            this.framejob$dropped = false;
        }
        this.framejob$lastBlocking = this.framejob$blocking;
        float target = this.framejob$isBlocking() ? 1 : 0;
        this.framejob$blocking += (target - this.framejob$blocking) * FRAMEJOB_BLOCK_RATE;
        if (Math.abs(target - this.framejob$blocking) < 0.001F) this.framejob$blocking = target;
    }

    @WrapOperation(
            method = "renderInFirstPerson",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/ItemInHandRenderer;render(Lnet/minecraft/entity/living/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/resource/model/ModelTransformations$Type;)V"
            )
    )
    private void framejob$blendBlocking(
            ItemInHandRenderer renderer,
            LivingEntity entity,
            ItemStack stack,
            ModelTransformations.Type type,
            Operation<Void> original,
            @Local(argsOnly = true) float tickDelta
    ) {
        float blocking = this.framejob$lastBlocking + (this.framejob$blocking - this.framejob$lastBlocking) * Framejob.exponentialTickDelta(tickDelta, FRAMEJOB_BLOCK_RATE);
        boolean current = this.framejob$isBlocking();
        if (Framejob.enabled && Framejob.smoothItemUse && blocking != (current ? 1 : 0)) {
            float[] pose = framejob$modelView();
            GlStateManager.popMatrix();
            GlStateManager.pushMatrix();
            float equip = 1 - (this.lastHandHeight + (this.handHeight - this.lastHandHeight) * tickDelta);
            if (current) {
                float swing = this.minecraft.player.getAttackAnimationProgress(tickDelta);
                this.applyArmSwing(swing);
                this.applyFirstPersonTransform(equip, swing);
            } else {
                this.applyFirstPersonTransform(equip, 0);
                this.applySwordBlocking();
            }
            float[] other = framejob$modelView();
            framejob$load(current ? framejob$blend(other, pose, blocking) : framejob$blend(pose, other, blocking));
        }
        original.call(renderer, entity, stack, type);
    }

    @Unique
    private boolean framejob$isBlocking() {
        return this.minecraft.player != null
                && this.minecraft.player.getItemUseTimer() > 0
                && this.itemInHand != null
                && this.itemInHand.getUseAction() == UseAction.BLOCK;
    }

    @Unique
    private static float[] framejob$modelView() {
        float[] matrix = new float[16];
        GlStateManager.getFloat(FRAMEJOB_MODELVIEW_MATRIX, FRAMEJOB_MATRIX);
        FRAMEJOB_MATRIX.get(matrix).rewind();
        return matrix;
    }

    @Unique
    private static void framejob$load(float[] matrix) {
        FRAMEJOB_MATRIX.put(matrix).rewind();
        GlStateManager.loadIdentity();
        GlStateManager.multMatrix(FRAMEJOB_MATRIX);
    }

    @Unique
    private static float[] framejob$blend(float[] from, float[] to, float t) {
        float[] out = new float[16];
        for (int i = 0; i < 16; i++) out[i] = from[i] + (to[i] - from[i]) * t;
        float scale = framejob$length(from[0], from[1], from[2]) + (framejob$length(to[0], to[1], to[2]) - framejob$length(from[0], from[1], from[2])) * t;
        float[] x = framejob$normalized(out[0], out[1], out[2]);
        float dot = x[0] * out[4] + x[1] * out[5] + x[2] * out[6];
        float[] y = framejob$normalized(out[4] - x[0] * dot, out[5] - x[1] * dot, out[6] - x[2] * dot);
        float[] z = {x[1] * y[2] - x[2] * y[1], x[2] * y[0] - x[0] * y[2], x[0] * y[1] - x[1] * y[0]};
        for (int i = 0; i < 3; i++) {
            out[i] = x[i] * scale;
            out[4 + i] = y[i] * scale;
            out[8 + i] = z[i] * scale;
        }
        return out;
    }

    @Unique
    private static float framejob$length(float x, float y, float z) {
        return (float) Math.sqrt(x * x + y * y + z * z);
    }

    @Unique
    private static float[] framejob$normalized(float x, float y, float z) {
        float length = framejob$length(x, y, z);
        return new float[]{x / length, y / length, z / length};
    }
}
