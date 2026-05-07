const API = '';

// Active filter state
const filters = { category: 'all', gender: 'all', concern: '', rating: '0' };
let chatHistory = [];

// =====================
// SECTION NAVIGATION
// =====================
function showSection(name) {
  document.querySelectorAll('.section').forEach(s => s.classList.remove('active'));
  document.getElementById(`section-${name}`).classList.add('active');
  window.scrollTo({ top: 0, behavior: 'smooth' });
  if (name === 'products') loadProducts();
}

// =====================
// HOME PAGE
// =====================
async function initHome() {
  try {
    const [statsRes, productsRes] = await Promise.all([
      fetch(`${API}/api/stats`),
      fetch(`${API}/api/products?limit=8`)
    ]);
    const stats = await statsRes.json();
    const products = await productsRes.json();

    document.getElementById('stat-products').textContent = stats.total_products + '+';
    document.getElementById('stat-sources').textContent = stats.sources + '+';
    document.getElementById('stat-brands').textContent = stats.brands + '+';

    renderProductGrid('home-featured-grid', products);
  } catch (e) {
    console.error('Failed to load home data:', e);
    renderFallbackProducts('home-featured-grid');
  }
  loadConcernOptions();
}

function heroSearch() {
  const q = document.getElementById('hero-search').value.trim();
  if (!q) return;
  showSection('products');
  document.getElementById('products-search').value = q;
  setTimeout(() => searchProducts(), 100);
}

function quickSearch(concern) {
  showSection('products');
  setTimeout(() => {
    document.getElementById('products-search').value = concern;
    searchProducts();
  }, 100);
}

// =====================
// PRODUCTS PAGE
// =====================
async function loadProducts() {
  const grid = document.getElementById('products-grid');
  grid.innerHTML = '<div class="loading-spinner"><div class="spinner"></div></div>';

  const params = new URLSearchParams();
  if (filters.category !== 'all') params.set('category', filters.category);
  if (filters.gender !== 'all') params.set('gender', filters.gender);
  if (filters.concern) params.set('concern', filters.concern);
  if (parseFloat(filters.rating) > 0) params.set('min_rating', filters.rating);
  params.set('limit', '50');

  try {
    const res = await fetch(`${API}/api/products?${params}`);
    const products = await res.json();
    updateProductsHeader(products.length);
    renderProductGrid('products-grid', products);
  } catch (e) {
    grid.innerHTML = '<div class="empty-state"><h3>Failed to load products</h3><p>Please ensure the backend server is running.</p></div>';
  }
}

function updateProductsHeader(count) {
  const genderLabel = filters.gender === 'all' ? '' : `${capitalize(filters.gender)}'s `;
  const catLabel = filters.category === 'all' ? 'All Products' : capitalize(filters.category);
  document.getElementById('products-title').textContent = `${genderLabel}${catLabel}`;
  document.getElementById('products-count').textContent = `${count} product${count !== 1 ? 's' : ''} found`;
}

async function searchProducts() {
  const q = document.getElementById('products-search').value.trim();
  if (!q) { loadProducts(); return; }

  const grid = document.getElementById('products-grid');
  grid.innerHTML = '<div class="loading-spinner"><div class="spinner"></div></div>';

  try {
    const res = await fetch(`${API}/api/products/search/query?q=${encodeURIComponent(q)}`);
    const products = await res.json();
    document.getElementById('products-title').textContent = `Results for "${q}"`;
    document.getElementById('products-count').textContent = `${products.length} product${products.length !== 1 ? 's' : ''} found`;
    renderProductGrid('products-grid', products);
  } catch (e) {
    grid.innerHTML = '<div class="empty-state"><h3>Search failed</h3><p>Please try again.</p></div>';
  }
}

function filterByCategory(cat) {
  filters.category = cat;
  filters.gender = 'all';
  showSection('products');
  updateFilterButtons('category', cat);
  updateFilterButtons('gender', 'all');
  setTimeout(loadProducts, 100);
}

