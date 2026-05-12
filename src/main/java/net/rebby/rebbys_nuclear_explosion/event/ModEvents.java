package net.rebby.rebbys_nuclear_explosion.event;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.rebby.rebbys_nuclear_explosion.Config;
import net.rebby.rebbys_nuclear_explosion.RebbysNuclearExplosion;
import net.rebby.rebbys_nuclear_explosion.entity.Entities;
import net.rebby.rebbys_nuclear_explosion.entity.custom.NuclearExplosionEntity;
import net.rebby.rebbys_nuclear_explosion.util.Irradiation;
import org.joml.Vector3i;

import java.util.Objects;


public class ModEvents {

    @EventBusSubscriber(modid = RebbysNuclearExplosion.MODID)
    public static class ForgeEvents {

        @SubscribeEvent
        public static void onEntitySpawn(EntityJoinLevelEvent event) {
            if (!event.getLevel().isClientSide && event.getEntity() instanceof NuclearExplosionEntity entity) {

                entity.setCustomNameVisible(false);
                if (entity.getAge() == 0) {

                    for (int x = entity.chunkPosition().x - Config.r0 / 16; x < entity.chunkPosition().x + Config.r0 / 16; x++) {
                        for (int z = entity.chunkPosition().z - Config.r0 / 16; z < entity.chunkPosition().z + Config.r0 / 16; z++) {
                            ((ServerLevel) event.getLevel()).setChunkForced(x,z, true);
                        }
                    }

                    entity.setInvulnerable(true);
                    Vector3i origin = new Vector3i((int) entity.position().x, (int) entity.position().y, (int) entity.position().z);
                    Vector3i origin1 = origin.sub(1, 1, 1, new Vector3i());
                    entity.setIrradiation(new Irradiation(origin, origin, origin1, origin, origin1, 0));

                    event.getLevel().explode(entity, entity.getX(), entity.getY(), entity.getZ(), (float) Config.r / 2 - 1, Level.ExplosionInteraction.TNT);
                }
            }
        }

        @SubscribeEvent
        public static void onNukeDamage(LivingIncomingDamageEvent event) {
            if (Objects.requireNonNull(event.getSource().getEntity()).getType().equals(Entities.NUCLEAR_EXPLOSION.get())) {
                event.getEntity().setRemainingFireTicks(20);
            }
        }

    }

    @EventBusSubscriber(modid = RebbysNuclearExplosion.MODID)
    public static class ModEventBusEvents {

        @SubscribeEvent
        public static void entityAttributeEvent(EntityAttributeCreationEvent event) {
            event.put(Entities.NUCLEAR_EXPLOSION.get(), NuclearExplosionEntity.setAttributes());
        }

    }
}
