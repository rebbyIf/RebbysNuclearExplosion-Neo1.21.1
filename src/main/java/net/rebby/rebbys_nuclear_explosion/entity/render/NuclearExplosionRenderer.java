package net.rebby.rebbys_nuclear_explosion.entity.render;

import com.ibm.icu.impl.Pair;
import com.mojang.blaze3d.vertex.*;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.rendertype.VeilRenderType;
import foundry.veil.api.event.VeilRenderLevelStageEvent;
import foundry.veil.platform.VeilEventPlatform;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.rebby.rebbys_nuclear_explosion.Config;
import net.rebby.rebbys_nuclear_explosion.RebbysNuclearExplosion;
import net.rebby.rebbys_nuclear_explosion.client.rendering.PostProcessing;
import net.rebby.rebbys_nuclear_explosion.entity.Entities;
import net.rebby.rebbys_nuclear_explosion.entity.custom.NuclearExplosionEntity;
import net.rebby.rebbys_nuclear_explosion.util.Timeline;
import org.jetbrains.annotations.NotNull;

public class NuclearExplosionRenderer extends EntityRenderer<NuclearExplosionEntity> {

//    private static final int N_SLICES = 32;
//    private static final int N_STACKS = 16;
//
//    private static final Timeline SIZE = new Timeline(1)
//            .pushEntry(Pair.of(20.0f,new Float[]{0.6f}), Timeline.InterpolationMethod.CUBIC_EASE_OUT)
//            .pushEntry(Pair.of(100.0f,new Float[]{0.9f}), Timeline.InterpolationMethod.LINEAR)
//            .pushEntry(Pair.of(300.0f,new Float[]{1.1f}), Timeline.InterpolationMethod.EASE_OUT)
//            .pushEntry(Pair.of(400.0f,new Float[]{1.0f}), Timeline.InterpolationMethod.EASE_IN);
//
//    private static final Timeline ALPHA = new Timeline(Pair.of(300.0f, new Float[]{1.0f}))
//            .pushEntry(Pair.of(400.0f,new Float[]{0.0f}), Timeline.InterpolationMethod.CUBIC_EASE_OUT);
//
    public static final ResourceLocation RENDER_TYPE_ID = RebbysNuclearExplosion.getResource("plasma");

    protected NuclearExplosionRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    public static void init(){
        if (!VeilRenderSystem.renderer().getPostProcessingManager().isActive(PostProcessing.PLASMA_POST_PIPELINE))
            VeilRenderSystem.renderer().getPostProcessingManager().add(PostProcessing.PLASMA_POST_PIPELINE);
        if (!VeilRenderSystem.renderer().getPostProcessingManager().isActive(PostProcessing.RADIATION_POST_PIPELINE))
            VeilRenderSystem.renderer().getPostProcessingManager().add(PostProcessing.RADIATION_POST_PIPELINE);
        if (!VeilRenderSystem.renderer().getPostProcessingManager().isActive(PostProcessing.DARKEN_SKY_POST_PIPELINE))
            VeilRenderSystem.renderer().getPostProcessingManager().add(PostProcessing.DARKEN_SKY_POST_PIPELINE);
    }


    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull NuclearExplosionEntity nuclearExplosionEntity) {
        return Entities.NUCLEAR_EXPLOSION.getId();
    }

//    public static float getSize(float age){
//        return SIZE.interpolate(age)[0] * Config.r3;
//    }

    @Override
    public void render(@NotNull NuclearExplosionEntity p_entity, float entityYaw, float partialTick, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight) {

//        RenderType renderType = VeilRenderType.get(RENDER_TYPE_ID);
//
//        //Minecraft.getInstance().renderBuffers().bufferSource();
//
//        if (renderType == null){
//            RebbysNuclearExplosion.LOGGER.error("Error in getting RenderType");
//            return;
//        }
//        VertexConsumer vertexconsumer = bufferSource.getBuffer(renderType);
//        poseStack.pushPose();
//
//        float size = SIZE.interpolate(p_entity.getAge())[0] * Config.r3;
//        float alpha = ALPHA.interpolate(p_entity.getAge())[0];
//
//        poseStack.scale(size,size,size);
//        PoseStack.Pose pose = poseStack.last();
//
//        for (int i = 0; i < N_STACKS; i++) {
//            double phi = Math.PI * i / N_STACKS;
//            double phi1 = Math.PI * (i + 1) / N_STACKS;
//            for (int j = 0; j < N_SLICES; j++) {
//                double theta = 2.0 * Math.PI * j / N_SLICES;
//                double theta1 = 2.0 * Math.PI * (j + 1) / N_SLICES;
//
//                float x0 = (float) (Math.sin(phi) * Math.cos(theta));
//                float y0 = (float) (Math.cos(phi));
//                float z0 = (float) (Math.sin(phi) * Math.sin(theta));
//                vertexconsumer.addVertex(pose, x0, y0, z0
//                ).setWhiteAlpha((int) (alpha*255))
//                        .setUv(0,0)
//                        .setNormal(0, 1, 0)
//                        .setLight(packedLight)
//                        .setOverlay(OverlayTexture.NO_OVERLAY);
//
//                float x1 = (float) (Math.sin(phi1) * Math.cos(theta));
//                float y1 = (float) (Math.cos(phi1));
//                float z1 = (float) (Math.sin(phi1) * Math.sin(theta));
//                vertexconsumer.addVertex(pose, x1, y1, z1
//                ).setWhiteAlpha((int) (alpha*255))
//                        .setUv(1,0)
//                        .setNormal(0, 1, 0)
//                        .setLight(packedLight)
//                        .setOverlay(OverlayTexture.NO_OVERLAY);
//
//                float x2 = (float) (Math.sin(phi1) * Math.cos(theta1));
//                float y2 = (float) (Math.cos(phi1));
//                float z2 = (float) (Math.sin(phi1) * Math.sin(theta1));
//                vertexconsumer.addVertex(pose, x2, y2, z2
//                        ).setWhiteAlpha((int) (alpha*255))
//                        .setUv(1,1)
//                        .setNormal(0, 1, 0)
//                        .setLight(packedLight)
//                        .setOverlay(OverlayTexture.NO_OVERLAY);
//
//                float x3 = (float) (Math.sin(phi) * Math.cos(theta1));
//                float y3 = (float) (Math.cos(phi));
//                float z3 = (float) (Math.sin(phi) * Math.sin(theta1));
//                vertexconsumer.addVertex(pose, x3, y3, z3
//                        ).setWhiteAlpha((int) (alpha*255))
//                        .setUv(0,1)
//                        .setNormal(0, 1, 0)
//                        .setLight(packedLight)
//                        .setOverlay(OverlayTexture.NO_OVERLAY);
//            }
//        }
//
//
//        poseStack.popPose();


        super.render(p_entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }



    public static class Provider implements EntityRendererProvider<NuclearExplosionEntity> {

        @Override
        public @NotNull EntityRenderer<NuclearExplosionEntity> create(@NotNull Context context) {
            return new NuclearExplosionRenderer(context);
        }
    }
}
