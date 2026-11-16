package com.victusclient;

import com.victusclient.setting.Setting;
import java.util.List;

public final class GlobalSettings {
    private GlobalSettings() {}
    public static final Setting.Bool animations = new Setting.Bool("UI Animations", true);
    public static final Setting.Bool titleScreen = new Setting.Bool("Custom Title Screen", true);
    public static final Setting.Bool pauseMenu = new Setting.Bool("Custom Pause Menu", true);
    public static final Setting.Slider menuOpacity = new Setting.Slider("Menu Opacity", 95, 60, 100, 5);
    public static final List<Setting> ALL = List.of(animations, titleScreen, pauseMenu, menuOpacity);
}
