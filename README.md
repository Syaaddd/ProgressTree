<div align="center">

# 🌳 ProgressTree

**Unlock Your Path, Not Just Rewards.**

The progression tree plugin that lets players *choose* their own destiny —
visual milestone trees, choice-based rewards, and zero bloat.

[![Version](https://img.shields.io/badge/version-2.0.0-2ea44f?style=flat-square)](#)
[![Java](https://img.shields.io/badge/Java-21-orange?style=flat-square&logo=openjdk)](#)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.21+-green?style=flat-square)](#)
[![License](https://img.shields.io/badge/license-MIT-blue?style=flat-square)](#)

</div>

---

## 💡 Why ProgressTree?

Most reward plugins hand players a fixed prize and call it a day. Your players aren't all the same — why should their rewards be?

**ProgressTree** turns grinding into a *decision*. Every milestone unlocks a branch of **reward choices**: the miner takes claim blocks, the trader takes money, the PvP addict takes tokens. Players feel ownership over their progression — and your server gets retention that generic "vote-reward" plugins never deliver.

> Rebranded from **MilestoneMP** with a full feature expansion and one-command data migration.

---

## ✨ Features

### 🎮 For Players
- **Visual Progression Tree GUI** — locked, available, and claimed states at a glance, with animated progress bars `[████████░░] 80%`
- **Choice-Based Rewards** — every milestone offers multiple rewards; the player decides
- **7 Milestone Trackers** — playtime, blocks broken/placed, mob kills, PvP kills, login streaks, and global server playtime
- **Color-Coded Tiers** — 7 tier colors from `&7` to `&5` so progression *feels* like progression

### 🛠️ For Admins
- **Fully Configurable** — every milestone, icon, color, slot, and message is a config option. Up to 43 display slots for massive trees
- **Works With Your Economy** — rewards are console commands, so ProgressTree integrates with *any* plugin: Vault economies, claims, tokens, XP, custom items — no dependencies required
- **SQLite & MySQL** — run it on a SMP or a network; swap backends with one line
- **PlaceholderAPI** — 11 placeholders to surface progress in scoreboards, scoreboards, holograms, and scoreboards… everywhere
- **Hot Reload** — `/pt reload` applies config changes without a restart
- **Legacy Migration** — upgrading from MilestoneMP? One command transfers all player data

---

## 🚀 Quick Start

```bash
# 1. Drop the jar into /plugins
# 2. Start your server (Paper/Purpur 1.21+, Java 21)
# 3. Done — ProgressTree generates sensible defaults on first boot
```

Then make it yours: configure milestones in `config.yml` and run `/progresstree reload`.

---

## 🧩 Milestone Types

| Type | Tracks | Levels |
|------|--------|:------:|
| `PLAYTIME` | Individual playtime | 7 |
| `BLOCK_BREAK` | Blocks broken | 7 |
| `BLOCK_PLACE` | Blocks placed | 7 |
| `MOB_KILL` | Mobs killed | 7 |
| `PLAYER_KILL` | PvP kills | ✓ |
| `JOIN` | Login streak (days) | ✓ |
| `COMMUNITY_PLAYTIME` | Total server playtime | ✓ |

---

## ⌨️ Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/progresstree` | Open the progression tree GUI | `progresstree.open` |
| `/progresstree check` | View current progress | `progresstree.check` |
| `/progresstree claim <id>` | Claim a specific milestone | `progresstree.claim` |
| `/progresstree help` | Show command help | — |
| `/progresstree reload` | Reload configuration | `progresstree.admin` |
| `/progresstree migrate` | Migrate from MilestoneMP | `progresstree.admin` |

**Aliases:** `/pt`, `/ptree`

---

## 🔐 Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `progresstree.open` | Open the progress tree GUI | everyone |
| `progresstree.claim` | Claim milestones | everyone |
| `progresstree.check` | Check progress | everyone |
| `progresstree.admin` | Reload & migration commands | op |

---

## 📊 Placeholders (PlaceholderAPI)

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

---

## ⚙️ Configuration

```yaml
database:
  type: sqlite        # or: mysql
  host: localhost
  port: 3306
  database: progresstree
  username: root
  password: ""

settings:
  check-interval: 60                    # seconds between progress checks
  community-reward-broadcast: true      # announce server-wide milestones

milestones:
  PlayTime_1:
    type: PLAYTIME
    amount: 36000                       # 10 hours
    icon: CLOCK
    color: "&7"
    choices:                            # ← the player picks one
      - id: pt1_money
        name: "&650 Server Money"
        command: "eco give {player} 50"
      - id: pt1_claim
        name: "&a1000 Claim Blocks"
        command: "adjustbonusclaimblocks {player} 1000"
```

### 💰 Reward Commands

Rewards execute as console commands — `{player}` is replaced automatically. If it has a command, it can be a reward:

```yaml
command: "give {player} diamond 10"
command: "eco give {player} 5000"
command: "xp give {player} 50 levels"
command: "tokens give {player} 100"
command: "adjustbonusclaimblocks {player} 1000"
```

---

## 🖥️ GUI Details

- Progress bars rendered in-lore: `[████████░░] 80%`
- 7-tier color progression per milestone rank
- Clean layout — empty slots auto-filled with glass panes
- Instant status feedback: 🔒 Locked / 🟢 Available / 🟡 Claimed
- Dedicated reward-choice screen for multi-choice milestones
- Scales to **43 milestone slots** for deep progression trees

---

## 🔄 Migrating from MilestoneMP

1. Replace the MilestoneMP jar with ProgressTree
2. Start the server
3. Run `/progresstree migrate`

All player progress transfers automatically. Your players won't lose a single block broken.

---

## 📋 Requirements

| Requirement | Version |
|-------------|---------|
| Server | Paper / Purpur |
| Minecraft | 1.21+ |
| Java | 21 |
| PlaceholderAPI | *(optional)* |

---

## 🐛 Support & Feedback

Found a bug or have a feature idea?

📬 **[GitHub Issues](https://github.com/Syaaddd/ProgressTree/issues)** — fast responses, public roadmap.

---

<div align="center">

**License:** MIT &nbsp;•&nbsp; **Version:** 2.0.0 &nbsp;•&nbsp; **Author:** [Syaaddd](https://github.com/Syaaddd)

*Made with ☕ and too many late-night test servers.*

</div>
