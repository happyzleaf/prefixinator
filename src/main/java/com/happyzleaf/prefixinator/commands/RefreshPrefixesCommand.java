package com.happyzleaf.prefixinator.commands;

import com.happyzleaf.prefixinator.config.Config;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.types.MetaNode;
import net.luckperms.api.node.types.PrefixNode;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

import static com.happyzleaf.prefixinator.Prefixinator.META_GROUP_KEY;

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
        MetaNode meta = null;

        boolean modified = false;
        for (Node node : user.data().toCollection()) {
            if (node instanceof PrefixNode) {
                if (user.data().remove(node).wasSuccessful()) {
                    modified = true;
                }
            } else if (node instanceof MetaNode && META_GROUP_KEY.equals(((MetaNode) node).getMetaKey())) {
                meta = (MetaNode) node;
                if (user.data().remove(node).wasSuccessful()) {
                    modified = true;
                }
            }
        }
        if (meta == null) return modified;

        Group group = luck.getGroupManager().getGroup(meta.getMetaValue());
        if (group == null) return true;

        if (!user.getCachedData().getPermissionData().checkPermission("group." + group.getName()).asBoolean())
            return true;

        String prefix = group.getCachedData().getMetaData().getPrefix();
        if (prefix == null) return true;

        user.data().add(MetaNode.builder(META_GROUP_KEY, group.getName()).build());
        user.data().add(PrefixNode.builder(prefix, 9999).build());

        return true;
    }
}
