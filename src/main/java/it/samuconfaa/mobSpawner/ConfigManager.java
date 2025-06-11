package it.samuconfaa.mobSpawner;

import it.samuconfaa.mobSpawner.MobSpawner;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {

    private final MobSpawner plugin;
    private FileConfiguration config;

    public ConfigManager(MobSpawner plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    private void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        this.config = plugin.getConfig();

        // Imposta valori predefiniti se non esistono
        if (!config.contains("activation-distance")) {
            config.set("activation-distance", 10.0);
        }

        if (!config.contains("check-interval")) {
            config.set("check-interval", 20); // ticks (1 secondo)
        }

        if (!config.contains("messages.spawner-created")) {
            config.set("messages.spawner-created", "&aSpawner creato con successo! ID: &e{id}");
        }

        if (!config.contains("messages.spawner-exists")) {
            config.set("messages.spawner-exists", "&cUno spawner con questo ID esiste già!");
        }

        if (!config.contains("messages.invalid-mob")) {
            config.set("messages.invalid-mob", "&cMob MythicMobs non trovato: &e{mob}");
        }

        if (!config.contains("messages.invalid-number")) {
            config.set("messages.invalid-number", "&cNumero non valido: &e{number}");
        }

        if (!config.contains("messages.no-permission")) {
            config.set("messages.no-permission", "&cNon hai il permesso per utilizzare questo comando!");
        }

        if (!config.contains("messages.usage")) {
            config.set("messages.usage", "&cUso: /setmob <id> <mobName> <quantity>");
        }

        plugin.saveConfig();
    }

    public double getActivationDistance() {
        return config.getDouble("activation-distance", 10.0);
    }

    public int getCheckInterval() {
        return config.getInt("check-interval", 20);
    }

    public String getMessage(String path) {
        return config.getString("messages." + path, "&cMessaggio non trovato: " + path)
                .replace("&", "§");
    }

    public String getMessage(String path, String placeholder, String value) {
        return getMessage(path).replace("{" + placeholder + "}", value);
    }

    public void reloadConfig() {
        plugin.reloadConfig();
        loadConfig();
    }
}