package cn.elytra.mod.rl.common;

import cn.elytra.mod.rl.entity.CraftingPlanInfo;

public interface RemoteLoginCraftingPlan {

    /**
     * Start the crafting plan.
     */
    void start();

    /**
     * Create crafting plan info from this plan.
     *
     * @return the info.
     */
    CraftingPlanInfo getCraftingPlanInfo();

}
