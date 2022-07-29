package com.gandy.automod.commands;


import com.gandy.automod.CommandManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextComponent;
import org.apache.logging.log4j.LogManager;

public class AutoWoodCommand implements IModCommand {

    public boolean active = false;
    public String label = "autowood";

    @Override
    public void register(CommandDispatcher<CommandSource> dispatcher) {
        LogManager.getLogger().info("Loading auto Command");
        LiteralArgumentBuilder<CommandSource> autoCommand = Commands.literal(label).executes(commandContext -> run(commandContext));
        dispatcher.register(autoCommand);
    }

    @Override
    public int run (CommandContext<CommandSource> commandContext) {
        active = !active;
        if (active) {
            createMessage("enabled autowood");
            if (CommandManager.getInstance().autoMineCommand.active) {
                CommandManager.getInstance().autoMineCommand.active = false;
                createMessage("disabled automine");
            }
        }  else {
            createMessage("disabled autowood");
        }

        return 1;
    }
}
