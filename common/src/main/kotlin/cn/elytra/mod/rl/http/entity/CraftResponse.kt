package cn.elytra.mod.rl.http.entity

import cn.elytra.mod.rl.entity.CraftingPlanInfo

data class CraftResponse(
	val key: String,
	val info: CraftingPlanInfo,
)
