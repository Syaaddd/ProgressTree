# Implementation Plan: Category-Based GUI (v2.1.0)

**PRD Reference:** `progresstree-category-gui-prd.md`
**Base Version:** 2.0.0 (commit `428a52e`)
**Target Version:** 2.1.0

---

## Phase 1: Model & Config Foundation
**Goal:** Define category data structures and load them from config with backward compatibility.

### Tasks:
1. **Create `Category` model class**
   - Fields: `id`, `name`, `icon` (Material), `color`, `order`, `description` (List<String>), `types` (Set<MilestoneType>)
   - Immutable record or final class

2. **Create `CategoryRegistry` class**
   - Load `gui.categories` section from config
   - If section missing → generate default 7 categories (Opsi 1 from PRD) and write to config
   - Validate each category: icon must be valid Material, types must be valid MilestoneType
   - Log warnings for invalid entries
   - Provide `getCategory(String id)`, `getAllCategories()` (sorted by order), `getCategoryForMilestone(Milestone)`

3. **Add `category` field to `Milestone` model**
   - Optional String field, nullable
   - Parse from config (`category:` key in milestone section)

4. **Implement category resolution logic** (PRD §7.3)
   - Priority: milestone.category > type-based mapping > fallback "other"
   - Log warning if explicit category not found in registry

5. **Update `ConfigManager`**
   - Parse `gui.hub.*` settings (title, rows, hide-empty, filler, category-slots)
   - Parse `gui.categories.*` section
   - Add `back-button-slot` to navigation config
   - Auto-migrate: if no `gui.categories` exists, create defaults on first load

6. **Update `config.yml`**
   - Add full `gui.hub` and `gui.categories` sections with defaults
   - Add example `category:` override in one milestone

**Deliverable:** Categories load correctly, milestones resolve to categories, old configs auto-migrate.

---

## Phase 2: Category Summary System
**Goal:** Compute per-player stats per category without DB queries on main thread.

### Tasks:
1. **Create `CategorySummary` class**
   - Fields: `categoryId`, `totalMilestones`, `claimedCount`, `claimableCount`, `nextMilestone` (nullable), `nextProgress` (double), `isComplete`
   - Computed from PlayerData + MilestoneManager (both already cached)

2. **Create `CategorySummaryService`**
   - Method: `computeSummary(PlayerData, Category)` → CategorySummary
   - Uses existing `MilestoneManager.hasReached()` and `PlayerData.hasClaimed()`
   - No DB access — pure computation from cached data
   - Handle COMMUNITY_PLAYTIME specially (uses global playtime from Repository)

3. **Integrate into `ProgressTree` main class**
   - Expose `getCategorySummaryService()` getter
   - Refresh summaries on GUI open and after claim (event-driven, not polling)

**Deliverable:** Accurate per-category stats available instantly from cache.

---

## Phase 3: Hub GUI
**Goal:** Render category hub with badges, glow indicators, and player stats.

### Tasks:
1. **Create `HubGui` class**
   - Render hub inventory using `gui.hub` config
   - Fill border/filler slots with configured material
   - Place category icons at `category-slots` positions
   - Skip empty categories if `hide-empty: true`

2. **Render category icon items**
   - Display name: category color + name
   - Lore: description lines, claimed/total count, next milestone progress bar (reuse `buildProgressBarComponent`), status badge
   - Enchant glint override if `claimableCount > 0`
   - Hide category entirely if empty and `hide-empty: true`

3. **Render player stats item**
   - Position: configurable slot in hub (default center-bottom)
   - Lore: total claimed across all categories, total milestones, most complete category

4. **Render close button**
   - Same style as tree nav bar close button

5. **Handle hub clicks**
   - Click category → open tree for that category (page 0)
   - Click stats/close → appropriate action
   - Ignore filler/border clicks

**Deliverable:** Functional hub showing all categories with accurate badges.

---

## Phase 4: Tree Per Category
**Goal:** Filter tree view by category, add Back button, dynamic title.

### Tasks:
1. **Modify `ProgressTreeGUI.open()`**
   - Accept `categoryId` parameter
   - Filter milestones by category before pagination
   - Sort filtered milestones by `amount` ascending (stable sort)
   - Set inventory title: `"&8ProgressTree » " + category.color + category.name`

2. **Add Back button to nav bar**
   - New config key: `gui.navigation.back-slot` (default 47, shift player-info if needed)
   - Render as ARROW with "&e&l◀ Back to Hub" name
   - Click → reopen HubGui

3. **Adjust nav bar layout**
   - Prev: slot 45
   - Back: slot 46 (new)
   - Player Info: slot 47 → move to 48
   - Page Indicator: slot 49 → stays
   - Close: slot 51 → stays
   - Next: slot 53 → stays
   - Update config defaults accordingly

4. **Update click handler**
   - Route Back button click to HubGui
   - Preserve existing milestone click behavior

5. **Handle empty category edge case**
   - If category has 0 milestones after filter → show single info item "No milestones in this category" + Back button

