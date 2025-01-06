package com.nodiumhosting.vaultchatchannels.event;

import com.nodiumhosting.vaultchatchannels.ChannelPlayerData;
import com.nodiumhosting.vaultchatchannels.ChatChannel;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.world.data.ServerVaults;
import iskallia.vault.world.data.VaultPartyData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Mod.EventBusSubscriber({Dist.DEDICATED_SERVER})
public class ServerChatEventHandler {
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void handleChat(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();
        String message = event.getMessage();
        Component component = event.getComponent();
        ChannelPlayerData playerData = ChannelPlayerData.get(player);
        ChatChannel channel = playerData.getChatChannel();
        List<Character> prefixes = playerData.getPrefixes();

        for (Character prefix : prefixes) {
            if (message.charAt(0) != prefix) {
                continue;
            }

            String newMessage = message.substring(1);
            component = new TextComponent(component.getString().replace(message, newMessage)).setStyle(component.getStyle());
            message = newMessage;
            channel = playerData.getChannelByPrefix(prefix);
            break;
        }

        event.setCanceled(true);

        if (channel == ChatChannel.global) {
            sendGlobalMessage(player, component);
        } else if (channel == ChatChannel.party) {
            sendPartyMessage(player, component);
        } else if (channel == ChatChannel.vault) {
            sendVaultMessage(player, component);
        }
    }

    private static void sendGlobalMessage(ServerPlayer player, Component component) {
        MinecraftServer server = player.getServer();
        if (server == null) return;
        PlayerList playerList = server.getPlayerList();
        List<ServerPlayer> players = playerList.getPlayers();
        sendComponentToPlayers(players, component, ChatChannel.global);
    }

    private static void sendPartyMessage(ServerPlayer player, Component component) {
        VaultPartyData partyData = VaultPartyData.get(player.getLevel());
        Optional<VaultPartyData.Party> party = partyData.getParty(player.getUUID());

        if (party.isEmpty()) {
            player.sendMessage(new TextComponent("[VaultChatChannels] You are not in a party - please switch your chat channel.").withStyle(ChatFormatting.RED), player.getUUID());
            return;
        }

        List<UUID> memberUUIDs = party.get().getMembers();
        MinecraftServer server = player.getServer();
        if (server == null) return;
        PlayerList serverPlayerList = server.getPlayerList();
        List<ServerPlayer> members = memberUUIDs.stream()
                .map(serverPlayerList::getPlayer)
                .toList();

        MutableComponent partyTextComponent = new TextComponent("PARTY ")
                .withStyle(ChatFormatting.BOLD, ChatFormatting.DARK_PURPLE);
        sendComponentToPlayers(members, new TextComponent("").append(partyTextComponent).append(component), ChatChannel.party);
    }

    private static void sendVaultMessage(ServerPlayer player, Component component) {
        ServerLevel world = player.getLevel();
        Optional<Vault> optionalVault = ServerVaults.get(world);
        if (optionalVault.isEmpty()) {
            player.sendMessage(new TextComponent("[VaultChatChannels] You are not in a vault - please switch your chat channel.").withStyle(ChatFormatting.RED), player.getUUID());
            return;
        }

        List<ServerPlayer> players = world.players();

        MutableComponent vaultTextComponent = new TextComponent("VAULT ")
                .withStyle(ChatFormatting.BOLD, ChatFormatting.BLUE);
        sendComponentToPlayers(players, new TextComponent("").append(vaultTextComponent).append(component), ChatChannel.vault);
    }

    private static void sendComponentToPlayers(List<ServerPlayer> players, Component component, ChatChannel channel) {
        players.forEach(player -> {
            ChannelPlayerData playerData = ChannelPlayerData.get(player);
            ChatChannel playerChannel = playerData.getChatChannel();
            if (playerChannel != channel) {
                player.sendMessage(new TextComponent("").append(component).withStyle(ChatFormatting.DARK_GRAY), player.getUUID());
                return;
            }
            player.sendMessage(component, player.getUUID());
        });
    }
}