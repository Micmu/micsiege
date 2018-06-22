package net.micmu.mcmods.micsiege.core;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

/**
 *
 * @author Micmu
 */
public class CommandSiege extends CommandBase {

    /**
     *
     */
    public CommandSiege() {
    }

    /**
     *
     */
    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    /**
     *
     */
    @Override
    public String getName() {
        return "zombiesiege";
    }

    /**
     *
     */
    @Override
    public String getUsage(ICommandSender sender) {
        return "/" + getName();
    }

    /**
     *
     */
    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        final SiegeCore core = SiegeCore.getInstance();
        final EntityPlayer player = getCommandSenderAsPlayer(sender);
        switch (core.triggerSiegeNow(player.getEntityWorld(), player.getPosition())) {
            case -3:
                throw new CommandException("Zombie Siege does not work while in Peaceful difficulty.");
            case -2:
                throw new CommandException("Zombie Siege is currently in progress.");
            case -4:
            case -1:
                throw new CommandException("You must be inside a village to trigger Zombie Siege.");
            case 0:
                throw new CommandException("Enhanced Zombie Siege not available in this World.");
            case 1:
                player.sendMessage(new TextComponentString("Zombie Siege triggered!"));
                return;
            default:
                throw new CommandException("Can not trigger Zombie Siege here.");
        }
    }
}
