package com.toonystank.liby;


import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("unused")
public class ConfigManager {

    /**
     * -- GETTER --
     *  Get Initialized file
     *
     */
    @Getter
    private File file;
    private Logger logger;
    public String fileName;
    /**
     * -- GETTER --
     *  Get Initialized config.
     * <p>
     * -- GETTER --
     *  Get Initialized config.
     *

     */
    @Getter
    private FileConfiguration config;
    @Nullable
    protected Plugin plugin;
    private final boolean isInFolder;
    private String configVersion;
    private boolean consoleLogger = true;
    @Nullable
    private String corePath = "";

    /**
     * Initializes the Config.
     *
     * @param plugin   Instance of the plugin you want to initialize the config for
     * @param fileName String
     * @param force    boolean enable/disable force file update
     * @param copy     boolean either copy the file from the plugin or not
     */
    public ConfigManager(@Nullable Plugin plugin, String fileName, boolean force, boolean copy) throws IOException {
        this.plugin = plugin;
        if (plugin != null) this.logger = plugin.getLogger();
        else this.logger = Logger.getGlobal();
        this.fileName = fileName;
        process(plugin,fileName,force,copy);
        isInFolder = false;
    }
    private void process(Plugin plugin, String fileName, boolean force, boolean copy) throws IOException {
        if (plugin == null) {
            this.file = new File(fileName);
        }else this.file = new File(plugin.getDataFolder(), fileName);
        if (copy) {
            try {
                copy(force);
            } catch (IllegalArgumentException e) {
                logger.info(e.getMessage());
            }
        }
        if (!file.exists()) {
            if (file.createNewFile()) {
                if (consoleLogger) logger.info("Created new file: " + file.getName());
            }
        }
        this.config = YamlConfiguration.loadConfiguration(this.file);
    }

    /**
     * Initializes the Config. in given path
     *
     * @param plugin   Instance of the plugin you want to initialize the config for
     * @param fileName String
     * @param path     String path you want to initialize the config in
     * @param force    boolean enable/disable force file update
     * @param copy     boolean either copy the file from the plugin or not
     */
    public ConfigManager(@Nullable Plugin plugin, String fileName, String path, boolean force, boolean copy) throws IOException {
        this.plugin = plugin;
        if (plugin != null) this.logger = plugin.getLogger();
        else this.logger = Logger.getGlobal();
        this.corePath = path;
        this.fileName = fileName;
        process(plugin, fileName, path, force, copy);
        isInFolder = true;
    }
    /**
     * Initializes the ConfigManager without creating a file.
     *
     * @param plugin   Instance of the plugin you want to initialize the config for
     * @param fileName String
     * @param path     String path you want to initialize the config in
     */
    public ConfigManager(Plugin plugin, String fileName, @Nullable String path) throws IOException {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.corePath = path;
        this.fileName = fileName;
        isInFolder = true;
    }

    /**
     * Check if the file exist.
     * @return boolean
     */
    public boolean isFileExist() {
        String corePath = plugin.getDataFolder() + File.separator + this.corePath;
        this.file = new File(corePath, fileName);
        return file.exists();
    }
    /**
     * Create the config File. use with InitializeConfig(Plugin, String fileName, String path)
     */
    public void make() throws IOException {
        process(plugin, file.getName(), corePath, false, false);
    }
    private void process(Plugin plugin, String fileName, String path, boolean force, boolean copy) throws IOException {
        String corePath;
        if (plugin == null) {
            corePath = path;
        }else corePath = plugin.getDataFolder() + File.separator + path;
        this.file = new File(corePath, fileName);
        if (!file.exists()) {
            File file = new File(corePath);
            if (file.mkdirs()) {
                if (consoleLogger) logger.info("Created new directory: " + file.getName());
            }
            if (copy) try {
                copy(false, path + File.separator + fileName);
            } catch (IllegalArgumentException e) {
                if (consoleLogger) logger.warning("Failed to copy file: " + fileName + " to path: " + corePath);
                logger.info(e.getMessage());
            }
            else {
                if (this.file.createNewFile()) if (consoleLogger) logger.info("Creating file: " + this.file.getName());
            }
        }
        this.config = YamlConfiguration.loadConfiguration(this.file);
    }

