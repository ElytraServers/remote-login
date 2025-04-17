package cn.elytra.mod.rl.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CraftingPlan {

    long byteUsedTotal;

    List<ItemRepresentation> storage;
    List<ItemRepresentation> pending;
    List<ItemRepresentation> missing;

}
