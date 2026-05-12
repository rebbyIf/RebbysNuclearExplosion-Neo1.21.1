package net.rebby.rebbys_nuclear_explosion.entity.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.rebby.rebbys_nuclear_explosion.Config;
import net.rebby.rebbys_nuclear_explosion.entity.client.model.NuclearExplosionModel;
import net.rebby.rebbys_nuclear_explosion.entity.custom.NuclearExplosionEntity;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.util.Color;

public class NuclearExplosionRenderer extends GeoEntityRenderer<NuclearExplosionEntity> {


    public NuclearExplosionRenderer(EntityRendererProvider.Context context) {
        super(context, new NuclearExplosionModel());

        addRenderLayer(new NuclearExplosionDebrisRenderLayer(this));
        addRenderLayer(new NuclearExplosionPlasmaRenderLayer(this));
    }

    @Override
    public boolean shouldShowName(@NotNull NuclearExplosionEntity animatable) {
        return false;
    }

    @Override
    public void render(@NotNull NuclearExplosionEntity entity, float entityYaw, float partialTick, PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight) {
        poseStack.scale((float) Config.nukeModelSize, (float) Config.nukeModelSize, (float) Config.nukeModelSize);



        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }
}
