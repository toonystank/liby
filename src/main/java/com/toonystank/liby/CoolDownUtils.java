package com.toonystank.liby;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class CoolDownUtils {

    private static final Map<String, Map<Player, Long>> cooldowns = new HashMap<>();

    /**
     * Check if a player has a cooldown for a specific command.
     *
     * @param player     The player to check.
     * @param command    The command name.
     * @param cooldownSeconds The cooldown duration in seconds.
     * @return True if the player is still on cooldown, false otherwise.
     */
    public static boolean hasCooldown(Player player, String command, int cooldownSeconds) {
        long coolDownMillis = cooldownSeconds * 1000L;
        if (!cooldowns.containsKey(command)) {
            return false;
        }

        Map<Player, Long> playerCooldowns = cooldowns.get(command);
        if (playerCooldowns.containsKey(player)) {
            long lastTime = playerCooldowns.get(player);
            long currentTime = System.currentTimeMillis();
            long elapsedTime = (currentTime - lastTime) / 1000; // Convert to seconds

            return elapsedTime < coolDownMillis;
        }
        return false;
    }

    /**
     * Set a cooldown for a player on a specific command.
     *
     * @param player     The player to set the cooldown for.
     * @param command    The command name.
     * @param cooldownSeconds The cooldown duration in seconds.
     */
    public static void setCoolDown(Player player, String command, int cooldownSeconds) {
        long coolDownMillis = cooldownSeconds * 1000L;
        cooldowns.computeIfAbsent(command, k -> new HashMap<>()).put(player, coolDownMillis);
    }

    /**
     * Get the remaining cooldown time for a player on a specific command.
     *
     * @param player     The player to get the cooldown for.
     * @param command    The command name.
     * @param cooldownSeconds The cooldown duration in seconds.
     * @return The remaining cooldown time in seconds, or 0 if the player is not on cooldown.
     */
    public static long getRemainingCooldown(Player player, String command, int cooldownSeconds) {
        long coolDownMillis = cooldownSeconds * 1000L;
        if (!cooldowns.containsKey(command)) {
            return 0;
        }

        Map<Player, Long> playerCooldowns = cooldowns.get(command);
        if (playerCooldowns.containsKey(player)) {
            long lastTime = playerCooldowns.get(player);
            long currentTime = System.currentTimeMillis();
            long elapsedTime = (currentTime - lastTime) / 1000; // Convert to seconds

            long remainingTime = coolDownMillis - elapsedTime;
            if (remainingTime <= 0) {
                playerCooldowns.remove(player); // Remove the player from cooldown if time is up
                return 0;
            } else {
                return remainingTime;
            }
        }
        return 0;
    }
}