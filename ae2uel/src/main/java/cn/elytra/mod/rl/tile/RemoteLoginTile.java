package cn.elytra.mod.rl.tile;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.crafting.ICraftingJob;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import appeng.api.util.DimensionalCoord;
import appeng.crafting.CraftingJob;
import appeng.me.GridAccessException;
import appeng.me.helpers.MachineSource;
import appeng.tile.grid.AENetworkInvTile;
import appeng.util.inv.InvOperation;
import appeng.util.item.AEItemStack;
import cn.elytra.mod.rl.RemoteLoginExceptionImpl;
import cn.elytra.mod.rl.RemoteLoginTileManager;
import cn.elytra.mod.rl.common.RemoteLoginAccessPoint;
import cn.elytra.mod.rl.common.RemoteLoginCraftingHandle;
import cn.elytra.mod.rl.common.RemoteLoginException;
import cn.elytra.mod.rl.entity.AccessPointInfo;
import cn.elytra.mod.rl.entity.CraftingCpuInfo;
import cn.elytra.mod.rl.entity.CraftingPlan;
import cn.elytra.mod.rl.entity.ItemRepresentation;
import cn.elytra.mod.rl.util.IteratorUtils;
import cn.elytra.mod.rl.util.SecretHelper;
import cn.elytra.mod.rl.util.TypeConverter;
import com.google.common.collect.ImmutableSet;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.EmptyHandler;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class RemoteLoginTile extends AENetworkInvTile implements RemoteLoginAccessPoint {

    protected UUID uuid;
    protected String secret;

    // transient
    protected ICraftingJob lastCraftingJob;

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

    public ICraftingJob getLastCraftingJob() {
        return lastCraftingJob;
    }

    public void setLastCraftingJob(ICraftingJob lastCraftingJob) {
        this.lastCraftingJob = lastCraftingJob;
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

    protected IActionSource newThisActionSource() {
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
    public List<ItemRepresentation> getAllItemsInNetwork() throws RemoteLoginException {
        IItemList<IAEItemStack> itemList = getAEItemListInNetwork();
        return IteratorUtils.collectFromIterator(itemList.iterator()).stream()
                .map(TypeConverter::itemStack2Ir)
                .collect(Collectors.toList());
    }

    @Nullable
    protected CraftingJob makeCraftingJob(ItemRepresentation ir) throws GridAccessException {
        AEItemStack aeItemStack = TypeConverter.ir2AeItemStack(ir);
        return aeItemStack != null ? new CraftingJob(world, getProxy().getGrid(), new MachineSource(this), aeItemStack, null) : null;
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

    /**
     * Calculate the Crafting Plan from the given Crafting Job.
     *
     * @param futureJob the crafting job to wait and calculate
     * @return the calculated crafting plan data.
     */
    @Blocking
    protected CraftingPlan calculateToCraftingPlan(Future<ICraftingJob> futureJob) {
        try {
            ICraftingJob result = futureJob.get();

            // update last crafting job for later use
            this.setLastCraftingJob(result);

            // storage, pending, missing
            List<IAEItemStack> a = new ArrayList<>(), b = new ArrayList<>(), c = new ArrayList<>();

            IItemList<IAEItemStack> plan = AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class).createList();
            result.populatePlan(plan);

            long byteTotal = result.getByteTotal();

            for(IAEItemStack out : plan) {
                IAEItemStack o = out.copy();
                o.reset();
                o.setStackSize(out.getStackSize());

                IAEItemStack p = out.copy();
                p.reset();
                p.setStackSize(out.getCountRequestable());

                IStorageGrid sg = getProxy().getStorage();
                IMEMonitor<IAEItemStack> items = sg.getInventory(AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class));

                IAEItemStack m;

                m = o.copy();
                o = items.extractItems(o, Actionable.SIMULATE, newThisActionSource());

                if(o == null) {
                    o = m.copy();
                    o.setStackSize(0);
                }

                m.setStackSize(m.getStackSize() - o.getStackSize());

                if(o.getStackSize() > 0) {
                    a.add(o);
                }

                if(p.getStackSize() > 0) {
                    b.add(p);
                }

                if(m.getStackSize() > 0) {
                    c.add(m);
                }
            }

            return new CraftingPlan(byteTotal,
                    a.stream().map(TypeConverter::itemStack2Ir).collect(Collectors.toList()),
                    b.stream().map(TypeConverter::itemStack2Ir).collect(Collectors.toList()),
                    c.stream().map(TypeConverter::itemStack2Ir).collect(Collectors.toList()));
        } catch(GridAccessException e) {
            throw RemoteLoginExceptionImpl.wrap(e);
        } catch(InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public CompletableFuture<CraftingPlan> simulateCraftingPlan(ItemRepresentation ir) {
        Future<ICraftingJob> futureJob = makeCraftingJob(TypeConverter.ir2AeItemStack(ir));
        return CompletableFuture.supplyAsync(() -> calculateToCraftingPlan(futureJob));
    }

    protected ICraftingLink submitCraftingPlan(ICraftingJob result, @Nullable ICraftingCPU selectedCpu) {
        if(result.isSimulation()) return null;

        try {
            return getProxy().getCrafting().submitJob(result, null, selectedCpu, true, newThisActionSource());
        } catch(GridAccessException e) {
            throw RemoteLoginExceptionImpl.wrap(e);
        }
    }

    @Override
    public RemoteLoginCraftingHandle submitLastCraftingPlan() {
        if(getLastCraftingJob() == null) return null;
        ICraftingLink link = submitCraftingPlan(getLastCraftingJob(), null);
        if(link != null) setLastCraftingJob(null); // remove if it is deployed successfully
        return TypeConverter.wrapToCraftingHandle(link);
    }
}
