package com.hotmail.shinyclef.shinyutilities;

import com.hotmail.shinyclef.shinybase.ShinyBase;
import com.hotmail.shinyclef.shinybase.ShinyBaseAPI;
import com.hotmail.shinyclef.shinybridge.ShinyBridge;
import com.hotmail.shinyclef.shinybridge.ShinyBridgeAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;

/**
 * Author: ShinyClef
 * Date: 24/06/12
 * Time: 9:42 PM
 */

public class ShinyUtilities extends JavaPlugin
{
    private static ShinyBaseAPI shinyBaseAPI;
    private static ShinyBridgeAPI shinyBridgeAPI;

    @Override
    public void onEnable()
    {
        Plugin p;
        p = Bukkit.getPluginManager().getPlugin("ShinyBase");
        if (p != null)
        {
            shinyBaseAPI = ((ShinyBase)p).getShinyBaseAPI();
        }

        //get ShinyBridge
        p = Bukkit.getPluginManager().getPlugin("ShinyBridge");
        if (p != null)
        {
            ShinyBridge shinyBridge = (ShinyBridge) p;
            shinyBridgeAPI = shinyBridge.getShinyBridgeAPI();
        }

        new EventListener(this);
        CmdExecutor cmdExecutor = new CmdExecutor();
        getCommand("msg").setExecutor(cmdExecutor);
        getCommand("ml").setExecutor(cmdExecutor);
        getCommand("r").setExecutor(cmdExecutor);
        getCommand("spy").setExecutor(cmdExecutor);
        getCommand("mute").setExecutor(cmdExecutor);
        getCommand("unmute").setExecutor(cmdExecutor);
        getCommand("mutelist").setExecutor(cmdExecutor);
        getCommand("busy").setExecutor(cmdExecutor);
        getCommand("bookimport").setExecutor(cmdExecutor);

        Bridge.initialize(this, shinyBridgeAPI);
        PrivateMessage.initialize(this, shinyBaseAPI);
        Mute.initialize(this, shinyBaseAPI);
        Busy.initialize(this);
        BookImport.initialise(this);

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
