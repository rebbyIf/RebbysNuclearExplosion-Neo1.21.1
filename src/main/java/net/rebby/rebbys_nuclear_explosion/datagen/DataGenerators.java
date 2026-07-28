package net.rebby.rebbys_nuclear_explosion.datagen;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.rebby.rebbys_nuclear_explosion.RebbysNuclearExplosion;
import net.rebby.rebbys_nuclear_explosion.client.particle.ModParticleDescriptionProvider;

@EventBusSubscriber(modid = RebbysNuclearExplosion.MODID)
public class DataGenerators {

    @SubscribeEvent // on the mod event bus
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

        // other providers here
        generator.addProvider(
                event.includeClient(),
                new ModParticleDescriptionProvider(output, existingFileHelper)
        );
    }
}
