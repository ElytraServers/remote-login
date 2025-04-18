package cn.elytra.mod.rl.xmod;

import cn.elytra.mod.rl.RemoteLoginExceptionImpl;
import cn.elytra.mod.rl.common.RemoteLoginException;
import cn.elytra.mod.rl.common.RemoteLoginIconProvider;
import com.google.common.util.concurrent.ListenableFuture;
import itemrender.rendering.FBOHelper;
import itemrender.rendering.Renderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.concurrent.ExecutionException;

@SideOnly(Side.CLIENT)
public class ItemRenderDarkIconProvider implements RemoteLoginIconProvider {

    private final FBOHelper fboSmall = new FBOHelper(32);
    private final RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();

    @Override
    public String getItemIconBase64(String registryName, int metadata) throws RemoteLoginException {
        Item item = Item.getByNameOrId(registryName);
        if(item == null) throw new IllegalArgumentException("registryName " + registryName + " not found");

        ItemStack itemStack = new ItemStack(item, 1, metadata);
        ListenableFuture<String> future = Minecraft.getMinecraft().addScheduledTask(() -> Renderer.getItemBase64(itemStack, fboSmall, renderItem));
        try {
            return future.get(); // just wait here :P
        } catch(InterruptedException | ExecutionException e) {
            throw RemoteLoginExceptionImpl.wrap(e);
        }
    }

}
