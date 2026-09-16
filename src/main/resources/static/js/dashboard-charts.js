/**
 * FreshMart POS - Dashboard Analytics Charts (Chart.js)
 */

document.addEventListener('DOMContentLoaded', function () {
  const salesCanvas = document.getElementById('salesTrendChart');
  const categoryCanvas = document.getElementById('categoryDistributionChart');

  if (!salesCanvas && !categoryCanvas) return;

  fetch('/api/dashboard/chart-data')
    .then(response => response.json())
    .then(data => {
      if (salesCanvas) {
        initSalesTrendChart(salesCanvas, data.salesDates, data.salesTotals);
      }
      if (categoryCanvas) {
        initCategoryChart(categoryCanvas, data.categoryLabels, data.categoryCounts, data.categoryColors);
      }
    })
    .catch(error => {
      console.error('Error fetching dashboard chart data:', error);
    });
});

function initSalesTrendChart(canvas, labels, values) {
  const ctx = canvas.getContext('2d');

  // Create subtle emerald gradient fill
  const gradient = ctx.createLinearGradient(0, 0, 0, 300);
  gradient.addColorStop(0, 'rgba(16, 185, 129, 0.35)');
  gradient.addColorStop(1, 'rgba(16, 185, 129, 0.00)');

  new Chart(ctx, {
    type: 'line',
    data: {
      labels: labels,
      datasets: [{
        label: 'Daily Revenue (₹)',
        data: values,
        borderColor: '#10b981',
        borderWidth: 3,
        backgroundColor: gradient,
        fill: true,
        tension: 0.38,
        pointBackgroundColor: '#fff',
        pointBorderColor: '#10b981',
        pointBorderWidth: 2.5,
        pointRadius: 5,
        pointHoverRadius: 7,
        pointHoverBackgroundColor: '#10b981',
        pointHoverBorderColor: '#fff',
        pointHoverBorderWidth: 2
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: {
          display: false
        },
        tooltip: {
          backgroundColor: '#0f172a',
          titleFont: { family: 'Plus Jakarta Sans', size: 13, weight: '600' },
          bodyFont: { family: 'Plus Jakarta Sans', size: 13 },
          padding: 12,
          cornerRadius: 10,
          displayColors: false,
          callbacks: {
            label: function (context) {
              return ' Revenue: ₹' + parseFloat(context.parsed.y).toLocaleString('en-IN', { minimumFractionDigits: 2 });
            }
          }
        }
      },
      scales: {
        x: {
          grid: {
            display: false,
            drawBorder: false
          },
          ticks: {
            font: { family: 'Plus Jakarta Sans', size: 12 },
            color: '#64748b'
          }
        },
        y: {
          beginAtZero: true,
          grid: {
            color: '#f1f5f9',
            drawBorder: false
          },
          ticks: {
            font: { family: 'Plus Jakarta Sans', size: 12 },
            color: '#64748b',
            callback: function (value) {
              return '₹' + value.toLocaleString('en-IN');
            }
          }
        }
      }
    }
  });
}

function initCategoryChart(canvas, labels, counts, colors) {
  const ctx = canvas.getContext('2d');

  new Chart(ctx, {
    type: 'doughnut',
    data: {
      labels: labels,
      datasets: [{
        data: counts,
        backgroundColor: colors.length > 0 ? colors : ['#10b981', '#3b82f6', '#f59e0b', '#ec4899', '#8b5cf6'],
        borderWidth: 2,
        borderColor: '#ffffff',
        hoverOffset: 6
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      cutout: '72%',
      plugins: {
        legend: {
          position: 'bottom',
          labels: {
            boxWidth: 12,
            boxHeight: 12,
            borderRadius: 4,
            usePointStyle: true,
            pointStyle: 'circle',
            font: { family: 'Plus Jakarta Sans', size: 11, weight: '500' },
            color: '#475569',
            padding: 15
          }
        },
        tooltip: {
          backgroundColor: '#0f172a',
          titleFont: { family: 'Plus Jakarta Sans', size: 13, weight: '600' },
          bodyFont: { family: 'Plus Jakarta Sans', size: 13 },
          padding: 12,
          cornerRadius: 10,
          callbacks: {
            label: function (context) {
              return ' ' + context.label + ': ' + context.parsed + ' items';
            }
          }
        }
      }
    }
  });
}
