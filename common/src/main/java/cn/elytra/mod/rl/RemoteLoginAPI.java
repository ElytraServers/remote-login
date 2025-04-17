package cn.elytra.mod.rl;

import cn.elytra.mod.rl.common.RemoteLoginConfig;
import cn.elytra.mod.rl.common.RemoteLoginManager;
import cn.elytra.mod.rl.http.RemoteLoginHttpServer;
import lombok.experimental.UtilityClass;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

@UtilityClass
public class RemoteLoginAPI {

    public static final Logger LOGGER = LogManager.getLogger();

    @NotNull
    private static final RemoteLoginHttpServer server = new RemoteLoginHttpServer();

    @Nullable
    private static RemoteLoginManager manager = null;

    @NotNull
    private static RemoteLoginConfig config = new RemoteLoginConfig() {
    };

    public static void initAndStartServer() {
        // check manager before starting the server
        if(manager == null) {
            LOGGER.info("Missing manager, loading from ServiceLoader!");
            RemoteLoginManager m = RemoteLoginManager.loadService();
            if(m != null) {
                setManager(m);
            } else {
                throw new IllegalStateException("RemoteLoginManager is neither manually set nor loaded from ServiceLoader.");
            }
        }

        LOGGER.info("Starting Remote Login Http Server");
        server.start();
    }

    public static void stopServer() {
        LOGGER.info("Stopping Remote Login Http Server");
        server.stop();
    }

    @NotNull
    public static RemoteLoginManager getManager() {
        return Objects.requireNonNull(manager, "RemoteLoginAPI#manager is null");
    }

    public static void setManager(@NotNull RemoteLoginManager managerIn) {
        if(manager == null) {
            manager = managerIn;
        } else {
            throw new IllegalStateException("RemoteLoginManager is already set");
        }
    }

    @NotNull
    public static RemoteLoginConfig getConfig() {
        return config;
    }

    public static void setConfig(@NotNull RemoteLoginConfig config) {
        RemoteLoginAPI.config = config;
    }
}
