package cn.elytra.mod.rl.common.dummy;

import cn.elytra.mod.rl.common.RemoteLoginException;
import cn.elytra.mod.rl.common.RemoteLoginIconProvider;

public class DummyRemoteLoginIconProvider implements RemoteLoginIconProvider {

    @Override
    public String getItemIconBase64(String registryName, int metadata) throws RemoteLoginException {
        return BLACK_PURPLE_TEXTURE;
    }
}
