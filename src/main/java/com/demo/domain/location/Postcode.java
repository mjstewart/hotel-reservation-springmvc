package com.demo.domain.location;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.Objects;

@Embeddable
public class Postcode {
    @Column(nullable = false)
    @Pattern(regexp = "[0-9]{4}", message = "Postcode must be 4 digits")
    @NotNull(message = "required")
    private String value;

    public Postcode(String value) {
        this.value = value;
    }

    public Postcode() {
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Postcode postcode = (Postcode) o;
        return Objects.equals(value, postcode.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "Postcode{" +
                "value='" + value + '\'' +
                '}';
    }
}
