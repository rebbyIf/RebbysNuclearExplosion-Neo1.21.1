package net.rebby.rebbys_nuclear_explosion.client.rendertype;

import foundry.veil.api.client.registry.VeilShaderBufferRegistry;
import foundry.veil.api.client.render.VeilShaderBufferLayout;
import foundry.veil.platform.registry.RegistrationProvider;
import net.minecraft.core.Registry;
import net.rebby.rebbys_nuclear_explosion.RebbysNuclearExplosion;

import java.util.function.Supplier;

public final class ModShaderBufferRegistry {

    private static final RegistrationProvider<VeilShaderBufferLayout<?>> PROVIDER =
            RegistrationProvider.get(VeilShaderBufferRegistry.REGISTRY_KEY, RebbysNuclearExplosion.MODID);

    public static final Registry<VeilShaderBufferLayout<?>> REGISTRY = PROVIDER.asVanillaRegistry();

    public static final Supplier<VeilShaderBufferLayout<RadiationUniforms>> RADIATION_UNIFORMS =
            register("radiation", RadiationUniforms::createLayout);

    private ModShaderBufferRegistry() {}

    private static <T> Supplier<VeilShaderBufferLayout<T>> register(String name, Supplier<VeilShaderBufferLayout<T>> layout) {
        return PROVIDER.register(name, layout);
    }
}
