/**
 * ShopEase — Microservices Online Shopping Frontend Application
 * Handles interaction with Product Service (port 8081) and Order Service (port 8082).
 */

const PRODUCT_API = 'http://localhost:8081/api/products';
const ORDER_API = 'http://localhost:8082/api/orders';

// State Management
let products = [];
let orders = [];
let cart = JSON.parse(localStorage.getItem('shopease_cart') || '[]');
let activeCategory = 'ALL';
let searchKeyword = '';

// DOM Elements
const productsGrid = document.getElementById('products-grid');
const productCountBadge = document.getElementById('product-count-badge');
const ordersTbody = document.getElementById('orders-tbody');
const adminProductsTbody = document.getElementById('admin-products-tbody');
const cartCount = document.getElementById('cart-count');
const cartDrawer = document.getElementById('cart-drawer');
const cartOverlay = document.getElementById('cart-overlay');
const cartItemsList = document.getElementById('cart-items-list');
const cartFooter = document.getElementById('cart-footer');
const cartSubtotal = document.getElementById('cart-subtotal');
const cartTotal = document.getElementById('cart-total');
const btnTotalAmount = document.getElementById('btn-total-amount');
const checkoutForm = document.getElementById('checkout-form');
const searchInput = document.getElementById('search-input');
const clearSearchBtn = document.getElementById('clear-search-btn');

// Health Check Elements
const productStatusText = document.getElementById('product-status-text');
const orderStatusText = document.getElementById('order-status-text');
const productPillDot = document.querySelector('#product-service-pill .pulse-dot');
const orderPillDot = document.querySelector('#order-service-pill .pulse-dot');

// ==============================================================================
// 1. Initialization & Event Listeners
// ==============================================================================
document.addEventListener('DOMContentLoaded', () => {
    initTabs();
    initCartDrawer();
    initSearchAndFilters();
    initAdminModal();
    updateCartUI();

    // Fetch initial data
    checkServicesHealth();
    fetchProducts();
    fetchOrders();

    // Periodic health check every 15s
    setInterval(checkServicesHealth, 15000);
});

// ==============================================================================
// 2. Health Monitoring
// ==============================================================================
async function checkServicesHealth() {
    // Check Product Service
    try {
        const res = await fetch('http://localhost:8081/actuator/health', { method: 'GET', mode: 'cors' })
            .catch(() => fetch(PRODUCT_API, { method: 'GET', mode: 'cors' }));
        if (res && res.ok) {
            setServiceStatus('product', true);
        } else {
            setServiceStatus('product', false);
        }
    } catch {
        setServiceStatus('product', false);
    }

    // Check Order Service
    try {
        const res = await fetch('http://localhost:8082/actuator/health', { method: 'GET', mode: 'cors' })
            .catch(() => fetch(ORDER_API, { method: 'GET', mode: 'cors' }));
        if (res && res.ok) {
            setServiceStatus('order', true);
        } else {
            setServiceStatus('order', false);
        }
    } catch {
        setServiceStatus('order', false);
    }
}

function setServiceStatus(service, isOnline) {
    if (service === 'product') {
        productStatusText.textContent = isOnline ? 'Online' : 'Offline';
        productPillDot.className = 'pulse-dot ' + (isOnline ? 'status-online' : 'status-offline');
    } else {
        orderStatusText.textContent = isOnline ? 'Online' : 'Offline';
        orderPillDot.className = 'pulse-dot ' + (isOnline ? 'status-online' : 'status-offline');
    }
}

// ==============================================================================
// 3. Navigation Tabs
// ==============================================================================
function initTabs() {
    const tabButtons = document.querySelectorAll('.tab-btn');
    const tabContents = document.querySelectorAll('.tab-content');

    tabButtons.forEach(btn => {
        btn.addEventListener('click', () => {
            tabButtons.forEach(b => b.classList.remove('active'));
            tabContents.forEach(c => c.classList.remove('active'));

            btn.classList.add('active');
            const targetId = btn.dataset.tab;
            document.getElementById(targetId).classList.add('active');

            if (targetId === 'orders-tab') {
                fetchOrders();
            } else if (targetId === 'admin-tab' || targetId === 'catalog-tab') {
                fetchProducts();
            }
        });
    });
}

