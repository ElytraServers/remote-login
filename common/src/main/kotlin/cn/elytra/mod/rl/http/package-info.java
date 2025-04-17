/**
 * The package of the actual HTTP server based on Ktor, so Kotlin is required.
 * <p>
 * The artifact from Kotlin is compatible with Java with required standard libraries, which is provided by mods like
 * Forgelin, KotlinForForge, etc.
 * <p>
 * The Java part should not directly interact with anything in this package except the {@link cn.elytra.mod.rl.RemoteLoginAPI RemoteLoginAPI}.
 *
 * @see cn.elytra.mod.rl.RemoteLoginAPI
 */
package cn.elytra.mod.rl.http;
