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

/* OrderStatus — mirrors domain/model/OrderStatus.kt */
const STATUSES = {
  DRAFT:            { label: "Draft",            color: "#666979", step: -1 },
  ORDERED:          { label: "Ordered",          color: "#5B5FEF", step: 0 },
  PROCESSING:       { label: "Processing",       color: "#7C5CFC", step: 1 },
  PACKED:           { label: "Packed",           color: "#3B82F6", step: 2 },
  SHIPPED:          { label: "Shipped",          color: "#06B6D4", step: 3 },
  DELIVERED:        { label: "Delivered",        color: "#0FA881", step: 4 },
  CANCELLED:        { label: "Cancelled",        color: "#F05252", step: -1 },
  RETURNED:         { label: "Returned",         color: "#F97316", step: -1 },
  REFUNDED:         { label: "Refunded",         color: "#14B8A6", step: -1 },
};
/* ShipmentStatus — mirrors domain/model/ShipmentStatus.kt (OUT_FOR_DELIVERY lives here, not on Order) */
const SHIP_STATUSES = {
  UNKNOWN:          { label: "Unknown",          color: "#666979", step: -1 },
  PREPARING:        { label: "Preparing",        color: "#7C5CFC", step: 0 },
  PICKED_UP:        { label: "Picked up",        color: "#3B82F6", step: 1 },
  IN_TRANSIT:       { label: "In transit",       color: "#06B6D4", step: 2 },
  ARRIVED_AT_HUB:   { label: "Arrived at hub",   color: "#0891B2", step: 3 },
  OUT_FOR_DELIVERY: { label: "Out for delivery", color: "#D98E04", step: 4 },
  DELIVERED:        { label: "Delivered",        color: "#0FA881", step: 5 },
  DELIVERY_FAILED:  { label: "Delivery failed",  color: "#F05252", step: -1 },
  RETURNING:        { label: "Returning",        color: "#F97316", step: -1 },
  RETURNED:         { label: "Returned",         color: "#F97316", step: -1 },
};
const TIMELINE_STEPS = ["Preparing", "Picked up", "In transit", "Arrived at hub", "Out for delivery", "Delivered"];
const TERMINAL = ["DELIVERED", "CANCELLED", "RETURNED", "REFUNDED"];
const PAY_METHODS = ["COD", "GCash", "Maya", "Card", "Bank transfer", "Other"];
const PAY_STATUSES = ["UNPAID", "PAID", "PARTIAL"];
const COURIERS = ["J&T Express", "Ninja Van", "Flash Express", "LBC", "JRS Express", "SPX Express", "Other"];

