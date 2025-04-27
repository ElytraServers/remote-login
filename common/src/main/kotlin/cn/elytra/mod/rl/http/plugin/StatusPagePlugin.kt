package cn.elytra.mod.rl.http.plugin

import cn.elytra.mod.rl.RemoteLoginAPI
import cn.elytra.mod.rl.common.RemoteLoginException
import cn.elytra.mod.rl.http.InvalidSecretException
import cn.elytra.mod.rl.http.entity.RLResponseException
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*

internal fun Application.configureStatusPage() {
	install(StatusPages) {
		exception<RemoteLoginException> { call, cause ->
			if(cause.isGridAccessException) {
				call.respond(
					HttpStatusCode.InternalServerError,
					RLResponseException(
						"Grid Access Exception occurred while executing the operation. Try again!",
						RLResponseException.C_GRID_ACCESS_EXCEPTION
					),
				)
			} else {
				RemoteLoginAPI.LOGGER.warn("Exception occurred while executing Remote Login executions.", cause)
				call.respond(
					HttpStatusCode.InternalServerError,
					RLResponseException("Internal Server Error!"),
				)
			}
		}

		exception<InvalidSecretException> { call, cause ->
			call.respond(
				HttpStatusCode.Unauthorized,
				RLResponseException("Secret is invalid.", RLResponseException.C_INVALID_SECRET)
			)
		}

		exception<Exception> { call, cause ->
			RemoteLoginAPI.LOGGER.warn("Unknown server error has been captured.", cause)
			call.respond(
				HttpStatusCode.InternalServerError,
				RLResponseException("Unknown server error!")
			)
		}
	}
}