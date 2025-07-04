package xyz.themis.discordSyncSource.config

import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.io.IOException
import java.util.logging.Logger

class ConfigManager(private val plugin: JavaPlugin) {
    val logger: Logger

    private var config: FileConfiguration
    var languageConfig: YamlConfiguration
    var playersConfig: YamlConfiguration

    private val configFile: File
    private val playersFile: File
    private val languageFile: File
    init {
        logger = plugin.logger

        // Folder check
        if (!plugin.dataFolder.exists()) {
            logger.info("DiscordSync folder not found, creating folder...")
            plugin.dataFolder.mkdirs()
        }

        // Config file check (config.yml)
        configFile = File(plugin.dataFolder, "config.yml")
        if (!configFile.exists()) {
            logger.info("config.yml not found, creating file...")
            plugin.saveDefaultConfig()
        }

        config = plugin.config

        // Language file check (language.yml)
        languageFile = File(plugin.dataFolder, "language.yml")
        if (!languageFile.exists()) {
            logger.info("language.yml not found, creating file...")
            plugin.saveResource("language.yml", false)
        }
        languageConfig = YamlConfiguration.loadConfiguration(languageFile)
        
        // Players file check (players.yml)
        playersFile = File(plugin.dataFolder, "players.yml")
        if (!playersFile.exists()) {
            plugin.saveResource("players.yml", false)
            playersConfig = YamlConfiguration.loadConfiguration(playersFile)
        }
        else {
            // players section check
            playersConfig = YamlConfiguration.loadConfiguration(playersFile)
            if (playersConfig.getConfigurationSection("players") == null) {
                playersConfig.createSection("players")
                savePlayersConfig()
                logger.info("There was no players section in players.yml, it was added.")
            }
            logger.info("players.yml loaded")
        }
    }

    fun getPlayersConfig(): FileConfiguration {
        return playersConfig
    }

    fun savePlayersConfig() {
        try {
            playersConfig.save(playersFile)
            logger.info("players.yml saved.")
        } catch (e: IOException) {
            logger.severe("players.yml couldn't save: " + e.message)
        }
    }
    fun getLanguageMessage(key: String): String {
        val lang: String = config.getString("language", "tr")!!
        val path = lang + "." + key
        val msg = languageConfig.getString(path)
        if (msg == null) {
            logger.warning("Message not found: " + path)
            return "Mesaj not found: " + key
        }
        return msg
    }
    fun reloadConfigs() {
        config = YamlConfiguration.loadConfiguration(configFile)
        languageConfig = YamlConfiguration.loadConfiguration(languageFile)
        playersConfig = YamlConfiguration.loadConfiguration(playersFile)
        if (playersConfig.getConfigurationSection("players") == null) {
            playersConfig.createSection("players")
            savePlayersConfig()
            logger.info("There was no players section in players.yml, it was added.")
        }
        logger.info("Config files reloaded")
    }
    fun getConfig(): FileConfiguration = config
}