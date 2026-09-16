/**
 * FreshMart POS - Core Application JavaScript
 */

document.addEventListener('DOMContentLoaded', function () {
  // Initialize AOS (Animate On Scroll) if present
  if (typeof AOS !== 'undefined') {
    AOS.init({
      duration: 600,
      easing: 'ease-out-cubic',
      once: true,
      offset: 30
    });
  }

  // Animated Number Counters
  initNumberCounters();

  // Sidebar toggle for mobile/responsive views
  initSidebarToggle();

  // Auto-dismiss alerts after 5 seconds
  initAutoDismissAlerts();
});

/**
 * Smoothly animates numbers from 0 to their target value
 */
function initNumberCounters() {
  const counterElements = document.querySelectorAll('[data-counter]');

  counterElements.forEach(el => {
    const target = parseFloat(el.getAttribute('data-counter')) || 0;
    const isCurrency = el.hasAttribute('data-counter-currency');
    const duration = 1200; // ms
    const startTime = performance.now();

    function updateCounter(currentTime) {
      const elapsed = currentTime - startTime;
      const progress = Math.min(elapsed / duration, 1);

      // Ease out cubic
      const easeProgress = 1 - Math.pow(1 - progress, 3);
      const currentVal = target * easeProgress;

      if (isCurrency) {
        el.textContent = '₹' + currentVal.toLocaleString('en-IN', {
          minimumFractionDigits: 2,
          maximumFractionDigits: 2
        });
      } else {
        el.textContent = Math.round(currentVal).toLocaleString('en-IN');
      }

      if (progress < 1) {
        requestAnimationFrame(updateCounter);
      } else {
        if (isCurrency) {
          el.textContent = '₹' + target.toLocaleString('en-IN', {
            minimumFractionDigits: 2,
            maximumFractionDigits: 2
          });
        } else {
          el.textContent = Math.round(target).toLocaleString('en-IN');
        }
      }
    }

    requestAnimationFrame(updateCounter);
  });
}

/**
 * Universal Sidebar Toggle & Auto-Minimise Logic
 */
function initSidebarToggle() {
  const toggleBtn = document.getElementById('sidebar-toggle');
  const collapseBtn = document.getElementById('sidebar-collapse-btn');
  const sidebar = document.getElementById('sidebar');
  const appWrapper = document.getElementById('app-wrapper');
  const backdrop = document.getElementById('sidebar-backdrop');

  if (!sidebar) return;

  // Restore desktop collapsed state from localStorage; default to minimized (collapsed) so content is never blocked
  const isCollapsed = localStorage.getItem('freshmart_sidebar_collapsed');
  const isPosPage = window.location.pathname.includes('/pos');

  // Auto-collapse / minimise by default on desktop, or if user previously selected a page, or on POS page
  if (appWrapper && window.innerWidth > 1200) {
    if (isCollapsed === 'true' || isPosPage || isCollapsed === null) {
      appWrapper.classList.add('sidebar-collapsed');
    }
  }

  function toggleSidebar(e) {
    if (e) e.preventDefault();

    if (window.innerWidth > 1200) {
      // Desktop: toggle between full width (260px) and compact icon width (72px)
      if (appWrapper) {
        appWrapper.classList.toggle('sidebar-collapsed');
        const collapsedNow = appWrapper.classList.contains('sidebar-collapsed');
        localStorage.setItem('freshmart_sidebar_collapsed', collapsedNow ? 'true' : 'false');
      }
    } else {
      // Tablet / Mobile / Moderate screen: toggle off-canvas overlay
      const willShow = !sidebar.classList.contains('show');
      if (willShow) {
        sidebar.classList.add('show');
        if (backdrop) backdrop.classList.add('show');
      } else {
        closeOverlaySidebar();
      }
    }
  }

  function closeOverlaySidebar() {
    sidebar.classList.remove('show');
    if (backdrop) backdrop.classList.remove('show');
  }

  if (toggleBtn) {
    toggleBtn.addEventListener('click', toggleSidebar);
  }

  if (collapseBtn) {
    collapseBtn.addEventListener('click', function (e) {
      e.preventDefault();
      if (window.innerWidth > 1200) {
        if (appWrapper) {
          appWrapper.classList.add('sidebar-collapsed');
          localStorage.setItem('freshmart_sidebar_collapsed', 'true');
        }
      } else {
        closeOverlaySidebar();
      }
    });
  }

  if (backdrop) {
    backdrop.addEventListener('click', closeOverlaySidebar);
  }

  // Key requirement: After selecting any page/link from the menubar, auto-minimise/close sidebar!
  const navLinks = sidebar.querySelectorAll('.nav-link, .brand-logo');
  navLinks.forEach(link => {
    link.addEventListener('click', function () {
      // Remember minimised state across page navigation
      localStorage.setItem('freshmart_sidebar_collapsed', 'true');
      if (window.innerWidth <= 1200) {
        closeOverlaySidebar();
      } else if (appWrapper) {
        appWrapper.classList.add('sidebar-collapsed');
      }
    });
  });

  // Also close on Escape key press
  document.addEventListener('keydown', function (e) {
    if (e.key === 'Escape') {
      if (sidebar.classList.contains('show')) {
        closeOverlaySidebar();
      } else if (appWrapper && !appWrapper.classList.contains('sidebar-collapsed') && window.innerWidth > 1200) {
        appWrapper.classList.add('sidebar-collapsed');
        localStorage.setItem('freshmart_sidebar_collapsed', 'true');
      }
    }
  });
}

/**
 * Auto-dismiss alerts
 */
function initAutoDismissAlerts() {
  const alerts = document.querySelectorAll('.alert-dismissible');
  alerts.forEach(alert => {
    setTimeout(() => {
      const bsAlert = bootstrap.Alert.getOrCreateInstance(alert);
      if (bsAlert) {
        bsAlert.close();
      }
    }, 4500);
  });
}

/**
 * Quick toast notification utility
 */
function showToast(message, type = 'success') {
  const container = document.getElementById('toast-container');
  if (!container) return;

  const toastEl = document.createElement('div');
  const bgClass = type === 'success' ? 'bg-success text-white' : (type === 'danger' ? 'bg-danger text-white' : 'bg-primary text-white');
  const iconClass = type === 'success' ? 'bi-check-circle-fill' : (type === 'danger' ? 'bi-exclamation-triangle-fill' : 'bi-info-circle-fill');

  toastEl.className = `toast align-items-center ${bgClass} border-0 shadow-lg`;
  toastEl.setAttribute('role', 'alert');
  toastEl.setAttribute('aria-live', 'assertive');
  toastEl.setAttribute('aria-atomic', 'true');

  toastEl.innerHTML = `
    <div class="d-flex">
      <div class="toast-body d-flex align-items-center gap-2">
        <i class="bi ${iconClass} fs-5"></i>
        <span>${message}</span>
      </div>
      <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast" aria-label="Close"></button>
    </div>
  `;

  container.appendChild(toastEl);
  const toast = new bootstrap.Toast(toastEl, { delay: 3500 });
  toast.show();

  toastEl.addEventListener('hidden.bs.toast', () => {
    toastEl.remove();
  });
}
