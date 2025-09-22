package food.values;

import food.domain.Food;

public record FoodSelection(Food food, Integer amount) {
    public static final FoodSelection NONE = new FoodSelection(new Food(), -1);
}
