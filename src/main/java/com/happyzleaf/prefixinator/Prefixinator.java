package com.happyzleaf.prefixinator;

import com.happyzleaf.prefixinator.commands.PrefixCommand;
import com.happyzleaf.prefixinator.commands.RefreshPrefixesCommand;
import com.happyzleaf.prefixinator.config.Config;
import com.happyzleaf.prefixinator.listeners.LoginListener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public final class Prefixinator extends JavaPlugin {
    public static final String META_GROUP_KEY = "prefixinator-group";
    public static final int PREFIX_WEIGHT = 9999;

    public final Config config;

    public Prefixinator() {
        this.config = new Config(getDataFolder().toPath().resolve("prefixinator.json"));
    }

    @Override
    public void onEnable() {
        try {
            this.config.load();
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Could not load the config from path '" + config.path + "'.", e);
            return;
        }

        new PrefixCommand(this.config, getCommand("prefix"));
        new RefreshPrefixesCommand(this.config, getCommand("refreshprefixes"));

        getServer().getPluginManager().registerEvents(new LoginListener(), this);

        getLogger().info("Loaded! This plugin was made by happyz. https://happyzleaf.com/");
    }

    @Override
    public void onDisable() {
    }
}
