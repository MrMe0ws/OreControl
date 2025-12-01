package ru.orecontrol.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import ru.orecontrol.OreControl;
import ru.orecontrol.config.ConfigManager;

import java.util.ArrayList;
import java.util.List;

public class OreControlCommand implements CommandExecutor, TabCompleter {

    private final OreControl plugin;
    private final ConfigManager configManager;

    public OreControlCommand(OreControl plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§6OreControl §7v" + plugin.getDescription().getVersion());
            sender.sendMessage("§7Используйте: §e/orecontrol reload §7для перезагрузки конфигурации");
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("orecontrol.reload")) {
                sender.sendMessage("§cУ вас нет прав для выполнения этой команды!");
                return true;
            }

            try {
                configManager.reload();
                if (configManager.isEnabled()) {
                    sender.sendMessage("§aКонфигурация OreControl успешно перезагружена!");
                    sender.sendMessage("§7Плагин: §aвключен");
                } else {
                    sender.sendMessage("§aКонфигурация OreControl успешно перезагружена!");
                    sender.sendMessage("§7Плагин: §cвыключен");
                }
                plugin.getLogger().info("Конфигурация перезагружена игроком " + sender.getName());
            } catch (Exception e) {
                sender.sendMessage("§cОшибка при перезагрузке конфигурации: " + e.getMessage());
                plugin.getLogger().severe("Ошибка при перезагрузке конфигурации: " + e.getMessage());
                e.printStackTrace();
            }
            return true;
        }

        sender.sendMessage("§cНеизвестная подкоманда. Используйте: §e/orecontrol reload");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            if (sender.hasPermission("orecontrol.reload")) {
                completions.add("reload");
            }
            return completions;
        }
        return new ArrayList<>();
    }
}
