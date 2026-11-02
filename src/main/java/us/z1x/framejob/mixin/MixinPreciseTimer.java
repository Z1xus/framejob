package us.z1x.framejob.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import it.unimi.dsi.fastutil.floats.FloatUnaryOperator;
import net.minecraft.client.DeltaTracker;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import us.z1x.framejob.Framejob;

@Mixin(DeltaTracker.Timer.class)
public class MixinPreciseTimer {
    @Shadow
    @Final
    private float msPerTick;
    @Shadow
    @Final
    private FloatUnaryOperator targetMsptProvider;

    @Unique
    private long framejob$lastNanos;

    @WrapOperation(
            method = "advanceGameTime",
            at = @At(value = "FIELD", target = "Lnet/minecraft/client/DeltaTracker$Timer;deltaTicks:F", opcode = Opcodes.PUTFIELD)
    )
    private void framejob$preciseDeltaTicks(DeltaTracker.Timer timer, float deltaTicks, Operation<Void> original) {
        long now = System.nanoTime();
        if (Framejob.enabled && Framejob.preciseClock && this.framejob$lastNanos != 0) {
            deltaTicks = (now - this.framejob$lastNanos) / 1.0E6F / this.targetMsptProvider.apply(this.msPerTick);
        }
        this.framejob$lastNanos = now;
        original.call(timer, deltaTicks);
    }
}
