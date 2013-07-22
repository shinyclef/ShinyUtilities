package com.hotmail.shinyclef.shinyutilities;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * User: Peter
 * Date: 23/07/13
 * Time: 1:00 AM
 */

public class Busy
{
    private static ShinyUtilities plugin;
    private static Configuration config;

    private static Map<String, Object> busyMap;

    public static void initialize(ShinyUtilities thePlugin)
    {
        plugin = thePlugin;
        config = plugin.getConfig();
        //get ticketMap from config

        try
        {
            busyMap = config.getConfigurationSection("Busy").getValues(false);
        }
        catch (NullPointerException ex)
        {
            busyMap = new HashMap<String, Object>();
        }
    }

    public static boolean busy(CommandSender sender, String[] args)
    {
        //if sender is already in map
        if (busyMap.containsKey(sender.getName().toLowerCase()))
        {
            setBack(sender);
        }
        else
        {
            setBusy(sender, args);
        }

        return true;
    }

    private static void setBack(CommandSender sender)
    {
        //remove from map and save config
        busyMap.remove(sender.getName().toLowerCase());
        config.set("Busy", busyMap);
        plugin.saveConfig();

        //notify sender
        sender.sendMessage(ChatColor.YELLOW + "You are now accepting private messages.");
    }

    private static void setBusy(CommandSender sender, String[] args)
    {
        //get busy message
        String busyMsg = "";
        for (String item : args)
        {
            busyMsg = busyMsg + item + " ";
        }
        if (busyMsg.length() > 0)
        {
            busyMsg = busyMsg.substring(0, busyMsg.length() - 1); //remove trailing space
        }

        //add to map and save config
        busyMap.put(sender.getName().toLowerCase(), busyMsg);
        config.set("Busy", busyMap);
        plugin.saveConfig();

        //notify sender
        sender.sendMessage(ChatColor.YELLOW + "You are no longer accepting private messages.");
    }

    public static void commandPreProcess(PlayerCommandPreprocessEvent event, String message, String command)
    {
        if (!event.getPlayer().hasPermission("rolyd.mod") && (command.equals("/msg") || command.equals("/tell")))
        {
            //get recipient
            int firstSpace = message.indexOf(' ');
            int secondSpace = message.indexOf(' ', firstSpace + 1);
            if (firstSpace == -1 || secondSpace == -1)
            {
                return; //do nothing, the command will give its own error.
            }
            String recipient = message.substring(firstSpace + 1, secondSpace).toLowerCase();

            //check list
            if (busyMap.containsKey(recipient))
            {
                event.setCancelled(true); //cancel their message

                //check for busy message and send feedback to user
                String busyMsg = (String)busyMap.get(recipient);
                if (busyMsg.equals(""))
                {
                    event.getPlayer().sendMessage(ChatColor.YELLOW + "Sorry, " + recipient + " is currently busy.");
                }
                else
                {
                    event.getPlayer().sendMessage(ChatColor.YELLOW + "Sorry, " + recipient + " is current busy: " +
                            ChatColor.AQUA + busyMsg);
                }
            }
        }
    }
}
