# ProgressTree

**Unlock Your Path, Not Just Rewards.**

A unique Minecraft server plugin that brings a progression tree system where players can choose their own rewards when reaching milestones. Rebranded from MilestoneMP with expanded features and migration support.

---

## Features

- **Progression Tree GUI** - Visual milestone system with locked, available, and claimed states
- **Choice-Based Rewards** - Players choose from multiple reward options per milestone
- **Multiple Milestone Types**
  - `PLAYTIME` - Play time tracking (7 levels)
  - `BLOCK_BREAK` - Blocks broken (7 levels)
  - `BLOCK_PLACE` - Blocks placed (7 levels)
  - `MOB_KILL` - Mobs killed (7 levels)
  - `PLAYER_KILL` - PvP kills
  - `JOIN` - Login streak
  - `COMMUNITY_PLAYTIME` - Total server playtime
- **SQLite & MySQL Support** - Flexible database options
- **PlaceholderAPI Integration** - Display progress anywhere
- **Console Command Rewards** - Commands executed via console sender
- **Migration Service** - Migrate data from legacy MilestoneMP plugin
- **Java 21** - Built on modern Java LTS

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

Example `config.yml`:

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

gui:
  title: "&8ProgressTree"
  available-color: "&a"
  locked-color: "&7"
  claimed-color: "&e"
  claim-button: "&aClick to Claim"
  choose-button: "&eChoose Reward"
  milestone-slots:
    - 10
    - 11
    - 12
    # ... up to 43 slots for large trees

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
```

---

## Reward System

Rewards are executed as **console commands**. Use `{player}` as placeholder for player name.

**Examples:**
```yaml
command: "give {player} diamond 10"
command: "eco give {player} 5000"
command: "xp give {player} 50 levels"
command: "tokens give {player} 100"
command: "adjustbonusclaimblocks {player} 1000"
```

---

## GUI Preview

The GUI features:
- Progress bar visual: `[████████░░] 80%`
- Color-coded items by milestone tier (&7 → &f → &e → &6 → &9 → &d → &5)
- Empty slots filled with glass panes for clean look
- Clear status indicators (Locked, Available, Claimed)
- Choice selection GUI for multiple reward options
- Support for up to 43 milestone display slots

---

## Installation

1. Download the plugin JAR file (requires Java 21+)
2. Place it in your server's `plugins` folder
3. Start the server
4. Configure `config.yml` to your needs
5. Use `/progresstree reload` to reload config after changes

### Migrating from MilestoneMP

If you're upgrading from MilestoneMP:
1. Install ProgressTree alongside or replacing MilestoneMP
2. Run `/progresstree migrate` as admin
3. All player progress data will be transferred automatically

---

## Changelog

### Version 2.0.0
- Rebranded from MilestoneMP to ProgressTree
- Added migration service for MilestoneMP data
- Expanded milestone tiers to 7 levels per type
- Updated to Java 21 and Paper API 1.21.8
- New placeholder identifier: `%progresstree_*%`
- Enhanced GUI with more display slots
- Added debug mode toggle

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

---

**License:** MIT  
**Version:** 2.0.0  
**Author:** Syaaddd  
**API Version:** 1.21

</content>