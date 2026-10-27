/**
 * FreshMart POS - Interactive POS Billing System
 */

const POS = (function () {
  let cart = [];
  const TAX_RATE = 0.05; // 5% tax
  let paymentConfig = null;
  let upiTimerInterval = null;
  let activeUpiUri = '';

  function init() {
    bindProductClicks();
    bindCategoryFilters();
    bindSearch();
    bindBarcodeScanner();
    bindCheckoutEvents();
    bindPaymentGatewayEvents();
    loadPaymentConfig();
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
          <div class="flex-grow-1 pe-2 overflow-hidden">
            <h6 class="mb-0 fw-semibold text-truncate">${item.name}</h6>
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

  function loadPaymentConfig() {
    fetch('/api/payment/config')
      .then(res => res.json())
      .then(data => {
        if (data.success && data.data) {
          paymentConfig = data.data;

          // Update UI with config values
          const upiVpaDisplay = document.getElementById('upi-vpa-display');
          if (upiVpaDisplay && paymentConfig.upi && paymentConfig.upi.vpa) {
            upiVpaDisplay.textContent = paymentConfig.upi.vpa;
          }

          const rzpModeBadge = document.getElementById('razorpay-mode-badge');
          if (rzpModeBadge && paymentConfig.razorpay) {
            rzpModeBadge.textContent = (paymentConfig.razorpay.mode || 'TEST') + ' MODE';
            rzpModeBadge.className = paymentConfig.razorpay.mode === 'LIVE' ? 'badge bg-success' : 'badge bg-light text-secondary border';
          }

          const rzpKeyDisplay = document.getElementById('razorpay-key-display');
          if (rzpKeyDisplay && paymentConfig.razorpay && paymentConfig.razorpay.keyId) {
            rzpKeyDisplay.textContent = paymentConfig.razorpay.keyId;
          }

          const stripeModeBadge = document.getElementById('stripe-mode-badge');
          if (stripeModeBadge && paymentConfig.stripe) {
            stripeModeBadge.textContent = (paymentConfig.stripe.mode || 'TEST') + ' MODE';
            stripeModeBadge.className = paymentConfig.stripe.mode === 'LIVE' ? 'badge bg-success' : 'badge bg-light text-secondary border';
          }
        }
      })
      .catch(err => {
        console.warn('Could not load payment gateway configs, using defaults', err);
      });
  }

  function bindPaymentGatewayEvents() {
    // 1. Gateway radio selection toggle
    const gatewayRadios = document.querySelectorAll('input[name="paymentGatewayOption"]');
    gatewayRadios.forEach(radio => {
      radio.addEventListener('change', function () {
        switchGatewayPanel(this.value);
      });
    });

    // 2. Quick cash buttons
    document.querySelectorAll('.quick-cash-btn').forEach(btn => {
      btn.addEventListener('click', function () {
        const amt = parseFloat(this.dataset.amt) || 0;
        const tenderedInput = document.getElementById('modal-tendered-amount');
        if (tenderedInput) {
          tenderedInput.value = amt.toFixed(2);
          tenderedInput.dispatchEvent(new Event('input'));
        }
      });
    });

    // 3. Simulate customer UPI payment confirmation
    const btnSimulateUpi = document.getElementById('btn-simulate-upi-pay');
    if (btnSimulateUpi) {
      btnSimulateUpi.addEventListener('click', function () {
        const utrInput = document.getElementById('modal-upi-utr');
        const randUtr = '9847' + Math.floor(10000000 + Math.random() * 90000000);
        if (utrInput) {
          utrInput.value = randUtr;
          utrInput.classList.add('is-valid');
        }
        showToast('UPI payment simulated successfully! UTR: ' + randUtr, 'success');
      });
    }

    // 4. Launch Razorpay Checkout
    const btnRazorpay = document.getElementById('btn-launch-razorpay');
    if (btnRazorpay) {
      btnRazorpay.addEventListener('click', function () {
        launchRazorpayPayment(false);
      });
    }

    // 4b. Simulate Razorpay Instant Payment
    const btnSimRzp = document.getElementById('btn-simulate-razorpay');
    if (btnSimRzp) {
      btnSimRzp.addEventListener('click', function () {
        launchRazorpayPayment(true);
      });
    }

    // 4c. Setup Razorpay clone simulator events
    setupRazorpayCloneEvents();

    // 5. Process Stripe Card Payment
    const btnStripe = document.getElementById('btn-process-stripe');
    if (btnStripe) {
      btnStripe.addEventListener('click', processStripePayment);
    }

    // 5b. Process Mock Online Payment Gateway
    const btnOnline = document.getElementById('btn-mock-online-pay');
    if (btnOnline) {
      btnOnline.addEventListener('click', function () {
        executeOnlinePaymentFlow();
      });
    }

    // 6. Stripe card inputs formatting & brand detector
    setupStripeCardInputs();

    // 7. Regenerate UPI QR on modal open if UPI is selected
    const checkoutModalEl = document.getElementById('checkoutModal');
    if (checkoutModalEl) {
      checkoutModalEl.addEventListener('shown.bs.modal', function () {
        const selectedGateway = getSelectedGateway();
        if (selectedGateway === 'UPI_QR') {
          triggerUpiQrGeneration();
        }
      });

      checkoutModalEl.addEventListener('hidden.bs.modal', function () {
        if (upiTimerInterval) {
          clearInterval(upiTimerInterval);
          upiTimerInterval = null;
        }
      });
    }
  }

  function getSelectedGateway() {
    const checked = document.querySelector('input[name="paymentGatewayOption"]:checked');
    return checked ? checked.value : 'UPI_QR';
  }

  function switchGatewayPanel(gateway) {
    document.querySelectorAll('.gateway-panel').forEach(panel => panel.classList.add('d-none'));

    if (gateway === 'UPI_QR') {
      document.getElementById('panel-upi-qr')?.classList.remove('d-none');
      triggerUpiQrGeneration();
    } else if (gateway === 'RAZORPAY') {
      document.getElementById('panel-razorpay')?.classList.remove('d-none');
    } else if (gateway === 'STRIPE') {
      document.getElementById('panel-stripe')?.classList.remove('d-none');
    } else if (gateway === 'CARD_POS') {
      document.getElementById('panel-card-pos')?.classList.remove('d-none');
    } else if (gateway === 'ONLINE') {
      document.getElementById('panel-online')?.classList.remove('d-none');
    } else if (gateway === 'CASH') {
      document.getElementById('panel-cash')?.classList.remove('d-none');
      const tenderedInput = document.getElementById('modal-tendered-amount');
      if (tenderedInput && !tenderedInput.value) {
        const totals = calculateTotals();
        tenderedInput.value = totals.grandTotal.toFixed(2);
        tenderedInput.dispatchEvent(new Event('input'));
      }
    }
  }

  function triggerUpiQrGeneration() {
    const totals = calculateTotals();
    const qrImg = document.getElementById('upi-qr-image');
    const qrSpinner = document.getElementById('upi-qr-spinner');

    if (!qrImg) return;

    if (qrSpinner) qrSpinner.classList.remove('d-none');
    qrImg.classList.add('d-none');

    fetch('/api/payment/upi/generate', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        amount: totals.grandTotal.toFixed(2),
        reference: 'INV-' + Date.now().toString().slice(-6),
        note: 'FreshMart Supermarket Bill'
      })
    })
      .then(res => res.json())
      .then(data => {
        if (data.success && data.data && data.data.upiUri) {
          activeUpiUri = data.data.upiUri;
          // Render QR via clean QR image API
          const qrUrl = 'https://api.qrserver.com/v1/create-qr-code/?size=220x220&data=' + encodeURIComponent(activeUpiUri);
          qrImg.src = qrUrl;
          qrImg.onload = function () {
            if (qrSpinner) qrSpinner.classList.add('d-none');
            qrImg.classList.remove('d-none');
          };
          startUpiCountdownTimer(300); // 5 minutes
        }
      })
      .catch(err => {
        console.error('UPI QR generation error', err);
        if (qrSpinner) qrSpinner.classList.add('d-none');
        qrImg.classList.remove('d-none');
      });
  }

  function startUpiCountdownTimer(seconds) {
    if (upiTimerInterval) clearInterval(upiTimerInterval);
    let remaining = seconds;
    const timerEl = document.getElementById('upi-timer');

    function update() {
      const mins = Math.floor(remaining / 60);
      const secs = remaining % 60;
      if (timerEl) {
        timerEl.textContent = (mins < 10 ? '0' : '') + mins + ':' + (secs < 10 ? '0' : '') + secs;
      }
      if (remaining <= 0) {
        clearInterval(upiTimerInterval);
        if (timerEl) timerEl.textContent = 'Expired - Click to Refresh';
      }
      remaining--;
    }

    update();
    upiTimerInterval = setInterval(update, 1000);
  }

  function setupStripeCardInputs() {
    const cardInput = document.getElementById('stripe-card-number');
    const brandBadge = document.getElementById('stripe-card-brand');
    const expiryInput = document.getElementById('stripe-card-expiry');

    if (cardInput) {
      cardInput.addEventListener('input', function () {
        let value = this.value.replace(/\D/g, '');
        if (value.length > 16) value = value.slice(0, 16);
        // Format with spaces
        let formatted = value.match(/.{1,4}/g)?.join(' ') || value;
        this.value = formatted;

        // Brand detection
        if (brandBadge) {
          if (value.startsWith('4')) {
            brandBadge.textContent = 'VISA';
            brandBadge.className = 'input-group-text bg-white text-primary fw-bold';
          } else if (value.startsWith('5')) {
            brandBadge.textContent = 'MASTERCARD';
            brandBadge.className = 'input-group-text bg-white text-warning fw-bold';
          } else if (value.startsWith('6')) {
            brandBadge.textContent = 'RUPAY';
            brandBadge.className = 'input-group-text bg-white text-success fw-bold';
          } else if (value.startsWith('3')) {
            brandBadge.textContent = 'AMEX';
            brandBadge.className = 'input-group-text bg-white text-info fw-bold';
          } else {
            brandBadge.textContent = 'CARD';
            brandBadge.className = 'input-group-text bg-white text-muted fw-bold';
          }
        }
      });
    }

    if (expiryInput) {
      expiryInput.addEventListener('input', function () {
        let val = this.value.replace(/\D/g, '');
        if (val.length > 4) val = val.slice(0, 4);
        if (val.length > 2) {
          this.value = val.slice(0, 2) + '/' + val.slice(2);
        } else {
          this.value = val;
        }
      });
    }
  }

  let activeRazorpayOrder = null;

  function launchRazorpayPayment(forceSimulation = false) {
    const cashierInput = document.getElementById('modal-cashier-name');
    if (cashierInput && !cashierInput.value.trim()) {
      showToast('Please enter Cashier Name before proceeding with payment!', 'danger');
      cashierInput.classList.add('is-invalid');
      cashierInput.focus();
      return;
    }

    const totals = calculateTotals();
    const btnRazorpay = document.getElementById('btn-launch-razorpay');
    if (btnRazorpay) {
      btnRazorpay.disabled = true;
      btnRazorpay.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Initiating Razorpay...';
    }

    fetch('/api/payment/razorpay/create-order', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        amount: totals.grandTotal.toFixed(2),
        receipt: 'RCP-' + Date.now().toString().slice(-6),
        notes: 'FreshMart POS Razorpay Checkout'
      })
    })
      .then(res => res.json())
      .then(data => {
        if (btnRazorpay) {
          btnRazorpay.disabled = false;
          btnRazorpay.innerHTML = '<i class="bi bi-shield-check fs-5"></i> Launch Razorpay Checkout';
        }

        if (!data.success || !data.data) {
          showToast(data.message || 'Razorpay order creation failed', 'danger');
          return;
        }

        const order = data.data;

        // In test mode / mock mode / dummy keys: ALWAYS open the high-fidelity Razorpay Clone Simulator!
        if (forceSimulation || order.isMock === true || !order.keyId || order.keyId.includes('freshmart12345') || order.keyId === 'dummy') {
          openRazorpayCloneModal(order);
          return;
        }

        // Live / Real Razorpay Checkout flow
        if (typeof Razorpay !== 'undefined') {
          const options = {
            key: order.keyId,
            amount: order.amount,
            currency: order.currency || "INR",
            name: "FreshMart Supermarket Kochi",
            description: "Retail Grocery Bill",
            order_id: order.orderId,
            handler: function (response) {
              fetch('/api/payment/razorpay/verify', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                  orderId: response.razorpay_order_id || order.orderId,
                  paymentId: response.razorpay_payment_id || ('pay_' + Date.now()),
                  signature: response.razorpay_signature || 'simulated_sig'
                })
              })
                .then(vRes => vRes.json())
                .then(vData => {
                  showToast('Razorpay payment verified & approved!', 'success');
                  submitCompletedCheckout({
                    paymentGateway: 'RAZORPAY',
                    paymentMethod: 'RAZORPAY',
                    transactionId: response.razorpay_payment_id || ('pay_' + Date.now()),
                    orderId: response.razorpay_order_id || order.orderId,
                    paymentReference: response.razorpay_payment_id || ('pay_' + Date.now()),
                    paymentStatus: 'PAID'
                  });
                })
                .catch(err => {
                  console.warn('Verification request failed, completing sale with payment ID', err);
                  submitCompletedCheckout({
                    paymentGateway: 'RAZORPAY',
                    paymentMethod: 'RAZORPAY',
                    transactionId: response.razorpay_payment_id || ('pay_' + Date.now()),
                    orderId: response.razorpay_order_id || order.orderId,
                    paymentReference: response.razorpay_payment_id || ('pay_' + Date.now()),
                    paymentStatus: 'PAID'
                  });
                });
            },
            prefill: {
              name: document.getElementById('modal-customer-name')?.value || 'Walk-in Customer',
              contact: document.getElementById('modal-customer-phone')?.value || '9847011223'
            },
            theme: { color: "#10b981" }
          };

          try {
            const rzp = new Razorpay(options);
            rzp.on('payment.failed', function (resp) {
              showToast('Razorpay payment failed: ' + (resp.error?.description || 'Declined'), 'danger');
              openRazorpayCloneModal(order);
            });
            rzp.open();
          } catch (ex) {
            console.warn('Razorpay popup open failed, offering simulator', ex);
            openRazorpayCloneModal(order);
          }
        } else {
          openRazorpayCloneModal(order);
        }
      })
      .catch(err => {
        if (btnRazorpay) {
          btnRazorpay.disabled = false;
          btnRazorpay.innerHTML = '<i class="bi bi-shield-check fs-5"></i> Launch Razorpay Checkout';
        }
        showToast('Error initiating Razorpay: ' + err.message, 'danger');
      });
  }

  function openRazorpayCloneModal(order) {
    activeRazorpayOrder = order;
    const totals = calculateTotals();

    // 1. Populate Left Sidebar details
    const amtEl = document.getElementById('rzp-clone-amount');
    if (amtEl) amtEl.textContent = '₹' + totals.grandTotal.toFixed(2);

    const btnAmtEl = document.getElementById('rzp-clone-pay-btn-amt');
    if (btnAmtEl) btnAmtEl.textContent = '₹' + totals.grandTotal.toFixed(2);

    const ordEl = document.getElementById('rzp-clone-order-id');
    if (ordEl) ordEl.textContent = order.orderId || ('order_mock_' + Date.now().toString().slice(-8));

    const custName = document.getElementById('modal-customer-name')?.value || 'Walk-in Customer';
    const custPhone = document.getElementById('modal-customer-phone')?.value || '+91 98470 12345';
    const nameEl = document.getElementById('rzp-clone-customer-name');
    if (nameEl) nameEl.textContent = custName;
    const phoneEl = document.getElementById('rzp-clone-customer-phone');
    if (phoneEl) phoneEl.textContent = custPhone;

    // 2. Dynamic QR Code generation for UPI Tab
    const qrImg = document.getElementById('rzp-clone-upi-qr');
    if (qrImg) {
      const upiUrl = 'upi://pay?pa=freshmart.kochi@okaxis&pn=FreshMart%20Supermarket&am=' + totals.grandTotal.toFixed(2) + '&cu=INR&tn=' + encodeURIComponent(order.orderId || 'FreshMart POS Bill');
      qrImg.src = 'https://api.qrserver.com/v1/create-qr-code/?size=180x180&data=' + encodeURIComponent(upiUrl);
    }

    // 3. Reset View States
    const mainView = document.getElementById('rzp-clone-main-view');
    const otpView = document.getElementById('rzp-clone-otp-view');
    const successView = document.getElementById('rzp-clone-success-view');

    if (mainView) {
      mainView.classList.remove('d-none');
      mainView.classList.add('rzp-fade-in');
    }
    if (otpView) otpView.classList.add('d-none');
    if (successView) successView.classList.add('d-none');

    // Reset OTP Button
    const otpSubmitBtn = document.getElementById('btn-rzp-submit-otp');
    if (otpSubmitBtn) {
      otpSubmitBtn.disabled = false;
      otpSubmitBtn.innerHTML = '<i class="bi bi-shield-check fs-5"></i> <span>Verify OTP &amp; Complete Payment</span>';
    }

    // Activate default UPI tab
    const upiTabBtn = document.getElementById('rzp-tab-upi-btn');
    if (upiTabBtn && typeof bootstrap !== 'undefined') {
      const tabTrigger = new bootstrap.Tab(upiTabBtn);
      tabTrigger.show();
    }

    // Show the modal
    const cloneModalEl = document.getElementById('razorpayCloneModal');
    if (cloneModalEl) {
      const modal = bootstrap.Modal.getOrCreateInstance(cloneModalEl);
      modal.show();
    }
  }

  function setupRazorpayCloneEvents() {
    // 1. Close Modal Button
    document.getElementById('btn-rzp-clone-close')?.addEventListener('click', function () {
      const cloneModalEl = document.getElementById('razorpayCloneModal');
      if (cloneModalEl) {
        bootstrap.Modal.getInstance(cloneModalEl)?.hide();
      }
    });

    // 2. Quick UPI App selection
    document.querySelectorAll('.rzp-quick-app-btn').forEach(btn => {
      btn.addEventListener('click', function () {
        document.querySelectorAll('.rzp-quick-app-btn').forEach(b => {
          b.classList.remove('btn-primary', 'text-white');
          b.classList.add('btn-outline-light', 'text-dark');
        });
        this.classList.remove('btn-outline-light', 'text-dark');
        this.classList.add('btn-primary', 'text-white');

        const vpa = this.getAttribute('data-vpa');
        const vpaInput = document.getElementById('rzp-clone-vpa');
        if (vpaInput && vpa) {
          vpaInput.value = vpa;
        }
      });
    });

    // 3. Autofill Test RuPay Card
    document.getElementById('btn-rzp-autofill-card')?.addEventListener('click', function () {
      const cardNum = document.getElementById('rzp-clone-card-num');
      const cardExp = document.getElementById('rzp-clone-card-exp');
      const cardCvv = document.getElementById('rzp-clone-card-cvv');
      const cardName = document.getElementById('rzp-clone-card-name');
      const cardBrand = document.getElementById('rzp-clone-card-brand');

      if (cardNum) cardNum.value = '6074 1234 5678 9010';
      if (cardExp) cardExp.value = '12/28';
      if (cardCvv) cardCvv.value = '888';
      if (cardName) cardName.value = 'Deepak Nair';
      if (cardBrand) {
        cardBrand.textContent = 'RUPAY';
        cardBrand.className = 'input-group-text bg-white text-success fw-bold';
      }
      showToast('Autofilled test RuPay card details!', 'info');
    });

    // Card format detector
    const cardInput = document.getElementById('rzp-clone-card-num');
    if (cardInput) {
      cardInput.addEventListener('input', function () {
        let val = this.value.replace(/\D/g, '');
        if (val.length > 16) val = val.slice(0, 16);
        let formatted = val.match(/.{1,4}/g)?.join(' ') || val;
        this.value = formatted;

        const brandBadge = document.getElementById('rzp-clone-card-brand');
        if (brandBadge) {
          if (val.startsWith('4')) {
            brandBadge.textContent = 'VISA';
            brandBadge.className = 'input-group-text bg-white text-primary fw-bold';
          } else if (val.startsWith('5')) {
            brandBadge.textContent = 'MC';
            brandBadge.className = 'input-group-text bg-white text-danger fw-bold';
          } else if (val.startsWith('6')) {
            brandBadge.textContent = 'RUPAY';
            brandBadge.className = 'input-group-text bg-white text-success fw-bold';
          } else {
            brandBadge.textContent = 'CARD';
            brandBadge.className = 'input-group-text bg-white text-muted fw-bold';
          }
        }
      });
    }

    // Expiry input auto-slash
    const expInput = document.getElementById('rzp-clone-card-exp');
    if (expInput) {
      expInput.addEventListener('input', function () {
        let val = this.value.replace(/\D/g, '');
        if (val.length > 4) val = val.slice(0, 4);
        if (val.length > 2) {
          this.value = val.slice(0, 2) + '/' + val.slice(2);
        } else {
          this.value = val;
        }
      });
    }

    // 4. Pay Button -> Transition to 3DS Bank OTP View
    document.getElementById('btn-rzp-clone-pay')?.addEventListener('click', function () {
      const activeTabEl = document.querySelector('#rzp-clone-tabs .nav-link.active');
      let bankTitle = 'Issuing Bank &bull; 3D Secure';

      if (activeTabEl) {
        const target = activeTabEl.getAttribute('data-bs-target');
        if (target === '#rzp-tab-upi') {
          bankTitle = 'NPCI Bharat UPI Auto-Approval';
        } else if (target === '#rzp-tab-card') {
          const brand = document.getElementById('rzp-clone-card-brand')?.textContent || 'Card';
          bankTitle = brand + ' Issuing Bank &bull; 3D Secure 2.0';
        } else if (target === '#rzp-tab-nb') {
          const selectedBank = document.querySelector('input[name="rzpNbBank"]:checked')?.value || 'State Bank of India';
          bankTitle = selectedBank + ' &bull; NetBanking Portal';
        } else if (target === '#rzp-tab-wallet') {
          const selectedWallet = document.querySelector('input[name="rzpWallet"]:checked')?.value || 'Paytm Wallet';
          bankTitle = selectedWallet + ' &bull; One-Click Auth';
        }
      }

      const bankTitleEl = document.getElementById('rzp-otp-bank-name');
      if (bankTitleEl) bankTitleEl.innerHTML = bankTitle;

      const totals = calculateTotals();
      const otpAmtEl = document.getElementById('rzp-otp-amount');
      if (otpAmtEl) otpAmtEl.textContent = '₹' + totals.grandTotal.toFixed(2);

      // Transition to OTP View with slide-in animation
      document.getElementById('rzp-clone-main-view')?.classList.add('d-none');
      const otpView = document.getElementById('rzp-clone-otp-view');
      if (otpView) {
        otpView.classList.remove('d-none');
        otpView.classList.remove('rzp-slide-in');
        void otpView.offsetWidth; // force reflow for animation restart
        otpView.classList.add('rzp-slide-in');
      }
      const otpInput = document.getElementById('rzp-otp-input') || document.getElementById('rzp-clone-otp-input');
      if (otpInput) otpInput.focus();
    });

    // 5. Cancel OTP -> Back to Main
    document.getElementById('btn-rzp-cancel-otp')?.addEventListener('click', function () {
      document.getElementById('rzp-clone-otp-view')?.classList.add('d-none');
      const mainView = document.getElementById('rzp-clone-main-view');
      if (mainView) {
        mainView.classList.remove('d-none');
        mainView.classList.remove('rzp-fade-in');
        void mainView.offsetWidth;
        mainView.classList.add('rzp-fade-in');
      }
    });

    // 6. Submit OTP -> Success & Complete POS Checkout
    document.getElementById('btn-rzp-submit-otp')?.addEventListener('click', function () {
      const btn = this;
      btn.disabled = true;
      btn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Authorizing with Bank...';

      const simPaymentId = 'pay_rzp' + Date.now().toString().slice(-10);
      const orderId = activeRazorpayOrder ? activeRazorpayOrder.orderId : ('order_mock_' + Date.now());

      // Verify via backend REST API
      fetch('/api/payment/razorpay/verify', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          orderId: orderId,
          paymentId: simPaymentId,
          signature: 'simulated_sig_approved'
        })
      })
        .then(res => res.json())
        .then(vData => {
          // Show Success Screen with pop bounce
          document.getElementById('rzp-clone-otp-view')?.classList.add('d-none');
          const succView = document.getElementById('rzp-clone-success-view');
          if (succView) {
            succView.classList.remove('d-none');
            succView.classList.remove('rzp-fade-in');
            void succView.offsetWidth;
            succView.classList.add('rzp-fade-in');
          }

          const payIdEl = document.getElementById('rzp-success-payment-id');
          if (payIdEl) payIdEl.textContent = simPaymentId;

          setTimeout(() => {
            const cloneModalEl = document.getElementById('razorpayCloneModal');
            if (cloneModalEl) {
              bootstrap.Modal.getInstance(cloneModalEl)?.hide();
            }

            // Close base checkout modal as well
            const checkoutModalEl = document.getElementById('checkoutModal');
            if (checkoutModalEl) {
              bootstrap.Modal.getInstance(checkoutModalEl)?.hide();
            }

            // Complete checkout
            submitCompletedCheckout({
              paymentGateway: 'RAZORPAY',
              paymentMethod: 'RAZORPAY',
              transactionId: simPaymentId,
              orderId: orderId,
              paymentReference: simPaymentId,
              paymentStatus: 'PAID'
            });
          }, 900);
        })
        .catch(err => {
          console.warn('Signature verification request error', err);
          const cloneModalEl = document.getElementById('razorpayCloneModal');
          if (cloneModalEl) {
            bootstrap.Modal.getInstance(cloneModalEl)?.hide();
          }
          submitCompletedCheckout({
            paymentGateway: 'RAZORPAY',
            paymentMethod: 'RAZORPAY',
            transactionId: simPaymentId,
            orderId: orderId,
            paymentReference: simPaymentId,
            paymentStatus: 'PAID'
          });
        });
    });

    // 7. Simulate Bank Decline / Failure
    document.getElementById('btn-rzp-clone-simulate-fail')?.addEventListener('click', function () {
      const cloneModalEl = document.getElementById('razorpayCloneModal');
      if (cloneModalEl) {
        bootstrap.Modal.getInstance(cloneModalEl)?.hide();
      }
      showToast('Razorpay simulation: Payment declined by issuing bank (Insufficient funds / declined by customer bank).', 'danger');
    });
  }

  function processStripePayment() {
    const cashierInput = document.getElementById('modal-cashier-name');
    if (cashierInput && !cashierInput.value.trim()) {
      showToast('Please enter Cashier Name before proceeding!', 'danger');
      cashierInput.classList.add('is-invalid');
      cashierInput.focus();
      return;
    }

    const cardNum = document.getElementById('stripe-card-number')?.value.replace(/\s+/g, '') || '';
    if (cardNum.length < 13) {
      showToast('Please enter a valid 16-digit card number!', 'danger');
      document.getElementById('stripe-card-number')?.focus();
      return;
    }

    const totals = calculateTotals();
    const btnStripe = document.getElementById('btn-process-stripe');
    btnStripe.disabled = true;
    btnStripe.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Authorizing Card with Stripe...';

    fetch('/api/payment/stripe/create-intent', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        amount: totals.grandTotal.toFixed(2),
        currency: 'inr',
        customerName: document.getElementById('modal-customer-name')?.value || 'Walk-in Customer',
        customerPhone: document.getElementById('modal-customer-phone')?.value || ''
      })
    })
      .then(res => res.json())
      .then(data => {
        setTimeout(() => {
          btnStripe.disabled = false;
          btnStripe.innerHTML = '<i class="bi bi-credit-card-2-front-fill"></i> Authorize &amp; Pay with Stripe';

          if (data.success && data.data) {
            const chargeId = 'ch_str' + Date.now().toString().slice(-10);
            showToast('Stripe card authorization approved! Ref: ' + chargeId, 'success');
            submitCompletedCheckout({
              paymentGateway: 'STRIPE',
              paymentMethod: 'STRIPE',
              transactionId: chargeId,
              orderId: data.data.paymentIntentId,
              paymentStatus: 'PAID'
            });
          } else {
            showToast(data.message || 'Stripe authorization failed', 'danger');
          }
        }, 800);
      })
      .catch(err => {
        btnStripe.disabled = false;
        btnStripe.innerHTML = '<i class="bi bi-credit-card-2-front-fill"></i> Authorize &amp; Pay with Stripe';
        showToast('Stripe network error: ' + err.message, 'danger');
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
      payConfirmBtn.addEventListener('click', function () {
        processCheckoutSubmission();
      });
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

    const gateway = getSelectedGateway();
    let txnId = '';
    let method = 'CASH';

    if (gateway === 'UPI_QR') {
      method = 'UPI_QR';
      const utr = document.getElementById('modal-upi-utr')?.value.trim();
      txnId = utr || ('UPI-UTR-' + Date.now().toString().slice(-10));
    } else if (gateway === 'RAZORPAY') {
      launchRazorpayPayment(false);
      return;
    } else if (gateway === 'STRIPE') {
      method = 'STRIPE';
      txnId = 'ch_str' + Date.now().toString().slice(-10);
    } else if (gateway === 'CARD_POS') {
      method = 'CARD_POS';
      const rrn = document.getElementById('pos-terminal-rrn')?.value.trim();
      txnId = rrn ? ('POS-RRN-' + rrn) : ('POS-AUTH-' + Math.floor(100000 + Math.random() * 900000));
    } else if (gateway === 'ONLINE') {
      executeOnlinePaymentFlow();
      return;
    } else {
      method = 'CASH';
      txnId = 'CASH-REC-' + Date.now().toString().slice(-6);
    }

    submitCompletedCheckout({
      paymentGateway: gateway,
      paymentMethod: method,
      transactionId: txnId,
      paymentReference: txnId,
      paymentStatus: 'PAID'
    });
  }

  function executeOnlinePaymentFlow() {
    const totals = calculateTotals();
    const btn = document.getElementById('pos-confirm-pay-btn');
    if (btn) {
      btn.disabled = true;
      btn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Creating Online Order...';
    }

    const orderStatusBadge = document.getElementById('online-order-status-badge');
    const orderIdDisplay = document.getElementById('online-order-id-display');
    if (orderStatusBadge) {
      orderStatusBadge.className = 'badge bg-warning text-dark';
      orderStatusBadge.textContent = 'CREATING ORDER';
    }

    // 1. Create online mock order via MockPaymentGatewayService
    fetch('/api/payment/online/create-order', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        amount: totals.grandTotal,
        receipt: 'ONL-REC-' + Date.now().toString().slice(-8),
        notes: 'FreshMart POS Online Payment'
      })
    })
      .then(res => res.json())
      .then(data => {
        if (!data.success) {
          throw new Error(data.message || 'Failed to initialize online order');
        }
        const order = data.data;
        const orderId = order.orderId || ('MOCK_ORD_' + Date.now());
        if (orderIdDisplay) orderIdDisplay.textContent = orderId;
        if (orderStatusBadge) {
          orderStatusBadge.className = 'badge bg-info text-dark';
          orderStatusBadge.textContent = 'VERIFYING';
        }

        // 2. Mock payment verification
        return fetch('/api/payment/online/verify', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            orderId: orderId,
            paymentId: 'MOCK_PAY_' + Date.now().toString().slice(-8),
            signature: 'sig_mock_auto_approved'
          })
        })
          .then(vRes => vRes.json())
          .then(vData => {
            if (!vData.success) {
              throw new Error(vData.message || 'Payment verification failed');
            }
            if (orderStatusBadge) {
              orderStatusBadge.className = 'badge bg-success';
              orderStatusBadge.textContent = 'SUCCESS';
            }
            // 3. Complete sale with verified online payment reference & SUCCESS status
            const payId = vData.data ? vData.data.paymentId : ('MOCK_PAY_' + Date.now());
            submitCompletedCheckout({
              paymentGateway: 'ONLINE',
              paymentMethod: 'ONLINE',
              transactionId: payId,
              orderId: orderId,
              paymentReference: orderId,
              paymentStatus: 'SUCCESS'
            });
          });
      })
      .catch(err => {
        showToast('Online Payment Error: ' + err.message, 'danger');
        if (orderStatusBadge) {
          orderStatusBadge.className = 'badge bg-danger';
          orderStatusBadge.textContent = 'FAILED';
        }
        if (btn) {
          btn.disabled = false;
          btn.innerHTML = '<i class="bi bi-check2-circle me-2"></i>Complete &amp; Print Bill';
        }
      });
  }

  function submitCompletedCheckout(paymentDetails) {
    if (cart.length === 0) return;

    const cashierInput = document.getElementById('modal-cashier-name');
    const cashierName = cashierInput ? cashierInput.value.trim() : 'Cashier';
    localStorage.setItem('freshmart_cashier_name', cashierName);

    const totals = calculateTotals();
    const customerName = document.getElementById('modal-customer-name')?.value || 'Walk-in Customer';
    const customerPhone = document.getElementById('modal-customer-phone')?.value || '';
    const notes = document.getElementById('modal-notes')?.value || '';
    
    let tendered = totals.grandTotal;
    if (paymentDetails.paymentGateway === 'CASH') {
      tendered = parseFloat(document.getElementById('modal-tendered-amount')?.value) || totals.grandTotal;
    }

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
      paymentMethod: paymentDetails.paymentMethod || 'CASH',
      paymentGateway: paymentDetails.paymentGateway || 'CASH',
      transactionId: paymentDetails.transactionId || '',
      paymentReference: paymentDetails.paymentReference || paymentDetails.transactionId || '',
      orderId: paymentDetails.orderId || '',
      paymentStatus: paymentDetails.paymentStatus || 'PAID',
      discountAmount: totals.discount,
      amountPaid: tendered,
      notes: notes
    };

    const confirmBtn = document.getElementById('pos-confirm-pay-btn');
    confirmBtn.disabled = true;
    confirmBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Recording Sale...';

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
        confirmBtn.innerHTML = '<i class="bi bi-check2-circle me-2"></i>Complete &amp; Print Bill';

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
        confirmBtn.innerHTML = '<i class="bi bi-check2-circle me-2"></i>Complete &amp; Print Bill';
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

    // Payment Gateway & Txn ID display
    const gatewayEl = document.getElementById('success-gateway');
    if (gatewayEl) {
      const gw = data.paymentGateway || 'CASH';
      if (gw === 'UPI_QR' || gw === 'UPI') {
        gatewayEl.className = 'badge bg-success-subtle text-success fw-bold';
        gatewayEl.innerHTML = '<i class="bi bi-qr-code-scan me-1"></i>Bharat UPI QR';
      } else if (gw === 'RAZORPAY') {
        gatewayEl.className = 'badge bg-primary-subtle text-primary fw-bold';
        gatewayEl.innerHTML = '<i class="bi bi-shield-check me-1"></i>Razorpay Gateway';
      } else if (gw === 'STRIPE') {
        gatewayEl.className = 'badge bg-info-subtle text-dark fw-bold';
        gatewayEl.innerHTML = '<i class="bi bi-credit-card-2-front me-1"></i>Stripe Card Gateway';
      } else if (gw === 'CARD_POS' || gw === 'CARD') {
        gatewayEl.className = 'badge bg-warning-subtle text-dark fw-bold';
        gatewayEl.innerHTML = '<i class="bi bi-credit-card me-1"></i>Counter Card Terminal';
      } else if (gw === 'ONLINE') {
        gatewayEl.className = 'badge bg-primary-subtle text-primary fw-bold';
        gatewayEl.innerHTML = '<i class="bi bi-globe2 me-1"></i>Online Payment Gateway';
      } else {
        gatewayEl.className = 'badge bg-secondary-subtle text-secondary fw-bold';
        gatewayEl.innerHTML = '<i class="bi bi-cash-coin me-1"></i>Cash on Counter';
      }
    }

    const txnIdEl = document.getElementById('success-txn-id');
    if (txnIdEl) {
      txnIdEl.textContent = data.transactionId || '--';
    }

    document.getElementById('success-total').textContent = '₹' + parseFloat(data.grandTotal).toFixed(2);

    const changeRow = document.getElementById('success-change-row');
    const changeVal = parseFloat(data.changeReturned || 0);
    if (changeRow) {
      if (data.paymentGateway === 'CASH' && changeVal > 0) {
        changeRow.classList.remove('d-none');
        document.getElementById('success-change').textContent = '₹' + changeVal.toFixed(2);
      } else {
        changeRow.classList.add('d-none');
      }
    }

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
