package cn.elytra.mod.rl.common;

import org.jetbrains.annotations.NotNull;

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

}
