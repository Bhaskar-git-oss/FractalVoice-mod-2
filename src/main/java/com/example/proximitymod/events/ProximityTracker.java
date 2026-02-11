package com.example.proximitymod.events;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;

import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class ProximityTracker {

    public static void register() {

        // ENTER
        ClientEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (!(entity instanceof PlayerEntity player)) return;

            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null) return;
            if (player.getUuid().equals(client.player.getUuid())) return;

            sendEvent(
                    "visibility_add",
                    client.player.getUuidAsString(),
                    player.getUuidAsString(),
                    client.player.getGameProfile().getName(),
                    player.getGameProfile().getName()
            );
        });

        // LEAVE
        ClientEntityEvents.ENTITY_UNLOAD.register((entity, world) -> {
            if (!(entity instanceof PlayerEntity player)) return;

            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null) return;
            if (player.getUuid().equals(client.player.getUuid())) return;

            sendEvent(
                    "visibility_remove",
                    client.player.getUuidAsString(),
                    player.getUuidAsString(),
                    client.player.getGameProfile().getName(),
                    player.getGameProfile().getName()
            );
        });
    }

    private static void sendEvent(String event, String playerA, String playerB, String nameA, String nameB) {
        new Thread(() -> {
            try {
                URL url = new URL("https://discord.com/api/webhooks/1470846057595539509/sazm6NyVbgWznBmI8EsL6Vu2Bp2COBJVCAduzN7tWxqdFnYB8Ddm432DqVrCm4wNXbPy");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                String payload = String.format(
                        "{ \"content\": \"{\\\"event\\\":\\\"%s\\\",\\\"playerA\\\":\\\"%s\\\",\\\"playerB\\\":\\\"%s\\\",\\\"nameA\\\":\\\"%s\\\",\\\"nameB\\\":\\\"%s\\\"}\" }",
                        event, playerA, playerB, nameA, nameB
                );

                conn.getOutputStream().write(payload.getBytes(StandardCharsets.UTF_8));
                conn.getInputStream();
                conn.disconnect();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }, "FractalVoice-Proximity").start();
    }
}