// ==============================================================================
// 4. Products Management & Rendering
// ==============================================================================
async function fetchProducts() {
    try {
        let url = PRODUCT_API;
        if (searchKeyword) {
            url = `${PRODUCT_API}/search?keyword=${encodeURIComponent(searchKeyword)}`;
        }
        const res = await fetch(url);
        const json = await res.json();

        if (json.success && Array.isArray(json.data)) {
            products = json.data;
            renderCatalog();
            renderAdminProducts();
        } else {
            showToast('Failed to load products from server.', 'error');
        }
    } catch (err) {
        console.error('Error fetching products:', err);
        productsGrid.innerHTML = `
            <div class="empty-cart-state" style="grid-column: 1/-1;">
                <i class="fa-solid fa-triangle-exclamation" style="color:#ef4444;"></i>
                <p>Unable to connect to Product Service (:8081)</p>
                <span>Please ensure Product Service is started and running.</span>
            </div>`;
    }
}

function renderCatalog() {
    let filtered = products;
    if (activeCategory !== 'ALL') {
        filtered = filtered.filter(p => p.category.toLowerCase() === activeCategory.toLowerCase());
    }

    productCountBadge.textContent = `${filtered.length} items`;

    if (filtered.length === 0) {
        productsGrid.innerHTML = `
            <div class="empty-cart-state" style="grid-column: 1/-1;">
                <i class="fa-solid fa-box-open"></i>
                <p>No products found.</p>
                <span>Try changing your search keywords or category filters.</span>
            </div>`;
        return;
    }

    productsGrid.innerHTML = filtered.map(p => {
        const inStock = p.stock > 0;
        const stockClass = p.stock > 10 ? 'stock-in' : (p.stock > 0 ? 'stock-low' : 'stock-out');
        const stockLabel = p.stock > 10 ? `${p.stock} in stock` : (p.stock > 0 ? `Only ${p.stock} left!` : 'Out of stock');
        const imgUrl = p.imageUrl || 'https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=600&auto=format&fit=crop&q=80';

        return `
            <div class="product-card">
                <div class="product-img-wrapper">
                    <img src="${imgUrl}" alt="${escapeHtml(p.name)}" class="product-img" onerror="this.src='https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=600&auto=format&fit=crop&q=80'">
                    <span class="product-category-tag">${escapeHtml(p.category)}</span>
                    <span class="product-stock-tag ${stockClass}">${stockLabel}</span>
                </div>
                <div class="product-body">
                    <h3 class="product-name">${escapeHtml(p.name)}</h3>
                    <p class="product-desc">${escapeHtml(p.description || 'No description provided.')}</p>
                    <div class="product-footer">
                        <span class="product-price">$${Number(p.price).toFixed(2)}</span>
                        <button class="btn btn-primary btn-sm" ${!inStock ? 'disabled' : ''} onclick="addToCart(${p.id})">
                            <i class="fa-solid fa-cart-plus"></i> ${inStock ? 'Add to Cart' : 'Out of Stock'}
                        </button>
                    </div>
                </div>
            </div>
        `;
    }).join('');
}

