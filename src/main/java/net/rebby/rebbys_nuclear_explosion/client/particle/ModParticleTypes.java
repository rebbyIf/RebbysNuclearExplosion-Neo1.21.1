package net.rebby.rebbys_nuclear_explosion.client.particle;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.rebby.rebbys_nuclear_explosion.RebbysNuclearExplosion;

public class ModParticleTypes {

    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(BuiltInRegistries.PARTICLE_TYPE, RebbysNuclearExplosion.MODID);

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> DEBRIS_PARTICLE_TYPE =
            PARTICLE_TYPES.register("debris_particle", () -> new SimpleParticleType(true));

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> PLUME_PARTICLE =
            PARTICLE_TYPES.register("plume", () -> new SimpleParticleType(false));

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> FIREBALL_PARTICLE =
            PARTICLE_TYPES.register("fireball", () -> new SimpleParticleType(false));

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> NUCLEAR_EXPLOSION_PARTICLE =
            PARTICLE_TYPES.register("nuclear_explosion", () -> new SimpleParticleType(false));

    public static void register(IEventBus bus) {
        PARTICLE_TYPES.register(bus);
    }
}
