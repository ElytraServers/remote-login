package cn.elytra.mod.rl.common;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface RemoteLoginConfig {

    default int getHttpServerPort() {
        return 14439;
    }

    @NotNull
    default String getHttpServerHost() {
        return "0.0.0.0";
    }

    /**
     * @return {@code true} to pretty-print the response JSON if applicable.
     */
    default boolean usePrettyPrintJsonResponse() {
        return false;
    }

    /**
     * @return the allowed origins for CORS, or {@code null} to allow any host.
     */
    @Nullable
    default List<String> getCorsAllowedOrigins() {
        return null;
    }

}
