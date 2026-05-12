package net.rebby.rebbys_nuclear_explosion;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.rebby.rebbys_nuclear_explosion.util.Irradiation;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Neo's config APIs
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.BooleanValue IS_TESTING_ENVIRONMENT = BUILDER
            .comment("For testing purposes...")
            .define("testingEnvironment", false);
    private static final ModConfigSpec.IntValue MAX_EXPLOSION_THREADS = BUILDER
            .comment("Number of threads used for all nuclear explosions, defaults to available processors")
            .defineInRange("maxExplosionThreads", Runtime.getRuntime().availableProcessors(),
                    1, (int)(Runtime.getRuntime().maxMemory() / (1024L * 1024L * 8L)));
    private static final ModConfigSpec.IntValue EXPLOSION_RADIUS = BUILDER
            .comment("Explosion radius of Nuke")
            .defineInRange("r", 128, 16, 256);

    static final ModConfigSpec SPEC = BUILDER.build();

    public static boolean isTestingEnvironment;
    public static int maxExplosionThreads;
    public static int r;
    public static int r0;
    public static int r1;
    public static int r2;
    public static int r3;
    public static int nukeModelSize;

    private static boolean validateItemName(final Object obj)
    {
        return obj instanceof final String itemName && BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse(itemName));
    }

    public static void onLoad(final ModConfigEvent.Loading event)
    {
        load();
    }

    public static void onReload(final ModConfigEvent.Reloading event)
    {
        load();
    }

    private static void load() {
        isTestingEnvironment = IS_TESTING_ENVIRONMENT.get();
        maxExplosionThreads = MAX_EXPLOSION_THREADS.get();
        Irradiation.initThreads();

        r = EXPLOSION_RADIUS.get();
        r0 = r * 3 /4;
        r1 = r / 2;
        r2 = r / 4;
        r3 = r2 * 9 /10;
        nukeModelSize = r / 7;
    }
}
