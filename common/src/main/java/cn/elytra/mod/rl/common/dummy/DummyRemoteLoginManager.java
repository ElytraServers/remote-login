package cn.elytra.mod.rl.common.dummy;

import cn.elytra.mod.rl.common.RemoteLoginManager;
import cn.elytra.mod.rl.common.RemoteLoginAccessPoint;
import cn.elytra.mod.rl.entity.AccessPointInfo;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class DummyRemoteLoginManager implements RemoteLoginManager {

    @Override
    public @NotNull Collection<RemoteLoginAccessPoint> getAccessPoints() {
        return Collections.emptyList();
    }

    @Override
    public RemoteLoginAccessPoint getAccessPointByUuid(String uuid) {
        return null;
    }

}
