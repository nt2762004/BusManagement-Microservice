// Simple client-side role guard. NOTE: Chỉ mang tính UI, không thay thế bảo mật backend.
// Logic: gọi /api/auth/me để lấy role; nếu thất bại fallback sessionStorage.
// Nếu không hợp lệ -> chuyển về /login hoặc /dashboard (dashboard dùng chung cho mọi role).

const GATEWAY_API = (typeof GATEWAY_BASE !== 'undefined') ? GATEWAY_BASE : 'http://localhost:8080';

const ROLE_DASHBOARD_MAP = {
  'ADMIN': '/dashboard',
  'CHỦ NHÀ XE': '/dashboard',
  'ĐIỀU HÀNH': '/dashboard',
  // TÀI XẾ: quay về dashboard
  'TÀI XẾ': '/dashboard'
};

const PAGE_REQUIRED_ROLE = {
  // Dashboard: cho phép tất cả role bao gồm TÀI XẾ
  '/dashboard': ['ADMIN', 'CHỦ NHÀ XE', 'ĐIỀU HÀNH', 'TÀI XẾ'],
  // Trang quản trị theo chức năng
  '/user_mana': 'ADMIN',
  '/settings': 'ADMIN',
  '/buses': ['ADMIN', 'CHỦ NHÀ XE'],
  '/drivers': 'CHỦ NHÀ XE',
  '/routes': 'CHỦ NHÀ XE',
  '/schedules': ['ĐIỀU HÀNH', 'TÀI XẾ', 'CHỦ NHÀ XE'],
  '/reports': ['ĐIỀU HÀNH', 'CHỦ NHÀ XE'],
  // Trang trips: tài xế không được truy cập
  '/trips': ['ĐIỀU HÀNH', 'CHỦ NHÀ XE']
};

async function fetchCurrentRole() {
  try {
    const res = await fetch(GATEWAY_API + '/api/auth/me', { credentials: 'include' });
    if (!res.ok) return null;
    const data = await res.json();
    if (data && data.success && data.role) {
      sessionStorage.setItem('role', data.role); // đồng bộ hóa
      try { window.__userRole = data.role; } catch(_) {}
      return data.role;
    }
  } catch (e) { /* network ignore */ }
  const role = sessionStorage.getItem('role');
  try { window.__userRole = role; } catch(_) {}
  return role;
}

async function enforceRoleGuard() {
  const path = window.location.pathname;
  // Nếu không phải trang cần guard (login / root) thì bỏ qua
  if (path === '/' || path === '/login') return;
  const required = PAGE_REQUIRED_ROLE[path];
  if (!required) {
    // Không cần guard theo trang, nhưng vẫn ẩn menu theo role nếu có
    const roleAny = await fetchCurrentRole();
    if (roleAny) { applyRoleMenuFilter(roleAny); applyDenyRoles(roleAny); }
    return;
  }
  const role = await fetchCurrentRole();
  if (!role) {
    window.location.replace('/login');
    return;
  }
  // Hỗ trợ required dạng string hoặc mảng
  const allowed = Array.isArray(required) ? required : [required];
  if (!allowed.includes(role)) {
    // Nếu ADMIN truy cập trang không được phép, điều hướng về /buses; các role khác về dashboard chung
    const target = (role === 'ADMIN') ? '/buses' : (ROLE_DASHBOARD_MAP[role] || '/login');
    if (target !== path) window.location.replace(target);
  } else {
    // Đã hợp lệ -> áp dụng filter menu theo role
    applyRoleMenuFilter(role);
    applyDenyRoles(role);
  }
}

document.addEventListener('DOMContentLoaded', enforceRoleGuard);

// Ẩn các mục sidebar không dành cho role hiện tại
function applyRoleMenuFilter(role) {
  try {
    // Nếu là tài xế: chỉ cho /dashboard và /schedules
    if (role === 'TÀI XẾ') {
      const allowed = new Set(['/dashboard','/schedules']);
      document.querySelectorAll('.nav-menu .nav-item a').forEach(a => {
        const href = a.getAttribute('href');
        if (!allowed.has(href)) {
          a.parentElement.style.pointerEvents = 'none';
          a.parentElement.style.opacity = '0.35';
          a.setAttribute('aria-disabled','true');
          if (!a.dataset._href) a.dataset._href = href || '';
          a.setAttribute('href','javascript:void(0)');
        } else {
          a.parentElement.style.pointerEvents = '';
          a.parentElement.style.opacity = '';
          a.removeAttribute('aria-disabled');
          if (a.dataset._href) { a.setAttribute('href', a.dataset._href); delete a.dataset._href; }
        }
      });
    }
    document.querySelectorAll('[data-roles]')?.forEach(el => {
      const allow = (el.getAttribute('data-roles') || '')
        .split(',')
        .map(s => s.trim())
        .filter(Boolean);
      if (allow.length && !allow.includes(role)) {
        // Disable thay vì ẩn: khóa tương tác và làm mờ
        el.style.pointerEvents = 'none';
        el.style.opacity = '0.5';
        el.setAttribute('aria-disabled', 'true');
        // Nếu là thẻ a bên trong, bỏ href để ngăn điều hướng
        const a = el.querySelector('a');
        if (a) { a.dataset._href = a.getAttribute('href') || ''; a.setAttribute('href', 'javascript:void(0)'); }
      } else {
        // Khôi phục khi hợp lệ
        el.style.pointerEvents = '';
        el.style.opacity = '';
        el.removeAttribute('aria-disabled');
        const a = el.querySelector('a');
        if (a && a.dataset._href != null) { a.setAttribute('href', a.dataset._href); a.removeAttribute('data-_href'); }
      }
    });
  } catch (e) { /* ignore */ }
}

// Disable các nút/chức năng theo vai trò bằng thuộc tính data-deny-roles="ROLE1, ROLE2"
function applyDenyRoles(role) {
  try {
    document.querySelectorAll('[data-deny-roles]')?.forEach(el => {
      const deny = (el.getAttribute('data-deny-roles') || '')
        .split(',')
        .map(s => s.trim())
        .filter(Boolean);
      const shouldDisable = deny.length && deny.includes(role);
      if (shouldDisable) {
        el.style.pointerEvents = 'none';
        el.style.opacity = '0.5';
        el.setAttribute('aria-disabled', 'true');
        if (typeof el.disabled !== 'undefined') el.disabled = true;
        const a = el.tagName === 'A' ? el : el.querySelector('a');
        if (a) { a.dataset._href = a.getAttribute('href') || ''; a.setAttribute('href', 'javascript:void(0)'); }
      } else {
        el.style.pointerEvents = '';
        el.style.opacity = '';
        el.removeAttribute('aria-disabled');
        if (typeof el.disabled !== 'undefined') el.disabled = false;
        const a = el.tagName === 'A' ? el : el.querySelector('a');
        if (a && a.dataset._href != null) { a.setAttribute('href', a.dataset._href); a.removeAttribute('data-_href'); }
      }
    });
  } catch (e) { /* ignore */ }
}

// Expose helper to re-apply when DOM updates
try { window.applyDenyRoles = applyDenyRoles; } catch(_) {}
