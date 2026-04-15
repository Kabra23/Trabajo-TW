// Script de validación de formulario de inicio de sesión - VERSIÓN THYMELEAF
// El formulario se envía al servidor Spring Security, NO usa window.location
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

    // Validación básica sin interceptar el submit real
    const loginForm = document.getElementById('loginForm');
    if (loginForm) {
        loginForm.addEventListener('submit', function(e) {
            // El campo se llama "username" pero el label dice "email" - validamos igual
            const emailInput = document.getElementById('username');
            const passwordInput = document.getElementById('password');
            let isValid = true;

            document.querySelectorAll('.error-message').forEach(err => {
                err.textContent = '';
                err.style.display = 'none';
            });

            if (emailInput && !emailInput.value.trim()) {
                showError('emailError', 'El correo electrónico es obligatorio');
                isValid = false;
            }

            if (passwordInput && !passwordInput.value) {
                showError('passwordError', 'La contraseña es obligatoria');
                isValid = false;
            }

            if (!isValid) {
                e.preventDefault();
            }
            // Si isValid=true, el form se envía a /login (Spring Security lo procesa)
        });
    }

    function showError(elementId, message) {
        const el = document.getElementById(elementId);
        if (el) { el.textContent = message; el.style.display = 'block'; }
    }
});
