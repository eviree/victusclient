# Victus Client mod (Fabric 1.21.1 → 1.21.11) — v2.2

* **Right Shift** → Victus menu (sidebar, search, animated switches, profiles, settings)
* Custom title screen + pause menu styling
* Minimap, custom crosshair, hitboxes, freelook helper, waypoints, TNT timer, time/weather changers
* Performance: FPS Boost, Entity Culling, Particle Limiter
* Inventory Overhaul, Friends list, Reconnect, Nick Hider, Scoreboard controls, Shulker tooltips toggle
* Dozens of HUD / PvP / utility modules
* Multi-version builds (`versions/`)

## New / completed modules in this pass
- Minimap, TPS, Custom Crosshair, Hitbox, Perspective (freelook helper)
- Time Changer, TNT Timer, Waypoints, Elytras
- FPS Boost, Entity Culling, Particle Limiter
- Inventory Overhaul, Friends, Reconnect, Nick Hider, Scoreboard, Shulker Tooltips

## Still Coming Soon (need deeper mixins / external libs)
Motion Blur, Color Saturation, Custom Fog, Glint, ViewModel, Block Overlay, Item Physic, Loot Beams, Nametags, Snaplook, Custom Chat, Discord RPC, Tablist, Voice.

## Notes
- **Friends / world join:** local friends list + alerts. Online-mode auth cannot be bypassed by a client mod.
- **Inventory Overhaul:** animation + dim + keep-world-ticking flags; not full 3D world-space inventory.
- **Shulker Tooltips / Scoreboard hide:** module flags ready; full visual effect may need extra mixins on some versions.

## Build
```bash
./gradlew build -Ptarget=1.21.11
```
