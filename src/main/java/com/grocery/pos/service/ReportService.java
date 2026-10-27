package com.grocery.pos.service;

import com.grocery.pos.dto.GstReturnDto;
import com.grocery.pos.dto.ProfitLossDto;
import com.grocery.pos.dto.TrialBalanceDto;
import com.grocery.pos.model.*;
import com.grocery.pos.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ReportService {

    private final SaleRepository saleRepository;
    private final SaleReturnRepository saleReturnRepository;
    private final ProductReturnRepository productReturnRepository;
    private final PurchaseInvoiceRepository purchaseInvoiceRepository;
    private final ProductRepository productRepository;

    public ReportService(SaleRepository saleRepository,
                         SaleReturnRepository saleReturnRepository,
                         ProductReturnRepository productReturnRepository,
                         PurchaseInvoiceRepository purchaseInvoiceRepository,
                         ProductRepository productRepository) {
        this.saleRepository = saleRepository;
        this.saleReturnRepository = saleReturnRepository;
        this.productReturnRepository = productReturnRepository;
        this.purchaseInvoiceRepository = purchaseInvoiceRepository;
        this.productRepository = productRepository;
    }

    /**
     * Profit and Loss Report calculation
     */
    public ProfitLossDto generateProfitAndLossReport(LocalDate startDate, LocalDate endDate) {
        if (startDate == null) startDate = LocalDate.now().withDayOfMonth(1);
        if (endDate == null) endDate = LocalDate.now();

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        ProfitLossDto dto = new ProfitLossDto();
        dto.setStartDate(startDate);
        dto.setEndDate(endDate);

        List<Sale> sales = saleRepository.findBySaleDateBetweenOrderBySaleDateDesc(startDateTime, endDateTime);
        BigDecimal grossSales = BigDecimal.ZERO;
        BigDecimal discounts = BigDecimal.ZERO;
        BigDecimal outputGst = BigDecimal.ZERO;
        BigDecimal cogs = BigDecimal.ZERO;

        for (Sale sale : sales) {
            grossSales = grossSales.add(sale.getSubtotal() != null ? sale.getSubtotal() : BigDecimal.ZERO);
            discounts = discounts.add(sale.getDiscountAmount() != null ? sale.getDiscountAmount() : BigDecimal.ZERO);
            outputGst = outputGst.add(sale.getTaxAmount() != null ? sale.getTaxAmount() : BigDecimal.ZERO);

            // Compute COGS: sum of each item's cost price * quantity
            if (sale.getItems() != null) {
                for (SaleItem item : sale.getItems()) {
                    if (item.getProduct() != null && item.getProduct().getCostPrice() != null) {
                        BigDecimal itemCost = item.getProduct().getCostPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                        cogs = cogs.add(itemCost);
                    }
                }
            }
        }

        // Sale returns in this date range
        List<SaleReturn> returns = saleReturnRepository.findByReturnDateBetweenOrderByReturnDateDesc(startDateTime, endDateTime);
        BigDecimal saleReturnsTotal = BigDecimal.ZERO;
        BigDecimal cogsRestoredFromReturns = BigDecimal.ZERO;

        for (SaleReturn sr : returns) {
            saleReturnsTotal = saleReturnsTotal.add(sr.getRefundAmount() != null ? sr.getRefundAmount() : BigDecimal.ZERO);
            if (sr.getItems() != null) {
                for (SaleReturnItem sri : sr.getItems()) {
                    if (sri.getProduct() != null && sri.getProduct().getCostPrice() != null) {
                        cogsRestoredFromReturns = cogsRestoredFromReturns.add(
                                sri.getProduct().getCostPrice().multiply(BigDecimal.valueOf(sri.getQuantity())));
                    }
                }
            }
        }

        // Net COGS adjusted for customer returned goods restored to stock
        BigDecimal netCogs = cogs.subtract(cogsRestoredFromReturns);
        if (netCogs.compareTo(BigDecimal.ZERO) < 0) netCogs = BigDecimal.ZERO;

        BigDecimal netSalesRevenue = grossSales.subtract(discounts).subtract(saleReturnsTotal);
        if (netSalesRevenue.compareTo(BigDecimal.ZERO) < 0) netSalesRevenue = BigDecimal.ZERO;

        BigDecimal grossProfit = netSalesRevenue.subtract(netCogs);
        BigDecimal netProfit = grossProfit; // In retail POS context, gross profit equals net trading profit

        dto.setGrossSales(grossSales);
        dto.setSaleDiscounts(discounts);
        dto.setSaleReturns(saleReturnsTotal);
        dto.setNetSalesRevenue(netSalesRevenue);
        dto.setCostOfGoodsSold(netCogs);
        dto.setGrossProfit(grossProfit);
        dto.setOutputGst(outputGst);
        dto.setNetProfit(netProfit);
        dto.setTotalSalesCount(sales.size());
        dto.setTotalReturnsCount(returns.size());

        if (netSalesRevenue.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal gpMargin = grossProfit.multiply(BigDecimal.valueOf(100)).divide(netSalesRevenue, 2, RoundingMode.HALF_UP);
            dto.setGrossProfitMargin(gpMargin);
            dto.setNetProfitMargin(gpMargin);
        }

        return dto;
    }

    /**
     * Trial Balance Report with balanced Debit / Credit columns
     */
    public TrialBalanceDto generateTrialBalanceReport(LocalDate startDate, LocalDate endDate) {
        if (startDate == null) startDate = LocalDate.now().withDayOfMonth(1);
        if (endDate == null) endDate = LocalDate.now();

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        TrialBalanceDto dto = new TrialBalanceDto();
        dto.setStartDate(startDate);
        dto.setEndDate(endDate);

        // 1. Sales Revenue (Credit)
        BigDecimal salesRevenue = saleRepository.sumGrandTotalBetween(startDateTime, endDateTime);
        if (salesRevenue == null) salesRevenue = BigDecimal.ZERO;

        // 2. Sales Returns & Allowances (Debit)
        BigDecimal saleReturns = saleReturnRepository.sumRefundAmountBetween(startDateTime, endDateTime);
        if (saleReturns == null) saleReturns = BigDecimal.ZERO;

        // 3. Purchase Invoices / Goods Received (Debit)
        BigDecimal purchasesTotal = purchaseInvoiceRepository.sumGrandTotalBetween(startDate, endDate);
        if (purchasesTotal == null) purchasesTotal = BigDecimal.ZERO;

        // 4. Product Returns to Suppliers (Credit)
        BigDecimal productReturns = productReturnRepository.sumTotalRefundAmountBetween(startDateTime, endDateTime);
        if (productReturns == null) productReturns = BigDecimal.ZERO;

        // 5. Current Inventory Valuation (Debit asset)
        BigDecimal inventoryValuation = productRepository.calculateTotalInventoryValue();
        if (inventoryValuation == null) inventoryValuation = BigDecimal.ZERO;

        // 6. Output Tax / GST (Credit liability)
        BigDecimal gstLiability = saleRepository.sumTaxAmountBetween(startDateTime, endDateTime);
        if (gstLiability == null) gstLiability = BigDecimal.ZERO;

        // 7. Cash & Bank Receipts Account (Debit asset: Net cash/bank received from sales minus returns)
        BigDecimal netCashReceived = salesRevenue.subtract(saleReturns);
        if (netCashReceived.compareTo(BigDecimal.ZERO) < 0) netCashReceived = BigDecimal.ZERO;

        // 8. Accounts Payable / Supplier Clearing (Credit liability: Net purchases minus returns)
        BigDecimal accountsPayable = purchasesTotal.subtract(productReturns);
        if (accountsPayable.compareTo(BigDecimal.ZERO) < 0) accountsPayable = BigDecimal.ZERO;

        // 9. Retained Store Capital / Equity Balancing Entry
        BigDecimal debitSum = netCashReceived.add(saleReturns).add(purchasesTotal).add(inventoryValuation);
        BigDecimal creditSum = salesRevenue.add(productReturns).add(accountsPayable).add(gstLiability);

        BigDecimal equityDebit = BigDecimal.ZERO;
        BigDecimal equityCredit = BigDecimal.ZERO;

        if (debitSum.compareTo(creditSum) < 0) {
            equityDebit = creditSum.subtract(debitSum);
        } else if (debitSum.compareTo(creditSum) > 0) {
            equityCredit = debitSum.subtract(creditSum);
        }

        // Add standardized ledger accounts
        dto.addAccount("1010", "Cash & Bank Register Account", "Asset", netCashReceived, BigDecimal.ZERO);
        dto.addAccount("1020", "Merchandise Inventory (At Cost)", "Asset", inventoryValuation, BigDecimal.ZERO);
        dto.addAccount("2010", "Accounts Payable (Trade Suppliers)", "Liability", BigDecimal.ZERO, accountsPayable);
        dto.addAccount("2020", "Output GST Tax Liability", "Liability", BigDecimal.ZERO, gstLiability);
        dto.addAccount("3010", "Store Capital & Retained Earnings", "Equity", equityDebit, equityCredit);
        dto.addAccount("4010", "Gross Sales Revenue", "Revenue", BigDecimal.ZERO, salesRevenue);
        dto.addAccount("4020", "Sales Returns & Allowances", "Contra-Revenue", saleReturns, BigDecimal.ZERO);
        dto.addAccount("5010", "Merchandise Purchases (Inward)", "Expense", purchasesTotal, BigDecimal.ZERO);
        dto.addAccount("5020", "Purchase Returns to Suppliers", "Contra-Expense", BigDecimal.ZERO, productReturns);

        return dto;
    }

    /**
     * VAT / GST Tax Return Report
     */
    public GstReturnDto generateGstReturnReport(LocalDate startDate, LocalDate endDate) {
        if (startDate == null) startDate = LocalDate.now().withDayOfMonth(1);
        if (endDate == null) endDate = LocalDate.now();

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        GstReturnDto dto = new GstReturnDto();
        dto.setStartDate(startDate);
        dto.setEndDate(endDate);

        // Sales Taxable and GST
        BigDecimal taxableSales = saleRepository.sumSubtotalBetween(startDateTime, endDateTime);
        BigDecimal outputGst = saleRepository.sumTaxAmountBetween(startDateTime, endDateTime);

        // Purchase Inward Taxable and GST
        BigDecimal taxablePurchases = purchaseInvoiceRepository.sumTotalAmountBetween(startDate, endDate);
        BigDecimal inputGst = purchaseInvoiceRepository.sumTaxAmountBetween(startDate, endDate);

        // Sales Returns
        BigDecimal saleReturns = saleReturnRepository.sumRefundAmountBetween(startDateTime, endDateTime);
        BigDecimal saleReturnsGst = (saleReturns != null) ? saleReturns.multiply(BigDecimal.valueOf(5)).divide(BigDecimal.valueOf(105), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;

        // Product Returns
        BigDecimal productReturns = productReturnRepository.sumTotalRefundAmountBetween(startDateTime, endDateTime);
        BigDecimal productReturnsGst = (productReturns != null) ? productReturns.multiply(BigDecimal.valueOf(5)).divide(BigDecimal.valueOf(105), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;

        dto.setTaxableSales(taxableSales != null ? taxableSales : BigDecimal.ZERO);
        dto.setOutputGstTotal(outputGst != null ? outputGst : BigDecimal.ZERO);
        dto.setTaxablePurchases(taxablePurchases != null ? taxablePurchases : BigDecimal.ZERO);
        dto.setInputGstTotal(inputGst != null ? inputGst : BigDecimal.ZERO);
        dto.setSaleReturnsTotal(saleReturns != null ? saleReturns : BigDecimal.ZERO);
        dto.setSaleReturnsGst(saleReturnsGst);
        dto.setProductReturnsTotal(productReturns != null ? productReturns : BigDecimal.ZERO);
        dto.setProductReturnsGst(productReturnsGst);

        dto.recalculateNet();
        return dto;
    }

    public List<SaleReturn> getSaleReturnsReport(LocalDate startDate, LocalDate endDate) {
        if (startDate == null) startDate = LocalDate.now().withDayOfMonth(1);
        if (endDate == null) endDate = LocalDate.now();
        return saleReturnRepository.findByReturnDateBetweenOrderByReturnDateDesc(
                startDate.atStartOfDay(), endDate.atTime(23, 59, 59));
    }

    public List<ProductReturn> getProductReturnsReport(LocalDate startDate, LocalDate endDate) {
        if (startDate == null) startDate = LocalDate.now().withDayOfMonth(1);
        if (endDate == null) endDate = LocalDate.now();
        return productReturnRepository.findByReturnDateBetweenOrderByReturnDateDesc(
                startDate.atStartOfDay(), endDate.atTime(23, 59, 59));
    }
}
