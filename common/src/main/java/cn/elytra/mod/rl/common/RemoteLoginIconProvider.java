package cn.elytra.mod.rl.common;

import cn.elytra.mod.rl.entity.ItemRepresentation;

public interface RemoteLoginIconProvider {

    @SuppressWarnings("SpellCheckingInspection")
    String BLACK_PURPLE_TEXTURE = "iVBORw0KGgoAAAANSUhEUgAAAKAAAACgAQMAAACxAfVuAAAABlBMVEUAAAD4APit1uGJAAAAJ0lEQVR4Xu3JoQEAAAjDMP5/GhxI3FQq07p6gxBCCNP4fQghhDCAA24TOht3kTKDAAAAAElFTkSuQmCC";

    /**
     * Transform the given item to the rendered image.
     * <p>
     * If it is called on the server-side, it should be obtained from static files, because the server environment is not supposed to do rendering things.
     *
     * @param ir the item
     * @return the string-form base64-encoded image.
     */
    default String getItemIconBase64(ItemRepresentation ir) throws RemoteLoginException {
        return getItemIconBase64(ir.getItemId(), ir.getMetadata());
    }

    /**
     * Transform the given item to the rendered image.
     * <p>
     * If it is called on the server-side, it should be obtained from static files, because the server environment is not supposed to do rendering things.
     *
     * @param registryName the registry name of the given item
     * @param metadata     the metadata of the given item
     * @return the string-form base64-encoded image.
     */
    String getItemIconBase64(String registryName, int metadata) throws RemoteLoginException;

}
