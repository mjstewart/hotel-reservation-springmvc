package com.demo.reservation.testcheckboxes;

import java.util.List;

public class Person {
    private Long id;
    private List<Drink> drinks;

    public Person(Long id) {
        this.id = id;
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

    @Override
    public String toString() {
        return "Person{" +
                "id=" + id +
                ", drinks=" + drinks +
                '}';
    }
}
