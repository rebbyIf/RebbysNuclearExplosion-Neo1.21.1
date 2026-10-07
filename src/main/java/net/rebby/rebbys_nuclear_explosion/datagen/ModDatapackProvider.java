package net.rebby.rebbys_nuclear_explosion.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.rebby.rebbys_nuclear_explosion.RebbysNuclearExplosion;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class ModDatapackProvider extends DatapackBuiltinEntriesProvider {

    public static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(Registries.DAMAGE_TYPE, ModDamageTypes::bootstrap);
    public ModDatapackProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, BUILDER, Set.of(RebbysNuclearExplosion.MODID));
    }
}