function renderAdminProducts() {
    if (!adminProductsTbody) return;

    if (products.length === 0) {
        adminProductsTbody.innerHTML = `<tr><td colspan="7" class="text-center py-4">No products in catalog.</td></tr>`;
        return;
    }

    adminProductsTbody.innerHTML = products.map(p => {
        const imgUrl = p.imageUrl || 'https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=600&auto=format&fit=crop&q=80';
        return `
            <tr>
                <td><strong>#${p.id}</strong></td>
                <td><img src="${imgUrl}" alt="" style="width:40px;height:40px;object-fit:cover;border-radius:4px;"></td>
                <td><strong>${escapeHtml(p.name)}</strong></td>
                <td><span class="badge badge-product">${escapeHtml(p.category)}</span></td>
                <td><strong>$${Number(p.price).toFixed(2)}</strong></td>
                <td><span class="badge ${p.stock > 0 ? 'badge-delivered' : 'badge-cancelled'}">${p.stock} units</span></td>
                <td>
                    <div style="display:flex;gap:0.4rem;">
                        <button class="btn btn-secondary btn-sm" onclick="openEditProductModal(${p.id})">
                            <i class="fa-solid fa-pen"></i> Edit
                        </button>
                        <button class="btn btn-danger btn-sm" onclick="deleteProduct(${p.id})">
                            <i class="fa-solid fa-trash"></i>
                        </button>
                    </div>
                </td>
            </tr>
        `;
    }).join('');
}

// Search & Filter controls
function initSearchAndFilters() {
    const chips = document.querySelectorAll('.filter-chip');
    chips.forEach(chip => {
        chip.addEventListener('click', () => {
            chips.forEach(c => c.classList.remove('active'));
            chip.classList.add('active');
            activeCategory = chip.dataset.category;
            renderCatalog();
        });
    });

    let searchTimeout;
    searchInput.addEventListener('input', (e) => {
        searchKeyword = e.target.value.trim();
        clearSearchBtn.style.display = searchKeyword ? 'block' : 'none';
        clearTimeout(searchTimeout);
        searchTimeout = setTimeout(() => {
            fetchProducts();
        }, 300);
    });

    clearSearchBtn.addEventListener('click', () => {
        searchInput.value = '';
        searchKeyword = '';
        clearSearchBtn.style.display = 'none';
        fetchProducts();
    });

    document.getElementById('refresh-products-btn').addEventListener('click', () => {
        fetchProducts();
        showToast('Products refreshed', 'info');
    });
}

// ==============================================================================
// 5. Shopping Cart Operations
// ==============================================================================
function initCartDrawer() {
    const openCartBtn = document.getElementById('open-cart-btn');
    const closeCartBtn = document.getElementById('close-cart-btn');

    openCartBtn.addEventListener('click', () => {
        cartDrawer.classList.add('active');
        cartOverlay.classList.add('active');
    });

    closeCartBtn.addEventListener('click', closeCart);
    cartOverlay.addEventListener('click', closeCart);

    checkoutForm.addEventListener('submit', handleCheckout);
}

function closeCart() {
    cartDrawer.classList.remove('active');
    cartOverlay.classList.remove('active');
}

function addToCart(productId) {
    const product = products.find(p => p.id === productId);
    if (!product) return;

    const existing = cart.find(item => item.productId === productId);
    if (existing) {
        if (existing.quantity >= product.stock) {
            showToast(`Cannot add more. Only ${product.stock} units in stock.`, 'error');
            return;
        }
        existing.quantity += 1;
    } else {
        cart.push({
            productId: product.id,
            productName: product.name,
            unitPrice: Number(product.price),
            imageUrl: product.imageUrl,
            quantity: 1,
            maxStock: product.stock
        });
    }

    saveCart();
    updateCartUI();
    showToast(`Added '${product.name}' to cart!`, 'success');
}

function updateCartQuantity(productId, delta) {
    const item = cart.find(i => i.productId === productId);
    if (!item) return;

    const product = products.find(p => p.id === productId);
    const maxStock = product ? product.stock : item.maxStock;

    item.quantity += delta;

    if (item.quantity > maxStock) {
        item.quantity = maxStock;
        showToast(`Stock limit reached for ${item.productName}`, 'warning');
    }

    if (item.quantity <= 0) {
        cart = cart.filter(i => i.productId !== productId);
    }

    saveCart();
    updateCartUI();
}

function saveCart() {
    localStorage.setItem('shopease_cart', JSON.stringify(cart));
}

