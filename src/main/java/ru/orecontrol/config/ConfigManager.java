package ru.orecontrol.config;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import ru.orecontrol.OreControl;

import java.util.HashMap;
import java.util.Map;

public class ConfigManager {

    private final OreControl plugin;
    private final Map<String, Map<Material, Double>> worldOreMultipliers;
    private final Map<String, Map<Material, OreNFCConfig>> worldOreNFCConfigs;
    private boolean enabled;
    private boolean nfcGeneration;
    private boolean debugLogging;

    public ConfigManager(OreControl plugin) {
        this.plugin = plugin;
        this.worldOreMultipliers = new HashMap<>();
        this.worldOreNFCConfigs = new HashMap<>();
        this.enabled = true;
        this.nfcGeneration = false;
        loadConfig();
    }

    private void loadConfig() {
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();

        // Загружаем статус включения/выключения плагина
        enabled = config.getBoolean("enabled", true);

        // Читаем use-nfc-generation (boolean флаг для включения/выключения
        // NFC-генерации)
        // Используем отдельный ключ, чтобы избежать конфликта с секцией nfc-generation:
        nfcGeneration = config.getBoolean("use-nfc-generation", false);

        debugLogging = config.getBoolean("debug-logging", false);

        // Отладочный лог для проверки загрузки настроек
        plugin.getLogger().info("Загружено: enabled=" + enabled + ", use-nfc-generation=" + nfcGeneration
                + ", debug-logging=" + debugLogging);

        // Определяем секцию для загрузки настроек
        String configSection = nfcGeneration ? "nfc-generation" : "default-generation";
        String worldsPath = configSection + ".worlds";

        plugin.getLogger().info(
                "Попытка загрузить настройки из секции: " + worldsPath + " (nfcGeneration=" + nfcGeneration + ")");

        if (config.contains(worldsPath)) {
            ConfigurationSection worldsSection = config.getConfigurationSection(worldsPath);
            if (worldsSection != null) {
                for (String worldName : worldsSection.getKeys(false)) {
                    Map<Material, Double> multipliers = new HashMap<>();
                    Map<Material, OreNFCConfig> nfcConfigs = new HashMap<>();
                    String worldPath = worldsPath + "." + worldName;

                    // Загружаем все настройки руд для этого мира
                    if (config.contains(worldPath)) {
                        ConfigurationSection worldSection = config.getConfigurationSection(worldPath);
                        if (worldSection != null) {
                            for (String oreName : worldSection.getKeys(false)) {
                                // Пропускаем ключи, которые не являются названиями руд
                                // (например, комментарии или другие служебные ключи)
                                if (oreName.startsWith("#") || oreName.trim().isEmpty()) {
                                    continue;
                                }

                                // Пропускаем ключи, которые явно являются параметрами конфигурации
                                // (это параметры внутри секций руд, которые не должны быть на уровне мира)
                                if (oreName.equals("multiplier") || oreName.equals("generator-type") ||
                                        oreName.equals("base-chance") || oreName.equals("vein-size") ||
                                        oreName.equals("radius") || oreName.equals("density") ||
                                        oreName.equals("amount") || oreName.equals("veins")) {
                                    continue;
                                }

                                // Получаем значение для этого ключа
                                Object oreValue = worldSection.get(oreName);

                                // Пропускаем ключи, которые не являются секциями или числами
                                // (это могут быть параметры конфигурации, которые попали на неправильный
                                // уровень)
                                if (!(oreValue instanceof ConfigurationSection) && !(oreValue instanceof Number)) {
                                    // Пропускаем невалидные ключи (например, параметры конфигурации на неправильном
                                    // уровне)
                                    continue;
                                }

                                // Пытаемся преобразовать в Material - если не получается, пропускаем без
                                // предупреждения
                                // (это могут быть параметры конфигурации или другие служебные ключи)
                                Material material;
                                try {
                                    material = Material.valueOf(oreName.toUpperCase());
                                } catch (IllegalArgumentException e) {
                                    // Не является валидным Material - пропускаем (это не руда)
                                    continue;
                                }

                                try {

                                    if (nfcGeneration) {
                                        // Загружаем NFC конфигурацию
                                        if (oreValue instanceof ConfigurationSection) {
                                            // Расширенный формат
                                            ConfigurationSection oreSection = (ConfigurationSection) oreValue;
                                            double multiplier = oreSection.getDouble("multiplier", 1.0);
                                            String genTypeStr = oreSection.getString("generator-type", "minable")
                                                    .toUpperCase();
                                            OreNFCConfig.GeneratorType genType;
                                            try {
                                                genType = OreNFCConfig.GeneratorType.valueOf(genTypeStr);
                                            } catch (IllegalArgumentException e) {
                                                plugin.getLogger().warning("Неизвестный тип генератора для " + oreName
                                                        + ": " + genTypeStr + ", используется MINABLE");
                                                genType = OreNFCConfig.GeneratorType.MINABLE;
                                            }

                                            double baseChance = oreSection.getDouble("base-chance",
                                                    getDefaultBaseChance(material));
                                            int veinSize = oreSection.getInt("vein-size", getDefaultVeinSize(material));
                                            int radius = oreSection.getInt("radius", getDefaultRadius(material));
                                            int density = oreSection.getInt("density", getDefaultDensity(material));
                                            int amount = oreSection.getInt("amount", getDefaultAmount(material));
                                            int veins = oreSection.getInt("veins", getDefaultVeins(material));
                                            int attempts = oreSection.getInt("attempts", getDefaultAttempts(material));

                                            nfcConfigs.put(material, new OreNFCConfig(material, multiplier, genType,
                                                    baseChance, veinSize, radius, density, amount, veins, attempts));
                                            multipliers.put(material, multiplier);
                                        } else if (oreValue instanceof Number) {
                                            // Простой формат (только множитель)
                                            double multiplier = ((Number) oreValue).doubleValue();
                                            multipliers.put(material, multiplier);

                                            // Создаем NFC конфиг с дефолтными значениями
                                            OreNFCConfig.GeneratorType genType = getDefaultGeneratorType(material);
                                            double baseChance = getDefaultBaseChance(material);
                                            int veinSize = getDefaultVeinSize(material);
                                            int radius = getDefaultRadius(material);
                                            int density = getDefaultDensity(material);
                                            int amount = getDefaultAmount(material);
                                            int veins = getDefaultVeins(material);
                                            int attempts = getDefaultAttempts(material);

                                            nfcConfigs.put(material, new OreNFCConfig(material, multiplier, genType,
                                                    baseChance, veinSize, radius, density, amount, veins, attempts));
                                        }
                                    } else {
                                        // Старая логика (default-generation) - только множитель
                                        double multiplier = config.getDouble(worldPath + "." + oreName, 1.0);
                                        multipliers.put(material, multiplier);
                                    }
                                } catch (Exception e) {
                                    // Игнорируем ошибки при загрузке конфигурации для конкретной руды
                                    // (невалидные ключи уже обработаны выше)
                                    plugin.getLogger().fine(
                                            "Ошибка при загрузке конфигурации для " + oreName + ": " + e.getMessage());
                                }
                            }
                        }
                    }

                    worldOreMultipliers.put(worldName, multipliers);
                    if (nfcGeneration) {
                        worldOreNFCConfigs.put(worldName, nfcConfigs);
                    }
                }
            }
        }

        if (enabled) {
            String mode = nfcGeneration ? "NFC-generation" : "стандартная";
            int totalOres = 0;
            for (Map<Material, Double> multipliers : worldOreMultipliers.values()) {
                totalOres += multipliers.size();
            }
            plugin.getLogger().info("Загружено настроек для " + worldOreMultipliers.size() + " мир(ов), " + totalOres
                    + " тип(ов) руд (режим: " + mode + ")");
        } else {
            plugin.getLogger().info("Плагин отключен в конфигурации");
        }
    }

