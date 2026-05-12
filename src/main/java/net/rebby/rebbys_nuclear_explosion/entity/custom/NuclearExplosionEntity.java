package net.rebby.rebbys_nuclear_explosion.entity.custom;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Unit;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.world.chunk.ForcedChunkManager;
import net.rebby.rebbys_nuclear_explosion.Config;
import net.rebby.rebbys_nuclear_explosion.RebbysNuclearExplosion;
import net.rebby.rebbys_nuclear_explosion.util.Irradiation;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3i;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class NuclearExplosionEntity extends LivingEntity implements GeoEntity {
    public static final ResourceLocation ID = RebbysNuclearExplosion.getResource("nuclear_explosion");

    public static final String IRRADIATION_ID = setNBTId("irradiation");
    public static final String IS_DETONATING_ID = setNBTId("isDetonating");
    public static final String AGE_ID = setNBTId("age");

    protected static final RawAnimation DETONATION_ANIM = RawAnimation.begin()
            .thenPlay("animation.nuclear_explosion.detonate")
            .thenLoop("animation.nuclear_explosion.ambient");
    protected static final RawAnimation AMBIENT_ANIM = RawAnimation.begin()
            .thenLoop("animation.nuclear_explosion.ambient");

    private static String setNBTId(String name) {
        return name;
    }

    private final NonNullList<ItemStack> handItems = NonNullList.withSize(2, ItemStack.EMPTY);
    private final NonNullList<ItemStack> armorItems = NonNullList.withSize(4, ItemStack.EMPTY);

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private Irradiation irradiation;
    private int age;
    private boolean isDetonating;

    public NuclearExplosionEntity(EntityType<? extends LivingEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        Vector3i origin = new Vector3i(blockPosition().getX(), blockPosition().getY(), blockPosition().getZ());
        Vector3i origin1 = origin.sub(1,1,1,new Vector3i());
        irradiation = new Irradiation(origin, origin, origin1, origin, origin1, 0);
        age = 0;
        isDetonating = true;
        setNoGravity(true);
    }

    @Override
    public boolean canCollideWith(@NotNull Entity pEntity) {
        return false;
    }

    public static AttributeSupplier setAttributes() {

        return LivingEntity.createLivingAttributes()
                .add(Attributes.FLYING_SPEED, 0.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .build();
    }

    @Override
    public boolean isCustomNameVisible() {
        return false; // Name tag will never show
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.literal("Nuclear Explosion");
    }

    public Irradiation getIrradiation() {
        return irradiation;
    }

    public void setIrradiation(Irradiation irradiation) {
        this.irradiation = irradiation;
    }

    public boolean isDetonating() {
        return isDetonating;
    }

    public void setDetonating(boolean detonating) {
        isDetonating = detonating;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    @Override
    public boolean isInvulnerableTo(DamageSource pSource) {
        return !pSource.is(DamageTypes.GENERIC_KILL);
    }

    @Override
    public boolean isAlwaysTicking() {
        return true;
    }

    @Override
    public @NotNull AABB getBoundingBoxForCulling() {
        return new AABB(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
    }



    @Override
    public boolean shouldRenderAtSqrDistance(double pDistance) {
        double d0 = this.getBoundingBox().getSize() * 128.0D;
        if (Double.isNaN(d0) || d0 == 0.0D) {
            d0 = 128.0D;
        }

        d0 *= 128.0D;
        return pDistance < d0 * d0;
    }

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide || irradiation == null) {
            return;
        }

        irradiation.setOrigin(new Vector3i((int) position().x, (int) position().y, (int) position().z));

        //System.out.println("Ticking...");

        if (age > 300) {
            setInvisible(true);
        }

        if (isDetonating && age > 20) {
            isDetonating = irradiation.irradiateThreaded((ServerLevel) level());
            if (age % 3 == 0)
                playSound(SoundEvents.LIGHTNING_BOLT_THUNDER, 128.0f, 1.0f);
            //System.out.println(isDetonating);
        }

        if (!isDetonating && age > 300) {
            for (int x = chunkPosition().x - Config.r0 / 16; x < chunkPosition().x + Config.r0 / 16; x++) {
                for (int z = chunkPosition().z - Config.r0 / 16; z < chunkPosition().z + Config.r0 / 16; z++) {
                    ((ServerLevel) level()).setChunkForced(x,z,false);
                }
            }
            remove(RemovalReason.DISCARDED);
        }
        age++;
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag pCompound) {
        CompoundTag modData = pCompound.getCompound(RebbysNuclearExplosion.MODID);
        if (modData.contains(IRRADIATION_ID)) {
            DataResult<Irradiation> result = Irradiation.IRRADIATION_CODEC.parse(NbtOps.INSTANCE, modData.get(IRRADIATION_ID));
            result.resultOrPartial(errorMessage -> LogUtils.getLogger().warn("Nuclear Explosion Loading Error: {}", errorMessage))
                    .ifPresent(decodedObject -> irradiation = decodedObject);
        }

        if (modData.contains(IS_DETONATING_ID))
            isDetonating = modData.getBoolean(IS_DETONATING_ID);
        if (modData.contains(AGE_ID))
            age = modData.getInt(AGE_ID);

        super.readAdditionalSaveData(pCompound);

    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag pCompound) {

        CompoundTag modData = new CompoundTag();

        if (irradiation == null) {
            return;
        }

        DataResult<Tag> result = Irradiation.IRRADIATION_CODEC.encodeStart(NbtOps.INSTANCE, irradiation);
        result.resultOrPartial(errorMessage -> LogUtils.getLogger().error("Nuclear Explosion Saving Error: {}", errorMessage))
            .ifPresent(decodedObject -> modData.put(IRRADIATION_ID, decodedObject));

        modData.putBoolean(IS_DETONATING_ID, isDetonating);
        modData.putInt(AGE_ID, age);

        pCompound.put(RebbysNuclearExplosion.MODID, modData);

        super.addAdditionalSaveData(pCompound);

    }

    @Override
    public @NotNull Vec3 getDeltaMovement() {
        return Vec3.ZERO;
    }

    public @NotNull Iterable<ItemStack> getHandSlots() {
        return this.handItems;
    }

    public @NotNull Iterable<ItemStack> getArmorSlots() {
        return this.armorItems;
    }

    public @NotNull ItemStack getItemBySlot(@NotNull EquipmentSlot pSlot) {
        return ItemStack.EMPTY;
    }

    public void setItemSlot(EquipmentSlot pSlot, @NotNull ItemStack pStack) {

    }

    public @NotNull HumanoidArm getMainArm() {
        return HumanoidArm.RIGHT;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {

        controllerRegistrar.add(new AnimationController<GeoAnimatable>(this, "Detonating", state -> {
            if (age < 300) {
                // Add animation later
                return state.setAndContinue(DETONATION_ANIM);
            }
            return state.setAndContinue(AMBIENT_ANIM);
        }));

    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return geoCache;
    }
}
