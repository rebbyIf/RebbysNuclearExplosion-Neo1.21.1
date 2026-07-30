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
            .pushEntry(Pair.of(40.0f,new Float[]{1.0f}), Timeline.InterpolationMethod.CUBIC_EASE_IN)
            .pushEntry(Pair.of(460.0f,new Float[]{0.0f}), Timeline.InterpolationMethod.EASE_OUT);

    private static final Timeline FACTOR_1 = new Timeline(1)
            .pushEntry(Pair.of(5.0f,new Float[]{1.0f}), Timeline.InterpolationMethod.CUBIC_EASE_IN)
            .pushEntry(Pair.of(160.0f,new Float[]{0.0f}), Timeline.InterpolationMethod.EASE_OUT);

    public static final ResourceLocation RADIATION_POST_PIPELINE = RebbysNuclearExplosion.getResource("radiation");
    public static final ResourceLocation DARKEN_SKY_POST_PIPELINE = RebbysNuclearExplosion.getResource("darken_sky");

    public static void processing() {
        // This works for pipeline-specific uniforms
        VeilEventPlatform.INSTANCE.preVeilPostProcessing((pipelineName, pipeline, context) -> {
            Minecraft mc = Minecraft.getInstance();
            float renderDistance = mc.gameRenderer.getRenderDistance();
            float factor0 = 0.0f, factor1 = 0.0f;
            if (mc.level != null &&
                    mc.player != null) {
                AtomicReference<NuclearExplosionEntity> nearestNuke = new AtomicReference<>();
                AtomicReference<Float> distance = new AtomicReference<>(-1.0f);

                mc.level.entitiesForRendering().forEach(entity -> {
                    if (entity instanceof NuclearExplosionEntity explosionEntity &&
                            explosionEntity.getAge() < 460 &&
                            (explosionEntity.distanceTo(mc.player) < distance.get() || distance.get() < 0)) {

                        distance.set(explosionEntity.distanceTo(mc.player));
                        nearestNuke.set(explosionEntity);
                    }
                });

                if (distance.get() >= 0) {

                    // Calculates Timeline of events
                    float mixing0 = FACTOR_0.interpolate(nearestNuke.get().getAge())[0];
                    float mixing1 = FACTOR_1.interpolate(nearestNuke.get().getAge())[0];

                    //RebbysNuclearExplosion.LOGGER.info("Nuke age is {}", nearestNuke.get().getAge());



                    // Multiplies mixing0 by distance.
                    factor0 = mixing0 * (float) (1 - Math.pow(distance.get() / renderDistance, 2));
                    factor1 = mixing1;

                } else {
                    VeilRenderSystem.renderer().getPostProcessingManager().remove(RADIATION_POST_PIPELINE);
                    VeilRenderSystem.renderer().getPostProcessingManager().remove(DARKEN_SKY_POST_PIPELINE);
                }
            }

            if (RADIATION_POST_PIPELINE.equals(pipelineName)
                || DARKEN_SKY_POST_PIPELINE.equals(pipelineName)) {
                ShaderUniformAccess factor = pipeline.getUniform("Factor0");
                if (factor != null) {
                    factor.setFloat(factor0);

                }
            }

            if (RADIATION_POST_PIPELINE.equals(pipelineName)) {
                ShaderUniformAccess factor = pipeline.getUniform("Factor1");
                if (factor != null) {
                    factor.setFloat(factor1);

                }
            }
        });
    }
}
