package it.samuconfaa.mobSpawner;

import it.samuconfaa.mobSpawner.MobSpawner;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerMoveListener implements Listener {

    private final MobSpawner plugin;
    private final Map<UUID, Long> lastCheckTime;
    private final long CHECK_COOLDOWN; // millisecondi

    public PlayerMoveListener(MobSpawner plugin) {
        this.plugin = plugin;
        this.lastCheckTime = new HashMap<>();
        this.CHECK_COOLDOWN = plugin.getConfigManager().getCheckInterval() * 50L; // converti ticks in ms
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        long currentTime = System.currentTimeMillis();

        // Verifica se è passato abbastanza tempo dall'ultimo controllo
        Long lastCheck = lastCheckTime.get(playerUUID);
        if (lastCheck != null && currentTime - lastCheck < CHECK_COOLDOWN) {
            return; // Troppo presto per un altro controllo
        }

        // Verifica se il giocatore si è effettivamente mosso (non solo rotazione)
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
                event.getFrom().getBlockY() == event.getTo().getBlockY() &&
                event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return; // Il giocatore non ha cambiato blocco
        }

        // Aggiorna il tempo dell'ultimo controllo
        lastCheckTime.put(playerUUID, currentTime);

        // Esegui il controllo in modo asincrono per non bloccare il thread principale
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                // Torna al thread principale per le operazioni sui mob
                Bukkit.getScheduler().runTask(plugin, () -> {
                    plugin.getSpawnerManager().activateMobsNearPlayer(player);
                });
            } catch (Exception e) {
                plugin.getLogger().warning("Errore nell'attivazione mob per " + player.getName() + ": " + e.getMessage());
            }
        });
    }
}