
package com.lifesteal;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public class LifestealPlugin extends JavaPlugin implements Listener, TabExecutor {

    private FileConfiguration config;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.config = getConfig();
        getServer().getPluginManager().registerEvents(this, this);
        getCommand("revive").setExecutor(this);
        getCommand("hearts").setExecutor(this);
        getLogger().info("Lifesteal plugin has been enabled.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("hearts")) {
            if (sender instanceof Player player) {
                double hearts = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue() / 2;
                player.sendMessage(ChatColor.GREEN + "You have " + hearts + " hearts.");
                return true;
            }
        }

        if (command.getName().equalsIgnoreCase("revive")) {
            if (args.length == 1 && sender.hasPermission("lifesteal.revive")) {
                Player target = Bukkit.getPlayer(args[0]);
                if (target != null && target.getGameMode() == GameMode.SPECTATOR) {
                    target.setGameMode(GameMode.SURVIVAL);
                    target.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(config.getDouble("min-hearts"));
                    target.setHealth(target.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue());
                    Bukkit.broadcastMessage(ChatColor.AQUA + target.getName() + " has been revived!");
                    return true;
                } else {
                    sender.sendMessage(ChatColor.RED + "Player not found or not in spectator mode.");
                }
            } else {
                sender.sendMessage(ChatColor.RED + "Usage: /revive <player>");
            }
        }
        return false;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        double heartGain = config.getDouble("heart-gain");
        double heartLoss = config.getDouble("heart-loss");
        double maxHearts = config.getDouble("max-hearts");
        double minHearts = config.getDouble("min-hearts");

        if (killer != null && killer != victim) {
            double killerHealth = killer.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue();
            if (killerHealth < maxHearts) {
                killer.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(Math.min(killerHealth + heartGain, maxHearts));
                killer.sendMessage(ChatColor.GREEN + "You stole " + (heartGain / 2) + " hearts!");
            }
        }

        double victimHealth = victim.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue();
        if (victimHealth > minHearts) {
            victim.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(Math.max(victimHealth - heartLoss, minHearts));
            victim.sendMessage(ChatColor.RED + "You lost " + (heartLoss / 2) + " hearts!");
        } else {
            victim.setGameMode(GameMode.SPECTATOR);
            victim.sendMessage(ChatColor.DARK_RED + "You're out of hearts and now in spectator mode.");
        }
    }
}
