package net.rebby.rebbys_nuclear_explosion.client.rendertype;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.rebby.rebbys_nuclear_explosion.Config;
import net.rebby.rebbys_nuclear_explosion.RebbysNuclearExplosion;
import net.rebby.rebbys_nuclear_explosion.entity.custom.NuclearExplosionEntity;
import net.rebby.rebbys_nuclear_explosion.util.InterpolationMethod;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

public class RenderTypes {

    public static final ResourceLocation RADIATION_SHADER_LOC = RebbysNuclearExplosion.getResource("shaders/post/radiation.json");

    public static PostChain RADIATION_CHAIN;

    static {
        Minecraft mc = Minecraft.getInstance();
        try {
            RADIATION_CHAIN = new PostChain(mc.getTextureManager(), mc.getResourceManager(), mc.getMainRenderTarget(), RADIATION_SHADER_LOC);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static final int[] MIXING_POS = {0, 20, 300};

    private static final float[] MIXING_VAL = {0.0f, 1.0f, 0.0f};

    private static final InterpolationMethod[] MIXING_EASING = {
            null,
            (Float t) -> (float) (1 - Math.pow(1 - t, 3)),
            (Float t) -> (float) (Math.pow(t, 2))
    };



    public static RenderType largeExplosion(ResourceLocation texture) {
        return CustomRenderTypes.LARGE_EXPLOSION.apply(texture);
    }

    @EventBusSubscriber(value = Dist.CLIENT, modid = RebbysNuclearExplosion.MODID)
    public static class ModClientEvents {
        private static float mixingValue;

        public static float getMixingValue() {
            return mixingValue;
        }

        @SubscribeEvent
        public static void shaderRegistry(RegisterShadersEvent event) throws IOException {
            event.registerShader(new ShaderInstance(event.getResourceProvider(),
                    RebbysNuclearExplosion.getResource("rendertype_large_explosion"),
                    DefaultVertexFormat.NEW_ENTITY), shaderInstance ->
                    CustomRenderTypes.largeExplosionShader = shaderInstance);

        }

        @SubscribeEvent
        public static void renderRadiationOverlay(RenderLevelStageEvent event){
            Minecraft mc = Minecraft.getInstance();
            if (event.getStage().equals(RenderLevelStageEvent.Stage.AFTER_LEVEL) &&
                    mc.level != null &&
                    mc.player != null) {
                AtomicReference<NuclearExplosionEntity> nearestNuke = new AtomicReference<>();
                AtomicReference<Float> distance = new AtomicReference<>(-1.0f);

                mc.level.entitiesForRendering().forEach(entity -> {
                    if (entity instanceof NuclearExplosionEntity explosionEntity &&
                            explosionEntity.getAge() < 300 &&
                            (explosionEntity.distanceTo(mc.player) < distance.get() || distance.get() < 0)) {

                        distance.set(explosionEntity.distanceTo(mc.player));
                        nearestNuke.set(explosionEntity);
                    }
                });

                if (distance.get() >= 0 && distance.get() < 32 * Config.r) {

                    // Calculates Timeline of events
                    float mixing = MIXING_VAL[0];
                    float t = nearestNuke.get().getAge() + event.getPartialTick().getGameTimeDeltaPartialTick(true);

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
                    mixingValue = mixing * (float) (1 - Math.pow(distance.get() / (32 * Config.r), 2));

                    RADIATION_CHAIN.resize(mc.getWindow().getWidth(), mc.getWindow().getHeight());
                    RADIATION_CHAIN.setUniform("Amount", mixingValue);
                    RADIATION_CHAIN.process(event.getPartialTick().getRealtimeDeltaTicks());
                    mc.getMainRenderTarget().bindWrite(false);




                }

                //EXPLOSION_RENDER_TARGET.blitToScreen(Minecraft.getInstance().getWindow().getWidth(), Minecraft.getInstance().getWindow().getHeight());
            }
        }
    }

    private static class CustomRenderTypes extends RenderType {

        public static ShaderInstance largeExplosionShader;

        private static final ShaderStateShard RENDERTYPE_LARGE_EXPLOSION_SHADER = new ShaderStateShard(() -> largeExplosionShader);

        private CustomRenderTypes(String pName, VertexFormat pFormat, VertexFormat.Mode pMode, int pBufferSize, boolean pAffectsCrumbling, boolean pSortOnUpload, Runnable pSetupState, Runnable pClearState) {
            super(pName, pFormat, pMode, pBufferSize, pAffectsCrumbling, pSortOnUpload, pSetupState, pClearState);
            throw new IllegalArgumentException("This class isn't supposed to be constructed!");
        }

        public static Function<ResourceLocation, RenderType> LARGE_EXPLOSION = Util.memoize(CustomRenderTypes::largeExplosion);

        private static RenderType largeExplosion(ResourceLocation location) {
            CompositeState renderType$state = CompositeState.builder()
                    .setShaderState(RENDERTYPE_LARGE_EXPLOSION_SHADER)
                    .setTextureState(new TextureStateShard(location, false, false))
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setLightmapState(LIGHTMAP)
                    .setOverlayState(OVERLAY)
                    .setCullState(NO_CULL)
                    .createCompositeState(true);
            return create("large_explosion", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, true, false, renderType$state);
        }
    }
}
