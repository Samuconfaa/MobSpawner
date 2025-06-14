package it.samuconfaa.mobSpawner;

import it.samuconfaa.mobSpawner.ConfigManager;
import it.samuconfaa.mobSpawner.SpawnerManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import it.samuconfaa.mobSpawner.SetMobCommand;
import it.samuconfaa.mobSpawner.PlayerMoveListener;
import it.samuconfaa.mobSpawner.SpawnerManager;
import it.samuconfaa.mobSpawner.ConfigManager;

public class MobSpawner extends JavaPlugin {

    private static MobSpawner instance;
    private SpawnerManager spawnerManager;
    private ConfigManager configManager;

    @Override
    public void onEnable() {
        instance = this;

        // Verifica dipendenza MythicMobs
        if (!Bukkit.getPluginManager().isPluginEnabled("MythicMobs")) {
            getLogger().severe("MythicMobs non trovato! Plugin disabilitato.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // Inizializza managers
        configManager = new ConfigManager(this);
        spawnerManager = new SpawnerManager(this);

        // Registra comandi
        getCommand("setmob").setExecutor(new SetMobCommand(this));
        getCommand("deletespawner").setExecutor(new DeleteSpawnerCommand(this)); // NUOVO COMANDO

        // Registra listeners
        Bukkit.getPluginManager().registerEvents(new PlayerMoveListener(this), this);

        // Carica spawners dal file
        spawnerManager.loadSpawners();

        getLogger().info("MobSpawner Plugin abilitato!");
    }

    @Override
    public void onDisable() {
        if (spawnerManager != null) {
            spawnerManager.saveSpawners();
        }
        getLogger().info("MobSpawner Plugin disabilitato!");
    }

    public static MobSpawner getInstance() {
        return instance;
    }

    public SpawnerManager getSpawnerManager() {
        return spawnerManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }
}