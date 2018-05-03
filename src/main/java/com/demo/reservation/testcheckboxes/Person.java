package com.demo.reservation.testcheckboxes;

import java.util.List;

public class Person {
    private Long id;
    private String name;
    private List<Drink> drinks;
    private List<EnumDrink> enumDrinks;

    public Person(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public List<Drink> getDrinks() {
        return drinks;
    }

    public void setDrinks(List<Drink> drinks) {
        this.drinks = drinks;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public List<EnumDrink> getEnumDrinks() {
        return enumDrinks;
    }

    public void setEnumDrinks(List<EnumDrink> enumDrinks) {
        this.enumDrinks = enumDrinks;
    }


    @Override
    public String toString() {
        return "Person{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", drinks=" + drinks +
                ", enumDrinks=" + enumDrinks +
                '}';
    }
}
