package com.lockedin.careercenter.model;

public enum EventPriority {
    HIGH(1),
    MEDIUM(2),
    LOW(3);

    private final int sortOrder;

    EventPriority(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public int getSortOrder() {
        return sortOrder;
    }
}
