package cn.elytra.mod.rl.http

import cn.elytra.mod.rl.RemoteLoginAPI
import cn.elytra.mod.rl.common.RemoteLoginAccessPoint
import cn.elytra.mod.rl.common.RemoteLoginException
import cn.elytra.mod.rl.entity.ItemRepresentation
import cn.elytra.mod.rl.http.entity.RLResponseException
import io.ktor.http.*
import io.ktor.serialization.gson.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.future.asDeferred
import kotlinx.coroutines.runBlocking

class RemoteLoginHttpServer {

	private fun createServer(): EmbeddedServer<CIOApplicationEngine, CIOApplicationEngine.Configuration> {
		val config = RemoteLoginAPI.getConfig()
		return embeddedServer(CIO, port = config.httpServerPort, host = config.httpServerHost) {
			install(ContentNegotiation) {
				gson {
					if(config.usePrettyPrintJsonResponse()) {
						setPrettyPrinting()
					}
				}
			}

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
			}

			authentication {
				basic(name = "ap") {
					realm = "Access Point Authentication"
					validate { credentials ->
						if(credentials.name == "remote-login") {
							RemoteLoginPrincipal(credentials.password)
						} else {
							null
						}
					}
				}
			}

			install(CORS) {
				allowMethod(HttpMethod.Options)
				allowMethod(HttpMethod.Put)
				allowMethod(HttpMethod.Delete)
				allowMethod(HttpMethod.Patch)
				allowHeader(HttpHeaders.Authorization)
				// allowHeader("MyCustomHeader")

				val corsAllowedOrigins = config.corsAllowedOrigins
				if(corsAllowedOrigins != null) {
					corsAllowedOrigins.forEach {
						allowHost(it)
					}
				} else {
					anyHost()
				}
			}

			routing {
				get("/") {
					println("Hello!")
					call.respondText("Hello from the Remote Login Server!", ContentType.Text.Plain)
				}

				route("/v1") {

					get("/list") {
						call.respond(RemoteLoginAPI.getManager().remoteLoginInfoList)
					}

					// network-related operations
					authenticate("ap") {
						route("/{ap}") {
							get("/cpus") {
								val ap = call.getAccessPoint()
								call.respond(ap.craftingCpuInfoList)
							}

							get("/items") {
								val ap = call.getAccessPoint()
								call.respond(ap.allItemsInNetwork)
							}

							post("/simulate-craft") {
								val ap = call.getAccessPoint()
								val ir = call.receive<ItemRepresentation>()
								val deferred = ap.simulateCraftingPlan(ir).asDeferred()
								call.respond(deferred.await())
							}

							post("/deploy-craft") {
								val ap = call.getAccessPoint()
								ap.submitLastCraftingPlan()
								call.respond(status = HttpStatusCode.Accepted, message = "OK")
							}
						}
					}
				}
			}
		}
	}

	private fun ApplicationCall.getAccessPoint(
		validatePrincipal: Boolean = true,
		apParameterName: String = "ap",
	): RemoteLoginAccessPoint {
		val apId = this.parameters["ap"] ?: throw MissingRequestParameterException(apParameterName)
		val ap = RemoteLoginAPI.getManager().getAccessPointByUuid(apId)
			?: throw NotFoundException()
		if(validatePrincipal) {
			val principal = this.principal<RemoteLoginPrincipal>()
			if(principal == null || !ap.isSecretValid(principal.secret)) {
				throw InvalidSecretException()
			}
		}
		return ap
	}

	val server by lazy { createServer() }

	fun start() = runBlocking {
		server.startSuspend()
	}

	fun stop() {
		server.stop()
	}

}