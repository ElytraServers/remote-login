package cn.elytra.mod.rl.http.entity

data class RLResponseException(val message: String, val code: Int = C_UNKNOWN) {
	@Suppress("unused")
	companion object {
		const val C_UNKNOWN: Int = 0
		const val C_GRID_ACCESS_EXCEPTION: Int = 1
		const val C_PRESERVED_2: Int = 2
		const val C_INVALID_SECRET: Int = 3
	}
}
