package com.happyzleaf.prefixinator.config;

import com.google.gson.*;
import net.md_5.bungee.api.chat.BaseComponent;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import static com.happyzleaf.prefixinator.utils.FormatUtil.format;

public class Config {
    public transient final Path path;
    private transient final Gson gson;

    private final Message commandHeader = Message.of("&bAvailable prefixes:\n");
    private final Message commandBody = Message.of("&3- &r{prefix}&r\n");
    private final Message commandFooter = Message.of("");

    private final Message commandSuccess = Message.of("&aPrefix set to {prefix}&r&a.");

    // Internal, just for defaults
    private Config() {
        this.path = null;
        this.gson = null;
    }

    public Config(Path path) {
        this.path = path;
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .registerTypeAdapter(Message.class, Message.SERIALIZER)
                .registerTypeAdapter(Message.class, Message.DESERIALIZER)
                .registerTypeAdapter(Config.class, (InstanceCreator<Config>) type -> Config.this)
                .create();
    }

    public void load() throws Exception {
        if (!Files.exists(this.path)) {
            Files.createDirectories(this.path.getParent());
            String json = this.gson.toJson(new Config()); // Creating a new instance to reset to defaults
            Files.write(this.path, json.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE_NEW);
        }

        // Also doing this when the file was just created to regain defaults if reset
        this.gson.fromJson(Files.newBufferedReader(path), Config.class);
    }

    public Message getCommandHeader() {
        return this.commandHeader;
    }

    public Message getCommandBody() {
        return this.commandBody;
    }

    public Message getCommandFooter() {
        return this.commandFooter;
    }

    public Message getCommandSuccess() {
        return this.commandSuccess;
    }

    public static class Message {
        public static final JsonSerializer<Message> SERIALIZER = (src, t, ctx) -> ctx.serialize(src.t);
        public static final JsonDeserializer<Message> DESERIALIZER = (json, t, ctx) -> Message.of(ctx.deserialize(json, String.class));

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
