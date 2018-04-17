package com.demo.thymeleaf.utils;

public enum SortDirection {
    ASC("asc"),
    DESC("desc"),
    NONE("");

    public String value;

    SortDirection(String value) {
        this.value = value;
    }

    public static SortDirection from(String value) {
        switch (value) {
            case "asc":
                return ASC;
            case "desc":
                return DESC;
            default:
                return NONE;
        }
    }

    public String getValue() {
        return value;
    }

    /**
     * Toggles the current sort direction from {@code ASC} to {@code DESC} and vice versa.
     * The default sort direction is needed in order to apply the correct direction change in cases where no direction
     * is specified.
     *
     * <p>For example, {@code sort=country} has no direction specified, so toggling could mean changing to ASC or DESC.
     * If the default direction is {@code ASC}, the first toggle would change it to {@code DESC} etc.</p>
     *
     * @param defaultDirection The default sort direction.
     * @return The new sort direction.
     */
    public SortDirection toggle(SortDirection defaultDirection) {
        switch (this) {
            case ASC:
                return DESC;
            case DESC:
                return ASC;
            default:
                return defaultDirection == ASC ? DESC : ASC;
        }
    }

    /**
     * If {@code NONE}, only the field will be used resulting in spring using the default sort direction.
     *
     * @param field The sort field.
     * @return The sort field and direction format expected by spring {@code PagingAndSortingRepository}
     */
    public String withSortField(String field) {
        if (this == NONE) {
            return field;
        }
        return field + "," + value;
    }
}
