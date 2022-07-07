package org.mmga.fastmap.utils;

import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Created On 2022/7/7 21:55
 *
 * @author wzp
 * @version 1.0.0
 */
public class UuidData implements PersistentDataType<long[], UUID> {
    public final static UuidData UUID_DATA = new UuidData();

    @Override
    public @NotNull Class<long[]> getPrimitiveType() {
        return long[].class;
    }

    @Override
    public @NotNull Class<UUID> getComplexType() {
        return UUID.class;
    }

    @Override
    public long @NotNull [] toPrimitive(@NotNull UUID complex, @NotNull PersistentDataAdapterContext context) {
        long leastSignificantBits = complex.getLeastSignificantBits();
        long mostSignificantBits = complex.getMostSignificantBits();
        return new long[]{leastSignificantBits, mostSignificantBits};
    }

    @Override
    public @NotNull UUID fromPrimitive(long @NotNull [] primitive, @NotNull PersistentDataAdapterContext context) {
        return new UUID(primitive[1], primitive[0]);
    }
}
