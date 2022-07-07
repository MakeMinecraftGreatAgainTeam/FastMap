package org.mmga.fastmap.event;

import io.papermc.paper.event.packet.PlayerChunkLoadEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.mmga.fastmap.FastMap;
import org.mmga.fastmap.utils.UuidData;
import org.slf4j.Logger;

import java.util.Objects;
import java.util.UUID;

/**
 * Created On 2022/7/7 20:15
 *
 * @author wzp
 * @version 1.0.0
 */
public class OnPlayerJoinChunkEvent implements Listener {
    private final JavaPlugin plugin;
    private final NamespacedKey teacherNumKey;
    private final NamespacedKey teacherDataKey;
    private final NamespacedKey xKey;
    private final NamespacedKey yKey;
    private final NamespacedKey zKey;
    private final NamespacedKey nameKey;
    private final NamespacedKey uuidKey;
    private final @NotNull Logger log;

    public OnPlayerJoinChunkEvent() {
        plugin = JavaPlugin.getPlugin(FastMap.class);
        this.teacherNumKey = new NamespacedKey(plugin, "t_num");
        this.teacherDataKey = new NamespacedKey(plugin, "t_data");
        this.xKey = new NamespacedKey(plugin, "x");
        this.yKey = new NamespacedKey(plugin, "y");
        this.zKey = new NamespacedKey(plugin, "z");
        this.nameKey = new NamespacedKey(plugin, "name");
        this.uuidKey = new NamespacedKey(plugin, "uuid");
        this.log = plugin.getSLF4JLogger();
    }

    @EventHandler
    public void onPlayerJoinChunk(PlayerChunkLoadEvent event) {
        Chunk chunk = event.getChunk();
        PersistentDataContainer chunkData = chunk.getPersistentDataContainer();
        Integer tCount = chunkData.get(teacherNumKey, PersistentDataType.INTEGER);
        if (!Objects.isNull(tCount) && tCount != 0) {
            @NotNull Entity[] entities = chunk.getEntities();
            int chunkTeacherCount = 0;
            for (Entity entity : entities) {
                if (entity.getScoreboardTags().contains("teacher")) {
                    chunkTeacherCount++;
                }
            }
            if (chunkTeacherCount < tCount) {
                int chunkX = chunk.getX();
                int chunkZ = chunk.getZ();
                log.warn("检测到区块{}-{}缺少村民{}只，开始补齐", chunkX, chunkZ, tCount - chunkTeacherCount);
                PersistentDataContainer[] teachersData = chunkData.get(teacherDataKey, PersistentDataType.TAG_CONTAINER_ARRAY);
                assert teachersData != null;
                World world = chunk.getWorld();
                for (PersistentDataContainer tData : teachersData) {
                    UUID uuid = tData.get(uuidKey, UuidData.UUID_DATA);
                    assert uuid != null;
                    Entity entity = world.getEntity(uuid);
                    if (Objects.isNull(entity)) {
                        String name = tData.get(nameKey, PersistentDataType.STRING);
                        log.warn("检测到区块{}-{}缺少村民：{}", chunkX, chunkZ, name);
                        Integer x = tData.get(xKey, PersistentDataType.INTEGER);
                        Integer y = tData.get(yKey, PersistentDataType.INTEGER);
                        Integer z = tData.get(zKey, PersistentDataType.INTEGER);
                        assert x != null;
                        assert y != null;
                        assert z != null;
                        assert name != null;
                        Entity villager = world.spawnEntity(new Location(world, x, y, z), EntityType.VILLAGER);
                        villager.customName(Component.text(name));
                        villager.addScoreboardTag("teacher");
                        Villager v = (Villager) villager;
                        v.setAI(false);
                        log.info("生成成功！");
                    }
                }
                log.info("补全成功！");
            }
        }
    }
}
