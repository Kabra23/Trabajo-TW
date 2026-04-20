// Script de validación de formulario de registro
document.addEventListener('DOMContentLoaded', function() {

    // Toggle password visibility
    const togglePasswordButtons = document.querySelectorAll('.toggle-password');
    togglePasswordButtons.forEach(button => {
        button.addEventListener('click', function() {
            const targetId = this.getAttribute('data-target');
            const passwordInput = document.getElementById(targetId);
            const icon = this.querySelector('span');

            if (passwordInput && passwordInput.type === 'password') {
                passwordInput.type = 'text';
                icon.innerHTML = '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path><circle cx="12" cy="12" r="3"></circle></svg>';
            } else if (passwordInput) {
                passwordInput.type = 'password';
                icon.innerHTML = '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="1" y1="1" x2="23" y2="23"></line><path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"></path></svg>';
            }
        });
    });

    // Form validation
    const registerForm = document.getElementById('registerForm');

    if (registerForm) {
        registerForm.addEventListener('submit', function(e) {
            e.preventDefault();

            const nombre = document.getElementById('nombre').value.trim();
            const apellidos = document.getElementById('apellidos').value.trim();
            const email = document.getElementById('email').value.trim();
            const password = document.getElementById('passwordRegistro').value;
            const confirmPassword = document.getElementById('confirmPassword').value;

            let isValid = true;

            // Clear previous errors
            document.querySelectorAll('.error-message').forEach(error => {
                error.textContent = '';
                error.style.display = 'none';
            });

            document.querySelectorAll('input').forEach(input => {
                input.classList.remove('error');
            });

            // Validate nombre
            if (!nombre) {
                showError('nombreError', 'El nombre es obligatorio');
                document.getElementById('nombre').classList.add('error');
                isValid = false;
            } else if (nombre.length < 2) {
                showError('nombreError', 'El nombre debe tener al menos 2 caracteres');
                document.getElementById('nombre').classList.add('error');
                isValid = false;
            }

            // Validate apellidos
            if (!apellidos) {
                showError('apellidosError', 'Los apellidos son obligatorios');
                document.getElementById('apellidos').classList.add('error');
                isValid = false;
            } else if (apellidos.length < 2) {
                showError('apellidosError', 'Los apellidos deben tener al menos 2 caracteres');
                document.getElementById('apellidos').classList.add('error');
                isValid = false;
            }

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
            const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$/;
            if (!password) {
                showError('passwordRegistroError', 'La contraseña es obligatoria');
                document.getElementById('passwordRegistro').classList.add('error');
                isValid = false;
            } else if (!passwordRegex.test(password)) {
                showError('passwordRegistroError', 'La contraseña debe tener al menos 8 caracteres, mayúscula, minúscula, número y carácter especial');
                document.getElementById('passwordRegistro').classList.add('error');
                isValid = false;
            }

            // Validate confirm password
            if (!confirmPassword) {
                showError('confirmPasswordError', 'Debes confirmar tu contraseña');
                document.getElementById('confirmPassword').classList.add('error');
                isValid = false;
            } else if (password !== confirmPassword) {
                showError('confirmPasswordError', 'Las contraseñas no coinciden');
                document.getElementById('confirmPassword').classList.add('error');
                isValid = false;
            }

            if (isValid) {
                // Si todo es válido, enviar el formulario
                this.submit();
            } else {
                // Hacer scroll al primer error
                const firstError = document.querySelector('.error-message:not(:empty)');
                if (firstError) {
                    firstError.parentElement.scrollIntoView({ behavior: 'smooth', block: 'center' });
                }
            }
        });

        // Validación en tiempo real para la contraseña
        const passwordInput = document.getElementById('passwordRegistro');
        if (passwordInput) {
            passwordInput.addEventListener('input', function() {
                const password = this.value;
                const requirements = document.querySelector('.password-requirements');

                if (requirements) {
                    const hasLower = /[a-z]/.test(password);
                    const hasUpper = /[A-Z]/.test(password);
                    const hasNumber = /\d/.test(password);
                    const hasSpecial = /[@$!%*?&]/.test(password);
                    const isLongEnough = password.length >= 8;

                    if (isLongEnough && hasLower && hasUpper && hasNumber && hasSpecial) {
                        requirements.style.color = '#069c6f';
                        this.classList.remove('error');
                    } else if (password.length > 0) {
                        requirements.style.color = '#d32f2f';
                    } else {
                        requirements.style.color = '#666';
                    }
                }
            });
        }

        // Validación en tiempo real para confirmar contraseña
        const confirmPasswordInput = document.getElementById('confirmPassword');
        if (confirmPasswordInput && passwordInput) {
            confirmPasswordInput.addEventListener('input', function() {
                const password = passwordInput.value;
                const confirmPassword = this.value;
                const errorElement = document.getElementById('confirmPasswordError');

                if (confirmPassword.length > 0) {
                    if (password === confirmPassword) {
                        this.classList.remove('error');
                        if (errorElement) {
                            errorElement.textContent = 'Las contraseñas coinciden';
                            errorElement.style.color = '#069c6f';
                            errorElement.style.display = 'block';
                        }
                    } else {
                        this.classList.add('error');
                        if (errorElement) {
                            errorElement.textContent = 'Las contraseñas no coinciden';
                            errorElement.style.color = '#d32f2f';
                            errorElement.style.display = 'block';
                        }
                    }
                }
            });
        }
    }

    function showError(elementId, message) {
        const errorElement = document.getElementById(elementId);
        if (errorElement) {
            errorElement.textContent = message;
            errorElement.style.display = 'block';
            errorElement.style.color = '#d32f2f';
        }
    }
});

