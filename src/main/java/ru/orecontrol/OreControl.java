package ru.orecontrol;

import org.bukkit.plugin.java.JavaPlugin;
import ru.orecontrol.command.OreControlCommand;
import ru.orecontrol.config.ConfigManager;
import ru.orecontrol.listener.OreGenerationListener;

public class OreControl extends JavaPlugin {

    private ConfigManager configManager;

    @Override
    public void onEnable() {
        // Сохраняем дефолтный конфиг если его нет
        saveDefaultConfig();
        
        // Инициализируем менеджер конфигурации
        configManager = new ConfigManager(this);
        
        // Регистрируем команду
        OreControlCommand commandExecutor = new OreControlCommand(this, configManager);
        getCommand("orecontrol").setExecutor(commandExecutor);
        getCommand("orecontrol").setTabCompleter(commandExecutor);
        
        // Регистрируем слушатель генерации
        getServer().getPluginManager().registerEvents(
            new OreGenerationListener(this, configManager), 
            this
        );
        
        getLogger().info("OreControl успешно загружен!");
    }

    @Override
    public void onDisable() {
        getLogger().info("OreControl выгружен!");
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }
}

