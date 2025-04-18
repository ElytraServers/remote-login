package cn.elytra.mod.rl.http

interface RemoteLoginHttpResponseExceptions

class InvalidSecretException() : Exception("Secret is invalid"), RemoteLoginHttpResponseExceptions