function updateCartUI() {
    const totalCount = cart.reduce((sum, item) => sum + item.quantity, 0);
    cartCount.textContent = totalCount;

    if (cart.length === 0) {
        cartItemsList.innerHTML = `
            <div class="empty-cart-state">
                <i class="fa-solid fa-cart-arrow-down"></i>
                <p>Your cart is empty.</p>
                <span>Add some products from the catalog to get started!</span>
            </div>`;
        cartFooter.style.display = 'none';
        return;
    }

    cartFooter.style.display = 'block';

    let total = 0;
    cartItemsList.innerHTML = cart.map(item => {
        const subtotal = item.unitPrice * item.quantity;
        total += subtotal;
        const imgUrl = item.imageUrl || 'https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=600&auto=format&fit=crop&q=80';

        return `
            <div class="cart-item">
                <img src="${imgUrl}" alt="${escapeHtml(item.productName)}" class="cart-item-img" onerror="this.src='https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=600&auto=format&fit=crop&q=80'">
                <div class="cart-item-info">
                    <div class="cart-item-title">${escapeHtml(item.productName)}</div>
                    <div class="cart-item-price">$${item.unitPrice.toFixed(2)} &times; ${item.quantity} = <strong>$${subtotal.toFixed(2)}</strong></div>
                </div>
                <div class="cart-item-qty">
                    <button class="qty-btn" onclick="updateCartQuantity(${item.productId}, -1)">-</button>
                    <span style="font-weight:700;font-size:0.85rem;min-width:18px;text-align:center;">${item.quantity}</span>
                    <button class="qty-btn" onclick="updateCartQuantity(${item.productId}, 1)">+</button>
                </div>
            </div>
        `;
    }).join('');

    const formattedTotal = `$${total.toFixed(2)}`;
    cartSubtotal.textContent = formattedTotal;
    cartTotal.textContent = formattedTotal;
    btnTotalAmount.textContent = total.toFixed(2);
}

// ==============================================================================
// 6. Order Placement (Inter-Service Orchestration)
// ==============================================================================
async function handleCheckout(e) {
    e.preventDefault();
    if (cart.length === 0) return;

    const placeBtn = document.getElementById('place-order-btn');
    placeBtn.disabled = true;
    placeBtn.innerHTML = `<i class="fa-solid fa-spinner fa-spin"></i> Processing Order...`;

    const orderPayload = {
        customerName: document.getElementById('cust-name').value.trim(),
        customerEmail: document.getElementById('cust-email').value.trim(),
        customerAddress: document.getElementById('cust-address').value.trim(),
        items: cart.map(i => ({
            productId: i.productId,
            quantity: i.quantity
        }))
    };

    try {
        const res = await fetch(ORDER_API, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(orderPayload)
        });

        const json = await res.json();

        if (res.ok && json.success) {
            showToast(`Order #${json.data.orderNumber} placed successfully!`, 'success');
            cart = [];
            saveCart();
            updateCartUI();
            closeCart();
            checkoutForm.reset();

            // Refresh product catalog (to reflect deducted stock) and orders list
            fetchProducts();
            fetchOrders();
        } else {
            showToast(json.message || 'Order placement failed. Check stock availability.', 'error');
        }
    } catch (err) {
        console.error('Checkout error:', err);
        showToast('Network error contacting Order Service (:8082). Is it running?', 'error');
    } finally {
        placeBtn.disabled = false;
        placeBtn.innerHTML = `<i class="fa-solid fa-lock"></i> Place Order ($<span id="btn-total-amount">${cartTotal.textContent.replace('$', '')}</span>)`;
    }
}

// ==============================================================================
// 7. Orders Dashboard Operations
// ==============================================================================
async function fetchOrders() {
    try {
        const res = await fetch(ORDER_API);
        const json = await res.json();

        if (json.success && Array.isArray(json.data)) {
            orders = json.data;
            renderOrders();
        }
    } catch (err) {
        console.error('Error fetching orders:', err);
        if (ordersTbody) {
            ordersTbody.innerHTML = `
                <tr>
                    <td colspan="7" class="text-center py-4" style="color:#ef4444;">
                        <i class="fa-solid fa-triangle-exclamation"></i> Unable to connect to Order Service (:8082).
                    </td>
                </tr>`;
        }
    }
}

