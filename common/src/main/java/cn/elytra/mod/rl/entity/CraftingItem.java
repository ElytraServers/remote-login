package cn.elytra.mod.rl.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class CraftingItem {

    public ItemRepresentation item;

    public long countRequestable;
    public long countCurrent;
    public long countMissing;

}
