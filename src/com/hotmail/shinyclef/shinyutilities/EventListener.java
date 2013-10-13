package com.hotmail.shinyclef.shinyutilities;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Author: Shinyclef
 * Date: 24/06/12
 * Time: 9:43 PM
 */

public class EventListener implements Listener
{
    private static ShinyUtilities p;

    public EventListener(ShinyUtilities plugin)
    {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        p = plugin;
    }

    @EventHandler
    public void eventCommandPreprocess(PlayerCommandPreprocessEvent event)
    {
        String message = event.getMessage().trim();
        String command;

        if (message.indexOf(' ') == -1)
        {
            command = message;
        }
        else
        {
            command = message.substring(0, message.indexOf(' ')).toLowerCase();
        }
        Mute.commandPreProcess(event, message, command);
        Busy.commandPreProcess(event, message, command);
    }

    @EventHandler
    public void eventCommandPreprocess (AsyncPlayerChatEvent event)
    {
        Mute.chatPreProcess(event);
    }

    @EventHandler
    public void eventPlayerJoin(PlayerJoinEvent e)
    {
        final String playerName = e.getPlayer().getName();

        //delay 5 seconds, then remind all busy players that they are still busy
        Bukkit.getScheduler().scheduleSyncDelayedTask(p, new BukkitRunnable()
        {
            @Override
            public void run()
            {
                Busy.remindBusyPlayer(playerName);
            }
        }, 100);
    }
}