/* ---------- helpers ---------- */
const d = n => { const t = new Date(); t.setDate(t.getDate() + n); return t.toISOString().slice(0, 10); };
const peso = minor => "₱" + (minor / 100).toLocaleString("en-PH", { maximumFractionDigits: minor % 100 ? 2 : 0 });
const fmtDate = iso => iso ? new Date(iso + "T00:00:00").toLocaleDateString("en-PH", { month: "short", day: "numeric" }) : "—";
const esc = s => String(s ?? "").replace(/[&<>"']/g, c => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[c]));
const orderTotal = o => o.totalMinor ?? (o.items.reduce((s, i) => s + i.qty * i.priceMinor, 0) + o.shippingMinor - o.discountMinor);
const itemsSummary = o => o.title || (o.items.length === 1 ? o.items[0].name : `${o.items[0].name} + ${o.items.length - 1} more`);

/* ---------- sample data ---------- */
let nextId = 10;
let orders = [
  {
    id: "ord-1", platform: "SHOPEE", sellerName: "AudioHub PH", isCod: true,
    paymentMethod: "COD", paymentStatus: "UNPAID", status: "SHIPPED", shipStatus: "IN_TRANSIT", shipStatus: "OUT_FOR_DELIVERY",
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
    paymentMethod: "COD", paymentStatus: "UNPAID", status: "PACKED", shipStatus: "PREPARING",
    courier: "SPX Express", tracking: "SPX71200843", orderedAt: d(-1), etaStart: d(4), etaEnd: d(6), deliveredAt: null,
    items: [{ name: "Stainless tumbler 1L", variant: "Sage", qty: 1, priceMinor: 38900 }],
    shippingMinor: 4500, discountMinor: 0, notes: "", archived: false, createdAt: d(-1),
  },
  {
    id: "ord-4", platform: "FACEBOOK", sellerName: "Iloilo Crafts", customPlatformName: "FB Marketplace", isCod: false,
    paymentMethod: "Bank transfer", paymentStatus: "PAID", status: "PROCESSING", shipStatus: "UNKNOWN",
    courier: "LBC", tracking: "", orderedAt: d(-3), etaStart: d(7), etaEnd: d(10), deliveredAt: null,
    items: [{ name: "Handmade woven bag", variant: "Natural", qty: 1, priceMinor: 95000 }],
    shippingMinor: 12000, discountMinor: 0, notes: "Seller ships every Friday.", archived: false, createdAt: d(-3),
  },
  {
    id: "ord-5", platform: "INSTAGRAM", sellerName: "analog.supply", isCod: true,
    paymentMethod: "COD", paymentStatus: "UNPAID", status: "ORDERED", shipStatus: null,
    courier: "", tracking: "", orderedAt: d(0), etaStart: d(5), etaEnd: d(8), deliveredAt: null,
    items: [{ name: "Film camera strap", variant: "Tan leather", qty: 1, priceMinor: 78000 }],
    shippingMinor: 9000, discountMinor: 0, notes: "", archived: false, createdAt: d(0),
  },
  {
    id: "ord-6", platform: "TEMU", sellerName: "Temu", isCod: false,
    paymentMethod: "Card", paymentStatus: "PAID", status: "ORDERED", shipStatus: null,
    courier: "", tracking: "", orderedAt: d(-1), etaStart: d(9), etaEnd: d(14), deliveredAt: null,
    items: [
      { name: "LED strip 5m RGB", variant: "", qty: 2, priceMinor: 16000 },
      { name: "Cable clips 20pc", variant: "", qty: 1, priceMinor: 4900 },
    ],
    shippingMinor: 0, discountMinor: 4900, notes: "", archived: false, createdAt: d(-1),
  },
  {
    id: "ord-7", platform: "LAZADA", sellerName: "Keychron PH", isCod: false,
    paymentMethod: "Maya", paymentStatus: "PAID", status: "DELIVERED", shipStatus: "DELIVERED",
    courier: "Ninja Van", tracking: "NVPH55021933", orderedAt: d(-9), etaStart: d(-2), etaEnd: d(-1), deliveredAt: d(-1),
    items: [{ name: "K870 mechanical keyboard", variant: "Hot-swap / Brown", qty: 1, priceMinor: 215000 }],
    shippingMinor: 0, discountMinor: 15000, notes: "", archived: false, createdAt: d(-9),
    rating: 5, isFavorite: true,
    warranties: [{ provider: "Keychron", start: d(-9), end: d(356) }],
    receipts: [{ label: "Lazada invoice" }],
  },
  {
    id: "ord-8", platform: "SHOPEE", sellerName: "ClickCity", isCod: false,
    paymentMethod: "GCash", paymentStatus: "PAID", status: "RETURNED", shipStatus: "RETURNED",
    courier: "J&T Express", tracking: "JT820177455", orderedAt: d(-12), etaStart: d(-7), etaEnd: d(-6), deliveredAt: d(-6),
    items: [{ name: "Wireless mouse", variant: "Grey", qty: 1, priceMinor: 64900 }],
    shippingMinor: 3800, discountMinor: 0, notes: "Scroll wheel defective — returned, refund pending.", archived: false, createdAt: d(-12),
    rating: 2, isFavorite: false,
    returns: [{ status: "COMPLETED", reason: "Scroll wheel defective", at: d(-6) }],
    refunds: [{ status: "PENDING", amountMinor: 64900, reason: "Defective item" }],
  },
  {
    id: "ord-9", platform: "AMAZON", sellerName: "Amazon", isCod: false,
    paymentMethod: "Card", paymentStatus: "PAID", status: "DELIVERED", shipStatus: "DELIVERED",
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
    id: "det-2", status: "DELIVERED", shipStatus: "DELIVERED", confidence: "HIGH",
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
  homeArchived: false,         // HomeScreen onToggleArchived
  query: "",
  sort: "newest",
  fq: { platforms: new Set(), statuses: new Set(), codOnly: false, includeArchived: false }, // OrdersScreen query
  theme: "light",
};
let draft = null;
let importNotice = false; /* show "imported — review" chips on the manual form */
let sessionCats = []; /* categories created this session — mirrors the Room categories table */
const allCats = () => [...new Set([...orders.flatMap(o => o.categories || []), ...sessionCats])];

/* ---------- DOM ---------- */
const $ = s => document.querySelector(s);
/* localStorage can throw (blocked storage / file:// privacy modes) — never let it kill the page */
const lsGet = k => { try { return localStorage.getItem(k); } catch { return null; } };
const lsSet = (k, v) => { try { localStorage.setItem(k, v); } catch {} };
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
  if (name === "timeline") renderTimeline();
  if (name === "cod") renderCod();
  if (name === "about") renderAbout();
}

function renderAppbar() {
  const logo = `<span class="appbar-logo">
    <svg viewBox="0 0 512 512" aria-hidden="true"><rect width="512" height="512" rx="120" fill="url(#lg)"/><circle cx="256" cy="256" r="132" fill="none" stroke="#fff" stroke-width="42"/><path d="M194 238 L256 202 L318 238 L318 310 L256 346 L194 310 Z" fill="none" stroke="#fff" stroke-width="24" stroke-linejoin="round"/><path d="M224 273 L250 299 L300 247" fill="none" stroke="#35D0A0" stroke-width="22" stroke-linecap="round" stroke-linejoin="round"/></svg>
    <span>Ordra</span></span>`;
  const back = `<button class="icon-btn" data-back type="button" aria-label="Back"><span class="mi">arrow_back</span></button>`;
  switch (state.screen) {
    case "home":
      appbar.innerHTML = `${logo}
        <button class="icon-btn" type="button" aria-label="Orders" data-goto="orders"><span class="mi">package_2</span></button>
        <button class="icon-btn" type="button" aria-label="Smart import" data-goto="import"><span class="mi">photo_camera</span></button>
        <button class="icon-btn" type="button" aria-label="Delivery calendar" data-goto="calendar"><span class="mi">calendar_month</span></button>
        <button class="icon-btn" type="button" aria-label="Settings" data-goto="settings"><span class="mi">settings</span></button>
        <button class="icon-btn ${state.homeArchived ? "is-on" : ""}" type="button" aria-label="Show archived" onclick="state.homeArchived=!state.homeArchived;renderAppbar();renderHome()"><span class="mi">archive</span></button>`;
      break;
    case "orders":
      appbar.innerHTML = `${back}<span class="appbar-title">Orders</span>
        <button class="icon-btn" type="button" aria-label="Import screenshot" data-goto="import"><span class="mi">photo_camera</span></button>
        <button class="icon-btn" type="button" aria-label="Sort" onclick="openSortMenu(this)"><span class="mi">sort</span></button>`;
      break;
    case "timeline":
      appbar.innerHTML = `${back}<span class="appbar-title">Shipment timeline</span>`;
      break;
    case "cod":
      appbar.innerHTML = `${back}<span class="appbar-title">COD overview</span>`;
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
    case "about":
      appbar.innerHTML = `${back}<span class="appbar-title">About</span>`;
      break;
    case "detail": {
      const o = orders.find(x => x.id === state.detailId);
      appbar.innerHTML = `${back}<span class="appbar-title"></span>
        <button class="icon-btn" type="button" aria-label="Edit order" onclick="startEdit('${o?.id}')"><span class="mi">edit</span></button>
        <button class="icon-btn" type="button" aria-label="More options" onclick="openDetailMenu(this,'${o?.id}')"><span class="mi">more_vert</span></button>`;
      break;
    }
    case "add":
      appbar.innerHTML = `${back}<span class="appbar-title">${editOrderId ? "Edit order" : "New order"}</span>`;
      break;
    case "settings":
      appbar.innerHTML = `${back}<span class="appbar-title">Settings</span>`;
      break;
  }
}

/* ---------- dropdown menu (mirrors DropdownMenu) ---------- */
let menuAnchor = null;
function openMenu(anchor, items) {
  const menu = $("#popupMenu");
  menuAnchor = anchor;
  menu.innerHTML = items.map((it, i) =>
    `<button class="menu-item ${it.danger ? "danger" : ""}" data-mi="${i}" type="button">${it.check ? `<span class="mi">check</span>` : ""}${esc(it.label)}</button>`).join("");
  menu.hidden = false;
  const r = anchor.getBoundingClientRect(), host = $("#device").getBoundingClientRect();
  menu.style.top = `${r.bottom - host.top + 4}px`;
  menu.style.right = `${host.right - r.right}px`;
  menu._items = items;
}
function closeMenu() { $("#popupMenu").hidden = true; menuAnchor = null; }

const SORTS = [["newest", "Newest first"], ["oldest", "Oldest first"], ["eta", "Delivery soonest"], ["total-desc", "Highest total"], ["total-asc", "Lowest total"]];
function openSortMenu(btn) {
  openMenu(btn, SORTS.map(([k, label]) => ({
    label, check: state.sort === k,
    fn: () => { state.sort = k; renderOrders(); },
  })));
}
function openDetailMenu(btn, id) {
  const o = orders.find(x => x.id === id);
  openMenu(btn, [
    { label: o?.archived ? "Restore" : "Archive", fn: () => toggleArchive(id) },
    { label: "Delete", danger: true, fn: () => askDelete() },
  ]);
}

/* ---------- shared bits ---------- */
const platOf = o => PLATFORMS[o.platform] || PLATFORMS.OTHER;
/* status icons — DESIGN_SYSTEM "Icon pairing" table */
const STATUS_ICONS = {
  DRAFT: "draft", ORDERED: "receipt_long", PROCESSING: "inventory_2", PACKED: "inventory_2",
  SHIPPED: "local_shipping", DELIVERED: "check_circle", CANCELLED: "cancel",
  RETURNED: "assignment_return", REFUNDED: "currency_exchange",
  UNKNOWN: "help", PREPARING: "inventory_2", PICKED_UP: "local_shipping",
  IN_TRANSIT: "route", ARRIVED_AT_HUB: "warehouse", OUT_FOR_DELIVERY: "local_shipping",
  DELIVERY_FAILED: "error", RETURNING: "assignment_return",
};
const chipFor = (s, table, hero) => {
  const st = table[s];
  return `<span class="status-chip ${hero ? "hero" : ""}" style="color:${st.color};background:${st.color}1f"><span class="mi">${STATUS_ICONS[s] || "help"}</span>${st.label}</span>`;
};
const statusChip = s => chipFor(s, STATUSES);
const platformBadge = o => {
  const p = platOf(o);
  return `<span class="plat-chip" style="color:${p.color};background:${p.color}1c">${esc(o.customPlatformName || p.label)}</span>`;
};
const courierBadge = o => o.courier ? `<span class="courier-chip"><span class="mi">local_shipping</span>${esc(o.courier)}</span>` : "";
const codChip = () => `<span class="cod-chip"><span class="mi">payments</span>COD</span>`;

/* compact TrackingProgress — shipment step dots */
function shipProgress(o) {
  const ship = o.shipStatus ? SHIP_STATUSES[o.shipStatus] : null;
  if (!ship || ship.step < 0) return "";
  const step = ship.step;
  return `<span class="tprog">${TIMELINE_STEPS.map((_, i) => {
    const cls = i < step ? "done" : i === step ? "now" : "";
    return `<span class="tprog-dot ${cls}"></span>${i < TIMELINE_STEPS.length - 1 ? `<span class="tprog-line ${i < step ? "done" : ""}"></span>` : ""}`;
  }).join("")}</span>`;
}

/* ParcelCard standard variant — PlatformBadge → name → CourierBadge+tracking → StatusChip → progress → ETA */
function orderCard(o) {
  const eta = o.status === "DELIVERED" ? `Delivered ${fmtDate(o.deliveredAt)}`
    : TERMINAL.includes(o.status) ? STATUSES[o.status].label
    : o.etaEnd ? `ETA ${fmtDate(o.etaStart)} – ${fmtDate(o.etaEnd)}` : "No ETA yet";
  return `<button class="order-card" data-open="${o.id}" type="button">
    <span class="order-main">
      <span style="display:flex;gap:6px;align-items:center;margin-bottom:6px">${platformBadge(o)}${o.isCod && o.paymentStatus !== "PAID" ? codChip() : ""}</span>
      <span class="order-title">${esc(itemsSummary(o))}</span>
      <span class="order-sub">${courierBadge(o)}${o.tracking ? `<span class="mono" style="font-size:11px;color:var(--text-2)">${esc(o.tracking)}</span>` : ""}<span>·</span>${eta}</span>
      <span style="display:flex;gap:6px;margin-top:7px;align-items:center">${statusChip(o.status)}</span>
      ${shipProgress(o)}
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
    <ellipse cx="50" cy="95" rx="30" ry="4.4" fill="rgba(20,22,77,.28)"/>
    <line x1="70" y1="24" x2="74" y2="12" stroke="#7C5CFC" stroke-width="2.4" stroke-linecap="round"/>
    <circle cx="75" cy="9" r="3.4" fill="${state === "offline" ? "none" : "#35D0A0"}" ${state === "offline" ? `stroke="#14164D" stroke-width="1.6"` : ""}/>
    <rect x="12" y="22" width="76" height="72" rx="26" fill="url(#ogBody)"/>
    <rect x="12" y="22" width="76" height="72" rx="26" fill="url(#ogShade)"/>
    <ellipse cx="38" cy="36" rx="24" ry="10" fill="rgba(255,255,255,.20)" transform="rotate(-8 38 36)"/>
    <line x1="16" y1="40" x2="84" y2="40" stroke="rgba(255,255,255,.25)" stroke-width="1.8" stroke-linecap="round"/>
    <line x1="50" y1="22" x2="50" y2="40" stroke="rgba(255,255,255,.25)" stroke-width="3.2" stroke-linecap="round"/>
    ${look[0]}${look[1]}${badge}${parcel}${check}
  </svg>`;
}

/* ---------- home (mirrors HomeScreen: banner → summary → upcoming → recent; archive toggle flips to archived list) ---------- */
function renderHome() {
  const body = $("#homeBody");

  if (state.homeArchived) {
    const arch = orders.filter(o => o.archived);
    body.innerHTML = `
      <div class="section-head" style="margin-top:10px"><h3>Archived</h3></div>
      <div class="stack">${arch.length ? arch.map(orderCard).join("")
        : `<div class="empty"><span class="mi">inventory_2</span><p>No archived orders</p></div>`}</div>
      <div class="scroll-pad"></div>`;
    return;
  }

  const h = new Date().getHours();
  const active = orders.filter(isActive);
  const soon = active.filter(o => o.etaEnd && o.etaEnd <= d(3)).sort((a, b) => a.etaEnd.localeCompare(b.etaEnd));
  const codDue = orders.filter(isPendingCod).reduce((s, o) => s + orderTotal(o), 0);

  // AI banner — same priority resolver as HomeViewModel (attention > today > delivered > active > empty).
  const today = d(0);
  const attention = active.filter(o => o.etaEnd && o.etaEnd < today).length;
  const arrivingToday = active.filter(o => (o.etaStart || o.etaEnd) === today).length;
  const deliveredRecent = orders.filter(o => !o.archived && o.status === "DELIVERED" && o.deliveredAt && o.deliveredAt >= d(-2)).length;
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

  const upcoming = soon.slice(0, 3);
  const recent = [...orders].filter(o => !o.archived).sort((a, b) => b.createdAt.localeCompare(a.createdAt)).slice(0, 3);
  // featured ParcelCard — the most important current shipment (soonest ETA active order)
  const featured = soon[0] || active.filter(o => o.status !== "DELIVERED").sort((a, b) => b.createdAt.localeCompare(a.createdAt))[0] || null;

  body.innerHTML = `
    <div class="ai-banner" style="margin-top:10px">
      <div class="ai-banner-text">
        <p class="ai-eyebrow"><span class="ai-dot"></span>ORDRA AI</p>
        <p class="ai-greet">${h < 12 ? "Good morning" : h < 18 ? "Good afternoon" : "Good evening"}</p>
        <p class="ai-msg">${msgs[banner.kind](banner.n)}</p>
        ${banner.extra ? `<p class="ai-msg-sub">${msgs.arriving(banner.extra)}</p>` : ""}
        <div class="ai-actions">
          <button class="btn btn-tonal" type="button" data-goto="orders">View parcels</button>
          <button class="btn btn-tonal" type="button" onclick="showScreen('add',{fresh:true})">Add order</button>
        </div>
      </div>
      <button class="ordi-btn" type="button" onclick="openChat()" aria-label="Chat with Ordi">${ordiSvg(banner.state)}</button>
    </div>

    ${featured ? `
      <button class="order-card parcel-featured" data-open="${featured.id}" type="button" style="margin-top:0">
        <span class="order-main">
          <span style="display:flex;gap:6px;align-items:center;margin-bottom:7px">${platformBadge(featured)}${featured.isCod && featured.paymentStatus !== "PAID" ? codChip() : ""}</span>
          <span class="order-title">${esc(itemsSummary(featured))}</span>
          <span class="order-sub" style="margin-top:3px">${courierBadge(featured)}${featured.tracking ? `<span class="mono" style="font-size:11px;color:var(--text-2)">${esc(featured.tracking)}</span>` : ""}</span>
          <span style="display:flex;gap:6px;margin-top:8px;align-items:center">${statusChip(featured.status)}${featured.shipStatus ? chipFor(featured.shipStatus, SHIP_STATUSES) : ""}</span>
          ${shipProgress(featured)}
          <span class="parcel-eta"><span class="mi">event</span>${featured.status === "DELIVERED" ? `Delivered ${fmtDate(featured.deliveredAt)}` : featured.etaEnd ? `ETA ${fmtDate(featured.etaStart)} – ${fmtDate(featured.etaEnd)}` : "No ETA yet"}</span>
        </span>
        <span class="order-side"><span class="order-total" style="font-size:16px">${peso(orderTotal(featured))}</span></span>
      </button>` : ""}

    <div class="stat-row">
      <div class="stat-card"><span class="stat-ico indigo"><span class="mi">package_2</span></span><div class="stat-num">${active.length}</div><div class="stat-label">Active orders</div></div>
      <div class="stat-card"><span class="stat-ico cyan"><span class="mi">local_shipping</span></span><div class="stat-num">${soon.length}</div><div class="stat-label">Arriving soon</div></div>
      <button class="stat-card" type="button" data-goto="cod"><span class="stat-ico amber"><span class="mi">payments</span></span><div class="stat-num money">${peso(codDue)}</div><div class="stat-label">COD to prepare</div></button>
    </div>

    ${upcoming.length ? `
      <div class="section-head"><h3>Upcoming deliveries</h3></div>
      <div class="stack">${upcoming.map(orderCard).join("")}</div>` : ""}

    ${recent.length ? `
      <div class="section-head"><h3>Recent orders</h3></div>
      <div class="stack">${recent.map(orderCard).join("")}</div>` : ""}

    ${!upcoming.length && !recent.length ? `<div class="empty"><span class="mi">package_2</span><p>No orders yet — add your first one.</p></div>` : ""}
    <div class="scroll-pad"></div>`;
}

/* ---------- orders list (mirrors OrdersScreen: search → Filters dialog → sort menu) ---------- */
function renderOrders() {
  const q = state.query.toLowerCase(), fq = state.fq;
  let list = orders.filter(o => {
    if (!fq.includeArchived && o.archived) return false;
    if (fq.platforms.size && !fq.platforms.has(o.platform)) return false;
    if (fq.statuses.size && !fq.statuses.has(o.status)) return false;
    if (fq.codOnly && !isPendingCod(o)) return false;
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
  $("#searchClear").hidden = !state.query;
  $("#ordersList").innerHTML = list.length
    ? list.map(orderCard).join("")
    : `<div class="empty"><span class="mi">search_off</span><p>No orders match${q ? ` “${esc(state.query)}”` : " this filter"}.</p></div>`;
}

/* Filters dialog — mirrors OrdersScreen FilterSheet (platform/status/category checkboxes + COD + archived + Clear all/Done) */
function openFiltersDialog() {
  const fq = state.fq;
  const cats = [...new Set(orders.flatMap(o => o.categories || []))];
  const check = (group, val, label, on) =>
    `<label class="check-row"><input type="checkbox" data-fg="${group}" value="${esc(val)}" ${on ? "checked" : ""}><span>${esc(label)}</span></label>`;
  showDialog({
    title: "Filters",
    bodyHtml: `
      <p class="dlg-label">Platform</p>
      ${Object.entries(PLATFORMS).map(([k, p]) => check("platforms", k, p.label, fq.platforms.has(k))).join("")}
      <p class="dlg-label">Status</p>
      ${Object.entries(STATUSES).map(([k, v]) => check("statuses", k, v.label, fq.statuses.has(k))).join("")}
      ${cats.length ? `<p class="dlg-label">Category</p>${cats.map(c => check("cats", c, c, (fq.cats || new Set()).has(c))).join("")}` : ""}
      <p class="dlg-label"></p>
      ${check("codOnly", "1", "COD pending only", fq.codOnly)}
      ${check("includeArchived", "1", "Show archived", fq.includeArchived)}`,
    actions: [
      { label: "Clear all", cls: "btn-text", fn: () => { state.fq = { platforms: new Set(), statuses: new Set(), codOnly: false, includeArchived: false }; renderOrders(); } },
      { label: "Done", cls: "btn-fill", fn: () => {} },
    ],
    keepOpen: true,
  });
  document.querySelectorAll("[data-fg]").forEach(cb => cb.onchange = () => {
    const fq2 = state.fq, g = cb.dataset.fg;
    if (g === "platforms" || g === "statuses" || g === "cats") {
      fq2[g] = fq2[g] || new Set();
      cb.checked ? fq2[g].add(cb.value) : fq2[g].delete(cb.value);
    } else fq2[g] = cb.checked;
    renderOrders();
  });
}

/* ---------- generic dialog (mirrors AlertDialog) ---------- */
let dlgOpen = null;
function showDialog({ title, bodyHtml = "", actions = [], keepOpen = false }) {
  const ov = $("#dialogOverlay");
  $("#dlgTitle").textContent = title;
  $("#dlgDesc").innerHTML = bodyHtml;
  $("#dlgActions").innerHTML = actions.map((a, i) =>
    `<button class="btn ${a.cls || "btn-text"}" data-da="${i}" type="button">${esc(a.label)}</button>`).join("");
  ov.hidden = false;
  dlgOpen = keepOpen ? () => {} : null;
  document.querySelectorAll("[data-da]").forEach(b => b.onclick = () => {
    const a = actions[+b.dataset.da];
    a.fn?.();
    if (!keepOpen) closeDialog();
    else if (a.close !== false) closeDialog();
  });
}
function closeDialog() { $("#dialogOverlay").hidden = true; }
function openPicker(title, entries, current, onPick) {
  showDialog({
    title,
    bodyHtml: `<div class="picker-list">${entries.map(([k, v]) =>
      `<label class="check-row"><input type="radio" name="picker" value="${k}" ${k === current ? "checked" : ""}><span>${esc(v.label)}</span></label>`).join("")}</div>`,
    actions: [{ label: "Cancel", cls: "btn-text", fn: () => {} }],
    keepOpen: true,
  });
  document.querySelectorAll('input[name="picker"]').forEach(r => r.onchange = () => { onPick(r.value); closeDialog(); });
}

/* ---------- detail (mirrors OrderDetailScreen) ---------- */
function renderDetail() {
  const o = orders.find(x => x.id === state.detailId);
  const body = $("#detailBody");
  if (!o) { body.innerHTML = `<div class="empty"><p>This order no longer exists.</p></div>`; return; }
  const p = platOf(o);
  const sub = o.items.reduce((s, i) => s + i.qty * i.priceMinor, 0);
  const ship = o.shipStatus ? SHIP_STATUSES[o.shipStatus] : null;

  const shipmentCard = (o.courier || o.tracking || ship) ? `
    <div class="card"><h4 class="card-title">Shipment</h4>
      <div class="kv-row"><span class="k">Courier</span><span class="v">${esc(o.courier || "Not set")}</span></div>
      <div class="kv-row"><span class="k">Tracking</span><span class="v">${o.tracking ? `<span class="mono">${esc(o.tracking)}</span> <button class="copy-btn" type="button" aria-label="Copy tracking number" onclick="navigator.clipboard?.writeText('${esc(o.tracking)}');toast('Tracking number copied')"><span class="mi">content_copy</span></button>` : "—"}</span></div>
      <div class="kv-row"><span class="k">Status</span><span class="v">${ship ? statusChip2(o.shipStatus, SHIP_STATUSES) : "—"}</span></div>
      <div class="kv-row"><span class="k">Estimated delivery</span><span class="v">${o.etaStart ? `${fmtDate(o.etaStart)} – ${fmtDate(o.etaEnd)}` : "—"}</span></div>
      <div class="kv-row"><span class="k">Source</span><span class="v">Set manually</span></div>
      <div class="dialog-actions" style="margin-top:8px;justify-content:flex-start;flex-wrap:wrap">
        <button class="btn btn-outline" type="button" onclick="updateShipStatus('${o.id}')">Update shipment status</button>
        ${o.courier && o.tracking ? `<button class="btn btn-outline" type="button" onclick="toast('Opens ${esc(o.courier)} tracking in browser on device')">Track externally</button>` : ""}
        <button class="btn btn-outline" type="button" onclick="showScreen('timeline')">View timeline</button>
      </div>
    </div>` : `
    <div class="card"><h4 class="card-title">Shipment</h4>
      <p class="card-sub">No shipment details yet — add a courier or tracking number when editing.</p>
    </div>`;

  body.innerHTML = `
    <div class="detail-hero">
      <span class="plat-badge" style="background:${p.color}">${p.initial}</span>
      <div>
        <h3>${esc(itemsSummary(o))}</h3>
        <div class="order-sub">${esc(o.sellerName)} · Ordered ${fmtDate(o.orderedAt)}</div>
        <div style="display:flex;gap:6px;margin-top:8px;flex-wrap:wrap;align-items:center">${platformBadge(o)}${statusChip(o.status)}${o.archived ? `<span class="status-chip" style="color:var(--text-2);background:var(--surface-var)"><span class="mi">archive</span>Archived</span>` : ""}</div>
      </div>
      <span class="order-side"><span class="order-total" style="font-size:17px">${peso(orderTotal(o))}</span></span>
    </div>

    ${!TERMINAL.includes(o.status) ? `
    <div class="dialog-actions" style="margin-top:10px;justify-content:flex-start">
      <button class="btn btn-outline" type="button" onclick="updateOrderStatus('${o.id}')">Update status</button>
      <button class="btn btn-outline" type="button" onclick="markDelivered('${o.id}')">Mark delivered</button>
    </div>` : ""}

    ${shipmentCard}

    <div class="card"><h4 class="card-title">Items</h4>
      ${o.items.map(i => `<div class="item-row"><span><div class="item-name">${esc(i.name)}</div>${i.variant ? `<div class="item-var">${esc(i.variant)}</div>` : ""}<div class="item-var">${peso(i.priceMinor)} ×${i.qty}</div></span><span class="item-price">${peso(i.qty * i.priceMinor)}</span></div>`).join("")}
      <hr class="hr">
      <div class="kv-row"><span class="k">Items subtotal</span><span class="v">${peso(sub)}</span></div>
      <div class="kv-row"><span class="k">Shipping</span><span class="v">${o.shippingMinor ? peso(o.shippingMinor) : "Free"}</span></div>
      ${o.discountMinor ? `<div class="kv-row"><span class="k">Discount</span><span class="v">−${peso(o.discountMinor)}</span></div>` : ""}
      <div class="kv-row total"><span class="k">Total</span><span class="v">${peso(orderTotal(o))}</span></div>
    </div>

    <div class="card"><h4 class="card-title">Payment</h4>
      <div class="kv-row"><span class="k">Method</span><span class="v">${esc(o.paymentMethod)}</span></div>
      <div class="kv-row"><span class="k">Status</span><span class="v">${o.paymentStatus === "PAID" ? "Paid" : o.paymentStatus === "PARTIAL" ? "Partially paid" : "Unpaid"}</span></div>
      ${o.isCod && o.paymentStatus !== "PAID" ? `<div class="banner warn" style="margin-top:8px"><span class="mi">payments</span>Prepare ${peso(orderTotal(o))} cash for the rider.</div>` : ""}
    </div>

    ${o.notes ? `<div class="card"><h4 class="card-title">Notes</h4><p style="font-size:13.5px;color:var(--text-2);margin:0;line-height:1.55">${esc(o.notes)}</p></div>` : ""}

    ${postPurchaseHtml(o)}
    <div class="scroll-pad"></div>`;
}

const statusChip2 = (s, table) => chipFor(s, table);

/* StatusProgression: shipment DELIVERED → order DELIVERED; shipment RETURNED → order RETURNED */
function applyShipStatus(o, s) {
  o.shipStatus = s;
  if (s === "DELIVERED") { o.status = "DELIVERED"; o.deliveredAt = d(0); if (o.isCod) o.paymentStatus = "PAID"; }
  if (s === "RETURNED") o.status = "RETURNED";
}

function updateOrderStatus(id) {
  const o = orders.find(x => x.id === id);
  openPicker("Update status", Object.entries(STATUSES).filter(([k]) => k !== "DRAFT"), o.status, s => {
    o.status = s;
    if (s === "DELIVERED") { o.deliveredAt = d(0); o.shipStatus = "DELIVERED"; }
    renderDetail(); toast(`Status set to ${STATUSES[s].label}`);
  });
}

function updateShipStatus(id) {
  const o = orders.find(x => x.id === id);
  openPicker("Update shipment status", Object.entries(SHIP_STATUSES), o.shipStatus || "UNKNOWN", s => {
    applyShipStatus(o, s);
    renderDetail(); toast(`Shipment ${SHIP_STATUSES[s].label.toLowerCase()}`);
  });
}

function markDelivered(id) {
  const o = orders.find(x => x.id === id);
  if (!o) return;
  showDialog({
    title: "Mark as delivered?",
    bodyHtml: "This sets the order status to Delivered and stamps the delivery time.",
    actions: [
      { label: "Cancel", cls: "btn-text", fn: () => {} },
      { label: "Mark delivered", cls: "btn-fill", fn: () => {
        o.status = "DELIVERED"; o.shipStatus = "DELIVERED"; o.deliveredAt = d(0);
        if (o.isCod) o.paymentStatus = "PAID";
        refreshCurrent(); toast(o.isCod ? "Delivered — COD marked as paid" : "Marked as delivered");
      }},
    ],
  });
}
function refreshCurrent() { showScreen(state.screen); }

function toggleArchive(id) {
  const o = orders.find(x => x.id === id);
  if (!o) return;
  o.archived = !o.archived;
  renderDetail(); renderAppbar();
  toast(o.archived ? "Order archived" : "Order restored");
}

function askDelete() {
  const o = orders.find(x => x.id === state.detailId);
  if (!o) return;
  showDialog({
    title: "Delete this order?",
    bodyHtml: `This permanently removes “${esc(itemsSummary(o))}” and its items. This can't be undone.`,
    actions: [
      { label: "Cancel", cls: "btn-text", fn: () => {} },
      { label: "Delete", cls: "btn-danger", fn: () => {
        orders = orders.filter(x => x.id !== o.id);
        showScreen("orders"); toast("Order deleted");
      }},
    ],
  });
}

/* ---------- dialog test triggers (stage rail — mockup chrome, not app UI) ---------- */
function testDialog(kind) {
  const o = orders.find(x => x.id === state.detailId) || orders.find(x => !TERMINAL.includes(x.status)) || orders[0];
  const viaDetail = fn => { if (!o) return toast("No orders to test with"); state.detailId = o.id; showScreen("detail"); fn(o.id); };
  switch (kind) {
    case "filters": showScreen("orders"); openFiltersDialog(); break;
    case "sort": showScreen("orders"); openSortMenu($('#appbar [aria-label="Sort"]')); break;
    case "menu": viaDetail(id => openDetailMenu($('#appbar [aria-label="More options"]'), id)); break;
    case "status": viaDetail(updateOrderStatus); break;
    case "ship": viaDetail(updateShipStatus); break;
    case "delivered": viaDetail(markDelivered); break;
    case "delete": viaDetail(() => askDelete()); break;
    case "chat": openChat(); break;
    case "onboard": obPage = 0; renderOnboard(); $("#onboard").hidden = false; break;
    case "toast": toast("Toasts confirm actions — they never ask questions"); break;
  }
}
document.querySelectorAll("[data-dlg]").forEach(b =>
  b.addEventListener("click", () => testDialog(b.dataset.dlg)));

/* ---------- add/edit order — FORM → REVIEW (mirrors AddEditOrderScreen) ---------- */
let editStep = "form";        // "form" | "review" — mirrors EditorStep
let editMode = "manual";      // "manual" | "import" — entry choice on new orders
let editOrderId = null;
let editAttempted = false;    // mirrors attemptedSave: every error visible after Review
let editTouched = new Set();  // field keys already left once — validated on blur

/* money parsing mirrors MoneyFormat.parseToMinor — null when invalid */
const parseMoney = s => {
  const cleaned = String(s ?? "").trim().replace(/,/g, "").replace(/[^\d.\-]/g, "");
  if (!cleaned || cleaned === "-") return null;
  const n = Number(cleaned);
  return Number.isFinite(n) ? Math.round(n * 100) : null;
};
/* minor units -> editable string, mirrors BigDecimal.stripTrailingZeros().toPlainString() */
const minorStr = m => m == null ? "" : (m % 100 ? (m / 100).toFixed(2) : String(m / 100));

/* mirrors OrderItemForm.isCompletelyEmpty */
const itemEmpty = it => !it.name.trim() && !it.variant.trim() && !it.price.trim() && it.qty === "1";
/* mirrors OrderFormState.itemsSubtotalMinor / suggestedTotalMinor */
const itemsSubtotal = dr => dr.items.reduce((s, it) => {
  const u = parseMoney(it.price), q = parseInt(it.qty, 10);
  return s + (u > 0 && q > 0 ? u * q : 0);
}, 0);
const suggestedTotal = dr => itemsSubtotal(dr) + (parseMoney(dr.shipping) || 0) - (parseMoney(dr.discount) || 0);
const draftTotal = dr => parseMoney(dr.total) ?? suggestedTotal(dr);

/* mirrors OrderFormState.fieldErrors() — same keys and messages */
function fieldErrors(dr) {
  const e = {};
  if (!dr.title.trim() && !dr.items.some(i => i.name.trim())) e.title = "Add an order name or at least one item";
  if (!dr.platform) e.platform = "Platform is required";
  if (dr.platform === "OTHER" && !dr.customPlatformName.trim()) e.customPlatformName = "Name the platform or store";
  if (parseMoney(dr.total) == null) e.total = "Valid total is required";
  dr.items.forEach((it, i) => {
    if (itemEmpty(it)) return;
    if (!it.name.trim()) e[`item_name_${i}`] = "Item name is required";
    const q = parseInt(it.qty, 10);
    if (!Number.isInteger(q) || q < 1) e[`item_qty_${i}`] = "Quantity must be at least 1";
  });
  if (dr.etaStart && dr.etaEnd && dr.etaEnd < dr.etaStart) e.etaEnd = "End date can't be before start";
  if (dr.shipping.trim() && parseMoney(dr.shipping) == null) e.shipping = "Invalid amount";
  if (dr.discount.trim() && parseMoney(dr.discount) == null) e.discount = "Invalid amount";
  if (dr.courier === "Other" && !dr.customCourierName.trim()) e.customCourierName = "Name the courier";
  return e;
}
/* paints current errors without re-rendering — keeps focus while typing */
function updateErrorUI() {
  if (!draft) return;
  const errs = fieldErrors(draft);
  document.querySelectorAll("#editBody [data-fk]").forEach(el => {
    const k = el.dataset.fk, msg = (editAttempted || editTouched.has(k)) ? errs[k] : null;
    el.classList.toggle("is-error", !!msg);
    el.setAttribute("aria-invalid", msg ? "true" : "false");
    const slot = document.querySelector(`#editBody [data-err="${k}"]`);
    if (slot) { slot.textContent = msg || ""; slot.hidden = !msg; }
  });
  const th = document.querySelector('[data-hint="total"]');
  if (th) {
    const s = suggestedTotal(draft);
    th.hidden = !(draft.total.trim() === "" && s !== 0);
    th.textContent = `Suggested: ${peso(s)}`;
  }
}
function touchField(key) { editTouched.add(key); updateErrorUI(); }

function startWizard() { startEdit(null); }
function startEdit(orderId) {
  editOrderId = orderId || null;
  const o = orderId ? orders.find(x => x.id === orderId) : null;
  editStep = "form"; editMode = "manual";
  editAttempted = false; editTouched = new Set();
  importStep = 0; importDraft = null; importNotice = false;
  draft = o ? {
    platform: o.platform, customPlatformName: o.customPlatformName || "", title: o.title || "",
    sellerName: o.sellerName,
    items: o.items.map(i => ({ name: i.name, variant: i.variant || "", qty: String(i.qty), price: minorStr(i.priceMinor) })),
    shipping: minorStr(o.shippingMinor), discount: minorStr(o.discountMinor),
    total: minorStr(o.totalMinor ?? orderTotal(o)),
    paymentMethod: o.paymentMethod, paymentStatus: o.paymentStatus, isCod: o.isCod,
    courier: COURIERS.includes(o.courier) ? o.courier : (o.courier ? "Other" : ""),
    customCourierName: o.courier && !COURIERS.includes(o.courier) ? o.courier : "",
    tracking: o.tracking, orderedAt: o.orderedAt,
    etaStart: o.etaStart, etaEnd: o.etaEnd, notes: o.notes,
    reminders: o.reminders || ["dayof"], status: o.status,
    categories: [...(o.categories || [])], image: o.image || null,
  } : {
    platform: null, customPlatformName: "", title: "", sellerName: "",
    items: [{ name: "", variant: "", qty: "1", price: "" }],
    shipping: "", discount: "", total: "",
    paymentMethod: "COD", paymentStatus: "UNPAID", isCod: true,
    courier: "", customCourierName: "", tracking: "", orderedAt: d(0),
    etaStart: "", etaEnd: "", notes: "",
    reminders: ["dayof"], status: "ORDERED",
    categories: [], image: null,
  };
  showScreen("add"); renderEdit();
}

const entrySeg = () => editOrderId ? "" : `
  <div class="segmented" id="entrySeg" style="margin-top:8px">
    <button class="seg ${editMode === "manual" ? "is-on" : ""}" data-em="manual" type="button">
      <span class="mi">edit_note</span>Manual entry</button>
    <button class="seg ${editMode === "import" ? "is-on" : ""}" data-em="import" type="button">
      <span class="mi">document_scanner</span>Import</button>
  </div>`;

function renderEdit() {
  const dr = draft;
  $("#editBack").textContent = editStep === "form" ? "Cancel" : "Back to edit";
  $("#editNext").innerHTML = editStep === "form" ? "Review" : `<span class="mi">check</span>Save order`;
  $("#editFooter").hidden = editMode === "import";
  const body = $("#editBody");

  if (editMode === "import") { renderEditImport(body); return; }

  if (editStep === "review") {
    const p = dr.platform ? platOf({ platform: dr.platform }) : null;
    const ship = parseMoney(dr.shipping), disc = parseMoney(dr.discount);
    body.innerHTML = `<div class="fade-in">
      <div class="card" style="margin-top:10px"><h4 class="card-title">Review order</h4>
        <div class="kv-row"><span class="k">Platform</span><span class="v">${p ? esc(dr.customPlatformName || p.label) : "—"}</span></div>
        <div class="kv-row"><span class="k">Order name</span><span class="v">${esc(dr.title || dr.items.find(i => !itemEmpty(i))?.name || "") || "—"}</span></div>
        ${dr.sellerName ? `<div class="kv-row"><span class="k">Seller / store</span><span class="v">${esc(dr.sellerName)}</span></div>` : ""}
        ${dr.items.filter(i => !itemEmpty(i)).length ? `<hr class="hr">
        ${dr.items.filter(i => !itemEmpty(i)).map(i => `<div class="item-row"><span><div class="item-name">${esc(i.name)}</div>${i.variant ? `<div class="item-var">${esc(i.variant)}</div>` : ""}</span><span class="item-price">${parseInt(i.qty, 10) || 1} × ${peso(parseMoney(i.price) || 0)}</span></div>`).join("")}` : ""}
        <hr class="hr">
        ${ship != null ? `<div class="kv-row"><span class="k">Shipping</span><span class="v">${peso(ship)}</span></div>` : ""}
        ${disc != null ? `<div class="kv-row"><span class="k">Discount</span><span class="v">−${peso(disc)}</span></div>` : ""}
        <div class="kv-row total"><span class="k">Total</span><span class="v">${peso(parseMoney(dr.total))}</span></div>
        <hr class="hr">
        <div class="kv-row"><span class="k">Payment</span><span class="v">${dr.paymentMethod} · ${dr.paymentStatus === "PAID" ? "Paid" : dr.paymentStatus === "PARTIAL" ? "Partial" : "Unpaid"}</span></div>
        <div class="kv-row"><span class="k">Status</span><span class="v">${STATUSES[dr.status].label}</span></div>
        ${dr.courier ? `<div class="kv-row"><span class="k">Courier</span><span class="v">${esc(dr.courier === "Other" ? dr.customCourierName : dr.courier)}</span></div>` : ""}
        ${dr.tracking ? `<div class="kv-row"><span class="k">Tracking</span><span class="v">${esc(dr.tracking)}</span></div>` : ""}
        <div class="kv-row"><span class="k">ETA</span><span class="v">${dr.etaStart ? `${fmtDate(dr.etaStart)} – ${fmtDate(dr.etaEnd)}` : "—"}</span></div>
        <div class="kv-row"><span class="k">Reminders</span><span class="v">${dr.reminders.length ? dr.reminders.map(r => ({ dayof: "On delivery day", minus1: "1 day before", minus2: "2 days before" })[r]).join(", ") : "None"}</span></div>
      </div>
      <div class="banner info"><span class="mi">info</span>Saved locally on this device — works offline.</div>
      <div class="scroll-pad"></div></div>`;
    body.scrollTop = 0;
    return;
  }

  /* manual form — mirrors OrderFormContent: required summary + per-field supportingText errors */
  body.innerHTML = `<div class="fade-in">
    ${entrySeg()}
    <p class="req-note">Required: a name, platform, and total.</p>
    ${importNotice && importDraft ? `<div class="card" style="margin-top:10px"><h4 class="card-title">Imported from screenshot</h4>
      <div class="banner warn" style="margin:0 0 8px"><span class="mi">fact_check</span>
        Review before saving — every extracted field is a suggestion you can correct.
      </div>
      <div class="conf-row">${Object.entries(importDraft.conf).map(([k, v]) => `<span class="conf-chip conf-${v}">${k} · ${v}</span>`).join("")}</div>
      <button class="btn btn-text" id="wImportDismiss" type="button" style="margin-top:8px">Dismiss</button>
    </div>` : ""}

    <div class="card" style="margin-top:10px"><h4 class="card-title">Platform</h4>
      <p class="field-label">Order name <span class="req">*</span></p>
      <input class="field" id="wTitle" data-fk="title" placeholder="e.g. Soundcore R50i earbuds" value="${esc(dr.title)}">
      <p class="field-error" data-err="title" hidden></p>
      <p class="field-label">Platform <span class="req">*</span></p>
      <div class="plat-grid" data-fk="platform">${Object.entries(PLATFORMS).map(([k, p]) => `
        <button type="button" class="plat-opt ${dr.platform === k ? "is-on" : ""}" data-plat="${k}">
          <span class="plat-badge" style="background:${p.color}">${p.initial}</span>${p.label}
        </button>`).join("")}
      </div>
      <p class="field-error" data-err="platform" hidden></p>
      <div class="custom-plat" ${dr.platform === "OTHER" ? "" : "hidden"}>
        <p class="field-label">Platform or store name <span class="req">*</span></p>
        <input class="field" id="wCustomPlat" data-fk="customPlatformName" placeholder="e.g. Carousell, Viber seller" value="${esc(dr.customPlatformName)}">
        <p class="field-error" data-err="customPlatformName" hidden></p>
      </div>
      <p class="field-label">Seller / store</p>
      <input class="field" id="wSeller" placeholder="e.g. AudioHub PH" value="${esc(dr.sellerName)}">
    </div>

    <div class="card"><h4 class="card-title">Items</h4>
      ${dr.items.map((it, i) => `
        <div class="wiz-item">
          <div class="wiz-item-head">
            <strong style="font-size:12.5px;color:var(--text-2)">Item ${i + 1}</strong>
            ${dr.items.length > 1 ? `<button class="remove-item" data-rm="${i}" type="button" aria-label="Remove item"><span class="mi">close</span></button>` : ""}
          </div>
          <p class="field-label" style="margin-top:8px">Item name <span class="req">*</span></p>
          <input class="field" data-item="${i}" data-k="name" data-fk="item_name_${i}" placeholder="Item name" value="${esc(it.name)}">
          <p class="field-error" data-err="item_name_${i}" hidden></p>
          <div class="field-row3" style="margin-top:8px">
            <input class="field" data-item="${i}" data-k="variant" placeholder="Variant" value="${esc(it.variant)}">
            <span><input class="field" data-item="${i}" data-k="qty" data-fk="item_qty_${i}" inputmode="numeric" placeholder="Qty" value="${esc(it.qty)}">
            <p class="field-error" data-err="item_qty_${i}" hidden></p></span>
            <input class="field" data-item="${i}" data-k="price" inputmode="decimal" placeholder="Unit price" value="${esc(it.price)}">
          </div>
        </div>`).join("")}
      <button class="btn btn-outline" id="wAddItem" type="button" style="width:100%"><span class="mi">add</span>Add item</button>
    </div>

    <div class="card"><h4 class="card-title">Pricing</h4>
      <div class="kv-row" id="wSubtotalRow" ${itemsSubtotal(dr) > 0 ? "" : "hidden"}>
        <span class="k">Items subtotal</span><span class="v" id="wSubtotal">${peso(itemsSubtotal(dr))}</span>
      </div>
      <div class="field-row">
        <span><p class="field-label">Shipping</p>
          <input class="field" id="wShip" data-fk="shipping" inputmode="decimal" placeholder="0.00" value="${esc(dr.shipping)}">
          <p class="field-error" data-err="shipping" hidden></p></span>
        <span><p class="field-label">Discount</p>
          <input class="field" id="wDisc" data-fk="discount" inputmode="decimal" placeholder="0.00" value="${esc(dr.discount)}">
          <p class="field-error" data-err="discount" hidden></p></span>
      </div>
      <p class="field-label">Total <span class="req">*</span></p>
      <input class="field" id="wTotal" data-fk="total" inputmode="decimal" placeholder="0.00" value="${esc(dr.total)}">
      <p class="field-hint" data-hint="total" hidden></p>
      <p class="field-error" data-err="total" hidden></p>
    </div>

    <div class="card"><h4 class="card-title">Payment</h4>
      ${PAY_METHODS.map(m => `
        <label class="radio-card"><input type="radio" name="wPay" value="${m}" ${dr.paymentMethod === m ? "checked" : ""}>
        <span>${m}${m === "COD" ? "<small>Cash on delivery — prepare exact amount</small>" : ""}</span></label>`).join("")}
      <p class="field-label">Payment status</p>
      <div class="segmented" id="wPayStatus">
        ${PAY_STATUSES.map(s => `<button class="seg ${dr.paymentStatus === s ? "is-on" : ""}" data-ps="${s}" type="button">${s === "UNPAID" ? "Unpaid" : s === "PAID" ? "Paid" : "Partial"}</button>`).join("")}
      </div>
      <div class="banner warn" id="wCodBanner" style="margin-top:14px" ${dr.isCod && parseMoney(dr.total) != null ? "" : "hidden"}><span class="mi">payments</span>COD amount to prepare:<strong id="wCodAmt">&nbsp;${peso(parseMoney(dr.total) ?? 0)}</strong></div>
    </div>

    <div class="card"><h4 class="card-title">Delivery</h4>
      <p class="field-label">Order status</p>
      <select class="field" id="wStatus">${Object.entries(STATUSES).filter(([k]) => k !== "DRAFT").map(([k, v]) => `<option value="${k}" ${dr.status === k ? "selected" : ""}>${v.label}</option>`).join("")}</select>
      <p class="field-label">Courier</p>
      <select class="field" id="wCourier"><option value="">Not set</option>${COURIERS.map(c => `<option ${dr.courier === c ? "selected" : ""}>${c}</option>`).join("")}</select>
      <div class="custom-plat" ${dr.courier === "Other" ? "" : "hidden"}>
        <p class="field-label">Courier name <span class="req">*</span></p>
        <input class="field" id="wCustomCourier" data-fk="customCourierName" placeholder="e.g. Lalamove, UPS" value="${esc(dr.customCourierName)}">
        <p class="field-error" data-err="customCourierName" hidden></p>
      </div>
      <p class="field-label">Tracking number</p>
      <input class="field" id="wTracking" placeholder="e.g. JT824011592PH" value="${esc(dr.tracking)}">
      <p class="field-label">Order date</p>
      <input class="field" id="wOrdered" type="date" value="${dr.orderedAt}">
      <div class="field-row">
        <span><p class="field-label">Earliest delivery</p><input class="field" id="wEtaA" type="date" value="${dr.etaStart}"></span>
        <span><p class="field-label">Latest delivery</p>
          <input class="field" id="wEtaB" data-fk="etaEnd" type="date" value="${dr.etaEnd}">
          <p class="field-error" data-err="etaEnd" hidden></p></span>
      </div>
      <p class="field-label">Reminders</p>
      <div class="chip-row" style="padding-top:0" id="wRemindChips">
        ${[["dayof", "On delivery day"], ["minus1", "1 day before"], ["minus2", "2 days before"]].map(([k, l]) =>
          `<button class="chip ${dr.reminders?.includes(k) ? "is-on" : ""}" data-rem="${k}" type="button">${l}</button>`).join("")}
      </div>
    </div>

    <div class="card"><h4 class="card-title">Notes</h4>
      <textarea class="field" id="wNotes" rows="3" placeholder="e.g. Rider calls first">${esc(dr.notes)}</textarea>
    </div>

    <div class="card"><h4 class="card-title">Categories</h4>
      <div class="chip-row" style="padding-top:0" id="wCatChips">
        ${allCats().map(c => `<button class="chip ${dr.categories.includes(c) ? "is-on" : ""}" data-cat="${esc(c)}" type="button">${esc(c)}</button>`).join("")}
      </div>
      <div class="field-row" style="margin-top:8px">
        <span style="flex:1"><input class="field" id="wNewCat" placeholder="New category name"></span>
        <button class="btn btn-tonal" id="wAddCat" type="button"><span class="mi">add</span>Add</button>
      </div>
    </div>

    <div class="card"><h4 class="card-title">Product image</h4>
      <label class="import-photo" id="wImgPick" ${dr.image ? "hidden" : ""}>
        <input type="file" accept="image/*" id="wImgFile">
        <span class="mi">add_photo_alternate</span><strong>Pick image</strong><br><small>Shown on this order — stays on device</small>
      </label>
      <div id="wImgView" ${dr.image ? "" : "hidden"} style="display:flex;align-items:center;gap:10px">
        <img id="wImgThumb" src="${dr.image || ""}" alt="" style="width:44px;height:44px;border-radius:10px;object-fit:cover">
        <span style="flex:1;font-size:13px;color:var(--text-2)">Image attached</span>
        <button class="remove-item" id="wImgRm" type="button" aria-label="Remove image"><span class="mi">close</span></button>
      </div>
    </div>
    <div class="scroll-pad"></div></div>`;
  wireEditInputs();
  updateErrorUI();
}

/* smart-import pane inside the editor — pick → OCR → values merge into the
   SAME manual draft (fill-blanks); the manual form then shows for review */
function renderEditImport(body) {
  body.innerHTML = `<div class="fade-in">
    ${entrySeg()}
    <div class="banner info" style="margin-top:10px"><span class="mi">photo_scan</span>
      Import a marketplace screenshot — on-device OCR extracts the details for your review. Nothing saves until you confirm.
    </div>
    <label class="import-photo" style="margin-top:14px">
      <input type="file" accept="image/*" id="wImportFile">
      <span class="mi">add_photo_alternate</span>
      <strong>Pick a screenshot</strong><br><small>Shopee, TikTok Shop, Lazada or any order page</small>
    </label>
    <button class="btn btn-tonal" type="button" style="width:100%;margin-top:12px" id="wImportSample">
      <span class="mi">auto_awesome</span>Try with sample screenshot data
    </button>
    <div class="scroll-pad"></div></div>`;
  $("#wImportFile").onchange = simulateImport;
  $("#wImportSample").onclick = simulateImport;
  wireEntrySeg();
}

/* extracted values -> SAME manual draft (fill-blanks) -> manual form shows
   the imported-notice chips; user reviews identical fields before saving */
function importToDraft() {
  const f = importDraft, blank = v => !v || !String(v).trim();
  if (!blank(f.name)) {
    if (blank(draft.title)) draft.title = f.name;
    if (draft.items.every(itemEmpty)) {
      draft.items = [{ name: f.name, variant: "", qty: "1",
        price: f.totalMinor != null ? (f.totalMinor / 100).toFixed(2) : "" }];
    }
  }
  if (blank(draft.sellerName) && f.seller) draft.sellerName = f.seller;
  if (!draft.platform && f.platform) draft.platform = f.platform;
  if (blank(draft.total) && f.totalMinor != null) {
    draft.total = (f.totalMinor / 100).toFixed(2);
  }
  if (!draft.courier && f.courier) {
    draft.courier = COURIERS.includes(f.courier) ? f.courier : "Other";
    draft.customCourierName = COURIERS.includes(f.courier) ? "" : f.courier;
  }
  if (blank(draft.tracking) && f.tracking) draft.tracking = f.tracking;
  importStep = 0;
  editMode = "manual"; editStep = "form"; importNotice = true;
  renderEdit();
}

function collectDraft() {
  const dr = draft, gv = id => document.getElementById(id);
  if (gv("wCustomPlat")) dr.customPlatformName = gv("wCustomPlat").value;
  if (gv("wTitle")) dr.title = gv("wTitle").value;
  if (gv("wSeller")) dr.sellerName = gv("wSeller").value;
  dr.items.forEach((it, i) => {
    const q = k => document.querySelector(`[data-item="${i}"][data-k="${k}"]`);
    if (q("name")) {
      it.name = q("name").value;
      it.variant = q("variant").value;
      it.qty = q("qty").value;
      it.price = q("price").value;
    }
  });
  if (gv("wShip")) dr.shipping = gv("wShip").value;
  if (gv("wDisc")) dr.discount = gv("wDisc").value;
  if (gv("wTotal")) dr.total = gv("wTotal").value;
  const pay = document.querySelector('input[name="wPay"]:checked');
  if (pay) { dr.paymentMethod = pay.value; dr.isCod = pay.value === "COD"; }
  if (gv("wCourier")) dr.courier = gv("wCourier").value;
  if (gv("wCustomCourier")) dr.customCourierName = gv("wCustomCourier").value;
  if (gv("wTracking")) dr.tracking = gv("wTracking").value;
  if (gv("wOrdered")) dr.orderedAt = gv("wOrdered").value || d(0);
  if (gv("wEtaA")) { dr.etaStart = gv("wEtaA").value; dr.etaEnd = gv("wEtaB").value; }
  if (gv("wNotes")) dr.notes = gv("wNotes").value;
  if (gv("wStatus")) dr.status = gv("wStatus").value;
}

function wireEntrySeg() {
  document.querySelectorAll("#entrySeg .seg").forEach(b => b.onclick = () => {
    if (editMode === b.dataset.em) return;
    collectDraft();
    editMode = b.dataset.em;
    renderEdit();
  });
}

function wireEditInputs() {
  const dr = draft, body = $("#editBody");
  wireEntrySeg();
  body.querySelectorAll("[data-plat]").forEach(b => b.onclick = () => {
    collectDraft(); dr.platform = b.dataset.plat; renderEdit();
  });
  body.querySelectorAll("[data-rm]").forEach(b => b.onclick = () => {
    collectDraft(); dr.items.splice(+b.dataset.rm, 1);
    editTouched = new Set([...editTouched].filter(k => !k.startsWith("item_")));
    renderEdit();
  });
  const add = $("#wAddItem");
  if (add) add.onclick = () => {
    collectDraft(); dr.items.push({ name: "", variant: "", qty: "1", price: "" });
    renderEdit();
  };
  document.querySelectorAll("#wPayStatus .seg").forEach(b => b.onclick = () => {
    dr.paymentStatus = b.dataset.ps;
    document.querySelectorAll("#wPayStatus .seg").forEach(x => x.classList.toggle("is-on", x === b));
  });
  document.querySelectorAll('input[name="wPay"]').forEach(r => r.onchange = () => {
    dr.paymentMethod = r.value; dr.isCod = r.value === "COD"; collectDraft(); renderEdit();
  });
  document.querySelectorAll("[data-rem]").forEach(b => b.onclick = () => {
    dr.reminders = dr.reminders || [];
    dr.reminders = dr.reminders.includes(b.dataset.rem)
      ? dr.reminders.filter(x => x !== b.dataset.rem)
      : [...dr.reminders, b.dataset.rem];
    b.classList.toggle("is-on");
  });
  body.querySelectorAll("[data-cat]").forEach(b => b.onclick = () => {
    const c = b.dataset.cat;
    dr.categories = dr.categories.includes(c)
      ? dr.categories.filter(x => x !== c)
      : [...dr.categories, c];
    b.classList.toggle("is-on");
  });
  const dismiss = $("#wImportDismiss");
  if (dismiss) dismiss.onclick = () => { importNotice = false; importDraft = null; renderEdit(); };
  const addCat = $("#wAddCat");
  if (addCat) addCat.onclick = () => {
    collectDraft();
    const name = $("#wNewCat").value.trim();
    if (name && !allCats().includes(name)) {
      sessionCats.push(name);
      dr.categories = [...dr.categories, name];
      renderEdit();
    }
  };
  const imgFile = $("#wImgFile");
  if (imgFile) imgFile.onchange = () => {
    collectDraft();
    if (imgFile.files[0]) { dr.image = URL.createObjectURL(imgFile.files[0]); renderEdit(); }
  };
  const imgRm = $("#wImgRm");
  if (imgRm) imgRm.onclick = () => { dr.image = null; renderEdit(); };
  const courierSel = $("#wCourier");
  if (courierSel) courierSel.onchange = () => { collectDraft(); dr.courier = courierSel.value; renderEdit(); };
  const etaA = $("#wEtaA");
  if (etaA) etaA.addEventListener("change", () => { collectDraft(); updateErrorUI(); });
  /* validate on leaving a field: blur (focusout) for text, change for selects/dates */
  body.querySelectorAll("[data-fk]").forEach(el => {
    el.addEventListener("focusout", () => touchField(el.dataset.fk));
    el.addEventListener("change", () => touchField(el.dataset.fk));
  });
  /* re-validate visible errors while typing + live totals */
  body.querySelectorAll("input.field, textarea.field").forEach(el => el.addEventListener("input", () => {
    if (el.dataset.k === "qty") el.value = el.value.replace(/\D/g, "");
    collectDraft(); updateErrorUI();
    const sub = itemsSubtotal(dr), row = $("#wSubtotalRow");
    if (row) { row.hidden = sub <= 0; $("#wSubtotal").textContent = peso(sub); }
    const cod = $("#wCodBanner");
    if (cod) {
      const tm = parseMoney(dr.total);
      cod.hidden = !(dr.isCod && tm != null);
      $("#wCodAmt").textContent = " " + peso(tm ?? 0);
    }
  }));
}

/* mirrors goToReview: attemptedSave=true -> valid? REVIEW : stay on form with all errors */
function goReview() {
  collectDraft();
  editAttempted = true;
  if (Object.keys(fieldErrors(draft)).length === 0) {
    editStep = "review"; renderEdit(); return;
  }
  renderEdit();
  document.querySelector("#editBody .is-error")?.scrollIntoView({ block: "center", behavior: "smooth" });
}

function editNext() {
  if (editMode === "import") return;
  if (editStep === "form") { goReview(); return; }
  collectDraft();
  editAttempted = true;
  if (Object.keys(fieldErrors(draft)).length) { editStep = "form"; renderEdit(); return; }
  const dr = draft;
  const existing = editOrderId ? orders.find(x => x.id === editOrderId) : null;
  const o = existing || {
    id: `ord-${nextId++}`, shipStatus: null, deliveredAt: null, archived: false, createdAt: d(0),
  };
  const ship = parseMoney(dr.shipping), disc = parseMoney(dr.discount);
  Object.assign(o, {
    platform: dr.platform, customPlatformName: dr.customPlatformName.trim(),
    title: dr.title.trim() || null,
    sellerName: dr.sellerName.trim() || platOf({ platform: dr.platform }).label,
    isCod: dr.isCod, paymentMethod: dr.paymentMethod, paymentStatus: dr.paymentStatus,
    status: dr.status,
    courier: dr.courier === "Other" ? dr.customCourierName.trim() : dr.courier,
    tracking: dr.tracking.trim(),
    orderedAt: dr.orderedAt, etaStart: dr.etaStart, etaEnd: dr.etaEnd,
    items: dr.items.filter(i => !itemEmpty(i)).map(i => ({
      name: i.name.trim(), variant: i.variant.trim(),
      qty: Math.max(1, parseInt(i.qty, 10) || 1),
      priceMinor: parseMoney(i.price) ?? 0,
    })),
    shippingMinor: ship ?? 0, discountMinor: disc ?? 0, totalMinor: parseMoney(dr.total),
    notes: dr.notes.trim(), reminders: dr.reminders,
    categories: dr.categories, image: dr.image,
  });
  if (dr.courier || dr.tracking) o.shipStatus = o.shipStatus || "PREPARING"; else o.shipStatus = null;
  if (!existing) orders.unshift(o);
  draft = null; editOrderId = null;
  state.detailId = o.id;
  showScreen("detail");
  toast(existing ? "Order updated" : "Order saved");
}

function editBack() {
  if (editMode === "import") { editMode = "manual"; renderEdit(); return; }
  if (editStep === "review") { editStep = "form"; renderEdit(); return; }
  draft = null;
  showScreen(editOrderId ? "detail" : "home");
  editOrderId = null;
}

/* ---------- shipment timeline (mirrors ShipmentTimelineScreen) ---------- */
function renderTimeline() {
  const o = orders.find(x => x.id === state.detailId);
  const body = $("#timelineBody");
  if (!o) { body.innerHTML = `<div class="empty"><p>No parcel selected.</p></div>`; return; }
  const ship = o.shipStatus ? SHIP_STATUSES[o.shipStatus] : null;
  const step = ship ? ship.step : -1;
  /* SCREEN_SPECS hierarchy: status chip hero → ETA → order summary + badges → stepper → events → courier → tracking+copy */
  body.innerHTML = `
    <div style="margin-top:10px;display:flex;flex-direction:column;gap:8px;align-items:flex-start">
      ${ship ? chipFor(o.shipStatus, SHIP_STATUSES, true) : `<span class="status-chip hero" style="color:var(--text-2);background:var(--surface-var)"><span class="mi">help</span>No shipment yet</span>`}
      <div class="parcel-eta"><span class="mi">event</span>${o.shipStatus === "DELIVERED" ? `Delivered ${fmtDate(o.deliveredAt)}` : o.etaStart ? `ETA ${fmtDate(o.etaStart)} – ${fmtDate(o.etaEnd)}` : "No ETA set"}</div>
    </div>
    <div class="card" style="margin-top:10px">
      <div style="display:flex;gap:8px;align-items:center;flex-wrap:wrap;margin-bottom:8px">${platformBadge(o)}${courierBadge(o)}</div>
      <div class="item-name" style="font-size:15px">${esc(itemsSummary(o))}</div>
      <div class="order-sub">${esc(o.sellerName)} · Ordered ${fmtDate(o.orderedAt)} · ${peso(orderTotal(o))}</div>
    </div>
    ${ship && step >= 0 ? `
      <div class="card"><h4 class="card-title">Tracking</h4>
        <ul class="timeline">${TIMELINE_STEPS.map((label, i) => {
          const cls = i < step ? "done" : i === step ? "now" : "";
          const date = i === step ? `<div class="tl-date">${o.shipStatus === "DELIVERED" ? fmtDate(o.deliveredAt) : "Current status"}</div>` : "";
          return `<li class="${cls}"><span class="tl-dot"><span class="mi">check</span></span><span><div class="tl-name">${label}</div>${date}</span></li>`;
        }).join("")}</ul>
      </div>` : `
      <div class="banner warn" style="margin-top:10px"><span class="mi">info</span>
        ${!ship ? "Statuses appear here as updates are detected or added — manual update is always available."
          : ship.step < 0 ? `${ship.label}.` : ""}
      </div>`}
    <div class="card"><h4 class="card-title">Courier</h4>
      <div class="kv-row"><span class="k">Courier</span><span class="v">${courierBadge(o) || "Not set"}</span></div>
      <div class="kv-row"><span class="k">Tracking</span><span class="v">${o.tracking ? `<span class="mono">${esc(o.tracking)}</span> <button class="copy-btn" type="button" aria-label="Copy tracking number" onclick="navigator.clipboard?.writeText('${esc(o.tracking)}');toast('Tracking number copied')"><span class="mi">content_copy</span></button>` : "—"}</span></div>
      <div class="kv-row"><span class="k">Source</span><span class="v">Set manually · updated ${fmtDate(o.createdAt)}</span></div>
    </div>
    <div class="action-col">
      <button class="btn btn-outline" type="button" onclick="updateShipStatus('${o.id}')">Update shipment status</button>
      <button class="btn btn-tonal" type="button" data-goto="detail"><span class="mi">arrow_back</span>Back to order</button>
    </div>
    <div class="scroll-pad"></div>`;
}

/* ---------- COD overview (mirrors CodOverviewScreen) ---------- */
function renderCod() {
  const due = orders.filter(isPendingCod);
  const total = due.reduce((s, o) => s + orderTotal(o), 0);
  const today = due.filter(o => (o.etaEnd || o.etaStart) === d(0));
  const soon = due.filter(o => o.etaEnd && o.etaEnd > d(0) && o.etaEnd <= d(3));
  $("#codBody").innerHTML = `
    <div class="stat-row" style="grid-template-columns:1fr 1fr 1fr;margin-top:10px">
      <div class="stat-card"><div class="stat-num">${due.length}</div><div class="stat-label">Pending COD</div></div>
      <div class="stat-card"><div class="stat-num">${today.length}</div><div class="stat-label">Due today</div></div>
      <div class="stat-card"><div class="stat-num money">${peso(total)}</div><div class="stat-label">Total due</div></div>
    </div>
    <div class="banner warn" style="margin-top:12px"><span class="mi">payments</span>Prepare exact cash — dates are estimates, not confirmed arrivals.</div>
    <div class="section-head" style="margin-top:14px"><h3>Due today</h3></div>
    <div class="stack">${today.length ? today.map(orderCard).join("") : `<div class="empty"><span class="mi">event_available</span><p>Nothing due today.</p></div>`}</div>
    <div class="section-head" style="margin-top:14px"><h3>Next 3 days</h3></div>
    <div class="stack">${soon.length ? soon.map(orderCard).join("") : `<div class="empty"><span class="mi">event_available</span><p>Nothing due soon.</p></div>`}</div>
    <div class="section-head" style="margin-top:14px"><h3>All pending COD</h3></div>
    <div class="stack">${due.length ? due.map(orderCard).join("") : `<div class="empty"><span class="mi">done_all</span><p>All COD is settled.</p></div>`}</div>
    <div class="scroll-pad"></div>`;
}

/* ---------- backup / export (Phase 8) ---------- */

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
      Detected from notifications on this device. High-confidence matches apply automatically — anything uncertain waits here for your review.
    </div>
    ${detections.length ? `<div class="stack" style="margin-top:12px">${detections.map(det => {
      const o = orders.find(x => x.id === det.orderId);
      return `<div class="card">
        <div style="display:flex;justify-content:space-between;align-items:center;gap:8px">
          <strong style="font-size:14px">${esc(SHIP_STATUSES[det.status]?.label || det.status)}</strong>
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
  applyShipStatus(o, det.status); // det.status is a ShipmentStatus
  detections = detections.filter(x => x.id !== id);
  renderDetections(); renderAppbar();
  toast(`${esc(SHIP_STATUSES[det.status].label)} applied to ${itemsSummary(o)}`);
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
        <input type="file" accept="image/*" id="importFile" multiple>
        <span class="mi">add_photo_alternate</span>
        <strong>Pick screenshots</strong><br><small>Shopee, TikTok Shop, Lazada — select up to 5 order pages</small>
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
  toast("OCR extracted 5 fields");
  /* add-screen import merges into the SAME manual draft; standalone import
     keeps its own review card */
  if (state.screen === "add") { importToDraft(); return; }
  importStep = 1; renderImport();
}

function saveImport() {
  const o = {
    id: `ord-${nextId++}`,
    platform: $("#iPlat").value, sellerName: $("#iSeller").value || "Imported order",
    isCod: true, paymentMethod: "COD", paymentStatus: "UNPAID", status: "ORDERED", shipStatus: null,
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
    if (state.screen === "add") { editBack(); return; }
    if (state.screen === "timeline") { showScreen("detail"); return; }
    showScreen(state.detailId && orders.some(o => o.id === state.detailId) && state.screen === "detail" ? "orders" : "home");
    return;
  }
  const mi = e.target.closest("[data-mi]");
  if (mi) { const it = $("#popupMenu")._items?.[+mi.dataset.mi]; closeMenu(); it?.fn?.(); return; }
  if (!e.target.closest("#popupMenu") && menuAnchor && !e.target.closest(".icon-btn")) closeMenu();
});

$("#fabAdd").addEventListener("click", () => showScreen("add", { fresh: true }));
$("#editNext").addEventListener("click", editNext);
$("#editBack").addEventListener("click", editBack);
$("#searchInput").addEventListener("input", e => { state.query = e.target.value; renderOrders(); });
$("#searchClear").addEventListener("click", () => { $("#searchInput").value = ""; state.query = ""; renderOrders(); });
$("#filtersBtn").addEventListener("click", openFiltersDialog);
$("#dialogOverlay").addEventListener("click", e => { if (e.target === e.currentTarget) closeDialog(); });
document.addEventListener("keydown", e => { if (e.key === "Escape") { closeDialog(); closeMenu(); } });
$("#dailySummaryToggle").addEventListener("change", e =>
  toast(e.target.checked ? "Daily summary on" : "Daily summary off"));
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

/* ---------- Ordi chat (companion surface — answers reflect local data only, mirroring the "no invented updates" rule) ---------- */
const ORDI_PROMPTS = ["What's arriving?", "COD to prepare", "Anything late?", "Detected updates", "How much did I spend?"];
let chats = JSON.parse(lsGet("ordra_chats") || "[]"); // [{id,title,msgs:[{who,text}]}]
let chatIdx = -1;              // -1 = not started; index into chats once first message lands
let chatLog = [];              // active chat messages (mirrors chats[chatIdx].msgs)
let chatMode = "chat";         // "chat" | "history"

function saveChats() {
  if (chatIdx >= 0 && chats[chatIdx]) chats[chatIdx].msgs = chatLog;
  lsSet("ordra_chats", JSON.stringify(chats));
}
function startNewChat() {
  chatIdx = -1; chatLog = []; chatMode = "chat";
  const active = orders.filter(isActive);
  pushMsg("or", `Hey, I'm Ordi — your ORDRA AI companion. You've got ${active.length} active deliver${active.length === 1 ? "y" : "ies"} right now. What do you want to know?`, true);
}

function ordiReply(q) {
  const t = q.toLowerCase(), active = orders.filter(isActive), today = d(0);
  const soon = active.filter(o => o.etaEnd && o.etaEnd <= d(3)).sort((a, b) => a.etaEnd.localeCompare(b.etaEnd));
  const late = active.filter(o => o.etaEnd && o.etaEnd < today);
  const cod = orders.filter(isPendingCod);
  const codTotal = cod.reduce((s, o) => s + orderTotal(o), 0);

  if (/arriv|eta|when|deliver|coming/.test(t)) {
    if (!soon.length) return "Nothing is due in the next 3 days. Your next parcel isn't dated yet — add an ETA on its order and I'll watch it.";
    return `Arriving soon: ${soon.slice(0, 3).map(o => `${itemsSummary(o)} (${fmtDate(o.etaStart)}–${fmtDate(o.etaEnd)})`).join(", ")}. ${cod.length ? `${cod.length} of your parcels still need COD cash.` : ""}`;
  }
  if (/cod|cash|prepare|pay/.test(t))
    return cod.length
      ? `You owe ${peso(codTotal)} across ${cod.length} COD parcel${cod.length === 1 ? "" : "s"}. Biggest: ${itemsSummary(cod.sort((a, b) => orderTotal(b) - orderTotal(a))[0])} at ${peso(orderTotal(cod[0]))}. Have the exact amount ready.`
      : "No COD pending — everything due is already paid.";
  if (/late|delay|problem|attention|fail|stuck/.test(t))
    return late.length
      ? `${late.length} parcel${late.length === 1 ? "" : "s"} passed their ETA: ${late.map(o => itemsSummary(o)).join(", ")}. Worth checking with the seller or courier.`
      : "Nothing is past its ETA — all parcels are on schedule.";
  if (/detect|notif|update|scan|track/.test(t))
    return detections.length
      ? `${detections.length} detected update${detections.length === 1 ? "" : "s"} waiting for your review — I never apply ambiguous ones without you. Open Detected updates to confirm.`
      : "No pending detected updates. When a courier or marketplace notification matches a parcel, I'll suggest it here first.";
  if (/spend|spen|total|much|analytics|bought/.test(t)) {
    const mo = today.slice(0, 7);
    const spent = orders.filter(o => (o.orderedAt || "").startsWith(mo)).reduce((s, o) => s + orderTotal(o), 0);
    return `This month you've ordered ${peso(spent)} across ${orders.filter(o => (o.orderedAt || "").startsWith(mo)).length} orders. Full breakdown is in Spending analytics.`;
  }
  return `Right now you have ${active.length} active deliver${active.length === 1 ? "y" : "ies"}, ${peso(codTotal)} COD to prepare, and ${detections.length} update${detections.length === 1 ? "" : "s"} to review. Ask me about arrivals, COD, late parcels or spending.`;
}

function openChat() {
  $("#chatOverlay").hidden = false;
  $("#chatHead").hidden = true;
  if (chatMode === "chat" && chatIdx < 0 && !chatLog.length) startNewChat();
  $("#chatPrompts").innerHTML = ORDI_PROMPTS.map(p => `<button class="chip" type="button" data-qp="${esc(p)}">${esc(p)}</button>`).join("");
  renderChat();
}
function closeChat() { $("#chatOverlay").hidden = true; $("#chatHead").hidden = false; saveChats(); }
function pushMsg(who, text, silent) {
  chatLog.push({ who, text });
  if (!silent) {
    if (chatIdx < 0) {
      const title = who === "me" ? text.slice(0, 34) : "New chat";
      chats.unshift({ id: `c${Date.now()}`, title: who === "me" ? title : "New chat", msgs: [] });
      chatIdx = 0;
    }
    if (chats[chatIdx].title === "New chat" && who === "me") chats[chatIdx].title = text.slice(0, 34);
    saveChats();
  }
  if (chatMode === "chat") renderChat();
}
function renderChat() {
  const box = $("#chatMsgs");
  if (chatMode === "history") {
    box.innerHTML = chats.length
      ? chats.map((c, i) => `
        <button class="chat-hist" type="button" data-chat="${i}">
          <span class="mi">forum</span>
          <span style="flex:1;min-width:0;text-align:left"><strong>${esc(c.title || "Chat")}</strong>
          <small>${c.msgs.length} message${c.msgs.length === 1 ? "" : "s"}</small></span>
        </button>`).join("")
      : `<div class="empty"><span class="mi">forum</span><p>No past chats yet.</p></div>`;
    $("#chatPrompts").innerHTML = "";
    return;
  }
  box.innerHTML = chatLog.map(m =>
    `<div class="chat-msg ${m.who === "me" ? "me" : "or"}">${m.who === "or" ? `<span class="chat-avatar">${ordiSvg("idle")}</span>` : ""}<p>${esc(m.text)}</p></div>`).join("");
  box.scrollTop = box.scrollHeight;
}
function toggleChatMode() {
  chatMode = chatMode === "chat" ? "history" : "chat";
  $("#chatHistBtn").classList.toggle("is-on", chatMode === "history");
  renderChat();
}

$("#chatHead").addEventListener("click", openChat);
$("#chatClose").addEventListener("click", closeChat);
$("#chatOverlay").addEventListener("click", e => { if (e.target === e.currentTarget) closeChat(); });
$("#chatForm").addEventListener("submit", e => {
  e.preventDefault();
  const q = $("#chatText").value.trim();
  if (!q) return;
  $("#chatText").value = "";
  pushMsg("me", q);
  setTimeout(() => pushMsg("or", ordiReply(q)), 350);
});
document.addEventListener("click", e => {
  const qp = e.target.closest("[data-qp]");
  if (qp) { pushMsg("me", qp.dataset.qp); setTimeout(() => pushMsg("or", ordiReply(qp.dataset.qp)), 350); }
  const hc = e.target.closest("[data-chat]");
  if (hc) {
    chatIdx = +hc.dataset.chat;
    chatLog = chats[chatIdx]?.msgs || [];
    chatMode = "chat";
    $("#chatHistBtn").classList.remove("is-on");
    $("#chatPrompts").innerHTML = ORDI_PROMPTS.map(p => `<button class="chip" type="button" data-qp="${esc(p)}">${esc(p)}</button>`).join("");
    renderChat();
  }
});
$("#chatHead").innerHTML = ordiSvg("idle");
$("#chatOrdi").innerHTML = ordiSvg("happy");

/* ---------- about (mirrors AboutScreen) ---------- */
function renderAbout() {
  $("#aboutBody").innerHTML = `
    <div class="card" style="margin-top:10px;text-align:center">
      <svg class="about-logo" viewBox="0 0 512 512" aria-hidden="true" style="margin:8px auto">
        <rect width="512" height="512" rx="120" fill="url(#lg)"/>
        <circle cx="256" cy="256" r="132" fill="none" stroke="#fff" stroke-width="42"/>
        <path d="M194 238 L256 202 L318 238 L318 310 L256 346 L194 310 Z" fill="none" stroke="#fff" stroke-width="24" stroke-linejoin="round"/>
        <path d="M224 273 L250 299 L300 247" fill="none" stroke="#35D0A0" stroke-width="22" stroke-linecap="round" stroke-linejoin="round"/>
      </svg>
      <strong style="font-size:17px">Ordra</strong>
      <p class="card-sub" style="margin-top:2px">Version 0.1.0</p>
    </div>
    <div class="card"><h4 class="card-title">Privacy</h4>
      <p class="card-sub" style="font-size:13.5px;line-height:1.55">Ordra keeps orders, tracking numbers and receipts on your device. There is no account, no cloud sync, and no analytics collection. Smart Import can optionally use Groq to interpret screenshot text — nothing is sent without you asking.</p>
    </div>
    <div class="card"><h4 class="card-title">Ordi &amp; ORDRA AI</h4>
      <div style="display:flex;gap:12px;align-items:center">
        ${ordiSvg("happy")}
        <p class="card-sub" style="font-size:13.5px;line-height:1.55;margin:0">Ordi is the companion face of ORDRA AI, Ordra's optional smart layer for features like screenshot import. Ordi only reflects your real order data — it never invents tracking updates or order details.</p>
      </div>
    </div>
    <div class="card"><h4 class="card-title">Licenses</h4>
      <p class="card-sub" style="font-size:13.5px;line-height:1.55">Built with Jetpack Compose, Room, Hilt, WorkManager, ML Kit and Coil. Open-source licenses apply to those libraries.</p>
    </div>
    <div class="scroll-pad"></div>`;
}

/* ---------- onboarding (mirrors OnboardingScreen — first run only) ---------- */
const OB_PAGES = [
  { title: "Every order, one place", body: "Save purchases from Shopee, TikTok Shop, Lazada and anywhere else — manually, no account needed. Ordi, your ORDRA AI companion, helps you spot what needs attention.", or: "happy" },
  { title: "Know what to prepare", body: "Track statuses, ETAs and COD totals so cash is ready when the rider arrives.", or: "delivery" },
  { title: "Local-first and private", body: "Your data stays on this device. Reminders and analytics work fully offline.", or: "idle" },
];
let obPage = 0;
function renderOnboard() {
  const p = OB_PAGES[obPage];
  $("#obPages").innerHTML = `
    <div class="onboard-page">
      ${ordiSvg(p.or)}
      <h2>${esc(p.title)}</h2>
      <p>${esc(p.body)}</p>
      ${obPage === OB_PAGES.length - 1 ? `
        <input class="field" id="obKey" type="password" placeholder="Groq API key — optional" style="margin-top:16px" autocomplete="off">
        <p style="font-size:11.5px;color:var(--text-2);margin-top:6px">Paste a Groq key to enable online AI analysis for Smart Import. It stays on this device. Leave blank for fully local mode.</p>` : ""}
    </div>`;
  $("#obDots").innerHTML = OB_PAGES.map((_, i) => `<span class="ob-dot ${i === obPage ? "is-on" : ""}"></span>`).join("");
  $("#obNext").textContent = obPage === OB_PAGES.length - 1 ? "Get started" : "Next";
}
function finishOnboard() {
  const key = $("#obKey")?.value.trim();
  if (key) { lsSet("ordra_groq_key", key); const g = $("#groqKey"); if (g) g.value = key; }
  lsSet("ordra_onboarded", "1");
  $("#onboard").hidden = true;
}
$("#obSkip").addEventListener("click", finishOnboard);
$("#obNext").addEventListener("click", () => {
  if (obPage === OB_PAGES.length - 1) return finishOnboard();
  obPage++; renderOnboard();
});
if (!lsGet("ordra_onboarded")) { $("#onboard").hidden = false; renderOnboard(); }

applyTheme("light");
showScreen("home");
