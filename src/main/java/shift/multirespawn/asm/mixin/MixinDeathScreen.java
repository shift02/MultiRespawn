package shift.multirespawn.asm.mixin;

import shift.multirespawn.gui.RespawnListScreen;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DeathScreen.class)
public abstract class MixinDeathScreen extends Screen{

    private static final Logger LOGGER = LogManager.getLogger();

//    @Shadow
//    public int width;
//
//    @Shadow
//    public int height;

    public MixinDeathScreen(ITextComponent titleIn) {
        super(titleIn);
    }

    @Inject(at = @At("HEAD"), method = "init()V", remap = false)
    private void init( CallbackInfo callback) {

        this.minecraft.player.revive();

        this.addButton(new Button(this.width / 2 - 100, this.height / 4 + 120, 200, 20, new TranslationTextComponent("deathScreen.respawn.multi"), (p_213021_1_) -> {
            //this.minecraft.player.respawnPlayer();
            //this.minecraft.displayGuiScreen((Screen)null);
            this.minecraft.displayGuiScreen(new RespawnListScreen(this));
        }));

    }
//
//    @Shadow
//    public <T extends Widget> T addButton(T button)  {
//        throw new IllegalStateException("Mixin failed to shadow getItem()");
//    }

}
