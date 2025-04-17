package cn.elytra.mod.rl;

import appeng.me.GridAccessException;
import cn.elytra.mod.rl.common.RemoteLoginException;

public class RemoteLoginExceptionImpl extends RemoteLoginException {

    public RemoteLoginExceptionImpl(Throwable cause) {
        super(cause);
    }

    @Override
    public boolean isGridAccessException() {
        return getCause() instanceof GridAccessException;
    }

    public static RemoteLoginException wrap(Throwable e) {
        return new RemoteLoginExceptionImpl(e);
    }
}
