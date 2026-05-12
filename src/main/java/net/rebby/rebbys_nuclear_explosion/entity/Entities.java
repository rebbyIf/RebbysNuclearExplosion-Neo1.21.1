package net.rebby.rebbys_nuclear_explosion.entity;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.rebby.rebbys_nuclear_explosion.RebbysNuclearExplosion;
import net.rebby.rebbys_nuclear_explosion.entity.custom.NuclearExplosionEntity;

public class Entities {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, RebbysNuclearExplosion.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<NuclearExplosionEntity>> NUCLEAR_EXPLOSION =
            ENTITY_TYPES.register("nuclear_explosion", () ->
            EntityType.Builder.of(NuclearExplosionEntity::new, MobCategory.MISC)
            .fireImmune()
            .clientTrackingRange(256)
            .setTrackingRange(256)
            .updateInterval(1)
            .setShouldReceiveVelocityUpdates(false)
            .sized(4.0f,8.0f)
            .build(NuclearExplosionEntity.ID.toString()));

    public static void register(IEventBus bus) {
        ENTITY_TYPES.register(bus);
    }

}
