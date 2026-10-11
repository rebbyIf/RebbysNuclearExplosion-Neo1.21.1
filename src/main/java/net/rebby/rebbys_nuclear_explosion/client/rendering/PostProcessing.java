package net.rebby.rebbys_nuclear_explosion.client.rendering;

import com.ibm.icu.impl.Pair;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.shader.uniform.ShaderUniformAccess;
import foundry.veil.platform.VeilEventPlatform;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.rebby.rebbys_nuclear_explosion.RebbysNuclearExplosion;
import net.rebby.rebbys_nuclear_explosion.entity.custom.NuclearExplosionEntity;
import net.rebby.rebbys_nuclear_explosion.util.Timeline;

import java.util.concurrent.atomic.AtomicReference;

public final class PostProcessing {

    private static final Timeline FACTOR_0 = new Timeline(1)
            .pushEntry(Pair.of(100.0f,new Float[]{1.0f}), Timeline.InterpolationMethod.CUBIC_EASE_OUT)
            .pushEntry(Pair.of(400.0f,new Float[]{0.0f}), Timeline.InterpolationMethod.EASE_IN);

    private static final Timeline FACTOR_1 = new Timeline(1)
            .pushEntry(Pair.of(20.0f,new Float[]{1.0f}), Timeline.InterpolationMethod.CUBIC_EASE_OUT)
            .pushEntry(Pair.of(100.0f,new Float[]{0.0f}), Timeline.InterpolationMethod.EASE_IN);

    public static final ResourceLocation RADIATION_POST_PIPELINE = RebbysNuclearExplosion.getResource("radiation");
    public static final ResourceLocation DARKEN_SKY_POST_PIPELINE = RebbysNuclearExplosion.getResource("darken_sky");
    public static final ResourceLocation PLASMA_POST_PIPELINE = RebbysNuclearExplosion.getResource("plasma");

    public static void processing() {
        // This works for pipeline-specific uniforms
        VeilEventPlatform.INSTANCE.preVeilPostProcessing((pipelineName, pipeline, context) -> {
            Minecraft mc = Minecraft.getInstance();
            float renderDistance = mc.gameRenderer.getRenderDistance();
            AtomicReference<Float> factor0 = new AtomicReference<>(0.0f);
            AtomicReference<Float> factor1 = new AtomicReference<>(0.0f);
            if (mc.level != null &&
                    mc.player != null) {
                AtomicReference<NuclearExplosionEntity> nearestNuke = new AtomicReference<>();
                AtomicReference<Float> distance = new AtomicReference<>(-1.0f);

                mc.level.entitiesForRendering().forEach(entity -> {
                    if (entity instanceof NuclearExplosionEntity explosionEntity &&
                            explosionEntity.getAge() < 460) {

                        // Calculates Timeline of events
                        float mixing0 = FACTOR_0.interpolate(explosionEntity.getAge())[0];
                        float mixing1 = FACTOR_1.interpolate(explosionEntity.getAge())[0];

                        // Multiplies mixing0 by distance.
                        factor0.set(factor0.get() + mixing0 *
                                (float) (1 - Math.pow(explosionEntity.distanceTo(mc.player) / renderDistance, 2)));
                        factor1.set(factor1.get() + mixing1);

                    }
                });
            }

            if (RADIATION_POST_PIPELINE.equals(pipelineName)
                || DARKEN_SKY_POST_PIPELINE.equals(pipelineName)) {
                ShaderUniformAccess factor = pipeline.getUniform("Factor0");
                if (factor != null) {
                    factor.setFloat(factor0.get());

                }
            }

            if (RADIATION_POST_PIPELINE.equals(pipelineName)) {
                ShaderUniformAccess factor = pipeline.getUniform("Factor1");
                if (factor != null) {
                    factor.setFloat(factor1.get());

                }
            }
        });
    }
}
