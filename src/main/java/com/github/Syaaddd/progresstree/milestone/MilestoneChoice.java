package com.github.Syaaddd.progresstree.milestone;

public class MilestoneChoice {

    private final String id;
    private final String name;
    private final String command;

    public MilestoneChoice(String id, String name, String command) {
        this.id = id;
        this.name = name;
        this.command = command;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getCommand() { return command; }

    public String formatCommand(String playerName) {
        return command.replace("{player}", playerName);
    }
}
