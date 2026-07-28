package net.rebby.rebbys_nuclear_explosion;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockState;
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
            .defineInRange("r", 256, 64, 512);
    private static final ModConfigSpec.IntValue EXPLOSION_STEP = BUILDER
            .comment("Steps between each mini explosion")
            .defineInRange("explosionStep", 16, 1, 64);
    private static final ModConfigSpec.IntValue MAX_EXPLOSION_POWER = BUILDER
            .comment("Max Power of each mini explosion")
            .defineInRange("maxPower", 20, 1, 64);

    private static final ModConfigSpec.ConfigValue<String> FIRE_BLOCK = BUILDER
            .comment("Fire block used in explosion")
            .define("fire_block", "minecraft:fire");
    private static final ModConfigSpec.ConfigValue<String> MELTED_GLASS_BLOCK = BUILDER
            .comment("Melted glass block used in explosion")
            .define("melted_glass_block", "minecraft:black_stained_glass");
    private static final ModConfigSpec.ConfigValue<String> DIAMONDIZED_BLOCK = BUILDER
            .comment("Block used to simulate diamondization in explosion")
            .define("diamondized_block", "minecraft:deepslate_diamond_ore");
    private static final ModConfigSpec.ConfigValue<String> IRRADIATED_BLOCK = BUILDER
            .comment("Block used to simulate irradiation in explosion")
            .define("irradiated_block", "minecraft:mossy_cobblestone");
    private static final ModConfigSpec.ConfigValue<String> IRRADIATED_LIQUID = BUILDER
            .comment("Liquid used to simulate irradiation in explosion")
            .define("irradiated_liquid", "minecraft:lava");
    private static final ModConfigSpec.ConfigValue<String> SCORCHED_GRASS_BLOCK = BUILDER
            .comment("Scorched grass block used in explosion")
            .define("scorched_grass", "minecraft:coal_block");
    private static final ModConfigSpec.ConfigValue<String> SCORCHED_MOSS_BLOCK = BUILDER
            .comment("Scorched moss block used in explosion")
            .define("scorched_moss", "minecraft:coal_block");

    static final ModConfigSpec SPEC = BUILDER.build();

    public static boolean isTestingEnvironment;
    public static int maxExplosionThreads;
    public static int explosionStep;
    public static int maxExplosionPower;
    public static int r;
    public static int r0;
    public static int r1;
    public static int r2;
    public static int r3;
    public static int nukeModelSize;

    public static BlockState fireBlockState;
    public static BlockState meltedGlassBlockState;
    public static BlockState diamondizedBlockState;
    public static BlockState irradiatedBlockState;
    public static BlockState irradiatedLiquidState;
    public static BlockState scorchedGrassBlockState;
    public static BlockState scorchedMossBlockState;

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
        explosionStep = EXPLOSION_STEP.get();
        maxExplosionPower = MAX_EXPLOSION_POWER.get();

        r = EXPLOSION_RADIUS.get();
        r0 = r * 3 /4;
        r1 = r / 2;
        r2 = r / 5;
        r3 = r2 * 9 /10;
        nukeModelSize = r / 10;

        fireBlockState = Irradiation.getDefaultBlockState(FIRE_BLOCK.get());
        meltedGlassBlockState = Irradiation.getDefaultBlockState(MELTED_GLASS_BLOCK.get());
        diamondizedBlockState = Irradiation.getDefaultBlockState(DIAMONDIZED_BLOCK.get());
        irradiatedBlockState = Irradiation.getDefaultBlockState(IRRADIATED_BLOCK.get());
        irradiatedLiquidState = Irradiation.getDefaultBlockState(IRRADIATED_LIQUID.get());
        scorchedGrassBlockState = Irradiation.getDefaultBlockState(SCORCHED_GRASS_BLOCK.get());
        scorchedMossBlockState = Irradiation.getDefaultBlockState(SCORCHED_MOSS_BLOCK.get());
    }
}
