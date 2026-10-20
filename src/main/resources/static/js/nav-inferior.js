// Marca el item activo de la cinta inferior: gana el enlace cuyo href es el
// prefijo mas largo de la ruta actual ("/" solo coincide exacto).
(function () {
  var path = location.pathname.replace(/\/+$/, '') || '/';
  var mejor = null, largo = -1;
  document.querySelectorAll('.nav-inferior a.nav-item').forEach(function (a) {
    var h = (a.getAttribute('href') || '').replace(/\/+$/, '') || '/';
    var ok = h === '/' ? path === '/' : path === h || path.indexOf(h + '/') === 0;
    if (ok && h.length > largo) { mejor = a; largo = h.length; }
  });
  if (mejor) { mejor.classList.add('activo'); mejor.setAttribute('aria-current', 'page'); }
})();
