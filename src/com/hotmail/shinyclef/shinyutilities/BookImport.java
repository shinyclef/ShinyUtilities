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

/**
 * User: Shinyclef
 * Date: 29/07/13
 * Time: 12:45 AM
 */

public class BookImport
{
    private static ShinyUtilities plugin;
    private static final int MAX_LENGTH = 256;

    public static void initialise(ShinyUtilities thePlugin)
    {
        plugin = thePlugin;
    }

    public static boolean importBook(CommandSender sender, String[] args)
    {
        //perms
        if (!sender.hasPermission("rolyd.mod") && !sender.hasPermission("rolyd.vip"))
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
        if (!urlString.endsWith(".txt"))
        {
            sender.sendMessage(ChatColor.RED + "You must provide a url of a .txt file.");
            return true;
        }

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
        new ParseBook((Player)sender, url, refund).runTaskAsynchronously(plugin);

        return true;
    }

    private static class ParseBook extends BukkitRunnable
    {
        private static Player player;
        private static URL url;
        private static List<String> pageList;
        private static ItemStack refund;

        private ParseBook(Player thePlayer, URL theUrl, ItemStack theRefund)
        {
            player = thePlayer;
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

                //get title and author
                line = in.readLine();
                title = line.substring(line.indexOf(" ") + 1);
                line = in.readLine();
                author = line.substring(line.indexOf(" ") + 1);

                //working vars
                String currentPage = "";

                //get pages
                while ((line = in.readLine()) != null)
                {
                    currentPage = currentPage + line + "\n"; //add new line to the currentPage
                    currentPage = splitPages(line, currentPage);
                }

                //add the final page
                pageList.add(currentPage);
            }
            catch (FileNotFoundException e)
            {
                bookData.error = "File not found.";
            }
            catch (IOException e)
            {
                bookData.error = "IO Exception: " + e.getMessage();
            }
            finally
            {
                try
                {
                    in.close();
                }
                catch (IOException e){}
            }

            bookData.title = title;
            bookData.author = author;
            bookData.pages = new String[pageList.size()];
            bookData.pages = pageList.toArray(bookData.pages);

            //pass the book back to sync thread to finish
            new BookReturn(bookData, player, refund).runTask(plugin);
        }

        /* Splits pages in a line and returns working page */
        private String splitPages(String line, String currentPage)
        {
            int breakIndex;
            int pageLength;

            breakIndex = currentPage.indexOf(" |");
            pageLength = (breakIndex == -1) ? currentPage.length() : breakIndex - 1;

            while (pageLength > MAX_LENGTH) //split pages while currentPage does not fit on a mc book page
            {
                //split the page and add to pageList
                int lastSpaceIndex = currentPage.lastIndexOf(" ", MAX_LENGTH);
                String splitPage = currentPage.substring(0, lastSpaceIndex);
                pageList.add(splitPage);

                //start new current page and get new page break index + page length
                currentPage = currentPage.substring(lastSpaceIndex + 1);
                breakIndex = currentPage.indexOf(" |");
                pageLength = (breakIndex == -1) ? currentPage.length() : breakIndex - 1;
            }

            if (breakIndex != -1) //split pages on user designated breaks
            {
                //add everything before " |" to a new page, and start building next page
                currentPage = currentPage.substring(0, currentPage.indexOf(" |"));
                pageList.add(currentPage);
                currentPage = line.substring(line.indexOf(" |") + 2);
            }

            return currentPage;
        }

        private ColourString colourString (String input, ChatColor firstColour)
        {
            //make a new colour page object
            ColourString colourString = new ColourString();
            input = firstColour + input; //set initial colour

            //working vars
            int index;
            ChatColor newColour;

            //replace colour codes with chat colours
            while ((index = input.indexOf("~!")) != -1)
            {
                //get the chat colour with colour code
            }
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
            if (inventory.firstEmpty() == -1)
            {
                player.sendMessage(ChatColor.RED + "Your inventory is full.");
                return;
            }

            //give it to the player
            inventory.addItem(bookItem);

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

    private static class ColourString
    {
        String currentPage;
        ChatColor lastColour;
    }
}
