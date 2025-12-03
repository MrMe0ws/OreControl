package ru.orecontrol.generator.nfc;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;

import java.util.Random;

/**
 * Генератор концентрированных скоплений руды (адаптация WorldGenConcentrated из
 * бета 1.7.3)
 * Создает несколько жил в радиусе, используя NFCWorldGenMinable для каждой жилы
 */
public class NFCWorldGenConcentrated {

    private final Material oreType;
    private final Material deepslateType;
    private final int radius;
    private final int veins;
    private final int amount;
    private final Material generateIn;

    public NFCWorldGenConcentrated(Material oreType, Material deepslateType, int radius, int veins, int amount,
            Material generateIn) {
        this.oreType = oreType;
        this.deepslateType = deepslateType;
        this.radius = radius;
        this.veins = veins;
        this.amount = amount;
        this.generateIn = generateIn;
    }

    public int generate(World world, Random random, Chunk chunk, int chunkX, int chunkZ, int x, int y, int z) {
        double deg = (2.0 * Math.PI) / (double) veins;
        int totalBlocks = 0;

        // Генерируем veins жил (исправлено: было veins + 1, теперь точно veins)
        for (int p = veins - 1; p >= 0; p--) {
            double length = (double) radius;
            length = length * random.nextFloat();

            // Вычисляем смещение относительно центральной точки
            int xx = (int) (length * Math.cos(deg * p));
            int zz = (int) (length * Math.sin(deg * p));
            int yy = y + random.nextInt(3) - 1; // -1 до +1 для небольшого вертикального разброса

            // Ограничиваем смещение, чтобы жилы не выходили слишком далеко за пределы чанка
            // Но все равно позволяем небольшое выхождение, так как жилы могут быть частично в чанке
            int newX = x + xx;
            int newZ = z + zz;

            // Используем NFCWorldGenMinable для генерации каждой жилы
            // NFCWorldGenMinable сам проверит, находятся ли блоки в пределах чанка
            NFCWorldGenMinable minable = new NFCWorldGenMinable(oreType, deepslateType, amount, generateIn);
            totalBlocks += minable.generate(world, random, chunk, chunkX, chunkZ, newX, yy, newZ);
        }

        return totalBlocks;
    }
}
