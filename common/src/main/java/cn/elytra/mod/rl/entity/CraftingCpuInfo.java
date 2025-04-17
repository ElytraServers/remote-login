package cn.elytra.mod.rl.entity;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class CraftingCpuInfo {

    public final boolean busy;
    public final long availableStorage;
    public final int coProcessors;
    public final String name;
    public final ItemRepresentation finalOutput;
    public final long remainingItemCount;
    public final long startItemCount;

    public Integer remainingOperations;

}
