package cn.elytra.mod.rl.http.plugin

import cn.elytra.mod.rl.common.RemoteLoginConfig
import io.ktor.http.CacheControl
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.content.CachingOptions
import io.ktor.serialization.gson.gson
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.cachingheaders.CachingHeaders
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import kotlin.collections.forEach

internal fun Application.configureHttp(config: RemoteLoginConfig) {

	// CORS
	install(CORS) {
		allowMethod(HttpMethod.Options)
		allowMethod(HttpMethod.Put)
		allowMethod(HttpMethod.Delete)
		allowMethod(HttpMethod.Patch)
		allowHeader(HttpHeaders.ContentType)
		allowHeader(HttpHeaders.Authorization)
		// allowHeader("MyCustomHeader")

		allowCredentials = true
		allowNonSimpleContentTypes = true

		val corsAllowedOrigins = config.corsAllowedOrigins
		if(corsAllowedOrigins != null) {
			corsAllowedOrigins.forEach {
				allowHost(it)
			}
		} else {
			anyHost()
		}
	}

	// Caching
	install(CachingHeaders) {
		options { call, outgoingContent ->
			when(outgoingContent.contentType?.withoutParameters()) {
				ContentType.Text.CSS -> CachingOptions(CacheControl.MaxAge(maxAgeSeconds = 24 * 60 * 60))
				ContentType.Image.PNG -> CachingOptions(CacheControl.MaxAge(maxAgeSeconds = 24 * 60 * 60))
				else -> null
			}
		}
	}

	// Content Negotiation
	install(ContentNegotiation) {
		gson {
			if(config.usePrettyPrintJsonResponse()) {
				setPrettyPrinting()
			}
		}
	}
}