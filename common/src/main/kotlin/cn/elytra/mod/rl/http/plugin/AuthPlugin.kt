package cn.elytra.mod.rl.http.plugin

import cn.elytra.mod.rl.http.RemoteLoginPrincipal
import io.ktor.server.application.Application
import io.ktor.server.auth.authentication
import io.ktor.server.auth.basic

internal fun Application.configureAuthentication() {

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

}