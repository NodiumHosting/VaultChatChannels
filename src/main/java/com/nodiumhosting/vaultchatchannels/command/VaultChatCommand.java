package com.nodiumhosting.vaultchatchannels.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.nodiumhosting.vaultchatchannels.ChatChannel;
import com.nodiumhosting.vaultchatchannels.ChannelPlayerData;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.server.command.EnumArgument;

import java.util.Arrays;
import java.util.List;

public class VaultChatCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("chatchannels")
                .executes(VaultChatCommand::root)
                .then(Commands.literal("switch")
                        .then(Commands.argument("channel", EnumArgument.enumArgument(ChatChannel.class))
                                .executes(VaultChatCommand::switchSubCommand)
                                .suggests(channelSuggestionProvider)
                        )
                )
        );

        dispatcher.register(Commands.literal("cc")
                .executes(VaultChatCommand::ccUsage)
                .then(Commands.argument("channel", EnumArgument.enumArgument(ChatChannel.class))
                        .executes(VaultChatCommand::switchSubCommand)
                )
        );
    }

    private static final SuggestionProvider<CommandSourceStack> channelSuggestionProvider = (ctx, builder) -> {
        builder.suggest("global");
        builder.suggest("party");
        builder.suggest("vault");
        builder.suggest("group");
        return builder.buildFuture();
    };

    private static int ccUsage(CommandContext<CommandSourceStack> ctx) {
        List<String> channelNames = Arrays.stream(ChatChannel.values()).map(ChatChannel::name).toList();
        MutableComponent availableChannels = new TextComponent("");
        for (int i = 0; i < channelNames.size(); i++) {
            availableChannels.append(new TextComponent(channelNames.get(i)).withStyle(ChatFormatting.GOLD));
            if (i < channelNames.size() - 1) {
                availableChannels.append(new TextComponent(", "));
            }
        }
        if (!(ctx.getSource().getEntity() instanceof Player player)) {
            ctx.getSource().sendFailure(new TextComponent("You must be a player to use this command"));
            return 0;
        }
        ctx.getSource().sendSuccess(new TextComponent("Current channel: " + ChannelPlayerData.get(player).getChatChannel().name()).append(new TextComponent(" Usage: ")).append(new TextComponent("/cc <channel>").withStyle(ChatFormatting.GOLD)).append(new TextComponent(" Available channels: ").append(availableChannels)), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int root(CommandContext<CommandSourceStack> ctx) {
        MutableComponent line0 = new TextComponent("===== Vault Chat Channels =====\n").withStyle(Style.EMPTY.applyFormats(ChatFormatting.BLUE));
        MutableComponent line1 = new TextComponent("Use /chatchannels switch <channel> to switch chat channels\n").withStyle(Style.EMPTY.applyFormats(ChatFormatting.GRAY));
//        String channels = Arrays.stream(ChatChannel.values()).map(ChatChannel::name).reduce((a, b) -> a + ", " + b).orElse("");
        // add ($prefix) to the end of each channel name
        List<Character> prefixes = ChannelPlayerData.getPrefixes();
        MutableComponent line2 = new TextComponent("Available channels: ");
        for (int i = 0; i < prefixes.size(); i++) {
            ChatChannel channel = ChannelPlayerData.getChannelByPrefix(prefixes.get(i));
            line2.append(new TextComponent(channel.name() + " (" + prefixes.get(i) + ")").withStyle(ChatFormatting.GOLD));
            if (i < prefixes.size() - 1) {
                line2.append(new TextComponent(", "));
            }
        }
        MutableComponent line3 = new TextComponent("\n").withStyle(Style.EMPTY);
        MutableComponent line4 = new TextComponent("").append(new TextComponent("You can also use /cc as a shortcut for /chatchannels switch\n").withStyle(Style.EMPTY.applyFormats(ChatFormatting.GRAY)));
        MutableComponent line5 = new TextComponent("============================\n").withStyle(Style.EMPTY.applyFormats(ChatFormatting.BLUE));
        MutableComponent text = new TextComponent("").append(line0).append(line1).append(line2).append(line3).append(line4).append(line5);
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
}