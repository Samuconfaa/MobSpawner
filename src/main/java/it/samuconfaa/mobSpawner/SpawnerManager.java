package it.samuconfaa.mobSpawner;

import it.samuconfaa.mobSpawner.MobSpawner;
import it.samuconfaa.mobSpawner.SpawnerData;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SpawnerManager {

    private final MobSpawner plugin;
    private final Map<String, SpawnerData> spawners;
    private final File spawnersFile;
    private FileConfiguration spawnersConfig;

    public SpawnerManager(MobSpawner plugin) {
        this.plugin = plugin;
        this.spawners = new ConcurrentHashMap<>();
        this.spawnersFile = new File(plugin.getDataFolder(), "spawners.yml");

        // Crea la cartella se non esiste
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        // Crea il file se non esiste
        if (!spawnersFile.exists()) {
            try {
                spawnersFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Impossibile creare spawners.yml: " + e.getMessage());
            }
        }

        this.spawnersConfig = YamlConfiguration.loadConfiguration(spawnersFile);
    }

    /**
     * API Method - Crea un nuovo spawner
     */
    public boolean createSpawner(String id, String mobName, Location location, int maxMobs, UUID creatorUUID) {
        if (spawners.containsKey(id)) {
            return false; // Spawner già esistente
        }

        // Verifica che il mob MythicMobs esista
        if (!MythicBukkit.inst().getMobManager().getMythicMob(mobName).isPresent()) {
            return false; // Mob non trovato
        }

        SpawnerData spawner = new SpawnerData(id, mobName, location, maxMobs, creatorUUID);
        spawners.put(id, spawner);

        // AGGIUNTO: Salva immediatamente nel file YAML
        saveSpawners();

        plugin.getLogger().info("Spawner creato e salvato: " + spawner.toString());
        return true;
    }

    /**
     * API Method - Spawna i mob di uno spawner specifico
     */
    public boolean spawnMobs(String spawnerId) {
        SpawnerData spawner = spawners.get(spawnerId);
        if (spawner == null || !spawner.canSpawn()) {
            return false;
        }

        try {
            // Spawna un mob usando MythicMobs
            ActiveMob mob = MythicBukkit.inst().getMobManager().spawnMob(
                    spawner.getMobName(),
                    spawner.getLocation()
            );

            if (mob != null) {
                Entity bukkitEntity = mob.getEntity().getBukkitEntity();

                // Imposta il mob come immobile fino all'attivazione
                if (bukkitEntity instanceof Mob) {
                    ((Mob) bukkitEntity).setAware(false);
                } else if (bukkitEntity instanceof LivingEntity) {
                    ((LivingEntity) bukkitEntity).setAI(false);
                }

                spawner.incrementMobCount();
                plugin.getLogger().info("Mob spawnato: " + spawner.getMobName() + " (" +
                        spawner.getMobCount() + "/" + spawner.getMaxMobs() + ")");

                // Se ha raggiunto il limite, disattiva lo spawner
                if (!spawner.isActive()) {
                    plugin.getLogger().info("Spawner " + spawnerId + " ha raggiunto il limite e è stato disattivato");
                }

                return true;
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Errore nello spawn del mob " + spawner.getMobName() + ": " + e.getMessage());
        }

        return false;
    }

    /**
     * API Method - Attiva i mob vicini a un giocatore
     */
    public void activateMobsNearPlayer(Player player) {
        double activationDistance = plugin.getConfigManager().getActivationDistance();
        Location playerLoc = player.getLocation();

        // Trova i mob MythicMobs nel raggio di attivazione
        MythicBukkit.inst().getMobManager().getActiveMobs().forEach(mob -> {
            Entity bukkitEntity = mob.getEntity().getBukkitEntity();

            if (bukkitEntity.getLocation().getWorld().equals(playerLoc.getWorld())) {
                double distance = bukkitEntity.getLocation().distance(playerLoc);

                if (distance <= activationDistance) {
                    // Verifica se il mob è inattivo e attivalo
                    boolean shouldActivate = false;

                    if (bukkitEntity instanceof Mob) {
                        Mob mobEntity = (Mob) bukkitEntity;
                        if (!mobEntity.isAware()) {
                            mobEntity.setAware(true);
                            shouldActivate = true;
                        }
                    } else if (bukkitEntity instanceof LivingEntity) {
                        LivingEntity livingEntity = (LivingEntity) bukkitEntity;
                        if (!livingEntity.hasAI()) {
                            livingEntity.setAI(true);
                            shouldActivate = true;
                        }
                    }

                    if (shouldActivate) {
                        plugin.getLogger().info("Mob attivato vicino a " + player.getName());
                    }
                }
            }
        });
    }

    /**
     * API Method - Rimuove uno spawner
     */
    public boolean removeSpawner(String id) {
        SpawnerData removed = spawners.remove(id);
        if (removed != null) {
            // Salva immediatamente dopo la rimozione
            saveSpawners();
            plugin.getLogger().info("Spawner rimosso e salvato: " + id);
            return true;
        }
        return false;
    }

    /**
     * API Method - Ottiene tutti gli spawner
     */
    public Map<String, SpawnerData> getAllSpawners() {
        return new HashMap<>(spawners);
    }

    /**
     * API Method - Ottiene uno spawner specifico
     */
    public SpawnerData getSpawner(String id) {
        return spawners.get(id);
    }

    /**
     * API Method - Ottiene gli spawner attivi
     */
    public List<SpawnerData> getActiveSpawners() {
        return spawners.values().stream()
                .filter(SpawnerData::isActive)
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    /**
     * Salva gli spawner nel file YAML
     */
    public void saveSpawners() {
        try {
            // Pulisce la configurazione
            for (String key : spawnersConfig.getKeys(false)) {
                spawnersConfig.set(key, null);
            }

            // Salva ogni spawner
            for (SpawnerData spawner : spawners.values()) {
                String path = "spawners." + spawner.getId();
                spawnersConfig.set(path + ".mobName", spawner.getMobName());
                spawnersConfig.set(path + ".world", spawner.getWorldName());
                spawnersConfig.set(path + ".x", spawner.getX());
                spawnersConfig.set(path + ".y", spawner.getY());
                spawnersConfig.set(path + ".z", spawner.getZ());
                spawnersConfig.set(path + ".mobCount", spawner.getMobCount());
                spawnersConfig.set(path + ".maxMobs", spawner.getMaxMobs());
                spawnersConfig.set(path + ".isActive", spawner.isActive());
                spawnersConfig.set(path + ".creator", spawner.getCreatorUUID().toString());
            }

            spawnersConfig.save(spawnersFile);
            plugin.getLogger().info("Spawners salvati: " + spawners.size());

        } catch (IOException e) {
            plugin.getLogger().severe("Errore nel salvare gli spawners: " + e.getMessage());
        }
    }

    /**
     * Carica gli spawner dal file YAML
     */
    public void loadSpawners() {
        spawners.clear();

        ConfigurationSection spawnersSection = spawnersConfig.getConfigurationSection("spawners");
        if (spawnersSection == null) {
            plugin.getLogger().info("Nessun spawner da caricare");
            return;
        }

        for (String id : spawnersSection.getKeys(false)) {
            try {
                ConfigurationSection spawnerSection = spawnersSection.getConfigurationSection(id);
                if (spawnerSection != null) {
                    SpawnerData spawner = new SpawnerData(
                            id,
                            spawnerSection.getString("mobName"),
                            spawnerSection.getString("world"),
                            spawnerSection.getDouble("x"),
                            spawnerSection.getDouble("y"),
                            spawnerSection.getDouble("z"),
                            spawnerSection.getInt("mobCount"),
                            spawnerSection.getInt("maxMobs"),
                            spawnerSection.getBoolean("isActive"),
                            spawnerSection.getString("creator")
                    );

                    if (spawner.getLocation() != null) {
                        spawners.put(id, spawner);
                    } else {
                        plugin.getLogger().warning("Spawner " + id + " ha un mondo invalido, saltato");
                    }
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Errore nel caricare lo spawner " + id + ": " + e.getMessage());
            }
        }

        plugin.getLogger().info("Spawners caricati: " + spawners.size());
    }
}