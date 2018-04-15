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
     * @return The sort field and direction expected by spring {@code PagingAndSortingRepository}
     */
    public String withSortField(String field) {
        if (this == NONE) {
            return field;
        }
        return field + "," + value;
    }
}
