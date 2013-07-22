package com.hotmail.shinyclef.shinyutilities;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Author: Shinyclef
 * Date: 25/06/12
 * Time: 12:34 AM
 */

public class CmdExecutor implements CommandExecutor
{
    private ShinyUtilities plugin;
    private EventListener eventListener;
    private List<String> muteinfolist = null;
    private String muteinfo = "";
    private String ucasename = "";
    private Player[] onlineplayers = null;

    CmdExecutor (ShinyUtilities plugin, EventListener eventListener)
    {
        this.plugin = plugin;
        this.eventListener = eventListener;

        if (muteinfolist == null)
            muteinfolist = new ArrayList<String>();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        if (command.getName().equalsIgnoreCase("mute"))
        {
            //1 arg only.
            if (args.length != 1)
                return false;

            //vars
            String lcasename = args[0].toLowerCase();
            Player player = null;

            //if player is already muted
            if (eventListener.getMuted().contains(lcasename))
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
            eventListener.getMuted().add(lcasename);

            //notify admin team
            onlineplayers = plugin.getServer().getOnlinePlayers();
            for (Player onlineplayer : onlineplayers)
            {
                if (onlineplayer.hasPermission("rolyd.mod"))
                {
                    onlineplayer.sendMessage(ChatColor.DARK_RED + plugin.getServer().getPlayer(args[0]).getName() + ChatColor.GOLD + " has been muted by " + ChatColor.DARK_GREEN + sender.getName());
                }
            }

            //if online, notify muted player
            if (player.isOnline())
            {
                player.sendMessage(ChatColor.DARK_RED + "You can't chat anymore.");
                return true;
            }

        }

        if (command.getName().equalsIgnoreCase("unmute"))
        {
            //1 arg only.
            if (args.length != 1)
                return false;

            //vars
            String lcasename = args[0].toLowerCase();

            //if player is not in Set 'muted'.
            if (!eventListener.getMuted().contains(lcasename))
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
            eventListener.getMuted().remove(lcasename);

            //notify admin team
            onlineplayers = plugin.getServer().getOnlinePlayers();
            for (Player onlineplayer : onlineplayers)
            {
                if (onlineplayer.hasPermission("rolyd.mod"))
                {
                    onlineplayer.sendMessage(ChatColor.DARK_RED + ucasename + ChatColor.GOLD + " has been unmuted by " + ChatColor.DARK_GREEN + sender.getName());
                }
            }

            //if online, notify unmuted player
            if (plugin.getServer().getOfflinePlayer(args[0]).isOnline())
            {
                plugin.getServer().getPlayer(args[0]).sendMessage(ChatColor.DARK_GREEN + "You can chat again.");
            }

            return true;
        }

        if (command.getName().equalsIgnoreCase("mutelist"))
        {
            //if Set 'muted' is empty
            if (eventListener.getMuted().isEmpty())
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
                sender.sendMessage(ChatColor.DARK_RED + item[0] + ChatColor.GOLD + " - " + item[1] + ChatColor.DARK_GREEN + " - " + item[2]);
            }

            return true;
        }

        //if (command.getName().equalsIgnoreCase("debugmutelist"))
        //{
        //    sender.sendMessage(eventListener.getMuted() + "");
        //}

        return false;
    }
}

