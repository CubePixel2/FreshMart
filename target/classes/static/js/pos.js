/**
 * FreshMart POS - Interactive POS Billing System
 */

const POS = (function () {
  let cart = [];
  const TAX_RATE = 0.05; // 5% tax

  function init() {
    bindProductClicks();
    bindCategoryFilters();
    bindSearch();
    bindBarcodeScanner();
    bindCheckoutEvents();
    renderCart();
  }

  function bindProductClicks() {
    document.querySelectorAll('.pos-product-card').forEach(card => {
      card.addEventListener('click', function () {
        const id = parseInt(this.dataset.id);
        const name = this.dataset.name;
        const barcode = this.dataset.barcode;
        const price = parseFloat(this.dataset.price);
        const stock = parseInt(this.dataset.stock);
        const unit = this.dataset.unit || 'pcs';

        addToCart({ id, name, barcode, price, stock, unit });
      });
    });
  }

  function addToCart(product) {
    if (product.stock <= 0) {
      showToast('Cannot add ' + product.name + ' - Out of stock!', 'danger');
      return;
    }

    const existingIndex = cart.findIndex(item => item.id === product.id);

    if (existingIndex > -1) {
      if (cart[existingIndex].quantity + 1 > product.stock) {
        showToast('Only ' + product.stock + ' available in stock for ' + product.name, 'danger');
        return;
      }
      cart[existingIndex].quantity += 1;
    } else {
      cart.push({
        id: product.id,
        name: product.name,
        barcode: product.barcode,
        price: product.price,
        stock: product.stock,
        unit: product.unit,
        quantity: 1
      });
    }

    // Trigger cart badge bounce
    const cartIcon = document.getElementById('cart-badge');
    if (cartIcon) {
      cartIcon.classList.remove('cart-bounce-effect');
      void cartIcon.offsetWidth; // trigger reflow
      cartIcon.classList.add('cart-bounce-effect');
    }

    renderCart();
  }

  function updateQuantity(id, delta) {
    const item = cart.find(i => i.id === id);
    if (!item) return;

    const newQty = item.quantity + delta;
    if (newQty <= 0) {
      removeFromCart(id);
    } else if (newQty > item.stock) {
      showToast('Cannot exceed available stock (' + item.stock + ')', 'danger');
    } else {
      item.quantity = newQty;
      renderCart();
    }
  }

  function removeFromCart(id) {
    cart = cart.filter(i => i.id !== id);
    renderCart();
  }

  function clearCart() {
    if (cart.length === 0) return;
    cart = [];
    renderCart();
    showToast('Cart cleared', 'primary');
  }

  function calculateTotals() {
    let subtotal = 0;
    cart.forEach(item => {
      subtotal += item.price * item.quantity;
    });

    const tax = subtotal * TAX_RATE;
    const discountInput = document.getElementById('cart-discount-input');
    let discount = discountInput ? (parseFloat(discountInput.value) || 0) : 0;
    if (discount > subtotal + tax) {
      discount = subtotal + tax;
    }

    const grandTotal = Math.max(0, subtotal + tax - discount);

    return { subtotal, tax, discount, grandTotal };
  }

  function renderCart() {
    const container = document.getElementById('pos-cart-items');
    const badge = document.getElementById('cart-badge');
    const totalItemsBadge = document.getElementById('pos-total-items-badge');
    const checkoutBtn = document.getElementById('pos-checkout-btn');
    const totals = calculateTotals();

    const totalQty = cart.reduce((acc, curr) => acc + curr.quantity, 0);

    if (badge) badge.textContent = totalQty;
    if (totalItemsBadge) totalItemsBadge.textContent = totalQty + ' items';

    if (!container) return;

    if (cart.length === 0) {
      container.innerHTML = `
        <div class="text-center py-5 text-muted">
          <i class="bi bi-cart-x fs-1 opacity-50 mb-3 d-block"></i>
          <h6 class="fw-semibold text-secondary">Cart is empty</h6>
          <p class="small text-muted mb-0">Click products or scan barcode to add items</p>
        </div>
      `;
      if (checkoutBtn) checkoutBtn.disabled = true;
      updateTotalDisplays(totals);
      return;
    }

    if (checkoutBtn) checkoutBtn.disabled = false;

    let html = '';
    cart.forEach(item => {
      const lineTotal = (item.price * item.quantity).toFixed(2);
      html += `
        <div class="pos-cart-item" data-id="${item.id}">
          <div class="flex-grow-1 pe-2">
            <h6 class="mb-0 fw-semibold text-truncate" style="max-width: 170px;">${item.name}</h6>
            <div class="d-flex align-items-center gap-2 mt-1">
              <span class="text-muted small">₹${item.price.toFixed(2)} / ${item.unit}</span>
              <span class="fw-bold text-success small">₹${lineTotal}</span>
            </div>
          </div>
          <div class="qty-control me-2">
            <button class="qty-btn" onclick="POS.updateQuantity(${item.id}, -1)">
              <i class="bi bi-dash"></i>
            </button>
            <span class="qty-input">${item.quantity}</span>
            <button class="qty-btn" onclick="POS.updateQuantity(${item.id}, 1)">
              <i class="bi bi-plus"></i>
            </button>
          </div>
          <button class="btn btn-sm btn-light text-danger rounded-circle p-1" onclick="POS.removeFromCart(${item.id})" title="Remove">
            <i class="bi bi-trash3"></i>
          </button>
        </div>
      `;
    });

    container.innerHTML = html;
    updateTotalDisplays(totals);
  }

  function updateTotalDisplays(totals) {
    const elSubtotal = document.getElementById('cart-subtotal');
    const elTax = document.getElementById('cart-tax');
    const elGrandTotal = document.getElementById('cart-grand-total');

    if (elSubtotal) elSubtotal.textContent = '₹' + totals.subtotal.toFixed(2);
    if (elTax) elTax.textContent = '₹' + totals.tax.toFixed(2);
    if (elGrandTotal) elGrandTotal.textContent = '₹' + totals.grandTotal.toFixed(2);

    // Also update checkout modal if present
    const modalTotal = document.getElementById('modal-payable-amount');
    if (modalTotal) modalTotal.textContent = '₹' + totals.grandTotal.toFixed(2);
  }

  function bindCategoryFilters() {
    const buttons = document.querySelectorAll('.pos-category-btn');
    const cards = document.querySelectorAll('.pos-product-card');

    buttons.forEach(btn => {
      btn.addEventListener('click', function () {
        buttons.forEach(b => b.classList.remove('active'));
        this.classList.add('active');

        const category = this.dataset.category;

        cards.forEach(card => {
          if (category === 'all' || card.dataset.category === category) {
            card.style.display = 'flex';
          } else {
            card.style.display = 'none';
          }
        });
      });
    });
  }

  function bindSearch() {
    const searchInput = document.getElementById('pos-search-input');
    const cards = document.querySelectorAll('.pos-product-card');

    if (searchInput) {
      searchInput.addEventListener('input', function () {
        const query = this.value.toLowerCase().trim();
        cards.forEach(card => {
          const name = card.dataset.name.toLowerCase();
          const barcode = card.dataset.barcode.toLowerCase();
          if (name.includes(query) || barcode.includes(query)) {
            card.style.display = 'flex';
          } else {
            card.style.display = 'none';
          }
        });
      });
    }
  }

  function bindBarcodeScanner() {
    const barcodeInput = document.getElementById('pos-barcode-input');
    if (!barcodeInput) return;

    barcodeInput.addEventListener('keydown', function (e) {
      if (e.key === 'Enter') {
        e.preventDefault();
        const code = this.value.trim();
        if (!code) return;

        const targetCard = Array.from(document.querySelectorAll('.pos-product-card'))
          .find(card => card.dataset.barcode === code);

        if (targetCard) {
          const id = parseInt(targetCard.dataset.id);
          const name = targetCard.dataset.name;
          const barcode = targetCard.dataset.barcode;
          const price = parseFloat(targetCard.dataset.price);
          const stock = parseInt(targetCard.dataset.stock);
          const unit = targetCard.dataset.unit || 'pcs';

          addToCart({ id, name, barcode, price, stock, unit });
          showToast('Scanned: ' + name, 'success');
        } else {
          showToast('Product with barcode "' + code + '" not found!', 'danger');
        }
        this.value = '';
      }
    });
  }

  function bindCheckoutEvents() {
    const discountInput = document.getElementById('cart-discount-input');
    if (discountInput) {
      discountInput.addEventListener('input', function () {
        const totals = calculateTotals();
        updateTotalDisplays(totals);
      });
    }

    const cashTendered = document.getElementById('modal-tendered-amount');
    const changeDisplay = document.getElementById('modal-change-amount');

    if (cashTendered && changeDisplay) {
      cashTendered.addEventListener('input', function () {
        const totals = calculateTotals();
        const paid = parseFloat(this.value) || 0;
        const change = Math.max(0, paid - totals.grandTotal);
        changeDisplay.textContent = '₹' + change.toFixed(2);
      });
    }

    // Cashier Name prefill & real-time validation
    const cashierInput = document.getElementById('modal-cashier-name');
    if (cashierInput) {
      const savedCashier = localStorage.getItem('freshmart_cashier_name');
      if (savedCashier) {
        cashierInput.value = savedCashier;
      }
      cashierInput.addEventListener('input', function () {
        if (this.value.trim()) {
          this.classList.remove('is-invalid');
        }
      });
    }

    const checkoutModalEl = document.getElementById('checkoutModal');
    if (checkoutModalEl) {
      checkoutModalEl.addEventListener('show.bs.modal', function () {
        if (cashierInput && !cashierInput.value.trim()) {
          const saved = localStorage.getItem('freshmart_cashier_name');
          if (saved) cashierInput.value = saved;
        }
      });
    }

    const payConfirmBtn = document.getElementById('pos-confirm-pay-btn');
    if (payConfirmBtn) {
      payConfirmBtn.addEventListener('click', processCheckoutSubmission);
    }
  }

  function processCheckoutSubmission() {
    if (cart.length === 0) return;

    const cashierInput = document.getElementById('modal-cashier-name');
    const cashierName = cashierInput ? cashierInput.value.trim() : '';

    if (!cashierName) {
      showToast('Please enter the Cashier Name before completing the bill!', 'danger');
      if (cashierInput) {
        cashierInput.classList.add('is-invalid');
        cashierInput.focus();
      }
      return;
    } else if (cashierInput) {
      cashierInput.classList.remove('is-invalid');
      localStorage.setItem('freshmart_cashier_name', cashierName);
    }

    const totals = calculateTotals();
    const customerName = document.getElementById('modal-customer-name')?.value || 'Walk-in Customer';
    const customerPhone = document.getElementById('modal-customer-phone')?.value || '';
    const paymentMethod = document.getElementById('modal-payment-method')?.value || 'CASH';
    const notes = document.getElementById('modal-notes')?.value || '';
    const tendered = parseFloat(document.getElementById('modal-tendered-amount')?.value) || totals.grandTotal;

    const payload = {
      items: cart.map(item => ({
        productId: item.id,
        barcode: item.barcode,
        name: item.name,
        price: item.price,
        quantity: item.quantity
      })),
      cashierName: cashierName,
      customerName: customerName,
      customerPhone: customerPhone,
      paymentMethod: paymentMethod,
      discountAmount: totals.discount,
      amountPaid: tendered,
      notes: notes
    };

    const confirmBtn = document.getElementById('pos-confirm-pay-btn');
    confirmBtn.disabled = true;
    confirmBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Processing...';

    fetch('/pos/api/checkout', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(payload)
    })
      .then(res => res.json())
      .then(data => {
        confirmBtn.disabled = false;
        confirmBtn.innerHTML = '<i class="bi bi-check2-circle me-2"></i>Complete & Print';

        if (data.success) {
          // Close checkout modal
          const checkoutModalEl = document.getElementById('checkoutModal');
          const checkoutModal = bootstrap.Modal.getInstance(checkoutModalEl);
          if (checkoutModal) checkoutModal.hide();

          // Deduct local card stocks
          cart.forEach(item => {
            const card = document.querySelector(`.pos-product-card[data-id="${item.id}"]`);
            if (card) {
              const currentStock = parseInt(card.dataset.stock) - item.quantity;
              card.dataset.stock = Math.max(0, currentStock);
              const stockBadge = card.querySelector('.product-stock-badge');
              if (stockBadge) {
                stockBadge.textContent = Math.max(0, currentStock) + ' ' + item.unit;
                if (currentStock <= 0) {
                  card.classList.add('out-of-stock');
                  stockBadge.className = 'badge bg-danger product-stock-badge';
                  stockBadge.textContent = 'Out of Stock';
                }
              }
            }
          });

          // Show success modal with invoice details
          showSuccessModal(data.data);
          cart = [];
          renderCart();
        } else {
          showToast(data.message || 'Checkout failed', 'danger');
        }
      })
      .catch(err => {
        confirmBtn.disabled = false;
        confirmBtn.innerHTML = '<i class="bi bi-check2-circle me-2"></i>Complete & Print';
        showToast('Network error during checkout: ' + err.message, 'danger');
      });
  }

  function showSuccessModal(data) {
    const modalEl = document.getElementById('saleSuccessModal');
    if (!modalEl) {
      window.location.href = data.invoiceUrl;
      return;
    }

    document.getElementById('success-invoice-no').textContent = data.invoiceNumber;
    const cashierEl = document.getElementById('success-cashier');
    if (cashierEl) {
      cashierEl.textContent = data.cashierName || 'Cashier';
    }
    document.getElementById('success-total').textContent = '₹' + parseFloat(data.grandTotal).toFixed(2);
    document.getElementById('success-change').textContent = '₹' + parseFloat(data.changeReturned || 0).toFixed(2);
    document.getElementById('success-print-link').href = data.invoiceUrl;

    const modal = new bootstrap.Modal(modalEl);
    modal.show();
  }

  return {
    init,
    addToCart,
    updateQuantity,
    removeFromCart,
    clearCart
  };
})();

document.addEventListener('DOMContentLoaded', POS.init);
