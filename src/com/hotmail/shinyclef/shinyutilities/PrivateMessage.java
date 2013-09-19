package com.hotmail.shinyclef.shinyutilities;

import com.hotmail.shinyclef.shinybase.ShinyBaseAPI;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * User: Shinyclef
 * Date: 21/08/13
 * Time: 4:28 AM
 */

public class PrivateMessage
{
    private static ShinyUtilities p;
    private static ShinyBaseAPI base;
    private static Map<String, String> replyMap; //normal name for key
    private static Map<String, String> lastMsgMap; //normal name for key
    private static List<String> spyList; //normal case

    public static void initialize(ShinyUtilities plugin, ShinyBaseAPI base)
    {
        p = plugin;
        PrivateMessage.base = base;
        replyMap = new HashMap<>();
        lastMsgMap = new HashMap<>();
        spyList = plugin.getConfig().getStringList("Spy");
        if (spyList == null)
        {
            spyList = new ArrayList<String>();
        }
    }

    public static boolean processPrivateMessage(CommandSender sender, String[] args)
    {
        //args at least 2
        if (args.length < 2)
        {
            return false;
        }

        //get recipient using AdminCmd algorithm, and make sure they're available
        String recipient = getRecipientName(args[0]);
        if (!isAvailable(sender, recipient))
        {
            //isAvailable handles user feedback if unavailable
            return true;
        }

        sendPrivateMessage(sender, args, recipient, false);

        return true;
    }

    public static boolean reply(CommandSender sender, String[] args)
    {
        //args length
        if (args.length < 1)
        {
            return false;
        }

        //check if they have received any private messages
        if (!replyMap.containsKey(sender.getName()))
        {
            sender.sendMessage(ChatColor.RED +
                    "No one has sent you a private message to reply to.");
            return true;
        }

        //check if recipient is offline
        String recipient = replyMap.get(sender.getName());
        if (!Bridge.isOnlineAnywhere(recipient))
        {
            sender.sendMessage(ChatColor.RED + recipient + " is not online.");
            return true;
        }

        //send the message
        sendPrivateMessage(sender, args, recipient, true);

        return true;
    }

    public static boolean messageLast(CommandSender sender, String[] args)
    {
        //args length
        if (args.length < 1)
        {
            return false;
        }

        //check if they have sent any private messages
        if (!lastMsgMap.containsKey(sender.getName()))
        {
            sender.sendMessage(ChatColor.RED +
                    "You have not sent any private messages yet.");
            return true;
        }

        //check if recipient is offline
        String recipient = lastMsgMap.get(sender.getName());
        if (!Bridge.isOnlineAnywhere(recipient))
        {
            sender.sendMessage(ChatColor.RED + recipient + " is not online.");
            return true;
        }

        //send the message
        sendPrivateMessage(sender, args, recipient, true);

        return true;
    }

    private static void sendPrivateMessage(CommandSender sender, String[] args, String recipient, boolean isReply)
    {
        int startIndex = 1;
        if (isReply)
        {
            startIndex = 0;
        }

        //make sentence and convert colour codes
        String senderName = sender.getName();
        String message = base.makeSentence(args, startIndex);
        String colourMsg = ChatColor.translateAlternateColorCodes('&', message);

        //create 'to'/'from' tags
        String toTag = ChatColor.RED + "To " + ChatColor.YELLOW + recipient + ChatColor.RED + " <- ";
        String fromTag = ChatColor.RED + "From " + ChatColor.YELLOW + senderName + ChatColor.RED + " -> ";

        //send messages
        Bridge.sendMessage(senderName, toTag + ChatColor.WHITE + colourMsg);
        Bridge.sendMessage(recipient, fromTag + ChatColor.WHITE + colourMsg);

        //set values in replyMap nad lastMsgMap
        replyMap.put(recipient, senderName);
        lastMsgMap.put(senderName, recipient);

        //send message to spy recipients
        if (!spyList.isEmpty())
        {
            String spyMsg = ChatColor.GREEN + "[Spy] " + ChatColor.YELLOW +
                    senderName + " -> " + recipient + ": " + ChatColor.WHITE + colourMsg;

            for (String spyName : spyList)
            {
                //if the spy is 'not' the sender or the recipient, send them the spy msg
                if (!senderName.equals(spyName) && (!recipient.equals(spyName)))
                {
                    Bridge.sendMessage(spyName, spyMsg);
                }
            }
        }
    }

    public static boolean toggleSpy(CommandSender sender, String[] args)
    {
        //perm
        if (!sender.hasPermission("rolyd.mod"))
        {
            sender.sendMessage("You do not have permission to do that.");
            return true;
        }

        //args
        if (args.length != 0)
        {
            return false;
        }

        String feedback;

        if (spyList.contains(sender.getName()))
        {
            //remove from spy list
            spyList.remove(sender.getName());
            feedback = "SpyMsg mode disabled.";

        }
        else //spyList does not contain sender's name
        {
            //add to spy list
            spyList.add(sender.getName());
            feedback = "SpyMsg mode enabled.";
        }

        //update config
        p.getConfig().set("Spy", spyList);
        p.saveConfig();

        //send user feedback
        sender.sendMessage(ChatColor.DARK_AQUA + feedback);
        return true;
    }

    /* Taken directly from AdminCmd code with some adaptions. Full credit goes to Balor.
    * https://github.com/Belphemur/AdminCmd/blob/master/src/main/java/be/Balor/Tools/Utils.java*/
    public static String getRecipientName(String name)
    {
        final Set<Player> players = Bridge.getOnlinePlayersSet();
        Player found = null;
        final String lowerName = name.toLowerCase();
        int smallestDif = Integer.MAX_VALUE;

        //search through all online players
        for (final Player player : players)
        {
            //if the online player starts with the search string
            if (player.getName().toLowerCase().startsWith(lowerName))
            {
                //store difference in length in thisDif
                int thisDif = player.getName().length() - lowerName.length();

                //if it's the smallest difference, make this our found player
                if (thisDif < smallestDif)
                {
                    found = player;
                    smallestDif = thisDif;
                }

                //stop searching if the name is exact
                if (thisDif == 0)
                {
                    break;
                }
            }
        }

        //return result's name if there is one
        if (found != null)
        {
            return found.getName();
        }
        else
        {
            return null;
        }
    }

    private static boolean isAvailable(CommandSender sender, String recipient)
    {
        if (recipient == null)
        {
            sender.sendMessage(ChatColor.RED + "That player is not online.");
            return false;
        }

        //make sure recipient is not busy
        if (Busy.getBusyMap().containsKey(recipient.toLowerCase()))
        {
            //check for busy message and send feedback to user
            String busyMsg = (String)Busy.getBusyMap().get(recipient);
            if (busyMsg.equals(""))
            {
                sender.sendMessage(ChatColor.YELLOW + "Sorry, " + recipient + " is currently busy.");
            }
            else
            {
                sender.sendMessage(ChatColor.YELLOW + "Sorry, " + recipient + " is current busy: " +
                        ChatColor.AQUA + busyMsg);
            }

            return false;
        }

        return true;
    }

    public static String getReplyTarget(String playerName)
    {
        if (replyMap.containsKey(playerName))
        {
            return replyMap.get(playerName);
        }
        else
        {
            return null;
        }
    }

    public static String getLastMsgTarget(String playerName)
    {
        return "shinyclef";
    }
}