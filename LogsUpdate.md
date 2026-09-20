# LogsUpdate.md

---

## v2.0.0

### Breaking Changes
- Rebranded from **MilestoneMP** to **ProgressTree**
- Command changed: `/milestone` → `/progresstree` (aliases: `/pt`, `/ptree`)
- Permission prefix changed: `milestonemp.*` → `progresstree.*`
- Placeholder prefix changed: `%milestone_mp_*%` → `%progresstree_*%`
- Plugin folder changed: `plugins/MilestoneMP/` → `plugins/ProgressTree/`
- Database name changed: `milestonemp.db` → `progresstree.db`

### Features
- Added migration service: automatic detection and manual `/progresstree migrate` command for MilestoneMP data
- Expanded milestone tiers to 7 levels per type (PlayTime, Break, Place, Kill)
- Updated to Java 21 and Paper API 1.21.8
- Enhanced GUI with up to 43 milestone display slots
- Added debug mode toggle in config.yml
- New message keys: `migration-success`, `migration-failed`, `data-loading`

### Technical
- Built with Maven shade plugin
- SQLite JDBC 3.46.1.0
- PlaceholderAPI 2.11.5

---

## v1.0.5 (as MilestoneMP)

### Fixes
- Fixed reward commands not executing to console
- Fixed %reward% placeholder not being replaced in chat message

### Features
- Added custom icon configuration per milestone in config.yml
- Added custom color configuration per milestone in config.yml
- Added logging for reward command execution

Example config:
```yaml
milestones:
  beginner:
    type: PLAYTIME
    amount: 3600
    icon: CLOCK
    color: "&6"
```

---

## v1.0.4

### Fixes
- Fixed color codes not working in chat messages (applied MessageUtil.color() to all sendMessage calls)

### Features
- Improved GUI tooltip/lore design:
  - Added progress bar visual ([████░░░░░] 40%)
  - Better color hierarchy for readability
  - Added Unicode icons (🔒, ✓)
  - More detailed lore with progress info
- Improved item icons based on milestone type:
  - PLAYTIME: CLOCK
  - BLOCK_BREAK: DIAMOND_PICKAXE / COBBLESTONE
  - MOB_KILL: ZOMBIE_HEAD
  - PLAYER_KILL: IRON_SWORD
  - etc.
- Improved GUI layout:
  - Fill empty slots with BLACK_STAINED_GLASS_PANE
  - Better visual hierarchy

---

## v1.0.3

### Changes
- Changed all messages to English
- Changed all GUI text to English
- Changed default milestone rewards to English names
- Added new message keys:
  - `no-permission`
  - `already-claimed`
  - `milestone-claimed-self`

---

## v1.0.2

### Fixes
- Fixed color codes still not working (applied color in ConfigManager getters)
- Fixed GUI items can still be moved/dragged (added InventoryDragEvent)
- Improved GUI title matching for better event handling

---

## v1.0.1

### Features
- Added slot configuration in config.yml (`gui.milestone-slots`)
- Added command tab completer (`/milestone` auto-complete)
- Added `/milestone help` command

### Fixes
- Fixed color codes not showing in chat messages (applied in ConfigManager getters)
- Fixed items in GUI being movable (added InventoryDragEvent handler)
- Fixed items in GUI being draggable (added proper event cancellation)
- Fixed lambda variable errors (final variables)
- Fixed SQLite Collection type error
- Fixed `selectedChoiceId` not effectively final

---

## v1.0.0

### Initial Release
- Progression Tree GUI system
- Choice-based rewards system
- Multiple milestone types:
  - PLAYTIME
  - BLOCK_BREAK
  - BLOCK_PLACE
  - MOB_KILL
  - PLAYER_KILL
  - JOIN
  - COMMUNITY_PLAYTIME
- SQLite & MySQL support
- PlaceholderAPI integration
- Commands:
  - `/milestone` - Open GUI
  - `/milestone open` - Open GUI
  - `/milestone claim <id>` - Claim milestone
  - `/milestone check` - Check progress
  - `/milestone reload` - Reload config
- Permissions system
- Configurable messages and GUI
