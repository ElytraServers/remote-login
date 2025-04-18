package cn.elytra.mod.rl;

import cn.elytra.mod.rl.block.RemoteLoginBlock;
import cn.elytra.mod.rl.config.RemoteLoginConfigForge;
import cn.elytra.mod.rl.tile.RemoteLoginTile;
import cn.elytra.mod.rl.xmod.ItemRenderDarkIconProvider;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.jetbrains.annotations.NotNull;

@Mod(modid = "remote_login")
@Mod.EventBusSubscriber
public class RemoteLogin {

    @Mod.Instance
    public static RemoteLogin instance;

    public RemoteLogin() {
        RemoteLoginAPI.setManager(RemoteLoginTileManager.INSTANCE);
    }

    public static ResourceLocation location(String path) {
        return new ResourceLocation("remote_login", path);
    }

    @NotNull
    public static final RemoteLoginBlock REMOTE_LOGIN_BLOCK = new RemoteLoginBlock();

    @SubscribeEvent
    public static void onBlockRegistration(RegistryEvent.Register<Block> event) {
        event.getRegistry().register(REMOTE_LOGIN_BLOCK);
        GameRegistry.registerTileEntity(RemoteLoginTile.class, location("remote_login"));
    }

    @SubscribeEvent
    public static void onItemRegistration(RegistryEvent.Register<Item> event) {
        event.getRegistry().register(REMOTE_LOGIN_BLOCK.getItemBlock());
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        RemoteLoginAPI.setConfig(new RemoteLoginConfigForge(new Configuration(event.getSuggestedConfigurationFile())));
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        try {
            tryItemRenderDarkCompat();
        } catch(Exception e) {
            RemoteLoginAPI.LOGGER.warn("Failed to load ItemRenderDark", e);
        }
    }

    @SuppressWarnings("SpellCheckingInspection")
    private void tryItemRenderDarkCompat() {
        if(Loader.isModLoaded("itemrender") && FMLCommonHandler.instance().getEffectiveSide().isClient()) {
            RemoteLoginAPI.LOGGER.info("Loaded ItemRenderDark as RemoteLoginIconProvider");
            RemoteLoginAPI.setIconProvider(new ItemRenderDarkIconProvider());
        }
    }

    @Mod.EventHandler
    public void onServerStarted(FMLServerStartedEvent event) {
        RemoteLoginAPI.initAndStartServer();
    }

    @Mod.EventHandler
    public void onServerStopping(FMLServerStoppingEvent event) {
        RemoteLoginAPI.stopServer();
    }

}
