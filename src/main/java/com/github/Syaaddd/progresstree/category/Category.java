package com.github.Syaaddd.progresstree.category;

import com.github.Syaaddd.progresstree.milestone.MilestoneType;
import org.bukkit.Material;

import java.util.*;

/**
 * Immutable category model for ProgressTree GUI hub.
 */
public final class Category {

    private final String id;
    private final String name;
    private final Material icon;
    private final String color;
    private final int order;
    private final List<String> description;
    private final Set<MilestoneType> types;

    public Category(String id, String name, Material icon, String color, int order,
                    List<String> description, Set<MilestoneType> types) {
        this.id = Objects.requireNonNull(id);
        this.name = Objects.requireNonNull(name);
        this.icon = Objects.requireNonNull(icon);
        this.color = Objects.requireNonNull(color);
        this.order = order;
        this.description = description != null ? Collections.unmodifiableList(new ArrayList<>(description)) : Collections.emptyList();
        this.types = types != null ? Collections.unmodifiableSet(EnumSet.copyOf(types)) : EnumSet.noneOf(MilestoneType.class);
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public Material getIcon() { return icon; }
    public String getColor() { return color; }
    public int getOrder() { return order; }
    public List<String> getDescription() { return description; }
    public Set<MilestoneType> getTypes() { return types; }

    public boolean matchesType(MilestoneType type) {
        return types.contains(type);
    }
}
