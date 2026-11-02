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
public class MixinSmoothItemUse {
    @Unique
    private static final float FRAMEJOB_BLOCK_RATE = 0.95F;
    @Unique
    private static final int FRAMEJOB_MODELVIEW_MATRIX = 0x0BA6;
    @Unique
    private static final FloatBuffer FRAMEJOB_BUFFER = ByteBuffer.allocateDirect(64).order(ByteOrder.nativeOrder()).asFloatBuffer();

    @Shadow
    @Final
    private Minecraft minecraft;
    @Shadow
    private ItemStack itemInHand;
    @Shadow
    private float handHeight;

    @Unique
    private final float[] framejob$holdPose = new float[16];
    @Unique
    private final float[] framejob$blockPose = new float[16];
    @Unique
    private final float[] framejob$pose = new float[16];
    @Unique
    private float framejob$heldHeight;
    @Unique
    private boolean framejob$dropped;
    @Unique
    private float framejob$blocking;
    @Unique
    private float framejob$lastBlocking;

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
        if (!this.framejob$canBlock()) {
            this.framejob$blocking = this.framejob$lastBlocking = 0;
            return;
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
        if (Framejob.enabled && Framejob.smoothItemUse && this.framejob$canBlock()) {
            boolean blocking = this.framejob$isBlocking();
            GlStateManager.getFloat(FRAMEJOB_MODELVIEW_MATRIX, FRAMEJOB_BUFFER);
            FRAMEJOB_BUFFER.get(blocking ? this.framejob$blockPose : this.framejob$holdPose).rewind();
            float amount = this.framejob$lastBlocking + (this.framejob$blocking - this.framejob$lastBlocking) * Framejob.exponentialTickDelta(tickDelta, FRAMEJOB_BLOCK_RATE);
            if (amount != (blocking ? 1 : 0) && this.framejob$holdPose[15] != 0 && this.framejob$blockPose[15] != 0) {
                framejob$blend(this.framejob$holdPose, this.framejob$blockPose, amount, this.framejob$pose);
                FRAMEJOB_BUFFER.put(this.framejob$pose).rewind();
                GlStateManager.loadIdentity();
                GlStateManager.multMatrix(FRAMEJOB_BUFFER);
            }
        }
        original.call(renderer, entity, stack, type);
    }

    @Unique
    private boolean framejob$canBlock() {
        return this.itemInHand != null && this.itemInHand.getUseAction() == UseAction.BLOCK;
    }

    @Unique
    private boolean framejob$isBlocking() {
        return this.minecraft.player != null && this.minecraft.player.getItemUseTimer() > 0 && this.framejob$canBlock();
    }

    @Unique
    private static void framejob$blend(float[] from, float[] to, float t, float[] out) {
        for (int i = 0; i < 16; i++) out[i] = from[i] + (to[i] - from[i]) * t;
        float scaleX = framejob$lerpScale(from, to, 0, t);
        float scaleY = framejob$lerpScale(from, to, 4, t);
        float scaleZ = framejob$lerpScale(from, to, 8, t);
        framejob$normalize(out, 0);
        float dot = out[0] * out[4] + out[1] * out[5] + out[2] * out[6];
        for (int i = 0; i < 3; i++) out[4 + i] -= out[i] * dot;
        framejob$normalize(out, 4);
        out[8] = out[1] * out[6] - out[2] * out[5];
        out[9] = out[2] * out[4] - out[0] * out[6];
        out[10] = out[0] * out[5] - out[1] * out[4];
        for (int i = 0; i < 3; i++) {
            out[i] *= scaleX;
            out[4 + i] *= scaleY;
            out[8 + i] *= scaleZ;
        }
    }

    @Unique
    private static float framejob$lerpScale(float[] from, float[] to, int column, float t) {
        float start = framejob$length(from, column);
        return start + (framejob$length(to, column) - start) * t;
    }

    @Unique
    private static float framejob$length(float[] matrix, int column) {
        return (float) Math.sqrt(matrix[column] * matrix[column] + matrix[column + 1] * matrix[column + 1] + matrix[column + 2] * matrix[column + 2]);
    }

    @Unique
    private static void framejob$normalize(float[] matrix, int column) {
        float length = framejob$length(matrix, column);
        for (int i = 0; i < 3; i++) matrix[column + i] /= length;
    }
}
