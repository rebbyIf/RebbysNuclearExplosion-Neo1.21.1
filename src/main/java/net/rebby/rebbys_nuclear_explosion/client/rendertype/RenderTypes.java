package net.rebby.rebbys_nuclear_explosion.client.rendertype;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import net.rebby.rebbys_nuclear_explosion.RebbysNuclearExplosion;

import java.io.IOException;
import java.util.function.Function;

public class RenderTypes {

    public static RenderType largeExplosion(ResourceLocation texture) {
        return CustomRenderTypes.LARGE_EXPLOSION.apply(texture);
    }

    @EventBusSubscriber(value = Dist.CLIENT, modid = RebbysNuclearExplosion.MODID)
    public static class ModClientEvents {
        @SubscribeEvent
        public static void shaderRegistry(RegisterShadersEvent event) throws IOException {
            event.registerShader(new ShaderInstance(event.getResourceProvider(),
                    RebbysNuclearExplosion.getResource("rendertype_large_explosion"),
                    DefaultVertexFormat.NEW_ENTITY), shaderInstance -> {
                CustomRenderTypes.largeExplosionShader = shaderInstance;
            });
        }
    }

    private static class CustomRenderTypes extends RenderType {

        private static ShaderInstance largeExplosionShader;

        private static final ShaderStateShard RENDERTYPE_LARGE_EXPLOSION_SHADER = new ShaderStateShard(() -> largeExplosionShader);

        private CustomRenderTypes(String pName, VertexFormat pFormat, VertexFormat.Mode pMode, int pBufferSize, boolean pAffectsCrumbling, boolean pSortOnUpload, Runnable pSetupState, Runnable pClearState) {
            super(pName, pFormat, pMode, pBufferSize, pAffectsCrumbling, pSortOnUpload, pSetupState, pClearState);
            throw new IllegalArgumentException("This class isn't supposed to be constructed!");
        }

        public static Function<ResourceLocation, RenderType> LARGE_EXPLOSION = Util.memoize(CustomRenderTypes::large_explosion);

        private static RenderType large_explosion(ResourceLocation location) {
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
