package cn.elytra.mod.rl.block;

import appeng.api.AEApi;
import appeng.api.util.AEPartLocation;
import appeng.core.sync.GuiBridge;
import appeng.util.Platform;
import cn.elytra.mod.rl.tile.RemoteLoginTile;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RemoteLoginBlock extends Block implements ITileEntityProvider {

    public RemoteLoginBlock() {
        super(Material.ANVIL);
        this.setRegistryName("remote_login");
        this.setTranslationKey("remote_login");
    }

    public ItemBlock getItemBlock() {
        return (ItemBlock) new ItemBlock(this).setRegistryName("remote_login");
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(@NotNull World worldIn, int meta) {
        return new RemoteLoginTile();
    }

    @Override
    public boolean onBlockActivated(@NotNull World worldIn, @NotNull BlockPos pos, @NotNull IBlockState state, @NotNull EntityPlayer playerIn, @NotNull EnumHand hand, @NotNull EnumFacing facing, float hitX, float hitY, float hitZ) {
        if(!worldIn.isRemote && hand == EnumHand.MAIN_HAND) {
            TileEntity tile = worldIn.getTileEntity(pos);
            if(tile instanceof RemoteLoginTile) {
                RemoteLoginTile ap = (RemoteLoginTile) tile;
                ItemStack heldItem = playerIn.getHeldItemMainhand();

                // renaming by the knives
                if(AEApi.instance().definitions().items().netherQuartzKnife().isSameAs(heldItem) ||
                        AEApi.instance().definitions().items().certusQuartzKnife().isSameAs(heldItem)) {
                    if(ForgeEventFactory.onItemUseStart(playerIn, heldItem, 1) <= 0) return false;
                    Platform.openGUI(playerIn, tile, AEPartLocation.fromFacing(facing), GuiBridge.GUI_RENAMER);
                    return true;
                }

                // getting the information
                if(playerIn.isSneaking() &&
                        AEApi.instance().definitions().items().networkTool().isSameAs(heldItem)) {
                    ap.reset();
                    playerIn.sendMessage(new TextComponentString("AP Reset!"));
                }
                playerIn.sendMessage(new TextComponentString("Uuid: " + ap.getUuid()));
                playerIn.sendMessage(new TextComponentString("Secret: " + ap.getSecret()));

                return true;
            }
        }
        return super.onBlockActivated(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ);
    }
}
