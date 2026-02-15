package ru.fkdev.donmarket.model;

import java.util.UUID;

public class MarketOrder {

    private int id;
    private UUID owner;
    private String ownerName;
    private Category category;
    private String productType;
    private String productKey;
    private String durationType;
    private double coinsMin;
    private double coinsOffer;
    private int tokenPrice;
    private long createdAt;
    private long expiresAt;
    private OrderStatus status;
    private String extraJson;

    public MarketOrder() {
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public UUID getOwner() { return owner; }
    public void setOwner(UUID owner) { this.owner = owner; }

    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public String getProductType() { return productType; }
    public void setProductType(String productType) { this.productType = productType; }

    public String getProductKey() { return productKey; }
    public void setProductKey(String productKey) { this.productKey = productKey; }

    public String getDurationType() { return durationType; }
    public void setDurationType(String durationType) { this.durationType = durationType; }

    public double getCoinsMin() { return coinsMin; }
    public void setCoinsMin(double coinsMin) { this.coinsMin = coinsMin; }

    public double getCoinsOffer() { return coinsOffer; }
    public void setCoinsOffer(double coinsOffer) { this.coinsOffer = coinsOffer; }

    public int getTokenPrice() { return tokenPrice; }
    public void setTokenPrice(int tokenPrice) { this.tokenPrice = tokenPrice; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getExpiresAt() { return expiresAt; }
    public void setExpiresAt(long expiresAt) { this.expiresAt = expiresAt; }

    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }

    public String getExtraJson() { return extraJson; }
    public void setExtraJson(String extraJson) { this.extraJson = extraJson; }

    public long getRemainingMs() {
        return Math.max(0, expiresAt - System.currentTimeMillis());
    }

    public String getRemainingFormatted() {
        long ms = getRemainingMs();
        if (ms <= 0) return "Истёк";
        long totalSec = ms / 1000;
        long hours = totalSec / 3600;
        long minutes = (totalSec % 3600) / 60;
        if (hours > 0) {
            return hours + " ч. " + minutes + " мин.";
        }
        return minutes + " мин.";
    }

    public double getRate() {
        if (tokenPrice <= 0) return 0;
        return coinsOffer / tokenPrice;
    }
}
