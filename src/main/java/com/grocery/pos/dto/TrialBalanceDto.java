package com.grocery.pos.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TrialBalanceDto {

    private LocalDate startDate;
    private LocalDate endDate;

    private List<TrialBalanceAccountDto> accounts = new ArrayList<>();
    private BigDecimal totalDebit = BigDecimal.ZERO;
    private BigDecimal totalCredit = BigDecimal.ZERO;

    public TrialBalanceDto() {
    }

    public void addAccount(String code, String accountName, String category, BigDecimal debit, BigDecimal credit) {
        BigDecimal d = debit != null ? debit : BigDecimal.ZERO;
        BigDecimal c = credit != null ? credit : BigDecimal.ZERO;
        accounts.add(new TrialBalanceAccountDto(code, accountName, category, d, c));
        this.totalDebit = this.totalDebit.add(d);
        this.totalCredit = this.totalCredit.add(c);
    }

    public boolean isBalanced() {
        return totalDebit.compareTo(totalCredit) == 0;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public List<TrialBalanceAccountDto> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<TrialBalanceAccountDto> accounts) {
        this.accounts = accounts;
    }

    public BigDecimal getTotalDebit() {
        return totalDebit;
    }

    public void setTotalDebit(BigDecimal totalDebit) {
        this.totalDebit = totalDebit;
    }

    public BigDecimal getTotalCredit() {
        return totalCredit;
    }

    public void setTotalCredit(BigDecimal totalCredit) {
        this.totalCredit = totalCredit;
    }

    public static class TrialBalanceAccountDto {
        private String code;
        private String accountName;
        private String category;
        private BigDecimal debit;
        private BigDecimal credit;

        public TrialBalanceAccountDto(String code, String accountName, String category, BigDecimal debit, BigDecimal credit) {
            this.code = code;
            this.accountName = accountName;
            this.category = category;
            this.debit = debit;
            this.credit = credit;
        }

        public String getCode() {
            return code;
        }

        public String getAccountName() {
            return accountName;
        }

        public String getCategory() {
            return category;
        }

        public BigDecimal getDebit() {
            return debit;
        }

        public BigDecimal getCredit() {
            return credit;
        }
    }
}
