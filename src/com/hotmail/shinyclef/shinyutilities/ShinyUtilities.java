package com.hotmail.shinyclef.shinyutilities;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * Author: ShinyClef
 * Date: 24/06/12
 * Time: 9:42 PM
 */

public class ShinyUtilities extends JavaPlugin
{
    private EventListener eventListener;

    @Override
    public void onEnable()
    {
        eventListener = new EventListener(this);
        CmdExecutor cmdExecutor = new CmdExecutor(this, eventListener);
        getCommand("mute").setExecutor(cmdExecutor);
        getCommand("unmute").setExecutor(cmdExecutor);
        getCommand("mutelist").setExecutor(cmdExecutor);


        // Save default config.yml
        //if (!new File(getDataFolder(), "config.yml").exists())
        //    saveDefaultConfig();
        //NOTE: IF CONFIG WILL BE USED, ADD IT TO PROJECT STRUCTURE.
    }

    @Override
    public void onDisable()
    {

    }
}
