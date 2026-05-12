package net.rebby.rebbys_nuclear_explosion.entity.client.model;

import net.minecraft.resources.ResourceLocation;
import net.rebby.rebbys_nuclear_explosion.RebbysNuclearExplosion;
import net.rebby.rebbys_nuclear_explosion.entity.custom.NuclearExplosionEntity;
import software.bernie.geckolib.model.GeoModel;

public class NuclearExplosionModel extends GeoModel<NuclearExplosionEntity> {

    private final ResourceLocation model = RebbysNuclearExplosion.getResource("geo/entity/nuclear_explosion.geo.json");
    private final ResourceLocation texture = RebbysNuclearExplosion.getResource( "textures/entity/nuclear_explosion.png");
    private final ResourceLocation animations = RebbysNuclearExplosion.getResource( "animations/entity/nuclear_explosion.animation.json");

    @Override
    public ResourceLocation getModelResource(NuclearExplosionEntity nuclearExplosionEntity) {
        return model;
    }

    @Override
    public ResourceLocation getTextureResource(NuclearExplosionEntity nuclearExplosionEntity) {
        return texture;
    }

    @Override
    public ResourceLocation getAnimationResource(NuclearExplosionEntity nuclearExplosionEntity) {
        return animations;
    }
}
