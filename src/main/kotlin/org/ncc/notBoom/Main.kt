package org.ncc.notBoom

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.Tag
import org.bukkit.World.Environment
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.EntityType
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

class Main : JavaPlugin(), CommandExecutor, Listener {
    val configFile: File = File(dataFolder, "config.yml")
    var tmpB: Boolean = false
    val defaultConfigMap = mapOf(
        "creeper" to false,
        "tnt" to false,
        "tnt_minecart" to false,
        "ghast_fireball" to false,
        "wither_skull" to false,
        "wither" to false,
        "ender_crystal" to false,
        "bed" to false
    )
    val currentConfigMap = defaultConfigMap.toMutableMap()
    val entityTypeToConfigNameMap = mapOf(
        EntityType.CREEPER to "creeper",
        EntityType.PRIMED_TNT to "tnt",
        EntityType.MINECART_TNT to "tnt_minecart",
        EntityType.SMALL_FIREBALL to "ghast_fireball",
        EntityType.WITHER to "wither",
        EntityType.WITHER_SKULL to "wither_skull",
        EntityType.ENDER_CRYSTAL to "ender_crystal"
    )

    val entityTypeSet = setOf(
        EntityType.CREEPER,
        EntityType.PRIMED_TNT,
        EntityType.MINECART_TNT,
        EntityType.SMALL_FIREBALL,
        EntityType.WITHER_SKULL,
        EntityType.WITHER,
        EntityType.ENDER_CRYSTAL
    )

    override fun onEnable() {
        if (!configFile.exists()) {
            if (!configFile.parentFile.exists()) {
                configFile.parentFile.mkdirs()
            }
            configFile.createNewFile()
            tmpB = true
        }
        defaultConfigMap.forEach { (key, value) ->
            run {
                if (config.get(key) == null) {
                    config.set(key, value)
                    tmpB = true
                } else {
                    currentConfigMap[key] = config.getBoolean(key, value)
                }
            }
        }
        if (tmpB) {
            saveConfig()
        }
        Bukkit.getPluginManager().registerEvents(this, this)

    }

    override fun onDisable() {

    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        if (!sender.hasPermission("notboom.usage")) {
            sender.sendMessage("${NamedTextColor.RED}You have no permission to use this command!")
            return true
        }
        reloadConfig()
        val tmpMap = currentConfigMap.toMap()
        tmpMap.forEach { (key, value) ->
            run {
                currentConfigMap[key] = config.getBoolean(key, false)
            }
        }
        sender.sendMessage("Plugin config reloaded!")
        return true
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onEntityExplode(event: EntityExplodeEvent) {
        if (event.entityType in entityTypeSet && currentConfigMap.get(entityTypeToConfigNameMap.get(event.entityType)) == true) {
            event.isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onPlayerEnterBed(event: PlayerInteractEvent) {
        if (event.action != Action.RIGHT_CLICK_BLOCK) return
        if ((event.player.world.environment == Environment.NETHER || event.player.world.environment == Environment.THE_END) && currentConfigMap.get(
                "bed"
            )!! && Tag.BEDS.isTagged(event.clickedBlock!!.type)
        ) {
            event.isCancelled = true
            event.player.sendMessage(
                Component.text(
                    "Bed Boom in the ${
                        event.player.world.name.replace(
                            "world_",
                            ""
                        )
                    } is not allowed in the server", NamedTextColor.RED
                )
            )
        }
    }

}