    private OreNFCConfig.GeneratorType getDefaultGeneratorType(Material material) {
        // По умолчанию используем типы генераторов на основе бета 1.7.3
        if (material == Material.COAL_ORE || material == Material.DEEPSLATE_COAL_ORE) {
            return OreNFCConfig.GeneratorType.CONCENTRATED;
        }
        if (material == Material.IRON_ORE || material == Material.DEEPSLATE_IRON_ORE) {
            return OreNFCConfig.GeneratorType.CLOUD;
        }
        if (material == Material.GOLD_ORE || material == Material.DEEPSLATE_GOLD_ORE) {
            return OreNFCConfig.GeneratorType.CLOUD;
        }
        if (material == Material.COPPER_ORE || material == Material.DEEPSLATE_COPPER_ORE) {
            return OreNFCConfig.GeneratorType.CLOUD;
        }
        // Для остальных руд используем MINABLE
        return OreNFCConfig.GeneratorType.MINABLE;
    }

    private double getDefaultBaseChance(Material material) {
        // Базовые шансы на основе точных значений из бета 1.7.3
        if (material == Material.DIAMOND_ORE || material == Material.DEEPSLATE_DIAMOND_ORE) {
            return 0.04; // rand.nextInt(25) == 1, шанс 1/25 = 0.04
        }
        if (material == Material.EMERALD_ORE || material == Material.DEEPSLATE_EMERALD_ORE) {
            return 0.142857; // rand.nextInt(7) == 1, шанс 1/7 ≈ 0.142857
        }
        if (material == Material.LAPIS_ORE || material == Material.DEEPSLATE_LAPIS_ORE) {
            return 0.5; // rand.nextInt(2) == 1, шанс 1/2 = 0.5
        }
        if (material == Material.GOLD_ORE || material == Material.DEEPSLATE_GOLD_ORE) {
            return 0.0625; // rand.nextInt(16) == 1, шанс 1/16 = 0.0625
        }
        if (material == Material.IRON_ORE || material == Material.DEEPSLATE_IRON_ORE) {
            return 0.071429; // rand.nextInt(42) <= 2, шанс 3/42 ≈ 0.071429
        }
        if (material == Material.COAL_ORE || material == Material.DEEPSLATE_COAL_ORE) {
            return 1.0; // Всегда генерируется (WorldGenConcentrated без условия)
        }
        if (material == Material.COPPER_ORE || material == Material.DEEPSLATE_COPPER_ORE) {
            return 1.0; // Генерируется всегда в цикле 3-6 раз (for (int k2 = 0; k2 < (3 +
                        // rand.nextInt(4)); k2++))
        }
        if (material == Material.REDSTONE_ORE || material == Material.DEEPSLATE_REDSTONE_ORE) {
            return 1.0; // Всегда генерируется 3 раза (for (int k2 = 0; k2 < 3; k2++))
        }
        // Для остальных руд используем более частую генерацию
        return 0.1; // 1/10
    }

