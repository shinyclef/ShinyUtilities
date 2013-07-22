package com.hotmail.shinyclef.shinyutilities;

import com.hotmail.shinyclef.shinyutilities.ShinyUtilities;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.HashSet;
import java.util.Set;

/**
 * Author: Shinyclef
 * Date: 24/06/12
 * Time: 9:43 PM
 */

public class EventListener implements Listener
{
    private ShinyUtilities plugin;
    private HashSet<String> muted;

    public EventListener(ShinyUtilities plugin)
    {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.plugin = plugin;

        if (muted == null)
            muted = new HashSet<String>();
    }

    public Set<String> getMuted()
    {
        return muted;
    }

    @EventHandler
    public void eventCommandPreprocess(PlayerCommandPreprocessEvent event)
    {
        if (muted.contains(event.getPlayer().getName().toLowerCase()))
        {
            final String message = event.getMessage().trim();

            if (message.startsWith("/me") || message.startsWith("/vip") || message.startsWith("/mail") || message.startsWith("/afk"))
            {
                event.setCancelled(true);
                event.getPlayer().sendMessage(ChatColor.DARK_RED + "You are muted.");
                return;
            }

            if (message.startsWith("/msg") || message.startsWith("/tell"))
            {
                int firstSpaceIndex = message.indexOf(' ');
                int secondSpaceIndex = message.indexOf(' ', firstSpaceIndex + 1);
                String playerName = message.substring(firstSpaceIndex + 1, secondSpaceIndex);
                Player recipient = plugin.getServer().getPlayer(playerName);

                if (recipient == null)
                    return;

                if (!recipient.hasPermission("rolyd.mod"))
                {
                    event.setCancelled(true);
                    event.getPlayer().sendMessage(ChatColor.DARK_RED + "You are muted and can only message moderators.");
                    return;
                }
            }
        }
    }

    @EventHandler
    public void eventCommandPreprocess (AsyncPlayerChatEvent event)
    {
        if (muted.contains(event.getPlayer().getName().toLowerCase()))
        {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.DARK_RED + "You are muted.");
        }
    }
}
