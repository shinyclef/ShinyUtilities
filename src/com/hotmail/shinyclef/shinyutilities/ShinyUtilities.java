package com.hotmail.shinyclef.shinyutilities;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

/**
 * Author: ShinyClef
 * Date: 24/06/12
 * Time: 9:42 PM
 */

public class ShinyUtilities extends JavaPlugin
{
    @Override
    public void onEnable()
    {
        new EventListener(this);
        CmdExecutor cmdExecutor = new CmdExecutor();
        getCommand("mute").setExecutor(cmdExecutor);
        getCommand("unmute").setExecutor(cmdExecutor);
        getCommand("mutelist").setExecutor(cmdExecutor);
        getCommand("busy").setExecutor(cmdExecutor);

        Mute.initialize(this);
        Busy.initialize(this);

        // Save default config.yml
        if (!new File(getDataFolder(), "config.yml").exists())
        {
            saveDefaultConfig();
        }
    }

    @Override
    public void onDisable()
    {

    }

    /* Taken directly from AdminCmd code with a couple of small changes. Full credit goes to Balor.
     * https://github.com/Belphemur/AdminCmd/blob/master/src/main/java/be/Balor/Tools/Utils.java*/
    public String getPlayerName(final String name)
    {
        final Player[] players = this.getServer().getOnlinePlayers();
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

        //return result's name in lower case if there is one
        if (found != null)
        {
            return found.getName().toLowerCase();
        }
        else
        {
            return null;
        }
    }
}
