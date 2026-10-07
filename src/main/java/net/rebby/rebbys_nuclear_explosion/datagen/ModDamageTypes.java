package net.rebby.rebbys_nuclear_explosion.datagen;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageEffects;
import net.minecraft.world.damagesource.DamageScaling;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.level.Level;
import net.rebby.rebbys_nuclear_explosion.RebbysNuclearExplosion;

public class ModDamageTypes {

    public static final ResourceKey<DamageType> RADIATION =
            ResourceKey.create(Registries.DAMAGE_TYPE, RebbysNuclearExplosion.getResource("radiation"));

    public static void bootstrap(BootstrapContext<DamageType> context) {
        context.register(RADIATION, new DamageType("radiation", DamageScaling.NEVER, 20.0f, DamageEffects.BURNING));
    }

    public static DamageSource create(Level level, ResourceKey<DamageType> key) {
        return new DamageSource(level.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(key));
    }
}
