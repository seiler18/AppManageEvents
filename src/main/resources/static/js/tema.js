// Se carga en <head>, antes de pintar: evita el destello de tema claro en
// quien eligió oscuro. localStorage puede fallar (modo privado); se ignora.
(function () {
  var guardado = null;
  try { guardado = localStorage.getItem('tema'); } catch (e) { /* sin almacenamiento */ }
  var oscuro = window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches;
  document.documentElement.setAttribute('data-bs-theme', guardado || (oscuro ? 'dark' : 'light'));
})();
