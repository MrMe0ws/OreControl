package ru.orecontrol.generator.nfc;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;

import java.util.Random;

/**
 * Генератор концентрированных скоплений руды (адаптация WorldGenConcentrated из бета 1.7.3)
 * Создает несколько жил в радиусе, используя NFCWorldGenMinable для каждой жилы
 */
public class NFCWorldGenConcentrated {
    
    private final Material oreType;
    private final Material deepslateType;
    private final int radius;
    private final int veins;
    private final int amount;
    private final Material generateIn;
    
    public NFCWorldGenConcentrated(Material oreType, Material deepslateType, int radius, int veins, int amount, Material generateIn) {
        this.oreType = oreType;
        this.deepslateType = deepslateType;
        this.radius = radius;
        this.veins = veins;
        this.amount = amount;
        this.generateIn = generateIn;
    }
    
    public boolean generate(World world, Random random, Chunk chunk, int chunkX, int chunkZ, int x, int y, int z) {
        double deg = (2.0 * Math.PI) / (double) veins;
        
        for (int p = veins; p >= 0; p--) {
            double length = (double) radius;
            length = length * random.nextFloat();
            
            int xx = (int) (length * Math.cos(deg * p)) + radius;
            int zz = (int) (length * Math.sin(deg * p)) + radius;
            int yy = y + random.nextInt(3);
            
            // Используем NFCWorldGenMinable для генерации каждой жилы
            NFCWorldGenMinable minable = new NFCWorldGenMinable(oreType, deepslateType, amount, generateIn);
            minable.generate(world, random, chunk, chunkX, chunkZ, x + xx, yy, z + zz);
        }
        
        return true;
    }
}

