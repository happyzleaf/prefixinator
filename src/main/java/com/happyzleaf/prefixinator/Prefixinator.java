package com.happyzleaf.prefixinator;

import com.happyzleaf.prefixinator.commands.PrefixCommand;
import com.happyzleaf.prefixinator.commands.RefreshPrefixesCommand;
import com.happyzleaf.prefixinator.listeners.LoginListener;
import org.bukkit.plugin.java.JavaPlugin;

public final class Prefixinator extends JavaPlugin {
    public static final String META_GROUP_KEY = "prefixinator-group";

    @Override
    public void onEnable() {
        new PrefixCommand(getCommand("prefix"));
        new RefreshPrefixesCommand(getCommand("refreshprefixes"));
        getServer().getPluginManager().registerEvents(new LoginListener(), this);
        getLogger().info("Loaded! This plugin was made by happyz. https://happyzleaf.com/");
    }

    @Override
    public void onDisable() {}
}
