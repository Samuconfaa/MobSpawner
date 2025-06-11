package it.samuconfaa.mobSpawner;

import it.samuconfaa.mobSpawner.MobSpawner;
import io.lumine.mythic.bukkit.MythicBukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SetMobCommand implements CommandExecutor, TabCompleter {

    private final MobSpawner plugin;

    public SetMobCommand(MobSpawner plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Verifica che sia un giocatore
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cQuesto comando può essere usato solo dai giocatori!");
            return true;
        }

        Player player = (Player) sender;

        // Verifica permessi
        if (!player.hasPermission("mobspawner.setmob")) {
            player.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
            return true;
        }

        // Verifica argomenti
        if (args.length != 3) {
            player.sendMessage(plugin.getConfigManager().getMessage("usage"));
            return true;
        }

        String spawnerId = args[0];
        String mobName = args[1];
        String quantityStr = args[2];

        // Verifica che la quantità sia un numero valido
        int quantity;
        try {
            quantity = Integer.parseInt(quantityStr);
            if (quantity <= 0) {
                throw new NumberFormatException("Numero deve essere positivo");
            }
        } catch (NumberFormatException e) {
            player.sendMessage(plugin.getConfigManager().getMessage("invalid-number", "number", quantityStr));
            return true;
        }

        // Verifica che il mob MythicMobs esista
        if (!MythicBukkit.inst().getMobManager().getMythicMob(mobName).isPresent()) {
            player.sendMessage(plugin.getConfigManager().getMessage("invalid-mob", "mob", mobName));
            return true;
        }

        // Tenta di creare lo spawner
        boolean success = plugin.getSpawnerManager().createSpawner(
                spawnerId,
                mobName,
                player.getLocation(),
                quantity,
                player.getUniqueId()
        );

        if (success) {
            player.sendMessage(plugin.getConfigManager().getMessage("spawner-created", "id", spawnerId));
            plugin.getLogger().info("Spawner creato da " + player.getName() + ": " +
                    spawnerId + " (" + mobName + " x" + quantity + ")");
        } else {
            player.sendMessage(plugin.getConfigManager().getMessage("spawner-exists"));
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // Suggerimenti per ID spawner (potrebbero essere esistenti o nuovi)
            completions.add("spawner_" + System.currentTimeMillis() % 1000);
            completions.add("mob_spawner_1");
            completions.add("custom_spawner");
        } else if (args.length == 2) {
            // Suggerimenti per nomi mob MythicMobs
            try {
                completions = MythicBukkit.inst().getMobManager().getMobNames()
                        .stream()
                        .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            } catch (Exception e) {
                // Fallback se non riesce a ottenere i nomi dei mob
                completions.add("SkeletonKing");
                completions.add("ZombieMinion");
                completions.add("CustomBoss");
            }
        } else if (args.length == 3) {
            // Suggerimenti per quantità
            completions.add("1");
            completions.add("5");
            completions.add("10");
            completions.add("20");
            completions.add("50");
        }

        return completions;
    }
}