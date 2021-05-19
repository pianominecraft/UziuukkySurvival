package com.pianominecraft.survival

import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.*
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.event.player.*
import org.bukkit.inventory.*
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.lang.Exception
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.ArrayList

class Survival : JavaPlugin(), Listener {

    private val config = File(dataFolder, "survivors.txt")
    private val deaths = ArrayList<UUID>()

    override fun onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this)
        setupRecipe()
        if (!dataFolder.exists()) {
            dataFolder.mkdir()
        }
        if (!config.exists()) {
            config.createNewFile()
        }
        BufferedReader(FileReader(config)).use {
            list = it.readLines()
        }
        repeat {
            Bukkit.getOnlinePlayers().forEach {
                if (it.name in list) {
                    it.addPotionEffect(PotionEffect(PotionEffectType.SPEED, 5, 2, false, false))
                    Bukkit.getScoreboardManager().mainScoreboard.getTeam("survivor")?.addPlayer(it) ?: Bukkit.getScoreboardManager().mainScoreboard.registerNewTeam("survivor").apply { this.color = ChatColor.WHITE }
                }
                else {
                    Bukkit.getScoreboardManager().mainScoreboard.getTeam("hunter")?.addPlayer(it) ?: Bukkit.getScoreboardManager().mainScoreboard.registerNewTeam("hunter").apply { this.color = ChatColor.WHITE }
                }
            }
        } // team, speed
        repeat (60) {
            list.forEach { l ->
                Bukkit.getOnlinePlayers().forEach { online ->
                    if (online.name !in list) {
                        Bukkit.getPlayer(l)?.let {
                            with (it) {
                                if (this.location.distance(online.location) <= 50) {
                                    this.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent(text("%red%%bold%근처에 사냥꾼이 있습니다!")))
                                    this.playSound(this.location, Sound.BLOCK_NOTE_BLOCK_HARP, 1f, 2f)
                                }
                            }
                        }
                    }
                }
            }
        } // danger
        repeat (24000) {
            Bukkit.getOnlinePlayers().forEach { p ->
                p.sendMessage(text("%gold%생존자 좌표%white%가 도착했습니다!"))
                list.forEach { l ->
                    Bukkit.getPlayer(l)?.let {
                        if (it.world.name == "world") {
                            p.sendMessage(text("%green%${it.name} %white%: %green%오버월드 %white%/ (%green%${it.location.x.toInt()}%white%, %green%${it.location.y.toInt()}%white%, %green%${it.location.z.toInt()}%white%)"))
                        } else {
                            p.sendMessage(text("%green%${it.name} %white%: %green%네더 %white%/ (%green%${it.location.x.toInt()}%white%, %green%${it.location.y.toInt()}%white%, %green%${it.location.z.toInt()}%white%)"))
                        }
                    }
                }
                p.playSound(p.location, Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1f, 1f)
            }
        } // survivor position
    }

    private fun setupRecipe() {
        with (server) {
            this.addRecipe(
                ShapedRecipe(
                    NamespacedKey.minecraft("teambattle_blaze_powder"),
                    ItemStack(Material.BLAZE_POWDER)
                ).apply {
                    shape(
                        "QGQ",
                        "QGQ",
                        "QGQ"
                    )
                    setIngredient('G', Material.GOLD_INGOT)
                    setIngredient('Q', Material.QUARTZ)
                }
            )
            this.addRecipe(
                ShapelessRecipe(
                    NamespacedKey.minecraft("blaze_rod"),
                    ItemStack(Material.BLAZE_ROD)
                ).apply {
                    addIngredient(Material.BLAZE_POWDER)
                    addIngredient(Material.BLAZE_POWDER)
                }
            )
            this.addRecipe(
                ShapedRecipe(
                    NamespacedKey.minecraft("compacted_nether_brick"),
                    ItemStack(Material.NETHER_BRICK).apply {
                        itemMeta = itemMeta.apply {
                            setDisplayName(text("%aqua%압축된 네더 벽돌"))
                            addItemFlags(ItemFlag.HIDE_ENCHANTS)
                            addEnchant(Enchantment.MENDING, 1, false)
                        }
                    }
                ).apply {
                    shape(
                        "NNN",
                        "NNN",
                        "NNN"
                    )
                    setIngredient('N', Material.NETHER_BRICK)
                }
            )
            this.addRecipe(
                ShapelessRecipe(
                    NamespacedKey.minecraft("compacted_obsidian"),
                    ItemStack(Material.OBSIDIAN).apply {
                        itemMeta = itemMeta.apply {
                            setDisplayName(text("%aqua%압축된 흑요석"))
                            addItemFlags(ItemFlag.HIDE_ENCHANTS)
                            addEnchant(Enchantment.MENDING, 1, false)
                        }
                    }
                ).apply {
                    addIngredient(Material.OBSIDIAN)
                    addIngredient(Material.OBSIDIAN)
                    addIngredient(Material.OBSIDIAN)
                    addIngredient(Material.OBSIDIAN)
                }
            )
            this.addRecipe(
                ShapedRecipe(
                    NamespacedKey.minecraft("compacted_blaze_rod"),
                    ItemStack(Material.BLAZE_ROD).apply {
                        itemMeta = itemMeta.apply {
                            setDisplayName(text("%aqua%압축된 블레이즈 막대"))
                            addItemFlags(ItemFlag.HIDE_ENCHANTS)
                            addEnchant(Enchantment.MENDING, 1, false)
                        }
                    }
                ).apply {
                    shape(
                        " B ",
                        " B ",
                        " B "
                    )
                    setIngredient('B', Material.BLAZE_ROD)
                }
            )
            this.addRecipe(
                ShapedRecipe(
                    NamespacedKey.minecraft("teambattle_netherite"),
                    ItemStack(Material.NETHERITE_INGOT)
                ).apply {
                    shape(
                        "ONO",
                        "NBN",
                        "ONO"
                    )
                    setIngredient('O', RecipeChoice.ExactChoice(ItemStack(Material.OBSIDIAN).apply {
                        itemMeta = itemMeta.apply {
                            setDisplayName(text("%aqua%압축된 흑요석"))
                            addItemFlags(ItemFlag.HIDE_ENCHANTS)
                            addEnchant(Enchantment.MENDING, 1, false)
                        }
                    }))
                    setIngredient('N', RecipeChoice.ExactChoice(ItemStack(Material.NETHER_BRICK).apply {
                        itemMeta = itemMeta.apply {
                            setDisplayName(text("%aqua%압축된 네더 벽돌"))
                            addItemFlags(ItemFlag.HIDE_ENCHANTS)
                            addEnchant(Enchantment.MENDING, 1, false)
                        }
                    }))
                    setIngredient('B', RecipeChoice.ExactChoice(ItemStack(Material.BLAZE_ROD).apply {
                        itemMeta = itemMeta.apply {
                            setDisplayName(text("%aqua%압축된 블레이즈 막대"))
                            addItemFlags(ItemFlag.HIDE_ENCHANTS)
                            addEnchant(Enchantment.MENDING, 1, false)
                        }
                    }))
                }
            )
        } // Netherite
        with (server) {
            this.addRecipe(
                ShapedRecipe(
                    NamespacedKey.minecraft("enchanted_golden_apple_for_totem"),
                    ItemStack(Material.ENCHANTED_GOLDEN_APPLE)
                ).apply {
                    shape(
                        " G ",
                        "GAG",
                        " G "
                    )
                    setIngredient('G', Material.GOLD_BLOCK)
                    setIngredient('A', Material.APPLE)
                }
            )
            this.addRecipe(
                ShapedRecipe(
                    NamespacedKey.minecraft("totem"),
                    ItemStack(Material.TOTEM_OF_UNDYING)
                ).apply {
                    shape(
                        " E ",
                        "GAG",
                        " G "
                    )
                    setIngredient('E', Material.EMERALD)
                    setIngredient('G', Material.GOLD_INGOT)
                    setIngredient('A', Material.ENCHANTED_GOLDEN_APPLE)
                }
            )
        } // totem
    }

    @EventHandler
    fun onCraft(e: CraftItemEvent) {
        if (e.whoClicked.name !in list) {
            if (e.currentItem?.type == Material.ANVIL || e.currentItem?.type == Material.ENCHANTING_TABLE) {
                e.isCancelled = true
            }
        }
        if (e.inventory.result?.type == Material.NETHERITE_INGOT) {
            if (e.whoClicked.name !in list) {
                e.inventory.result = null
                e.inventory.matrix.forEach {
                    it.amount = it.amount - 1
                }
                e.whoClicked.sendMessage(text("%red%네더라이트 주괴가 일정 확률(100%)로 터졌습니다!"))
            }
        }
    }

    @EventHandler
    fun onDamage(e: EntityDamageByEntityEvent) {
        if (e.entity is Player) {
            if (e.damager is Player) {
                if (e.entity.name in list) {
                    if (e.damager.name in list) {
                        e.isCancelled = true
                        return
                    }
                } else if (e.damager.name !in list) {
                    if (e.damager.name !in list) {
                        e.isCancelled = true
                        return
                    }
                }
            }
        }
        if (e.cause == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION) {
            e.isCancelled = true
        }
        if (e.cause == EntityDamageEvent.DamageCause.FALL) {
            if (e.entity.name in list) {
                e.isCancelled = true
            }
        }
    }

    @EventHandler
    fun onPortal(e: PlayerPortalEvent) {
        if (e.cause == PlayerTeleportEvent.TeleportCause.END_PORTAL) {
            e.isCancelled = true
            e.player.sendMessage(text("엔더 월드는 이용할 수 없습니다"))
        }
    }

    @EventHandler
    fun onJoin(e: PlayerJoinEvent) {
        if (wl) {
            if (e.player.name !in list) {
                e.player.kickPlayer("아직 안돼")
            }
        }
        e.joinMessage = ""
        val x = (Math.random() * 1000).toInt() - 500
        val z = (Math.random() * 1000).toInt() - 500
        var y = 256
        while (e.player.world.getBlockAt(x, y - 1, z).type == Material.AIR) {
            y--
        }
        e.player.teleport(Location(Bukkit.getWorld("world"), x.toDouble(), y.toDouble(), z.toDouble()))
    }

    @EventHandler
    fun onRespawn(e: PlayerRespawnEvent) {
        if (e.player.name in list) {
            Bukkit.getOnlinePlayers().forEach { p ->
                p.sendTitle(text("%red%생존자 사망"), text("%red%${e.player.name}"), 20, 60, 20)
            }
            e.player.gameMode = GameMode.SPECTATOR
            e.player.sendTitle(text("%red%풉"), text("%gray%죽었대요~"), 20, 60, 20)
            deaths.add(e.player.uniqueId)
        } else {
            val x = (Math.random() * 1000).toInt() - 500
            val z = (Math.random() * 1000).toInt() - 500
            var y = 256
            while (e.player.world.getBlockAt(x, y - 1, z).type == Material.AIR) {
                y--
            }
            e.player.teleport(Location(Bukkit.getWorld("world"), x.toDouble(), y.toDouble(), z.toDouble()))
        }
    }

    @EventHandler
    fun onQuit(e: PlayerQuitEvent) {
        e.quitMessage = ""
    }

    @EventHandler
    fun onRightClick(e: PlayerInteractEvent) {
        if (e.player.name in list) {
            if (e.player.itemInHand.type == Material.BEACON) {
                if (e.action == Action.RIGHT_CLICK_BLOCK || e.action == Action.RIGHT_CLICK_AIR) {
                    Bukkit.getOnlinePlayers().forEach {
                        it.sendMessage("${ChatColor.GREEN}최종 목표${ChatColor.WHITE}를 ${ChatColor.GREEN}달성${ChatColor.WHITE}했습니다!")
                    }
                }
            }
            else if (e.player.itemInHand.type == Material.TOTEM_OF_UNDYING) {
                if (deaths.isNotEmpty()) {
                    val r = deaths.random()
                    if (Bukkit.getOfflinePlayer(r).isOnline) {
                        e.player.itemInHand.apply {
                            amount--
                        }
                        with (Bukkit.getPlayer(r)!!) {
                            e.player.sendTitle("${ChatColor.GOLD}소생!", "${ChatColor.GREEN}${this.name}님을 살렸습니다!", 0, 60, 40)
                            this.teleport(e.player)
                            this.gameMode = GameMode.SURVIVAL
                            Bukkit.getOnlinePlayers().forEach {
                                it.spawnParticle(Particle.TOTEM, e.player.eyeLocation, 50, 0.0, 0.0, 0.0, 0.5)
                                delay(4) {
                                    it.spawnParticle(Particle.TOTEM, e.player.eyeLocation, 50, 0.0, 0.0, 0.0, 0.5)
                                }
                                delay(8) {
                                    it.spawnParticle(Particle.TOTEM, e.player.eyeLocation, 50, 0.0, 0.0, 0.0, 0.5)
                                }
                                delay(12) {
                                    it.spawnParticle(Particle.TOTEM, e.player.eyeLocation, 50, 0.0, 0.0, 0.0, 0.5)
                                }
                                delay(16) {
                                    it.spawnParticle(Particle.TOTEM, e.player.eyeLocation, 50, 0.0, 0.0, 0.0, 0.5)
                                }
                                it.playSound(e.player.location, Sound.ITEM_TOTEM_USE, 1f, 1f)
                            }
                            this.sendTitle("${ChatColor.GOLD}부활!", "${ChatColor.GREEN}${e.player.name}님이 당신을 살렸습니다!", 0, 60, 40)
                        }
                        deaths.remove(r)
                    } else {
                        e.player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent("${ChatColor.RED}살릴 사람이 오프라인입니다. 다시 시도해 주세요."))
                    }
                } else {
                    e.player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent("${ChatColor.RED}살릴 사람이 없습니다."))
                }
            }
        }
    }

    @EventHandler
    fun onChat(e: AsyncPlayerChatEvent) {
        e.isCancelled = true
        if (e.player.name in list) {
            list.forEach { p ->
                Bukkit.getPlayer(p)?.sendMessage(text("%yellow%[Team Chating] %white%<${e.player.name}> ") + e.message)
            }
        } else {
            Bukkit.getOnlinePlayers().forEach { p ->
                if (p.name !in list) {
                    p.sendMessage(text("%yellow%[Team Chating] %white%<%red%${e.player.name}%white%> ") + e.message)
                }
            }
        }
    }

    override fun onCommand(s: CommandSender, c: Command, l: String, a: Array<out String>): Boolean {

        if (c.name.equals("start", ignoreCase = true)) {
            if (s.isOp) {
                val world = Bukkit.getWorld("world")!!
                val worldNether = Bukkit.getWorld("world_nether")!!
                world.worldBorder.setCenter(0.0, 0.0)
                worldNether.worldBorder.setCenter(0.0, 0.0)
                world.worldBorder.size = 1000.0
                worldNether.worldBorder.size = 1000.0
                world.setGameRule(GameRule.COMMAND_BLOCK_OUTPUT, false)
                world.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true)
                world.setGameRule(GameRule.SEND_COMMAND_FEEDBACK, false)
                world.setGameRule(GameRule.SHOW_DEATH_MESSAGES, false)
                world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false)
                worldNether.setGameRule(GameRule.COMMAND_BLOCK_OUTPUT, false)
                worldNether.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true)
                worldNether.setGameRule(GameRule.SEND_COMMAND_FEEDBACK, false)
                worldNether.setGameRule(GameRule.SHOW_DEATH_MESSAGES, false)
                worldNether.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false)
                Bukkit.getOnlinePlayers().forEach {
                    it.sendTitle("${ChatColor.GREEN}게임 시작", "${ChatColor.YELLOW}생존자들이 파밍을 시작합니다", 20, 60, 20)
                }
                delay(12000) {
                    Bukkit.getOnlinePlayers().forEach {
                        it.sendMessage(text("%gold%20분 %red%남음"))
                    }
                }
                delay(24000) {
                    Bukkit.getOnlinePlayers().forEach {
                        it.sendMessage(text("%gold%10분 %red%남음"))
                    }
                }
                delay(30000) {
                    Bukkit.getOnlinePlayers().forEach {
                        it.sendMessage(text("%gold%5분 %red%남음"))
                    }
                }
                delay(34800) {
                    Bukkit.getOnlinePlayers().forEach {
                        it.sendMessage(text("%gold%1분 %red%남음"))
                    }
                }
                delay(36000) {
                    Bukkit.getOnlinePlayers().forEach {
                        it.sendMessage(text("%dark_red%헌터가 등장하기 시작합니다"))
                        it.playSound(it.location, Sound.AMBIENT_CAVE, 1f, 1f)
                    }
                    wl = false
                }
            }
        }
        else if (c.name.equals("wlreload", ignoreCase = true)) {
            BufferedReader(FileReader(config)).use {
                list = it.readLines()
            }
        }
        else if (c.name.equals("wlopen", ignoreCase = true)) {
            wl = false
        }

        return false
    }

    private fun delay(delay: Long = 1, task: () -> Unit) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(this, task, delay)
    }
    private fun repeat(delay: Long = 1, task: () -> Unit) {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, task, 1, delay)
    }

}

var list = listOf(
    "Uziuukky",
    "pianominecraft",
    "_eepee_",
    "Rtrital",
    "OnlyJunyoung",
    "bkh1214",
    "SOORYE0N",
    "_mugum_",
    "Uz1uukky"
)

var wl = true

fun text(string: String) : String {
    var s = string
    val rgb = Pattern.compile("#[0-9a-f]{6}").matcher(string)
    while (rgb.find()) {
        try {
            s = s.replaceFirst(rgb.group(), net.md_5.bungee.api.ChatColor.of(rgb.group()).toString())
        } catch (e: Exception) {
        }
    }
    val color = Pattern.compile("%[a-zA-Z_]*%").matcher(string)
    while (color.find()) {
        try {
            s = s.replaceFirst(
                color.group(),
                net.md_5.bungee.api.ChatColor.of(color.group().replace("%", "")).toString()
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    return s
}