package org.mmga.fastmap.commands;

import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.mmga.fastmap.FastMap;
import org.mmga.fastmap.utils.UuidData;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created On 2022/7/7 20:26
 *
 * @author wzp
 * @version 1.0.0
 */
public class SummonT implements CommandExecutor {
    private final JavaPlugin plugin;
    private final Logger log;
    private final NamespacedKey numKey;
    private final NamespacedKey dataKey;
    private final NamespacedKey xKey;
    private final NamespacedKey yKey;
    private final NamespacedKey zKey;
    private final NamespacedKey nameKey;
    private final NamespacedKey uuidKey;


    public SummonT() {
        this.plugin = JavaPlugin.getPlugin(FastMap.class);
        this.log = plugin.getLogger();
        this.numKey = new NamespacedKey(plugin, "t_num");
        this.dataKey = new NamespacedKey(plugin, "t_data");
        this.xKey = new NamespacedKey(plugin, "x");
        this.yKey = new NamespacedKey(plugin, "y");
        this.zKey = new NamespacedKey(plugin, "z");
        this.nameKey = new NamespacedKey(plugin, "name");
        this.uuidKey = new NamespacedKey(plugin, "uuid");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player player) {
            if (args.length == 1) {
                World world = player.getWorld();
                Block targetBlock = player.getTargetBlock(20);
                assert targetBlock != null;
                if (targetBlock.getBlockData().getMaterial().isAir()) {
                    player.sendMessage(ChatColor.RED + "你指了个茄子！");
                } else {
                    Location location = targetBlock.getLocation();
                    Location add = location.add(0, 1, 0);
                    Chunk chunk = targetBlock.getChunk();
                    Entity entity = world.spawnEntity(add, EntityType.VILLAGER);
                    entity.addScoreboardTag("teacher");
                    String name = args[0];
                    for (ChatColor value : ChatColor.values()) {
                        name = name.replace(value.name(), value.toString());
                    }
                    entity.customName(Component.text(name));
                    Villager villager = (Villager) entity;
                    villager.setAI(false);
                    PersistentDataContainer chunkData = chunk.getPersistentDataContainer();
                    PersistentDataAdapterContext adapterContext = chunkData.getAdapterContext();
                    PersistentDataContainer thisData = adapterContext.newPersistentDataContainer();
                    thisData.set(xKey, PersistentDataType.INTEGER, add.getBlockX());
                    thisData.set(yKey, PersistentDataType.INTEGER, add.getBlockY());
                    thisData.set(zKey, PersistentDataType.INTEGER, add.getBlockZ());
                    thisData.set(nameKey, PersistentDataType.STRING, name);
                    thisData.set(uuidKey, UuidData.UUID_DATA, entity.getUniqueId());
                    PersistentDataContainer[] defaultData = {};
                    PersistentDataContainer[] teachersData = chunkData.getOrDefault(dataKey, PersistentDataType.TAG_CONTAINER_ARRAY, defaultData);
                    int teachersCount = chunkData.getOrDefault(numKey, PersistentDataType.INTEGER, 0);
                    chunkData.set(numKey, PersistentDataType.INTEGER, teachersCount + 1);
                    ArrayList<PersistentDataContainer> teachersDataArray = new ArrayList<>(List.of(teachersData));
                    teachersDataArray.add(thisData);
                    chunkData.set(dataKey, PersistentDataType.TAG_CONTAINER_ARRAY, teachersDataArray.toArray(new PersistentDataContainer[]{}));
                    player.sendMessage(ChatColor.GREEN + "添加成功！");
                }
            } else {
                player.sendMessage(ChatColor.RED + "这个村民不配拥有名字？");
            }
        } else {
            log.warning("我不C你爸我！");
        }
        return true;
    }
}
