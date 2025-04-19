package cn.elytra.mod.rl.entity;

import lombok.Data;

import java.util.List;

@Data
public class CraftingPlanInfo {

    private final long byteUsedTotal;

    private final List<ItemRepresentation> storage;
    private final List<ItemRepresentation> pending;
    private final List<ItemRepresentation> missing;

}
