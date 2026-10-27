package com.grocery.pos.service;

import com.grocery.pos.dto.RealtimeEvent;
import com.grocery.pos.dto.RealtimeSummaryDto;
import com.grocery.pos.model.*;
import com.grocery.pos.repository.ProductRepository;
import com.grocery.pos.repository.ProductReturnRepository;
import com.grocery.pos.repository.SaleRepository;
import com.grocery.pos.repository.SaleReturnRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class RealtimeSyncService {

    private static final Logger log = LoggerFactory.getLogger(RealtimeSyncService.class);

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();
    private final ConcurrentLinkedDeque<RealtimeEvent> recentEvents = new ConcurrentLinkedDeque<>();

    private final SaleRepository saleRepository;
    private final SaleReturnRepository saleReturnRepository;
    private final ProductReturnRepository productReturnRepository;
    private final ProductRepository productRepository;

    public RealtimeSyncService(SaleRepository saleRepository,
                               SaleReturnRepository saleReturnRepository,
                               ProductReturnRepository productReturnRepository,
                               ProductRepository productRepository) {
        this.saleRepository = saleRepository;
        this.saleReturnRepository = saleReturnRepository;
        this.productReturnRepository = productReturnRepository;
        this.productRepository = productRepository;
    }

    /**
     * Registers a new client browser for Server-Sent Events (SSE).
     */
    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(180_000L); // 3 minutes timeout

        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> {
            emitter.complete();
            emitters.remove(emitter);
        });
        emitter.onError(e -> {
            emitter.complete();
            emitters.remove(emitter);
        });

        emitters.add(emitter);

        // Send instant welcome / connected handshake
        try {
            RealtimeEvent handshake = new RealtimeEvent(
                    "CONNECTED",
                    "FM-SYNC",
                    BigDecimal.ZERO,
                    "FreshMart POS",
                    "System",
                    "Real-time event stream active"
            );
            emitter.send(SseEmitter.event().name("realtime").data(handshake));
        } catch (IOException ex) {
            emitters.remove(emitter);
        }

        return emitter;
    }

    /**
     * Broadcasts an event to all connected browsers.
     */
    public void broadcast(RealtimeEvent event) {
        if (event == null) return;

        // Maintain in-memory recent event ring buffer (max 30)
        recentEvents.addFirst(event);
        while (recentEvents.size() > 30) {
            recentEvents.removeLast();
        }

        List<SseEmitter> deadEmitters = new ArrayList<>();
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name("realtime").data(event));
            } catch (Exception ex) {
                deadEmitters.add(emitter);
            }
        }
        if (!deadEmitters.isEmpty()) {
            emitters.removeAll(deadEmitters);
        }
    }

    /**
     * Periodic 25-second heartbeat ping to keep HTTP/2 and reverse proxies open.
     */
    @Scheduled(fixedRate = 25000)
    public void sendHeartbeat() {
        if (emitters.isEmpty()) return;

        RealtimeEvent ping = new RealtimeEvent(
                "HEARTBEAT",
                "PING",
                BigDecimal.ZERO,
                "Server",
                "System",
                "Keep-alive heartbeat"
        );

        List<SseEmitter> deadEmitters = new ArrayList<>();
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name("heartbeat").data(ping));
            } catch (Exception ex) {
                deadEmitters.add(emitter);
            }
        }
        if (!deadEmitters.isEmpty()) {
            emitters.removeAll(deadEmitters);
        }
    }

    // =========================================================================
    // Real-Time Notification Triggers
    // =========================================================================

    public void notifySale(Sale sale) {
        if (sale == null) return;
        RealtimeEvent event = new RealtimeEvent(
                "SALE_CREATED",
                sale.getInvoiceNumber(),
                sale.getGrandTotal(),
                sale.getCustomerName() != null ? sale.getCustomerName() : "Walk-in Customer",
                sale.getCashierName() != null ? sale.getCashierName() : "Cashier",
                "Sale #" + sale.getInvoiceNumber() + " completed: ₹" + (sale.getGrandTotal() != null ? sale.getGrandTotal().toPlainString() : "0.00")
        );
        broadcast(event);
    }

    public void notifySaleReturn(SaleReturn sr) {
        if (sr == null) return;
        String party = (sr.getOriginalSale() != null && sr.getOriginalSale().getCustomerName() != null)
                ? sr.getOriginalSale().getCustomerName() : "Customer";
        RealtimeEvent event = new RealtimeEvent(
                "SALE_RETURN_CREATED",
                sr.getReturnNumber(),
                sr.getRefundAmount(),
                party,
                sr.getProcessedBy() != null ? sr.getProcessedBy() : "Cashier",
                "Customer Return #" + sr.getReturnNumber() + " processed: ₹" + (sr.getRefundAmount() != null ? sr.getRefundAmount().toPlainString() : "0.00") + " refunded"
        );
        broadcast(event);
    }

    public void notifyProductReturn(ProductReturn pr) {
        if (pr == null) return;
        String supplier = (pr.getSupplier() != null && pr.getSupplier().getName() != null)
                ? pr.getSupplier().getName() : "Vendor";
        RealtimeEvent event = new RealtimeEvent(
                "PRODUCT_RETURN_CREATED",
                pr.getReturnNumber(),
                pr.getTotalRefundAmount(),
                supplier,
                pr.getProcessedBy() != null ? pr.getProcessedBy() : "Admin",
                "Vendor Return #" + pr.getReturnNumber() + " logged: ₹" + (pr.getTotalRefundAmount() != null ? pr.getTotalRefundAmount().toPlainString() : "0.00") + " debit note"
        );
        broadcast(event);
    }

    public void notifyPurchaseInvoice(PurchaseInvoice pi) {
        if (pi == null) return;
        String supplier = (pi.getSupplier() != null && pi.getSupplier().getName() != null)
                ? pi.getSupplier().getName() : "Vendor";
        RealtimeEvent event = new RealtimeEvent(
                "PURCHASE_POSTED",
                pi.getInvoiceNumber(),
                pi.getGrandTotal(),
                supplier,
                pi.getCreatedBy() != null ? pi.getCreatedBy() : "Staff",
                "Purchase Invoice #" + pi.getInvoiceNumber() + " posted: Inward stock updated"
        );
        broadcast(event);
    }

    /**
     * Generates a real-time live summary snapshot for dashboards and topbars.
     */
    public RealtimeSummaryDto getLiveSummary() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(23, 59, 59);

        RealtimeSummaryDto dto = new RealtimeSummaryDto();
        dto.setServerTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm:ss")));

        // 1. Today's Sales
        List<Sale> todaySales = saleRepository.findBySaleDateBetweenOrderBySaleDateDesc(startOfDay, endOfDay);
        dto.setTodaySalesCount(todaySales.size());
        BigDecimal salesSum = todaySales.stream()
                .map(Sale::getGrandTotal)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        dto.setTodaySalesTotal(salesSum);

        // 2. Today's Customer Sale Returns
        List<SaleReturn> todayReturns = saleReturnRepository.findByReturnDateBetweenOrderByReturnDateDesc(startOfDay, endOfDay);
        dto.setTodayReturnsCount(todayReturns.size());
        BigDecimal returnSum = todayReturns.stream()
                .map(SaleReturn::getRefundAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        dto.setTodayRefundTotal(returnSum);

        // 3. Today's Product Returns to Suppliers
        List<ProductReturn> todayProdReturns = productReturnRepository.findByReturnDateBetweenOrderByReturnDateDesc(startOfDay, endOfDay);
        dto.setTodayProductReturnsCount(todayProdReturns.size());
        BigDecimal prodReturnSum = todayProdReturns.stream()
                .map(ProductReturn::getTotalRefundAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        dto.setTodayProductRefundTotal(prodReturnSum);

        // 4. Net today's revenue (Sales minus Customer Returns)
        BigDecimal netToday = salesSum.subtract(returnSum);
        if (netToday.compareTo(BigDecimal.ZERO) < 0) netToday = BigDecimal.ZERO;
        dto.setGrossProfitToday(netToday);

        // 5. Inventory Metrics
        long lowStock = productRepository.countLowStockProducts();
        dto.setLowStockCount(lowStock);
        dto.setTotalProducts(productRepository.count());
        BigDecimal invVal = productRepository.calculateTotalInventoryValue();
        dto.setTotalInventoryValue(invVal != null ? invVal : BigDecimal.ZERO);

        // 6. Recent Events
        dto.setRecentEvents(new ArrayList<>(recentEvents));

        return dto;
    }

    public List<RealtimeEvent> getRecentEvents() {
        return new ArrayList<>(recentEvents);
    }

    public int getConnectedClientsCount() {
        return emitters.size();
    }
}
