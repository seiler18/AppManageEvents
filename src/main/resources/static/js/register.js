document.getElementById('registerForm').addEventListener('submit', function (e) {
    e.preventDefault();

    fetch('/api/auth/register', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({
            username: document.getElementById('username').value,
            password: document.getElementById('password').value
        })
    })
        .then(response => response.text())
        .then(data => {
            alert(data);
            if (data === "User registered successfully!") {
                window.location.href = '/login';
            }
        })
        .catch((error) => {
            console.error('Error:', error);
        });
});