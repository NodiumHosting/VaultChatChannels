package com.nodiumhosting.vaultchatchannels.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.nodiumhosting.vaultchatchannels.ChannelPlayerData;
import com.nodiumhosting.vaultchatchannels.ChatChannel;
import com.nodiumhosting.vaultchatchannels.Group;
import com.nodiumhosting.vaultchatchannels.GroupData;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.server.command.EnumArgument;

import java.util.Arrays;
import java.util.List;

public class ChatGroupCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("chatgroup")
                .executes(ChatGroupCommand::root)
                .then(Commands.literal("invite")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ChatGroupCommand::inviteSubCommand)
                        )
                )
                .then(Commands.literal("accept")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ChatGroupCommand::acceptSubCommand)
                        )
                )
                .then(Commands.literal("leave")
                        .executes(ChatGroupCommand::leaveSubCommand)
                )
        );
    }

    private static int root(CommandContext<CommandSourceStack> ctx) {
        if (!(ctx.getSource().getEntity() instanceof Player player)) {
            ctx.getSource().sendFailure(new TextComponent("You must be a player to use this command"));
            return 0;
        }
        Group group = GroupData.getGroup(player.getUUID());
        if (group == null) {
            ctx.getSource().sendSuccess(new TextComponent("You are not in a chat group. Use /chatgroup invite <player> to create a chat group."), false);
            return Command.SINGLE_SUCCESS;
        }

        MinecraftServer server = ctx.getSource().getServer();
        PlayerList serverPlayerList = server.getPlayerList();
        List<ServerPlayer> members = group.getPlayers().stream()
                .map(serverPlayerList::getPlayer)
                .toList();

        MutableComponent text = new TextComponent("Chat Group Members: ");
        for (int i = 0; i < members.size(); i++) {
            text.append(new TextComponent(members.get(i).getDisplayName().getString()).withStyle(ChatFormatting.GOLD));
            if (i < members.size() - 1) {
                text.append(new TextComponent(", "));
            }
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int inviteSubCommand(CommandContext<CommandSourceStack> ctx) {
        if (!(ctx.getSource().getEntity() instanceof Player player)) {
            ctx.getSource().sendFailure(new TextComponent("You must be a player to use this command"));
            return 0;
        }
        try {
            ServerPlayer invitedPlayer = EntityArgument.getPlayer(ctx, "player");
            Group group = GroupData.getGroup(player.getUUID());
            group.invite(invitedPlayer.getUUID());

            player.sendMessage(new TextComponent("Invited " + invitedPlayer.getDisplayName().getString() + " to your chat group."), player.getUUID());
            MutableComponent acceptMessage = new TextComponent("You have been invited to join " + player.getDisplayName().getString() + "'s chat group. Click here to join.")
                    .withStyle(ChatFormatting.GOLD)
                    .withStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/chatgroup accept " + player.getDisplayName().getString())));

            return Command.SINGLE_SUCCESS;
        } catch (CommandSyntaxException e) {
            ctx.getSource().sendFailure(new TextComponent("Player not found"));
            return 0;
        }
    }

    private static int acceptSubCommand(CommandContext<CommandSourceStack> ctx) {
        if (!(ctx.getSource().getEntity() instanceof Player player)) {
            ctx.getSource().sendFailure(new TextComponent("You must be a player to use this command"));
            return 0;
        }
        try {
            ServerPlayer invitingPlayer = EntityArgument.getPlayer(ctx, "player");
            Group group = GroupData.getGroup(invitingPlayer.getUUID());
            if (!group.isInvited(player.getUUID())) {
                player.sendMessage(new TextComponent("You have not been invited to join " + invitingPlayer.getDisplayName().getString() + "'s chat group."), player.getUUID());
                return 0;
            }

            group.removeInvite(player.getUUID());
            group.add(player.getUUID());

            player.sendMessage(new TextComponent("You have joined " + invitingPlayer.getDisplayName().getString() + "'s chat group."), player.getUUID());
            invitingPlayer.sendMessage(new TextComponent(player.getDisplayName().getString() + " has joined your chat group."), invitingPlayer.getUUID());

            return Command.SINGLE_SUCCESS;
        } catch (CommandSyntaxException e) {
            ctx.getSource().sendFailure(new TextComponent("Player not found"));
            return 0;
        }
    }

    private static int leaveSubCommand(CommandContext<CommandSourceStack> ctx) {
        if (!(ctx.getSource().getEntity() instanceof Player player)) {
            ctx.getSource().sendFailure(new TextComponent("You must be a player to use this command"));
            return 0;
        }
        Group group = GroupData.getGroup(player.getUUID());
        if (group == null) {
            player.sendMessage(new TextComponent("You are not in a chat group."), player.getUUID());
            return 0;
        }

        group.remove(player.getUUID());
        player.sendMessage(new TextComponent("You have left the chat group."), player.getUUID());

        MinecraftServer server = ctx.getSource().getServer();
        PlayerList serverPlayerList = server.getPlayerList();

        List<ServerPlayer> members = group.getPlayers().stream()
                .map(serverPlayerList::getPlayer)
                .toList();

        for (ServerPlayer member : members) {
            member.sendMessage(new TextComponent(player.getDisplayName().getString() + " has left the chat group."), member.getUUID());
        }

        return Command.SINGLE_SUCCESS;
    }
}