    private int getDefaultVeinSize(Material material) {
        // Размеры жил на основе примеров из бета-версии
        if (material == Material.DIAMOND_ORE || material == Material.DEEPSLATE_DIAMOND_ORE) {
            return 8; // WorldGenMinable(Block.oreDiamond.blockID, 8)
        }
        if (material == Material.EMERALD_ORE || material == Material.DEEPSLATE_EMERALD_ORE) {
            return 1; // WorldGenMinable(..., 1)
        }
        if (material == Material.LAPIS_ORE || material == Material.DEEPSLATE_LAPIS_ORE) {
            return 6; // WorldGenMinable(Block.oreLapis.blockID, 6)
        }
        if (material == Material.REDSTONE_ORE || material == Material.DEEPSLATE_REDSTONE_ORE) {
            return 7; // WorldGenMinable(Block.oreRedstone.blockID, 7)
        }
        if (material == Material.IRON_ORE || material == Material.DEEPSLATE_IRON_ORE) {
            return 200; // WorldGenMinableCloud(..., 16, 2, 200) - это amount для cloud
        }
        if (material == Material.GOLD_ORE || material == Material.DEEPSLATE_GOLD_ORE) {
            return 32; // WorldGenMinableCloud(Block.oreGold.blockID, 10, 0, 32) - это amount для cloud
        }
        if (material == Material.COPPER_ORE || material == Material.DEEPSLATE_COPPER_ORE) {
            return 16; // WorldGenMinableCloud(..., 4, 1, 16) - это amount для cloud
        }
        if (material == Material.COAL_ORE || material == Material.DEEPSLATE_COAL_ORE) {
            return 16; // WorldGenConcentrated(Block.oreCoal.blockID, 5, 2, 16) - это amount для
                       // concentrated
        }
        if (material == Material.RAW_IRON_BLOCK || material == Material.RAW_COPPER_BLOCK) {
            return 5; // Примерный размер
        }
        if (material == Material.NETHER_QUARTZ_ORE) {
            return 14; // Примерный размер
        }
        if (material == Material.NETHER_GOLD_ORE) {
            return 10; // Примерный размер
        }
        if (material == Material.ANCIENT_DEBRIS) {
            return 2; // Очень маленький размер
        }
        return 12; // Дефолтный размер
    }

    private int getDefaultRadius(Material material) {
        // Радиусы на основе примеров из бета-версии
        if (material == Material.IRON_ORE || material == Material.DEEPSLATE_IRON_ORE) {
            return 16; // WorldGenMinableCloud(..., 16, 2, 200)
        }
        if (material == Material.GOLD_ORE || material == Material.DEEPSLATE_GOLD_ORE) {
            return 10; // WorldGenMinableCloud(Block.oreGold.blockID, 10, 0, 32)
        }
        if (material == Material.COPPER_ORE || material == Material.DEEPSLATE_COPPER_ORE) {
            return 4; // WorldGenMinableCloud(..., 4, 1, 16)
        }
        if (material == Material.COAL_ORE || material == Material.DEEPSLATE_COAL_ORE) {
            return 5; // WorldGenConcentrated(Block.oreCoal.blockID, 5, 2, 16)
        }
        return 10; // Дефолтный радиус
    }

