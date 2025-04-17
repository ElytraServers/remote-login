package cn.elytra.mod.rl.common;

import cn.elytra.mod.rl.RemoteLoginAPI;
import cn.elytra.mod.rl.entity.AccessPointInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

/**
 * The abstract of Remote Login Manager, which is singleton and managing all the AP instances by UUIDs.
 * <p>
 * The implementations should be manually registered to {@link RemoteLoginAPI} by {@link RemoteLoginAPI#setManager(RemoteLoginManager)}
 * or using {@link ServiceLoader} for convenience.
 */
public interface RemoteLoginManager {

    /**
     * Get the currently active Access Points maintained by this manager.
     *
     * @return the access point list.
     */
    @NotNull
    @UnmodifiableView
    Collection<? extends RemoteLoginAccessPoint> getAccessPoints();

    @Nullable
    RemoteLoginAccessPoint getAccessPointByUuid(String uuid);

    @NotNull
    default List<AccessPointInfo> getRemoteLoginInfoList() {
        return getAccessPoints().stream().map(RemoteLoginAccessPoint::getInfo).collect(Collectors.toList());
    }

    /**
     * Load the very first found {@link RemoteLoginManager} by {@link ServiceLoader}.
     *
     * @return the very first found instance, or {@code null}.
     */
    static RemoteLoginManager loadService() {
        ServiceLoader<RemoteLoginManager> sl = ServiceLoader.load(RemoteLoginManager.class);

        Iterator<RemoteLoginManager> iter = sl.iterator();
        if(iter.hasNext()) {
            return iter.next();
        }

        return null;
    }

}
