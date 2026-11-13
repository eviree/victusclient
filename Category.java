package com.victusclient.module;

public enum Category {
    HUD("HUD"), PVP("PvP"), RENDER("Render"), MOVEMENT("Movement");
    public final String label;
    Category(String label) { this.label = label; }
}