function renderOrders() {
    if (!ordersTbody) return;

    if (orders.length === 0) {
        ordersTbody.innerHTML = `<tr><td colspan="7" class="text-center py-4">No orders placed yet.</td></tr>`;
        return;
    }

    ordersTbody.innerHTML = orders.map(order => {
        const dateStr = order.orderDate ? new Date(order.orderDate).toLocaleString() : 'N/A';
        const itemsSummary = (order.items || []).map(i => `${escapeHtml(i.productName)} (x${i.quantity})`).join(', ');

        const statusBadgeClass = {
            'PLACED': 'badge-placed',
            'PENDING': 'badge-pending',
            'SHIPPED': 'badge-shipped',
            'DELIVERED': 'badge-delivered',
            'CANCELLED': 'badge-cancelled'
        }[order.status] || 'badge-placed';

        const isCancelled = order.status === 'CANCELLED';

        return `
            <tr>
                <td><strong>${order.orderNumber}</strong></td>
                <td style="font-size:0.8rem;color:#64748b;">${dateStr}</td>
                <td>
                    <strong>${escapeHtml(order.customerName)}</strong><br>
                    <span style="font-size:0.8rem;color:#64748b;">${escapeHtml(order.customerEmail)}</span>
                </td>
                <td style="max-width:260px;font-size:0.85rem;" title="${escapeHtml(itemsSummary)}">
                    ${escapeHtml(itemsSummary)}
                </td>
                <td><strong>$${Number(order.totalAmount).toFixed(2)}</strong></td>
                <td>
                    <select class="status-select ${statusBadgeClass}" onchange="changeOrderStatus(${order.id}, this.value)" ${isCancelled ? 'disabled' : ''}>
                        <option value="PLACED" ${order.status === 'PLACED' ? 'selected' : ''}>PLACED</option>
                        <option value="PENDING" ${order.status === 'PENDING' ? 'selected' : ''}>PENDING</option>
                        <option value="SHIPPED" ${order.status === 'SHIPPED' ? 'selected' : ''}>SHIPPED</option>
                        <option value="DELIVERED" ${order.status === 'DELIVERED' ? 'selected' : ''}>DELIVERED</option>
                        <option value="CANCELLED" ${order.status === 'CANCELLED' ? 'selected' : ''}>CANCELLED</option>
                    </select>
                </td>
                <td>
                    <button class="btn btn-danger btn-sm" onclick="cancelOrder(${order.id})" ${isCancelled ? 'disabled' : ''} title="Cancel and Restore Stock">
                        <i class="fa-solid fa-ban"></i> Cancel
                    </button>
                </td>
            </tr>
        `;
    }).join('');
}

document.getElementById('refresh-orders-btn')?.addEventListener('click', () => {
    fetchOrders();
    showToast('Orders refreshed', 'info');
});

async function changeOrderStatus(orderId, newStatus) {
    try {
        const res = await fetch(`${ORDER_API}/${orderId}/status`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ status: newStatus })
        });
        const json = await res.json();
        if (res.ok && json.success) {
            showToast(`Order status updated to ${newStatus}`, 'success');
            fetchOrders();
            if (newStatus === 'CANCELLED') {
                fetchProducts(); // Stock was restored!
            }
        } else {
            showToast(json.message || 'Failed to update order status.', 'error');
            fetchOrders();
        }
    } catch (err) {
        showToast('Error updating status: ' + err.message, 'error');
    }
}

async function cancelOrder(orderId) {
    if (!confirm('Are you sure you want to cancel this order? The inventory will be automatically restored to the Product Service.')) {
        return;
    }

    try {
        const res = await fetch(`${ORDER_API}/${orderId}/cancel`, { method: 'PUT' });
        const json = await res.json();

        if (res.ok && json.success) {
            showToast('Order cancelled and product inventory restored!', 'success');
            fetchOrders();
            fetchProducts();
        } else {
            showToast(json.message || 'Could not cancel order.', 'error');
        }
    } catch (err) {
        showToast('Error cancelling order: ' + err.message, 'error');
    }
}

