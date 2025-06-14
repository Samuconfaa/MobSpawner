package it.samuconfaa.mobSpawner;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DeleteSpawnerCommand implements CommandExecutor, TabCompleter {

    private final MobSpawner plugin;

    public DeleteSpawnerCommand(MobSpawner plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Verifica permessi
        if (!sender.hasPermission("mobspawner.delete")) {
            sender.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
            return true;
        }

        // Verifica argomenti
        if (args.length != 1) {
            sender.sendMessage(plugin.getConfigManager().getMessage("delete-usage"));
            return true;
        }

        String spawnerId = args[0];

        // Verifica che lo spawner esista
        SpawnerData spawner = plugin.getSpawnerManager().getSpawner(spawnerId);
        if (spawner == null) {
            sender.sendMessage(plugin.getConfigManager().getMessage("spawner-not-found", "id", spawnerId));
            return true;
        }

        // Se è un giocatore, verifica che sia il creatore o abbia permessi admin
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (!spawner.getCreatorUUID().equals(player.getUniqueId()) &&
                    !player.hasPermission("mobspawner.admin")) {
                sender.sendMessage(plugin.getConfigManager().getMessage("not-spawner-owner"));
                return true;
            }
        }

        // Elimina lo spawner
        boolean success = plugin.getSpawnerManager().removeSpawner(spawnerId);

        if (success) {
            sender.sendMessage(plugin.getConfigManager().getMessage("spawner-deleted", "id", spawnerId));
            plugin.getLogger().info("Spawner eliminato da " + sender.getName() + ": " + spawnerId);
        } else {
            sender.sendMessage(plugin.getConfigManager().getMessage("delete-failed", "id", spawnerId));
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // Mostra tutti gli spawner se è admin, altrimenti solo quelli del giocatore
            if (sender instanceof Player) {
                Player player = (Player) sender;

                if (player.hasPermission("mobspawner.admin")) {
                    // Admin può vedere tutti gli spawner
                    completions = plugin.getSpawnerManager().getAllSpawners().keySet()
                            .stream()
                            .filter(id -> id.toLowerCase().startsWith(args[0].toLowerCase()))
                            .collect(Collectors.toList());
                } else {
                    // Giocatore normale vede solo i suoi spawner
                    completions = plugin.getSpawnerManager().getAllSpawners().entrySet()
                            .stream()
                            .filter(entry -> entry.getValue().getCreatorUUID().equals(player.getUniqueId()))
                            .map(entry -> entry.getKey())
                            .filter(id -> id.toLowerCase().startsWith(args[0].toLowerCase()))
                            .collect(Collectors.toList());
                }
            } else {
                // Console può vedere tutti gli spawner
                completions = plugin.getSpawnerManager().getAllSpawners().keySet()
                        .stream()
                        .filter(id -> id.toLowerCase().startsWith(args[0].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }

        return completions;
    }
}