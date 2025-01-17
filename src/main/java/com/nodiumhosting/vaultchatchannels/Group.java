package com.nodiumhosting.vaultchatchannels;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Group {
        private List<UUID> players = new ArrayList<>();
        private List<UUID> invites = new ArrayList<>();

        public Group() {}

        public List<UUID> getPlayers() {
            return players;
        }

        public void add(UUID player) {
            players.add(player);
        }

        public void remove(UUID player) {
            players.remove(player);
        }

        public int size() {
            return players.size();
        }

        public boolean invite(UUID player) {
            if (players.contains(player) || invites.contains(player)) {
                return false;
            }
            invites.add(player);
            return true;
        }

        public void removeInvite(UUID player) {
            invites.remove(player);
        }

        public boolean isInvited(UUID player) {
            return invites.contains(player);
        }
    }
