package com.hotmail.shinyclef.shinyutilities;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

/**
 * User: Shinyclef
 * Date: 22/07/13
 * Time: 10:42 PM
 */

public class MuteHandler
{

    private static ShinyUtilities plugin;

    private static HashSet<String> muted;
    private static List<String> muteinfolist = null;
    private static String muteinfo = "";
    private static String ucasename = "";
    private static Player[] onlineplayers = null;

    public static void initialize(ShinyUtilities thePlugin)
    {
        plugin = thePlugin;

        if (muteinfolist == null)
        {
            muteinfolist = new ArrayList<String>();
        }


        if (muted == null)
        {
            muted = new HashSet<String>();
        }
    }

    public static boolean mute(CommandSender sender, String[] args)
    {
        //1 arg only.
        if (args.length != 1)
            return false;

        //vars
        String lcasename = args[0].toLowerCase();
        Player player = null;

        //if player is already muted
        if (muted.contains(lcasename))
        {
            sender.sendMessage(ChatColor.GOLD + "That player is already muted.");
            return true;
        }

        //if player is not online
        if (!plugin.getServer().getOfflinePlayer(args[0]).isOnline())
        {
            sender.sendMessage(ChatColor.RED + "That player is not online.");
            return true;
        }

        //player is online (always true)
        player = plugin.getServer().getPlayer(args[0]);

        //date stuff
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        Date date = new Date();

        //add all info to muteinfo
        muteinfo = player.getName() + "|" + dateFormat.format(date) + "|" + sender.getName();

        //add muteinfo to mutelist.
        muteinfolist.add(muteinfo);

        //add player to Set 'muted' in eventListener.
        muted.add(lcasename);

        //notify admin team
        onlineplayers = plugin.getServer().getOnlinePlayers();
        for (Player onlineplayer : onlineplayers)
        {
            if (onlineplayer.hasPermission("rolyd.mod"))
            {
                onlineplayer.sendMessage(ChatColor.DARK_RED + plugin.getServer().getPlayer(args[0]).getName() +
                        ChatColor.GOLD + " has been muted by " + ChatColor.DARK_GREEN + sender.getName());
            }
        }

        //if online, notify muted player
        if (player.isOnline())
        {
            player.sendMessage(ChatColor.DARK_RED + "You can't chat anymore.");
            return true;
        }

        return true;
    }

    public static boolean unmute(CommandSender sender, String[] args)
    {
        //1 arg only.
        if (args.length != 1)
            return false;

        //vars
        String lcasename = args[0].toLowerCase();

        //if player is not in Set 'muted'.
        if (!muted.contains(lcasename))
        {
            sender.sendMessage(ChatColor.GOLD + "That player is not muted.");
            return true;
        }

        //remove player from muteinfolist
        for (String string : muteinfolist)
        {
            //divide the string into array 'item' on first char "|" (muted player | date, time, muter)
            String[] item = string.split("\\|", 2);

            //check name against arg and remove whole string on match
            if (item[0].equalsIgnoreCase(args[0]))
            {
                ucasename = item[0];
                muteinfolist.remove(string);
                break;
            }
        }

        //remove player from Set 'muted'.
        muted.remove(lcasename);

        //notify admin team
        onlineplayers = plugin.getServer().getOnlinePlayers();
        for (Player onlineplayer : onlineplayers)
        {
            if (onlineplayer.hasPermission("rolyd.mod"))
            {
                onlineplayer.sendMessage(ChatColor.DARK_RED + ucasename + ChatColor.GOLD + " has been unmuted by " +
                        ChatColor.DARK_GREEN + sender.getName());
            }
        }

        //if online, notify unmuted player
        if (plugin.getServer().getOfflinePlayer(args[0]).isOnline())
        {
            plugin.getServer().getPlayer(args[0]).sendMessage(ChatColor.DARK_GREEN + "You can chat again.");
        }

        return true;
    }

    public static boolean mutelist(CommandSender sender, String[] args)
    {
        //if Set 'muted' is empty
        if (muted.isEmpty())
        {
            sender.sendMessage(ChatColor.GOLD + "No one is muted.");
            return true;
        }

        //send title "Currently Muted"
        sender.sendMessage(ChatColor.GOLD + "Currently Muted");


        //Display mutelist in a message to command sender.
        for (String string : muteinfolist)
        {
            //divide the string into array 'item' on first char "|" (muted player | date, time | muter)
            String[] item = string.split("\\|");

            //send the line
            sender.sendMessage(ChatColor.DARK_RED + item[0] + ChatColor.GOLD + " - " + item[1] +
                    ChatColor.DARK_GREEN + " - " + item[2]);
        }

        return true;
    }

    public static void commandPreProcess(PlayerCommandPreprocessEvent event)
    {
        if (MuteHandler.getMuted().contains(event.getPlayer().getName().toLowerCase()))
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

    public static void chatPreProcess(AsyncPlayerChatEvent event)
    {
        if (MuteHandler.getMuted().contains(event.getPlayer().getName().toLowerCase()))
        {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.DARK_RED + "You are muted.");
        }
    }

    public static HashSet<String> getMuted()
    {
        return muted;
    }
}
