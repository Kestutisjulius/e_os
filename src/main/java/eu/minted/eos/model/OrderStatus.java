package eu.minted.eos.model;

public enum OrderStatus {
    PENDING, // Užsakymas priimtas, laukia apdorojimo
    SHIPPED, // Užsakymas išsiųstas
    DELIVERED, // Užsakymas pristatytas
    CANCELED // Užsakymas atšauktas
}
