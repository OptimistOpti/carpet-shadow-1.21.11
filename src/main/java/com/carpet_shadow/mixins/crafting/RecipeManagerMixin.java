package com.carpet_shadow.mixins.crafting;

import org.spongepowered.asm.mixin.Mixin;
import net.minecraft.recipe.RecipeManager;

// FIXME [PORT 1.21.11] see PORTING_NOTES.md #5.
//  DISABLED (removed from carpet-shadow.mixins.json) - this mixin injected into the exact
//  bytecode shape of RecipeManager#apply as it looked in 1.20.1 (a
//  Map<RecipeType<?>, ImmutableMap.Builder<Identifier, Recipe<?>>> built while iterating raw
//  JSON, captured via @Local(ordinal=...)). Recipe loading has been reworked multiple times
//  since (datapack/dynamic registries, RegistryOps-based codec decoding), so:
//    - RecipeManager#apply almost certainly no longer has this local-variable shape at all.
//    - Identifier's constructor is now private (use Identifier.of(namespace, path)).
//    - BookCloningRecipe's constructor no longer takes an Identifier (recipes don't carry their
//      own ID anymore - only CraftingRecipeCategory, e.g. `new BookCloningRecipe(category)`).
//    - Recipe's getRemainder/matches/craft/fits overrides may have also changed signature.
//  This feature (crafting a shadow item copy via ender chest + item) needs a proper rewrite once
//  someone can check the actual 1.21.11 RecipeManager/BookCloningRecipe/Recipe sources - likely
//  via a completely different registration approach (e.g. hooking recipe registration after
//  load, rather than reproducing the old ImmutableMap.Builder local-variable capture).
//  Original implementation preserved in git history for reference.
@Mixin(RecipeManager.class)
public class RecipeManagerMixin {
}
