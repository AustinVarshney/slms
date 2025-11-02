package com.java.slms.util;

public enum PromotionStatus {
    PENDING,      // Promotion assigned but not yet executed
    PROMOTED,     // Student successfully promoted to next class
    GRADUATED,    // Student graduated (no further promotion)
    DETAINED      // Student detained in same class
}
