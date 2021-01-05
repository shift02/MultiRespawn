package shift.multirespawn.gui;

import net.minecraft.client.entity.player.ClientPlayerEntity;
import shift.multirespawn.capability.RespawnPlayerData;
import shift.multirespawn.gui.widget.RespawnListWidget;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.DialogTexts;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.list.AbstractList;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.LanguageMap;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.client.gui.ScrollPanel;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.util.Size2i;
import net.minecraftforge.fml.client.gui.GuiUtils;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class RespawnListScreen extends Screen {

    private static final int PADDING = 6;

    private RespawnListWidget respawnListWidget;

    private Screen parentScreen;

    public RespawnListScreen(Screen parentScreen) {
        super(new TranslationTextComponent("deathScreen.respawn.list.title"));

        this.parentScreen = parentScreen;
    }

    @Override
    public void init()
    {
        super.init();

        int fullButtonHeight = PADDING + 20 + PADDING;
        this.respawnListWidget = new RespawnListWidget(this, this.width, this.height, 32, this.height - 40);
        //this.respawnListWidget.setLeftPos(20);
        this.children.add(this.respawnListWidget);

        this.addButton(new Button(this.width / 2 - 100, this.height -30, 200, 20, DialogTexts.GUI_BACK, (p_213079_1_) -> {
            this.minecraft.displayGuiScreen(this.parentScreen);
        }));
    }

    public Minecraft getMinecraftInstance()
    {
        return minecraft;
    }

    public FontRenderer getFontRenderer()
    {
        return font;
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {

        this.fillGradient(matrixStack, 0, 0, this.width, this.height, 1615855616, -1602211792);

        this.respawnListWidget.render(matrixStack, mouseX, mouseY, partialTicks);

        RenderSystem.pushMatrix();
        RenderSystem.scalef(2.0F, 2.0F, 2.0F);
        drawCenteredString(matrixStack, this.font, this.title, this.width / 2 / 2, 4, 16777215);
        RenderSystem.popMatrix();

        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    public <T extends AbstractList.AbstractListEntry<T>> void buildModList(Consumer<T> modListViewConsumer, Function<BlockPos, T> newEntry)
    {

        ClientPlayerEntity player = this.minecraft.player;
        player.revive();// Capabilityが削除されないようにフラグを追加

        Set<BlockPos> blockPos = player.getCapability(RespawnPlayerData.capability)
                .map(RespawnPlayerData::getBlockPosList)
                .orElse(new HashSet<>());

        blockPos.forEach(pos->modListViewConsumer.accept(newEntry.apply(pos)));
    }

    @Override
    public void closeScreen()
    {
        this.minecraft.displayGuiScreen(this.parentScreen);
    }
}
