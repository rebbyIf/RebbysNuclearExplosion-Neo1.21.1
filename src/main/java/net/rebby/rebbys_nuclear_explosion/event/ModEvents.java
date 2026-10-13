package net.rebby.rebbys_nuclear_explosion.event;

import com.ibm.icu.impl.Pair;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.rendertype.VeilRenderType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.rebby.rebbys_nuclear_explosion.Config;
import net.rebby.rebbys_nuclear_explosion.RebbysNuclearExplosion;
import net.rebby.rebbys_nuclear_explosion.client.Sounds;
import net.rebby.rebbys_nuclear_explosion.client.rendering.PostProcessing;
import net.rebby.rebbys_nuclear_explosion.datagen.ModDamageTypes;
import net.rebby.rebbys_nuclear_explosion.datagen.ModDatapackProvider;
import net.rebby.rebbys_nuclear_explosion.entity.Entities;
import net.rebby.rebbys_nuclear_explosion.entity.custom.NuclearExplosionEntity;
import net.rebby.rebbys_nuclear_explosion.entity.render.NuclearExplosionRenderer;
import net.rebby.rebbys_nuclear_explosion.networking.ClientPayloadHandler;
import net.rebby.rebbys_nuclear_explosion.networking.NukeData;
import net.rebby.rebbys_nuclear_explosion.util.Irradiation;
import net.rebby.rebbys_nuclear_explosion.util.Timeline;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.util.List;


public class ModEvents {

    @EventBusSubscriber(value = Dist.CLIENT, modid = RebbysNuclearExplosion.MODID)
    public static class ClientEvents {

        private static final int N_SLICES = 32;
        private static final int N_STACKS = 16;

        private static final Timeline SIZE = new Timeline(1)
                .pushEntry(Pair.of(20.0f,new Float[]{0.6f}), Timeline.InterpolationMethod.CUBIC_EASE_OUT)
                .pushEntry(Pair.of(100.0f,new Float[]{0.9f}), Timeline.InterpolationMethod.LINEAR)
                .pushEntry(Pair.of(250.0f,new Float[]{1.1f}), Timeline.InterpolationMethod.EASE_OUT)
                .pushEntry(Pair.of(400.0f,new Float[]{1.0f}), Timeline.InterpolationMethod.EASE_IN);

        private static final Timeline ALPHA = new Timeline(Pair.of(300.0f, new Float[]{1.0f}))
                .pushEntry(Pair.of(400.0f,new Float[]{0.0f}), Timeline.InterpolationMethod.CUBIC_EASE_OUT);

        public static float getSize(float age){
            return SIZE.interpolate(age)[0] * Config.r3;
        }

        @SubscribeEvent // on the mod event bus only on the physical client
        public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(Entities.NUCLEAR_EXPLOSION.get(), new NuclearExplosionRenderer.Provider());
        }

