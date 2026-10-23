package io.github.dirtyredz.herbalwitch.client.compat;

/** Geometry shared by EMI and JEI. Text and counted displays belong to client.recipe.RecipeText. */
public final class ViewerLayout {
    public static final int WIDTH = 132;
    public static final int HEIGHT = 74;
    public static final int SLOT = 18;
    public static final int ARROW_X = 58;
    public static final int ARROW_Y = 10;
    public static final int RESULT_X = 88;
    public static final int RESULT_Y = 10;
    public static final int TEXT_Y = 40;
    /** The requirements line under the footer (witch recipes). */
    public static final int REQUIREMENTS_Y = 50;

    private ViewerLayout() {
    }

    public static int ingredientX(int index) {
        return index * SLOT;
    }

    public static final int INGREDIENT_Y = 0;
    public static final int BASE_X = 0;
    public static final int PACKAGING_X = SLOT;
    public static final int LOWER_Y = SLOT + 2;

    public static int returnX(int index) {
        return RESULT_X + SLOT + 4 + index * SLOT;
    }

}
