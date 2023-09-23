package com.happyzleaf.prefixinator.commands;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.InheritanceNode;
import net.luckperms.api.node.types.MetaNode;
import net.luckperms.api.node.types.PrefixNode;
import net.luckperms.api.query.QueryOptions;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.happyzleaf.prefixinator.Prefixinator.META_GROUP_KEY;
import static com.happyzleaf.prefixinator.utils.FormatUtil.format;

public class PrefixCommand implements TabCompleter, CommandExecutor {
    private final LoadingCache<UUID, List<String>> usersGroupsCache = CacheBuilder.newBuilder()
            .expireAfterWrite(2, TimeUnit.MINUTES)
            .build(new CacheLoader<UUID, List<String>>() {
                @Override
                public List<String> load(UUID playerId) {
                    LuckPerms luck;
                    try {
                        luck = LuckPermsProvider.get();
                    } catch (IllegalStateException e) {
                        return Collections.emptyList();
                    }

                    User user = luck.getUserManager().getUser(playerId);
                    if (user == null) {
                        return Collections.emptyList();
                    }

                    return user.resolveInheritedNodes(NodeType.INHERITANCE, QueryOptions.nonContextual()).stream()
                            .map(InheritanceNode::getGroupName)
                            .collect(Collectors.toList());
                }
            });

    public PrefixCommand(PluginCommand command) {
        command.setTabCompleter(this);
        command.setExecutor(this);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1 && sender instanceof Player) {
            return usersGroupsCache.getUnchecked(((Player) sender).getUniqueId());
        }

        return Collections.emptyList();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        LuckPerms luck = LuckPermsProvider.get(); // Should fail before everything else

        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "You must be a player to use this command.");
            return false;
        }

        Player player = (Player) sender;
        User user = luck.getUserManager().getUser(player.getUniqueId());
        if (user == null) {
            sender.sendMessage(ChatColor.RED + "Could not find user '" + player.getUniqueId() + "'.");
            return false;
        }

        switch (args.length) {
            case 0: {
                ComponentBuilder text = new ComponentBuilder(ChatColor.AQUA + "Available prefixes:\n");
                user.resolveInheritedNodes(NodeType.INHERITANCE, QueryOptions.nonContextual()).stream()
                        .map(n -> luck.getGroupManager().getGroup(n.getGroupName()))
                        .forEach(group -> {
                            if (group == null) return;

                            String prefix = group.getCachedData().getMetaData().getPrefix();
                            if (prefix == null) return;

                            text.append(
                                    new ComponentBuilder(ChatColor.DARK_AQUA + "- " + ChatColor.RESET)
                                            .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(ChatColor.LIGHT_PURPLE + "Click to apply")))
                                            .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/prefixinator:prefix " + group.getName()))
                                            .create()
                            );
                            text.append(format(prefix));
                            text.append("\n", ComponentBuilder.FormatRetention.NONE);
                        });
                sender.spigot().sendMessage(text.create());

                return true;
            }
            case 1: {
                Group group = luck.getGroupManager().getGroup(args[0]);
                if (group == null) {
                    sender.sendMessage(ChatColor.RED + "Could not find group '" + args[0] + "'.");
                    return false;
                }

                if (!user.getCachedData().getPermissionData().checkPermission("group." + group.getName()).asBoolean()) {
                    sender.sendMessage(ChatColor.RED + "You are not in the group '" + args[0] + "'.");
                    return false;
                }

                String prefix = group.getCachedData().getMetaData().getPrefix();
                if (prefix == null) {
                    sender.sendMessage(ChatColor.RED + "Could not find any prefix for group '" + args[0] + "'.");
                    return false;
                }

                user.data().clear(n -> n instanceof PrefixNode || (n instanceof MetaNode && META_GROUP_KEY.equals(((MetaNode) n).getMetaKey())));
                user.data().add(MetaNode.builder(META_GROUP_KEY, group.getName()).build());
                user.data().add(PrefixNode.builder(prefix, 9999).build());
                luck.getUserManager().saveUser(user);

                sender.sendMessage(ChatColor.GREEN + "Prefix set to " + format(prefix) + ChatColor.RESET + ChatColor.GREEN + ".");

                return true;
            }
            default:
                sender.sendMessage(ChatColor.RED + "Usage /prefix");
                return false;
        }
    }
}