function filterByGenderAndCat(gender, cat) {
  filters.category = cat;
  filters.gender = gender;
  showSection('products');
  updateFilterButtons('category', cat);
  updateFilterButtons('gender', gender);
  setTimeout(loadProducts, 100);
}

function setFilter(type, value, btn) {
  filters[type] = value;
  if (btn && type !== 'concern') updateFilterButtons(type, value);
}

function updateFilterButtons(type, value) {
  document.querySelectorAll(`[data-filter="${type}"]`).forEach(b => {
    b.classList.toggle('active', b.dataset.value === value);
  });
}

function applyFilters() {
  filters.concern = document.getElementById('concern-select').value;
  loadProducts();
}

function resetFilters() {
  filters.category = 'all';
  filters.gender = 'all';
  filters.concern = '';
  filters.rating = '0';
  document.getElementById('concern-select').value = '';
  document.getElementById('products-search').value = '';
  updateFilterButtons('category', 'all');
  updateFilterButtons('gender', 'all');
  updateFilterButtons('rating', '0');
  loadProducts();
}

async function loadConcernOptions() {
  try {
    const res = await fetch(`${API}/api/concerns`);
    const concerns = await res.json();
    const select = document.getElementById('concern-select');
    concerns.forEach(c => {
      const opt = document.createElement('option');
      opt.value = c;
      opt.textContent = capitalize(c);
      select.appendChild(opt);
    });
  } catch (e) { /* non-critical */ }
}

// =====================
// RENDER PRODUCT CARD
// =====================
function renderProductGrid(containerId, products) {
  const grid = document.getElementById(containerId);
  if (!products.length) {
    grid.innerHTML = '<div class="empty-state"><h3>No products found</h3><p>Try adjusting your filters or search query.</p></div>';
    return;
  }
  grid.innerHTML = products.map(p => productCardHTML(p)).join('');
}

function productCardHTML(p) {
  const stars = starHTML(p.rating);
  const concerns = (p.concerns || []).slice(0, 3)
    .map(c => `<span class="concern-tag">${capitalize(c)}</span>`).join('');
  const genderDisplay = p.gender === 'unisex' ? 'Unisex' : capitalize(p.gender);

  return `
    <div class="product-card" onclick="openProductModal('${p.id}')">
      <div class="product-img">
        <img src="${p.image}" alt="${p.name}" loading="lazy"
          onerror="this.src='https://images.unsplash.com/photo-1556228578-0d85b1a4d571?w=400'" />
      </div>
      <div class="product-body">
        <div class="product-meta">
          <span class="tag tag-category">${capitalize(p.category)}</span>
          <span class="tag tag-gender">${genderDisplay}</span>
          <span class="tag tag-source">${p.source}</span>
        </div>
        <div class="product-name">${p.name}</div>
        <div class="product-brand">${p.brand}</div>
        <div class="product-rating">
          <span class="stars">${stars}</span>
          <span>${p.rating}</span>
          <span class="review-count">(${formatNum(p.reviews)})</span>
        </div>
        <div class="product-concerns">${concerns}</div>
        <div class="product-footer">
          <span class="product-price">${p.price}</span>
          <span class="product-source-btn">View on ${p.source}</span>
        </div>
      </div>
    </div>`;
}

// =====================
// PRODUCT MODAL
// =====================
async function openProductModal(productId) {
  const modal = document.getElementById('product-modal');
  modal.classList.add('open');
  document.body.style.overflow = 'hidden';

  try {
    const res = await fetch(`${API}/api/products/${productId}`);
    const p = await res.json();
    renderModal(p);
  } catch (e) {
    document.getElementById('modal-content').innerHTML = '<p style="padding:24px">Failed to load product details.</p>';
  }
}

