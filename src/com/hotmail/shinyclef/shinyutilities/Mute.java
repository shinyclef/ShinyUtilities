package com.hotmail.shinyclef.shinyutilities;

import com.hotmail.shinyclef.shinybase.ShinyBaseAPI;
import com.hotmail.shinyclef.shinybridge.ShinyBridgeAPI;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * User: Shinyclef
 * Date: 22/07/13
 * Time: 10:42 PM
 */

public class Mute
{
    private static ShinyUtilities plugin;
    private static ShinyBaseAPI base;
    private static Configuration config;


    private static List<String> muted;
    private static List<String> muteinfo = null;
    private static Player[] onlineplayers = null;

    public static void initialize(ShinyUtilities plugin, ShinyBaseAPI base)
    {
        Mute.plugin = plugin;
        Mute.base = base;
        config = plugin.getConfig();

        muted = config.getStringList("Mute.MutedPlayers");
        if (muted == null)
        {
            muted = new ArrayList<String>();
        }

        muteinfo = config.getStringList("Mute.MuteInfo");
        if (muteinfo == null)
        {
            muteinfo = new ArrayList<String>();
        }
    }

    public static boolean mute(CommandSender sender, String[] args)
    {
        //perm check
        if (!sender.hasPermission("rolyd.mod"))
        {
            sender.sendMessage(ChatColor.RED + "You do not have permission to do that.");
            return true;
        }

        //1 arg only.
        if (args.length != 1)
        {
            return false;
        }

        //vars
        String lcName = args[0].toLowerCase();
        String displayName;

        //if player is already muted
        if (muted.contains(lcName))
        {
            sender.sendMessage(ChatColor.GOLD + "That player is already muted.");
            return true;
        }

        //if player has not played before
        if (!base.isExistingPlayer(lcName))
        {
            sender.sendMessage(ChatColor.RED + "That player has never played here before.");
            return true;
        }

        //date stuff
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        Date date = new Date();

        //get display name
        if(plugin.getServer().getOfflinePlayer(lcName).isOnline())
        {
            displayName = plugin.getServer().getPlayer(lcName).getName();
        }
        else
        {
            displayName = lcName;
        }

        //add info to mutelist
        String info = displayName + "|" + dateFormat.format(date) + "|" + sender.getName();
        muteinfo.add(info);

        //add player to 'muted'
        muted.add(lcName);

        //save config
        config.set("Mute.MutedPlayers", muted);
        config.set("Mute.MuteInfo", muteinfo);
        plugin.saveConfig();

        //notify admin team
        Bridge.broadcastPermissionMessage(ChatColor.DARK_RED + displayName +
                ChatColor.GOLD + " has been muted by " + ChatColor.DARK_GREEN + sender.getName(), "rolyd.mod");

        //if online, notify muted player
        if (Bridge.isOnlineAnywhere(lcName))
        {
            Bridge.sendMessage(lcName, ChatColor.DARK_RED + "You have been muted.");
        }

        return true;
    }

    public static boolean unmute(CommandSender sender, String[] args)
    {
        //perm check
        if (!sender.hasPermission("rolyd.mod"))
        {
            sender.sendMessage(ChatColor.RED + "You do not have permission to do that.");
            return true;
        }

        //1 arg only.
        if (args.length != 1)
        {
            return false;
        }

        //vars
        String lcName = args[0].toLowerCase();
        String displayName = "";

        //if player is not in Set 'muted'.
        if (!muted.contains(lcName))
        {
            sender.sendMessage(ChatColor.GOLD + "That player is not muted.");
            return true;
        }

        //remove player from muteinfo
        for (String string : muteinfo)
        {
            //divide the string into array 'item' on first char "|" (muted player | date, time, muter)
            String[] item = string.split("\\|", 2);

            //check name against arg and remove whole string on match
            if (item[0].equalsIgnoreCase(lcName))
            {
                muteinfo.remove(string);
                displayName = item[0];
                break;
            }
        }

        //remove player from 'muted'
        muted.remove(lcName);

        //save config
        config.set("Mute.MutedPlayers", muted);
        config.set("Mute.MuteInfo", muteinfo);
        plugin.saveConfig();

        //notify admin team
        Bridge.broadcastPermissionMessage(ChatColor.DARK_RED + displayName + ChatColor.GOLD + " has been unmuted by " +
                ChatColor.DARK_GREEN + sender.getName(), "rolyd.mod");

        //if online, notify unmuted player
        if (Bridge.isOnlineAnywhere(lcName))
        {
            Bridge.sendMessage(lcName, ChatColor.DARK_GREEN + "You have been unmuted.");
        }

        return true;
    }

    public static boolean mutelist(CommandSender sender, String[] args)
    {
        //perm check
        if (!sender.hasPermission("rolyd.mod"))
        {
            sender.sendMessage(ChatColor.RED + "You do not have permission to do that.");
            return true;
        }

        //if Set 'muted' is empty
        if (muted.isEmpty())
        {
            sender.sendMessage(ChatColor.GOLD + "No one is muted.");
            return true;
        }

        //send title "Currently Muted"
        sender.sendMessage(ChatColor.GOLD + "Currently Muted");

        //Display mutelist in a message to command sender.
        for (String string : muteinfo)
        {
            //divide the string into array 'item' on first char "|" (muted player | date, time | muter)
            String[] item = string.split("\\|");

            //send the line
            sender.sendMessage(ChatColor.DARK_RED + item[0] + ChatColor.GOLD + " - " + item[1] +
                    ChatColor.DARK_GREEN + " - " + item[2]);
        }

        return true;
    }

    public static void commandPreProcess(PlayerCommandPreprocessEvent event, String message, String command)
    {
        if (Mute.getMuted().contains(event.getPlayer().getName().toLowerCase()))
        {
            if (command.equals("/me") || command.equals("/vip") ||
                    command.equals("/mail") || command.equals("/afk"))
            {
                event.setCancelled(true);
                event.getPlayer().sendMessage(ChatColor.DARK_RED + "You are muted.");
                return;
            }

            if (command.equals("/msg") || command.equals("/tell"))
            {
                int firstSpaceIndex = message.indexOf(' ');
                int secondSpaceIndex = message.indexOf(' ', firstSpaceIndex + 1);
                String playerName = message.substring(firstSpaceIndex + 1, secondSpaceIndex);
                Player recipient = plugin.getServer().getPlayer(playerName);

                if (recipient == null)
                {
                    return;
                }

                if (!recipient.hasPermission("rolyd.mod"))
                {
                    event.setCancelled(true);
                    event.getPlayer().sendMessage(ChatColor.DARK_RED +
                            "You are muted and can only message moderators and GMs.");
                    return;
                }
            }
        }
    }

    public static void chatPreProcess(AsyncPlayerChatEvent event)
    {
        if (Mute.getMuted().contains(event.getPlayer().getName().toLowerCase()))
        {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.DARK_RED + "You are muted.");
        }
    }

    public static List<String> getMuted()
    {
        return muted;
    }
}
