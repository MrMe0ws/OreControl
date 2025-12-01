package ru.orecontrol.listener;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkPopulateEvent;
import ru.orecontrol.OreControl;
import ru.orecontrol.config.ConfigManager;
import ru.orecontrol.generator.CustomOrePopulator;

import java.util.Random;

public class OreGenerationListener implements Listener {

    private final OreControl plugin;
    private final ConfigManager configManager;

    public OreGenerationListener(OreControl plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChunkPopulate(ChunkPopulateEvent event) {
        // Проверяем, включен ли плагин
        if (!configManager.isEnabled()) {
            return;
        }

        World world = event.getWorld();

        // Проверяем, есть ли настройки для этого мира
        if (!hasWorldConfig(world)) {
            return;
        }

        // Удаляем стандартную генерацию руды и применяем свою
        removeStandardOres(event);
        generateCustomOres(event);
    }

    private boolean hasWorldConfig(World world) {
        String worldName = world.getName();
        return configManager.hasWorldConfig(worldName);
    }

    private void removeStandardOres(ChunkPopulateEvent event) {
        // Удаляем стандартные руды из чанка только для тех, у которых множитель != 1.0
        World world = event.getWorld();
        org.bukkit.Chunk chunk = event.getChunk();

        Material[] ores = {
                Material.COAL_ORE, Material.DEEPSLATE_COAL_ORE,
                Material.COPPER_ORE, Material.DEEPSLATE_COPPER_ORE,
                Material.IRON_ORE, Material.DEEPSLATE_IRON_ORE,
                Material.GOLD_ORE, Material.DEEPSLATE_GOLD_ORE,
                Material.DIAMOND_ORE, Material.DEEPSLATE_DIAMOND_ORE,
                Material.REDSTONE_ORE, Material.DEEPSLATE_REDSTONE_ORE,
                Material.LAPIS_ORE, Material.DEEPSLATE_LAPIS_ORE,
                Material.EMERALD_ORE, Material.DEEPSLATE_EMERALD_ORE,
                Material.RAW_IRON_BLOCK, Material.RAW_COPPER_BLOCK,
                Material.NETHER_GOLD_ORE, Material.NETHER_QUARTZ_ORE,
                Material.ANCIENT_DEBRIS
        };

        // Оптимизация: проверяем только те руды, для которых есть настройки
        for (Material ore : ores) {
            double multiplier = configManager.getOreMultiplier(world, ore);
            // Пропускаем, если множитель = 1.0 (стандартная генерация)
            if (multiplier == 1.0) {
                continue;
            }

            // Удаляем только эту конкретную руду, используя chunk.getBlock() для избежания
            // рекурсии
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    int minY = world.getMinHeight();
                    int maxY = world.getMaxHeight();

                    for (int y = minY; y < maxY; y++) {
                        Block block = chunk.getBlock(x, y, z);
                        if (block.getType() == ore) {
                            // Определяем, на что заменять
                            Material replacement = getReplacementBlock(ore, world);
                            // Отключаем обновление физики, чтобы избежать загрузки соседних чанков
                            block.setType(replacement, false);
                        }
                    }
                }
            }
        }
    }

    private Material getReplacementBlock(Material ore, World world) {
        if (ore == Material.RAW_IRON_BLOCK || ore == Material.RAW_COPPER_BLOCK) {
            return Material.STONE;
        } else if (ore.name().contains("DEEPSLATE")) {
            return Material.DEEPSLATE;
        } else if (world.getEnvironment() == World.Environment.NETHER) {
            return Material.NETHERRACK;
        } else {
            return Material.STONE;
        }
    }

    private void generateCustomOres(ChunkPopulateEvent event) {
        // Используем кастомный генератор для создания руды с нашими настройками
        CustomOrePopulator populator = new CustomOrePopulator(configManager);
        populator.populate(event.getWorld(), new Random(), event.getChunk());
    }
}
