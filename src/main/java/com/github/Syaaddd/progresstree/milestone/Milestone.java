package com.github.Syaaddd.progresstree.milestone;

import java.util.List;

public class Milestone {

    private final String id;
    private final MilestoneType type;
    private final int amount;
    private final List<MilestoneChoice> choices;
    private final String icon;
    private final String displayColor;
    private final String category; // nullable — explicit category override

    public Milestone(String id, MilestoneType type, int amount, List<MilestoneChoice> choices, String icon, String displayColor) {
        this(id, type, amount, choices, icon, displayColor, null);
    }

    public Milestone(String id, MilestoneType type, int amount, List<MilestoneChoice> choices, String icon, String displayColor, String category) {
        this.id = id;
        this.type = type;
        this.amount = amount;
        this.choices = choices;
        this.icon = icon;
        this.displayColor = displayColor;
        this.category = category;
    }

    public String getId() { return id; }
    public MilestoneType getType() { return type; }
    public int getAmount() { return amount; }
    public List<MilestoneChoice> getChoices() { return choices; }
    public boolean hasChoices() { return choices != null && !choices.isEmpty(); }
    public String getIcon() { return icon; }
    public String getDisplayColor() { return displayColor; }
    public String getCategory() { return category; }
}
