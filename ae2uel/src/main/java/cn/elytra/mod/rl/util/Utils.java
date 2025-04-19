package cn.elytra.mod.rl.util;

import cn.elytra.mod.rl.RemoteLoginExceptionImpl;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.function.Function;

public class Utils {

    public static <T, R> CompletableFuture<R> waitFutureAndThen(Future<T> future, Function<T, R> then) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                T value = future.get();
                return then.apply(value);
            } catch(Exception e) {
                throw RemoteLoginExceptionImpl.wrap(e);
            }
        });
    }
}