// ==============================================================================
// 8. Admin Product Modal & CRUD
// ==============================================================================
function initAdminModal() {
    const modal = document.getElementById('product-modal');
    const openAddBtn = document.getElementById('open-add-product-modal-btn');
    const closeBtn = document.getElementById('close-product-modal-btn');
    const cancelBtn = document.getElementById('cancel-product-modal-btn');
    const form = document.getElementById('product-form');

    openAddBtn.addEventListener('click', () => {
        document.getElementById('product-modal-title').textContent = 'Add New Product';
        document.getElementById('prod-id').value = '';
        form.reset();
        modal.classList.add('active');
    });

    const closeModal = () => modal.classList.remove('active');
    closeBtn.addEventListener('click', closeModal);
    cancelBtn.addEventListener('click', closeModal);

    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        const prodId = document.getElementById('prod-id').value;
        const payload = {
            name: document.getElementById('prod-name').value.trim(),
            category: document.getElementById('prod-category').value,
            price: parseFloat(document.getElementById('prod-price').value),
            stock: parseInt(document.getElementById('prod-stock').value, 10),
            imageUrl: document.getElementById('prod-image').value.trim(),
            description: document.getElementById('prod-description').value.trim()
        };

        try {
            const url = prodId ? `${PRODUCT_API}/${prodId}` : PRODUCT_API;
            const method = prodId ? 'PUT' : 'POST';

            const res = await fetch(url, {
                method,
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });

            const json = await res.json();
            if (res.ok && json.success) {
                showToast(`Product ${prodId ? 'updated' : 'created'} successfully!`, 'success');
                closeModal();
                fetchProducts();
            } else {
                showToast(json.message || 'Failed to save product.', 'error');
            }
        } catch (err) {
            showToast('Error saving product: ' + err.message, 'error');
        }
    });
}

function openEditProductModal(productId) {
    const product = products.find(p => p.id === productId);
    if (!product) return;

    document.getElementById('product-modal-title').textContent = 'Edit Product';
    document.getElementById('prod-id').value = product.id;
    document.getElementById('prod-name').value = product.name;
    document.getElementById('prod-category').value = product.category;
    document.getElementById('prod-price').value = product.price;
    document.getElementById('prod-stock').value = product.stock;
    document.getElementById('prod-image').value = product.imageUrl || '';
    document.getElementById('prod-description').value = product.description || '';

    document.getElementById('product-modal').classList.add('active');
}

async function deleteProduct(productId) {
    if (!confirm('Are you sure you want to delete this product from the catalog?')) {
        return;
    }

    try {
        const res = await fetch(`${PRODUCT_API}/${productId}`, { method: 'DELETE' });
        const json = await res.json();
        if (res.ok && json.success) {
            showToast('Product deleted from catalog.', 'success');
            fetchProducts();
        } else {
            showToast(json.message || 'Failed to delete product.', 'error');
        }
    } catch (err) {
        showToast('Error deleting product: ' + err.message, 'error');
    }
}

// ==============================================================================
// 9. Toast Notification System & Helpers
// ==============================================================================
function showToast(message, type = 'info') {
    const container = document.getElementById('toast-container');
    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;

    const icon = {
        'success': 'fa-circle-check',
        'error': 'fa-circle-exclamation',
        'warning': 'fa-triangle-exclamation',
        'info': 'fa-circle-info'
    }[type] || 'fa-circle-info';

    toast.innerHTML = `<i class="fa-solid ${icon}"></i> <span>${escapeHtml(message)}</span>`;
    container.appendChild(toast);

    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateX(100%)';
        setTimeout(() => toast.remove(), 300);
    }, 3500);
}

function escapeHtml(str) {
    if (!str) return '';
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
}
