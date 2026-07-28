package net.rebby.rebbys_nuclear_explosion.client;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.rebby.rebbys_nuclear_explosion.RebbysNuclearExplosion;

public class Sounds {

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, RebbysNuclearExplosion.MODID);

    public static final Holder<SoundEvent> NUCLEAR_EXPLOSION_AMBIENCE = SOUND_EVENTS.register(
            "nuclear_explosion.ambience", SoundEvent::createVariableRangeEvent
    );

    public static void register(IEventBus bus) {
        SOUND_EVENTS.register(bus);
    }
}
