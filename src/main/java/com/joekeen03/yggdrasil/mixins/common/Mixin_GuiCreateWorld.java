package com.joekeen03.yggdrasil.mixins.common;

import com.joekeen03.yggdrasil.ModYggdrasil;
import com.joekeen03.yggdrasil.proxy.CommonProxy;
import com.joekeen03.yggdrasil.world.WorldTypeYggdrasil;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiCreateWorld;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.world.GameType;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

import static net.minecraft.client.gui.GuiCreateWorld.getUncollidingSaveDirName;

@Mixin(GuiCreateWorld.class)
public abstract class Mixin_GuiCreateWorld extends GuiScreen {
    public Mixin_GuiCreateWorld() {
        throw new RuntimeException("Shouldn't be called.");
    }

    public final String worldName = "Yggdrasil Testing";

    @Shadow
    private int selectedIndex;

    @Shadow
    private String gameMode;
    @Shadow
    private boolean allowCheats;
    @Shadow
    private boolean hardCoreMode;
    @Shadow
    private GuiButton btnAllowCommands;
    @Shadow
    private GuiButton btnBonusItems;
    @Shadow
    private GuiTextField worldNameField;
    @Shadow
    private GuiTextField worldSeedField;
    @Shadow
    private String saveDirName;
    @Shadow
    private boolean generateStructuresEnabled = true;
    @Shadow
    private boolean bonusChestEnabled;
    @Shadow
    private boolean alreadyGenerated;
    @Shadow
    public String chunkProviderSettingsJson = "";

    @Shadow
    abstract void updateDisplayState();
    @Shadow
    abstract void calcSaveDirName();

    @Inject(method="initGui",
            at=@At("TAIL"))
    public void inject(CallbackInfo ci)
    {
        this.selectedIndex = 6;
        this.allowCheats = true;
        this.hardCoreMode = false;
        this.gameMode = "creative";
        this.btnAllowCommands.enabled = true;
        this.btnBonusItems.enabled = true;
        this.worldNameField.setText(worldName);
        this.worldSeedField.setText("1");
        this.calcSaveDirName();
        this.updateDisplayState();
        startGame();
    }

    public void startGame() {

        this.mc.displayGuiScreen((GuiScreen)null);

        this.alreadyGenerated = true;
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
