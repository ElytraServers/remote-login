package cn.elytra.mod.rl.tile;

import appeng.api.AEApi;
import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.crafting.ICraftingJob;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import appeng.api.util.DimensionalCoord;
import appeng.me.GridAccessException;
import appeng.me.helpers.MachineSource;
import appeng.tile.grid.AENetworkInvTile;
import appeng.util.inv.InvOperation;
import cn.elytra.mod.rl.RemoteLoginCraftingPlanImpl;
import cn.elytra.mod.rl.RemoteLoginExceptionImpl;
import cn.elytra.mod.rl.RemoteLoginTileManager;
import cn.elytra.mod.rl.common.RemoteLoginAccessPoint;
import cn.elytra.mod.rl.common.RemoteLoginCraftingPlan;
import cn.elytra.mod.rl.common.RemoteLoginException;
import cn.elytra.mod.rl.entity.AccessPointInfo;
import cn.elytra.mod.rl.entity.CraftingCpuInfo;
import cn.elytra.mod.rl.entity.ItemRepresentation;
import cn.elytra.mod.rl.util.IteratorUtils;
import cn.elytra.mod.rl.util.SecretHelper;
import cn.elytra.mod.rl.util.TypeConverter;
import cn.elytra.mod.rl.util.Utils;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableSet;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.EmptyHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class RemoteLoginTile extends AENetworkInvTile implements RemoteLoginAccessPoint {

    protected UUID uuid;
    protected String secret;

    // transient
    @NotNull
    protected final Cache<String, RemoteLoginCraftingPlan> craftingPlanCache = CacheBuilder.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES).maximumSize(128).build();

    public RemoteLoginTile() {
        reset();
    }

    public void reset() {
        setUuid(UUID.randomUUID());
        setSecret(SecretHelper.generate());
        markDirty();
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
        // notify the manager that the uuid has been changed
        RemoteLoginTileManager.INSTANCE.updateUuidCache(this);
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    @Override
    public void validate() {
        super.validate();
        if(!world.isRemote) {
            RemoteLoginTileManager.INSTANCE.add(this);
        }
    }

    @Override
    public void invalidate() {
        super.invalidate();
        if(!world.isRemote) {
            RemoteLoginTileManager.INSTANCE.remove(this);
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.uuid = data.getUniqueId("uuid");
        this.secret = data.getString("secret");
    }

    @Override
    @NotNull
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        data.setUniqueId("uuid", this.uuid);
        data.setString("secret", this.secret);
        return super.writeToNBT(data);
    }

    public IActionSource newThisActionSource() {
        return new MachineSource(this);
    }

    @Override
    public DimensionalCoord getLocation() {
        return new DimensionalCoord(this);
    }

    @NotNull
    @Override
    public AECableType getCableConnectionType(@NotNull AEPartLocation aePartLocation) {
        return AECableType.SMART;
    }

    @NotNull
    @Override
    public IItemHandler getInternalInventory() {
        return EmptyHandler.INSTANCE;
    }

    @Override
    public void onChangeInventory(IItemHandler inv, int slot, InvOperation mc, ItemStack removed, ItemStack added) {
    }

    @Override
    public boolean isSecretValid(String secret) {
        return this.secret.equals(secret);
    }

    @Override
    public AccessPointInfo getInfo() throws RemoteLoginException {
        return TypeConverter.toAccessPointInfo(this);
    }

    protected ImmutableSet<ICraftingCPU> getCpus() throws RemoteLoginException {
        try {
            return getProxy().getCrafting().getCpus();
        } catch(Throwable e) {
            throw RemoteLoginExceptionImpl.wrap(e);
        }
    }

    @Override
    public Collection<CraftingCpuInfo> getCraftingCpuInfoList() throws RemoteLoginException {
        return getCpus().stream().map(TypeConverter::toCraftingCpuInfo).collect(Collectors.toList());
    }

    protected IItemList<IAEItemStack> getAEItemListInNetwork() throws RemoteLoginException {
        try {
            IMEMonitor<IAEItemStack> inventory = getProxy().getStorage().getInventory(AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class));
            return inventory.getStorageList();
        } catch(GridAccessException e) {
            throw RemoteLoginExceptionImpl.wrap(e);
        }
    }

    @Override
    public List<ItemRepresentation> getAllItemsInNetwork(boolean renderItem) throws RemoteLoginException {
        IItemList<IAEItemStack> itemList = getAEItemListInNetwork();
        return IteratorUtils.collectFromIterator(itemList.iterator()).stream()
                .map(aeStack -> TypeConverter.itemStack2Ir(aeStack, renderItem))
                .collect(Collectors.toList());
    }

    public Future<ICraftingJob> makeCraftingJob(IAEItemStack itemToCraft) {
        try {
            IGrid grid = getProxy().getGrid();
            ICraftingGrid craftingGrid = getProxy().getCrafting();
            IActionSource actionSource = new MachineSource(this);
            return craftingGrid.beginCraftingJob(world, grid, actionSource, itemToCraft, null);
        } catch(GridAccessException e) {
            throw RemoteLoginExceptionImpl.wrap(e);
        }
    }

    @Override
    public CompletableFuture<RemoteLoginCraftingPlan> getCraftingPlan(ItemRepresentation ir) {
        Future<ICraftingJob> futureJob = makeCraftingJob(TypeConverter.ir2AeItemStack(ir));
        return Utils.waitFutureAndThen(futureJob, (result) -> new RemoteLoginCraftingPlanImpl(this, result));
    }

    @Override
    public String putCraftingPlanCache(RemoteLoginCraftingPlan plan) {
        String key = UUID.randomUUID().toString();
        craftingPlanCache.put(key, plan);
        return key;
    }

    @Nullable
    @Override
    public RemoteLoginCraftingPlan getCraftingPlanCache(String key) {
        return craftingPlanCache.getIfPresent(key);
    }
}
