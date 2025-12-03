package ru.orecontrol.generator;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.generator.BlockPopulator;
import ru.orecontrol.config.ConfigManager;
import ru.orecontrol.config.OreNFCConfig;
import ru.orecontrol.generator.nfc.NFCWorldGenConcentrated;
import ru.orecontrol.generator.nfc.NFCWorldGenMinable;
import ru.orecontrol.generator.nfc.NFCWorldGenMinableCloud;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class CustomOrePopulator extends BlockPopulator {

    private final ConfigManager configManager;

    // Стандартные значения для генерации руды (примерные, основанные на ванильной
    // генерации)
    private static final int COAL_VEINS_PER_CHUNK = 20;
    private static final int COPPER_VEINS_PER_CHUNK = 16;
    private static final int IRON_VEINS_PER_CHUNK = 20;
    private static final int GOLD_VEINS_PER_CHUNK = 2;
    private static final int DIAMOND_VEINS_PER_CHUNK = 1;
    private static final int REDSTONE_VEINS_PER_CHUNK = 8;
    private static final int LAPIS_VEINS_PER_CHUNK = 1;
    private static final int EMERALD_VEINS_PER_CHUNK = 1;
    private static final int RAW_IRON_BLOCK_VEINS_PER_CHUNK = 2;
    private static final int RAW_COPPER_BLOCK_VEINS_PER_CHUNK = 2;
    private static final int NETHER_QUARTZ_VEINS_PER_CHUNK = 16;
    private static final int NETHER_GOLD_VEINS_PER_CHUNK = 10;
    private static final int ANCIENT_DEBRIS_VEINS_PER_CHUNK = 1;

    public CustomOrePopulator(ConfigManager configManager) {
        this.configManager = configManager;
    }

    @Override
    public void populate(World world, Random random, Chunk chunk) {
        // Статистика для логирования (если включено)
        boolean debugEnabled = configManager.isDebugLogging();
        Map<Material, Integer> oreStats = debugEnabled ? new HashMap<>() : null;

        // Отладочный лог для проверки вызова populate (только для отладки)
        // if (debugEnabled) {
        // configManager.getPlugin().getLogger().info("DEBUG: Populate вызван для чанка
        // [" + chunk.getX() + ", " + chunk.getZ() + "] в мире '" + world.getName() +
        // "'");
        // }

        // Проверяем, используется ли NFC-generation
        if (configManager.isNFCGeneration()) {
            // Используем NFC-генерацию
            if (world.getEnvironment() == World.Environment.NORMAL) {
                generateNFCOverworldOres(world, random, chunk, oreStats);
            } else if (world.getEnvironment() == World.Environment.NETHER) {
                generateNFCNetherOres(world, random, chunk, oreStats);
            }
        } else {
            // Используем старую логику
            if (world.getEnvironment() == World.Environment.NORMAL) {
                generateOverworldOres(world, random, chunk, oreStats);
            } else if (world.getEnvironment() == World.Environment.NETHER) {
                generateNetherOres(world, random, chunk, oreStats);
            }
        }

        // Выводим статистику, если включено логирование
        if (configManager.isDebugLogging()) {
            if (oreStats == null) {
                configManager.getPlugin().getLogger()
                        .warning("DEBUG: oreStats is null для чанка [" + chunk.getX() + ", " + chunk.getZ() + "]");
            } else if (oreStats.isEmpty()) {
                // Не выводим лог для пустых чанков - это нормально (руда генерируется не в
                // каждом чанке)
            } else {
                int chunkX = chunk.getX();
                int chunkZ = chunk.getZ();
                StringBuilder log = new StringBuilder(
                        "Чанк [" + chunkX + ", " + chunkZ + "] в мире '" + world.getName() + "': ");
                boolean first = true;
                for (Map.Entry<Material, Integer> entry : oreStats.entrySet()) {
                    if (!first)
                        log.append(", ");
                    log.append(entry.getKey().name()).append("=").append(entry.getValue());
                    first = false;
                }
                configManager.getPlugin().getLogger().info(log.toString());
            }
        }
    }

    private void generateOverworldOres(World world, Random random, Chunk chunk, Map<Material, Integer> oreStats) {
        // Угольная руда
        int count = generateOre(world, random, chunk,
                Material.COAL_ORE, Material.DEEPSLATE_COAL_ORE,
                COAL_VEINS_PER_CHUNK, 0, 192, 17);
        if (oreStats != null) {
            oreStats.put(Material.COAL_ORE, oreStats.getOrDefault(Material.COAL_ORE, 0) + count);
        }

        // Медная руда
        count = generateOre(world, random, chunk,
                Material.COPPER_ORE, Material.DEEPSLATE_COPPER_ORE,
                COPPER_VEINS_PER_CHUNK, -16, 112, 10);
        if (oreStats != null) {
            oreStats.put(Material.COPPER_ORE, oreStats.getOrDefault(Material.COPPER_ORE, 0) + count);
        }

        // Железная руда
        count = generateOre(world, random, chunk,
                Material.IRON_ORE, Material.DEEPSLATE_IRON_ORE,
                IRON_VEINS_PER_CHUNK, -64, 72, 9);
        if (oreStats != null) {
            oreStats.put(Material.IRON_ORE, oreStats.getOrDefault(Material.IRON_ORE, 0) + count);
        }

        // Золотая руда
        count = generateOre(world, random, chunk,
                Material.GOLD_ORE, Material.DEEPSLATE_GOLD_ORE,
                GOLD_VEINS_PER_CHUNK, -64, 32, 9);
        if (oreStats != null) {
            oreStats.put(Material.GOLD_ORE, oreStats.getOrDefault(Material.GOLD_ORE, 0) + count);
        }

        // Алмазная руда
        count = generateOre(world, random, chunk,
                Material.DIAMOND_ORE, Material.DEEPSLATE_DIAMOND_ORE,
                DIAMOND_VEINS_PER_CHUNK, -64, 16, 8);
        if (oreStats != null) {
            oreStats.put(Material.DIAMOND_ORE, oreStats.getOrDefault(Material.DIAMOND_ORE, 0) + count);
        }

        // Редстоун руда
        count = generateOre(world, random, chunk,
                Material.REDSTONE_ORE, Material.DEEPSLATE_REDSTONE_ORE,
                REDSTONE_VEINS_PER_CHUNK, -64, 16, 8);
        if (oreStats != null) {
            oreStats.put(Material.REDSTONE_ORE, oreStats.getOrDefault(Material.REDSTONE_ORE, 0) + count);
        }

        // Лазурит руда
        count = generateOre(world, random, chunk,
                Material.LAPIS_ORE, Material.DEEPSLATE_LAPIS_ORE,
                LAPIS_VEINS_PER_CHUNK, -64, 64, 7);
        if (oreStats != null) {
            oreStats.put(Material.LAPIS_ORE, oreStats.getOrDefault(Material.LAPIS_ORE, 0) + count);
        }

        // Изумрудная руда
        count = generateOre(world, random, chunk,
                Material.EMERALD_ORE, Material.DEEPSLATE_EMERALD_ORE,
                EMERALD_VEINS_PER_CHUNK, -16, 320, 3);
        if (oreStats != null) {
            oreStats.put(Material.EMERALD_ORE, oreStats.getOrDefault(Material.EMERALD_ORE, 0) + count);
        }

        // Блоки сырого железа
        count = generateOre(world, random, chunk,
                Material.RAW_IRON_BLOCK, null,
                RAW_IRON_BLOCK_VEINS_PER_CHUNK, -64, 64, 5);
        if (oreStats != null) {
            oreStats.put(Material.RAW_IRON_BLOCK, oreStats.getOrDefault(Material.RAW_IRON_BLOCK, 0) + count);
        }

        // Блоки сырой меди
        count = generateOre(world, random, chunk,
                Material.RAW_COPPER_BLOCK, null,
                RAW_COPPER_BLOCK_VEINS_PER_CHUNK, -64, 64, 5);
        if (oreStats != null) {
            oreStats.put(Material.RAW_COPPER_BLOCK, oreStats.getOrDefault(Material.RAW_COPPER_BLOCK, 0) + count);
        }
    }

    private void generateNetherOres(World world, Random random, Chunk chunk, Map<Material, Integer> oreStats) {
        // Кварц руда
        int count = generateOre(world, random, chunk,
                Material.NETHER_QUARTZ_ORE, null,
                NETHER_QUARTZ_VEINS_PER_CHUNK, 10, 117, 14);
        if (oreStats != null) {
            oreStats.put(Material.NETHER_QUARTZ_ORE, oreStats.getOrDefault(Material.NETHER_QUARTZ_ORE, 0) + count);
        }

        // Золотая адская руда
        count = generateOre(world, random, chunk,
                Material.NETHER_GOLD_ORE, null,
                NETHER_GOLD_VEINS_PER_CHUNK, 10, 117, 10);
        if (oreStats != null) {
            oreStats.put(Material.NETHER_GOLD_ORE, oreStats.getOrDefault(Material.NETHER_GOLD_ORE, 0) + count);
        }

        // Незерит
        count = generateOre(world, random, chunk,
                Material.ANCIENT_DEBRIS, null,
                ANCIENT_DEBRIS_VEINS_PER_CHUNK, 8, 119, 2);
        if (oreStats != null) {
            oreStats.put(Material.ANCIENT_DEBRIS, oreStats.getOrDefault(Material.ANCIENT_DEBRIS, 0) + count);
        }
    }

    // NFC-генерация для обычного мира
    // Генерируем только обычные версии руд - deepslate версии определяются
    // автоматически по высоте
    // Это предотвращает двойную генерацию (обычная + deepslate) и упрощает
    // конфигурацию
    private void generateNFCOverworldOres(World world, Random random, Chunk chunk, Map<Material, Integer> oreStats) {
        Material[] ores = {
                // Обычные руды (deepslate версии определяются автоматически по высоте)
                Material.COAL_ORE, Material.COPPER_ORE, Material.IRON_ORE, Material.GOLD_ORE,
                Material.DIAMOND_ORE, Material.REDSTONE_ORE, Material.LAPIS_ORE, Material.EMERALD_ORE,
                // Блоки сырого железа и меди
                Material.RAW_IRON_BLOCK, Material.RAW_COPPER_BLOCK
        };

        int chunkX = chunk.getX() * 16;
        int chunkZ = chunk.getZ() * 16;

        for (Material ore : ores) {
            int count = generateNFCOre(world, random, chunk, chunkX, chunkZ, ore);
            if (oreStats != null && count > 0) {
                oreStats.put(ore, oreStats.getOrDefault(ore, 0) + count);
            }
        }
    }

    // NFC-генерация для ада
    private void generateNFCNetherOres(World world, Random random, Chunk chunk, Map<Material, Integer> oreStats) {
        Material[] ores = {
                Material.NETHER_QUARTZ_ORE,
                Material.NETHER_GOLD_ORE,
                Material.ANCIENT_DEBRIS
        };

        int chunkX = chunk.getX() * 16;
        int chunkZ = chunk.getZ() * 16;

        for (Material ore : ores) {
            int count = generateNFCOre(world, random, chunk, chunkX, chunkZ, ore);
            if (oreStats != null && count > 0) {
                oreStats.put(ore, oreStats.getOrDefault(ore, 0) + count);
            }
        }
    }

    // Генерация руды с использованием NFC-логики
    // Возвращает количество размещенных блоков руды
    private int generateNFCOre(World world, Random random, Chunk chunk, int chunkX, int chunkZ, Material oreType) {
        OreNFCConfig config = configManager.getOreNFCConfig(world, oreType);

        // Если нет конфига для этой руды, пропускаем
        if (config == null) {
            return 0;
        }

        // Если множитель 0, не генерируем
        if (config.getMultiplier() == 0.0) {
            return 0;
        }

        // Вычисляем финальный шанс с учетом множителя
        double finalChance = config.getFinalChance();
        int attempts = config.getAttempts();

        // Определяем глубинный вариант (делаем это один раз, до цикла)
        Material deepslateType = null;
        Material baseOreType = oreType;

        if (oreType.name().startsWith("DEEPSLATE_")) {
            // Это уже глубинный вариант - используем его как основной
            // deepslateType остается null, так как это уже deepslate
            baseOreType = oreType;
        } else {
            // Это обычный вариант - ищем соответствующий глубинный вариант
            try {
                deepslateType = Material.valueOf("DEEPSLATE_" + oreType.name());
            } catch (IllegalArgumentException e) {
                // Нет глубинного варианта
            }
            baseOreType = oreType;
        }

        // Определяем блок, в котором генерируется руда
        Material generateIn = getGenerateInBlock(world, oreType);

        // Генерируем руду несколько раз, если attempts > 1
        // В оригинальном коде Beta 1.7.3 некоторые руды генерировались в циклах
        int totalBlocksPlaced = 0;
        for (int attempt = 0; attempt < attempts; attempt++) {
            // Проверяем шанс генерации для каждой попытки
            // Если base-chance = 1.0, всегда генерируем (как в оригинале)
            if (finalChance < 1.0 && random.nextDouble() >= finalChance) {
                continue; // Пропускаем эту попытку
            }

            // Генерируем случайную позицию в чанке для каждой попытки
            int x = chunkX + random.nextInt(16);
            int z = chunkZ + random.nextInt(16);
            int y = getRandomY(world, oreType, random);

            // Генерируем в зависимости от типа генератора
            int blocksPlaced = 0;
            switch (config.getGeneratorType()) {
                case MINABLE:
                    NFCWorldGenMinable minable = new NFCWorldGenMinable(
                            baseOreType, deepslateType, config.getVeinSize(), generateIn);
                    blocksPlaced = minable.generate(world, random, chunk, chunkX, chunkZ, x, y, z);
                    break;

                case CONCENTRATED:
                    NFCWorldGenConcentrated concentrated = new NFCWorldGenConcentrated(
                            baseOreType, deepslateType, config.getRadius(), config.getVeins(),
                            config.getVeinSize(), generateIn);
                    blocksPlaced = concentrated.generate(world, random, chunk, chunkX, chunkZ, x, y, z);
                    break;

                case CLOUD:
                    NFCWorldGenMinableCloud cloud = new NFCWorldGenMinableCloud(
                            baseOreType, deepslateType, config.getRadius(), config.getDensity(),
                            config.getAmount(), generateIn);
                    blocksPlaced = cloud.generate(world, random, chunk, chunkX, chunkZ, x, y, z);
                    break;
            }

            totalBlocksPlaced += blocksPlaced;
        }

        return totalBlocksPlaced;
    }

    private Material getGenerateInBlock(World world, Material ore) {
        if (world.getEnvironment() == World.Environment.NETHER) {
            return Material.NETHERRACK;
        } else {
            // Для deepslate руд используем DEEPSLATE, для остальных - STONE
            if (ore.name().startsWith("DEEPSLATE_")) {
                return Material.DEEPSLATE;
            }
            if (ore == Material.RAW_IRON_BLOCK || ore == Material.RAW_COPPER_BLOCK) {
                return Material.STONE;
            }
            return Material.STONE;
        }
    }

    private int getRandomY(World world, Material ore, Random random) {
        // Получаем базовое имя руды (без DEEPSLATE_ префикса)
        String oreName = ore.name();
        boolean isDeepslate = oreName.startsWith("DEEPSLATE_");
        if (isDeepslate) {
            oreName = oreName.replace("DEEPSLATE_", "");
        }

        // Для deepslate руд генерируем в основном ниже Y=0 (глубинные слои)
        // Для обычных руд используем стандартные диапазоны
        int y;

        if (oreName.equals("COAL_ORE")) {
            // Уголь: оригинал 0-81 (CONCENTRATED) и 48-95 (MINABLE)
            // Адаптируем: обычный уголь -64 до 95 (полный диапазон, deepslate генерируется
            // автоматически ниже Y=0)
            y = isDeepslate ? random.nextInt(64) - 64 : random.nextInt(160) - 64;
        } else if (oreName.equals("COPPER_ORE")) {
            // Медь: оригинал 38-81
            // Адаптируем: обычная медь -64 до 81 (полный диапазон, deepslate генерируется
            // автоматически ниже Y=0)
            y = isDeepslate ? random.nextInt(64) - 64 : random.nextInt(146) - 64;
        } else if (oreName.equals("IRON_ORE")) {
            // Железо: оригинал 2-33
            // Адаптируем: обычное железо -64 до 33 (полный диапазон, deepslate генерируется
            // автоматически ниже Y=0)
            y = isDeepslate ? random.nextInt(64) - 64 : random.nextInt(98) - 64;
        } else if (oreName.equals("GOLD_ORE")) {
            // Золото: оригинал 0-59
            // Адаптируем: обычное золото -64 до 59 (полный диапазон, deepslate генерируется
            // автоматически ниже Y=0)
            y = isDeepslate ? random.nextInt(64) - 64 : random.nextInt(124) - 64;
        } else if (oreName.equals("DIAMOND_ORE")) {
            // Алмаз: оригинал 0-15
            // Адаптируем: обычный алмаз -64 до 15 (полный диапазон, deepslate генерируется
            // автоматически ниже Y=0)
            y = isDeepslate ? random.nextInt(64) - 64 : random.nextInt(80) - 64;
        } else if (oreName.equals("REDSTONE_ORE")) {
            // Редстоун: оригинал 0-15
            // Адаптируем: обычный редстоун -64 до 15 (полный диапазон, deepslate
            // генерируется автоматически ниже Y=0)
            y = isDeepslate ? random.nextInt(64) - 64 : random.nextInt(80) - 64;
        } else if (oreName.equals("LAPIS_ORE")) {
            // Лазурит: оригинал 0-31
            // Адаптируем: обычный лазурит -64 до 31 (полный диапазон, deepslate
            // генерируется автоматически ниже Y=0)
            y = isDeepslate ? random.nextInt(64) - 64 : random.nextInt(96) - 64;
        } else if (oreName.equals("EMERALD_ORE")) {
            // Изумруд: используем ванильную генерацию (высокие горы)
            // В ваниле изумруды генерируются от -16 до 320, но чаще в горах
            // Обычный изумруд: -16 до 320 (ванильный диапазон)
            // Deepslate изумруд: -64 до -16 (глубинные слои)
            y = isDeepslate ? random.nextInt(48) - 64 : random.nextInt(336) - 16;
        } else if (ore == Material.RAW_IRON_BLOCK || ore == Material.RAW_COPPER_BLOCK) {
            y = random.nextInt(128) - 64;
        } else if (ore == Material.NETHER_QUARTZ_ORE) {
            y = random.nextInt(107) + 10;
        } else if (ore == Material.NETHER_GOLD_ORE) {
            y = random.nextInt(107) + 10;
        } else if (ore == Material.ANCIENT_DEBRIS) {
            y = random.nextInt(112) + 8;
        } else {
            // По умолчанию
            y = random.nextInt(world.getMaxHeight() - world.getMinHeight()) + world.getMinHeight();
        }

        // Ограничиваем Y в пределах мира
        return Math.max(world.getMinHeight(), Math.min(world.getMaxHeight() - 1, y));
    }

    private int generateOre(World world, Random random, Chunk chunk,
            Material oreType, Material deepslateType,
            int baseVeins, int minY, int maxY, int veinSize) {

        double multiplier = configManager.getOreMultiplier(world, oreType);
        int totalBlocks = 0;

        // Если множитель 0, не генерируем эту руду
        if (multiplier == 0.0) {
            return 0;
        }

        // Вычисляем количество жил с учетом множителя
        int veins = (int) Math.round(baseVeins * multiplier);

        for (int i = 0; i < veins; i++) {
            int localX = random.nextInt(16);
            int localZ = random.nextInt(16);
            int y = random.nextInt(maxY - minY) + minY;

            // Определяем, использовать ли глубинный сланец
            Material targetOre = oreType;
            if (deepslateType != null && y < 0) {
                // Ниже Y=0 используем глубинный сланец
                if (random.nextDouble() < 0.75) { // 75% шанс на глубинный сланец ниже Y=0
                    targetOre = deepslateType;
                }
            }

            // Генерируем жилу руды
            totalBlocks += generateVein(chunk, random, localX, y, localZ, targetOre, veinSize);
        }

        // Также генерируем глубинный вариант отдельно, если он есть
        if (deepslateType != null) {
            double deepslateMultiplier = configManager.getOreMultiplier(world, deepslateType);
            if (deepslateMultiplier != 0.0 && deepslateMultiplier != multiplier) {
                int deepslateVeins = (int) Math.round(baseVeins * deepslateMultiplier);
                int deepslateBlocks = 0;
                for (int i = 0; i < deepslateVeins; i++) {
                    int localX = random.nextInt(16);
                    int localZ = random.nextInt(16);
                    int y = random.nextInt(Math.abs(minY)) - Math.abs(minY); // Только отрицательные Y
                    deepslateBlocks += generateVein(chunk, random, localX, y, localZ, deepslateType, veinSize);
                }
                // Добавляем deepslate блоки к общему счету (они считаются как часть основной
                // руды)
                totalBlocks += deepslateBlocks;
            }
        }

        return totalBlocks;
    }

    private int generateVein(Chunk chunk, Random random, int localX, int y, int localZ, Material ore, int size) {
        World world = chunk.getWorld();
        int blocksPlaced = 0;

        // Простая генерация жилы руды
        for (int i = 0; i < size; i++) {
            int offsetX = random.nextInt(3) - 1;
            int offsetY = random.nextInt(3) - 1;
            int offsetZ = random.nextInt(3) - 1;

            int blockX = localX + offsetX;
            int blockY = y + offsetY;
            int blockZ = localZ + offsetZ;

            // Проверяем, что блок находится в пределах текущего чанка (0-15)
            if (blockX < 0 || blockX >= 16 || blockZ < 0 || blockZ >= 16) {
                continue;
            }

            if (blockY < world.getMinHeight() || blockY >= world.getMaxHeight()) {
                continue;
            }

            // Используем chunk.getBlock() вместо world.getBlockAt() для избежания рекурсии
            Block block = chunk.getBlock(blockX, blockY, blockZ);
            Material currentType = block.getType();

            // Заменяем только камень, глубокий сланец, туф и другие подходящие блоки
            // Отключаем обновление физики, чтобы избежать загрузки соседних чанков
            if (isReplaceableBlock(currentType, ore)) {
                block.setType(ore, false);
                blocksPlaced++;
            }
        }

        return blocksPlaced;
    }

    private boolean isReplaceableBlock(Material block, Material ore) {
        // Определяем, какие блоки можно заменять
        if (ore == Material.RAW_IRON_BLOCK || ore == Material.RAW_COPPER_BLOCK) {
            return block == Material.STONE || block == Material.DEEPSLATE ||
                    block == Material.GRANITE || block == Material.DIORITE ||
                    block == Material.ANDESITE || block == Material.TUFF;
        } else if (ore.name().contains("DEEPSLATE")) {
            return block == Material.DEEPSLATE || block == Material.TUFF || block == Material.STONE;
        } else if (ore == Material.NETHER_QUARTZ_ORE || ore == Material.NETHER_GOLD_ORE) {
            return block == Material.NETHERRACK || block == Material.BASALT || block == Material.BLACKSTONE;
        } else if (ore == Material.ANCIENT_DEBRIS) {
            return block == Material.NETHERRACK;
        } else {
            return block == Material.STONE || block == Material.GRANITE ||
                    block == Material.DIORITE || block == Material.ANDESITE;
        }
    }
}
