package com.gandy.automod.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandSource;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public interface IModCommand {
    public void register(CommandDispatcher<CommandSource> dispatcher);
    public int run(CommandContext<CommandSource> commandContext);

    public default void createMessage(String t) {
        ITextComponent text = new StringTextComponent(t);
        Minecraft.getInstance().player.sendMessage(text, Minecraft.getInstance().player.getUniqueID());
    }
}
