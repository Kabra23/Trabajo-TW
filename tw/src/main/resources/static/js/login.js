// Script de validación de formulario de inicio de sesión
document.addEventListener('DOMContentLoaded', function() {

    // Toggle password visibility
    const togglePasswordButtons = document.querySelectorAll('.toggle-password');
    togglePasswordButtons.forEach(button => {
        button.addEventListener('click', function() {
            const passwordInput = document.getElementById('password');
            const icon = this.querySelector('span');

            if (passwordInput.type === 'password') {
                passwordInput.type = 'text';
                icon.innerHTML = '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path><circle cx="12" cy="12" r="3"></circle></svg>';
            } else {
                passwordInput.type = 'password';
                icon.innerHTML = '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="1" y1="1" x2="23" y2="23"></line><path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"></path></svg>';
            }
        });
    });

    // Form validation
    const loginForm = document.getElementById('loginForm');

    if (loginForm) {
        loginForm.addEventListener('submit', function(e) {
            e.preventDefault();

            const email = document.getElementById('email').value.trim();
            const password = document.getElementById('password').value;

            let isValid = true;

            // Clear previous errors
            document.querySelectorAll('.error-message').forEach(error => {
                error.textContent = '';
                error.style.display = 'none';
            });

            document.querySelectorAll('input').forEach(input => {
                input.classList.remove('error');
            });

            // Validate email
            if (!email) {
                showError('emailError', 'El correo electrónico es obligatorio');
                document.getElementById('email').classList.add('error');
                isValid = false;
            } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
                showError('emailError', 'Introduce un correo electrónico válido');
                document.getElementById('email').classList.add('error');
                isValid = false;
            }

            // Validate password
            if (!password) {
                showError('passwordError', 'La contraseña es obligatoria');
                document.getElementById('password').classList.add('error');
                isValid = false;
            } else if (password.length < 8) {
                showError('passwordError', 'La contraseña debe tener al menos 8 caracteres');
                document.getElementById('password').classList.add('error');
                isValid = false;
            }

            if (isValid) {
                // Guardar datos de sesión simulados
                const userData = {
                    email: email,
                    loginTime: new Date().toISOString()
                };

                localStorage.setItem('userSession', JSON.stringify(userData));

                // Mostrar mensaje de éxito
                showSuccessMessage('Inicio de sesión exitoso');

                // Redirigir después de 1 segundo
                setTimeout(() => {
                    window.location.href = '/detalle-restaurante';
                }, 1000);
            }
        });
    }

    function showError(elementId, message) {
        const errorElement = document.getElementById(elementId);
        if (errorElement) {
            errorElement.textContent = message;
            errorElement.style.display = 'block';
        }
    }

    function showSuccessMessage(message) {
        const form = document.getElementById('loginForm');
        const successDiv = document.createElement('div');
        successDiv.className = 'success-message';
        successDiv.textContent = message;
        successDiv.style.cssText = `
            background-color: #069c6f;
            color: white;
            padding: 1rem;
            border-radius: 8px;
            margin-top: 1rem;
            text-align: center;
            font-weight: 600;
        `;
        form.appendChild(successDiv);
    }
});

