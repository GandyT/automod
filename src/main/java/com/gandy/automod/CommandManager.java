package com.gandy.automod;

import com.gandy.automod.commands.AutoMineCommand;
import com.gandy.automod.commands.AutoWoodCommand;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandSource;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class CommandManager {
    private static CommandManager INSTANCE;
    public static CommandManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new CommandManager();
            INSTANCE.init();
        }

        return INSTANCE;
    }

    public AutoWoodCommand autoWoodCommand;
    public AutoMineCommand autoMineCommand;

    private void init () {
        autoWoodCommand = new AutoWoodCommand();
        autoMineCommand = new AutoMineCommand();
    }

    @SubscribeEvent
    public void onRegisterCommandEvent (RegisterCommandsEvent event) {
        CommandDispatcher<CommandSource> commandDispatcher = event.getDispatcher();

        autoWoodCommand.register(commandDispatcher);
        autoMineCommand.register(commandDispatcher);
    }
}
