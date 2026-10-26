---
navigation:
  title: "Witch's Stove"
  parent: herbalwitch:apothecary/lavender.md
item_ids:
  - herbalwitch:stations/stove/station
---

# Witch's Stove

Place this functional stove on a solid floor. Open it and insert furnace fuel; it burns automatically. Put the [Tea Kettle](herbalwitch:stations/kettle.md) directly on its burner. The stove must remain lit to supply heat. Decorative cottage stoves do not supply heat.

<GameScene interactive={true} zoom={3} fullWidth={true}>
  <Block id="minecraft:stone" x="0" y="0" z="0" />
  <Block id="herbalwitch:stations/stove/station" x="0" y="1" z="0" p:facing="north" p:lit="true" p:part="0" />
  <Block id="minecraft:stone" x="-1" y="0" z="0" />
  <Block id="herbalwitch:stations/stove/station" x="-1" y="1" z="0" p:facing="north" p:lit="true" p:part="1" />
  <Block id="herbalwitch:stations/kettle/station" x="0" y="2" z="0" p:facing="north" />
</GameScene>

## Crafting

<HerbalCrafting id="herbalwitch:stations/stove/station" />

[Complete first-herb lesson](herbalwitch:apothecary/lavender.md) | [The Witch's Path](herbalwitch:path/start.md)
