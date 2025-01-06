package com.nodiumhosting.vaultchatchannels.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.nodiumhosting.vaultchatchannels.ChatChannel;
import com.nodiumhosting.vaultchatchannels.ChannelPlayerData;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.server.command.EnumArgument;

import java.util.Arrays;

public class VaultChatCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        EnumArgument<ChatChannel> channelEnumArgument = EnumArgument.enumArgument(ChatChannel.class);
        SuggestionProvider<CommandSourceStack> channelSuggestionProvider = (ctx, builder) -> {
            Arrays.stream(ChatChannel.values()).forEach(channel -> builder.suggest(channel.name()));
            return builder.buildFuture();
        };

        dispatcher.register(Commands.literal("chatchannels")
                .executes(VaultChatCommand::root)
                .then(Commands.literal("switch")
                        .then(Commands.argument("channel", channelEnumArgument)
                                .executes(VaultChatCommand::switchSubCommand)
                        )
                )
                .then(Commands.literal("prefix")
                        .then(Commands.argument("channel", channelEnumArgument)
                                .then(Commands.argument("prefix", StringArgumentType.word())
                                        .executes(VaultChatCommand::prefixSubCommand)
                                )
                        )
                )
        );

        dispatcher.register(Commands.literal("cc")
                .then(Commands.argument("channel", channelEnumArgument)
                        .executes(VaultChatCommand::switchSubCommand)
                )
        );
    }

    private static int root(CommandContext<CommandSourceStack> ctx) {
        MutableComponent line0 = new TextComponent("===== Vault Chat Channels =====\n").withStyle(Style.EMPTY.applyFormats(ChatFormatting.BLUE));
        MutableComponent line1 = new TextComponent("Use /chatchannels switch <channel> to switch chat channels\n").withStyle(Style.EMPTY.applyFormats(ChatFormatting.GRAY));
        MutableComponent line2 = new TextComponent("Use /chatchannels prefix <channel> <prefix> to set a prefix for a chat channel\n").withStyle(Style.EMPTY.applyFormats(ChatFormatting.GRAY));
        String channels = Arrays.stream(ChatChannel.values()).map(ChatChannel::name).reduce((a, b) -> a + ", " + b).orElse("");
        MutableComponent line3 = new TextComponent("Available channels: " + channels + "\n").withStyle(Style.EMPTY.applyFormats(ChatFormatting.GRAY));
        MutableComponent line4 = new TextComponent("\n").withStyle(Style.EMPTY);
        MutableComponent line5 = new TextComponent("").append(new TextComponent("You can also use /cc as a shortcut for /chatchannels switch\n").withStyle(Style.EMPTY.applyFormats(ChatFormatting.GRAY)));
        MutableComponent line6 = new TextComponent("============================\n").withStyle(Style.EMPTY.applyFormats(ChatFormatting.BLUE));
        MutableComponent text = new TextComponent("").append(line0).append(line1).append(line2).append(line3).append(line4).append(line5).append(line6);
        ctx.getSource().sendSuccess(text, false);
        return Command.SINGLE_SUCCESS;
    }

    private static int switchSubCommand(CommandContext<CommandSourceStack> ctx) {
        if (!(ctx.getSource().getEntity() instanceof Player player)) {
            ctx.getSource().sendFailure(new TextComponent("You must be a player to use this command"));
            return 0;
        }

        ChatChannel channel = ctx.getArgument("channel", ChatChannel.class);
        ChannelPlayerData.get(player).setChatChannel(channel);
        player.sendMessage(new TextComponent("Switched to chat channel: " + channel.name()), player.getUUID());

        return Command.SINGLE_SUCCESS;
    }

    private static int prefixSubCommand(CommandContext<CommandSourceStack> ctx) {
        if (!(ctx.getSource().getEntity() instanceof Player player)) {
            ctx.getSource().sendFailure(new TextComponent("You must be a player to use this command"));
            return 0;
        }

        ChatChannel channel = ctx.getArgument("channel", ChatChannel.class);
        String prefix = ctx.getArgument("prefix", String.class);
        if (prefix.length() != 1) {
            player.sendMessage(new TextComponent("Prefix must be a single character"), player.getUUID());
            return 0;
        }
        ChannelPlayerData.get(player).setPrefix(channel, prefix.charAt(0));
        player.sendMessage(new TextComponent("Set prefix for channel " + channel.name() + " to " + prefix), player.getUUID());

        return Command.SINGLE_SUCCESS;
    }
}