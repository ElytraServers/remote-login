package cn.elytra.mod.rl.config;

import cn.elytra.mod.rl.common.RemoteLoginConfig;
import net.minecraftforge.common.config.Configuration;
import org.jetbrains.annotations.NotNull;

public class RemoteLoginConfigForge implements RemoteLoginConfig {

    public final Configuration config;

    public RemoteLoginConfigForge(Configuration config) {
        this.config = config;
        init();
        this.config.save();
    }

    protected void init() {
        getHttpServerPort();
        getHttpServerHost();
        usePrettyPrintJsonResponse();
    }

    @Override
    public int getHttpServerPort() {
        return config.getInt("port", "http", 14439, 0, Short.MAX_VALUE, "the port of the http server listening to");
    }

    @NotNull
    @Override
    public String getHttpServerHost() {
        return config.getString("host", "http", "0.0.0.0", "the host address of the http server listening to");
    }

    @Override
    public boolean usePrettyPrintJsonResponse() {
        return config.getBoolean("use-pretty-print", "http", false, "if true and applicable, the response JSON will be pretty-printed");
    }
}
