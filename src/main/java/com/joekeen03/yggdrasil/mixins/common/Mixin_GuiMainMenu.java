package com.joekeen03.yggdrasil.mixins.common;

import com.joekeen03.yggdrasil.ModYggdrasil;
import com.joekeen03.yggdrasil.world.WorldTypeYggdrasil;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.world.GameType;
import net.minecraft.world.WorldSettings;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.minecraft.client.gui.GuiCreateWorld.getUncollidingSaveDirName;

@Mixin(GuiMainMenu.class)
public class Mixin_GuiMainMenu extends GuiScreen {

    public final String worldName = "Yggdrasil Testing";
    private static boolean alreadyStarted = false;

    @Inject(method="initGui",
            at=@At("TAIL"))
    public void inject(CallbackInfo ci)
    {
        if (!alreadyStarted) {
            startGame();
        }
    }

    public void startGame() {
        alreadyStarted = true;

        this.mc.displayGuiScreen((GuiScreen)null);

        WorldTypeYggdrasil YggdrasilType = ModYggdrasil.proxy.getYggdrasilType();

        YggdrasilType.onGUICreateWorldPress();

        WorldSettings worldsettings = new WorldSettings(0, GameType.getByName("creative"), true, false, YggdrasilType);
        worldsettings.setGeneratorOptions("");

        worldsettings.enableCommands();


        this.mc.launchIntegratedServer(computeValidSaveName(worldName), worldName.trim(), worldsettings);
    }

    public String computeValidSaveName(String name) {

        String saveDirName = name.trim();

        for (char c0 : ChatAllowedCharacters.ILLEGAL_FILE_CHARACTERS)
        {
            saveDirName = saveDirName.replace(c0, '_');
        }

        if (StringUtils.isEmpty(saveDirName))
        {
            saveDirName = "World";
        }

        saveDirName = getUncollidingSaveDirName(this.mc.getSaveLoader(), saveDirName);
        return saveDirName;
    }
}
