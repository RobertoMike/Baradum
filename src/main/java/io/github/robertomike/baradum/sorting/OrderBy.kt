package io.github.robertomike.baradum.sorting;

public record OrderBy(String name, String internalName) {
    public OrderBy(String name) {
        this(name, name);
    }
}
