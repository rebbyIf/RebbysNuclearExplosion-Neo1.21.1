package net.rebby.rebbys_nuclear_explosion.entity.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.rebby.rebbys_nuclear_explosion.client.rendertype.RenderTypes;
import net.rebby.rebbys_nuclear_explosion.entity.custom.NuclearExplosionEntity;
import net.rebby.rebbys_nuclear_explosion.util.InterpolationMethod;
import org.joml.Vector4f;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.texture.GeoAbstractTexture;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;
import software.bernie.geckolib.util.Color;
import software.bernie.geckolib.util.GeckoLibUtil;

import static net.rebby.rebbys_nuclear_explosion.RebbysNuclearExplosionClient.EXPLOSION_RENDER_TARGET;
import static net.rebby.rebbys_nuclear_explosion.client.rendertype.RenderTypes.largeExplosion;

public class NuclearExplosionDebrisRenderLayer extends GeoRenderLayer<NuclearExplosionEntity> {

    private static final String APPENDIX = "_debris";

    private final int[] colorPos = {200, 300};
    private final Vector4f[] colorVal = {
            new Vector4f(1f, 1f, 1f, 1f),
            new Vector4f(1f, 1f, 1f, 0f)
    };
    private final InterpolationMethod[] colorEasing = {
            null,
            (Float t) -> (float) Math.pow(t, 2)
    };

    public NuclearExplosionDebrisRenderLayer(GeoRenderer<NuclearExplosionEntity> entityRendererIn) {
        super(entityRendererIn);
    }





    @Override
    public void render(PoseStack poseStack, NuclearExplosionEntity animatable, BakedGeoModel bakedModel, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        //poseStack.pushPose();
        RenderType largeExplosionType = RenderTypes.largeExplosion(GeoAbstractTexture.appendToPath(getTextureResource(animatable), APPENDIX));

        Vector4f color = new Vector4f(colorVal[0]);
        float t = animatable.tickCount + partialTick;

        for (int i = 1; i < colorPos.length; i++) {
            if (t < colorPos[i] && t >= colorPos[i - 1]) {
                float t0 = t - colorPos[i - 1];
                float easedT = colorEasing[i].run(t0 / (colorPos[i] - colorPos[i - 1]));
                color = colorVal[i].sub(colorVal[i - 1], new Vector4f()).mul(easedT).add(colorVal[i - 1]);
                break;
            }
        }

        if (t >= colorPos[colorPos.length - 1])
            color = new Vector4f(colorVal[colorPos.length - 1]);


//        EXPLOSION_RENDER_TARGET.setClearColor(0.0f, 0.0f, 0.0f, 0.0f);
//
//        EXPLOSION_RENDER_TARGET.clear(Minecraft.ON_OSX);
//        EXPLOSION_RENDER_TARGET.bindWrite(false);



        getRenderer().reRender(getDefaultBakedModel(animatable), poseStack, bufferSource, animatable, largeExplosionType,
                bufferSource.getBuffer(largeExplosionType), partialTick, packedLight, OverlayTexture.NO_OVERLAY,
                Color.ofARGB(color.w, color.x, color.y, color.z).argbInt());

//        NUCLEAR_EXPLOSION_CHAIN.resize(Minecraft.getInstance().getWindow().getWidth(), Minecraft.getInstance().getWindow().getHeight());
//        NUCLEAR_EXPLOSION_CHAIN.setUniform("Amount", RenderTypes.ModClientEvents.getMixingValue());
//        NUCLEAR_EXPLOSION_CHAIN.process(partialTick);
//
//        Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
//        EXPLOSION_RENDER_TARGET.blitToScreen(Minecraft.getInstance().getWindow().getWidth(), Minecraft.getInstance().getWindow().getHeight(), false);
//

        //poseStack.popPose();

    }
}
