package cn.elytra.mod.rl;

import cn.elytra.mod.rl.common.RemoteLoginAccessPoint;
import cn.elytra.mod.rl.common.RemoteLoginManager;
import cn.elytra.mod.rl.tile.RemoteLoginTile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.*;

public class RemoteLoginTileManager implements RemoteLoginManager {

    public static final RemoteLoginTileManager INSTANCE = new RemoteLoginTileManager();

    private static final Logger LOGGER = LogManager.getLogger();

    protected final List<RemoteLoginTile> tiles = new ArrayList<>();
    protected final Map<String, WeakReference<RemoteLoginTile>> uuidCache = new HashMap<>();

    private RemoteLoginTileManager() {
    }

    public void add(RemoteLoginTile tile) {
        synchronized(tiles) {
            tiles.add(tile);
            uuidCache.put(tile.getUuid().toString(), new WeakReference<>(tile));
            LOGGER.info("RemoteLoginTile added: {}", tile.getLocation());
        }
    }

    public void remove(RemoteLoginTile tile) {
        synchronized(tiles) {
            tiles.remove(tile);
            uuidCache.remove(tile.getUuid().toString());
            LOGGER.info("RemoteLoginTile removed: {}", tile.getLocation());
        }
    }

    public void updateUuidCache(RemoteLoginTile tile) {
        uuidCache.put(tile.getUuid().toString(), new WeakReference<>(tile));
    }

    @Override
    public @NotNull Collection<? extends RemoteLoginAccessPoint> getAccessPoints() {
        return tiles;
    }

    @Override
    public RemoteLoginTile getAccessPointByUuid(String uuid) {
        return uuidCache.get(uuid).get();
    }

}
