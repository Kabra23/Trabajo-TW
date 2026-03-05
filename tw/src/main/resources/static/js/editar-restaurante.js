// Script para funcionalidad de editar restaurante
document.addEventListener('DOMContentLoaded', function() {

    // ========== CARGAR DATOS GUARDADOS AL INICIAR ==========
    loadSavedDataToForm();

    function loadSavedDataToForm() {
        const savedData = localStorage.getItem('restauranteData');
        if (savedData) {
            try {
                const data = JSON.parse(savedData);
                console.log('Cargando datos al formulario:', data);

                // Cargar datos básicos
                if (data.nombre) document.getElementById('nombre').value = data.nombre;
                if (data.direccion) document.getElementById('direccion').value = data.direccion;
                if (data.telefono) document.getElementById('telefono').value = data.telefono;
                if (data.email) document.getElementById('email').value = data.email;
                if (data.precioMin) document.getElementById('precioMin').value = data.precioMin;
                if (data.precioMax) document.getElementById('precioMax').value = data.precioMax;
                if (data.valoracion) {
                    document.getElementById('valoracion').value = data.valoracion;
                    const starDisplay = document.querySelector('.star-display span');
                    if (starDisplay) starDisplay.textContent = data.valoracion;
                }

                // Cargar categorías
                if (data.categorias && data.categorias.length > 0) {
                    // Desmarcar todas primero
                    document.querySelectorAll('input[name="categorias"]').forEach(cb => cb.checked = false);
                    // Marcar las guardadas
                    data.categorias.forEach(cat => {
                        const checkbox = document.querySelector(`input[name="categorias"][value="${cat}"]`);
                        if (checkbox) checkbox.checked = true;
                    });
                }

                // Cargar bike friendly
                if (data.bikeFriendly) {
                    const radio = document.querySelector(`input[name="bikeFriendly"][value="${data.bikeFriendly}"]`);
                    if (radio) radio.checked = true;
                }

            } catch (e) {
                console.error('Error al cargar datos guardados:', e);
            }
        }
    }

    // ========== FUNCIONALIDAD DE PESTAÑAS ==========
    const tabButtons = document.querySelectorAll('.tab-btn');
    const dishCards = document.querySelectorAll('.dish-edit-card');

    tabButtons.forEach(button => {
        button.addEventListener('click', function() {
            // Remover clase active de todos los botones
            tabButtons.forEach(btn => btn.classList.remove('active'));

            // Agregar clase active al botón clickeado
            this.classList.add('active');

            // Obtener la categoría seleccionada
            const selectedCategory = this.getAttribute('data-category');

            // Filtrar platos por categoría
            filterDishesByCategory(selectedCategory);
        });
    });

    function filterDishesByCategory(category) {
        let visibleCount = 0;

        dishCards.forEach(card => {
            const dishCategory = card.getAttribute('data-category');

            if (category === 'destacados') {
                // Mostrar todos los platos en destacados
                card.style.display = 'flex';
                visibleCount++;
            } else if (dishCategory === category) {
                card.style.display = 'flex';
                visibleCount++;
            } else {
                card.style.display = 'none';
            }
        });
    }

    // ========== VALIDACIÓN Y GUARDADO DE FORMULARIO ==========
    const form = document.getElementById('editRestaurantForm');
    let formChanged = false;

    // Detectar cambios en el formulario
    if (form) {
        const formElements = form.querySelectorAll('input, textarea, select');
        formElements.forEach(element => {
            element.addEventListener('change', function() {
                formChanged = true;
            });
        });

        form.addEventListener('submit', function(e) {
            e.preventDefault();

            let isValid = true;
            const errors = [];

            // Validar nombre
            const nombre = document.getElementById('nombre');
            if (nombre.value.trim() === '') {
                showError('nombreError', 'El nombre es obligatorio');
                errors.push('Nombre');
                isValid = false;
            } else {
                clearError('nombreError');
            }

            // Validar dirección
            const direccion = document.getElementById('direccion');
            if (direccion.value.trim() === '') {
                showError('direccionError', 'La dirección es obligatoria');
                errors.push('Dirección');
                isValid = false;
            } else {
                clearError('direccionError');
            }

            // Validar teléfono
            const telefono = document.getElementById('telefono');
            const telefonoPattern = /^[+]?[0-9\s-]+$/;
            if (!telefonoPattern.test(telefono.value.trim())) {
                showError('telefonoError', 'Formato de teléfono inválido');
                errors.push('Teléfono');
                isValid = false;
            } else {
                clearError('telefonoError');
            }

            // Validar email
            const email = document.getElementById('email');
            const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
            if (!emailPattern.test(email.value.trim())) {
                showError('emailError', 'Formato de email inválido');
                errors.push('Email');
                isValid = false;
            } else {
                clearError('emailError');
            }

            // Validar precios
            const precioMin = document.getElementById('precioMin');
            const precioMax = document.getElementById('precioMax');

            if (parseFloat(precioMin.value) < 0) {
                showError('precioMinError', 'El precio debe ser mayor o igual a 0');
                errors.push('Precio mínimo');
                isValid = false;
            } else {
                clearError('precioMinError');
            }

            if (parseFloat(precioMax.value) < parseFloat(precioMin.value)) {
                showError('precioMaxError', 'El precio máximo debe ser mayor que el mínimo');
                errors.push('Precio máximo');
                isValid = false;
            } else {
                clearError('precioMaxError');
            }

            // Validar que al menos una categoría esté seleccionada
            const categorias = document.querySelectorAll('input[name="categorias"]:checked');
            if (categorias.length === 0) {
                alert('Debes seleccionar al menos una categoría');
                errors.push('Categorías');
                isValid = false;
            }

            if (isValid) {
                saveRestaurantData();
            } else {
                // Scroll al primer error
                const firstError = document.querySelector('.error-message:not(:empty)');
                if (firstError) {
                    firstError.parentElement.scrollIntoView({ behavior: 'smooth', block: 'center' });
                }
                showNotification('Por favor, corrige los errores antes de guardar', 'error');
            }
        });
    }

    // Función para guardar los datos del restaurante
    function saveRestaurantData() {
        const submitBtn = document.querySelector('.btn-submit');
        const originalText = submitBtn.textContent;

        // Deshabilitar el botón y mostrar estado de carga
        submitBtn.disabled = true;
        submitBtn.textContent = 'Guardando...';
        submitBtn.style.opacity = '0.7';

        // Recopilar todos los datos del formulario
        const formData = {
            nombre: document.getElementById('nombre').value.trim(),
            direccion: document.getElementById('direccion').value.trim(),
            telefono: document.getElementById('telefono').value.trim(),
            email: document.getElementById('email').value.trim(),
            precioMin: parseFloat(document.getElementById('precioMin').value),
            precioMax: parseFloat(document.getElementById('precioMax').value),
            valoracion: parseFloat(document.getElementById('valoracion').value),
            bikeFriendly: document.querySelector('input[name="bikeFriendly"]:checked').value,
            categorias: Array.from(document.querySelectorAll('input[name="categorias"]:checked'))
                .map(cb => cb.value),
            timestamp: new Date().toISOString()
        };

        // Simular guardado (en producción, aquí iría la llamada al servidor)
        setTimeout(() => {
            // Guardar en localStorage para que persistan los cambios
            localStorage.setItem('restauranteData', JSON.stringify(formData));

            // Eliminar el borrador ya que se guardó correctamente
            localStorage.removeItem('restauranteFormDraft');

            console.log('Datos guardados:', formData);

            // Restaurar el botón
            submitBtn.disabled = false;
            submitBtn.textContent = originalText;
            submitBtn.style.opacity = '1';

            // Marcar que no hay cambios pendientes
            formChanged = false;

            // Mostrar notificación de éxito
            showNotification('Restaurante actualizado correctamente', 'success');

            // Redirigir después de 1.5 segundos
            setTimeout(() => {
                window.location.href = '/detalle-restaurante';
            }, 1500);

        }, 1000);
    }

    function showError(elementId, message) {
        const errorElement = document.getElementById(elementId);
        if (errorElement) {
            errorElement.textContent = message;
            errorElement.style.display = 'block';
        }
    }

    function clearError(elementId) {
        const errorElement = document.getElementById(elementId);
        if (errorElement) {
            errorElement.textContent = '';
            errorElement.style.display = 'none';
        }
    }

    // ========== BOTÓN CANCELAR ==========
    const btnCancel = document.querySelector('.btn-cancel');
    if (btnCancel) {
        btnCancel.addEventListener('click', function() {
            if (formChanged) {
                if (confirm('¿Estás seguro de que quieres cancelar? Se perderán los cambios no guardados.')) {
                    window.location.href = '/detalle-restaurante';
                }
            } else {
                window.location.href = '/detalle-restaurante';
            }
        });
    }

    // ========== ADVERTENCIA AL SALIR SIN GUARDAR ==========
    window.addEventListener('beforeunload', function(e) {
        if (formChanged) {
            e.preventDefault();
            e.returnValue = '¿Estás seguro de que quieres salir? Tienes cambios sin guardar.';
            return e.returnValue;
        }
    });

    // ========== FUNCIÓN DE NOTIFICACIÓN ==========
    function showNotification(message, type = 'info') {
        // Remover notificación existente si la hay
        const existingNotification = document.querySelector('.notification');
        if (existingNotification) {
            existingNotification.remove();
        }

        // Crear notificación
        const notification = document.createElement('div');
        notification.className = `notification notification-${type}`;

        let icon = '';
        if (type === 'success') {
            icon = '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="20 6 9 17 4 12"></polyline></svg>';
        } else if (type === 'error') {
            icon = '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="6" x2="6" y2="18"></line><line x1="6" y1="6" x2="18" y2="18"></line></svg>';
        } else {
            icon = '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"></circle><line x1="12" y1="16" x2="12" y2="12"></line><line x1="12" y1="8" x2="12.01" y2="8"></line></svg>';
        }

        notification.innerHTML = `
            <div class="notification-content">
                <span class="notification-icon">${icon}</span>
                <span class="notification-message">${message}</span>
            </div>
        `;

        document.body.appendChild(notification);

        // Mostrar con animación
        setTimeout(() => {
            notification.classList.add('show');
        }, 10);

        // Ocultar después de 3 segundos
        setTimeout(() => {
            notification.classList.remove('show');
            setTimeout(() => {
                notification.remove();
            }, 300);
        }, 3000);
    }

    // ========== AUTO-GUARDADO (OPCIONAL) ==========
    let autoSaveTimeout;
    function setupAutoSave() {
        const formElements = document.querySelectorAll('#editRestaurantForm input, #editRestaurantForm textarea, #editRestaurantForm select');
        formElements.forEach(element => {
            element.addEventListener('input', function() {
                clearTimeout(autoSaveTimeout);
                autoSaveTimeout = setTimeout(() => {
                    saveFormDataToLocalStorage();
                }, 2000); // Auto-guardar cada 2 segundos después de dejar de escribir
            });
        });
    }

    function saveFormDataToLocalStorage() {
        const formData = {
            nombre: document.getElementById('nombre').value,
            direccion: document.getElementById('direccion').value,
            telefono: document.getElementById('telefono').value,
            email: document.getElementById('email').value,
            precioMin: document.getElementById('precioMin').value,
            precioMax: document.getElementById('precioMax').value,
            valoracion: document.getElementById('valoracion').value,
            timestamp: new Date().toISOString()
        };

        localStorage.setItem('restauranteFormDraft', JSON.stringify(formData));
        console.log('Borrador guardado automáticamente');
    }

    // Cargar borrador al iniciar (si existe)
    function loadFormDraftFromLocalStorage() {
        const draft = localStorage.getItem('restauranteFormDraft');
        if (draft) {
            const data = JSON.parse(draft);
            const timestamp = new Date(data.timestamp);
            const now = new Date();
            const hoursSinceLastSave = (now - timestamp) / (1000 * 60 * 60);

            // Si el borrador es de menos de 24 horas
            if (hoursSinceLastSave < 24) {
                if (confirm('Se encontró un borrador guardado. ¿Deseas recuperarlo?')) {
                    document.getElementById('nombre').value = data.nombre || '';
                    document.getElementById('direccion').value = data.direccion || '';
                    document.getElementById('telefono').value = data.telefono || '';
                    document.getElementById('email').value = data.email || '';
                    document.getElementById('precioMin').value = data.precioMin || '';
                    document.getElementById('precioMax').value = data.precioMax || '';
                    document.getElementById('valoracion').value = data.valoracion || '';

                    showNotification('Borrador recuperado correctamente', 'success');
                }
            }
        }
    }

    // Inicializar auto-guardado
    setupAutoSave();
    loadFormDraftFromLocalStorage();

    // ========== ACTUALIZAR DISPLAY DE ESTRELLAS ==========
    const valoracionInput = document.getElementById('valoracion');
    if (valoracionInput) {
        valoracionInput.addEventListener('input', function() {
            const starDisplay = document.querySelector('.star-display span');
            if (starDisplay) {
                starDisplay.textContent = this.value;
            }
        });
    }

    // ========== FUNCIONALIDAD DEL MENÚ LATERAL ==========
    setupSidebarNavigation();

    function setupSidebarNavigation() {
        const navLinks = document.querySelectorAll('.edit-nav a');
        const sections = document.querySelectorAll('.form-section[id]');

        // Manejo de clicks en los enlaces del menú
        navLinks.forEach(link => {
            link.addEventListener('click', function(e) {
                const href = this.getAttribute('href');

                // Manejar el botón de Salir
                if (href === '#salir') {
                    e.preventDefault();
                    if (formChanged) {
                        if (confirm('Tienes cambios sin guardar. ¿Estás seguro de que quieres salir?')) {
                            window.location.href = '/detalle-restaurante';
                        }
                    } else {
                        window.location.href = '/detalle-restaurante';
                    }
                    return;
                }

                // Manejar el botón de Eliminar restaurante
                if (href === '#eliminar') {
                    e.preventDefault();
                    handleDeleteRestaurant();
                    return;
                }

                // Para las secciones que existen, hacer scroll suave
                const targetSection = document.querySelector(href);
                if (targetSection) {
                    e.preventDefault();

                    // Scroll suave a la sección
                    const headerOffset = 120; // Altura del header sticky
                    const elementPosition = targetSection.getBoundingClientRect().top;
                    const offsetPosition = elementPosition + window.pageYOffset - headerOffset;

                    window.scrollTo({
                        top: offsetPosition,
                        behavior: 'smooth'
                    });

                    // Actualizar estado activo
                    navLinks.forEach(l => l.classList.remove('nav-active'));
                    this.classList.add('nav-active');
                }
            });
        });

        // Detectar sección visible y activar el enlace correspondiente
        const observerOptions = {
            root: null,
            rootMargin: '-120px 0px -60% 0px',
            threshold: 0
        };

        const sectionObserver = new IntersectionObserver((entries) => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    const sectionId = entry.target.getAttribute('id');

                    // Actualizar el enlace activo
                    navLinks.forEach(link => {
                        link.classList.remove('nav-active');
                        if (link.getAttribute('href') === `#${sectionId}`) {
                            link.classList.add('nav-active');
                        }
                    });
                }
            });
        }, observerOptions);

        // Observar todas las secciones
        sections.forEach(section => {
            sectionObserver.observe(section);
        });
    }

    function handleDeleteRestaurant() {
        const confirmation = confirm('¿Estás seguro de que quieres eliminar este restaurante? Esta acción no se puede deshacer.');

        if (confirmation) {
            const doubleConfirmation = prompt('Escribe "ELIMINAR" para confirmar:');

            if (doubleConfirmation === 'ELIMINAR') {
                // Eliminar datos guardados
                localStorage.removeItem('restauranteData');
                localStorage.removeItem('restauranteFormDraft');

                showNotification('Restaurante eliminado correctamente', 'success');

                setTimeout(() => {
                    window.location.href = '/';
                }, 1500);
            } else if (doubleConfirmation !== null) {
                showNotification('No se eliminó el restaurante. Texto incorrecto.', 'error');
            }
        }
    }
});

