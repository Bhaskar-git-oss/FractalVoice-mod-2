package com.example.proximitymod.commands;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.UUID;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class LinkProximityCommand {

    // Cloudflare Worker endpoint (NOT a Discord webhook)
    private static final String WORKER_URL = "https://fractalvoiceworker.fractalvoice.workers.dev/";

    private static final HashMap<UUID, Long> COOLDOWN_MAP = new HashMap<>();
    private static final long COOLDOWN_MS = 30_000;

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(literal("linkproximity").executes(context -> {
                ClientPlayerEntity player = MinecraftClient.getInstance().player;
                if (player == null) return 0;

                UUID uuid = player.getUuid();
                long now = System.currentTimeMillis();

                // client side cooldown
                if (COOLDOWN_MAP.containsKey(uuid)) {
                    long last = COOLDOWN_MAP.get(uuid);
                    long remaining = (COOLDOWN_MS - (now - last)) / 1000;
                    if (remaining > 0) {
                        player.sendMessage(Text.literal("§cWait §e" + remaining + "§c seconds."), false);
                        return 0;
                    }
                }

                String code = generateCode();
                player.sendMessage(Text.literal("§aYour link code: §e" + code), false);

                sendToWorker(code, uuid.toString(), player.getName().getString());

                COOLDOWN_MAP.put(uuid, now);
                return 1;
            }));
        });
    }

    private static String generateCode() {
        return Integer.toString(10000 + (int) (Math.random() * 90000));
    }

    private static void sendToWorker(String code, String uuid, String username) {
        new Thread(() -> {
            try {
                URL url = new URL(WORKER_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setRequestMethod("POST");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                String json = String.format(
                        "{\"type\":\"link_request\",\"uuid\":\"%s\",\"name\":\"%s\",\"code\":\"%s\"}",
                        uuid, username, code
                );

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(json.getBytes(StandardCharsets.UTF_8));
                }

                int response = conn.getResponseCode();

                if (response != 200) {
                    System.err.println("Worker returned HTTP " + response);
                    InputStream err = conn.getErrorStream();
                    if (err != null) {
                        System.err.println(new String(err.readAllBytes(), StandardCharsets.UTF_8));
                    }
                } else {
                    System.out.println("Link request sent to Worker.");
                }

                conn.disconnect();

            } catch (Exception e) {
                System.err.println("Failed to contact Worker:");
                e.printStackTrace();
            }
        }, "FractalVoice-Link").start();
    }
}
