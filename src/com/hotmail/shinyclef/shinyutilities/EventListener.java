package com.hotmail.shinyclef.shinyutilities;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

/**
 * Author: Shinyclef
 * Date: 24/06/12
 * Time: 9:43 PM
 */

public class EventListener implements Listener
{
    public EventListener(ShinyUtilities plugin)
    {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void eventCommandPreprocess(PlayerCommandPreprocessEvent event)
    {
        MuteHandler.commandPreProcess(event);
    }

    @EventHandler
    public void eventCommandPreprocess (AsyncPlayerChatEvent event)
    {
        MuteHandler.chatPreProcess(event);
    }
}
