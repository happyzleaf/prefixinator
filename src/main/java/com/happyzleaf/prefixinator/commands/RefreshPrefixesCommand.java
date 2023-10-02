package com.happyzleaf.prefixinator.commands;

import com.happyzleaf.prefixinator.config.Config;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.types.PrefixNode;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

import static com.happyzleaf.prefixinator.Prefixinator.META_GROUP_KEY;
import static com.happyzleaf.prefixinator.Prefixinator.PREFIX_WEIGHT;
import static com.happyzleaf.prefixinator.commands.PrefixCommand.CLEAR_ALL;

public class RefreshPrefixesCommand implements TabCompleter, CommandExecutor {
    private final Config config;

    public RefreshPrefixesCommand(Config config, PluginCommand command) {
        this.config = config;

        command.setTabCompleter(this);
        command.setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        LuckPerms luck = LuckPermsProvider.get(); // Should fail before everything else

        if (args.length != 0) {
            sender.sendMessage(ChatColor.RED + "Usage /refreshprefixes");
            return false;
        }

        try {
            this.config.load();
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "Could not reload the config from path '" + config.path + "'. Check logs.");
            e.printStackTrace();
            return false;
        }

        int count = 0;
        for (Player player : sender.getServer().getOnlinePlayers()) {
            User user = luck.getUserManager().getUser(player.getUniqueId());
            if (user != null && refresh(luck, user)) {
                luck.getUserManager().saveUser(user);
                ++count;
            }
        }
        sender.sendMessage(ChatColor.GREEN + "Refreshed " + count + " users.");

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return Collections.emptyList();
    }

    public static boolean refresh(LuckPerms luck, User user) {
        CachedMetaData meta = user.getCachedData().getMetaData();
        String prefix = meta.getPrefixes().get(PREFIX_WEIGHT);
        if (prefix == null) return false;

        String groupName = meta.getMetaValue(META_GROUP_KEY);
        if (groupName == null) return false;

        Group group = luck.getGroupManager().getGroup(groupName);
        if (group == null || !user.getCachedData().getPermissionData().checkPermission("group." + group.getName()).asBoolean()) {
            user.data().clear(CLEAR_ALL);
            return true;
        }

        String groupPrefix = group.getCachedData().getMetaData().getPrefix();
        if (groupPrefix == null) {
            user.data().clear(CLEAR_ALL);
            return true;
        }

        if (prefix.equals(groupPrefix)) return false;

        user.data().add(PrefixNode.builder(groupPrefix, PREFIX_WEIGHT).build());
        return true;
    }
}
