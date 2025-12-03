package ru.orecontrol.config;

import org.bukkit.Material;

public class OreNFCConfig {
    public enum GeneratorType {
        MINABLE,        // Эллипсоидные жилы (WorldGenMinable)
        CONCENTRATED,   // Концентрированные скопления (WorldGenConcentrated)
        CLOUD          // Облака (WorldGenMinableCloud)
    }

    private final Material material;
    private final double multiplier;
    private final GeneratorType generatorType;
    private final double baseChance;
    private final int veinSize;
    private final int radius;
    private final int density;
    private final int amount;
    private final int veins;
    private final int attempts; // Количество попыток генерации за чанк (для руд, которые генерируются несколько раз)

    public OreNFCConfig(Material material, double multiplier, GeneratorType generatorType,
                       double baseChance, int veinSize, int radius, int density, int amount, int veins, int attempts) {
        this.material = material;
        this.multiplier = multiplier;
        this.generatorType = generatorType;
        this.baseChance = baseChance;
        this.veinSize = veinSize;
        this.radius = radius;
        this.density = density;
        this.amount = amount;
        this.veins = veins;
        this.attempts = attempts;
    }

    public Material getMaterial() {
        return material;
    }

    public double getMultiplier() {
        return multiplier;
    }

    public GeneratorType getGeneratorType() {
        return generatorType;
    }

    public double getBaseChance() {
        return baseChance;
    }

    public int getVeinSize() {
        return veinSize;
    }

    public int getRadius() {
        return radius;
    }

    public int getDensity() {
        return density;
    }

    public int getAmount() {
        return amount;
    }

    public int getVeins() {
        return veins;
    }

    public int getAttempts() {
        return attempts;
    }

    // Вычисляет финальный шанс с учетом множителя
    public double getFinalChance() {
        return Math.min(1.0, baseChance * multiplier);
    }
}

