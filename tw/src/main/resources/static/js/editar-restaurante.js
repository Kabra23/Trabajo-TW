// Script para funcionalidad de editar restaurante - VERSIÓN THYMELEAF
// El formulario ahora se envía al servidor (Spring MVC), NO usa localStorage
document.addEventListener('DOMContentLoaded', function() {

    // ========== FUNCIONALIDAD DE PESTAÑAS ==========
    const tabButtons = document.querySelectorAll('.tab-btn');
    const dishCards = document.querySelectorAll('.dish-edit-card');

    tabButtons.forEach(button => {
        button.addEventListener('click', function() {
            tabButtons.forEach(btn => btn.classList.remove('active'));
            this.classList.add('active');
            const selectedCategory = this.getAttribute('data-category');
            filterDishesByCategory(selectedCategory);
        });
    });

    function filterDishesByCategory(category) {
        dishCards.forEach(card => {
            if (category === 'destacados') {
                card.style.display = 'flex';
            } else {
                card.style.display = 'none';
            }
        });
    }

    // ========== VALIDACIÓN CLIENTE (sin interceptar submit) ==========
    const form = document.getElementById('editRestaurantForm');

    if (form) {
        form.addEventListener('submit', function(e) {
            let isValid = true;

            // Validar nombre
            const nombre = document.getElementById('nombre');
            if (nombre && nombre.value.trim() === '') {
                showError('nombreError', 'El nombre es obligatorio');
                isValid = false;
            } else {
                clearError('nombreError');
            }

            // Validar dirección
            const direccion = document.getElementById('direccion');
            if (direccion && direccion.value.trim() === '') {
                showError('direccionError', 'La dirección es obligatoria');
                isValid = false;
            } else {
                clearError('direccionError');
            }

            // Validar teléfono
            const telefono = document.getElementById('telefono');
            if (telefono) {
                const telefonoPattern = /^[+]?[0-9\s-]+$/;
                if (!telefonoPattern.test(telefono.value.trim())) {
                    showError('telefonoError', 'Formato de teléfono inválido');
                    isValid = false;
                } else {
                    clearError('telefonoError');
                }
            }

            // Validar email
            const email = document.getElementById('email');
            if (email) {
                const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
                if (!emailPattern.test(email.value.trim())) {
                    showError('emailError', 'Formato de email inválido');
                    isValid = false;
                } else {
                    clearError('emailError');
                }
            }

            // Validar precios
            const precioMin = document.getElementById('precioMin');
            const precioMax = document.getElementById('precioMax');
            if (precioMin && precioMax) {
                if (parseFloat(precioMin.value) < 0) {
                    showError('precioMinError', 'El precio debe ser mayor o igual a 0');
                    isValid = false;
                } else {
                    clearError('precioMinError');
                }
                if (precioMax.value && precioMin.value && parseFloat(precioMax.value) < parseFloat(precioMin.value)) {
                    showError('precioMaxError', 'El precio máximo debe ser mayor que el mínimo');
                    isValid = false;
                } else {
                    clearError('precioMaxError');
                }
            }

            if (!isValid) {
                e.preventDefault(); // Solo previene si hay errores de validación
                const firstError = document.querySelector('.error-message:not(:empty)');
                if (firstError) {
                    firstError.parentElement.scrollIntoView({ behavior: 'smooth', block: 'center' });
                }
                showNotification('Por favor, corrige los errores antes de guardar', 'error');
            }
            // Si isValid = true, el formulario SE ENVÍA AL SERVIDOR normalmente
        });
    }

    function showError(elementId, message) {
        const el = document.getElementById(elementId);
        if (el) { el.textContent = message; el.style.display = 'block'; }
    }

    function clearError(elementId) {
        const el = document.getElementById(elementId);
        if (el) { el.textContent = ''; el.style.display = 'none'; }
    }

    // ========== MENÚ LATERAL (scroll suave) ==========
    const navLinks = document.querySelectorAll('.edit-nav a');
    const sections = document.querySelectorAll('.form-section[id]');

    navLinks.forEach(link => {
        link.addEventListener('click', function(e) {
            const href = this.getAttribute('href');
            if (href && href.startsWith('#')) {
                const targetSection = document.querySelector(href);
                if (targetSection) {
                    e.preventDefault();
                    const headerOffset = 120;
                    const elementPosition = targetSection.getBoundingClientRect().top;
                    const offsetPosition = elementPosition + window.pageYOffset - headerOffset;
                    window.scrollTo({ top: offsetPosition, behavior: 'smooth' });
                    navLinks.forEach(l => l.classList.remove('nav-active'));
                    this.classList.add('nav-active');
                }
            }
        });
    });

    // Detectar sección visible
    const observerOptions = { root: null, rootMargin: '-120px 0px -60% 0px', threshold: 0 };
    const sectionObserver = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                const sectionId = entry.target.getAttribute('id');
                navLinks.forEach(link => {
                    link.classList.remove('nav-active');
                    if (link.getAttribute('href') === '#' + sectionId) {
                        link.classList.add('nav-active');
                    }
                });
            }
        });
    }, observerOptions);
    sections.forEach(section => sectionObserver.observe(section));

    // ========== NOTIFICACIÓN ==========
    function showNotification(message, type = 'info') {
        const existing = document.querySelector('.notification');
        if (existing) existing.remove();
        const notification = document.createElement('div');
        notification.className = 'notification notification-' + type;
        notification.innerHTML = '<div class="notification-content"><span class="notification-message">' + message + '</span></div>';
        document.body.appendChild(notification);
        setTimeout(() => notification.classList.add('show'), 10);
        setTimeout(() => {
            notification.classList.remove('show');
            setTimeout(() => notification.remove(), 300);
        }, 3000);
    }
});
