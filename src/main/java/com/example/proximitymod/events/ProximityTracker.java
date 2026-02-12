package com.example.proximitymod.events;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ProximityTracker {

    private static final String WORKER_URL = "https://fractalvoiceworker.fractalvoice.workers.dev/";

    // max 1 request per 300ms
    private static final long SEND_DELAY = 300;
    private static long lastSend = 0;

    private static final Queue<String> queue = new ConcurrentLinkedQueue<>();

    public static void register() {

        // start sender thread
        startSender();

        ClientEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (!(entity instanceof PlayerEntity player)) return;

            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null) return;
            if (player.getUuid().equals(client.player.getUuid())) return;

            queueEvent("visibility_add",
                    client.player.getUuidAsString(),
                    player.getUuidAsString(),
                    client.player.getGameProfile().getName(),
                    player.getGameProfile().getName());
        });

        ClientEntityEvents.ENTITY_UNLOAD.register((entity, world) -> {
            if (!(entity instanceof PlayerEntity player)) return;

            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null) return;
            if (player.getUuid().equals(client.player.getUuid())) return;

            queueEvent("visibility_remove",
                    client.player.getUuidAsString(),
                    player.getUuidAsString(),
                    client.player.getGameProfile().getName(),
                    player.getGameProfile().getName());
        });
    }

    private static void queueEvent(String event, String a, String b, String nameA, String nameB) {
        String json = String.format(
                "{\"type\":\"visibility\",\"event\":\"%s\",\"playerA\":\"%s\",\"playerB\":\"%s\",\"nameA\":\"%s\",\"nameB\":\"%s\"}",
                event, a, b, nameA, nameB
        );
        queue.add(json);
    }

    private static void startSender() {
        new Thread(() -> {
            while (true) {
                try {
                    long now = System.currentTimeMillis();

                    if (!queue.isEmpty() && now - lastSend >= SEND_DELAY) {
                        String payload = queue.poll();
                        sendToWorker(payload);
                        lastSend = now;
                    }

                    Thread.sleep(50);
                } catch (Exception ignored) {}
            }
        }, "FractalVoice-Proximity-Sender").start();
    }

    private static void sendToWorker(String payload) {
        try {
            URL url = new URL(WORKER_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(payload.getBytes(StandardCharsets.UTF_8));
            }

            conn.getInputStream();
            conn.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
