package cn.elytra.mod.rl.common;

/**
 * A wrapper of ICraftingLink, which controls the crafting process.
 */
public interface RemoteLoginCraftingHandle {

    boolean isDone();

    boolean isCanceled();

    void cancel();

}
