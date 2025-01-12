package com.nodiumhosting.vaultchatchannels;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GroupData {
    public static List<Group> groups = new ArrayList<>();

    public static Group getGroup(UUID player) {
        return groups.stream()
                .filter(group -> group.getPlayers().contains(player))
                .findFirst()
                .orElse(null);
    }
}