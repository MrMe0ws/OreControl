package ru.orecontrol.generator.nfc;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.Random;

/**
 * Генератор облаков руды (адаптация WorldGenMinableCloud из бета 1.7.3)
 * Создает сферические скопления руды с определенной плотностью
 */
public class NFCWorldGenMinableCloud {

    private final Material oreType;
    private final Material deepslateType;
    private final int radius;
    private final int density;
    private final int amount;
    private final Material generateIn;

    public NFCWorldGenMinableCloud(Material oreType, Material deepslateType, int radius, int density, int amount,
            Material generateIn) {
        this.oreType = oreType;
        this.deepslateType = deepslateType;
        this.radius = radius;
        this.density = density;
        this.amount = amount;
        this.generateIn = generateIn;
    }

    public int generate(World world, Random random, Chunk chunk, int chunkX, int chunkZ, int x, int y, int z) {
        int p = (int) (amount - (amount * (random.nextFloat() * 0.2)));
        boolean pregen[][][] = new boolean[radius * 2][radius * 2][radius * 2];
        double deg = (2.0 * Math.PI) / (double) p;

        for (int o = p - 1; o >= 0; o--) {
            double length = (double) radius;
            for (int w = 0; w <= density; w++) {
                length = length * random.nextFloat();
            }
            int xx = (int) (length * Math.cos(deg * o)) + radius;
            int yy = (int) (length * Math.sin(deg * o)) + radius;
            int zz = (int) (length * Math.sin(2.0 * Math.PI * random.nextFloat())) + radius;

            if (pregen[xx][yy][zz]) {
                o++;
            } else {
                pregen[xx][yy][zz] = true;
            }
        }

        // Генерируем блоки руды
        int blocksPlaced = 0;
        for (int o = 0; o < radius * 2; o++) {
            for (int l = 0; l < radius * 2; l++) {
                for (int f = 0; f < radius * 2; f++) {
                    if (pregen[o][l][f]) {
                        int blockX = o + x - radius;
                        int blockY = l + y - radius;
                        int blockZ = f + z - radius;

                        // Преобразуем в локальные координаты чанка
                        int localX = blockX - chunkX;
                        int localZ = blockZ - chunkZ;

                        if (localX >= 0 && localX < 16 &&
                                localZ >= 0 && localZ < 16 &&
                                blockY >= world.getMinHeight() && blockY < world.getMaxHeight()) {

                            Block block = chunk.getBlock(localX, blockY, localZ);
                            Material currentType = block.getType();

                            if (currentType == generateIn || isReplaceableBlock(currentType)) {
                                Material targetOre = shouldUseDeepslate(blockY, random) ? deepslateType : oreType;
                                if (targetOre != null) {
                                    block.setType(targetOre, false);
                                    blocksPlaced++;
                                }
                            }
                        }
                    }
                }
            }
        }

        return blocksPlaced;
    }

    private boolean shouldUseDeepslate(int y, Random random) {
        if (deepslateType == null) {
            return false;
        }
        if (y < -16) {
            return true; // 100% шанс на глубинный сланец ниже Y=-16
        }
        if (y < 0) {
            return random.nextDouble() < 0.75; // 75% шанс на глубинный сланец от Y=-16 до Y=0
        }
        return false;
    }

    private boolean isReplaceableBlock(Material block) {
        return block == Material.STONE || block == Material.DEEPSLATE ||
                block == Material.GRANITE || block == Material.DIORITE ||
                block == Material.ANDESITE || block == Material.TUFF ||
                block == Material.NETHERRACK || block == Material.BASALT ||
                block == Material.BLACKSTONE;
    }
}
