/* Ordra interactive mockup.
   Sample data stands in for Room. When the live app UI changes, update this
   file and index.html in the same change (docs/RULES.md → UI mockup consistency). */

/* ---------- reference data (mirrors domain enums) ---------- */
const PLATFORMS = {
  SHOPEE:      { label: "Shopee",      color: "#EE4D2D", initial: "S" },
  TIKTOK_SHOP: { label: "TikTok Shop", color: "#171821", initial: "T" },
  LAZADA:      { label: "Lazada",      color: "#1A0DAB", initial: "L" },
  TEMU:        { label: "Temu",        color: "#FB7701", initial: "Te" },
  AMAZON:      { label: "Amazon",      color: "#232F3E", initial: "A" },
  FACEBOOK:    { label: "Facebook",    color: "#1877F2", initial: "f" },
  INSTAGRAM:   { label: "Instagram",   color: "#C13584", initial: "I" },
  OTHER:       { label: "Other",       color: "#666979", initial: "?" },
};

const STATUSES = {
  ORDERED:          { label: "Ordered",          color: "#5B5FEF", step: 0 },
  PROCESSING:       { label: "Processing",       color: "#7C5CFC", step: 1 },
  PACKED:           { label: "Packed",           color: "#3B82F6", step: 2 },
  SHIPPED:          { label: "Shipped",          color: "#06B6D4", step: 3 },
  OUT_FOR_DELIVERY: { label: "Out for delivery", color: "#D98E04", step: 4 },
  DELIVERED:        { label: "Delivered",        color: "#0FA881", step: 5 },
  CANCELLED:        { label: "Cancelled",        color: "#F05252", step: -1 },
  RETURNED:         { label: "Returned",         color: "#F97316", step: -1 },
  REFUNDED:         { label: "Refunded",         color: "#14B8A6", step: -1 },
  DRAFT:            { label: "Draft",            color: "#666979", step: -1 },
};
const TIMELINE_STEPS = ["Ordered", "Processing", "Packed", "Shipped", "Out for delivery", "Delivered"];
const TERMINAL = ["DELIVERED", "CANCELLED", "RETURNED", "REFUNDED"];
const PAY_METHODS = ["COD", "GCash", "Maya", "Card", "Bank transfer", "Other"];
const PAY_STATUSES = ["UNPAID", "PAID", "PARTIAL"];
const COURIERS = ["J&T Express", "Ninja Van", "Flash Express", "LBC", "JRS Express", "SPX Express", "Other"];

