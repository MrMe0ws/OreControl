package ru.orecontrol.config;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import ru.orecontrol.OreControl;

import java.util.HashMap;
import java.util.Map;

public class ConfigManager {

    private final OreControl plugin;
    private final Map<String, Map<Material, Double>> worldOreMultipliers;
    private boolean enabled;

    public ConfigManager(OreControl plugin) {
        this.plugin = plugin;
        this.worldOreMultipliers = new HashMap<>();
        this.enabled = true;
        loadConfig();
    }

    private void loadConfig() {
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();

        // Загружаем статус включения/выключения плагина
        enabled = config.getBoolean("enabled", true);

        if (config.contains("worlds")) {
            for (String worldName : config.getConfigurationSection("worlds").getKeys(false)) {
                Map<Material, Double> multipliers = new HashMap<>();
                String worldPath = "worlds." + worldName;

                // Загружаем все настройки руд для этого мира
                if (config.contains(worldPath)) {
                    for (String oreName : config.getConfigurationSection(worldPath).getKeys(false)) {
                        try {
                            Material material = Material.valueOf(oreName.toUpperCase());
                            double multiplier = config.getDouble(worldPath + "." + oreName, 1.0);
                            multipliers.put(material, multiplier);
                        } catch (IllegalArgumentException e) {
                            plugin.getLogger().warning("Неизвестный тип руды: " + oreName);
                        }
                    }
                }

                worldOreMultipliers.put(worldName, multipliers);
            }
        }

        if (enabled) {
            plugin.getLogger().info("Загружено настроек для " + worldOreMultipliers.size() + " миров");
        } else {
            plugin.getLogger().info("Плагин отключен в конфигурации");
        }
    }

    public double getOreMultiplier(World world, Material material) {
        String worldName = world.getName();
        Map<Material, Double> multipliers = worldOreMultipliers.get(worldName);

        if (multipliers == null) {
            return 1.0; // По умолчанию нормальная частота
        }

        return multipliers.getOrDefault(material, 1.0);
    }

    public void reload() {
        worldOreMultipliers.clear();
        loadConfig();
    }

    public boolean hasWorldConfig(String worldName) {
        return worldOreMultipliers.containsKey(worldName);
    }

    public boolean isEnabled() {
        return enabled;
    }
}
