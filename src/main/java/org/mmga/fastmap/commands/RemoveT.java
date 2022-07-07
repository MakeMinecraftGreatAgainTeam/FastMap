package org.mmga.fastmap.commands;

import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.mmga.fastmap.FastMap;
import org.mmga.fastmap.utils.UuidData;

import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Created On 2022/7/7 23:03
 *
 * @author wzp
 * @version 1.0.0
 */
public class RemoveT implements CommandExecutor {
    private final JavaPlugin plugin;
    private final Logger log;
    private final NamespacedKey numKey;
    private final NamespacedKey dataKey;
    private final NamespacedKey uuidKey;

    public RemoveT() {
        this.plugin = JavaPlugin.getPlugin(FastMap.class);
        this.log = plugin.getLogger();
        this.numKey = new NamespacedKey(plugin, "t_num");
        this.dataKey = new NamespacedKey(plugin, "t_data");
        this.uuidKey = new NamespacedKey(plugin, "uuid");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player player) {
            Entity targetEntity = player.getTargetEntity(20);
            if (Objects.isNull(targetEntity)) {
                player.sendMessage(ChatColor.RED + "你指了个茄子！");
            } else {
                if (EntityType.VILLAGER.equals(targetEntity.getType()) && targetEntity.getScoreboardTags().contains("teacher")) {
                    UUID uniqueId = targetEntity.getUniqueId();
                    Chunk chunk = player.getChunk();
                    PersistentDataContainer chunkData = chunk.getPersistentDataContainer();
                    Integer count = chunkData.get(numKey, PersistentDataType.INTEGER);
                    assert count != null;
                    chunkData.set(numKey, PersistentDataType.INTEGER, count - 1);
                    PersistentDataContainer[] teachers = chunkData.get(dataKey, PersistentDataType.TAG_CONTAINER_ARRAY);
                    ArrayList<PersistentDataContainer> result = new ArrayList<>();
                    assert teachers != null;
                    for (PersistentDataContainer teacher : teachers) {
                        if (!uniqueId.equals(teacher.get(uuidKey, UuidData.UUID_DATA))) {
                            result.add(teacher);
                        }
                    }
                    chunkData.set(dataKey, PersistentDataType.TAG_CONTAINER_ARRAY, result.toArray(new PersistentDataContainer[]{}));
                    targetEntity.remove();
                    player.sendMessage("成功删除此村民");
                }
            }
        } else {
            log.warning("我不C你爸我！");
        }
        return true;
    }
}
