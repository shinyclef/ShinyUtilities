package com.hotmail.shinyclef.shinyutilities;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 * Author: Shinyclef
 * Date: 25/06/12
 * Time: 12:34 AM
 */

public class CmdExecutor implements CommandExecutor
{
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        if (command.getName().equalsIgnoreCase("mute"))
        {
            MuteHandler.mute(sender, args);
        }

        if (command.getName().equalsIgnoreCase("unmute"))
        {
            MuteHandler.unmute(sender, args);
        }

        if (command.getName().equalsIgnoreCase("mutelist"))
        {
            MuteHandler.mutelist(sender, args);
        }

        return false;
    }
}