    /**
     * Set logger of config load events. by default it's enabled
     * @param consoleLogger boolean
     * @return Manager
     */
    public ConfigManager setConsoleLogger(boolean consoleLogger) {
        this.consoleLogger = consoleLogger;
        return this;
    }
    /**
     * Set the version of the config. useful for updating the config.
     * @param configVersion Version of the config
     * @return Manager
     */
    public ConfigManager setConfigVersion(String configVersion) {
        this.configVersion = configVersion;
        return this;
    }
    public boolean deleteConfig() {
        this.config = null;
        return file.delete();
    }
    public boolean regenerateConfig() throws IOException {
        deleteConfig();
        if (isInFolder) {
            process(plugin, file.getName(), file.getParentFile().getName(), true, false);
            return true;
        }
        process(plugin, file.getName(), true, false);
        return true;
    }

    public ConfigurationSection getConfigurationSection(String section) {
        return config.getConfigurationSection(section);
    }
    public @Nullable Set<String> getConfigurationSection(String section, boolean key) {
        if (getConfigurationSection(section) == null) return null;
        return getConfigurationSection(section).getKeys(key);
    }
    public @NotNull Set<String> getConfigurationSection(String section, boolean key, boolean notNull) throws IOException {
        ConfigurationSection configurationSection = getConfigurationSection(section);
        if (configurationSection == null) {
            config.createSection(section);
            save();
        }
        try {
            return configurationSection.getKeys(key);
        }catch (NullPointerException e) {
            config.createSection(section);
            save();
            return config.getConfigurationSection(section).getKeys(key);
        }
    }

    /**
     * Save the config.
     */
    public void save() throws IOException {
        config.save(file);
    }

    /**
     * Reload the config.
     * @throws IOException IOException
     */
    public void reload() throws IOException {
        logger.info("Reloading config for " + file.getName());
        if (isInFolder) {
            process(plugin, file.getName(), file.getParentFile().getName(), false, false);
            return;
        }
        process(plugin, file.getName(), false, false);
    }

    /**
     * Copy the config.
     * @param force boolean enable/disable force copy
     */
    public void copy( boolean force) {
        if (!file.exists()) {
            this.saveResource(file.getName(), force);
        }
    }
    /**
     * Copy the config to the given path.
     * @param force boolean enable/disable force copy
     * @param path String path to save the resource
     */
    public void copy( boolean force, String path) {
        this.saveResource(path, force);
    }

    /**
     * Update the Config with the newer version of the file
     * @param versionPath String
     */
    public void updateConfig(@NotNull String versionPath) {
        String version = this.getString(versionPath);
        if (version == null) {
            logger.info("No version found in " + file.getName() + ". Creating new version of the config");
        } else if (!version.equals(this.configVersion)) {
            Path backupPath = file.getParentFile().toPath().resolve("backup");
            Path newBackupFilePath = backupPath.resolve(String.format("old_%s_%s", version, file.getName()));

            backupConfigFile(newBackupFilePath);
            updateConfigFile();
        }
    }

    private void backupConfigFile(Path backupFilePath) {
        try {
            Files.createDirectories(backupFilePath.getParent());
            Files.move(file.toPath(), backupFilePath);
            logger.info("Backed up " + file.getName() + " to " + backupFilePath.toString());
        } catch (IOException e) {
            logger.warning("Failed to create backup file " + backupFilePath.toString());
        }
    }

    private void updateConfigFile() {
        if (isInFolder) {
            copy(true, corePath + File.separator + file.getName());
        } else copy(true);
        this.config = YamlConfiguration.loadConfiguration(this.file);
        logger.info("Updated " + file.getName() + " to version " + this.configVersion);
    }


