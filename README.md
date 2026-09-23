# ProgressTree

Progression tree plugin for Paper/Purpur with choice-based rewards. Every milestone gives the player a set of reward options instead of one fixed prize, and milestones are grouped into a category hub so players can jump straight to the track they care about.

[![Version](https://img.shields.io/badge/version-2.1.0-2ea44f?style=flat-square)](https://github.com/Syaaddd/ProgressTree/releases)
[![Java](https://img.shields.io/badge/Java-21-orange?style=flat-square&logo=openjdk)](#)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.21+-green?style=flat-square)](#)
[![License](https://img.shields.io/badge/license-MIT-blue?style=flat-square)](#)

## What it does

- Players open `/pt`, pick a category in the hub, and see a paginated milestone tree for that category.
- Each milestone tracks one stat (playtime, blocks broken, mob kills, etc). When the threshold is reached, the player claims it and picks one reward from its choices.
- Rewards run as console commands, so anything with a command works: economy, claim blocks, tokens, XP, items.
- Progress persists in SQLite or MySQL. Upgrading from MilestoneMP? `/progresstree migrate` transfers old player data.

Rebranded from **MilestoneMP** with expanded features and one-command data migration.

## Features

**For players**
- Category hub GUI: one entry per milestone type (up to 7 categories), tree view per category with page navigation
- Progress bar in item lore showing current vs target value and percentage
- Claimed state shows which reward the player picked
- Status on every node: locked, available, or claimed

**For admins**
- Every milestone, icon, color, slot, layout template, and message is config-driven
- 7 milestone types: PLAYTIME, BLOCK_BREAK, BLOCK_PLACE, MOB_KILL, PLAYER_KILL, JOIN, COMMUNITY_PLAYTIME
- 11 PlaceholderAPI placeholders for scoreboards, holograms, and other PAPI consumers
- `/pt reload` applies config changes without restarting
- SQLite or MySQL backend, switched with one config line
- GUI routing by InventoryHolder, so custom titles and renamed items can't break click handling

## Quick start

1. Drop the jar into `/plugins`.
2. Start the server (Paper/Purpur 1.21+, Java 21).
3. Configure milestones in `config.yml`, then run `/progresstree reload`.

Default categories and sensible milestone defaults are generated on first boot.

## Milestone types

| Type | Tracks |
|------|--------|
| `PLAYTIME` | Individual playtime |
| `BLOCK_BREAK` | Blocks broken |
| `BLOCK_PLACE` | Blocks placed |
| `MOB_KILL` | Mobs killed |
| `PLAYER_KILL` | PvP kills |
| `JOIN` | Login streak (days) |
| `COMMUNITY_PLAYTIME` | Total server playtime |

## Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/progresstree` | Open the category hub GUI | `progresstree.open` |
| `/progresstree check` | View current progress | `progresstree.check` |
| `/progresstree claim <id>` | Claim a specific milestone | `progresstree.claim` |
| `/progresstree help` | Show command help | - |
| `/progresstree reload` | Reload configuration | `progresstree.admin` |
| `/progresstree migrate` | Migrate from MilestoneMP | `progresstree.admin` |

**Aliases:** `/pt`, `/ptree`

## Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `progresstree.open` | Open the progress tree GUI | everyone |
| `progresstree.claim` | Claim milestones | everyone |
| `progresstree.check` | Check progress | everyone |
| `progresstree.admin` | Reload and migration commands | op |

## Placeholders (PlaceholderAPI)

| Placeholder | Output |
|-------------|--------|
| `%progresstree_current%` | Current milestone ID |
| `%progresstree_next%` | Next available milestone |
| `%progresstree_progress%` | Progress percentage |
| `%progresstree_playtime%` | Playtime formatted `Xh Ym` |
| `%progresstree_blocks_broken%` | Blocks broken |
| `%progresstree_blocks_placed%` | Blocks placed |
| `%progresstree_mobs_killed%` | Mobs killed |
| `%progresstree_players_killed%` | PvP kills |
| `%progresstree_join_days%` | Login streak (days) |
| `%progresstree_community_playtime%` | Total server playtime (hours) |
| `%progresstree_can_claim%` | Yes / No |

## Configuration example

```yaml
database:
  type: sqlite        # or: mysql

milestones:
  PlayTime_1:
    type: PLAYTIME
    amount: 36000     # 10 hours
    icon: CLOCK
    color: "&7"
    choices:          # the player picks one
      - id: pt1_money
        name: "&650 Server Money"
        command: "eco give {player} 50"
      - id: pt1_claim
        name: "&a1000 Claim Blocks"
        command: "adjustbonusclaimblocks {player} 1000"
```

`{player}` is replaced with the player name when the reward command runs.

Categories are configured under `gui.categories` (name, icon, color, order, milestone types). If the section is missing, defaults are generated and written to the config.

## GUI layout

- 54-slot chest: 13 milestone nodes per page in a pyramid template, nav bar on the bottom row (prev/back/info/page/close/next)
- Hub page lists categories with claim progress; categories with ready rewards are flagged in lore
- Progress bar chars, colors, filler materials, and all slots are configurable (`gui.layout-template`, `gui.navigation`, `gui.progress-bar`)

## Migrating from MilestoneMP

1. Replace the MilestoneMP jar with ProgressTree.
2. Start the server.
3. Run `/progresstree migrate`.

Player progress transfers automatically.

## Requirements

| Requirement | Version |
|-------------|---------|
| Server | Paper / Purpur |
| Minecraft | 1.21+ |
| Java | 21 |
| PlaceholderAPI | *(optional)* |

## Support

Bugs and feature requests: [GitHub Issues](https://github.com/Syaaddd/ProgressTree/issues).

---

**License:** MIT | **Author:** [Syaaddd](https://github.com/Syaaddd)
