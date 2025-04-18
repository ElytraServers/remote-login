package cn.elytra.mod.rl.common;

import cn.elytra.mod.rl.entity.AccessPointInfo;
import cn.elytra.mod.rl.entity.CraftingCpuInfo;
import cn.elytra.mod.rl.entity.CraftingPlan;
import cn.elytra.mod.rl.entity.ItemRepresentation;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * The abstract of the AP tiles, providing the functionalities to http servers.
 * <p>
 * The instances of this are supposed to be got from {@link RemoteLoginManager} with UUIDs.
 * <p>
 * The operations are not protected with access control, so remember to check it by {@link #isSecretValid(String)}
 * before calling the methods.
 * <p>
 * It is supposed to ignore GridAccessException, which is not accessible in common.
 * But you can check if the exception is caused by GAE by {@link RemoteLoginException#isGridAccessException()}.
 *
 * @see RemoteLoginException
 * @see RemoteLoginManager
 */
public interface RemoteLoginAccessPoint {

    /**
     * Check if the given secret is matching or valid to the tile.
     * <p>
     * Used for access control. TODO: Not implemented yet.
     *
     * @param secret the given secret
     * @return {@code true} if valid.
     * @throws RemoteLoginException if any exception thrown.
     */
    boolean isSecretValid(String secret) throws RemoteLoginException;

    /**
     * Get the information of this Access Point.
     *
     * @return the information object
     * @throws RemoteLoginException if any exception thrown.
     */
    AccessPointInfo getInfo() throws RemoteLoginException;

    /**
     * Collect the information of Crafting CPUs of this network.
     *
     * @return the information list
     * @throws RemoteLoginException if any exception thrown.
     */
    Collection<CraftingCpuInfo> getCraftingCpuInfoList() throws RemoteLoginException;

    /**
     * Collect all the items in the network, including the requestable ones with zero amounts.
     *
     * @return the items.
     * @throws RemoteLoginException if any exception thrown.
     */
    default List<ItemRepresentation> getAllItemsInNetwork() throws RemoteLoginException {
        return getAllItemsInNetwork(false);
    }

    /**
     * Collect all the items in the network, including the requestable ones with zero amounts.
     * <p>
     * If renderItem is {@code true}, the Item Representations will have their iconBase64String set.
     *
     * @param renderItem {@code true} to render item icons.
     * @return the items.
     * @throws RemoteLoginException if any exception thrown.
     */
    List<ItemRepresentation> getAllItemsInNetwork(boolean renderItem) throws RemoteLoginException;

    /**
     * Get the crafting plan for the given item, asynchronized.
     *
     * @param ir the given item
     * @return the CompletableFuture of the crafting plan.
     */
    CompletableFuture<CraftingPlan> simulateCraftingPlan(ItemRepresentation ir);

    /**
     * Submit the crafting job of the last simulated one.
     *
     * @return the crafting job handler, or {@code null} if either the last job is absent or invalid.
     */
    RemoteLoginCraftingHandle submitLastCraftingPlan();
}
