package lld.DesignProblems.inventoryManagementSystem.model;

/**
 * Broad classification for an inventory item. Kept as a fixed enum for this
 * demo; a production system would likely back this with its own table/service.
 */
public enum Category {
    ELECTRONICS,
    GROCERY,
    APPAREL,
    FURNITURE,
    TOYS,
    OTHER
}
