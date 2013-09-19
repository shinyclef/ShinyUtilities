package com.hotmail.shinyclef.shinyutilities;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: Shinyclef
 * Date: 29/07/13
 * Time: 12:45 AM
 */

public class BookImport
{
    private static ShinyUtilities plugin;
    private static final int MAX_LENGTH = 256;
    private static final String USER_BREAK = " |";
    private static final String COLOUR_CODE_REGEX = "\\$[A-Za-z_]{1,13}\\$";


    public static void initialise(ShinyUtilities thePlugin)
    {
        plugin = thePlugin;
    }

    public static boolean importBook(CommandSender sender, String[] args)
    {
        //perms
        if (!sender.hasPermission("rolyd.mod") && !sender.hasPermission("rolyd.vip")
                &&!sender.hasPermission("rolyd.exp"))
        {
            sender.sendMessage(ChatColor.RED + "You don't have permission to do that.");
            return true;
        }

        //args
        if (args.length != 1)
        {
            return false;
        }

        ItemStack refund = null;

        //if vip, take a valid written book from their hand
        if (!sender.hasPermission("rolyd.mod")) //they are VIP
        {
            //if they're not holding a written book
            if (((Player) sender).getItemInHand().getType() != Material.WRITTEN_BOOK)
            {
                sender.sendMessage(ChatColor.RED + "You must have a written book in your hand.");
                return true;
            }
            else //take the book and save it in refund
            {
                refund = ((Player) sender).getItemInHand();
                ((Player) sender).setItemInHand(new ItemStack(Material.AIR));
            }
        }

        String urlString = args[0].trim().toLowerCase();
/*        if (!urlString.endsWith(".txt"))
        {
            sender.sendMessage(ChatColor.RED + "You must provide a url of a .txt file.");
            //return refund
            ((Player) sender).setItemInHand(refund);
            return true;
        }*/

        URL url;
        try
        {
            url = new URL(urlString);
        }
        catch (MalformedURLException e)
        {
            sender.sendMessage(ChatColor.RED + "Malformed URL.");
            return true;
        }

        //notify and start import process
        sender.sendMessage(ChatColor.YELLOW + "Importing book...");
        String perm;
        if (sender.hasPermission("rolyd.mod"))
        {
            perm = "rolyd.mod";
        }
        else
        {
            perm = "rolyd.vip";
        }
        new ParseBook(plugin, (Player)sender, sender.getName(), perm, url, refund).runTaskAsynchronously(plugin);

        return true;
    }

    private static class ParseBook extends BukkitRunnable
    {
        ShinyUtilities plugin;
        private static Player player;
        private static String playerName;
        private static String playerPermission;
        private static URL url;
        private static List<String> pageList;
        private static ItemStack refund;
        private static String currentPage; //this is for the splitPage method

        private ParseBook(ShinyUtilities thePlugin, Player thePlayer, String thePlayerName,
                          String thePlayerPermission, URL theUrl, ItemStack theRefund)
        {
            plugin = thePlugin;
            player = thePlayer;
            playerName = thePlayerName;
            playerPermission = thePlayerPermission;
            url = theUrl;
            refund = theRefund;
            pageList = new ArrayList<String>();
        }

        @Override
        public void run()
        {
            BookData bookData = new BookData();
            String line = "", title = "", author = "";
            BufferedReader in = null;

            //read file
            try
            {
                in = new BufferedReader(new InputStreamReader(url.openStream()));

                //format check
                line = in.readLine();
                if (!line.toLowerCase().startsWith("title: "))
                {
                    throw new Exception("Incorrect book format. Please supply a direct link to a .txt file.");
                }
                //get title
                title = line.substring(line.indexOf(" ") + 1);
                title = getColourString(title);

                //second line check
                line = in.readLine();
                if (!line.toLowerCase().startsWith("author: "))
                {
                    throw new Exception("Incorrect book format. Please supply a direct link to a .txt file.");
                }

                if (playerPermission.equals("rolyd.mod"))
                {
                    author = line.substring(line.indexOf(" ") + 1);
                    author = getColourString(author);
                }
                else
                {
                    author = playerName;
                }

                //get pages
                String currentUserPage = "";
                while ((line = in.readLine()) != null)
                {
                    currentUserPage = currentUserPage + line + "\n"; //reattach new line
                    currentUserPage = getColourString(currentUserPage); //get a colourString object
                    currentUserPage = splitPages(currentUserPage); //finally, split pages
                }

                //add the final page
                pageList.add(currentUserPage);

                in.close();
            }
            catch (FileNotFoundException e)
            {
                bookData.error = "File not found error: " + e.getMessage();
            }
            catch (IOException e)
            {
                bookData.error = "IO Error: " + e.getMessage();
            }
            catch (Exception e)
            {
                bookData.error = e.getMessage();
            }
            finally
            {
                try
                {
                    in.close();
                }
                catch (IOException e)
                {
                   SyncLog.log(e.getMessage());
                }
                catch (NullPointerException e)
                {
                    SyncLog.log(e.getMessage());
                }
            }

            bookData.title = title;
            bookData.author = author;
            bookData.pages = new String[pageList.size()];
            bookData.pages = pageList.toArray(bookData.pages);

            //pass the book back to sync thread to finish
            new BookReturn(bookData, player, refund).runTask(plugin);
        }