        @SubscribeEvent
        public static void renderPlasmaSpheres(RenderLevelStageEvent event){
            if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
                RenderType renderType = VeilRenderType.get(NuclearExplosionRenderer.RENDER_TYPE_ID);

                if (renderType == null){
                    RebbysNuclearExplosion.LOGGER.error("Error in getting RenderType");
                    return;
                }

                PoseStack poseStack = event.getPoseStack();
                MultiBufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
                int packedLight = 15728880;

                for (NukeData value : ClientPayloadHandler.getNukeMapValues()) {
                    if (value == null) continue;
                    Vec3 translation = new Vec3(
                            value.pos().x - event.getCamera().getPosition().x,
                            value.pos().y - event.getCamera().getPosition().y,
                            value.pos().z - event.getCamera().getPosition().z);
                    if (translation.distanceTo(Vec3.ZERO) > Config.visibilityRange) continue;
                    VertexConsumer vertexconsumer = bufferSource.getBuffer(renderType);
                    poseStack.pushPose();

                    float size = SIZE.interpolate(value.age())[0] * Config.r3;
                    float alpha = ALPHA.interpolate(value.age())[0];

                    poseStack.translate(translation.x,translation.y, translation.z);
                    poseStack.scale(size,size,size);
                    PoseStack.Pose pose = poseStack.last();

                    for (int i = 0; i < N_STACKS; i++) {
                        double phi = Math.PI * i / N_STACKS;
                        double phi1 = Math.PI * (i + 1) / N_STACKS;
                        for (int j = 0; j < N_SLICES; j++) {
                            double theta = 2.0 * Math.PI * j / N_SLICES;
                            double theta1 = 2.0 * Math.PI * (j + 1) / N_SLICES;

                            float x0 = (float) (Math.sin(phi) * Math.cos(theta));
                            float y0 = (float) (Math.cos(phi));
                            float z0 = (float) (Math.sin(phi) * Math.sin(theta));
                            vertexconsumer.addVertex(pose, x0, y0, z0
                                    ).setWhiteAlpha((int) (alpha*255))
                                    .setUv(0,0)
                                    .setNormal(0, 1, 0)
                                    .setLight(packedLight)
                                    .setOverlay(OverlayTexture.NO_OVERLAY);

                            float x1 = (float) (Math.sin(phi1) * Math.cos(theta));
                            float y1 = (float) (Math.cos(phi1));
                            float z1 = (float) (Math.sin(phi1) * Math.sin(theta));
                            vertexconsumer.addVertex(pose, x1, y1, z1
                                    ).setWhiteAlpha((int) (alpha*255))
                                    .setUv(1,0)
                                    .setNormal(0, 1, 0)
                                    .setLight(packedLight)
                                    .setOverlay(OverlayTexture.NO_OVERLAY);

                            float x2 = (float) (Math.sin(phi1) * Math.cos(theta1));
                            float y2 = (float) (Math.cos(phi1));
                            float z2 = (float) (Math.sin(phi1) * Math.sin(theta1));
                            vertexconsumer.addVertex(pose, x2, y2, z2
                                    ).setWhiteAlpha((int) (alpha*255))
                                    .setUv(1,1)
                                    .setNormal(0, 1, 0)
                                    .setLight(packedLight)
                                    .setOverlay(OverlayTexture.NO_OVERLAY);

                            float x3 = (float) (Math.sin(phi) * Math.cos(theta1));
                            float y3 = (float) (Math.cos(phi));
                            float z3 = (float) (Math.sin(phi) * Math.sin(theta1));
                            vertexconsumer.addVertex(pose, x3, y3, z3
                                    ).setWhiteAlpha((int) (alpha*255))
                                    .setUv(0,1)
                                    .setNormal(0, 1, 0)
                                    .setLight(packedLight)
                                    .setOverlay(OverlayTexture.NO_OVERLAY);
                        }
                    }


                    poseStack.popPose();
                }
            }
        }

    }

    @EventBusSubscriber(modid = RebbysNuclearExplosion.MODID)
    public static class ForgeEvents {

        @SubscribeEvent // on the mod event bus
        public static void register(final RegisterPayloadHandlersEvent event) {
            final PayloadRegistrar registrar = event.registrar("1");
            registrar.playToClient(
                    NukeData.TYPE,
                    NukeData.STREAM_CODEC,
                    ClientPayloadHandler::handleNukeDataOnMain
            );
        }

        @SubscribeEvent
        public static void gatherData(GatherDataEvent event){
            DataGenerator generator = event.getGenerator();
            PackOutput packOutput = generator.getPackOutput();
            var lookupProvider = event.getLookupProvider();

            generator.addProvider(event.includeServer(), new ModDatapackProvider(packOutput, lookupProvider));
        }

        @SubscribeEvent
        public static void onLevelUnloaded(LevelEvent.Unload event) {
            if (event.getLevel().isClientSide()) {
                ClientPayloadHandler.refreshNukeMap();
            }
        }

        @SubscribeEvent
        public static void onEntityLeave(EntityLeaveLevelEvent event) {
            if (event.getLevel().isClientSide) {
                return;
            }
            if (event.getEntity() instanceof NuclearExplosionEntity entity){

                PacketDistributor.sendToAllPlayers(new NukeData(
                        entity.getAge(),
                        entity.getR(),
                        new Vector3f((float) entity.getX(), (float) entity.getY(), (float) entity.getZ()),
                        entity.getId(),
                        true));

                for (int x = entity.chunkPosition().x - Config.r / 16; x < entity.chunkPosition().x + Config.r / 16; x++) {
                    for (int z = entity.chunkPosition().z - Config.r / 16; z < entity.chunkPosition().z + Config.r / 16; z++) {
                        ((ServerLevel) event.getLevel()).setChunkForced(x,z,false);
                    }
                }
            }
        }

        @SubscribeEvent
        public static void onEntitySpawn(EntityJoinLevelEvent event) {

            if (event.getEntity() instanceof NuclearExplosionEntity entity) {

                if (event.getLevel().isClientSide) {
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
                            .pushEntry(Pair.of((float)Config.r2, new Float[]{500.0f}), Timeline.InterpolationMethod.CUBIC_EASE_IN)
                            .pushEntry(Pair.of((float)Config.r1, new Float[]{20.0f}), Timeline.InterpolationMethod.CUBIC_EASE_OUT)
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