**Deliverable:** Category-filtered tree with proper navigation back to hub.

---

## Phase 5: Session State Management
**Goal:** Remember player's last viewed category/page, restore after claims.

### Tasks:
1. **Create `GuiSession` class**
   - Fields: `screen` (enum: HUB, TREE, CHOICE), `categoryId`, `pageNumber`
   - In-memory map keyed by UUID in `ProgressTreeGUI`
   - Cleared on player quit / plugin disable

2. **Update `open()` methods**
   - Store session state when opening hub/tree/choice
   - After claim → re-render same screen/category/page (existing behavior extended to include categoryId)

3. **Update ChoiceGUI**
   - Accept `returnCategoryId` and `returnPage` parameters
   - On close/back → reopen tree at exact position

4. **Handle `/pt reload` safety**
   - Clear all active sessions on reload
   - Players with open GUI get it closed gracefully (or rebuilt if feasible)

5. **Persist session across GUI interactions**
   - Claim from page 2 category Mining → returns to page 2 Mining
   - Navigate Hub → Tree → Choice → back → same tree page

**Deliverable:** Seamless navigation state preserved throughout session.

---

## Phase 6: Extras (Optional for 2.1.0, can defer to 2.1.1)
**Goal:** Quality-of-life features that enhance usability.

### Tasks:
1. **Shortcut command `/pt <category>`**
   - Tab-complete category IDs
   - Opens tree directly (skip hub)
   - Permission: `progresstree.open` (same as base)

2. **New placeholders**
   - `%progresstree_claimable_total%` — total claimable across all categories
   - `%progresstree_claimable_<category>%` — claimable count per category
   - Register in PlaceholderAPI expansion

3. **Category-aware notifications**
   - Modify existing notification message to include category name
   - Use existing `ProgressUpdateEvent`, no new events

4. **Per-category permissions**
   - `progresstree.category.<id>` — hide category from players without permission
   - Default: all allowed (non-breaking)
   - Check in HubGui render and category resolution

5. **Community category special styling**
   - Different icon/color for COMMUNITY_PLAYTIME category
   - Label "(Server-wide)" in lore
   - Already handled via config defaults

**Deliverable:** Enhanced UX with shortcuts, placeholders, and granular access control.

---

## Phase 7: Testing & Validation
**Goal:** Verify all acceptance criteria and edge cases.

### Test Cases (from PRD §10):
- [ ] Old config v2.0.0 without `gui.categories` → auto-migrates, GUI works
- [ ] Milestone with invalid `category:` → warning logged, falls back to type-based
- [ ] Empty category with `hide-empty: true` → hidden from hub
- [ ] Empty category with `hide-empty: false` → shown with info item
- [ ] Category with exactly 13 milestones → 1 page, no nav arrows
- [ ] Category with 14 milestones → 2 pages, nav functional
- [ ] Claim from page 2 → returns to page 2 same category
- [ ] All milestones claimed → badge shows "✅ Complete"
- [ ] Data still loading → hub shows loading state
- [ ] `/pt reload` with open GUI → safe handling
- [ ] More categories than hub slots → warning, excess hidden
- [ ] COMMUNITY_PLAYTIME displays global progress correctly
- [ ] Rapid clicking → no double-open or duplicate claims
- [ ] Same `amount` milestones → stable sort order

### Build Verification:
- [ ] `mvn clean package` succeeds
- [ ] No deprecation warnings related to new code
- [ ] JAR size reasonable (no unexpected bloat)

---

## File Change Summary

| File | Action | Description |
|------|--------|-------------|
| `Category.java` | NEW | Category model |
| `CategoryRegistry.java` | NEW | Load/validate categories from config |
| `CategorySummary.java` | NEW | Per-player category stats |
| `CategorySummaryService.java` | NEW | Compute summaries from cache |
| `HubGui.java` | NEW | Render category hub |
| `GuiSession.java` | NEW | Track player GUI state |
| `Milestone.java` | MODIFY | Add optional `category` field |
| `ConfigManager.java` | MODIFY | Parse hub/categories config, migration |
| `ProgressTreeGUI.java` | MODIFY | Category filtering, Back button, session |
| `ChoiceGUI.java` | MODIFY | Return to correct category/page |
| `ProgressTree.java` | MODIFY | Wire new services, session cleanup |
| `config.yml` | MODIFY | Add hub/categories sections |
| `plugin.yml` | MODIFY | Bump version to 2.1.0 |
| `pom.xml` | MODIFY | Bump version to 2.1.0 |
| `README.md` | MODIFY | Document new features |

---

## Execution Order

```
Phase 1 → Phase 2 → Phase 3 → Phase 4 → Phase 5 → [Build + Test] → Phase 6 → Phase 7
```

Phases 1-5 are mandatory for 2.1.0 release.
Phase 6 can be deferred to 2.1.1 if time-constrained.
Phase 7 runs incrementally after each phase + comprehensive at end.

</content>