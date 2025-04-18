package cn.elytra.mod.rl.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.Nullable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemRepresentation {

    /**
     * The id of the item.
     * <p>
     * In some edge cases, it can be the numerical ID, but it should be deprecated.
     */
    private String itemId;
    private long amount;
    private int metadata;

    /**
     * The Base64 encoded NBT of the item.
     */
    @Nullable
    private String nbtBase64String;

    /**
     * {@code true} if it is craftable/requestable in the network. {@code null} if this is not applicable.
     */
    @Nullable
    private Boolean craftable;

    @Nullable
    private String iconBase64String;

    /**
     * A constructor for general items to add NBT tags later.
     */
    public ItemRepresentation(String itemId, long amount, int metadata) {
        this.itemId = itemId;
        this.amount = amount;
        this.metadata = metadata;
    }

    public int getAmountInt() {
        return (int) Math.min(Integer.MAX_VALUE, amount);
    }
}
