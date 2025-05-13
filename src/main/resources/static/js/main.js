let priceChart = null;
let websocket = null;

document.addEventListener('DOMContentLoaded', function() {
    const searchBtn = document.getElementById('searchBtn');
    const stockSymbol = document.getElementById('stockSymbol');

    searchBtn.addEventListener('click', () => {
        const symbol = stockSymbol.value.trim().toUpperCase();
        if (symbol) {
            fetchStockData(symbol);
            connectWebSocket(symbol);
        }
    });

    // Allow Enter key to trigger search
    stockSymbol.addEventListener('keydown', function(e) {
        if (e.key === 'Enter') {
            searchBtn.click();
        }
    });

    // Initialize empty chart
    initializeChart();
    fetchNews();
});

function initializeChart() {
    const ctx = document.getElementById('priceChart').getContext('2d');
    priceChart = new Chart(ctx, {
        type: 'line',
        data: {
            labels: [],
            datasets: [{
                label: 'Stock Price',
                data: [],
                borderColor: '#007bff',
                tension: 0.1
            }]
        },
        options: {
            responsive: true,
            scales: {
                y: {
                    beginAtZero: false
                }
            }
        }
    });
}

// Debounce function
function debounce(func, wait) {
    let timeout;
    return function(...args) {
        clearTimeout(timeout);
        timeout = setTimeout(() => func.apply(this, args), wait);
    };
}

const searchBtn = document.getElementById('search-btn');
const searchInput = document.getElementById('stock-search-input');
const priceSpan = document.getElementById('stock-price');
const errorMsg = document.getElementById('error-message');

function setLoading(isLoading) {
    if (isLoading) {
        searchBtn.disabled = true;
        searchBtn.innerHTML = '<span class="spinner"></span> Searching...';
    } else {
        searchBtn.disabled = false;
        searchBtn.innerHTML = 'Search';
    }
}

async function fetchStockData(symbol) {
    setLoading(true);
    errorMsg.textContent = '';
    try {
        const response = await fetch(`/api/stock/${symbol}`);
        if (!response.ok) throw new Error('Stock not found');
        const data = await response.json();
        priceSpan.innerHTML = '₹' + data.price.toFixed(2);
        updateStockDetails(data);
        updateChart(data);
    } catch (err) {
        priceSpan.innerHTML = '₹0.00';
        errorMsg.textContent = 'Stock not found or API error.';
        updateStockDetails({symbol: symbol, price: 0, change: 0, volume: 0});
    } finally {
        setLoading(false);
    }
}

const debouncedSearch = debounce(() => {
    const symbol = searchInput.value.trim();
    if (symbol) fetchStockData(symbol);
}, 500);

searchBtn.addEventListener('click', debouncedSearch);
searchInput.addEventListener('keydown', function(e) {
    if (e.key === 'Enter') debouncedSearch();
});

function updateStockDetails(data) {
    document.getElementById('symbol').textContent = data.symbol || '';
    document.getElementById('price').textContent = `₹${data.price !== undefined ? data.price.toFixed(2) : '0.00'}`;
    const changeElement = document.getElementById('change');
    const change = data.change || 0;
    changeElement.textContent = `${change >= 0 ? '+' : ''}${change.toFixed(2)}%`;
    changeElement.className = change >= 0 ? 'positive' : 'negative';
    document.getElementById('volume').textContent = data.volume !== undefined ? data.volume.toLocaleString() : '0';
}

function updateChart(data) {
    const timestamp = new Date().toLocaleTimeString();
    priceChart.data.labels.push(timestamp);
    priceChart.data.datasets[0].data.push(data.price || 0);
    if (priceChart.data.labels.length > 20) {
        priceChart.data.labels.shift();
        priceChart.data.datasets[0].data.shift();
    }
    priceChart.update();
}

function connectWebSocket(symbol) {
    if (websocket) {
        websocket.close();
    }
    websocket = new WebSocket(`ws://${window.location.host}/ws/stock/${symbol}`);
    websocket.onmessage = function(event) {
        const data = JSON.parse(event.data);
        updateStockDetails(data);
        updateChart(data);
    };
    websocket.onerror = function(error) {
        console.error('WebSocket error:', error);
    };
    websocket.onclose = function() {
        console.log('WebSocket connection closed');
    };
}

function fetchNews() {
    fetch('/api/news')
        .then(response => response.json())
        .then(data => {
            const newsContainer = document.getElementById('newsContainer');
            newsContainer.innerHTML = '';
            data.forEach(news => {
                const newsItem = document.createElement('div');
                newsItem.className = 'news-item';
                newsItem.innerHTML = `
                    <h4>${news.title}</h4>
                    <p>${news.summary}</p>
                    <small>${new Date(news.publishedAt).toLocaleString()}</small>
                `;
                newsContainer.appendChild(newsItem);
            });
        })
        .catch(error => console.error('Error fetching news:', error));
}

// Fetch news every 5 minutes
setInterval(fetchNews, 300000); 