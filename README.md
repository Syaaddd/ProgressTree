# ProgressTree

**Unlock Your Path, Not Just Rewards.**

A Minecraft server plugin that brings a progression tree system where players choose their own rewards when reaching milestones. Rebranded from MilestoneMP with expanded features, pagination, and automatic config migration.

---

## Features

- **Progression Tree GUI** — Visual milestone system with locked, available, and claimed states
- **Pagination** — Automatic paging with navigation bar (prev/next/player stats/page indicator)
- **Choice-Based Rewards** — Players pick from multiple reward options per milestone
- **Configurable Tree Layout** — Custom slot templates, branch fillers, and progress bars
- **Auto Config Migration** — Seamlessly upgrades old `milestone-slots` configs to the new GUI schema
- **Multiple Milestone Types**
  - `PLAYTIME` — Play time tracking (7 levels)
  - `BLOCK_BREAK` — Blocks broken (7 levels)
  - `BLOCK_PLACE` — Blocks placed (7 levels)
  - `MOB_KILL` — Mobs killed (7 levels)
  - `PLAYER_KILL` — PvP kills
  - `JOIN` — Login streak
  - `COMMUNITY_PLAYTIME` — Total server playtime
- **SQLite & MySQL Support** — Flexible database options
- **PlaceholderAPI Integration** — Display progress anywhere
- **Console Command Rewards** — Commands executed via console sender
- **Migration Service** — Migrate player data from legacy MilestoneMP plugin
- **Java 21** — Built on modern Java LTS

---

## Commands

| Command | Description |
|---------|-------------|
| `/progresstree` | Open progress tree GUI |
| `/progresstree open` | Open progress tree GUI |
| `/progresstree check` | Check current progress |
| `/progresstree claim <id>` | Claim specific milestone |
| `/progresstree help` | Show help menu |
| `/progresstree reload` | Reload configuration (admin) |
| `/progresstree migrate` | Migrate from MilestoneMP (admin) |

**Aliases:** `/pt`, `/ptree`

---

## Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `progresstree.open` | Open progress tree GUI | true |
| `progresstree.claim` | Claim milestones | true |
| `progresstree.check` | Check progress | true |
| `progresstree.admin` | Admin commands (reload, migrate) | op |

---

## Placeholders

| Placeholder | Description |
|------------|-------------|
| `%progresstree_current%` | Current milestone ID |
| `%progresstree_next%` | Next available milestone |
| `%progresstree_progress%` | Progress percentage |
| `%progresstree_playtime%` | Player playtime (Xh Ym) |
| `%progresstree_blocks_broken%` | Blocks broken |
| `%progresstree_blocks_placed%` | Blocks placed |
| `%progresstree_mobs_killed%` | Mobs killed |
| `%progresstree_players_killed%` | PvP kills |
| `%progresstree_join_days%` | Login streak days |
| `%progresstree_community_playtime%` | Total server playtime (jam) |
| `%progresstree_can_claim%` | Can claim (Yes/No) |

---

## Configuration

### GUI Layout (v2.0+)

```yaml
gui:
  title: "&8ProgressTree"
  available-color: "&a"
  locked-color: "&7"
  claimed-color: "&e"
  claim-button: "&aClick to Claim"
  choose-button: "&eChoose Reward"

  # Slot template for milestone nodes (max 15 per page)
  layout-template:
    - 4
    - 11
    - 13
    - 15
    - 20
    - 22
    - 24
    - 26
    - 29
    - 31
    - 33
    - 35
    - 38
    - 40
    - 42

  # Navigation bar slots
  navigation:
    prev-page-slot: 45
    player-info-slot: 47
    page-indicator-slot: 49
    close-slot: 51
    next-page-slot: 53

  # Filler material for empty slots
  branch-filler:
    material: GRAY_STAINED_GLASS_PANE
    name: " "

  # Progress bar in milestone lore
  progress-bar:
    segments: 20
    filled-char: "▰"
    empty-char: "▱"
    filled-color: "&b"
    empty-color: "&8"
```

### Database & Settings

