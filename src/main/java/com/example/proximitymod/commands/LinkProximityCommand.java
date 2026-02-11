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

    private static final String WEBHOOK_URL =
            "https://discord.com/api/webhooks/1470846057595539509/sazm6NyVbgWznBmI8EsL6Vu2Bp2COBJVCAduzN7tWxqdFnYB8Ddm432DqVrCm4wNXbPy";

    // Cooldown map: Player UUID -> Last request timestamp (ms)
    private static final HashMap<UUID, Long> COOLDOWN_MAP = new HashMap<>();
    private static final long COOLDOWN_MS = 30_000; // 30 seconds cooldown

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(literal("linkproximity")
                .executes(context -> {
                    ClientPlayerEntity player = MinecraftClient.getInstance().player;
                    if (player == null) return 0;

                    UUID uuid = player.getUuid();
                    long now = System.currentTimeMillis();

                    // Check cooldown
                    if (COOLDOWN_MAP.containsKey(uuid)) {
                        long last = COOLDOWN_MAP.get(uuid);
                        long remaining = (COOLDOWN_MS - (now - last)) / 1000;
                        if (remaining > 0) {
                            player.sendMessage(Text.literal("§cYou must wait §e" + remaining + "§c seconds before requesting a new code."), false);
                            return 0;
                        }
                    }

                    // Generate and send code
                    String code = generateCode();
                    player.sendMessage(Text.literal("§aYour link code: §e" + code), false);

                    sendWebhook(code, uuid.toString(), player.getName().getString());

                    // Update cooldown
                    COOLDOWN_MAP.put(uuid, now);
                    return 1;
                })
            );
        });
    }

    private static String generateCode() {
        return Integer.toString(10000 + (int) (Math.random() * 90000));
    }

    private static void sendWebhook(String code, String uuid, String username) {
        new Thread(() -> {
            try {
                URL url = new URL(WEBHOOK_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json");

                // ✅ VALID JSON
                String json = String.format(
                        "{ \"username\": \"FractalVoice\", " +
                        "\"content\": \"**New Proximity Link Created**\\nPlayer: %s\\nUUID: %s\\nCode: %s\" }",
                        username, uuid, code
                );

                byte[] data = json.getBytes(StandardCharsets.UTF_8);
                conn.setFixedLengthStreamingMode(data.length);
                conn.connect();

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(data);
                    os.flush();
                }

                int response = conn.getResponseCode();

                if (response != 204 && response != 200) {
                    System.err.println("Webhook failed, response code: " + response);
                    InputStream err = conn.getErrorStream();
                    if (err != null) {
                        System.err.println(new String(err.readAllBytes(), StandardCharsets.UTF_8));
                    }
                } else {
                    System.out.println("Webhook sent successfully.");
                }

                conn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, "FractalVoice-Webhook").start();
    }
}
