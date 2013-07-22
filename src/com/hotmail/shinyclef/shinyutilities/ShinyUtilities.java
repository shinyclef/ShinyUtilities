package com.hotmail.shinyclef.shinyutilities;

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
}
