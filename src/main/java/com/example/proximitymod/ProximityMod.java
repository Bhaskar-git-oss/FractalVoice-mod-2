package com.example.proximitymod;

import com.example.proximitymod.commands.LinkProximityCommand;
import com.example.proximitymod.events.ProximityTracker;
import net.fabricmc.api.ClientModInitializer;

public class ProximityMod implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        LinkProximityCommand.register();
        ProximityTracker.register();
    }
}
