package cn.elytra.mod.rl;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.networking.crafting.ICraftingJob;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.me.GridAccessException;
import cn.elytra.mod.rl.common.RemoteLoginCraftingPlan;
import cn.elytra.mod.rl.entity.CraftingPlanInfo;
import cn.elytra.mod.rl.tile.RemoteLoginTile;
import cn.elytra.mod.rl.util.TypeConverter;

import java.util.ArrayList;
import java.util.List;

public class RemoteLoginCraftingPlanImpl implements RemoteLoginCraftingPlan {

    protected final RemoteLoginTile tile;
    protected final ICraftingJob result;

    protected final List<IAEItemStack> storage = new ArrayList<>();
    protected final List<IAEItemStack> pending = new ArrayList<>();
    protected final List<IAEItemStack> missing = new ArrayList<>();

    protected final boolean simulate;
    protected final long byteUsedTotal;

    public RemoteLoginCraftingPlanImpl(RemoteLoginTile tile, ICraftingJob result) {
        this.tile = tile;
        this.result = result;

        try {
            this.simulate = result.isSimulation();
            this.byteUsedTotal = result.getByteTotal();

            IItemList<IAEItemStack> plan = AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class).createList();
            result.populatePlan(plan);

            for(IAEItemStack out : plan) {
                IAEItemStack o = out.copy();
                o.reset();
                o.setStackSize(out.getStackSize());

                IAEItemStack p = out.copy();
                p.reset();
                p.setStackSize(out.getCountRequestable());

                IStorageGrid sg = tile.getProxy().getStorage();
                IMEMonitor<IAEItemStack> items = sg.getInventory(AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class));

                IAEItemStack m;

                m = o.copy();
                o = items.extractItems(o, Actionable.SIMULATE, tile.newThisActionSource());

                if(o == null) {
                    o = m.copy();
                    o.setStackSize(0);
                }

                m.setStackSize(m.getStackSize() - o.getStackSize());

                if(o.getStackSize() > 0) {
                    storage.add(o);
                }

                if(p.getStackSize() > 0) {
                    pending.add(p);
                }

                if(m.getStackSize() > 0) {
                    missing.add(m);
                }
            }
        } catch(GridAccessException e) {
            throw RemoteLoginExceptionImpl.wrap(e);
        }
    }

    @Override
    public void start() {
        if(result.isSimulation()) throw new IllegalArgumentException("The crafting plan is simulate!");

        try {
            ICraftingLink link = tile.getProxy().getCrafting()
                    .submitJob(result, null, null, true, tile.newThisActionSource());
            if(link == null) {
                throw new IllegalStateException("The crafting plan is not executed");
            }
        } catch(Exception e) {
            throw RemoteLoginExceptionImpl.wrap(e);
        }
    }

    @Override
    public CraftingPlanInfo getCraftingPlanInfo() {
        return new CraftingPlanInfo(
                simulate,
                byteUsedTotal,
                TypeConverter.itemStack2IrList(storage, true),
                TypeConverter.itemStack2IrList(pending, true),
                TypeConverter.itemStack2IrList(missing, true));
    }
}
