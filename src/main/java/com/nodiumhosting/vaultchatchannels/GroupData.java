package com.nodiumhosting.vaultchatchannels;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GroupData {
    public static List<Group> groups = new ArrayList<>();

    public static Group getGroup(UUID player) {
        return groups.stream()
                .filter(grp -> grp.getPlayers().contains(player))
                .findFirst()
                .orElse(null);
    }

    public static Group getOrCreateGroup(UUID player) {
        Group group = getGroup(player);

        if (group == null) {
            group = new Group();
            group.add(player);
            groups.add(group);
        }

        return group;
    }
}