package net.rebby.rebbys_nuclear_explosion.event;

import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
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

import java.util.List;
import java.util.Objects;

import static net.rebby.rebbys_nuclear_explosion.Config.r;
import static net.rebby.rebbys_nuclear_explosion.Config.r3;


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

                    Vec3 dim = new Vec3(r, r, r);
                    Vec3 o = new Vec3(origin.x, origin.y, origin.z);
                    AABB damageArea = new AABB(o.add(dim), o.subtract(dim));

                    // Gets entities to damage
                    List<Entity> entities = event.getLevel().getEntities((Entity) null, damageArea, entity1 -> {
                        if (entity1.distanceTo(entity) < r3) {
                            return true;
                        }
                        Vec3 eyePos = entity1.getEyePosition();
                        ClipContext context = new ClipContext(o, eyePos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity);
                        return entity1.distanceTo(entity) < r &&
                                event.getLevel().clip(context).getType() == HitResult.Type.MISS;
                    });

                    // Damages them
                    for (Entity entity1 : entities) {
                        float distance = entity1.distanceTo(entity);
                        DamageSource source = new DamageSource(
                                event.getLevel().registryAccess().holderOrThrow(DamageTypes.EXPLOSION),
                                entity
                        );
                        entity1.hurt(source, (float) Math.pow((Config.r3 - distance)/10, 2));
                        entity1.setRemainingFireTicks((int) (Math.pow((r3 - distance)/10, 2) * 5));
                    }

                    //event.getLevel().explode(entity, entity.getX(), entity.getY(), entity.getZ(), (float) Config.r / 2 - 1, Level.ExplosionInteraction.TNT);
                }
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
