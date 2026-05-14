package net.rebby.rebbys_nuclear_explosion.entity.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.rebby.rebbys_nuclear_explosion.entity.custom.NuclearExplosionEntity;
import net.rebby.rebbys_nuclear_explosion.util.InterpolationMethod;
import org.joml.Vector4f;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.texture.AutoGlowingTexture;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;
import software.bernie.geckolib.util.Color;

public class NuclearExplosionPlasmaRenderLayer extends AutoGlowingGeoLayer<NuclearExplosionEntity> {


    private final int[] colorPos = {0, 20, 40};
    private final Vector4f[] colorVal = {
            new Vector4f(1f, 1f,1f,1f),
            new Vector4f(1f, 1f, 1f, 0.8f),
            new Vector4f(1f, 1f, 1f, 0f)
    };
    private final InterpolationMethod[] colorEasing = {
            null,
            (Float t) -> (float) (1 - Math.pow(1 - t, 3)),
            (Float t) -> (float) (Math.pow(t, 2))
    };

    public NuclearExplosionPlasmaRenderLayer(GeoRenderer<NuclearExplosionEntity> renderer) {
        super(renderer);
    }



    @Override
    public void render(PoseStack poseStack, NuclearExplosionEntity animatable, BakedGeoModel bakedModel, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        //System.out.println("Render Layer is actually Rendering");

        Vector4f color = new Vector4f(colorVal[0]);
        float t = animatable.tickCount + partialTick;

        for (int i = 1; i < colorPos.length; i++) {
            if (t < colorPos[i] && t >= colorPos[i-1]) {
                float t0 = t - colorPos[i-1];
                float easedT = colorEasing[i].run(t0 / (colorPos[i] - colorPos[i-1]));
                color = colorVal[i].sub(colorVal[i-1], new Vector4f()).mul(easedT).add(colorVal[i-1]);
                break;
            }
        }

        if (t >= colorPos[colorPos.length - 1])
            color = new Vector4f(colorVal[colorPos.length - 1]);

        RenderType emissiveRenderType = AutoGlowingTexture.getRenderType(getTextureResource(animatable));

        getRenderer().reRender(bakedModel, poseStack, bufferSource, animatable, emissiveRenderType,
                bufferSource.getBuffer(emissiveRenderType), partialTick, LightTexture.FULL_SKY, OverlayTexture.NO_OVERLAY,
                Color.ofARGB(color.w, color.x, color.y, color.z).argbInt());
    }
}
