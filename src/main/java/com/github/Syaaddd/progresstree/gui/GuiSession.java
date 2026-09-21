package com.github.Syaaddd.progresstree.gui;

import java.util.*;

/**
 * In-memory session state per player for category-based GUI navigation.
 * Tracks current screen (hub/tree/choice), active category, and page number.
 * Cleared on logout or server reload.
 */
public class GuiSession {

    public enum Screen { HUB, TREE, CHOICE }

    private Screen screen = Screen.HUB;
    private String categoryId = null;
    private int page = 0;

    public Screen getScreen() { return screen; }
    public String getCategoryId() { return categoryId; }
    public int getPage() { return page; }

    public void openHub() {
        this.screen = Screen.HUB;
        this.categoryId = null;
        this.page = 0;
    }

    public void openTree(String categoryId, int page) {
        this.screen = Screen.TREE;
        this.categoryId = Objects.requireNonNull(categoryId);
        this.page = Math.max(0, page);
    }

    public void openChoice(String categoryId, int page) {
        this.screen = Screen.CHOICE;
        this.categoryId = Objects.requireNonNull(categoryId);
        this.page = Math.max(0, page);
    }

    /** Return to tree from choice screen, preserving category and page */
    public void backToTree() {
        this.screen = Screen.TREE;
        // categoryId and page remain unchanged
    }

    /** Return to hub from tree screen */
    public void backToHub() {
        this.screen = Screen.HUB;
        this.categoryId = null;
        this.page = 0;
    }
}
