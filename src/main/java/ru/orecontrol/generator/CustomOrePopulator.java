package ru.orecontrol.generator;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.generator.BlockPopulator;
import ru.orecontrol.config.ConfigManager;

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
        // Генерируем руды для обычного мира
        if (world.getEnvironment() == World.Environment.NORMAL) {
            generateOverworldOres(world, random, chunk);
        }
        // Генерируем руды для ада
        else if (world.getEnvironment() == World.Environment.NETHER) {
            generateNetherOres(world, random, chunk);
        }
    }

    private void generateOverworldOres(World world, Random random, Chunk chunk) {
        // Угольная руда
        generateOre(world, random, chunk,
                Material.COAL_ORE, Material.DEEPSLATE_COAL_ORE,
                COAL_VEINS_PER_CHUNK, 0, 192, 17);

        // Медная руда
        generateOre(world, random, chunk,
                Material.COPPER_ORE, Material.DEEPSLATE_COPPER_ORE,
                COPPER_VEINS_PER_CHUNK, -16, 112, 10);

        // Железная руда
        generateOre(world, random, chunk,
                Material.IRON_ORE, Material.DEEPSLATE_IRON_ORE,
                IRON_VEINS_PER_CHUNK, -64, 72, 9);

        // Золотая руда
        generateOre(world, random, chunk,
                Material.GOLD_ORE, Material.DEEPSLATE_GOLD_ORE,
                GOLD_VEINS_PER_CHUNK, -64, 32, 9);

        // Алмазная руда
        generateOre(world, random, chunk,
                Material.DIAMOND_ORE, Material.DEEPSLATE_DIAMOND_ORE,
                DIAMOND_VEINS_PER_CHUNK, -64, 16, 8);

        // Редстоун руда
        generateOre(world, random, chunk,
                Material.REDSTONE_ORE, Material.DEEPSLATE_REDSTONE_ORE,
                REDSTONE_VEINS_PER_CHUNK, -64, 16, 8);

        // Лазурит руда
        generateOre(world, random, chunk,
                Material.LAPIS_ORE, Material.DEEPSLATE_LAPIS_ORE,
                LAPIS_VEINS_PER_CHUNK, -64, 64, 7);

        // Изумрудная руда
        generateOre(world, random, chunk,
                Material.EMERALD_ORE, Material.DEEPSLATE_EMERALD_ORE,
                EMERALD_VEINS_PER_CHUNK, -16, 320, 3);

        // Блоки сырого железа
        generateOre(world, random, chunk,
                Material.RAW_IRON_BLOCK, null,
                RAW_IRON_BLOCK_VEINS_PER_CHUNK, -64, 64, 5);

        // Блоки сырой меди
        generateOre(world, random, chunk,
                Material.RAW_COPPER_BLOCK, null,
                RAW_COPPER_BLOCK_VEINS_PER_CHUNK, -64, 64, 5);
    }

    private void generateNetherOres(World world, Random random, Chunk chunk) {
        // Кварц руда
        generateOre(world, random, chunk,
                Material.NETHER_QUARTZ_ORE, null,
                NETHER_QUARTZ_VEINS_PER_CHUNK, 10, 117, 14);

        // Золотая адская руда
        generateOre(world, random, chunk,
                Material.NETHER_GOLD_ORE, null,
                NETHER_GOLD_VEINS_PER_CHUNK, 10, 117, 10);

        // Незерит
        generateOre(world, random, chunk,
                Material.ANCIENT_DEBRIS, null,
                ANCIENT_DEBRIS_VEINS_PER_CHUNK, 8, 119, 2);
    }

    private void generateOre(World world, Random random, Chunk chunk,
            Material oreType, Material deepslateType,
            int baseVeins, int minY, int maxY, int veinSize) {

        double multiplier = configManager.getOreMultiplier(world, oreType);

        // Если множитель 0, не генерируем эту руду
        if (multiplier == 0.0) {
            return;
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
            generateVein(chunk, random, localX, y, localZ, targetOre, veinSize);
        }

        // Также генерируем глубинный вариант отдельно, если он есть
        if (deepslateType != null) {
            double deepslateMultiplier = configManager.getOreMultiplier(world, deepslateType);
            if (deepslateMultiplier != 0.0 && deepslateMultiplier != multiplier) {
                int deepslateVeins = (int) Math.round(baseVeins * deepslateMultiplier);
                for (int i = 0; i < deepslateVeins; i++) {
                    int localX = random.nextInt(16);
                    int localZ = random.nextInt(16);
                    int y = random.nextInt(Math.abs(minY)) - Math.abs(minY); // Только отрицательные Y
                    generateVein(chunk, random, localX, y, localZ, deepslateType, veinSize);
                }
            }
        }
    }

    private void generateVein(Chunk chunk, Random random, int localX, int y, int localZ, Material ore, int size) {
        World world = chunk.getWorld();

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
            }
        }
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
