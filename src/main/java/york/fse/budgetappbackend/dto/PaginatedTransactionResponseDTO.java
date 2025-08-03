package york.fse.budgetappbackend.dto;

import java.util.List;

public class PaginatedTransactionResponseDTO {
    private List<TransactionResponseDTO> transactions;
    private int currentPage;
    private int totalPages;
    private long totalElements;
    private int pageSize;

    public PaginatedTransactionResponseDTO() {}

    public PaginatedTransactionResponseDTO(List<TransactionResponseDTO> transactions, int currentPage,
                                           int totalPages, long totalElements, int pageSize) {
        this.transactions = transactions;
        this.currentPage = currentPage;
        this.totalPages = totalPages;
        this.totalElements = totalElements;
        this.pageSize = pageSize;
    }

    // Getters and setters
    public List<TransactionResponseDTO> getTransactions() { return transactions; }
    public void setTransactions(List<TransactionResponseDTO> transactions) { this.transactions = transactions; }
    public int getCurrentPage() { return currentPage; }
    public void setCurrentPage(int currentPage) { this.currentPage = currentPage; }
    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
    public long getTotalElements() { return totalElements; }
    public void setTotalElements(long totalElements) { this.totalElements = totalElements; }
    public int getPageSize() { return pageSize; }
    public void setPageSize(int pageSize) { this.pageSize = pageSize; }
}