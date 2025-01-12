package com.nodiumhosting.vaultchatchannels;

import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ChannelPlayerData {
    private static Map<UUID, ChannelPlayerData> playerDataMap = new HashMap<>();

    public static ChannelPlayerData get(Player player) {
        return get(player.getUUID());
    }

    public static ChannelPlayerData get(UUID playerUUID) {
        if (!playerDataMap.containsKey(playerUUID)) {
            playerDataMap.put(playerUUID, new ChannelPlayerData());
        }
        return playerDataMap.get(playerUUID);
    }

    private ChatChannel chatChannel = ChatChannel.global;
    private Map<ChatChannel, Character> prefixes = Map.of(
            ChatChannel.global, '!',
            ChatChannel.party, ':',
            ChatChannel.vault, '#',
            ChatChannel.group, '@'
    );

    public ChannelPlayerData() {}

    public ChatChannel getChatChannel() {
        return chatChannel;
    }

    public void setChatChannel(ChatChannel chatChannel) {
        this.chatChannel = chatChannel;
    }

    public Character getPrefix(ChatChannel chatChannel) {
        return prefixes.get(chatChannel);
    }

    public void setPrefix(ChatChannel chatChannel, Character prefix) {
        prefixes.put(chatChannel, prefix);
    }

    public List<Character> getPrefixes() {
        return prefixes.values().stream().toList();
    }

    public ChatChannel getChannelByPrefix(Character prefix) {
        return prefixes.entrySet().stream()
                .filter(entry -> entry.getValue().equals(prefix))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }
}
