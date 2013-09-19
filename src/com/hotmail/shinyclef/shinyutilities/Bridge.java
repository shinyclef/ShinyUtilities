package com.hotmail.shinyclef.shinyutilities;

import com.hotmail.shinyclef.shinybridge.ShinyBridgeAPI;
import org.bukkit.Server;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * User: Shinyclef
 * Date: 21/08/13
 * Time: 3:51 AM
 */

public class Bridge
{
    private static Server server;
    private static ShinyBridgeAPI bridge;
    private static boolean haveBridge = false;


    public static void initialize(ShinyUtilities plugin, ShinyBridgeAPI bridge)
    {
        server = plugin.getServer();
        Bridge.bridge = bridge;
        if (bridge != null)
        {
            haveBridge = true;
        }
    }

    /* ----- Bridge Routing Methods ----- */

    public static boolean isOnlineAnywhere(String playerName)
    {
        if (haveBridge)
        {
            return bridge.isOnlineServerPlusClients(playerName);
        }
        else
        {
            return server.getOfflinePlayer(playerName).isOnline();
        }
    }

    public static void broadcastPermissionMessage(String message, String permission)
    {
        //send to bridge clients if we have bridge
        if (haveBridge)
        {
            bridge.broadcastMessage(message, permission, true);
        }
        else
        {
            //standard broadcast
            server.broadcast(message, permission);
        }
    }

    public static void sendMessage(String playerName, String message)
    {
        //standard send
        if (server.getOfflinePlayer(playerName).isOnline())
        {
            server.getPlayer(playerName).sendMessage(message);
        }

        //additionally if they're a bridge client
        if (haveBridge)
        {
            bridge.sendToClient(playerName, message);
        }
    }

    public static Set<Player> getOnlinePlayersSet()
    {
        if (haveBridge)
        {
            return bridge.getOnlinePlayersEverywhereSet();
        }
        else
        {
            return new HashSet<Player>(Arrays.asList(server.getOnlinePlayers()));
        }
    }
}
