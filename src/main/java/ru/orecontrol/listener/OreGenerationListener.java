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

import java.util.HashMap;
import java.util.Map;
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

        // Удаляем ванильную руду для всех руд из конфига
        // Это нужно, чтобы наша генерация полностью контролировала спавн руды
        // независимо от режима (default-generation или nfc-generation)
        removeStandardOres(event);

        // Применяем свою генерацию
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

        // Создаем карту руд, которые нужно заменить
        // Удаляем ванильную руду только для тех руд, которые есть в конфиге
        // Это нужно, чтобы наша генерация полностью контролировала спавн руды
        Map<Material, Material> replacementMap = new HashMap<>();
        for (Material ore : ores) {
            // Проверяем, есть ли эта руда в конфиге для этого мира
            // Если есть - удаляем ванильную руду, чтобы контролировать генерацию
            if (configManager.hasOreConfig(world, ore)) {
                replacementMap.put(ore, getReplacementBlock(ore, world));
            }
        }

        // Если нет руд для замены, выходим
        if (replacementMap.isEmpty()) {
            return;
        }

        // Оптимизация: проходим по блокам только один раз, проверяя все руды
        // одновременно
        // Используем ChunkSnapshot для более быстрого чтения
        org.bukkit.ChunkSnapshot snapshot = chunk.getChunkSnapshot();
        int minY = world.getMinHeight();
        int maxY = world.getMaxHeight();

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = minY; y < maxY; y++) {
                    Material blockType = snapshot.getBlockType(x, y, z);
                    Material replacement = replacementMap.get(blockType);

                    if (replacement != null) {
                        // Используем chunk.getBlock() только когда нужно изменить блок
                        Block block = chunk.getBlock(x, y, z);
                        // Отключаем обновление физики, чтобы избежать загрузки соседних чанков
                        block.setType(replacement, false);
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
