package net.rebby.rebbys_nuclear_explosion.entity.render;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.rebby.rebbys_nuclear_explosion.entity.Entities;
import net.rebby.rebbys_nuclear_explosion.entity.custom.NuclearExplosionEntity;
import org.jetbrains.annotations.NotNull;

public class NuclearExplosionRenderer extends EntityRenderer<NuclearExplosionEntity> {
    protected NuclearExplosionRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull NuclearExplosionEntity nuclearExplosionEntity) {
        return Entities.NUCLEAR_EXPLOSION.getId();
    }

    public static class Provider implements EntityRendererProvider<NuclearExplosionEntity> {

        @Override
        public @NotNull EntityRenderer<NuclearExplosionEntity> create(@NotNull Context context) {
            return new NuclearExplosionRenderer(context);
        }
    }
}