        /* Splits currentPage if it has a break or is too long and returns working currentPage */
        private String splitPages(String theCurrentPage)
        {
            int breakIndex;
            int pageLength;
            String lastColour;
            currentPage = theCurrentPage;

            //remove spaces after userbreaks
            currentPage = currentPage.replaceAll(" \\| ", USER_BREAK);

            //get break index and page length
            breakIndex = currentPage.indexOf(USER_BREAK);
            pageLength = getPageLength(breakIndex);

            while (pageLength > MAX_LENGTH) //split pages while currentPage does not fit on a mc book page
            {
                //split the page and add to pageList
                int lastSpaceIndex = currentPage.lastIndexOf(" ", MAX_LENGTH);
                String splitPage = currentPage.substring(0, lastSpaceIndex);
                pageList.add(splitPage);

                //get lastColour
                lastColour = ChatColor.getLastColors(splitPage);

                //start new current page (with possible starting colour)
                currentPage = currentPage.substring(lastSpaceIndex + 1);
                if (!lastColour.equals(ChatColor.BLACK.toString()) && !lastColour.equals(ChatColor.RESET.toString()))
                {
                    currentPage = lastColour + currentPage;
                }

                //get new page break index + page length
                breakIndex = currentPage.indexOf(USER_BREAK);
                pageLength = getPageLength(breakIndex);
            }

            //at the end of all the 'too long' splits, we may still have to split on user breaks
            String remainder = currentPage;
            int toIndex = remainder.indexOf(USER_BREAK);
            while (toIndex != -1) //split pages on user designated breaks
            {
                //add everything before USER_BREAK (" |") to a new page, and get its last colour
                currentPage = remainder.substring(0, toIndex);
                pageList.add(currentPage);
                lastColour = ChatColor.getLastColors(currentPage);

                //readjust remainder new page. remove first newline if present, and add starting colour if not black
                remainder = remainder.substring(toIndex + 2);
                if (remainder.startsWith("\n"))
                {
                    remainder = remainder.substring(1);
                }
                if (!lastColour.equals(ChatColor.BLACK.toString()) && !lastColour.equals(ChatColor.RESET.toString()))
                {
                    remainder = lastColour + remainder;
                }

                //get new toIndex
                toIndex = remainder.indexOf(USER_BREAK);
            }

            return remainder;
        }

        private int getPageLength(int breakIndex)
        {
            int pageLength;

            if(breakIndex == -1)
            {
                pageLength = currentPage.length();
            }
            else if(breakIndex == 0)
            {
                //cut off new page marker if it's at the start of the page
                currentPage = currentPage.substring(2);
                pageLength = currentPage.length();
            }
            else
            {
                pageLength = breakIndex - 1;
            }

            return pageLength;
        }

        private String getColourString (String input)
        {
            //working vars
            String toReplace;
            ChatColor colour;

            Pattern pattern = Pattern.compile(COLOUR_CODE_REGEX);
            Matcher matcher = pattern.matcher(input);
            while (matcher.find())
            {
                toReplace = input.substring(matcher.start(), matcher.end()); //get the bit to replace
                colour = getChatColor(toReplace); //get the colour to use
                input = matcher.replaceFirst(colour + ""); //replace the bit with ChatColor
                matcher.reset(input);
            }

            return input;
        }

        private ChatColor getChatColor(String input)
        {
            input = input.substring(1, input.length() - 1).toUpperCase();
            ChatColor colour;

            if (input.length() < 1)
            {
                return ChatColor.BLACK;
            }

            try
            {
                colour = ChatColor.valueOf(input);
            }
            catch (IllegalArgumentException e)
            {
                colour = ChatColor.BLACK;
            }

            return colour;
        }
    }

    private static class BookReturn extends BukkitRunnable
    {
        private static BookData bookData;
        private static Player player;
        private static ItemStack refund;

        private BookReturn(BookData theBookData, Player thePlayer, ItemStack theRefund)
        {
            bookData = theBookData;
            player = thePlayer;
            refund = theRefund;
        }

        @Override
        public void run()
        {
            //if we received an error, show the error, refund original and cancel
            if (bookData.error != null)
            {
                player.sendMessage(ChatColor.RED + bookData.error);
                if (refund != null)
                {
                    player.setItemInHand(refund);
                }
                return;
            }

            //make the book item
            ItemStack bookItem = new ItemStack(Material.WRITTEN_BOOK);
            BookMeta meta = (BookMeta)bookItem.getItemMeta();
            meta.setTitle(bookData.title);
            meta.setAuthor(bookData.author);
            meta.setPages(bookData.pages);
            bookItem.setItemMeta(meta);

            //check for inv space
            Inventory inventory = player.getInventory();
            boolean invFull = false;
            if (inventory.firstEmpty() == -1)
            {
                player.sendMessage(ChatColor.RED + "Your inventory is full, dropping book on the ground.");
                invFull = true;
            }

            //give it to the player
            if (!invFull)
            {
                inventory.addItem(bookItem);
            }
            else
            {
                player.getWorld().dropItemNaturally(player.getLocation(), bookItem);
            }

            //notify player
            player.sendMessage(ChatColor.AQUA + bookData.title + ChatColor.YELLOW + " by " + ChatColor.AQUA +
                    bookData.author + ChatColor.YELLOW + " successfully created.");
        }
    }

    private static class BookData
    {
        String title;
        String author;
        String[] pages;
        String error;
    }

    public static class SyncLog extends BukkitRunnable
    {
        String msg;

        public SyncLog(String msg)
        {
            this.msg = msg;
        }

        public static void log(String msg)
        {
            new SyncLog(msg).runTask(plugin);
        }

        @Override
        public void run()
        {
            plugin.getLogger().info(msg);
        }
    }
}
