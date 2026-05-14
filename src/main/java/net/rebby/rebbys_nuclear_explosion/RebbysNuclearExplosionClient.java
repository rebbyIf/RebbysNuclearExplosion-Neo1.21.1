package net.rebby.rebbys_nuclear_explosion;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.rebby.rebbys_nuclear_explosion.entity.Entities;
import net.rebby.rebbys_nuclear_explosion.entity.client.renderer.NuclearExplosionRenderer;

import java.io.IOException;

// This class will not load on dedicated servers. Accessing client side code from here is safe.
@Mod(value = RebbysNuclearExplosion.MODID, dist = Dist.CLIENT)
// You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
@EventBusSubscriber(modid = RebbysNuclearExplosion.MODID, value = Dist.CLIENT)
public class RebbysNuclearExplosionClient {

    public static final RenderTarget RADIATION_RENDER_TARGET = new TextureTarget(
            Minecraft.getInstance().getWindow().getWidth(),
            Minecraft.getInstance().getWindow().getWidth(), true, Minecraft.ON_OSX);
    public static final RenderTarget EXPLOSION_RENDER_TARGET = new TextureTarget(
            Minecraft.getInstance().getWindow().getWidth(),
            Minecraft.getInstance().getWindow().getWidth(), true, Minecraft.ON_OSX);



    public RebbysNuclearExplosionClient(ModContainer container) {
        // Allows NeoForge to create a config screen for this mod's configs.
        // The config screen is accessed by going to the Mods screen > clicking on your mod > clicking on config.
        // Do not forget to add translations for your config options to the en_us.json file.
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        // Some client setup code
        RebbysNuclearExplosion.LOGGER.info("HELLO FROM CLIENT SETUP");
        RebbysNuclearExplosion.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());

    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(Entities.NUCLEAR_EXPLOSION.get(), NuclearExplosionRenderer::new);
    }
}
