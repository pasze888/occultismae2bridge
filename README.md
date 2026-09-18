# Occultism AE2 Bridge

[English](README.md) | [简体中文](README.zh-CN.md)

A NeoForge 1.21.1 addon that exposes **Occultism's Storage Controller** to
**Applied Energistics 2** as native `MEStorage`, so an AE2 Storage Bus reads and
writes the controller's real map-backed aggregate totals — one entry per item
type, counts may exceed the stack limit — instead of the per-slot
`IItemHandler` view.

Pure behavior mod: it registers no blocks, items, or other content of its own.

## Why native MEStorage

- The plain slot view spreads an item type over several virtual slots when its
  count exceeds the stack limit; `MEStorage` reports the true aggregated total.
- Cost is an O(slots) scan plus an O(distinct types) diff; a single item's
  count is an aggregate value read in O(1).
- With no change monitor, AE2 puts the bus to sleep (`ITickingMonitor`) instead
  of actively polling like external-storage bridges do.

Structurally modeled on the official [Applied-Mekanistics](https://github.com/ramidzkh/Applied-Mekanistics)
QIO adapter (see `docs/ai/gotchas.md` for the licensing consequence).

## Requirements

| Dependency | Version |
|---|---|
| Minecraft | 1.21.1 |
| NeoForge | 21.1.244 or newer |
| Applied Energistics 2 | 19.2.17 or newer |
| Occultism | 1.205.0 or newer |

Both AE2 and Occultism are required load-order dependencies (`AFTER`).

## Installation

Drop `occultismae2bridge-<version>.jar` into your `mods/` folder alongside
NeoForge 1.21.1, AE2 and Occultism. No other steps needed.

## Usage

1. Place any Storage Controller variant — regular, stabilized, dark, or
   stabilized dark. All four are supported; base/pedestal blocks have no
   inventory and are intentionally excluded.
2. Attach an AE2 Storage Bus facing the controller.
3. The controller's contents appear in the ME network as aggregated item
   types, and inserts/extracts go straight into the controller's map-backed
   storage. Bus filters apply as usual.

## Configuration

There is none. The mod has no config file and registers nothing; it only
provides the `ME_STORAGE` block capability on the controllers.

## Common commands

Run inside this project directory:

```bash
./gradlew build        # compile + package the mod jar (build/libs/)
./gradlew runClient    # dev client (downloads the run environment first)
./gradlew runServer    # dev server
```

## Documentation

- `docs/reference/occultism-mestorage-bridge.md` — verified API facts
  (Occultism / AE2 / NeoForge signatures with source locations). Read this
  before touching the bridge code.
- `docs/ai/gotchas.md` — collaboration pitfalls to avoid in future sessions.
- `docs/troubleshooting.md` — build / environment issues.

## License

LGPL-3.0-or-later. Portions adapted from
[Applied-Mekanistics](https://github.com/ramidzkh/Applied-Mekanistics)
(Copyright ramidzkh, LGPL-3.0-or-later); attribution is marked in the affected
source files.
