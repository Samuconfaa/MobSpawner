package it.samuconfaa.mobSpawner;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.Bukkit;
import java.util.UUID;

public class SpawnerData {

    private String id;
    private String mobName;
    private Location location;
    private int mobCount;
    private int maxMobs;
    private boolean isActive;
    private UUID creatorUUID;

    public SpawnerData(String id, String mobName, Location location, int maxMobs, UUID creatorUUID) {
        this.id = id;
        this.mobName = mobName;
        this.location = location;
        this.maxMobs = maxMobs;
        this.mobCount = 0;
        this.isActive = true;
        this.creatorUUID = creatorUUID;
    }

    // Costruttore per caricamento da YAML
    public SpawnerData(String id, String mobName, String worldName, double x, double y, double z,
                       int mobCount, int maxMobs, boolean isActive, String creatorUUID) {
        this.id = id;
        this.mobName = mobName;
        this.mobCount = mobCount;
        this.maxMobs = maxMobs;
        this.isActive = isActive;
        this.creatorUUID = UUID.fromString(creatorUUID);

        World world = Bukkit.getWorld(worldName);
        if (world != null) {
            this.location = new Location(world, x, y, z);
        }
    }

    // Getters
    public String getId() { return id; }
    public String getMobName() { return mobName; }
    public Location getLocation() { return location; }
    public int getMobCount() { return mobCount; }
    public int getMaxMobs() { return maxMobs; }
    public boolean isActive() { return isActive; }
    public UUID getCreatorUUID() { return creatorUUID; }

    // Setters
    public void setMobCount(int mobCount) { this.mobCount = mobCount; }
    public void setActive(boolean active) { this.isActive = active; }

    // Metodi utili
    public void incrementMobCount() {
        this.mobCount++;
        if (this.mobCount >= this.maxMobs) {
            this.isActive = false;
        }
    }

    public boolean canSpawn() {
        return isActive && mobCount < maxMobs && location != null && location.getWorld() != null;
    }

    public int getRemainingMobs() {
        return maxMobs - mobCount;
    }

    // Metodi per serializzazione YAML
    public String getWorldName() {
        return location != null && location.getWorld() != null ? location.getWorld().getName() : "world";
    }

    public double getX() { return location != null ? location.getX() : 0; }
    public double getY() { return location != null ? location.getY() : 0; }
    public double getZ() { return location != null ? location.getZ() : 0; }

    @Override
    public String toString() {
        return String.format("SpawnerData{id='%s', mobName='%s', location=%s, mobCount=%d/%d, active=%s}",
                id, mobName, location, mobCount, maxMobs, isActive);
    }
}