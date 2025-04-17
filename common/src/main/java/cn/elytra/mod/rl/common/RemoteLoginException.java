package cn.elytra.mod.rl.common;

/**
 * The wrapper of the exceptions thrown in the API calls.
 * <p>
 * It wraps the cause exceptions to RuntimeException, to avoid forced checks;
 * and also provides the ability to check if the cause exception is GAE, which is supposed to be ignored.
 */
public abstract class RemoteLoginException extends RuntimeException {

    public RemoteLoginException() {
    }

    public RemoteLoginException(String message) {
        super(message);
    }

    public RemoteLoginException(String message, Throwable cause) {
        super(message, cause);
    }

    public RemoteLoginException(Throwable cause) {
        super(cause);
    }

    public RemoteLoginException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    /**
     * @return {@code true} if the cause is GridAccessException, which is ignorable in some cases.
     */
    public abstract boolean isGridAccessException();

}
