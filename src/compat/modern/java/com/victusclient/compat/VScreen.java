package com.victusclient.compat;

import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;

/** Version-neutral screen base for Minecraft 1.21.9+ (event-object input API). */
public abstract class VScreen extends Screen {
    protected VScreen(Text title) { super(title); }

    protected boolean onClick(double x, double y, int button) { return false; }
    protected boolean onDrag(double x, double y, int button) { return false; }
    protected boolean onRelease(double x, double y, int button) { return false; }
    protected boolean onKey(int key) { return false; }
    protected boolean onChar(int codepoint) { return false; }

    @Override
    public boolean mouseClicked(Click c, boolean doubled) {
        return onClick(c.x(), c.y(), c.button()) || super.mouseClicked(c, doubled);
    }

    @Override
    public boolean mouseDragged(Click c, double dx, double dy) {
        return onDrag(c.x(), c.y(), c.button()) || super.mouseDragged(c, dx, dy);
    }

    @Override
    public boolean mouseReleased(Click c) {
        return onRelease(c.x(), c.y(), c.button()) || super.mouseReleased(c);
    }

    @Override
    public boolean keyPressed(KeyInput in) {
        // Prefer getKeycode() (AbstractInput); fall back to key() record accessor
        int code;
        try {
            code = in.getKeycode();
        } catch (Throwable t) {
            code = in.key();
        }
        if (onKey(code)) return true;
        return super.keyPressed(in);
    }

    @Override
    public boolean charTyped(CharInput in) {
        int cp;
        try {
            if (!in.isValidChar()) return super.charTyped(in);
            cp = in.codepoint();
        } catch (Throwable t) {
            cp = in.codepoint();
        }
        if (onChar(cp)) return true;
        return super.charTyped(in);
    }
}
