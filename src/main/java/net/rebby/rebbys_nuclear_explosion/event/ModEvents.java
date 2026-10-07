package net.rebby.rebbys_nuclear_explosion.event;

import com.ibm.icu.impl.Pair;
import foundry.veil.api.client.render.VeilRenderSystem;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.rebby.rebbys_nuclear_explosion.Config;
import net.rebby.rebbys_nuclear_explosion.RebbysNuclearExplosion;
import net.rebby.rebbys_nuclear_explosion.client.Sounds;
import net.rebby.rebbys_nuclear_explosion.client.rendering.PostProcessing;
import net.rebby.rebbys_nuclear_explosion.datagen.ModDamageTypes;
import net.rebby.rebbys_nuclear_explosion.datagen.ModDatapackProvider;
import net.rebby.rebbys_nuclear_explosion.entity.Entities;
import net.rebby.rebbys_nuclear_explosion.entity.custom.NuclearExplosionEntity;
import net.rebby.rebbys_nuclear_explosion.entity.render.NuclearExplosionRenderer;
import net.rebby.rebbys_nuclear_explosion.util.Irradiation;
import net.rebby.rebbys_nuclear_explosion.util.Timeline;
import org.joml.Vector3i;

import java.util.List;


public class ModEvents {

    @EventBusSubscriber(value = Dist.CLIENT, modid = RebbysNuclearExplosion.MODID)
    public static class ClientEvents {

        @SubscribeEvent // on the mod event bus only on the physical client
        public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(Entities.NUCLEAR_EXPLOSION.get(), new NuclearExplosionRenderer.Provider());
        }

    }

    @EventBusSubscriber(modid = RebbysNuclearExplosion.MODID)
    public static class ForgeEvents {

        @SubscribeEvent
        public static void gatherData(GatherDataEvent event){
            DataGenerator generator = event.getGenerator();
            PackOutput packOutput = generator.getPackOutput();
            var lookupProvider = event.getLookupProvider();

            generator.addProvider(event.includeServer(), new ModDatapackProvider(packOutput, lookupProvider));
        }

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
                        if (entity1.distanceTo(entity) < Config.r2) {
                            return true;
                        }
                        Vec3 eyePos = entity1.getEyePosition();
                        ClipContext context = new ClipContext(o, eyePos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity);
                        return entity1.distanceTo(entity) < Config.r &&
                                event.getLevel().clip(context).getType() != HitResult.Type.BLOCK;
                    });

                    Timeline damageTimeline = new Timeline(Pair.of(0.0f, new Float[]{2000.0f}))
                            .pushEntry(Pair.of((float)Config.r2, new Float[]{1000.0f}), Timeline.InterpolationMethod.CUBIC_EASE_IN)
                            .pushEntry(Pair.of((float)Config.r1, new Float[]{100.0f}), Timeline.InterpolationMethod.CUBIC_EASE_OUT)
                            .pushEntry(Pair.of((float)Config.r0, new Float[]{0.0f}), Timeline.InterpolationMethod.EASE_OUT);
                    // Damages them
                    for (Entity entity1 : entities) {
                        float distance = entity1.distanceTo(entity);
                        float damage = damageTimeline.interpolate(distance)[0];
                        DamageSource source = new DamageSource(
                                ModDamageTypes.create(event.getLevel(), ModDamageTypes.RADIATION).typeHolder(),
                                entity
                        );
                        entity1.hurt(source, damage);
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
