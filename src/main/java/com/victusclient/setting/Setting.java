package com.victusclient.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import java.util.List;

public abstract class Setting {
    public final String name;
    protected Setting(String name) { this.name = name; }
    public abstract JsonElement save();
    public abstract void load(JsonElement e);

    public static class Bool extends Setting {
        public boolean value;
        public Bool(String name, boolean value) { super(name); this.value = value; }
        public void toggle() { value = !value; }
        @Override public JsonElement save() { return new JsonPrimitive(value); }
        @Override public void load(JsonElement e) { value = e.getAsBoolean(); }
    }

    public static class Slider extends Setting {
        public double value, min, max, step;
        public Slider(String name, double value, double min, double max, double step) {
            super(name); this.value = value; this.min = min; this.max = max; this.step = step;
        }
        public void setFraction(double f) {
            f = Math.max(0, Math.min(1, f));
            double v = min + (max - min) * f;
            value = Math.max(min, Math.min(max, Math.round(v / step) * step));
        }
        public double fraction() { return (value - min) / (max - min); }
        public String display() { return step >= 1 ? String.valueOf((int) value) : String.format("%.1f", value); }
        @Override public JsonElement save() { return new JsonPrimitive(value); }
        @Override public void load(JsonElement e) { value = Math.max(min, Math.min(max, e.getAsDouble())); }
    }

    public static class Mode extends Setting {
        public final List<String> modes;
        public int index;
        public Mode(String name, int index, String... modes) {
            super(name); this.modes = List.of(modes); this.index = index;
        }
        public String get() { return modes.get(index); }
        public void next() { index = (index + 1) % modes.size(); }
        public void prev() { index = (index - 1 + modes.size()) % modes.size(); }
        @Override public JsonElement save() { return new JsonPrimitive(index); }
        @Override public void load(JsonElement e) {
            int i = e.getAsInt();
            if (i >= 0 && i < modes.size()) index = i;
        }
    }
}
