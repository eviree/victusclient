package com.victusclient.module;

public enum Category {
    SPECIAL("Victus"), PVP("PvP"), HUD("HUD"), VISUAL("Visual"), MOVEMENT("Movement"), WORLD("World"), UTILITY("Utility");
    public final String label;
    Category(String label) { this.label = label; }
}
