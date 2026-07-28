package net.rebby.rebbys_nuclear_explosion.client.particle;

import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.ParticleDescriptionProvider;

public class ModParticleDescriptionProvider extends ParticleDescriptionProvider {
    public ModParticleDescriptionProvider(PackOutput output, ExistingFileHelper fileHelper) {
        super(output, fileHelper);
    }

    @Override
    protected void addDescriptions() {
        sprite(ModParticleTypes.DEBRIS_PARTICLE_TYPE.get(), ModParticleTypes.DEBRIS_PARTICLE_TYPE.getId());
    }
}
