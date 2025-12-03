package ru.orecontrol.generator.nfc;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.Random;

/**
 * Генератор эллипсоидных жил руды (адаптация WorldGenMinable из бета 1.7.3)
 */
public class NFCWorldGenMinable {
    
    private final Material oreType;
    private final Material deepslateType;
    private final int numberOfBlocks;
    private final Material generateIn;
    
    public NFCWorldGenMinable(Material oreType, Material deepslateType, int numberOfBlocks, Material generateIn) {
        this.oreType = oreType;
        this.deepslateType = deepslateType;
        this.numberOfBlocks = numberOfBlocks;
        this.generateIn = generateIn;
    }
    
    public boolean generate(World world, Random random, Chunk chunk, int chunkX, int chunkZ, int x, int y, int z) {
        // Преобразуем координаты в локальные координаты чанка
        int localX = x - chunkX;
        int localZ = z - chunkZ;
        
        // Проверяем, что координаты в пределах чанка
        if (localX < 0 || localX >= 16 || localZ < 0 || localZ >= 16) {
            return false;
        }
        
        if (numberOfBlocks == 1) {
            // Простая генерация одного блока
            if (y >= world.getMinHeight() && y < world.getMaxHeight()) {
                Block block = chunk.getBlock(localX, y, localZ);
                if (block.getType() == generateIn) {
                    Material targetOre = shouldUseDeepslate(y, random) ? deepslateType : oreType;
                    if (targetOre != null) {
                        block.setType(targetOre, false);
                    }
                }
            }
            return true;
        }
        
        // Генерация эллипсоидной жилы
        float f = random.nextFloat() * (float) Math.PI;
        double d = (double) (x + 8) + (Math.sin(f) * (double) numberOfBlocks) / 8.0;
        double d1 = (double) (x + 8) - (Math.sin(f) * (double) numberOfBlocks) / 8.0;
        double d2 = (double) (z + 8) + (Math.cos(f) * (double) numberOfBlocks) / 8.0;
        double d3 = (double) (z + 8) - (Math.cos(f) * (double) numberOfBlocks) / 8.0;
        double d4 = y + random.nextInt(3) + 2;
        double d5 = y + random.nextInt(3) + 2;
        
        for (int l = 0; l <= numberOfBlocks; l++) {
            double d6 = d + ((d1 - d) * (double) l) / (double) numberOfBlocks;
            double d7 = d4 + ((d5 - d4) * (double) l) / (double) numberOfBlocks;
            double d8 = d2 + ((d3 - d2) * (double) l) / (double) numberOfBlocks;
            double d9 = (random.nextDouble() * (double) numberOfBlocks) / 16.0;
            double d10 = (double) (Math.sin(((float) l * (float) Math.PI) / (float) numberOfBlocks) + 1.0F) * d9 + 1.0;
            double d11 = (double) (Math.sin(((float) l * (float) Math.PI) / (float) numberOfBlocks) + 1.0F) * d9 + 1.0;
            
            int i1 = (int) Math.floor(d6 - d10 / 2.0);
            int j1 = (int) Math.floor(d7 - d11 / 2.0);
            int k1 = (int) Math.floor(d8 - d10 / 2.0);
            int l1 = (int) Math.floor(d6 + d10 / 2.0);
            int i2 = (int) Math.floor(d7 + d11 / 2.0);
            int j2 = (int) Math.floor(d8 + d10 / 2.0);
            
            for (int k2 = i1; k2 <= l1; k2++) {
                double d12 = (((double) k2 + 0.5) - d6) / (d10 / 2.0);
                if (d12 * d12 >= 1.0) {
                    continue;
                }
                for (int l2 = j1; l2 <= i2; l2++) {
                    double d13 = (((double) l2 + 0.5) - d7) / (d11 / 2.0);
                    if (d12 * d12 + d13 * d13 >= 1.0) {
                        continue;
                    }
                    for (int i3 = k1; i3 <= j2; i3++) {
                        double d14 = (((double) i3 + 0.5) - d8) / (d10 / 2.0);
                        if (d12 * d12 + d13 * d13 + d14 * d14 < 1.0) {
                            // Преобразуем в локальные координаты чанка
                            int blockLocalX = k2 - chunkX;
                            int blockLocalZ = i3 - chunkZ;
                            
                            if (blockLocalX >= 0 && blockLocalX < 16 && 
                                blockLocalZ >= 0 && blockLocalZ < 16 &&
                                l2 >= world.getMinHeight() && l2 < world.getMaxHeight()) {
                                
                                Block block = chunk.getBlock(blockLocalX, l2, blockLocalZ);
                                if (block.getType() == generateIn || isReplaceableBlock(block.getType())) {
                                    Material targetOre = shouldUseDeepslate(l2, random) ? deepslateType : oreType;
                                    if (targetOre != null) {
                                        block.setType(targetOre, false);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        return true;
    }
    
    private boolean shouldUseDeepslate(int y, Random random) {
        if (deepslateType == null) {
            return false;
        }
        if (y < 0) {
            return random.nextDouble() < 0.75; // 75% шанс на глубинный сланец ниже Y=0
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

