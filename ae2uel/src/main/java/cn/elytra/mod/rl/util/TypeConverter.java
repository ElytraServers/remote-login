package cn.elytra.mod.rl.util;

import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.DimensionalCoord;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.util.item.AEItemStack;
import cn.elytra.mod.rl.RemoteLoginAPI;
import cn.elytra.mod.rl.common.RemoteLoginCraftingHandle;
import cn.elytra.mod.rl.entity.AccessPointInfo;
import cn.elytra.mod.rl.entity.CraftingCpuInfo;
import cn.elytra.mod.rl.entity.DimAndPos;
import cn.elytra.mod.rl.entity.ItemRepresentation;
import cn.elytra.mod.rl.tile.RemoteLoginTile;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A utility class to convert objects.
 */
public class TypeConverter {

    public static boolean DEBUG = Boolean.getBoolean("remote_login.debug") ||
            Boolean.getBoolean("remote_login.debug.type_converter");

    private static final Logger LOGGER = LogManager.getLogger();

    @SuppressWarnings("DataFlowIssue")
    private static String getItemId(ItemStack s) {
        return s.getItem().getRegistryName().toString();
    }

    public static DimAndPos toDimAndPos(DimensionalCoord dc) {
        return new DimAndPos(dc.getWorld().provider.getDimension(), dc.x, dc.y, dc.z);
    }

    public static CraftingCpuInfo toCraftingCpuInfo(ICraftingCPU cpu) {
        CraftingCpuInfo info = new CraftingCpuInfo(cpu.isBusy(), cpu.getAvailableStorage(), cpu.getCoProcessors(), cpu.getName(), itemStack2Ir(cpu.getFinalOutput(), false), cpu.getRemainingItemCount(), cpu.getStartItemCount());
        if(cpu instanceof CraftingCPUCluster) {
            info.remainingOperations = ReflectionUtils.<Integer>getFieldValueSafe(cpu, "remainingOperations", null);
        }
        return info;
    }

    public static AccessPointInfo toAccessPointInfo(RemoteLoginTile tile) {
        AccessPointInfo info = new AccessPointInfo(tile.getUuid().toString(), toDimAndPos(tile.getLocation()));
        if(tile.hasCustomInventoryName()) {
            info.setName(tile.getCustomInventoryName());
        }
        return info;
    }

    public static NBTTagCompound encodedString2Nbt(String s) throws IOException {
        ByteArrayInputStream is = new ByteArrayInputStream(Base64.getDecoder().decode(s));
        try(DataInputStream dis = new DataInputStream(is)) {
            return CompressedStreamTools.read(dis);
        }
    }

    public static String nbt2EncodedString(NBTTagCompound nbt) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try(DataOutputStream dos = new DataOutputStream(os)) {
            CompressedStreamTools.write(nbt, dos);
            return Base64.getEncoder().encodeToString(os.toByteArray());
        }
    }

    @Nullable
    public static ItemStack ir2ItemStack(ItemRepresentation ir) {
        Item item = Item.getByNameOrId(ir.getItemId());
        if(item == null) return null;

        ItemStack stack = new ItemStack(item, ir.getAmountInt(), ir.getMetadata());
        if(ir.getNbtBase64String() != null) {
            try {
                stack.setTagCompound(encodedString2Nbt(ir.getNbtBase64String()));
            } catch(IOException e) {
                if(DEBUG) {
                    LOGGER.error("Unable to deserialize the NBT data from IR {}", ir);
                }
            }
        }

        return stack;
    }

    public static AEItemStack ir2AeItemStack(ItemRepresentation ir) {
        ItemStack itemStack = ir2ItemStack(ir);
        if(itemStack == null) return null;

        // AEItemStack#fromItemStack() will return null if the stack is empty (sized 0 or air),
        // so we need to cache the amount and force it to 1, and reassign the count.
        int count = itemStack.getCount();
        itemStack.setCount(1);
        AEItemStack aeItemStack = AEItemStack.fromItemStack(itemStack);
        if(aeItemStack == null) return null;
        aeItemStack.setStackSize(count);
        return aeItemStack;
    }

    @Contract("null, _ -> null")
    public static ItemRepresentation itemStack2Ir(ItemStack stack, boolean renderItem) {
        if(stack == null) return null;

        ItemRepresentation ir = new ItemRepresentation(getItemId(stack), stack.getCount(), stack.getMetadata());
        if(stack.hasTagCompound()) {
            try {
                ir.setNbtBase64String(nbt2EncodedString(stack.getTagCompound()));
            } catch(IOException e) {
                if(DEBUG) {
                    LOGGER.error("Unable to serialize the NBT data from item {} (String-form: {})", stack, stack.getTagCompound(), e);
                }
            }
        }
        if(renderItem) {
            ir.setIconBase64String(RemoteLoginAPI.getIconProvider().getItemIconBase64(ir));
        }
        return ir;
    }

    public static ItemRepresentation itemStack2Ir(ItemStack stack) {
        return itemStack2Ir(stack, false);
    }

    @Contract("null, _ -> null")
    public static ItemRepresentation itemStack2Ir(IAEItemStack aeStack, boolean renderItem) {
        if(aeStack == null) return null;

        ItemStack def = aeStack.getDefinition();
        ItemRepresentation ir = new ItemRepresentation(getItemId(def), aeStack.getStackSize(), def.getMetadata());
        if(def.hasTagCompound()) {
            try {
                ir.setNbtBase64String(nbt2EncodedString(def.getTagCompound()));
            } catch(IOException e) {
                if(DEBUG) {
                    LOGGER.error("Failed to serialize the NBT data from item {} (String-form: {})", def, def.getTagCompound(), e);
                }
            }
        }
        if(renderItem) {
            ir.setIconBase64String(RemoteLoginAPI.getIconProvider().getItemIconBase64(ir));
        }

        // AEItemStack additions
        ir.setCraftable(aeStack.isCraftable());

        return ir;
    }

    public static ItemRepresentation itemStack2Ir(IAEItemStack aeStack) {
        return itemStack2Ir(aeStack, false);
    }

    public static List<ItemRepresentation> itemStack2IrList(List<IAEItemStack> aeStacks) {
        return aeStacks.stream().map(TypeConverter::itemStack2Ir).collect(Collectors.toList());
    }

    public static List<ItemRepresentation> itemStack2IrList(List<IAEItemStack> aeStacks, boolean renderItem) {
        return aeStacks.stream().map(i -> itemStack2Ir(i, renderItem)).collect(Collectors.toList());
    }

    private static class CraftingLinkWrapper implements RemoteLoginCraftingHandle {
        public final ICraftingLink link;

        public CraftingLinkWrapper(ICraftingLink link) {
            this.link = link;
        }

        @Override
        public boolean isDone() {
            return link.isDone();
        }

        @Override
        public boolean isCanceled() {
            return link.isCanceled();
        }

        @Override
        public void cancel() {
            link.cancel();
        }
    }

    @Contract("null -> null")
    public static RemoteLoginCraftingHandle wrapToCraftingHandle(ICraftingLink link) {
        return link != null ? new CraftingLinkWrapper(link) : null;
    }
}
