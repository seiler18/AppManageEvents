// Todo el JS de la app vive aquí: la CSP no permite scripts en línea.
(function () {
  'use strict';

  var boton = document.getElementById('cambiar-tema');
  if (boton) {
    boton.addEventListener('click', function () {
      var raiz = document.documentElement;
      var nuevo = raiz.getAttribute('data-bs-theme') === 'dark' ? 'light' : 'dark';
      raiz.setAttribute('data-bs-theme', nuevo);
      try { localStorage.setItem('tema', nuevo); } catch (e) { /* sin almacenamiento */ }
    });
  }

  // Formularios destructivos: <form data-confirmar="¿Seguro?">
  document.querySelectorAll('form[data-confirmar]').forEach(function (f) {
    f.addEventListener('submit', function (ev) {
      if (!window.confirm(f.getAttribute('data-confirmar'))) {
        ev.preventDefault();
      }
    });
  });

  // Paso a Webpay: el token debe llegar por POST al formulario de Transbank.
  var webpay = document.getElementById('form-webpay');
  if (webpay) {
    webpay.submit();
  }

  // Total en vivo en la ficha del evento.
  var cantidad = document.getElementById('cantidad');
  var total = document.getElementById('total');
  if (cantidad && total) {
    var precio = parseInt(cantidad.getAttribute('data-precio'), 10) || 0;
    var formato = new Intl.NumberFormat('es-CL');
    var pintar = function () {
      var n = parseInt(cantidad.value, 10) || 0;
      total.textContent = precio === 0 ? 'Gratis' : '$' + formato.format(precio * n);
    };
    cantidad.addEventListener('input', pintar);
    pintar();
  }

  // Validación en la puerta: el campo de código queda listo para un lector.
  var codigo = document.getElementById('codigo-entrada');
  if (codigo && !codigo.value) {
    codigo.focus();
  }
})();
