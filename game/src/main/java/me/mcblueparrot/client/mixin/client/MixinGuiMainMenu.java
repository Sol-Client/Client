package me.mcblueparrot.client.mixin.client;

import me.mcblueparrot.client.mod.ModsScreen;
import me.mcblueparrot.client.ui.BetterLanguageGui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.resources.LanguageManager;
import net.minecraft.client.settings.GameSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(GuiMainMenu.class)
public class MixinGuiMainMenu extends GuiScreen {

    @Shadow
    private GuiButton realmsButton;

    @Inject(method = "addSingleplayerMultiplayerButtons", at = @At("RETURN"))
    public void getModsButton(int x, int y, CallbackInfo callback) {

        buttonList.remove(realmsButton);
        buttonList.add(new GuiButton(realmsButton.id, realmsButton.xPosition, realmsButton.yPosition, "Mods"));
    }

    @Redirect(method = "actionPerformed", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiMainMenu;" +
            "switchToRealms()V"))
    public void openModsMenu(GuiMainMenu guiMainMenu) {
        mc.displayGuiScreen(new ModsScreen(guiMainMenu));
    }

}
