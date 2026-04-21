// Script para funcionalidad de detalle del restaurante
document.addEventListener('DOMContentLoaded', function() {

    // ========== CARGAR DATOS GUARDADOS ==========
    loadSavedRestaurantData();

    function loadSavedRestaurantData() {
        const savedData = localStorage.getItem('restauranteData');
        if (savedData) {
            try {
                const data = JSON.parse(savedData);
                console.log('Datos cargados:', data);

                // Actualizar nombre del restaurante
                const restaurantName = document.querySelector('.restaurant-header h1');
                if (restaurantName && data.nombre) {
                    restaurantName.textContent = data.nombre;
                }

                // Actualizar dirección
                const restaurantAddress = document.querySelector('.restaurant-address');
                if (restaurantAddress && data.direccion) {
                    restaurantAddress.innerHTML = `
                        <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
                            <path d="M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7zm0 9.5c-1.38 0-2.5-1.12-2.5-2.5s1.12-2.5 2.5-2.5 2.5 1.12 2.5 2.5-1.12 2.5-2.5 2.5z"/>
                        </svg>
                        ${data.direccion}
                    `;
                }

                // Actualizar teléfono
                const phoneElement = document.querySelector('.restaurant-contact a[href^="tel"]');
                if (phoneElement && data.telefono) {
                    phoneElement.textContent = data.telefono;
                    phoneElement.href = `tel:${data.telefono.replace(/\s/g, '')}`;
                }

                // Actualizar email
                const emailElement = document.querySelector('.restaurant-contact a[href^="mailto"]');
                if (emailElement && data.email) {
                    emailElement.textContent = data.email;
                    emailElement.href = `mailto:${data.email}`;
                }

                // Actualizar valoración
                const ratingElement = document.querySelector('.restaurant-meta strong');
                if (ratingElement && data.valoracion) {
                    ratingElement.textContent = `${data.valoracion}/5`;
                }

                // Actualizar categorías
                if (data.categorias && data.categorias.length > 0) {
                    const tagsContainer = document.querySelector('.restaurant-tags');
                    if (tagsContainer) {
                        tagsContainer.innerHTML = data.categorias.map(cat =>
                            `<span class="tag">${cat.charAt(0).toUpperCase() + cat.slice(1)}</span>`
                        ).join('');
                    }
                }

                // Actualizar bike friendly
                if (data.bikeFriendly === 'si') {
                    const featuresContainer = document.querySelector('.restaurant-features');
                    if (featuresContainer && !featuresContainer.querySelector('.bike-feature')) {
                        const bikeFeature = document.createElement('div');
                        bikeFeature.className = 'feature bike-feature';
                        bikeFeature.innerHTML = `
                            <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
                                <path d="M15.5 5.5c1.1 0 2-.9 2-2s-.9-2-2-2-2 .9-2 2 .9 2 2 2zM5 12c-2.8 0-5 2.2-5 5s2.2 5 5 5 5-2.2 5-5-2.2-5-5-5zm0 8.5c-1.9 0-3.5-1.6-3.5-3.5s1.6-3.5 3.5-3.5 3.5 1.6 3.5 3.5-1.6 3.5-3.5 3.5zm5.8-10l2.4-2.4.8.8c1.3 1.3 3 2.1 5.1 2.1V9c-1.5 0-2.7-.6-3.6-1.5l-1.9-1.9c-.5-.4-1-.6-1.6-.6s-1.1.2-1.4.6L7.8 8.4c-.4.4-.6.9-.6 1.4 0 .6.2 1.1.6 1.4L11 14v5h2v-6.2l-2.2-2.3zM19 12c-2.8 0-5 2.2-5 5s2.2 5 5 5 5-2.2 5-5-2.2-5-5-5zm0 8.5c-1.9 0-3.5-1.6-3.5-3.5s1.6-3.5 3.5-3.5 3.5 1.6 3.5 3.5-1.6 3.5-3.5 3.5z"/>
                            </svg>
                            Bike Friendly
                        `;
                        featuresContainer.appendChild(bikeFeature);
                    }
                }

            } catch (e) {
                console.error('Error al cargar datos guardados:', e);
            }
        }
    }

    // ========== FUNCIONALIDAD DE PESTAÑAS ==========
    const tabButtons = document.querySelectorAll('.tab-btn');
    const dishCards = document.querySelectorAll('.dish-card');
    // Mostrar toda la carta al cargar para que no parezca que faltan platos.
    let activeCategory = '';

    tabButtons.forEach(button => {
        button.addEventListener('click', function() {
            // Remover clase active de todos los botones
            tabButtons.forEach(btn => btn.classList.remove('active'));

            // Agregar clase active al botón clickeado
            this.classList.add('active');

            // Obtener la categoría seleccionada
            const selectedCategory = (this.getAttribute('data-category') || this.getAttribute('data-tab') || '').toLowerCase().trim();
            activeCategory = selectedCategory;

            // Filtrar platos por categoría
            applyDishFilters();
        });
    });

    function filterDishesByCategory(category) {
        let visibleCount = 0;

        dishCards.forEach(card => {
            const dishCategory = (card.getAttribute('data-category') || '').toLowerCase().trim();

            if (!category || dishCategory === category) {
                card.style.display = 'grid';
                visibleCount++;
            } else {
                card.style.display = 'none';
            }
        });

        // Actualizar el contador de productos
        updateProductCount(visibleCount);
    }

    // ========== FUNCIONALIDAD DE BÚSQUEDA ==========
    const searchInput = document.getElementById('searchDish');

    if (searchInput) {
        searchInput.addEventListener('input', function() {
            applyDishFilters();
        });
    }

    function filterDishesBySearch(searchTerm, category) {
        let visibleCount = 0;

        dishCards.forEach(card => {
            const dishTitle = card.querySelector('h3').textContent.toLowerCase();
            const dishDescription = card.querySelector('.dish-description').textContent.toLowerCase();
            const dishCategory = (card.getAttribute('data-category') || '').toLowerCase().trim();
            const coincideCategoria = !category || dishCategory === category;

            // Buscar en título y descripción
            if (coincideCategoria && (dishTitle.includes(searchTerm) || dishDescription.includes(searchTerm))) {
                card.style.display = 'grid';
                visibleCount++;
            } else {
                card.style.display = 'none';
            }
        });

        // Actualizar el contador de productos
        updateProductCount(visibleCount);

    }

    function applyDishFilters() {
        const term = searchInput ? searchInput.value.toLowerCase().trim() : '';
        if (term) {
            filterDishesBySearch(term, activeCategory);
        } else {
            filterDishesByCategory(activeCategory);
        }
    }

    // ========== ACTUALIZAR CONTADOR DE PRODUCTOS ==========
    function updateProductCount(count) {
        const subtitle = document.querySelector('.section-subtitle');
        if (subtitle) {
            subtitle.textContent = `${count} producto${count !== 1 ? 's' : ''}`;
        }
    }

    // ========== ANIMACIÓN SUAVE PARA ELEMENTOS ==========
    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.style.opacity = '0';
                entry.target.style.transform = 'translateY(20px)';

                setTimeout(() => {
                    entry.target.style.transition = 'opacity 0.5s ease, transform 0.5s ease';
                    entry.target.style.opacity = '1';
                    entry.target.style.transform = 'translateY(0)';
                }, 100);

                observer.unobserve(entry.target);
            }
        });
    }, { threshold: 0.1 });

    dishCards.forEach(card => observer.observe(card));

    applyDishFilters();
});

