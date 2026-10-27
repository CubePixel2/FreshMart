/**
 * FreshMart Real-Time Live Sync Engine
 * Server-Sent Events (SSE) + Fallback Live Polling
 * Synchronizes Reports, Returns, and Sales in Real-Time
 */
(function () {
  'use strict';

  let eventSource = null;
  let isConnected = false;
  let lastSyncTimestamp = Date.now();
  let pollInterval = null;

  // Sound chime effect via Web Audio API (subtle, non-intrusive)
  function playNotificationChime() {
    try {
      const AudioContext = window.AudioContext || window.webkitAudioContext;
      if (!AudioContext) return;
      const ctx = new AudioContext();
      const osc = ctx.createOscillator();
      const gain = ctx.createGain();
      osc.type = 'sine';
      osc.frequency.setValueAtTime(587.33, ctx.currentTime); // D5
      osc.frequency.exponentialRampToValueAtTime(880, ctx.currentTime + 0.12); // A5
      gain.gain.setValueAtTime(0.08, ctx.currentTime);
      gain.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + 0.25);
      osc.connect(gain);
      gain.connect(ctx.destination);
      osc.start();
      osc.stop(ctx.currentTime + 0.25);
    } catch (e) {
      // Audio autoplay policy might restrict without user interaction
    }
  }

  // Helper to format currency
  function formatINR(val) {
    const num = Number(val) || 0;
    return '₹' + num.toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  }

  // Update Status Pill in Navbar
  function updateStatusPill(status, text, lastSyncText) {
    const pill = document.getElementById('realtime-status-pill');
    const dot = document.querySelector('.realtime-pulse-dot');
    const textEl = document.getElementById('realtime-status-text');
    const timeEl = document.getElementById('realtime-last-sync');

    if (!pill) return;

    if (dot) {
      dot.className = 'realtime-pulse-dot';
      if (status === 'connected') dot.classList.add('pulse-green');
      else if (status === 'reconnecting') dot.classList.add('pulse-amber');
      else dot.classList.add('pulse-red');
    }

    if (textEl && text) textEl.textContent = text;
    if (timeEl && lastSyncText) timeEl.textContent = lastSyncText;
  }

  // Show Toast alert for live updates
  function showLiveToast(title, message, iconClass, badgeText, badgeBg) {
    const container = document.getElementById('toast-container');
    if (!container) return;

    const toastId = 'toast-rt-' + Date.now();
    const html = `
      <div id="${toastId}" class="toast border-0 shadow-lg mb-2 text-dark bg-white" role="alert" aria-live="assertive" aria-atomic="true" style="border-radius: 12px; overflow: hidden;">
        <div class="toast-header border-0 bg-light py-2 px-3">
          <i class="${iconClass || 'bi bi-lightning-charge-fill text-success'} fs-5 me-2"></i>
          <strong class="me-auto small">${title || 'Real-Time Update'}</strong>
          <span class="badge ${badgeBg || 'bg-success-subtle text-success'} me-2 font-monospace" style="font-size: 0.68rem;">${badgeText || 'LIVE'}</span>
          <button type="button" class="btn-close" data-bs-dismiss="toast" aria-label="Close"></button>
        </div>
        <div class="toast-body py-2 px-3 small">
          ${message}
        </div>
      </div>
    `;

    container.insertAdjacentHTML('beforeend', html);
    const toastEl = document.getElementById(toastId);
    if (toastEl && window.bootstrap && bootstrap.Toast) {
      const bsToast = new bootstrap.Toast(toastEl, { delay: 6000 });
      bsToast.show();
      toastEl.addEventListener('hidden.bs.toast', () => toastEl.remove());
    }
  }

  // Main SSE Connection
  function initEventSource() {
    if (eventSource) {
      try { eventSource.close(); } catch (e) { }
    }

    updateStatusPill('reconnecting', 'Connecting...', 'Initiating');

    eventSource = new EventSource('/api/realtime/stream');

    eventSource.onopen = function () {
      isConnected = true;
      lastSyncTimestamp = Date.now();
      updateStatusPill('connected', 'Live Sync', 'Just now');
    };

    eventSource.addEventListener('realtime', function (e) {
      try {
        const event = JSON.parse(e.data);
        handleRealtimeEvent(event);
      } catch (err) {
        console.error('Failed to parse realtime event payload', err);
      }
    });

    eventSource.addEventListener('heartbeat', function () {
      lastSyncTimestamp = Date.now();
      updateTimeDisplay();
    });

    eventSource.onerror = function () {
      isConnected = false;
      updateStatusPill('reconnecting', 'Live Sync', 'Retrying...');
    };
  }

  // Handle Event from Server (or fallback polling)
  function handleRealtimeEvent(event) {
    if (!event || event.type === 'CONNECTED' || event.type === 'HEARTBEAT') return;

    lastSyncTimestamp = Date.now();
    updateTimeDisplay();

    // 1. Toast Notification & Audio
    if (event.type === 'SALE_RETURN_CREATED') {
      playNotificationChime();
      showLiveToast(
        'Customer Return Processed',
        `<strong>Return #${event.entityNumber}</strong> for <strong>${formatINR(event.amount)}</strong> processed by <em>${event.processedBy || 'Cashier'}</em>. Stock restored!`,
        'bi bi-arrow-return-left text-info',
        'RETURN RESTOCKED',
        'bg-info-subtle text-info'
      );
    } else if (event.type === 'PRODUCT_RETURN_CREATED') {
      playNotificationChime();
      showLiveToast(
        'Vendor Return Logged',
        `<strong>Return #${event.entityNumber}</strong> to <strong>${event.partyName}</strong> for <strong>${formatINR(event.amount)}</strong> confirmed. Inventory deducted!`,
        'bi bi-truck text-warning',
        'VENDOR RETURN',
        'bg-warning-subtle text-dark'
      );
    } else if (event.type === 'SALE_CREATED') {
      showLiveToast(
        'New Sale Completed',
        `<strong>Invoice #${event.entityNumber}</strong> for <strong>${formatINR(event.amount)}</strong> completed by <em>${event.processedBy || 'Cashier'}</em>.`,
        'bi bi-cart-check-fill text-success',
        'SALE INWARD',
        'bg-success-subtle text-success'
      );
    } else if (event.type === 'PURCHASE_POSTED') {
      showLiveToast(
        'Purchase Stock Received',
        `<strong>Invoice #${event.entityNumber}</strong> stock received &amp; added to inventory.`,
        'bi bi-box-arrow-in-down text-primary',
        'INWARD STOCK',
        'bg-primary-subtle text-primary'
      );
    }

    // 2. Dispatch Custom Window Event
    window.dispatchEvent(new CustomEvent('freshmart:realtime', { detail: event }));

    // 3. Page-specific Real-Time DOM Updates
    triggerActivePageUpdate(event);
  }

  // Update "Last Synced: X ago" text
  function updateTimeDisplay() {
    const timeEl = document.getElementById('realtime-last-sync');
    if (!timeEl) return;
    const diffSec = Math.floor((Date.now() - lastSyncTimestamp) / 1000);
    if (diffSec < 5) timeEl.textContent = 'Just now';
    else if (diffSec < 60) timeEl.textContent = `${diffSec}s ago`;
    else timeEl.textContent = `${Math.floor(diffSec / 60)}m ago`;
  }

  // =========================================================================
  // DOM UPDATERS FOR REAL-TIME PAGES
  // =========================================================================

  function triggerActivePageUpdate(event) {
    // 1. Sidebar Badges
    updateSidebarSummary();

    // 2. Returns Pages
    if (document.getElementById('sale-returns-table')) {
      syncSaleReturnsTable();
    }
    if (document.getElementById('product-returns-table')) {
      syncProductReturnsTable();
    }

    // 3. Reports Pages
    if (document.getElementById('pl-report-container')) {
      syncProfitLossReport();
    }
    if (document.getElementById('tb-report-container')) {
      syncTrialBalanceReport();
    }
    if (document.getElementById('sr-report-container')) {
      syncSaleReturnsReport();
    }
    if (document.getElementById('pr-report-container')) {
      syncProductReturnsReport();
    }
    if (document.getElementById('gst-report-container')) {
      syncGstReturnReport();
    }

    // 4. Dashboard Page
    if (document.getElementById('dashboard-kpi-container') || document.querySelector('.stat-widget')) {
      syncDashboard();
    }
  }

  // 1. Sidebar Live Badge Update
  function updateSidebarSummary() {
    fetch('/api/realtime/summary')
      .then(res => res.json())
      .then(data => {
        const srBadge = document.getElementById('sidebar-sale-returns-badge');
        if (srBadge) {
          if (data.todayReturnsCount > 0) {
            srBadge.textContent = data.todayReturnsCount + ' today';
            srBadge.classList.remove('d-none');
          } else {
            srBadge.classList.add('d-none');
          }
        }
        const prBadge = document.getElementById('sidebar-product-returns-badge');
        if (prBadge) {
          if (data.todayProductReturnsCount > 0) {
            prBadge.textContent = data.todayProductReturnsCount + ' today';
            prBadge.classList.remove('d-none');
          } else {
            prBadge.classList.add('d-none');
          }
        }
      })
      .catch(() => { });
  }

  // 2. Sale Returns Table Live Sync (/returns/sales)
  function syncSaleReturnsTable() {
    fetch('/api/realtime/returns/sales')
      .then(res => res.json())
      .then(returns => {
        const tbody = document.getElementById('sale-returns-tbody');
        if (!tbody) return;

        const countBadge = document.getElementById('sale-returns-count-badge');
        if (countBadge) countBadge.textContent = returns.length + ' Total Returns';

        if (returns.length === 0) {
          tbody.innerHTML = `
            <tr>
              <td colspan="9" class="text-center py-5 text-muted">
                <i class="bi bi-arrow-return-left fs-1 mb-2 d-block opacity-50"></i>
                No customer sale returns recorded yet.
              </td>
            </tr>`;
          return;
        }

        let html = '';
        returns.forEach((ret, idx) => {
          const isLatest = (idx === 0);
          html += `
            <tr class="${isLatest ? 'realtime-row-glow' : ''}">
              <td>
                <a href="/returns/sales/${ret.id}" class="fw-bold text-info font-monospace text-decoration-none">${ret.returnNumber}</a>
              </td>
              <td>
                <span class="text-dark fw-medium">${ret.returnDate || 'Just now'}</span>
              </td>
              <td>
                <a href="/sales/${ret.originalSaleId}" class="fw-bold text-primary font-monospace">${ret.originalInvoiceNumber || 'INV'}</a>
              </td>
              <td>
                <div class="fw-semibold text-dark">${ret.customerName || 'Walk-in Customer'}</div>
                <div class="text-muted small">${ret.customerPhone || ''}</div>
              </td>
              <td>
                <span class="badge bg-light text-dark border rounded-pill px-2 py-1">${ret.totalReturnedQty} items</span>
              </td>
              <td>
                <span class="fw-bold text-danger fs-6">${formatINR(ret.refundAmount)}</span>
              </td>
              <td>
                <span class="text-muted small text-truncate d-inline-block" style="max-width: 180px;">${ret.reason || 'Customer Return'}</span>
              </td>
              <td>
                <span class="badge bg-light text-dark border">${ret.processedBy || 'Cashier'}</span>
              </td>
              <td class="text-end">
                <a href="/returns/sales/${ret.id}" class="btn btn-sm btn-light text-primary rounded-circle p-1" title="View Detail">
                  <i class="bi bi-eye"></i>
                </a>
              </td>
            </tr>
          `;
        });
        tbody.innerHTML = html;
      })
      .catch(err => console.warn('Failed to sync sale returns table', err));
  }

  // 3. Product Returns Table Live Sync (/returns/products)
  function syncProductReturnsTable() {
    fetch('/api/realtime/returns/products')
      .then(res => res.json())
      .then(returns => {
        const tbody = document.getElementById('product-returns-tbody');
        if (!tbody) return;

        const countBadge = document.getElementById('product-returns-count-badge');
        if (countBadge) countBadge.textContent = returns.length + ' Total Vendor Returns';

        if (returns.length === 0) {
          tbody.innerHTML = `
            <tr>
              <td colspan="8" class="text-center py-5 text-muted">
                <i class="bi bi-arrow-return-right fs-1 mb-2 d-block opacity-50"></i>
                No supplier returns recorded yet.
              </td>
            </tr>`;
          return;
        }

        let html = '';
        returns.forEach((ret, idx) => {
          const isLatest = (idx === 0);
          html += `
            <tr class="${isLatest ? 'realtime-row-glow' : ''}">
              <td>
                <a href="/returns/products/${ret.id}" class="fw-bold text-warning font-monospace text-decoration-none">${ret.returnNumber}</a>
              </td>
              <td>
                <span class="text-dark fw-medium">${ret.returnDate || 'Just now'}</span>
              </td>
              <td>
                <div class="fw-bold text-dark">${ret.supplierName || 'Supplier'}</div>
                <div class="text-muted small">${ret.contactPerson || ''}</div>
              </td>
              <td>
                <span class="badge bg-light text-dark border rounded-pill px-2 py-1">${ret.totalReturnedQty} items</span>
              </td>
              <td>
                <span class="fw-bold text-dark fs-6">${formatINR(ret.totalRefundAmount)}</span>
              </td>
              <td>
                <span class="text-muted small text-truncate d-inline-block" style="max-width: 200px;">${ret.reason || 'Damaged / Expired'}</span>
              </td>
              <td>
                <span class="badge bg-light text-dark border">${ret.processedBy || 'Administrator'}</span>
              </td>
              <td class="text-end">
                <a href="/returns/products/${ret.id}" class="btn btn-sm btn-light text-primary rounded-circle p-1" title="View Detail">
                  <i class="bi bi-eye"></i>
                </a>
              </td>
            </tr>
          `;
        });
        tbody.innerHTML = html;
      })
      .catch(err => console.warn('Failed to sync product returns table', err));
  }

  // 4. Profit & Loss Report Live Sync (/reports/profit-loss)
  function syncProfitLossReport() {
    const startInput = document.querySelector('input[name="startDate"]');
    const endInput = document.querySelector('input[name="endDate"]');
    const start = startInput ? startInput.value : '';
    const end = endInput ? endInput.value : '';

    fetch(`/api/realtime/reports/profit-loss?startDate=${start}&endDate=${end}`)
      .then(res => res.json())
      .then(report => {
        // KPI Metrics
        updateMetricText('pl-gross-revenue', formatINR(report.grossSales));
        updateMetricText('pl-sales-count', report.totalSalesCount + ' Sales');
        updateMetricText('pl-cogs', formatINR(report.costOfGoodsSold));
        updateMetricText('pl-gross-profit', formatINR(report.grossProfit));
        updateMetricText('pl-gp-margin', (report.grossProfitMargin || 0) + '% Margin');
        updateMetricText('pl-net-profit', formatINR(report.netProfit));
        updateMetricText('pl-np-margin', (report.netProfitMargin || 0) + '% Margin');

        // Table Statement Rows
        updateMetricText('pl-tbl-gross-sales', formatINR(report.grossSales));
        updateMetricText('pl-tbl-discounts', '-' + formatINR(report.saleDiscounts));
        updateMetricText('pl-tbl-returns', '-' + formatINR(report.saleReturns));
        updateMetricText('pl-tbl-net-revenue', formatINR(report.netSalesRevenue));
        updateMetricText('pl-tbl-cogs', formatINR(report.costOfGoodsSold));
        updateMetricText('pl-tbl-gross-profit', formatINR(report.grossProfit));
        updateMetricText('pl-tbl-net-profit', formatINR(report.netProfit));
      })
      .catch(err => console.warn('Failed to sync P&L report', err));
  }

  // 5. Trial Balance Report Live Sync (/reports/trial-balance)
  function syncTrialBalanceReport() {
    const startInput = document.querySelector('input[name="startDate"]');
    const endInput = document.querySelector('input[name="endDate"]');
    const start = startInput ? startInput.value : '';
    const end = endInput ? endInput.value : '';

    fetch(`/api/realtime/reports/trial-balance?startDate=${start}&endDate=${end}`)
      .then(res => res.json())
      .then(report => {
        const tbody = document.getElementById('tb-ledger-tbody');
        if (!tbody || !report.accounts) return;

        let html = '';
        report.accounts.forEach(acc => {
          const debitVal = acc.debit > 0 ? Number(acc.debit).toLocaleString('en-IN', { minimumFractionDigits: 2 }) : '-';
          const creditVal = acc.credit > 0 ? Number(acc.credit).toLocaleString('en-IN', { minimumFractionDigits: 2 }) : '-';
          const catClass = acc.category === 'Asset' ? 'bg-primary-subtle text-primary' :
            (acc.category === 'Liability' ? 'bg-danger-subtle text-danger' :
              (acc.category === 'Revenue' ? 'bg-success-subtle text-success' :
                (acc.category === 'Expense' ? 'bg-warning-subtle text-warning' : 'bg-secondary-subtle text-secondary')));

          html += `
            <tr>
              <td><span class="badge bg-light text-dark font-monospace border">${acc.code}</span></td>
              <td><div class="fw-semibold text-dark">${acc.accountName}</div></td>
              <td><span class="badge rounded-pill ${catClass}">${acc.category}</span></td>
              <td class="text-end font-monospace">${debitVal}</td>
              <td class="text-end font-monospace">${creditVal}</td>
            </tr>
          `;
        });
        tbody.innerHTML = html;

        updateMetricText('tb-total-debit', formatINR(report.totalDebit));
        updateMetricText('tb-total-credit', formatINR(report.totalCredit));

        const badge = document.getElementById('tb-balance-badge');
        if (badge) {
          if (report.balanced) {
            badge.className = 'badge bg-success-subtle text-success border border-success-subtle px-2.5 py-1.5';
            badge.innerHTML = '<i class="bi bi-check-circle-fill me-1"></i>Balanced';
          } else {
            badge.className = 'badge bg-danger-subtle text-danger border border-danger-subtle px-2.5 py-1.5';
            badge.innerHTML = '<i class="bi bi-exclamation-triangle-fill me-1"></i>Unbalanced';
          }
        }
      })
      .catch(err => console.warn('Failed to sync Trial Balance report', err));
  }

  // 6. Customer Sale Returns Report Live Sync (/reports/sale-returns)
  function syncSaleReturnsReport() {
    const startInput = document.querySelector('input[name="startDate"]');
    const endInput = document.querySelector('input[name="endDate"]');
    const start = startInput ? startInput.value : '';
    const end = endInput ? endInput.value : '';

    fetch(`/api/realtime/reports/sale-returns?startDate=${start}&endDate=${end}`)
      .then(res => res.json())
      .then(data => {
        updateMetricText('sr-report-count', data.count || 0);
        updateMetricText('sr-report-total-refund', formatINR(data.totalRefund));

        const tbody = document.getElementById('sr-report-tbody');
        if (!tbody || !data.returns) return;

        if (data.returns.length === 0) {
          tbody.innerHTML = `<tr><td colspan="8" class="text-center py-4 text-muted">No returns in selected period.</td></tr>`;
          return;
        }

        let html = '';
        data.returns.forEach(ret => {
          html += `
            <tr>
              <td><a href="/returns/sales/${ret.id}" class="fw-bold text-info font-monospace">${ret.returnNumber}</a></td>
              <td>${ret.returnDate}</td>
              <td><a href="/sales/${ret.originalSaleId}" class="fw-bold text-primary font-monospace">${ret.originalInvoiceNumber}</a></td>
              <td><strong>${ret.customerName}</strong></td>
              <td><span class="badge bg-light text-dark border">${ret.totalReturnedQty} items</span></td>
              <td class="text-end fw-bold text-danger">${formatINR(ret.refundAmount)}</td>
              <td>${ret.reason || ''}</td>
              <td><span class="badge bg-light text-dark border">${ret.processedBy}</span></td>
            </tr>
          `;
        });
        tbody.innerHTML = html;
      })
      .catch(err => console.warn('Failed to sync sale returns report', err));
  }

  // 7. Product Returns to Vendors Report Live Sync (/reports/product-returns)
  function syncProductReturnsReport() {
    const startInput = document.querySelector('input[name="startDate"]');
    const endInput = document.querySelector('input[name="endDate"]');
    const start = startInput ? startInput.value : '';
    const end = endInput ? endInput.value : '';

    fetch(`/api/realtime/reports/product-returns?startDate=${start}&endDate=${end}`)
      .then(res => res.json())
      .then(data => {
        updateMetricText('pr-report-count', data.count || 0);
        updateMetricText('pr-report-total-refund', formatINR(data.totalRefund));

        const tbody = document.getElementById('pr-report-tbody');
        if (!tbody || !data.returns) return;

        if (data.returns.length === 0) {
          tbody.innerHTML = `<tr><td colspan="7" class="text-center py-4 text-muted">No product returns in selected period.</td></tr>`;
          return;
        }

        let html = '';
        data.returns.forEach(ret => {
          html += `
            <tr>
              <td><a href="/returns/products/${ret.id}" class="fw-bold text-warning font-monospace">${ret.returnNumber}</a></td>
              <td>${ret.returnDate}</td>
              <td><strong>${ret.supplierName}</strong></td>
              <td><span class="badge bg-light text-dark border">${ret.totalReturnedQty} items</span></td>
              <td class="text-end fw-bold text-dark">${formatINR(ret.totalRefundAmount)}</td>
              <td>${ret.reason || ''}</td>
              <td><span class="badge bg-light text-dark border">${ret.processedBy}</span></td>
            </tr>
          `;
        });
        tbody.innerHTML = html;
      })
      .catch(err => console.warn('Failed to sync product returns report', err));
  }

  // 8. VAT / GST Tax Return Report Live Sync (/reports/gst-return)
  function syncGstReturnReport() {
    const startInput = document.querySelector('input[name="startDate"]');
    const endInput = document.querySelector('input[name="endDate"]');
    const start = startInput ? startInput.value : '';
    const end = endInput ? endInput.value : '';

    fetch(`/api/realtime/reports/gst-return?startDate=${start}&endDate=${end}`)
      .then(res => res.json())
      .then(gst => {
        updateMetricText('gst-taxable-sales', formatINR(gst.taxableSales));
        updateMetricText('gst-output-tax', formatINR(gst.outputGstTotal));
        updateMetricText('gst-input-itc', formatINR(gst.inputGstTotal));
        updateMetricText('gst-net-payable', formatINR(gst.netGstPayable));

        // Statement items
        updateMetricText('gst-tbl-taxable-sales', formatINR(gst.taxableSales));
        updateMetricText('gst-tbl-output-tax', formatINR(gst.outputGstTotal));
        updateMetricText('gst-tbl-taxable-purchases', formatINR(gst.taxablePurchases));
        updateMetricText('gst-tbl-input-tax', formatINR(gst.inputGstTotal));
        updateMetricText('gst-tbl-sale-returns-adj', formatINR(gst.saleReturnsGst));
        updateMetricText('gst-tbl-net-payable', formatINR(gst.netGstPayable));
      })
      .catch(err => console.warn('Failed to sync GST report', err));
  }

  // 9. Dashboard Live Sync (/dashboard)
  function syncDashboard() {
    fetch('/api/realtime/summary')
      .then(res => res.json())
      .then(data => {
        updateMetricText('dash-today-sales-count', data.todaySalesCount);
        updateMetricText('dash-today-revenue', formatINR(data.todaySalesTotal));
        updateMetricText('dash-today-returns-count', data.todayReturnsCount);
        updateMetricText('dash-today-refund', formatINR(data.todayRefundTotal));
        updateMetricText('dash-low-stock-count', data.lowStockCount);
        updateMetricText('dash-total-products', data.totalProducts);
      })
      .catch(err => console.warn('Failed to sync dashboard', err));
  }

  // Helper to update text with a subtle green pulse effect
  function updateMetricText(elementId, newText) {
    const el = document.getElementById(elementId);
    if (!el || el.textContent === String(newText)) return;
    el.textContent = newText;
    el.classList.remove('realtime-val-flash');
    void el.offsetWidth;
    el.classList.add('realtime-val-flash');
  }

  // Manual Refresh Button Click
  document.addEventListener('DOMContentLoaded', function () {
    const syncBtn = document.getElementById('btn-realtime-manual-sync');
    if (syncBtn) {
      syncBtn.addEventListener('click', function () {
        const icon = syncBtn.querySelector('i');
        if (icon) icon.classList.add('spin-animation');
        triggerActivePageUpdate({ type: 'MANUAL_REFRESH' });
        setTimeout(() => {
          if (icon) icon.classList.remove('spin-animation');
          lastSyncTimestamp = Date.now();
          updateTimeDisplay();
        }, 600);
      });
    }

    // Connect SSE stream
    initEventSource();

    // 8-second interval fallback poll and relative time ticker
    pollInterval = setInterval(function () {
      updateTimeDisplay();
      // If SSE disconnected or page is active, periodically check summary
      if (!isConnected || Date.now() - lastSyncTimestamp > 12000) {
        updateSidebarSummary();
        // Check if page needs periodic refresh
        if (document.getElementById('sale-returns-table')) syncSaleReturnsTable();
        if (document.getElementById('product-returns-table')) syncProductReturnsTable();
        if (document.getElementById('pl-report-container')) syncProfitLossReport();
        if (document.getElementById('tb-report-container')) syncTrialBalanceReport();
      }
    }, 8000);
  });

  // Expose global controller
  window.FreshMartRealtime = {
    syncNow: function () { triggerActivePageUpdate({ type: 'MANUAL' }); },
    getStatus: function () { return { isConnected: isConnected, lastSync: lastSyncTimestamp }; }
  };
})();
