// Herbal Witch M12 KubeJS qualification: recipe JSON through KubeJS, no bespoke API.
// Adds a two-wheat-to-bread grinding recipe and removes the purple dye grinding recipe, then reports
// what the server's recipe manager holds once the data has loaded.
ServerEvents.recipes(event => {
  event.custom({
    type: 'herbalwitch:processing/grinding',
    duration: 40,
    ingredients: [{ count: 2, ingredient: { item: 'minecraft:wheat' } }],
    result: { item: 'minecraft:bread' }
  }).id('kubejs:herbalwitch_qualification/wheat_to_bread')
  event.remove({ id: 'herbalwitch:processing/grinding/purple_dye' })
})

ServerEvents.loaded(event => {
  const recipes = event.server.recipeManager
  console.info('HWQ kubejs added=' + recipes.byKey('kubejs:herbalwitch_qualification/wheat_to_bread').isPresent())
  console.info('HWQ kubejs removed=' + !recipes.byKey('herbalwitch:processing/grinding/purple_dye').isPresent())
})
