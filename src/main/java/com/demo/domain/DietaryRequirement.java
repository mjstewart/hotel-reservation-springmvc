package com.demo.domain;

public enum DietaryRequirement {
    Vegan("Vegan"),
    Vegetarian("Vegetarian"),
    GlutenIntolerant("Gluten Intolerant"),
    LactoseIntolerant("Lactose Intolerant");

    private String description;

    DietaryRequirement(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name();
    }
}
