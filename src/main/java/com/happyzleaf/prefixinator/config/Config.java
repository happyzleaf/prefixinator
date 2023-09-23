package com.happyzleaf.prefixinator.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;
import net.md_5.bungee.api.chat.BaseComponent;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import static com.happyzleaf.prefixinator.utils.FormatUtil.format;

public class Config {
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .registerTypeAdapter(Message.class, (JsonSerializer<Message>) (src, t, ctx) -> ctx.serialize(src.t))
            .registerTypeAdapter(Message.class, (JsonDeserializer<Message>) (json, t, ctx) -> Message.of(ctx.deserialize(json, String.class)))
            .create();

    private final Message commandHeader = Message.of("&bAvailable prefixes:\n");
    private final Message commandBody = Message.of("&3- &r{prefix}&r\n");
    private final Message commandFooter = Message.of("");

    private final Message commandSuccess = Message.of("&aPrefix set to {prefix}&r&a.");

    public static Config from(Path path) throws IOException {
        System.out.println("path = " + path.toString());
        if (Files.exists(path)) {
            return GSON.fromJson(Files.newBufferedReader(path), Config.class);
        } else {
            Config config = new Config();
            String json = GSON.toJson(config);

            Files.createDirectories(path.getParent());
            Files.write(path, json.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE_NEW);

            return config;
        }
    }

    public Message getCommandHeader() {
        return commandHeader;
    }

    public Message getCommandBody() {
        return commandBody;
    }

    public Message getCommandFooter() {
        return commandFooter;
    }

    public Message getCommandSuccess() {
        return commandSuccess;
    }

    public static class Message {
        public final String t;
        public final BaseComponent[] c;

        private Message(String t, BaseComponent[] c) {
            this.t = t;
            this.c = c;
        }

        public static Message of(String text) {
            return new Message(text, format(text));
        }
    }
}
