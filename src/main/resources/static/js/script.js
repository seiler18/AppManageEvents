function limpiarCampos(){
    window.location='/';
}

setTimeout(function() {
    var alertMessage = document.getElementById('alertMessage');
    if (alertMessage) {
        alertMessage.style.display = 'none';
    }
}, 2000); // 2000 ms = 2 segundos