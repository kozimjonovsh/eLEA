(function () {
  'use strict';

  document.addEventListener('DOMContentLoaded', function () {
    initStaticPreviewLinks();
    initLucide();
    initSidebar();
    initOrderForm();
    initProductForm();
    initAlerts();
  });

  function initLucide() {
    if (typeof lucide !== 'undefined' && typeof lucide.createIcons === 'function') {
      lucide.createIcons();
    }
  }

  // Allows opening templates directly on a laptop browser (file:// or .html preview) and navigating between pages
  function initStaticPreviewLinks() {
    var path = window.location.pathname || '';
    var isStaticHtml = window.location.protocol === 'file:' || path.slice(-5) === '.html';
    if (!isStaticHtml) return;

    var inSubdir = /\/(orders|customers|products|categories)\/[^/]+\.html$/i.test(path);
    var prefix = inSubdir ? '../' : './';

    var routeMap = {
      '/': prefix + 'dashboard.html',
      '/orders': prefix + 'orders/list.html',
      '/orders/new': prefix + 'orders/form.html',
      '/customers': prefix + 'customers/list.html',
      '/customers/new': prefix + 'customers/form.html',
      '/products': prefix + 'products/list.html',
      '/products/new': prefix + 'products/form.html',
      '/categories': prefix + 'categories/list.html',
      '/categories/new': prefix + 'categories/form.html'
    };

    document.querySelectorAll('a[href]').forEach(function (a) {
      var href = a.getAttribute('href');
      if (routeMap[href]) {
        a.setAttribute('href', routeMap[href]);
      }
    });
  }

  function initSidebar() {
    var sidebar = document.getElementById('sidebar');
    var overlay = document.getElementById('sidebarOverlay');
    var menuBtn = document.getElementById('mobileMenuBtn');

    var currentPath = window.location.pathname || '/';
    document.querySelectorAll('.sidebar-nav-item').forEach(function (item) {
      var nav = item.getAttribute('data-nav');
      if (!nav) return;
      var isDashboard = nav === '/' && (currentPath === '/' || currentPath.slice(-14) === 'dashboard.html');
      var isSection = nav !== '/' && currentPath.indexOf(nav) !== -1;
      if (isDashboard || isSection) {
        item.classList.add('active');
      }
    });

    function openSidebar() {
      if (!sidebar || !overlay) return;
      sidebar.classList.add('open');
      overlay.classList.add('open');
      document.body.classList.add('sidebar-open');
    }

    function closeSidebar() {
      if (!sidebar || !overlay) return;
      sidebar.classList.remove('open');
      overlay.classList.remove('open');
      document.body.classList.remove('sidebar-open');
    }

    if (menuBtn) {
      menuBtn.addEventListener('click', openSidebar);
    }
    if (overlay) {
      overlay.addEventListener('click', closeSidebar);
    }

    document.querySelectorAll('.sidebar-nav-item').forEach(function (item) {
      item.addEventListener('click', function () {
        if (window.innerWidth <= 1024) {
          closeSidebar();
        }
      });
    });

    document.addEventListener('keydown', function (e) {
      if (e.key === 'Escape') {
        closeSidebar();
      }
    });
  }

  function formatKRW(value) {
    return Math.round(value).toLocaleString('en-US') + ' KRW';
  }

  function initOrderForm() {
    var productSelect = document.getElementById('productSelect');
    if (!productSelect) return;

    var quantityInput = document.getElementById('quantityInput');
    var displays = {
      unitPrice: document.getElementById('unitPriceDisplay'),
      unitCost: document.getElementById('unitCostDisplay'),
      revenue: document.getElementById('revenueDisplay'),
      cost: document.getElementById('costDisplay'),
      margin: document.getElementById('marginDisplay')
    };

    function applyPrices(soldPrice, costPrice, quantity) {
      var revenue = soldPrice * quantity;
      var cost = costPrice * quantity;
      displays.unitPrice.textContent = formatKRW(soldPrice);
      displays.unitCost.textContent = formatKRW(costPrice);
      displays.revenue.textContent = formatKRW(revenue);
      displays.cost.textContent = formatKRW(cost);
      displays.margin.textContent = formatKRW(revenue - cost);
    }

    function updateCalculations() {
      var selected = productSelect.options[productSelect.selectedIndex];
      if (!selected || !selected.value) {
        displays.unitPrice.textContent = 'Select product';
        displays.unitCost.textContent = 'Select product';
        displays.revenue.textContent = '0 KRW';
        displays.cost.textContent = '0 KRW';
        displays.margin.textContent = '0 KRW';
        return;
      }

      var quantity = parseInt(quantityInput ? quantityInput.value : '1', 10) || 1;
      var attrSold = parseFloat(selected.getAttribute('data-sold-price'));
      var attrCost = parseFloat(selected.getAttribute('data-price'));

      if (!isNaN(attrSold) && !isNaN(attrCost)) {
        applyPrices(attrSold, attrCost, quantity);
        return;
      }

      fetch('/orders/api/product-price/' + encodeURIComponent(selected.value))
        .then(function (res) {
          if (!res.ok) throw new Error('Request failed');
          return res.json();
        })
        .then(function (product) {
          if (!product) return;
          applyPrices(product.soldPrice || 0, product.price || 0, quantity);
        })
        .catch(function () {
          displays.unitPrice.textContent = 'Error loading';
          displays.unitCost.textContent = 'Error loading';
        });
    }

    productSelect.addEventListener('change', updateCalculations);
    if (quantityInput) {
      quantityInput.addEventListener('input', updateCalculations);
    }
    updateCalculations();
  }

  function initProductForm() {
    var realPriceInput = document.querySelector('input[name="realPrice"]');
    var calcPriceInput = document.getElementById('calculatedPricePreview');
    var soldPriceInput = document.getElementById('soldPricePreview');

    if (realPriceInput && calcPriceInput && soldPriceInput) {
      function updateProductPricing() {
        var realPrice = parseFloat(realPriceInput.value);
        if (isNaN(realPrice) || realPrice < 0) {
          calcPriceInput.value = 'Auto: realPrice × 1.03 × 1.05';
          soldPriceInput.value = 'Auto calculated & rounded to 1000';
          return;
        }
        var price = Math.round(realPrice * 1.03 * 1.05 * 100.0) / 100.0;
        var baseSold;
        if (price <= 5000) baseSold = price * 1.6;
        else if (price <= 10000) baseSold = price * 1.5;
        else if (price <= 20000) baseSold = price * 1.35;
        else baseSold = price * 1.25;
        var soldPrice = Math.round(baseSold / 1000.0) * 1000.0;
        calcPriceInput.value = formatKRW(price);
        soldPriceInput.value = formatKRW(soldPrice);
      }
      realPriceInput.addEventListener('input', updateProductPricing);
    }

    var imageInput = document.getElementById('productImageInput');
    if (!imageInput) return;

    imageInput.addEventListener('change', function () {
      var file = this.files && this.files[0];
      var box = document.getElementById('productImagePreviewBox');
      if (!box) return;
      var placeholder = document.getElementById('productImagePlaceholder');

      if (file) {
        var img = document.createElement('img');
        img.alt = 'Preview';
        box.replaceChildren(img);
        img.src = URL.createObjectURL(file);
      } else if (placeholder) {
        box.replaceChildren(placeholder);
      }
    });
  }

  function initAlerts() {
    document.querySelectorAll('.alert').forEach(function (alert) {
      setTimeout(function () {
        alert.classList.add('alert-fade');
        setTimeout(function () {
          if (alert.parentNode) alert.parentNode.removeChild(alert);
        }, 400);
      }, 6000);
    });
  }
})();