```yaml
debug: false

database:
  type: sqlite
  host: localhost
  port: 3306
  database: progresstree
  username: root
  password: ""

settings:
  check-interval: 60
  community-reward-broadcast: true

messages:
  prefix: "&8[&bProgressTree&8] "
  milestone-available: "&aMilestone available! Click to claim."
  milestone-locked: "&cThis milestone is still locked."
  milestone-claimed: "&eReward claimed: %reward%"
  no-milestone: "&cNo milestone available."
  player-not-found: "&cPlayer not found."
  config-reloaded: "&aConfiguration reloaded successfully."
  no-permission: "&cYou don't have permission."
  already-claimed: "&cThis milestone has already been claimed."
  milestone-claimed-self: "&eYou claimed: %reward%"
  data-loading: "&7Loading your progress data..."
  migration-success: "&aMigration completed successfully!"
  migration-failed: "&cMigration failed. Check console for details."
```

### Milestones

```yaml
milestones:
  PlayTime_1:
    type: PLAYTIME
    amount: 36000
    icon: CLOCK
    color: "&7"
    choices:
      - id: pt1_money
        name: "&650 Server Money"
        command: "eco give {player} 50"
      - id: pt1_claim
        name: "&a1000 Claim Blocks"
        command: "adjustbonusclaimblocks {player} 1000"

  Miner_1:
    type: BLOCK_BREAK
    amount: 100
    icon: DIAMOND_PICKAXE
    color: "&e"
    choices:
      - id: m1_pickaxe
        name: "&eDiamond Pickaxe"
        command: "give {player} diamond_pickaxe 1"
```

---

## Reward System

Rewards are executed as **console commands**. Use `{player}` as placeholder for player name.

```yaml
command: "give {player} diamond 10"
command: "eco give {player} 5000"
command: "xp give {player} 50 levels"
command: "tokens give {player} 100"
command: "adjustbonusclaimblocks {player} 1000"
```

---

## Config Migration

Upgrading from v1.x or MilestoneMP? ProgressTree automatically detects the old `gui.milestone-slots` format and migrates it to the new schema on first load. No manual editing needed — old keys are preserved as reference, and new defaults are applied for navigation, branch filler, and progress bar settings.

For player data migration from MilestoneMP, use `/progresstree migrate`.

---

## Installation

1. Download the plugin JAR file (requires Java 21+)
2. Place it in your server's `plugins` folder
3. Start the server
4. Configure `config.yml` to your needs
5. Use `/progresstree reload` to reload config after changes

**Requirements:** Paper/Purpur 1.21+, Java 21+

### Migrating from MilestoneMP

1. Install ProgressTree alongside or replacing MilestoneMP
2. Run `/progresstree migrate` as admin
3. All player progress data will be transferred automatically

---

## Changelog

### Version 2.0.0
- ✅ Rebranded from MilestoneMP to ProgressTree
- ✅ Full GUI rework with pagination system
- ✅ Configurable tree layout templates
- ✅ Navigation bar with player stats and page indicators
- ✅ Progress bar visualization in milestone lore
- ✅ Auto-migration from old config format
- ✅ Branch filler customization
- ✅ Migration service for MilestoneMP player data
- ✅ Expanded milestone tiers to 7 levels per type
- ✅ Updated to Java 21 and Paper API 1.21.8
- ✅ New placeholder identifier: `%progresstree_*%`
- ✅ Added debug mode toggle
- 🔧 Config schema changed (auto-migrates from v1.x)

### Version 1.0.7 (as MilestoneMP)
- Fixed: Milestone notification not showing when player completes quest through activities
- Fixed: Removed break statement to notify all reached milestones

### Version 1.0.6 (as MilestoneMP)
- Fixed: Choices not loaded from config
- Fixed: Player data loading to be synchronous
- Fixed: Milestone claim logic
- Fixed: Milestone notification spam

### Version 1.0.5 (as MilestoneMP)
- Initial release with progression tree system
- Choice-based rewards system
- Multiple milestone types support
- SQLite & MySQL support
- PlaceholderAPI integration

---

## Support

- Issues: [GitHub Issues](https://github.com/Syaaddd/ProgressTree/issues)
- Repository: [github.com/Syaaddd/ProgressTree](https://github.com/Syaaddd/ProgressTree)

---

**License:** MIT  
**Version:** 2.0.0  
**Author:** Syaaddd  
**API Version:** 1.21

</content>