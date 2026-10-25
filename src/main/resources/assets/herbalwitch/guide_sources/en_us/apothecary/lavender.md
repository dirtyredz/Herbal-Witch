---
navigation:
  title: "My first useful herb"
  parent: herbalwitch:path/start.md
  position: 1
---

# My first useful herb

Make a **Restful Infusion**: a drink that resets your time since rest, helping with phantoms without forcing you to sleep.
You can make and drink it before becoming a witch. A witch can also use it to prepare for their next rewarded sleep.

[Back to The Witch's Path](../path/start.md)

## Gather and grow

Begin with lavender. Harvest a mature wild plant by using it, or plant lavender seeds on farmland or an herb bed,
let the plant mature, then harvest it by hand. Save seeds to grow more.
Lavender starter seeds can also be crafted; the current recipe is below.

<RecipeFor id="herbalwitch:botany/temperate/lavender/seeds" />

The default drink also needs **valerian**. Look for it in shaded habitats, harvest mature plants and keep their seeds.
You can discover herbs and seeds by breaking grass in their habitats. Grow valerian on farmland or an herb bed.
The live cards below show what your server actually accepts.

## Dry the fresh herbs

Craft a Herb Drying Rack, place it on a solid floor with room for its whole frame, and use it to open its inventory.
Put the fresh herb in the input slot. Drying starts automatically; keep the area loaded and leave room in the output.
No fuel or heat is needed. Take the dried bundle when it finishes.

<RecipeFor id="herbalwitch:stations/drying_rack/station" />

Drag this scene to rotate the real rack model. The hanging bundles indicate occupancy; their colours do not identify the herb being dried.

<!-- scene:drying_rack -->

<HerbalRecipe id="herbalwitch:processing/drying/temperate/lavender" />

<HerbalRecipe id="herbalwitch:processing/drying/shade/valerian" />

## Grind the bundles

Craft and place a Mortar and Pestle. Open it and put a dried bundle in the input slot.
Grinding starts automatically; take the powder from the output. Prepare both herbs this way.

<RecipeFor id="herbalwitch:stations/mortar/station" />

<HerbalRecipe id="herbalwitch:processing/grinding/herbs/temperate/lavender" />

<HerbalRecipe id="herbalwitch:processing/grinding/herbs/shade/valerian" />

## Arrange the heat and brew

Craft a Tea Kettle and a functional Witch's Stove. Place the stove on a solid floor and the kettle on its burner,
as shown below. Open the stove and add furnace fuel; it burns automatically. The kettle needs the stove lit beneath it.
Decorative cottage stoves do not supply this heat.

<RecipeFor id="herbalwitch:stations/kettle/station" />

<RecipeFor id="herbalwitch:stations/stove/station" />

<!-- scene:heat -->

A functional hearth is another heat source: put the vessel on the chimney top, not beside the fire.
An extinguished or waterlogged heat source supplies no heat.

Fill a glass bottle by using it on water. Open the kettle, put the powders in separate ingredient slots
and the water bottle in the base-liquid slot. Follow the card for amounts, packaging and returned containers.
The default recipe uses the water bottle as the drink's container. Brewing starts automatically when the inputs and heat match.

<HerbalRecipe id="herbalwitch:processing/steeping/restful_infusion" />

If progress stops, check the fuel and heat arrangement, remove unrelated ingredients, check the base-liquid slot,
and make space for the result and any returns. The process advances while the area is loaded.

## Use your preparation

Take the finished Restful Infusion, hold it and use it until you finish drinking.
You receive an empty bottle back. Keep growing herbs for your next batch.

[Return to the path](../path/start.md)
