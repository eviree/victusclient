package com.victusclient.module;

/** Roadmap entry shown in the menu but not functional yet. */
public class ComingSoonModule extends Module {
    public ComingSoonModule(String name, Category category, String description) {
        super(name, category);
        this.description = description;
        this.soon = true;
    }
}