    /**
     * update the config's existing path with given value.
     * @param path String
     * @param value Object
     * @throws IllegalArgumentException IOException
     */
    public void update(String path, Object value) throws IOException {
        if (config.contains(path)) {
            config.set(path, value);
            save();
        }else {
            throw new IllegalArgumentException(path + " does not exist" + " in " + file.getName());
        }
    }

    /**
     * Set the given path with given value. And save the config.
     * @param path String
     * @param value Object
     */
    public void set(String path, Object value) throws IOException {
        config.set(path, value);
        save();
    }

    /**
     * Get the given path.
     * @param path String
     * @return Object
     */
    public @Nullable Object get(String path) {
        if (config.contains(path)) {
            return config.get(path);
        }
        return null;
    }

    /**
     * Get the given path as a list.
     * @param path String
     * @return List
     */
    public List<String> getStringList(String path) {
        if (config.contains(path)) {
            return config.getStringList(path);
        }
        return new ArrayList<>();
    }

    /**
     * Add value to list of strings
     * @param path Path to add string with node
     * @param value String to add to list
     */
    public void addToStringList(String path, String value) throws IOException {
        if (config.contains(path) && config.isList(path)) {
            List<String> list = config.getStringList(path);
            list.add(value);
            config.set(path, list);
            save();
        }else {
            throw new IllegalArgumentException(path + " does not exist" + " in " + file.getName() + " or is not a list");
        }
    }
    /**
     * Get the given path as a boolean.
     * @param path String
     * @return boolean
     */
    public boolean getBoolean(String path) {
        return config.getBoolean(path);
    }

    /**
     * Get the given path as an int.
     * @param path String
     * @return int
     */
    public int getInt(String path) {
        return config.getInt(path);
    }

    /**
     * Get the given path as a double.
     * @param path String
     * @return double
     */
    public double getDouble(String path) {
        return config.getDouble(path);
    }

    /**
     * Get the given path as a long.
     * @param path String
     * @return long
     */
    public String getString(String path) {
        return config.getString(path);
    }

    public void saveResource(String resourcePath, boolean replace) {
        if (resourcePath == null || resourcePath.isEmpty()) {
            throw new IllegalArgumentException("ResourcePath cannot be null or empty");
        }

        resourcePath = resourcePath.replace('\\', '/');
        InputStream in = getResource(resourcePath);
        if (in == null) {
            throw new IllegalArgumentException("The embedded resource '" + resourcePath + "' cannot be found in " + file);
        }

        File outFile = new File(plugin.getDataFolder(), resourcePath);
        int lastIndex = resourcePath.lastIndexOf('/');
        File outDir = new File(plugin.getDataFolder(), resourcePath.substring(0, Math.max(lastIndex, 0)));

        if (!outDir.exists()) {
            if (outDir.mkdirs()) {
                logger.log(Level.INFO, "Created directory " + outDir);
            } else {
                logger.log(Level.WARNING, "Could not create directory " + outDir);
            }
        }

        try {
            if (!outFile.exists() || replace) {
                OutputStream out = Files.newOutputStream(outFile.toPath());
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                out.close();
                in.close();
            } else {
                logger.log(Level.WARNING, "Could not save " + outFile.getName() + " to " + outFile + " because " + outFile.getName() + " already exists.");
            }
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Could not save " + outFile.getName() + " to " + outFile, ex);
        }
    }

    public InputStream getResource(String filename) {
        if (filename == null) {
            throw new IllegalArgumentException("Filename cannot be null");
        }

        try {
            URL url = this.getClass().getClassLoader().getResource(filename);

            if (url == null) {
                return null;
            }

            URLConnection connection = url.openConnection();
            connection.setUseCaches(false);
            return connection.getInputStream();
        } catch (IOException ex) {
            return null;
        }
    }
}
