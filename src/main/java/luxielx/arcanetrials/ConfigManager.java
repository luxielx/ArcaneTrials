package luxielx.arcanetrials;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ConfigManager {
    private static ConfigManager single_inst = null;
    private static Main plugin = null;
    private ArrayList<FileConfiguration> customConfigs = new ArrayList<FileConfiguration>();
    private ArrayList<String> configNames = new ArrayList<String>();

    private ConfigManager() {
    }

    public static ConfigManager getInstance() {
        if (single_inst == null) {
            single_inst = new ConfigManager();
        }
        return single_inst;
    }

    public static File getFile(String name) {
        File f = new File(plugin.getDataFolder() + File.separator + name + ".yml");
        return f;
    }

    public static FileConfiguration getBanList() {
        return getInstance().getConfig("banlist.yml");
    }

    public static FileConfiguration getConfig() {
        return getInstance().getConfig("config.yml");
    }

    public static YamlConfiguration getYamlFile(String name) {
        return YamlConfiguration.loadConfiguration(getFile(name));
    }

//    //Remove if not using placeholderAPI
//    public String getString(FileConfiguration conf, String path, Player player){
//        //Create dummy if not available
//        if (!conf.contains(path)) {
//            setData(conf, path, "dummy PAPI string");
//        }
//        return PlaceholderAPI.setPlaceholders(player, conf.getString(path));
//    }

    public void setPlugin(Main plugin) {
        ConfigManager.plugin = plugin;
    }

    public FileConfiguration getConfig(String name) {
        try {
            if (customConfigs.size() > 0) {
                for (FileConfiguration conf : customConfigs) {
                    if (conf.getName().equalsIgnoreCase(name)) {
                        return conf;
                    }
                }

            }
            return createNewCustomConfig(name);
        } catch (Exception e) {

        }
        return createNewCustomConfig(name);

    }

    public void reloadConfigs() {
        customConfigs.clear();
        configNames.clear();
    }

    public FileConfiguration createNewCustomConfig(String name) {
        FileConfiguration fileConfiguration;
        File configFile = new File(plugin.getDataFolder(), name);
        if (!configFile.exists()) {
            configFile.getParentFile().mkdirs();
            plugin.saveResource(name, false);
        }

        fileConfiguration = new YamlConfiguration();
        try {
            fileConfiguration.load(configFile);
            customConfigs.add(fileConfiguration);
            configNames.add(name);
            return fileConfiguration;
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
        return null;
    }

    //Returns true if saved successfully, returns false in case of error and prints error to console
    public boolean setData(FileConfiguration conf, String path, Object data) {
        conf.set(path, data);
        return saveData(conf);
    }

    private boolean saveData(FileConfiguration conf) {
        try {
            conf.save(new File(plugin.getDataFolder(), configNames.get(customConfigs.indexOf(conf))));
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public String getStringRaw(FileConfiguration conf, String path) {
        //Create dummy if not available
        if (!conf.contains(path)) {
            setData(conf, path, "dummy string");
        }
        return conf.getString(path);
    }

    public int getInt(FileConfiguration conf, String path) {
        //Create dummy if not available
        if (!conf.contains(path)) {
            setData(conf, path, 1);
        }
        return conf.getInt(path);
    }

    public double getDouble(FileConfiguration conf, String path) {
        //Create dummy if not available
        if (!conf.contains(path)) {
            setData(conf, path, 1.0);
        }
        return conf.getDouble(path);
    }

    public boolean getBoolean(FileConfiguration conf, String path) {
        //Create dummy if not available
        if (!conf.contains(path)) {
            setData(conf, path, false);
        }
        return conf.getBoolean(path);
    }

    public List<?> getList(FileConfiguration conf, String path) {
        //Create dummy list if not available
        if (!conf.contains(path)) {
            setData(conf, path, new ArrayList<Location>().add(new Location(Bukkit.getWorld("world"), 10, 10, 10)));
        }
        return conf.getList(path);
    }


}