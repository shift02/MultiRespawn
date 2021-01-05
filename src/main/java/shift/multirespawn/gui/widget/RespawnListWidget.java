package shift.multirespawn.gui.widget;

import shift.multirespawn.gui.RespawnListScreen;
import shift.multirespawn.net.PacketHandler;
import shift.multirespawn.net.SetRespawnPacket;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.list.AbstractList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.*;
import net.minecraft.world.World;

public class RespawnListWidget  extends AbstractList<RespawnListWidget.RespawnEntry> {

    private static String stripControlCodes(String value) { return net.minecraft.util.StringUtils.stripControlCodes(value); }

    private final int listWidth;

    private RespawnListScreen parent;

    public RespawnListWidget(RespawnListScreen parent, int listWidth,int listHeight, int top, int bottom)
    {
        super(parent.getMinecraftInstance(), listWidth, listHeight, top, bottom, 25);

        //背景の土の描画をやめる
        //this.func_244606_c(false);
        //this.func_244605_b(false);

        this.parent = parent;
        this.listWidth = listWidth;
        this.refreshList();
    }

    protected boolean isSelectedItem(int index) {
        return false;
    }

    public int getRowWidth() {
        return 400;
    }

    protected int getScrollbarPosition() {
        return super.getScrollbarPosition() + 32;
    }

    public void refreshList() {
        this.clearEntries();
        parent.buildModList(this::addEntry, pos->new RespawnListWidget.RespawnEntry(pos, this.parent));
    }

    public class RespawnEntry extends AbstractList.AbstractListEntry<RespawnListWidget.RespawnEntry> {

        private final BlockPos blockPos;
        private final RespawnListScreen parent;
        private final Button button;

        RespawnEntry(BlockPos blockPos, RespawnListScreen parent) {
            this.blockPos = blockPos;
            this.parent = parent;

            this.button = new Button(parent.width / 2 - 155, 0, 310, 20, new TranslationTextComponent(this.blockPos.toString()), (button)->{

                PacketHandler.INSTANCE.sendToServer(SetRespawnPacket.createSetRespawnPacket(World.OVERWORLD, blockPos));

                parent.getMinecraftInstance().player.respawnPlayer();
                parent.getMinecraftInstance().displayGuiScreen((Screen)null);

            });
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {

            return this.button.mouseClicked(mouseX,mouseY,button);

        }

        @Override
        public void render(MatrixStack mStack, int entryIdx, int top, int left, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean p_194999_5_, float partialTicks)
        {

            this.button.y =top;
            this.button.render(mStack, mouseX, mouseY, partialTicks);

//            ITextComponent pos = new StringTextComponent(stripControlCodes(this.blockPos.toString()));
//            ITextComponent version = new StringTextComponent(stripControlCodes("hoge"));
//            FontRenderer font = this.parent.getFontRenderer();
//            font.func_238422_b_(mStack, LanguageMap.getInstance().func_241870_a(ITextProperties.func_240655_a_(font.func_238417_a_(pos,    listWidth))), left + 3, top + 2, 0xFFFFFF);
//            font.func_238422_b_(mStack, LanguageMap.getInstance().func_241870_a(ITextProperties.func_240655_a_(font.func_238417_a_(version, listWidth))), left + 3, top + 2 + font.FONT_HEIGHT, 0xCCCCCC);
//            if (vercheck.status.shouldDraw())
//            {
//                //TODO: Consider adding more icons for visualization
//                Minecraft.getInstance().getTextureManager().bindTexture(VERSION_CHECK_ICONS);
//                RenderSystem.color4f(1, 1, 1, 1);
//                RenderSystem.pushMatrix();
//                AbstractGui.blit(mStack, getLeft() + width - 12, top + entryHeight / 4, vercheck.status.getSheetOffset() * 8, (vercheck.status.isAnimated() && ((System.currentTimeMillis() / 800 & 1)) == 1) ? 8 : 0, 8, 8, 64, 16);
//                RenderSystem.popMatrix();
//            }
        }
    }

}