    private int getDefaultDensity(Material material) {
        // Плотность на основе примеров из бета-версии
        if (material == Material.IRON_ORE || material == Material.DEEPSLATE_IRON_ORE) {
            return 2; // WorldGenMinableCloud(..., 16, 2, 200)
        }
        if (material == Material.GOLD_ORE || material == Material.DEEPSLATE_GOLD_ORE) {
            return 0; // WorldGenMinableCloud(Block.oreGold.blockID, 10, 0, 32)
        }
        if (material == Material.COPPER_ORE || material == Material.DEEPSLATE_COPPER_ORE) {
            return 1; // WorldGenMinableCloud(..., 4, 1, 16)
        }
        if (material == Material.COAL_ORE || material == Material.DEEPSLATE_COAL_ORE) {
            return 2; // WorldGenConcentrated(Block.oreCoal.blockID, 5, 2, 16)
        }
        return 2; // Дефолтная плотность
    }

    private int getDefaultAmount(Material material) {
        // Количество точек на основе примеров из бета-версии (для cloud)
        if (material == Material.IRON_ORE || material == Material.DEEPSLATE_IRON_ORE) {
            return 200; // WorldGenMinableCloud(..., 16, 2, 200)
        }
        if (material == Material.GOLD_ORE || material == Material.DEEPSLATE_GOLD_ORE) {
            return 32; // WorldGenMinableCloud(Block.oreGold.blockID, 10, 0, 32)
        }
        if (material == Material.COPPER_ORE || material == Material.DEEPSLATE_COPPER_ORE) {
            return 16; // WorldGenMinableCloud(..., 4, 1, 16)
        }
        return 80; // Дефолтное количество
    }

    private int getDefaultVeins(Material material) {
        // Количество жил на основе примеров из бета-версии (для concentrated)
        if (material == Material.COAL_ORE || material == Material.DEEPSLATE_COAL_ORE) {
            return 2; // WorldGenConcentrated(Block.oreCoal.blockID, 5, 2, 16)
        }
        return 3; // Дефолтное количество жил
    }

    private int getDefaultAttempts(Material material) {
        // Количество попыток генерации на основе оригинального кода Beta 1.7.3
        // В оригинале некоторые руды генерировались в циклах:
        if (material == Material.REDSTONE_ORE || material == Material.DEEPSLATE_REDSTONE_ORE) {
            return 3; // for (int k2 = 0; k2 < 3; k2++)
        }
        if (material == Material.COPPER_ORE || material == Material.DEEPSLATE_COPPER_ORE) {
            return 4; // for (int k2 = 0; k2 < (3 + rand.nextInt(4)); k2++) - среднее значение 4-5
        }
        if (material == Material.COAL_ORE || material == Material.DEEPSLATE_COAL_ORE) {
            return 2; // for (int k2 = 0; k2 < (1 + rand.nextInt(2)); k2++) - среднее значение 1-2
        }
        // Для остальных руд генерируем один раз
        return 1;
    }

    public double getOreMultiplier(World world, Material material) {
        String worldName = world.getName();
        Map<Material, Double> multipliers = worldOreMultipliers.get(worldName);

        if (multipliers == null) {
            return 1.0; // По умолчанию нормальная частота
        }

        return multipliers.getOrDefault(material, 1.0);
    }

    public boolean hasWorldConfig(String worldName) {
        return worldOreMultipliers.containsKey(worldName);
    }

    public boolean hasOreConfig(World world, Material material) {
        String worldName = world.getName();
        Map<Material, Double> multipliers = worldOreMultipliers.get(worldName);
        if (multipliers == null) {
            return false;
        }
        return multipliers.containsKey(material);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isNFCGeneration() {
        return nfcGeneration;
    }

    public boolean isDebugLogging() {
        return debugLogging;
    }

    public OreNFCConfig getOreNFCConfig(World world, Material material) {
        if (!nfcGeneration) {
            return null;
        }
        String worldName = world.getName();
        Map<Material, OreNFCConfig> nfcConfigs = worldOreNFCConfigs.get(worldName);
        if (nfcConfigs == null) {
            return null;
        }
        return nfcConfigs.get(material);
    }

    public void reload() {
        worldOreMultipliers.clear();
        worldOreNFCConfigs.clear();
        loadConfig();
    }

    public OreControl getPlugin() {
        return plugin;
    }
}
