package com.clothingstore.clothingstoreapp.model;

public abstract class BaseEntity {
    protected int id;

    protected BaseEntity(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}

