package com.gandy.automod.commands;

import com.gandy.automod.CommandManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import org.apache.logging.log4j.LogManager;

public class AutoMineCommand implements IModCommand {

    public boolean active = false;
    public String label = "automine";
    @Override
    public void register(CommandDispatcher<CommandSource> dispatcher) {
        LogManager.getLogger().info("Loading auto Command");
        LiteralArgumentBuilder<CommandSource> autoCommand = Commands.literal(label).executes(commandContext -> run(commandContext));
        dispatcher.register(autoCommand);
    }

    @Override
    public int run(CommandContext<CommandSource> commandContext) {
        active = !active;
        if (active) {
            createMessage("enabled automine");

            if (CommandManager.getInstance().autoWoodCommand.active) {
                CommandManager.getInstance().autoWoodCommand.active = false;
                createMessage("disabled autowood");
            }
        } else {
            createMessage("disabled automine");
        }

        return 1;
    }
}
