package net.rebby.rebbys_nuclear_explosion.client.rendertype;

import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.shader.uniform.ShaderUniformAccess;
import foundry.veil.platform.VeilEventPlatform;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.rebby.rebbys_nuclear_explosion.RebbysNuclearExplosion;
import net.rebby.rebbys_nuclear_explosion.entity.custom.NuclearExplosionEntity;
import net.rebby.rebbys_nuclear_explosion.util.InterpolationMethod;

import java.util.concurrent.atomic.AtomicReference;

public final class PostProcessing {

    private static final int[] MIXING_POS = {0, 40, 460};

    private static final float[] MIXING_VAL = {0.0f, 1.0f, 0.0f};

    private static final InterpolationMethod[] MIXING_EASING = {
            null,
            (Float t) -> (float) (1 - Math.pow(1 - t, 3)),
            (Float t) -> (float) (Math.pow(t, 2))
    };

    public static final ResourceLocation RADIATION_POST_PIPELINE = RebbysNuclearExplosion.getResource("radiation");
    public static final ResourceLocation DARKEN_SKY_POST_PIPELINE = RebbysNuclearExplosion.getResource("darken_sky");

    public static void processing() {
        // This works for pipeline-specific uniforms
        VeilEventPlatform.INSTANCE.preVeilPostProcessing((pipelineName, pipeline, context) -> {
            Minecraft mc = Minecraft.getInstance();
            float renderDistance = mc.gameRenderer.getRenderDistance();
            float mixingValue = 0.0f;
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
                    float mixing = MIXING_VAL[0];
                    float t = nearestNuke.get().getAge();

                    for (int i = 1; i < MIXING_POS.length; i++) {
                        if (t < MIXING_POS[i] && t >= MIXING_POS[i-1]) {
                            float t0 = t - MIXING_POS[i-1];
                            float easedT = MIXING_EASING[i].run(t0 / (MIXING_POS[i] - MIXING_POS[i-1]));
                            mixing = ((MIXING_VAL[i] - MIXING_VAL[i-1]) * easedT) + MIXING_VAL[i-1];
                            break;
                        }
                    }

                    if (t >= MIXING_POS[MIXING_POS.length - 1])
                        mixing = MIXING_VAL[MIXING_POS.length - 1];



                    // Multiplies mixing by distance.
                    mixingValue = mixing * (float) (1 - Math.pow(distance.get() / renderDistance, 2));

                } else {
                    VeilRenderSystem.renderer().getPostProcessingManager().remove(RADIATION_POST_PIPELINE);
                    VeilRenderSystem.renderer().getPostProcessingManager().remove(DARKEN_SKY_POST_PIPELINE);
                }
            }

            if (RADIATION_POST_PIPELINE.equals(pipelineName)
                || DARKEN_SKY_POST_PIPELINE.equals(pipelineName)) {
                ShaderUniformAccess factor = pipeline.getUniform("Factor");
                if (factor != null) {
                    factor.setFloat(mixingValue);

                }

                ShaderUniformAccess colorModulation = pipeline.getUniform("ColorModulation");

                if (colorModulation != null) {
                    colorModulation.setVector(0,2,0,1);
                }
            }
        });
    }
}
