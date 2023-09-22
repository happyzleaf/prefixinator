package com.happyzleaf.prefixinator.listeners;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import static com.happyzleaf.prefixinator.commands.RefreshPrefixesCommand.refresh;

public class LoginListener implements Listener {
    @EventHandler(ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        LuckPerms luck = LuckPermsProvider.get();
        User user = luck.getUserManager().getUser(event.getPlayer().getUniqueId());
        if (user != null && refresh(luck, user)) {
            luck.getUserManager().saveUser(user);
        }
    }
}
