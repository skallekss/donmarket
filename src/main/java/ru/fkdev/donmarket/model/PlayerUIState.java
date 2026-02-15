package ru.fkdev.donmarket.model;

import java.util.UUID;

public class PlayerUIState {

    private String currentMenu;
    private int page;
    private SortMode sortMode = SortMode.BEST_RATIO;
    private Category categoryFilter = Category.ALL;
    private boolean pendingChatInput;
    private String pendingProductType;
    private String pendingProductKey;
    private String pendingDuration;
    private double pendingMinPrice;
    private Category pendingCategory;
    private long chatInputTimeout;
    private int selectedDurationIndex;
    private String pendingDurationCode;

    public PlayerUIState() {
    }

    public String getCurrentMenu() { return currentMenu; }
    public void setCurrentMenu(String currentMenu) { this.currentMenu = currentMenu; }

    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }

    public SortMode getSortMode() { return sortMode; }
    public void setSortMode(SortMode sortMode) { this.sortMode = sortMode; }

    public Category getCategoryFilter() { return categoryFilter; }
    public void setCategoryFilter(Category categoryFilter) { this.categoryFilter = categoryFilter; }

    public boolean isPendingChatInput() { return pendingChatInput; }
    public void setPendingChatInput(boolean pendingChatInput) { this.pendingChatInput = pendingChatInput; }

    public String getPendingProductType() { return pendingProductType; }
    public void setPendingProductType(String pendingProductType) { this.pendingProductType = pendingProductType; }

    public String getPendingProductKey() { return pendingProductKey; }
    public void setPendingProductKey(String pendingProductKey) { this.pendingProductKey = pendingProductKey; }

    public String getPendingDuration() { return pendingDuration; }
    public void setPendingDuration(String pendingDuration) { this.pendingDuration = pendingDuration; }

    public double getPendingMinPrice() { return pendingMinPrice; }
    public void setPendingMinPrice(double pendingMinPrice) { this.pendingMinPrice = pendingMinPrice; }

    public Category getPendingCategory() { return pendingCategory; }
    public void setPendingCategory(Category pendingCategory) { this.pendingCategory = pendingCategory; }

    public long getChatInputTimeout() { return chatInputTimeout; }
    public void setChatInputTimeout(long chatInputTimeout) { this.chatInputTimeout = chatInputTimeout; }

    public int getSelectedDurationIndex() { return selectedDurationIndex; }
    public void setSelectedDurationIndex(int selectedDurationIndex) { this.selectedDurationIndex = selectedDurationIndex; }

    public String getPendingDurationCode() { return pendingDurationCode; }
    public void setPendingDurationCode(String pendingDurationCode) { this.pendingDurationCode = pendingDurationCode; }

    public void clearPending() {
        pendingChatInput = false;
        pendingProductType = null;
        pendingProductKey = null;
        pendingDuration = null;
        pendingDurationCode = null;
        pendingMinPrice = 0;
        pendingCategory = null;
        chatInputTimeout = 0;
    }
}
