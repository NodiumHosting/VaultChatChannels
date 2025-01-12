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
    private final static Map<ChatChannel, Character> prefixes = Map.of(
            ChatChannel.global, '!',
            ChatChannel.party, ':',
            ChatChannel.vault, '#',
            ChatChannel.group, '@',
            ChatChannel.voice, '*'
    );

    public ChannelPlayerData() {}

    public ChatChannel getChatChannel() {
        return chatChannel;
    }

    public void setChatChannel(ChatChannel chatChannel) {
        this.chatChannel = chatChannel;
    }

    public static List<Character> getPrefixes() {
        return prefixes.values().stream().toList();
    }

    public static ChatChannel getChannelByPrefix(Character prefix) {
        return prefixes.entrySet().stream()
                .filter(entry -> entry.getValue().equals(prefix))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }
}