/* ---------- helpers ---------- */
const d = n => { const t = new Date(); t.setDate(t.getDate() + n); return t.toISOString().slice(0, 10); };
const peso = minor => "₱" + (minor / 100).toLocaleString("en-PH", { maximumFractionDigits: minor % 100 ? 2 : 0 });
const fmtDate = iso => iso ? new Date(iso + "T00:00:00").toLocaleDateString("en-PH", { month: "short", day: "numeric" }) : "—";
const esc = s => String(s ?? "").replace(/[&<>"']/g, c => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[c]));
const orderTotal = o => o.items.reduce((s, i) => s + i.qty * i.priceMinor, 0) + o.shippingMinor - o.discountMinor;
const itemsSummary = o => o.items.length === 1 ? o.items[0].name : `${o.items[0].name} + ${o.items.length - 1} more`;

/* ---------- sample data ---------- */
let nextId = 10;
let orders = [
  {
    id: "ord-1", platform: "SHOPEE", sellerName: "AudioHub PH", isCod: true,
    paymentMethod: "COD", paymentStatus: "UNPAID", status: "OUT_FOR_DELIVERY",
    courier: "J&T Express", tracking: "JT824011592PH", orderedAt: d(-4), etaStart: d(0), etaEnd: d(1), deliveredAt: null,
    items: [{ name: "Soundcore R50i earbuds", variant: "Black", qty: 1, priceMinor: 129900 }],
    shippingMinor: 3800, discountMinor: 5000, notes: "Rider usually calls first. Prepare exact amount.",
    archived: false, createdAt: d(-4),
  },
  {
    id: "ord-2", platform: "TIKTOK_SHOP", sellerName: "StreetLab Manila", isCod: false,
    paymentMethod: "GCash", paymentStatus: "PAID", status: "SHIPPED",
    courier: "Flash Express", tracking: "FLX99120455", orderedAt: d(-2), etaStart: d(2), etaEnd: d(4), deliveredAt: null,
    items: [{ name: "Oversized graphic tee", variant: "Cream / L", qty: 1, priceMinor: 45900 }],
    shippingMinor: 0, discountMinor: 0, notes: "", archived: false, createdAt: d(-2),
  },
  {
    id: "ord-3", platform: "SHOPEE", sellerName: "HydraFlask", isCod: true,
    paymentMethod: "COD", paymentStatus: "UNPAID", status: "PACKED",
    courier: "SPX Express", tracking: "SPX71200843", orderedAt: d(-1), etaStart: d(4), etaEnd: d(6), deliveredAt: null,
    items: [{ name: "Stainless tumbler 1L", variant: "Sage", qty: 1, priceMinor: 38900 }],
    shippingMinor: 4500, discountMinor: 0, notes: "", archived: false, createdAt: d(-1),
  },
  {
    id: "ord-4", platform: "FACEBOOK", sellerName: "Iloilo Crafts", customPlatformName: "FB Marketplace", isCod: false,
    paymentMethod: "Bank transfer", paymentStatus: "PAID", status: "PROCESSING",
    courier: "LBC", tracking: "", orderedAt: d(-3), etaStart: d(7), etaEnd: d(10), deliveredAt: null,
    items: [{ name: "Handmade woven bag", variant: "Natural", qty: 1, priceMinor: 95000 }],
    shippingMinor: 12000, discountMinor: 0, notes: "Seller ships every Friday.", archived: false, createdAt: d(-3),
  },
  {
    id: "ord-5", platform: "INSTAGRAM", sellerName: "analog.supply", isCod: true,
    paymentMethod: "COD", paymentStatus: "UNPAID", status: "ORDERED",
    courier: "", tracking: "", orderedAt: d(0), etaStart: d(5), etaEnd: d(8), deliveredAt: null,
    items: [{ name: "Film camera strap", variant: "Tan leather", qty: 1, priceMinor: 78000 }],
    shippingMinor: 9000, discountMinor: 0, notes: "", archived: false, createdAt: d(0),
  },
  {
    id: "ord-6", platform: "TEMU", sellerName: "Temu", isCod: false,
    paymentMethod: "Card", paymentStatus: "PAID", status: "ORDERED",
    courier: "", tracking: "", orderedAt: d(-1), etaStart: d(9), etaEnd: d(14), deliveredAt: null,
    items: [
      { name: "LED strip 5m RGB", variant: "", qty: 2, priceMinor: 16000 },
      { name: "Cable clips 20pc", variant: "", qty: 1, priceMinor: 4900 },
    ],
    shippingMinor: 0, discountMinor: 4900, notes: "", archived: false, createdAt: d(-1),
  },
  {
    id: "ord-7", platform: "LAZADA", sellerName: "Keychron PH", isCod: false,
    paymentMethod: "Maya", paymentStatus: "PAID", status: "DELIVERED",
    courier: "Ninja Van", tracking: "NVPH55021933", orderedAt: d(-9), etaStart: d(-2), etaEnd: d(-1), deliveredAt: d(-1),
    items: [{ name: "K870 mechanical keyboard", variant: "Hot-swap / Brown", qty: 1, priceMinor: 215000 }],
    shippingMinor: 0, discountMinor: 15000, notes: "", archived: false, createdAt: d(-9),
    rating: 5, isFavorite: true,
    warranties: [{ provider: "Keychron", start: d(-9), end: d(356) }],
    receipts: [{ label: "Lazada invoice" }],
  },
  {
    id: "ord-8", platform: "SHOPEE", sellerName: "ClickCity", isCod: false,
    paymentMethod: "GCash", paymentStatus: "PAID", status: "RETURNED",
    courier: "J&T Express", tracking: "JT820177455", orderedAt: d(-12), etaStart: d(-7), etaEnd: d(-6), deliveredAt: d(-6),
    items: [{ name: "Wireless mouse", variant: "Grey", qty: 1, priceMinor: 64900 }],
    shippingMinor: 3800, discountMinor: 0, notes: "Scroll wheel defective — returned, refund pending.", archived: false, createdAt: d(-12),
    rating: 2, isFavorite: false,
    returns: [{ status: "COMPLETED", reason: "Scroll wheel defective", at: d(-6) }],
    refunds: [{ status: "PENDING", amountMinor: 64900, reason: "Defective item" }],
  },
  {
    id: "ord-9", platform: "AMAZON", sellerName: "Amazon", isCod: false,
    paymentMethod: "Card", paymentStatus: "PAID", status: "DELIVERED",
    courier: "UPS", tracking: "1Z8842W3PH", orderedAt: d(-21), etaStart: d(-14), etaEnd: d(-12), deliveredAt: d(-13),
    items: [{ name: "The Design of Everyday Things", variant: "Paperback", qty: 1, priceMinor: 112000 }],
    shippingMinor: 0, discountMinor: 0, notes: "", archived: true, createdAt: d(-21),
    rating: 5, isFavorite: true,
  },
];

/* ---------- detection suggestions (Phase 5 mirrors) ---------- */
let detections = [
  {
    id: "det-1", status: "OUT_FOR_DELIVERY", confidence: "MEDIUM",
    source: "Shopee notification", summary: "Your parcel SPX71200843 is out for delivery today",
    orderId: "ord-3", detectedAt: d(0),
  },
  {
    id: "det-2", status: "DELIVERED", confidence: "HIGH",
    source: "J&T Express notification", summary: "JT824011592PH delivered, signed by receiver",
    orderId: "ord-1", detectedAt: d(0),
  },
  {
    id: "det-3", status: "IN_TRANSIT", confidence: "LOW",
    source: "Messenger rider chat", summary: "padala niyo po on the way na sir, ETA biyernes",
    orderId: null, ambiguous: true, detectedAt: d(-1),
  },
];

/* ---------- state ---------- */
const state = {
  screen: "home",
  detailId: "ord-1",
  filter: "all",
  query: "",
  sort: "newest",
  theme: "light",
};
let draft = null;

/* ---------- DOM ---------- */
const $ = s => document.querySelector(s);
const device = $("#device"), appbar = $("#appbar"), toastEl = $("#toast");
let toastTimer;
function toast(msg) {
  toastEl.textContent = msg;
  toastEl.classList.add("show");
  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => toastEl.classList.remove("show"), 2200);
}

/* ---------- routing ---------- */
function showScreen(name, opts = {}) {
  state.screen = name;
  document.querySelectorAll(".screen").forEach(s => s.classList.toggle("is-active", s.dataset.screen === name));
  document.querySelectorAll(".nav-item").forEach(n => n.classList.toggle("is-active", n.dataset.nav === name));
  document.querySelectorAll(".rail-link").forEach(n => n.classList.toggle("is-active", n.dataset.goto === name));
  renderAppbar();
  if (name === "home") renderHome();
  if (name === "orders") renderOrders();
  if (name === "detail") renderDetail();
  if (name === "add" && (opts.fresh || !draft)) startWizard();
  if (name === "calendar") renderCalendar();
  if (name === "detections") renderDetections();
  if (name === "import") renderImport();
  if (name === "analytics") renderAnalytics();
}

function renderAppbar() {
  const logo = `<span class="appbar-logo">
    <svg viewBox="0 0 512 512" aria-hidden="true"><rect width="512" height="512" rx="120" fill="url(#lg)"/><circle cx="256" cy="256" r="132" fill="none" stroke="#fff" stroke-width="42"/><path d="M194 238 L256 202 L318 238 L318 310 L256 346 L194 310 Z" fill="none" stroke="#fff" stroke-width="24" stroke-linejoin="round"/><path d="M224 273 L250 299 L300 247" fill="none" stroke="#35D0A0" stroke-width="22" stroke-linecap="round" stroke-linejoin="round"/></svg>
    <span>Ordra</span></span>`;
  const back = `<button class="icon-btn" data-back type="button" aria-label="Back"><span class="mi">arrow_back</span></button>`;
  switch (state.screen) {
    case "home":
      appbar.innerHTML = `${logo}
        <button class="icon-btn" type="button" aria-label="Smart import" data-goto="import"><span class="mi">photo_camera</span></button>
        <button class="icon-btn" type="button" aria-label="Delivery calendar" data-goto="calendar"><span class="mi">calendar_month</span></button>
        <button class="icon-btn" type="button" aria-label="Detected updates" data-goto="detections"><span class="mi">notifications</span><span class="badge-dot" ${detections.length ? "" : "hidden"}></span></button>`;
      break;
    case "orders":
      appbar.innerHTML = `<span class="appbar-title">Orders</span>
        <button class="icon-btn" type="button" aria-label="Detected updates" data-goto="detections"><span class="mi">notifications</span></button>`;
      break;
    case "calendar":
      appbar.innerHTML = `${back}<span class="appbar-title">Delivery calendar</span>`;
      break;
    case "detections":
      appbar.innerHTML = `${back}<span class="appbar-title">Detected updates</span>`;
      break;
    case "import":
      appbar.innerHTML = `${back}<span class="appbar-title">Smart import</span>`;
      break;
    case "analytics":
      appbar.innerHTML = `${back}<span class="appbar-title">Spending analytics</span>`;
      break;
    case "detail": {
      const o = orders.find(x => x.id === state.detailId);
      appbar.innerHTML = `${back}<span class="appbar-title">Order details</span>
        <button class="icon-btn" type="button" aria-label="Edit order" onclick="toast('Edit reuses the New order flow')"><span class="mi">edit</span></button>
        <button class="icon-btn" type="button" aria-label="${o?.archived ? "Restore" : "Archive"} order" onclick="toggleArchive('${o?.id}')"><span class="mi">${o?.archived ? "unarchive" : "archive"}</span></button>
        <button class="icon-btn" type="button" aria-label="Delete order" onclick="askDelete()"><span class="mi">delete</span></button>`;
      break;
    }
    case "add":
      appbar.innerHTML = `${back}<span class="appbar-title">New order</span>`;
      break;
    case "settings":
      appbar.innerHTML = `<span class="appbar-title">Settings</span>`;
      break;
  }
}

/* ---------- shared bits ---------- */
const platOf = o => PLATFORMS[o.platform] || PLATFORMS.OTHER;
const statusChip = s => {
  const st = STATUSES[s];
  return `<span class="status-chip" style="color:${st.color};background:${st.color}1f">${st.label}</span>`;
};
const codChip = () => `<span class="cod-chip"><span class="mi">payments</span>COD</span>`;

function orderCard(o) {
  const p = platOf(o);
  const eta = o.status === "DELIVERED" ? `Delivered ${fmtDate(o.deliveredAt)}`
    : TERMINAL.includes(o.status) ? STATUSES[o.status].label
    : o.etaEnd ? `ETA ${fmtDate(o.etaStart)} – ${fmtDate(o.etaEnd)}` : "No ETA yet";
  return `<button class="order-card" data-open="${o.id}" type="button">
    <span class="plat-badge" style="background:${p.color}">${p.initial}</span>
    <span class="order-main">
      <span class="order-title">${esc(itemsSummary(o))}</span>
      <span class="order-sub">${esc(o.sellerName)} · ${esc(o.customPlatformName || p.label)} · ${eta}</span>
      <span style="display:flex;gap:6px;margin-top:7px">${statusChip(o.status)}${o.isCod && o.paymentStatus !== "PAID" ? codChip() : ""}</span>
    </span>
    <span class="order-side"><span class="order-total">${peso(orderTotal(o))}</span><span class="mi" style="font-size:17px;color:var(--text-2)">chevron_right</span></span>
  </button>`;
}

const isActive = o => !o.archived && !TERMINAL.includes(o.status);
const isPendingCod = o => o.isCod && o.paymentStatus !== "PAID" && o.status !== "CANCELLED";

/* ---------- ordi companion (mirrors core/ui/ai/Companion.kt) ---------- */
function ordiSvg(state = "idle") {
  const faces = {
    open: `<circle cx="38" cy="56" r="6.4" fill="#F5F5FA"/><circle cx="62" cy="56" r="6.4" fill="#F5F5FA"/><circle cx="38" cy="56" r="3" fill="#14164D"/><circle cx="62" cy="56" r="3" fill="#14164D"/>`,
    happy: `<path d="M32.6 54.6 A5.4 4.6 0 0 1 43.4 54.6" stroke="#F5F5FA" stroke-width="2.2" fill="none" stroke-linecap="round"/><path d="M56.6 54.6 A5.4 4.6 0 0 1 67.4 54.6" stroke="#F5F5FA" stroke-width="2.2" fill="none" stroke-linecap="round"/>`,
    dot: `<circle cx="38" cy="56" r="3.6" fill="#F5F5FA"/><circle cx="62" cy="56" r="3.6" fill="#F5F5FA"/>`,
    sleep: `<path d="M32.6 54.6 A5.4 4.6 0 0 0 43.4 54.6" stroke="#F5F5FA" stroke-width="2.2" fill="none" stroke-linecap="round"/><path d="M56.6 54.6 A5.4 4.6 0 0 0 67.4 54.6" stroke="#F5F5FA" stroke-width="2.2" fill="none" stroke-linecap="round"/>`,
  };
  const mouths = {
    smile: `<path d="M45 73.5 A5 3.5 0 0 0 55 73.5" stroke="#F5F5FA" stroke-width="2" fill="none" stroke-linecap="round"/>`,
    big: `<path d="M43 72.5 A7 5 0 0 0 57 72.5" stroke="#F5F5FA" stroke-width="2.2" fill="none" stroke-linecap="round"/>`,
    flat: `<line x1="46" y1="74" x2="54" y2="74" stroke="#F5F5FA" stroke-width="2" stroke-linecap="round"/>`,
    none: ``,
  };
  const look = {
    idle: [faces.open, mouths.smile], delivery: [faces.open, mouths.smile],
    happy: [faces.happy, mouths.big], success: [faces.happy, mouths.big],
    attention: [faces.dot, mouths.flat], warning: [faces.dot, mouths.flat],
    empty: [faces.open, mouths.smile], offline: [faces.sleep, mouths.none],
  }[state] || [faces.open, mouths.smile];
  const badge = state === "attention" || state === "warning"
    ? `<circle cx="24" cy="18" r="8" fill="#FFB547"/><line x1="24" y1="14" x2="24" y2="19.6" stroke="#fff" stroke-width="2.4" stroke-linecap="round"/><circle cx="24" cy="22.6" r="1.4" fill="#fff"/>` : "";
  const parcel = state === "delivery"
    ? `<rect x="38" y="80" width="24" height="16" rx="4" fill="#35D0A0"/><line x1="50" y1="80" x2="50" y2="96" stroke="#fff" stroke-width="2"/>` : "";
  const check = state === "success"
    ? `<circle cx="78" cy="18" r="8" fill="#35D0A0"/><path d="M74 18 L77 21.4 L82.6 14.6" stroke="#fff" stroke-width="2.2" fill="none" stroke-linecap="round" stroke-linejoin="round"/>` : "";
  return `<svg class="ordi" viewBox="0 0 100 100" role="img" aria-label="Ordi, your ORDRA AI companion">
    <rect x="12" y="22" width="76" height="72" rx="26" fill="url(#lg)"/>
    <line x1="16" y1="40" x2="84" y2="40" stroke="rgba(255,255,255,.25)" stroke-width="1.8" stroke-linecap="round"/>
    <line x1="50" y1="22" x2="50" y2="40" stroke="rgba(255,255,255,.25)" stroke-width="3.2" stroke-linecap="round"/>
    <line x1="70" y1="22" x2="74" y2="12" stroke="#7C5CFC" stroke-width="2.4" stroke-linecap="round"/>
    <circle cx="75" cy="9" r="3.4" fill="${state === "offline" ? "none" : "#35D0A0"}" ${state === "offline" ? `stroke="#14164D" stroke-width="1.6"` : ""}/>
    ${look[0]}${look[1]}${badge}${parcel}${check}
  </svg>`;
}

/* ---------- home ---------- */
function renderHome() {
  const h = new Date().getHours();
  $("#greeting").textContent = h < 12 ? "Good morning" : h < 18 ? "Good afternoon" : "Good evening";

  const active = orders.filter(isActive);
  const soon = active.filter(o => o.etaEnd && o.etaEnd <= d(3)).sort((a, b) => a.etaEnd.localeCompare(b.etaEnd));
  const codDue = orders.filter(isPendingCod).reduce((s, o) => s + orderTotal(o), 0);

  // AI banner — same priority resolver as HomeViewModel (attention > today > delivered > active > empty).
  const today = d(0);
  const attention = active.filter(o => o.etaEnd && o.etaEnd < today).length;
  const arrivingToday = active.filter(o => (o.etaStart || o.etaEnd) === today).length;
  const deliveredRecent = active.filter(o => o.status === "DELIVERED" && o.deliveredAt && o.deliveredAt >= d(-2)).length;
  const banner = attention > 0 ? { kind: "attention", n: attention, extra: arrivingToday, state: "attention" }
    : arrivingToday > 0 ? { kind: "arriving", n: arrivingToday, state: "delivery" }
    : deliveredRecent > 0 ? { kind: "delivered", n: deliveredRecent, state: "success" }
    : active.length > 0 ? { kind: "active", n: active.length, state: "idle" }
    : { kind: "empty", n: 0, state: "empty" };
  const msgs = {
    attention: n => `${n} parcel${n === 1 ? "" : "s"} need${n === 1 ? "s" : ""} attention`,
    arriving: n => `${n} parcel${n === 1 ? " is" : "s are"} expected today`,
    delivered: n => n === 1 ? "A parcel was delivered recently" : `${n} parcels were delivered recently`,
    active: n => `You have ${n} active deliver${n === 1 ? "y" : "ies"}`,
    empty: () => "No active parcels right now.",
  };
  $("#aiBanner").innerHTML = `
    <div class="ai-banner">
      <div class="ai-banner-text">
        <p class="ai-eyebrow"><span class="ai-dot"></span>ORDRA AI</p>
        <p class="ai-greet">${h < 12 ? "Good morning" : h < 18 ? "Good afternoon" : "Good evening"}</p>
        <p class="ai-msg">${msgs[banner.kind](banner.n)}</p>
        ${banner.extra ? `<p class="ai-msg-sub">${msgs.arriving(banner.extra)}</p>` : ""}
        <div class="ai-actions">
          <button class="btn btn-tonal" type="button" data-goto="orders">View parcels</button>
        </div>
      </div>
      ${ordiSvg(banner.state)}
    </div>`;

  $("#statRow").innerHTML = `
    <div class="stat-card"><span class="stat-ico indigo"><span class="mi">package_2</span></span><div class="stat-num">${active.length}</div><div class="stat-label">Active orders</div></div>
    <div class="stat-card"><span class="stat-ico cyan"><span class="mi">local_shipping</span></span><div class="stat-num">${soon.length}</div><div class="stat-label">Arriving soon</div></div>
    <div class="stat-card"><span class="stat-ico amber"><span class="mi">payments</span></span><div class="stat-num money">${peso(codDue)}</div><div class="stat-label">COD to prepare</div></div>`;

  $("#arrivingList").innerHTML = soon.length
    ? soon.slice(0, 3).map(orderCard).join("")
    : `<div class="empty"><span class="mi">event_available</span><p>Nothing arriving in the next 3 days.</p></div>`;

  const recent = [...orders].filter(o => !o.archived).sort((a, b) => b.createdAt.localeCompare(a.createdAt)).slice(0, 3);
  $("#recentList").innerHTML = recent.map(orderCard).join("");
}

/* ---------- orders list ---------- */
function renderOrders() {
  const q = state.query.toLowerCase();
  let list = orders.filter(o => {
    if (state.filter === "archived") return o.archived;
    if (o.archived) return false;
    if (state.filter === "active") return isActive(o);
    if (state.filter === "cod") return isPendingCod(o);
    if (state.filter === "delivered") return o.status === "DELIVERED";
    return true;
  });
  if (q) {
    list = list.filter(o => {
      const hay = [o.sellerName, o.tracking, o.notes, o.customPlatformName, platOf(o).label, ...o.items.map(i => i.name + " " + (i.variant || ""))].join(" ").toLowerCase();
      return hay.includes(q);
    });
  }
  const byTotal = fn => (a, b) => fn(orderTotal(a), orderTotal(b));
  list.sort({
    newest: (a, b) => b.createdAt.localeCompare(a.createdAt),
    oldest: (a, b) => a.createdAt.localeCompare(b.createdAt),
    eta: (a, b) => (a.etaEnd || "9999").localeCompare(b.etaEnd || "9999"),
    "total-desc": byTotal((x, y) => y - x),
    "total-asc": byTotal((x, y) => x - y),
  }[state.sort]);

  $("#listCount").textContent = `${list.length} order${list.length === 1 ? "" : "s"}`;
  $("#ordersList").innerHTML = list.length
    ? list.map(orderCard).join("")
    : `<div class="empty"><span class="mi">search_off</span><p>No orders match${q ? ` “${esc(state.query)}”` : " this filter"}.</p></div>`;
}

/* ---------- detail ---------- */
function renderDetail() {
  const o = orders.find(x => x.id === state.detailId);
  const body = $("#detailBody");
  if (!o) { body.innerHTML = `<div class="empty"><p>Order not found.</p></div>`; return; }
  const p = platOf(o);
  const st = STATUSES[o.status];
  const sub = o.items.reduce((s, i) => s + i.qty * i.priceMinor, 0);

  const timeline = st.step >= 0 ? `
    <div class="card"><h4 class="card-title">Status</h4>
      <ul class="timeline">${TIMELINE_STEPS.map((label, i) => {
        const cls = i < st.step ? "done" : i === st.step ? "now" : "";
        const date = i === st.step ? `<div class="tl-date">${o.status === "DELIVERED" ? fmtDate(o.deliveredAt) : "Current status"}</div>` : "";
        return `<li class="${cls}"><span class="tl-dot"><span class="mi">check</span></span><span><div class="tl-name">${label}</div>${date}</span></li>`;
      }).join("")}</ul>
    </div>` : `
    <div class="banner warn"><span class="mi">${o.status === "CANCELLED" ? "cancel" : o.status === "RETURNED" ? "assignment_return" : o.status === "DRAFT" ? "draft" : "currency_exchange"}</span>
      ${o.status === "RETURNED" ? "Returned — refund pending." : o.status === "CANCELLED" ? "This order was cancelled." : o.status === "REFUNDED" ? "This order was refunded." : "Draft — not saved as an order yet."}
    </div>`;

  body.innerHTML = `
    <div class="detail-hero">
      <span class="plat-badge" style="background:${p.color}">${p.initial}</span>
      <div>
        <h3>${esc(o.sellerName)}</h3>
        <div class="order-sub">${esc(o.customPlatformName || p.label)} · Ordered ${fmtDate(o.orderedAt)}</div>
        <div style="display:flex;gap:6px;margin-top:8px;flex-wrap:wrap">${statusChip(o.status)}${o.archived ? `<span class="status-chip" style="color:var(--text-2);background:var(--surface-var)">Archived</span>` : ""}</div>
      </div>
      <span class="order-side"><span class="order-total" style="font-size:17px">${peso(orderTotal(o))}</span></span>
    </div>

    ${timeline}

    <div class="card"><h4 class="card-title">Items</h4>
      ${o.items.map(i => `<div class="item-row"><span><div class="item-name">${esc(i.name)}</div>${i.variant ? `<div class="item-var">${esc(i.variant)}</div>` : ""}<div class="item-var">Qty ${i.qty} × ${peso(i.priceMinor)}</div></span><span class="item-price">${peso(i.qty * i.priceMinor)}</span></div>`).join("")}
      <hr class="hr">
      <div class="kv-row"><span class="k">Subtotal</span><span class="v">${peso(sub)}</span></div>
      <div class="kv-row"><span class="k">Shipping</span><span class="v">${o.shippingMinor ? peso(o.shippingMinor) : "Free"}</span></div>
      ${o.discountMinor ? `<div class="kv-row"><span class="k">Discount</span><span class="v">−${peso(o.discountMinor)}</span></div>` : ""}
      <div class="kv-row total"><span class="k">Total</span><span class="v">${peso(orderTotal(o))}</span></div>
    </div>

    <div class="card"><h4 class="card-title">Payment</h4>
      <div class="kv-row"><span class="k">Method</span><span class="v">${esc(o.paymentMethod)}</span></div>
      <div class="kv-row"><span class="k">Status</span><span class="v">${o.paymentStatus === "PAID" ? "Paid" : o.paymentStatus === "PARTIAL" ? "Partially paid" : "Unpaid"}</span></div>
      ${o.isCod && o.paymentStatus !== "PAID" ? `<div class="banner warn" style="margin-top:8px"><span class="mi">payments</span>Prepare ${peso(orderTotal(o))} cash for the rider.</div>` : ""}
    </div>

    <div class="card"><h4 class="card-title">Delivery</h4>
      <div class="kv-row"><span class="k">Courier</span><span class="v">${esc(o.courier || "Not set")}</span></div>
      <div class="kv-row"><span class="k">Tracking</span><span class="v">${o.tracking ? `<span class="mono">${esc(o.tracking)}</span> <button class="copy-btn" type="button" aria-label="Copy tracking number" onclick="navigator.clipboard?.writeText('${esc(o.tracking)}');toast('Tracking number copied')"><span class="mi">content_copy</span></button>` : "—"}</span></div>
      <div class="kv-row"><span class="k">Estimated delivery</span><span class="v">${o.etaStart ? `${fmtDate(o.etaStart)} – ${fmtDate(o.etaEnd)}` : "—"}</span></div>
    </div>

    ${o.notes ? `<div class="card"><h4 class="card-title">Notes</h4><p style="font-size:13.5px;color:var(--text-2);margin:0;line-height:1.55">${esc(o.notes)}</p></div>` : ""}

    ${postPurchaseHtml(o)}

    <div class="action-col">
      ${!TERMINAL.includes(o.status) ? `<button class="btn btn-fill" type="button" onclick="markDelivered('${o.id}')"><span class="mi">check_circle</span>Mark as delivered</button>` : ""}
      <button class="btn btn-tonal" type="button" onclick="toggleArchive('${o.id}')"><span class="mi">${o.archived ? "unarchive" : "archive"}</span>${o.archived ? "Restore order" : "Archive order"}</button>
    </div>
    <div class="scroll-pad"></div>`;
}

function markDelivered(id) {
  const o = orders.find(x => x.id === id);
  if (!o) return;
  o.status = "DELIVERED";
  o.deliveredAt = d(0);
  if (o.isCod) o.paymentStatus = "PAID";
  renderDetail(); renderAppbar();
  toast(o.isCod ? "Delivered — COD marked as paid" : "Marked as delivered");
}

function toggleArchive(id) {
  const o = orders.find(x => x.id === id);
  if (!o) return;
  o.archived = !o.archived;
  renderDetail(); renderAppbar();
  toast(o.archived ? "Order archived" : "Order restored");
}

let pendingDelete = null;
function askDelete() {
  pendingDelete = state.detailId;
  $("#dialogOverlay").hidden = false;
}
function confirmDelete() {
  orders = orders.filter(o => o.id !== pendingDelete);
  pendingDelete = null;
  $("#dialogOverlay").hidden = true;
  showScreen("orders");
  toast("Order deleted");
}

/* ---------- add-order wizard ---------- */
const WIZ_STEPS = ["Platform", "Details & items", "Payment", "Delivery", "Review"];

function startWizard() {
  draft = {
    step: 0, platform: null, customPlatformName: "", sellerName: "",
    items: [{ name: "", variant: "", qty: 1, priceMinor: 0 }],
    shippingMinor: 0, discountMinor: 0,
    paymentMethod: "COD", paymentStatus: "UNPAID", isCod: true,
    courier: "", tracking: "", orderedAt: d(0), etaStart: "", etaEnd: "", notes: "",
    reminders: ["dayof"],
  };
  renderWizard();
}

const draftTotal = dr => dr.items.reduce((s, i) => s + i.qty * i.priceMinor, 0) + dr.shippingMinor - dr.discountMinor;

function renderWizard() {
  const dr = draft;
  $("#stepLabel").textContent = `Step ${dr.step + 1} of 5`;
  $("#stepName").textContent = WIZ_STEPS[dr.step];
  $("#progressFill").style.width = `${((dr.step + 1) / 5) * 100}%`;
  $("#wizBack").textContent = dr.step === 0 ? "Cancel" : "Back";
  $("#wizNext").innerHTML = dr.step === 4 ? `<span class="mi">check</span>Save order` : "Continue";

  const body = $("#wizardBody");
  if (dr.step === 0) {
    body.innerHTML = `
      <p class="field-label">Where did you order?</p>
      <div class="plat-grid">${Object.entries(PLATFORMS).map(([k, p]) => `
        <button type="button" class="plat-opt ${dr.platform === k ? "is-on" : ""}" data-plat="${k}">
          <span class="plat-badge" style="background:${p.color}">${p.initial}</span>${p.label}
        </button>`).join("")}
      </div>
      <div class="custom-plat" ${dr.platform === "OTHER" ? "" : "hidden"}>
        <p class="field-label">Store / platform name</p>
        <input class="field" id="wCustomPlat" placeholder="e.g. Carousell, Viber seller" value="${esc(dr.customPlatformName)}">
      </div>`;
  }
  if (dr.step === 1) {
    body.innerHTML = `
      <p class="field-label">Seller name</p>
      <input class="field" id="wSeller" placeholder="e.g. AudioHub PH" value="${esc(dr.sellerName)}">
      <p class="field-label">Items</p>
      ${dr.items.map((it, i) => `
        <div class="wiz-item">
          <div class="wiz-item-head">
            <strong style="font-size:12.5px;color:var(--text-2)">Item ${i + 1}</strong>
            ${dr.items.length > 1 ? `<button class="remove-item" data-rm="${i}" type="button" aria-label="Remove item"><span class="mi">close</span></button>` : ""}
          </div>
          <input class="field" data-item="${i}" data-k="name" placeholder="Item name" value="${esc(it.name)}" style="margin-top:6px">
          <input class="field" data-item="${i}" data-k="variant" placeholder="Variant (size, color) — optional" value="${esc(it.variant)}" style="margin-top:8px">
          <div class="field-row" style="margin-top:8px">
            <input class="field" data-item="${i}" data-k="price" type="number" min="0" step="0.01" placeholder="Unit price ₱" value="${it.priceMinor ? it.priceMinor / 100 : ""}">
            <span class="qty-step"><button type="button" data-qty="${i}" data-d="-1" aria-label="Decrease quantity">−</button><strong>${it.qty}</strong><button type="button" data-qty="${i}" data-d="1" aria-label="Increase quantity">+</button></span>
          </div>
        </div>`).join("")}
      <button class="btn btn-outline" id="wAddItem" type="button" style="width:100%"><span class="mi">add</span>Add item</button>
      <div class="field-row">
        <span><p class="field-label">Shipping ₱</p><input class="field" id="wShip" type="number" min="0" step="0.01" value="${dr.shippingMinor ? dr.shippingMinor / 100 : ""}" placeholder="0"></span>
        <span><p class="field-label">Discount ₱</p><input class="field" id="wDisc" type="number" min="0" step="0.01" value="${dr.discountMinor ? dr.discountMinor / 100 : ""}" placeholder="0"></span>
      </div>
      <div class="kv-row total" style="margin-top:14px"><span class="k">Total</span><span class="v" id="wTotal">${peso(draftTotal(dr))}</span></div>`;
  }
  if (dr.step === 2) {
    body.innerHTML = `
      <p class="field-label">Payment method</p>
      ${PAY_METHODS.map(m => `
        <label class="radio-card"><input type="radio" name="wPay" value="${m}" ${dr.paymentMethod === m ? "checked" : ""}>
        <span>${m}${m === "COD" ? "<small>Cash on delivery — prepare exact amount</small>" : ""}</span></label>`).join("")}
      <p class="field-label">Payment status</p>
      <div class="segmented" id="wPayStatus">
        ${PAY_STATUSES.map(s => `<button class="seg ${dr.paymentStatus === s ? "is-on" : ""}" data-ps="${s}" type="button">${s === "UNPAID" ? "Unpaid" : s === "PAID" ? "Paid" : "Partial"}</button>`).join("")}
      </div>
      ${dr.isCod ? `<div class="banner warn" style="margin-top:14px"><span class="mi">payments</span>COD amount due on delivery: <strong>&nbsp;${peso(draftTotal(dr))}</strong></div>` : ""}`;
  }
  if (dr.step === 3) {
    body.innerHTML = `
      <p class="field-label">Courier</p>
      <select class="field" id="wCourier"><option value="">Not set</option>${COURIERS.map(c => `<option ${dr.courier === c ? "selected" : ""}>${c}</option>`).join("")}</select>
      <p class="field-label">Tracking number</p>
      <input class="field" id="wTracking" placeholder="e.g. JT824011592PH" value="${esc(dr.tracking)}">
      <p class="field-label">Order date</p>
      <input class="field" id="wOrdered" type="date" value="${dr.orderedAt}">
      <div class="field-row">
        <span><p class="field-label">ETA from</p><input class="field" id="wEtaA" type="date" value="${dr.etaStart}"></span>
        <span><p class="field-label">ETA to</p><input class="field" id="wEtaB" type="date" value="${dr.etaEnd}"></span>
      </div>
      <p class="field-label">Notes — optional</p>
      <input class="field" id="wNotes" placeholder="e.g. Rider calls first" value="${esc(dr.notes)}">
      <p class="field-label">Reminders</p>
      <div class="chip-row" style="padding-top:0" id="wRemindChips">
        ${[["dayof", "Delivery day"], ["minus1", "1 day before"], ["minus2", "2 days before"]].map(([k, l]) =>
          `<button class="chip ${dr.reminders?.includes(k) ? "is-on" : ""}" data-rem="${k}" type="button">${l}</button>`).join("")}
      </div>`;
  }
  if (dr.step === 4) {
    const p = dr.platform ? platOf({ platform: dr.platform }) : null;
    body.innerHTML = `
      <div class="card" style="margin-top:6px"><h4 class="card-title">Review order</h4>
        <div class="kv-row"><span class="k">Platform</span><span class="v">${p ? esc(dr.customPlatformName || p.label) : "—"}</span></div>
        <div class="kv-row"><span class="k">Seller</span><span class="v">${esc(dr.sellerName) || "—"}</span></div>
        <hr class="hr">
        ${dr.items.map(i => `<div class="item-row"><span><div class="item-name">${esc(i.name)}</div>${i.variant ? `<div class="item-var">${esc(i.variant)}</div>` : ""}</span><span class="item-price">${i.qty} × ${peso(i.priceMinor)}</span></div>`).join("")}
        <hr class="hr">
        <div class="kv-row"><span class="k">Shipping</span><span class="v">${peso(dr.shippingMinor)}</span></div>
        <div class="kv-row"><span class="k">Discount</span><span class="v">−${peso(dr.discountMinor)}</span></div>
        <div class="kv-row total"><span class="k">Total</span><span class="v">${peso(draftTotal(dr))}</span></div>
        <hr class="hr">
        <div class="kv-row"><span class="k">Payment</span><span class="v">${dr.paymentMethod} · ${dr.paymentStatus === "PAID" ? "Paid" : dr.paymentStatus === "PARTIAL" ? "Partial" : "Unpaid"}</span></div>
        <div class="kv-row"><span class="k">Courier</span><span class="v">${esc(dr.courier) || "Not set"}</span></div>
        <div class="kv-row"><span class="k">ETA</span><span class="v">${dr.etaStart ? `${fmtDate(dr.etaStart)} – ${fmtDate(dr.etaEnd)}` : "—"}</span></div>
      </div>
      <div class="banner info"><span class="mi">info</span>Saved locally on this device — works offline.</div>`;
  }
  wireWizardInputs();
  $("#wizardBody").scrollTop = 0;
}

function collectDraft() {
  const dr = draft, gv = id => document.getElementById(id);
  if (dr.step === 0) dr.customPlatformName = gv("wCustomPlat")?.value.trim() || dr.customPlatformName;
  if (dr.step === 1) {
    dr.sellerName = gv("wSeller").value.trim();
    dr.items.forEach((it, i) => {
      it.name = document.querySelector(`[data-item="${i}"][data-k="name"]`).value.trim();
      it.variant = document.querySelector(`[data-item="${i}"][data-k="variant"]`).value.trim();
      it.priceMinor = Math.round((parseFloat(document.querySelector(`[data-item="${i}"][data-k="price"]`).value) || 0) * 100);
    });
    dr.shippingMinor = Math.round((parseFloat(gv("wShip").value) || 0) * 100);
    dr.discountMinor = Math.round((parseFloat(gv("wDisc").value) || 0) * 100);
  }
  if (dr.step === 2) {
    dr.paymentMethod = document.querySelector('input[name="wPay"]:checked').value;
    dr.isCod = dr.paymentMethod === "COD";
  }
  if (dr.step === 3) {
    dr.courier = gv("wCourier").value; dr.tracking = gv("wTracking").value.trim();
    dr.orderedAt = gv("wOrdered").value || d(0);
    dr.etaStart = gv("wEtaA").value; dr.etaEnd = gv("wEtaB").value;
    dr.notes = gv("wNotes").value.trim();
  }
}

function wireWizardInputs() {
  const dr = draft;
  document.querySelectorAll("[data-plat]").forEach(b => b.onclick = () => {
    collectDraft(); dr.platform = b.dataset.plat; renderWizard();
  });
  document.querySelectorAll("[data-qty]").forEach(b => b.onclick = () => {
    collectDraft();
    const it = dr.items[+b.dataset.qty];
    it.qty = Math.max(1, it.qty + (+b.dataset.d));
    renderWizard();
  });
  document.querySelectorAll("[data-rm]").forEach(b => b.onclick = () => {
    collectDraft(); dr.items.splice(+b.dataset.rm, 1); renderWizard();
  });
  const add = $("#wAddItem");
  if (add) add.onclick = () => { collectDraft(); dr.items.push({ name: "", variant: "", qty: 1, priceMinor: 0 }); renderWizard(); };
  document.querySelectorAll("#wPayStatus .seg").forEach(b => b.onclick = () => {
    dr.paymentStatus = b.dataset.ps;
    document.querySelectorAll("#wPayStatus .seg").forEach(x => x.classList.toggle("is-on", x === b));
  });
  document.querySelectorAll('input[name="wPay"]').forEach(r => r.onchange = () => {
    dr.paymentMethod = r.value; dr.isCod = r.value === "COD"; collectDraft(); renderWizard();
  });
  document.querySelectorAll("[data-rem]").forEach(b => b.onclick = () => {
    dr.reminders = dr.reminders || [];
    dr.reminders = dr.reminders.includes(b.dataset.rem)
      ? dr.reminders.filter(x => x !== b.dataset.rem)
      : [...dr.reminders, b.dataset.rem];
    b.classList.toggle("is-on");
  });
  ["wSeller", "wShip", "wDisc"].forEach(id => {
    const el = document.getElementById(id);
    if (el) el.oninput = () => { collectDraft(); const t = $("#wTotal"); if (t) t.textContent = peso(draftTotal(dr)); };
  });
}

function wizNext() {
  collectDraft();
  const dr = draft;
  if (dr.step === 0 && !dr.platform) return toast("Pick a platform first");
  if (dr.platform === "OTHER" && !dr.customPlatformName) return toast("Name the store or platform");
  if (dr.step === 1) {
    if (!dr.items[0].name) return toast("Name at least the first item");
    if (draftTotal(dr) <= 0) return toast("Add a price — total can't be zero");
  }
  if (dr.step === 4) {
    const o = {
      id: `ord-${nextId++}`, platform: dr.platform, customPlatformName: dr.customPlatformName,
      sellerName: dr.sellerName || platOf({ platform: dr.platform }).label,
      isCod: dr.isCod, paymentMethod: dr.paymentMethod, paymentStatus: dr.paymentStatus,
      status: "ORDERED", courier: dr.courier, tracking: dr.tracking,
      orderedAt: dr.orderedAt, etaStart: dr.etaStart, etaEnd: dr.etaEnd, deliveredAt: null,
      items: dr.items.filter(i => i.name), shippingMinor: dr.shippingMinor, discountMinor: dr.discountMinor,
      notes: dr.notes, archived: false, createdAt: d(0),
    };
    orders.unshift(o);
    draft = null;
    state.detailId = o.id;
    showScreen("detail");
    return toast("Order saved");
  }
  dr.step++;
  renderWizard();
}

function wizBack() {
  if (draft.step === 0) { draft = null; showScreen("home"); return; }
  draft.step--;
  renderWizard();
}

/* ---------- delivery calendar (Phase 2) ---------- */
let calCursor = (() => { const t = new Date(); return `${t.getFullYear()}-${String(t.getMonth() + 1).padStart(2, "0")}`; })();
let calSel = d(0);

function renderCalendar() {
  const [Y, M] = calCursor.split("-").map(Number);
  $("#calTitle").textContent = new Date(Y, M - 1, 1).toLocaleDateString("en-PH", { month: "long", year: "numeric" });
  const first = new Date(Y, M - 1, 1).getDay();
  const daysIn = new Date(Y, M, 0).getDate();
  const byDay = {};
  orders.filter(o => !o.archived).forEach(o => {
    [o.etaStart, o.etaEnd, o.deliveredAt].filter(Boolean).forEach(dt => {
      (byDay[dt] = byDay[dt] || []).push(o);
    });
  });
  const dows = ["Su", "Mo", "Tu", "We", "Th", "Fr", "Sa"];
  let html = dows.map(x => `<div class="cal-dow">${x}</div>`).join("");
  for (let i = 0; i < first; i++) html += `<div class="cal-day is-out"></div>`;
  for (let day = 1; day <= daysIn; day++) {
    const iso = `${calCursor}-${String(day).padStart(2, "0")}`;
    const its = byDay[iso] || [];
    const cod = its.filter(isPendingCod).reduce((s, o) => s + orderTotal(o), 0);
    html += `<button class="cal-day ${iso === d(0) ? "is-today" : ""} ${iso === calSel ? "is-sel" : ""}" data-cal="${iso}" type="button">
      <span class="n">${day}</span>
      <span class="cal-dots">${its.slice(0, 3).map(o => `<span class="cal-dot ${isPendingCod(o) ? "cod" : ""}"></span>`).join("")}</span>
      ${cod ? `<span class="cal-amt">${peso(cod)}</span>` : ""}
    </button>`;
  }
  $("#calGrid").innerHTML = html;
  const its = byDay[calSel] || [];
  $("#calDayTitle").textContent = its.length ? `${its.length} on ${fmtDate(calSel)}` : `Nothing on ${fmtDate(calSel)}`;
  $("#calDayList").innerHTML = its.length
    ? its.map(orderCard).join("")
    : `<div class="empty"><span class="mi">event_available</span><p>No parcels due this day.</p></div>`;
}

/* ---------- detected updates (Phase 5) ---------- */
function renderDetections() {
  const body = $("#detectionsBody");
  body.innerHTML = `
    <div class="banner info" style="margin-top:10px"><span class="mi">verified_user</span>
      Detected from notifications on this device. Confirm before a status changes — nothing is applied silently.
    </div>
    ${detections.length ? `<div class="stack" style="margin-top:12px">${detections.map(det => {
      const o = orders.find(x => x.id === det.orderId);
      return `<div class="card">
        <div style="display:flex;justify-content:space-between;align-items:center;gap:8px">
          <strong style="font-size:14px">${esc(STATUSES[det.status]?.label || det.status)}</strong>
          <span class="conf-chip conf-${det.confidence.toLowerCase()}">${det.confidence}</span>
        </div>
        <p style="font-size:12.5px;color:var(--text-2);margin:6px 0 0;line-height:1.5">${esc(det.summary)}</p>
        <p class="det-src">${esc(det.source)} · ${o ? "matched: " + esc(itemsSummary(o)) : "ambiguous — pick a parcel"} · ${fmtDate(det.detectedAt)}</p>
        <div class="dialog-actions" style="margin-top:10px">
          <button class="btn btn-text" type="button" onclick="dismissDet('${det.id}')">Ignore</button>
          ${o
            ? `<button class="btn btn-tonal" type="button" onclick="applyDet('${det.id}')">Apply</button>`
            : `<button class="btn btn-tonal" type="button" onclick="pickDet('${det.id}')">Pick parcel</button>`}
        </div>
      </div>`;
    }).join("")}</div>`
    : `<div class="empty"><span class="mi">notifications_off</span><p>No detected parcel updates right now.</p></div>`}
    <div class="scroll-pad"></div>`;
}

function applyDet(id) {
  const det = detections.find(x => x.id === id);
  const o = det && orders.find(x => x.id === det.orderId);
  if (!o) return;
  o.status = det.status;
  if (det.status === "DELIVERED") { o.deliveredAt = d(0); if (o.isCod) o.paymentStatus = "PAID"; }
  detections = detections.filter(x => x.id !== id);
  renderDetections(); renderAppbar();
  toast(`${esc(STATUSES[det.status].label)} applied to ${itemsSummary(o)}`);
}
function dismissDet(id) {
  detections = detections.filter(x => x.id !== id);
  renderDetections(); renderAppbar();
  toast("Suggestion ignored");
}
function pickDet(id) {
  const active = orders.filter(o => !o.archived && !TERMINAL.includes(o.status));
  const det = detections.find(x => x.id === id);
  if (!det) return;
  $("#detectionsBody").innerHTML = `
    <div class="card" style="margin-top:10px"><h4 class="card-title">Which parcel is this for?</h4>
      <p class="card-sub">${esc(det.summary)}</p>
      <div class="stack">${active.map(o => `
        <button class="order-card" type="button" onclick="assignDet('${id}','${o.id}')">
          <span class="plat-badge" style="background:${platOf(o).color}">${platOf(o).initial}</span>
          <span class="order-main"><span class="order-title">${esc(itemsSummary(o))}</span>
          <span class="order-sub">${esc(o.sellerName)} · ${esc(o.courier || "No courier")}</span></span>
        </button>`).join("")}
      </div>
      <div class="dialog-actions"><button class="btn btn-text" type="button" onclick="renderDetections()">Cancel</button></div>
    </div>`;
}
function assignDet(detId, orderId) {
  const det = detections.find(x => x.id === detId);
  if (det) { det.orderId = orderId; det.ambiguous = false; }
  applyDet(detId);
}

/* ---------- smart import (Phases 3–4) ---------- */
let importStep = 0;
let importDraft = null;

function renderImport() {
  const body = $("#importBody");
  if (importStep === 0) {
    body.innerHTML = `
      <div class="banner info" style="margin-top:10px"><span class="mi">photo_scan</span>
        Import a marketplace screenshot — on-device OCR extracts the details for your review. Nothing saves until you confirm.
      </div>
      <label class="import-photo" style="margin-top:14px">
        <input type="file" accept="image/*" id="importFile">
        <span class="mi">add_photo_alternate</span>
        <strong>Pick a screenshot</strong><br><small>Shopee, TikTok Shop, Lazada or any order page</small>
      </label>
      <button class="btn btn-tonal" type="button" style="width:100%;margin-top:12px" onclick="simulateImport()">
        <span class="mi">auto_awesome</span>Try with sample screenshot data
      </button>
      <div class="scroll-pad"></div>`;
    $("#importFile").onchange = () => simulateImport();
    return;
  }
  const f = importDraft;
  body.innerHTML = `
    <div class="banner warn" style="margin-top:10px"><span class="mi">fact_check</span>
      Review before saving — every extracted field is a suggestion you can correct.
    </div>
    <div class="card" style="margin-top:12px"><h4 class="card-title">Extracted fields</h4>
      <div class="conf-row">${Object.entries(f.conf).map(([k, v]) => `<span class="conf-chip conf-${v}">${k} · ${v}</span>`).join("")}</div>
      <p class="field-label">Product name</p><input class="field" id="iName" value="${esc(f.name)}">
      <div class="field-row">
        <span><p class="field-label">Seller</p><input class="field" id="iSeller" value="${esc(f.seller)}"></span>
        <span><p class="field-label">Total ₱</p><input class="field" id="iTotal" type="number" step="0.01" value="${(f.totalMinor / 100).toFixed(2)}"></span>
      </div>
      <div class="field-row">
        <span><p class="field-label">Platform</p>
          <select class="field" id="iPlat">${Object.keys(PLATFORMS).map(k => `<option value="${k}" ${f.platform === k ? "selected" : ""}>${PLATFORMS[k].label}</option>`).join("")}</select></span>
        <span><p class="field-label">Courier</p>
          <select class="field" id="iCourier">${["", ...COURIERS].map(c => `<option ${f.courier === c ? "selected" : ""}>${c || "Not set"}</option>`).join("")}</select></span>
      </div>
      <p class="field-label">Tracking</p><input class="field" id="iTracking" value="${esc(f.tracking)}">
    </div>
    <div class="action-col">
      <button class="btn btn-fill" type="button" onclick="saveImport()"><span class="mi">check</span>Save order</button>
      <button class="btn btn-text" type="button" onclick="importStep=0;renderImport()">Discard</button>
    </div>
    <div class="scroll-pad"></div>`;
}

function simulateImport() {
  importDraft = {
    name: "Soundcore R50i earbuds", seller: "AudioHub PH", totalMinor: 129900,
    platform: "SHOPEE", courier: "J&T Express", tracking: "JT824011593PH",
    conf: { product: "high", seller: "high", amount: "medium", platform: "high", tracking: "low" },
  };
  importStep = 1;
  toast("OCR extracted 5 fields");
  renderImport();
}

function saveImport() {
  const o = {
    id: `ord-${nextId++}`,
    platform: $("#iPlat").value, sellerName: $("#iSeller").value || "Imported order",
    isCod: true, paymentMethod: "COD", paymentStatus: "UNPAID", status: "ORDERED",
    courier: $("#iCourier").value === "Not set" ? "" : $("#iCourier").value,
    tracking: $("#iTracking").value.trim(),
    orderedAt: d(0), etaStart: "", etaEnd: "", deliveredAt: null,
    items: [{ name: $("#iName").value || "Imported item", variant: "", qty: 1,
      priceMinor: Math.round((parseFloat($("#iTotal").value) || 0) * 100) }],
    shippingMinor: 0, discountMinor: 0, notes: "Imported from screenshot", archived: false, createdAt: d(0),
  };
  orders.unshift(o);
  importStep = 0; importDraft = null;
  state.detailId = o.id;
  showScreen("detail");
  toast("Order imported — review complete");
}

/* ---------- analytics (Phase 6) ---------- */
function renderAnalytics() {
  const spendable = orders.filter(o => !["CANCELLED", "REFUNDED"].includes(o.status));
  const spend = o => orderTotal(o);
  const delivered = spendable.filter(o => o.status === "DELIVERED").reduce((s, o) => s + spend(o), 0);
  const pending = spendable.filter(o => !TERMINAL.includes(o.status)).reduce((s, o) => s + spend(o), 0);
  const total = spendable.reduce((s, o) => s + spend(o), 0);
  const avg = spendable.length ? Math.round(total / spendable.length) : 0;
  const cod = spendable.filter(isPendingCod).reduce((s, o) => s + spend(o), 0);
  const byPlat = {};
  spendable.forEach(o => { byPlat[o.platform] = (byPlat[o.platform] || 0) + spend(o); });
  const platRows = Object.entries(byPlat).sort((a, b) => b[1] - a[1]);
  const maxPlat = platRows[0]?.[1] || 1;
  const byMonth = {};
  spendable.filter(o => o.orderedAt).forEach(o => {
    const m = o.orderedAt.slice(0, 7);
    byMonth[m] = (byMonth[m] || 0) + spend(o);
  });
  const months = Object.entries(byMonth).sort((a, b) => b[0].localeCompare(a[0])).slice(0, 6);
  const maxM = months[0]?.[1] || 1;

  $("#analyticsBody").innerHTML = `
    <div class="stat-row" style="grid-template-columns:1fr 1fr;margin-top:10px">
      <div class="stat-card"><div class="stat-num money">${peso(total)}</div><div class="stat-label">Total spent</div></div>
      <div class="stat-card"><div class="stat-num money">${peso(avg)}</div><div class="stat-label">Avg order</div></div>
      <div class="stat-card"><div class="stat-num money">${peso(pending)}</div><div class="stat-label">Pending spend</div></div>
      <div class="stat-card"><div class="stat-num money">${peso(cod)}</div><div class="stat-label">COD outstanding</div></div>
    </div>
    <div class="card" style="margin-top:14px"><h4 class="card-title">By platform</h4>
      ${platRows.map(([k, v]) => `<div class="bar-row">
        <div class="bar-meta"><span>${PLATFORMS[k].label}</span><span>${peso(v)}</span></div>
        <div class="bar-track"><div class="bar-fill" style="width:${Math.round(v / maxPlat * 100)}%"></div></div>
      </div>`).join("")}
    </div>
    <div class="card" style="margin-top:14px"><h4 class="card-title">Monthly history</h4>
      ${months.map(([m, v]) => {
        const label = new Date(m + "-01T00:00:00").toLocaleDateString("en-PH", { month: "long", year: "numeric" });
        return `<div class="bar-row">
          <div class="bar-meta"><span>${label}</span><span>${peso(v)}</span></div>
          <div class="bar-track"><div class="bar-fill" style="width:${Math.round(v / maxM * 100)}%"></div></div>
        </div>`;
      }).join("") || `<p class="card-sub">No spending data yet.</p>`}
    </div>
    <div class="banner info" style="margin-top:14px"><span class="mi">info</span>Computed locally — cancelled and refunded orders are excluded.</div>
    <div class="scroll-pad"></div>`;
}

/* ---------- post-purchase (Phase 7) ---------- */
function postPurchaseHtml(o) {
  const stars = `<div class="rate-stars">${[1, 2, 3, 4, 5].map(i =>
    `<button type="button" onclick="setRating('${o.id}',${i})" aria-label="${i} star${i > 1 ? "s" : ""}">${(o.rating || 0) >= i ? "★" : "☆"}</button>`).join("")}
    <button class="chip ${o.isFavorite ? "is-on" : ""}" style="margin-left:auto" type="button" onclick="toggleFav('${o.id}')">Favorite</button>
  </div>`;
  const lines = [
    ...(o.returns || []).map(r => `<div class="kv-row"><span class="k">Return — ${r.reason || "no reason"}</span><span class="v">${r.status.toLowerCase().replace("_", " ")}</span></div>`),
    ...(o.refunds || []).map(r => `<div class="kv-row"><span class="k">Refund ${r.amountMinor ? peso(r.amountMinor) : ""}</span><span class="v">${r.status.toLowerCase()}</span></div>`),
    ...(o.warranties || []).map(w => `<div class="kv-row"><span class="k">Warranty ${w.provider || ""}</span><span class="v">${fmtDate(w.start)} → ${fmtDate(w.end)}</span></div>`),
    ...(o.receipts || []).map(r => `<div class="kv-row"><span class="k">Receipt</span><span class="v">${esc(r.label || "attached")}</span></div>`),
  ].join("");
  return `<div class="card"><h4 class="card-title">Post-purchase</h4>
    ${stars}
    ${lines ? `<hr class="hr">${lines}` : ""}
    <div class="dialog-actions" style="margin-top:10px;justify-content:flex-start;flex-wrap:wrap">
      <button class="btn btn-text" type="button" onclick="ppAdd('${o.id}','return')">Return</button>
      <button class="btn btn-text" type="button" onclick="ppAdd('${o.id}','refund')">Refund</button>
      <button class="btn btn-text" type="button" onclick="ppAdd('${o.id}','warranty')">Warranty</button>
      <button class="btn btn-text" type="button" onclick="ppAdd('${o.id}','receipt')">Receipt</button>
    </div>
  </div>`;
}
function setRating(id, n) { const o = orders.find(x => x.id === id); o.rating = o.rating === n ? null : n; renderDetail(); }
function toggleFav(id) { const o = orders.find(x => x.id === id); o.isFavorite = !o.isFavorite; renderDetail(); toast(o.isFavorite ? "Added to favorites" : "Removed from favorites"); }
function ppAdd(id, kind) {
  const o = orders.find(x => x.id === id);
  const reason = prompt(kind === "warranty" ? "Provider (end = +1 year):" : "Reason (optional):", "") ?? "";
  if (kind === "return") (o.returns = o.returns || []).push({ status: "REQUESTED", reason, at: d(0) });
  if (kind === "refund") (o.refunds = o.refunds || []).push({ status: "PENDING", amountMinor: orderTotal(o), reason });
  if (kind === "warranty") (o.warranties = o.warranties || []).push({ provider: reason || "Seller", start: d(0), end: d(365) });
  if (kind === "receipt") (o.receipts = o.receipts || []).push({ label: "Photo attached" });
  renderDetail();
  toast(`${kind[0].toUpperCase() + kind.slice(1)} saved — warranty end dates create a reminder`);
}

/* ---------- backup / export (Phase 8) ---------- */
function download(name, mime, text) {
  const a = document.createElement("a");
  a.href = URL.createObjectURL(new Blob([text], { type: mime }));
  a.download = name; a.click(); URL.revokeObjectURL(a.href);
}
function exportBackup() {
  download("ordra-backup.json", "application/json", JSON.stringify({
    schemaVersion: 1, exportedAt: Date.now(), orders, detections,
  }, null, 2));
  toast("Backup exported");
}
function exportCsv() {
  const esc2 = v => `"${String(v ?? "").replace(/"/g, '""')}"`;
  const rows = [["id", "title", "platform", "status", "payment_method", "is_cod", "total_minor", "ordered_date"]
    .join(","), ...orders.map(o => [o.id, esc2(itemsSummary(o)), o.platform, o.status, o.paymentMethod, o.isCod ? 1 : 0, orderTotal(o), o.orderedAt].join(","))];
  download("ordra-orders.csv", "text/csv", rows.join("\n"));
  toast("CSV exported");
}
function importBackupFile(file) {
  const r = new FileReader();
  r.onload = () => {
    try {
      const doc = JSON.parse(r.result);
      if (doc.schemaVersion > 1 || !Array.isArray(doc.orders)) throw new Error("bad");
      doc.orders.forEach(no => {
        const i = orders.findIndex(o => o.id === no.id);
        if (i >= 0) orders[i] = no; else orders.push(no);
      });
      toast(`Restored ${doc.orders.length} orders`);
      showScreen("home");
    } catch { toast("That file isn't a valid Ordra backup"); }
  };
  r.readAsText(file);
}

/* ---------- theme ---------- */
function applyTheme(mode) {
  state.theme = mode;
  const dark = mode === "dark" || (mode === "system" && matchMedia("(prefers-color-scheme: dark)").matches);
  device.dataset.theme = dark ? "dark" : "light";
  document.querySelectorAll("#themeSeg .seg").forEach(s => s.classList.toggle("is-on", s.dataset.themeOpt === mode));
  $("#stageThemeLabel").textContent = dark ? "Dark" : "Light";
}

/* ---------- wiring ---------- */
document.addEventListener("click", e => {
  const open = e.target.closest("[data-open]");
  if (open) { state.detailId = open.dataset.open; showScreen("detail"); return; }
  const nav = e.target.closest("[data-nav]");
  if (nav) { showScreen(nav.dataset.nav); return; }
  const go = e.target.closest("[data-goto]");
  if (go) { showScreen(go.dataset.goto); return; }
  if (e.target.closest("[data-back]")) {
    if (state.screen === "add") { wizBack(); return; }
    showScreen(state.detailId && orders.some(o => o.id === state.detailId) && state.screen === "detail" ? "orders" : "home");
    return;
  }
  const chip = e.target.closest("#filterChips .chip");
  if (chip) {
    state.filter = chip.dataset.filter;
    document.querySelectorAll("#filterChips .chip").forEach(c => c.classList.toggle("is-on", c === chip));
    renderOrders();
  }
});

$("#fabAdd").addEventListener("click", () => showScreen("add", { fresh: true }));
$("#wizNext").addEventListener("click", wizNext);
$("#wizBack").addEventListener("click", wizBack);
$("#searchInput").addEventListener("input", e => { state.query = e.target.value; renderOrders(); });
$("#sortSelect").addEventListener("change", e => { state.sort = e.target.value; renderOrders(); });
$("#dlgCancel").addEventListener("click", () => { $("#dialogOverlay").hidden = true; pendingDelete = null; });
$("#dlgConfirm").addEventListener("click", confirmDelete);
$("#dialogOverlay").addEventListener("click", e => { if (e.target === e.currentTarget) { e.currentTarget.hidden = true; pendingDelete = null; } });
document.addEventListener("keydown", e => { if (e.key === "Escape") { $("#dialogOverlay").hidden = true; pendingDelete = null; } });
$("#calPrev").addEventListener("click", () => {
  const [Y, M] = calCursor.split("-").map(Number);
  const p = new Date(Y, M - 2, 1);
  calCursor = `${p.getFullYear()}-${String(p.getMonth() + 1).padStart(2, "0")}`;
  renderCalendar();
});
$("#calNext").addEventListener("click", () => {
  const [Y, M] = calCursor.split("-").map(Number);
  const p = new Date(Y, M, 1);
  calCursor = `${p.getFullYear()}-${String(p.getMonth() + 1).padStart(2, "0")}`;
  renderCalendar();
});
document.addEventListener("click", e => {
  const cell = e.target.closest("[data-cal]");
  if (cell) { calSel = cell.dataset.cal; renderCalendar(); }
});
$("#backupExport").addEventListener("click", exportBackup);
$("#csvExport").addEventListener("click", exportCsv);
$("#backupImport").addEventListener("click", () => $("#backupFile").click());
$("#backupFile").addEventListener("change", e => { if (e.target.files[0]) importBackupFile(e.target.files[0]); });
$("#aiToggle").addEventListener("change", e =>
  toast(e.target.checked ? "Online AI analysis on — Groq assists extraction" : "Back to local-only OCR"));
$("#stageThemeBtn").addEventListener("click", () => applyTheme(device.dataset.theme === "dark" ? "light" : "dark"));
document.querySelectorAll("#themeSeg .seg").forEach(s => s.addEventListener("click", () => applyTheme(s.dataset.themeOpt)));
$("#defPlatform").innerHTML = Object.values(PLATFORMS).map(p => `<option>${p.label}</option>`).join("");
$("#clock").textContent = new Date().toLocaleTimeString("en-PH", { hour: "numeric", minute: "2-digit", hour12: true }).replace(/\s?[AP]M/i, "");

matchMedia("(prefers-color-scheme: dark)").addEventListener("change", () => { if (state.theme === "system") applyTheme("system"); });

applyTheme("light");
showScreen("home");