function renderModal(p) {
  const stars = starHTML(p.rating);
  const genderDisplay = p.gender === 'unisex' ? 'Unisex' : capitalize(p.gender);
  const benefitsHTML = p.key_benefits.map(b => `<span class="modal-chip">${b}</span>`).join('');
  const ingredientsHTML = p.ingredients.map(i => `<span class="modal-chip">${i}</span>`).join('');
  const concernsHTML = p.concerns.map(c => `<span class="modal-chip">${capitalize(c)}</span>`).join('');

  document.getElementById('modal-content').innerHTML = `
    <div class="modal-img">
      <img src="${p.image}" alt="${p.name}"
        onerror="this.src='https://images.unsplash.com/photo-1556228578-0d85b1a4d571?w=400'" />
    </div>
    <div class="modal-body">
      <div class="modal-tags">
        <span class="tag tag-category">${capitalize(p.category)}</span>
        <span class="tag tag-gender">${genderDisplay}</span>
        <span class="tag tag-source">${p.source}</span>
      </div>
      <h2 class="modal-name">${p.name}</h2>
      <p class="modal-brand">${p.brand} · ${capitalize(p.subcategory)}</p>
      <div class="modal-rating">
        <span class="stars">${stars}</span>
        <strong>${p.rating}</strong>
        <span style="color:var(--text-light)">(${formatNum(p.reviews)} reviews)</span>
      </div>
      <div class="modal-price">${p.price}</div>
      <p class="modal-desc">${p.description}</p>

      <div class="modal-section">
        <h4>Key Benefits</h4>
        <div class="modal-chips">${benefitsHTML}</div>
      </div>
      <div class="modal-section">
        <h4>Treats / Concerns</h4>
        <div class="modal-chips">${concernsHTML}</div>
      </div>
      <div class="modal-section">
        <h4>Key Ingredients</h4>
        <div class="modal-chips">${ingredientsHTML}</div>
      </div>

      <div class="modal-cta">
        <a href="${p.source_url}" target="_blank" rel="noopener" class="modal-source-link">
          Shop on ${p.source} →
        </a>
      </div>
    </div>`;
}

function closeModal(e) {
  if (e.target === document.getElementById('product-modal')) closeModalBtn();
}
function closeModalBtn() {
  document.getElementById('product-modal').classList.remove('open');
  document.body.style.overflow = '';
}

// =====================
// AI ADVISOR
// =====================
async function getAIRecommendations() {
  const problem = document.getElementById('ai-problem').value.trim();
  if (!problem) {
    alert('Please describe your beauty concern first.');
    return;
  }

  const btn = document.getElementById('ai-submit-btn');
  btn.disabled = true;
  btn.textContent = 'Analyzing your concern...';

  const payload = {
    problem,
    gender: document.getElementById('ai-gender').value || null,
    age_range: document.getElementById('ai-age').value || null,
    skin_type: document.getElementById('ai-skin').value || null,
    hair_type: document.getElementById('ai-hair').value || null,
  };

  try {
    const res = await fetch(`${API}/api/ai/recommend`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    });

    if (!res.ok) {
      const err = await res.json();
      throw new Error(err.detail || 'AI service error');
    }

    const data = await res.json();

    document.getElementById('ai-results').style.display = 'block';

    // Format advice text with paragraphs
    const adviceText = data.advice.split('\n').filter(Boolean)
      .map(p => `<p style="margin-bottom:12px">${p}</p>`).join('');
    document.getElementById('ai-advice-text').innerHTML = adviceText;

    // Routine steps
    if (data.routine_steps && data.routine_steps.length) {
      const routineCard = document.getElementById('ai-routine-card');
      routineCard.style.display = 'block';
      document.getElementById('ai-routine-list').innerHTML =
        data.routine_steps.map(s => `<li>${s}</li>`).join('');
    }

    // Recommended products
    renderProductGrid('ai-products-grid', data.recommended_products);

    document.getElementById('ai-results').scrollIntoView({ behavior: 'smooth' });
  } catch (e) {
    alert(`Could not get AI recommendations: ${e.message}\n\nMake sure ANTHROPIC_API_KEY is set.`);
  } finally {
    btn.disabled = false;
    btn.textContent = 'Get AI Recommendations';
  }
}

