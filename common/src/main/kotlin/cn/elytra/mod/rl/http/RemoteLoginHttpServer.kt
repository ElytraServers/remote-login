package cn.elytra.mod.rl.http

import cn.elytra.mod.rl.RemoteLoginAPI
import cn.elytra.mod.rl.entity.ItemRepresentation
import io.ktor.http.*
import io.ktor.serialization.gson.*
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.contentnegotiation.*
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
					route("/{ap}") {
						get("/cpus") {
							val apId = call.parameters["ap"] ?: throw MissingRequestParameterException("ap")
							val ap = RemoteLoginAPI.getManager().getAccessPointByUuid(apId)
								?: throw NotFoundException()
							call.respond(ap.craftingCpuInfoList)
						}

						get("/items") {
							val apId = call.parameters["ap"] ?: throw MissingRequestParameterException("ap")
							val ap = RemoteLoginAPI.getManager().getAccessPointByUuid(apId)
								?: throw NotFoundException()
							call.respond(ap.allItemsInNetwork)
						}

						post("/simulate-craft") {
							val apId = call.parameters["ap"] ?: throw MissingRequestParameterException("ap")
							val ap = RemoteLoginAPI.getManager().getAccessPointByUuid(apId)
								?: throw NotFoundException()
							val ir = call.receive<ItemRepresentation>()
							val deferred = ap.simulateCraftingPlan(ir).asDeferred()
							call.respond(deferred.await())
						}

						post("/deploy-craft") {
							val apId = call.parameters["ap"] ?: throw MissingRequestParameterException("ap")
							val ap = RemoteLoginAPI.getManager().getAccessPointByUuid(apId)
								?: throw NotFoundException()
							ap.submitLastCraftingPlan()
							call.respond(status = HttpStatusCode.Accepted, message = "OK")
						}
					}
				}
			}
		}
	}

	val server by lazy { createServer() }

	fun start() = runBlocking {
		server.startSuspend()
	}

	fun stop() {
		server.stop()
	}

}