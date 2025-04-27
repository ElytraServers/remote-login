package cn.elytra.mod.rl.http

import cn.elytra.mod.rl.RemoteLoginAPI
import cn.elytra.mod.rl.common.RemoteLoginAccessPoint
import cn.elytra.mod.rl.entity.ItemRepresentation
import cn.elytra.mod.rl.http.entity.CraftResponse
import cn.elytra.mod.rl.http.plugin.configureAuthentication
import cn.elytra.mod.rl.http.plugin.configureHttp
import cn.elytra.mod.rl.http.plugin.configureStatusPage
import cn.elytra.mod.rl.http.plugin.configureWebSocket
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import io.ktor.util.converters.*
import io.ktor.util.reflect.*
import kotlinx.coroutines.future.await
import kotlinx.coroutines.runBlocking
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class RemoteLoginHttpServer {

	private fun createServer(): EmbeddedServer<CIOApplicationEngine, CIOApplicationEngine.Configuration> {
		val config = RemoteLoginAPI.getConfig()
		return embeddedServer(CIO, port = config.httpServerPort, host = config.httpServerHost) {

			configureHttp(config)
			configureAuthentication()
			configureStatusPage()
			configureWebSocket()

			routing {
				get("/") {
					println("Hello!")
					call.respondText("Hello from the Remote Login Server!", ContentType.Text.Plain)
				}

				route("/v1") {

					get("/list") {
						call.respond(RemoteLoginAPI.getManager().remoteLoginInfoList)
					}

					get("/icon/{itemId}/{itemMetadata}") {
						val iconProvider = RemoteLoginAPI.getIconProvider()
						val itemId: String by call.pathParameters
						val itemMetadata: Int? by call.pathParameters

						val base64 = iconProvider.getItemIconBase64(itemId, itemMetadata ?: 0)
						val bytes = java.util.Base64.getDecoder().decode(base64)

						call.respondBytes(bytes, contentType = ContentType.Image.PNG)
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
								val renderItem by call.queryParameters.optional<Boolean>()
								call.respond(ap.getAllItemsInNetwork(renderItem ?: true))
							}

							post("/craft/simulate") {
								val ap = call.getAccessPoint()
								val ir = call.receive<ItemRepresentation>()
								val plan = ap.getCraftingPlan(ir).await()
								val key = ap.putCraftingPlanCache(plan)
								val info = plan.craftingPlanInfo
								call.respond(CraftResponse(key, info))
							}

							post("/craft/deploy") {
								val ap = call.getAccessPoint()
								val key = call.receive<String>()
								val plan = ap.getCraftingPlanCache(key)
									?: throw NotFoundException()
								plan.start()
								call.respond(
									HttpStatusCode.Accepted,
									"OK"
								)
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

	private inline fun <reified T : Any> Parameters.optional(typeInfo: TypeInfo = typeInfo<T>()): ReadOnlyProperty<Nothing?, T?> {
		return object : ReadOnlyProperty<Nothing?, T?> {
			override fun getValue(thisRef: Nothing?, property: KProperty<*>): T? {
				val name = property.name
				val values = getAll(name)
				if(values == null) return null
				return try {
					DefaultConversionService.fromValues(values, typeInfo) as T
				} catch(cause: Exception) {
					throw ParameterConversionException(
						name,
						typeInfo.type.simpleName ?: typeInfo.type.toString(),
						cause
					)
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