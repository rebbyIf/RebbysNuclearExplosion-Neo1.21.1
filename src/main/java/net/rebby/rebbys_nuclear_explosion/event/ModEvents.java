package net.rebby.rebbys_nuclear_explosion.event;

import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.event.VeilPostProcessingEvent;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.registration.HandlerThread;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.rebby.rebbys_nuclear_explosion.Config;
import net.rebby.rebbys_nuclear_explosion.RebbysNuclearExplosion;
import net.rebby.rebbys_nuclear_explosion.client.Sounds;
import net.rebby.rebbys_nuclear_explosion.client.particle.DebrisParticle;
import net.rebby.rebbys_nuclear_explosion.client.particle.ModParticleTypes;
import net.rebby.rebbys_nuclear_explosion.client.rendertype.PostProcessing;
import net.rebby.rebbys_nuclear_explosion.entity.Entities;
import net.rebby.rebbys_nuclear_explosion.entity.custom.NuclearExplosionEntity;
import net.rebby.rebbys_nuclear_explosion.entity.render.NuclearExplosionRenderer;
import net.rebby.rebbys_nuclear_explosion.util.Irradiation;
import org.joml.Vector3i;

import java.util.List;


public class ModEvents {

    @EventBusSubscriber(value = Dist.CLIENT, modid = RebbysNuclearExplosion.MODID)
    public static class ClientEvents {

        @SubscribeEvent // on the mod event bus only on the physical client
        public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
            event.registerSpriteSet(ModParticleTypes.DEBRIS_PARTICLE_TYPE.get(), DebrisParticle.Provider::new);
        }

        @SubscribeEvent // on the mod event bus only on the physical client
        public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(Entities.NUCLEAR_EXPLOSION.get(), new NuclearExplosionRenderer.Provider());
        }

    }

    @EventBusSubscriber(modid = RebbysNuclearExplosion.MODID)
    public static class ForgeEvents {

        @SubscribeEvent
        public static void onEntityLeave(EntityLeaveLevelEvent event) {
            if (event.getEntity() instanceof NuclearExplosionEntity entity){
                if (event.getLevel().isClientSide) {
                    return;
                }
                for (int x = entity.chunkPosition().x - Config.r0 / 16; x < entity.chunkPosition().x + Config.r0 / 16; x++) {
                    for (int z = entity.chunkPosition().z - Config.r0 / 16; z < entity.chunkPosition().z + Config.r0 / 16; z++) {
                        ((ServerLevel) event.getLevel()).setChunkForced(x,z,false);
                    }
                }
            }
        }

        @SubscribeEvent
        public static void onEntitySpawn(EntityJoinLevelEvent event) {

            if (event.getEntity() instanceof NuclearExplosionEntity entity) {

                if (event.getLevel().isClientSide) {
                    if (!VeilRenderSystem.renderer().getPostProcessingManager().isActive(PostProcessing.RADIATION_POST_PIPELINE))
                        VeilRenderSystem.renderer().getPostProcessingManager().add(PostProcessing.RADIATION_POST_PIPELINE);
                    if (!VeilRenderSystem.renderer().getPostProcessingManager().isActive(PostProcessing.DARKEN_SKY_POST_PIPELINE))
                        VeilRenderSystem.renderer().getPostProcessingManager().add(PostProcessing.DARKEN_SKY_POST_PIPELINE);
                    return;
                }

                entity.setCustomNameVisible(false);

//                level.getPlayers(serverPlayer -> serverPlayer.distanceTo(entity) < 1000)
//                        .forEach(serverPlayer -> PacketDistributor.sendToPlayer(serverPlayer, new AdvancedAddEntityPayload(entity)));
                
                if (entity.getAge() == 0) {

                    ((ServerLevel) event.getLevel()).setChunkForced(entity.chunkPosition().x,entity.chunkPosition().z, true);

                    entity.setInvulnerable(true);
                    Vector3i origin = new Vector3i((int) entity.position().x, (int) entity.position().y, (int) entity.position().z);
                    Vector3i origin1 = origin.sub(1, 1, 1, new Vector3i());
                    entity.setIrradiation(new Irradiation(origin, origin, origin1, origin, origin1, 0));

                    Vec3 dim = new Vec3(Config.r, Config.r, Config.r);
                    Vec3 o = new Vec3(origin.x, origin.y, origin.z);
                    AABB damageArea = new AABB(o.add(dim), o.subtract(dim));

                    // Gets entities to damage
                    List<Entity> entities = event.getLevel().getEntities((Entity) null, damageArea, entity1 -> {
                        if (entity1.distanceTo(entity) < Config.r3) {
                            return true;
                        }
                        Vec3 eyePos = entity1.getEyePosition();
                        ClipContext context = new ClipContext(o, eyePos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity);
                        return entity1.distanceTo(entity) < Config.r &&
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
                        entity1.setRemainingFireTicks((int) (Math.pow((Config.r3 - distance)/10, 2) * 5));
                    }

                    entity.playSound(Sounds.NUCLEAR_EXPLOSION_AMBIENCE.value(), 128.0f,1.0f);



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