// =====================
// CHAT
// =====================
async function sendChat() {
  const input = document.getElementById('chat-input');
  const msg = input.value.trim();
  if (!msg) return;
  input.value = '';
  sendChatMessage(msg);
}

async function sendChatMessage(msg) {
  appendChatMessage('user', msg);
  chatHistory.push({ role: 'user', content: msg });

  const btn = document.querySelector('.chat-send-btn');
  btn.disabled = true;

  const typingEl = appendTypingIndicator();

  try {
    const res = await fetch(`${API}/api/ai/chat`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ message: msg, history: chatHistory.slice(-10) }),
    });

    typingEl.remove();

    if (!res.ok) {
      const err = await res.json();
      appendChatMessage('bot', `Sorry, I couldn't process that: ${err.detail || 'Unknown error'}. Please ensure ANTHROPIC_API_KEY is configured.`);
      return;
    }

    const data = await res.json();
    appendChatMessage('bot', data.reply);
    chatHistory.push({ role: 'assistant', content: data.reply });
  } catch (e) {
    typingEl.remove();
    appendChatMessage('bot', 'Connection error. Please ensure the backend server is running.');
  } finally {
    btn.disabled = false;
  }
}

function appendChatMessage(role, text) {
  const container = document.getElementById('chat-messages');
  const div = document.createElement('div');
  div.className = `chat-msg ${role}`;
  div.innerHTML = `<div class="chat-bubble">${escapeHTML(text)}</div>`;
  container.appendChild(div);
  container.scrollTop = container.scrollHeight;
  return div;
}

function appendTypingIndicator() {
  const container = document.getElementById('chat-messages');
  const div = document.createElement('div');
  div.className = 'chat-msg bot';
  div.innerHTML = `<div class="chat-bubble"><div class="typing-indicator"><span></span><span></span><span></span></div></div>`;
  container.appendChild(div);
  container.scrollTop = container.scrollHeight;
  return div;
}

// =====================
// FALLBACK DATA
// =====================
function renderFallbackProducts(containerId) {
  const fallback = [
    { id: 'f1', name: 'CeraVe Moisturizing Cream', brand: 'CeraVe', category: 'skincare', subcategory: 'moisturizer', gender: 'unisex', concerns: ['dry skin', 'eczema'], description: 'Hydrating moisturizer with ceramides.', price: '$19.99', rating: 4.8, reviews: 45230, image: 'https://images.unsplash.com/photo-1556228578-0d85b1a4d571?w=400', source: 'Amazon', source_url: 'https://amazon.com', ingredients: ['Ceramides', 'Hyaluronic Acid'], key_benefits: ['24-hour hydration', 'Fragrance-free'] },
    { id: 'f2', name: 'OGX Argan Oil Shampoo', brand: 'OGX', category: 'haircare', subcategory: 'shampoo', gender: 'women', concerns: ['dry hair', 'frizzy hair'], description: 'Nourishing shampoo with argan oil.', price: '$9.99', rating: 4.5, reviews: 89400, image: 'https://images.unsplash.com/photo-1585747860715-2ba37e788b70?w=400', source: 'Target', source_url: 'https://target.com', ingredients: ['Argan Oil', 'Keratin'], key_benefits: ['Reduces frizz', 'Adds shine'] },
  ];
  renderProductGrid(containerId, fallback);
}

// =====================
// HELPERS
// =====================
function starHTML(rating) {
  const full = Math.floor(rating);
  const half = rating % 1 >= 0.5 ? 1 : 0;
  const empty = 5 - full - half;
  return '★'.repeat(full) + (half ? '½' : '') + '☆'.repeat(empty);
}

function formatNum(n) {
  if (n >= 1000) return (n / 1000).toFixed(1) + 'k';
  return String(n);
}

function capitalize(str) {
  if (!str) return '';
  return str.charAt(0).toUpperCase() + str.slice(1);
}

function escapeHTML(text) {
  return text
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/\n/g, '<br>');
}

// =====================
// INIT
// =====================
document.addEventListener('DOMContentLoaded', () => {
  initHome();
});
