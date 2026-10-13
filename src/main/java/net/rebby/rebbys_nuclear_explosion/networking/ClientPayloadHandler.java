package net.rebby.rebbys_nuclear_explosion.networking;

import com.google.common.collect.ImmutableCollection;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

import java.util.*;

public class ClientPayloadHandler {

    private static Map<Integer, NukeData> nukeDataMap = new HashMap<>();

    public static void handleNukeDataOnMain(final NukeData data, final IPayloadContext context) {
        if (data.remove()) {
            nukeDataMap.put(data.id(), null);
        }
        else {
            nukeDataMap.put(data.id(), data);
        }
    }

    public static Collection<NukeData> getNukeMapValues(){
        return nukeDataMap.values();
    }

    public static void refreshNukeMap(){
        nukeDataMap = new HashMap<>();
    }
}
