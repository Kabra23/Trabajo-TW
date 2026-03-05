// Sistema de filtros dinámicos para restaurantes
const FILTER_ABIERTOS_AHORA = 'abiertosAhora';

document.addEventListener('DOMContentLoaded', function() {
    // ========== CARGAR DIRECCIÓN GUARDADA ==========
    loadSavedAddress();

    function loadSavedAddress() {
        const savedAddress = localStorage.getItem('selectedAddress');
        const locationElement = document.querySelector('.location');

        if (savedAddress && locationElement) {
            locationElement.textContent = savedAddress;
            console.log('Dirección cargada en restaurantes:', savedAddress);
        }
    }

    // Referencias a elementos
    const resultsHeader = document.querySelector('.results-header h2');
    const clearFilterBtn = document.querySelector('.filter-link');
    const categoryPills = document.querySelectorAll('.category-pill');
    const restaurantCards = document.querySelectorAll('.restaurant-card');
    const searchInput = document.getElementById('searchRestaurants');

    // Estado de los filtros
    let activeFilters = {
        search: '',
        availability: [],
        minOrder: 'todos',
        rating: [],
        offers: [],
        diet: [],
        category: null
    };

    // Inicializar
    updateResults();

    // Event listener para búsqueda
    if (searchInput) {
        searchInput.addEventListener('input', function() {
            activeFilters.search = this.value.trim().toLowerCase();
            updateResults();
        });
    }

    // Event listeners para checkboxes de disponibilidad
    document.querySelectorAll('.filter-checkbox input').forEach(checkbox => {
        checkbox.addEventListener('change', function() {
            if (this.name === FILTER_ABIERTOS_AHORA || this.name === 'nuevo' || this.name === 'gastosEnvioGratis') {
                updateAvailabilityFilters();
            } else if (this.name === 'ofertas' || this.name === 'tarjetaSelllos') {
                updateOffersFilters();
            } else if (this.name === 'halal') {
                updateDietFilters();
            }
            updateResults();
        });
    });

    // Event listeners para radio buttons de pedido mínimo
    document.querySelectorAll('.filter-radio input').forEach(radio => {
        radio.addEventListener('change', function() {
            activeFilters.minOrder = this.value;
            updateResults();
        });
    });

    // Event listeners para estrellas
    document.querySelectorAll('.star-checkbox').forEach(checkbox => {
        checkbox.addEventListener('change', function() {
            updateRatingFilters();
            updateResults();
        });
    });

    // Event listeners para categorías
    categoryPills.forEach(pill => {
        pill.addEventListener('click', function() {
            const categoryName = this.textContent.trim();

            // Si ya está activa, desactivarla
            if (this.classList.contains('active')) {
                this.classList.remove('active');
                activeFilters.category = null;
            } else {
                // Remover active de todas las pills
                categoryPills.forEach(p => p.classList.remove('active'));
                // Agregar active a la clickeada
                this.classList.add('active');
                // Actualizar filtro de categoría
                activeFilters.category = categoryName;
            }

            updateResults();
        });
    });

    // Event listener para botón de limpiar filtros
    if (clearFilterBtn) {
        clearFilterBtn.addEventListener('click', function() {
            clearAllFilters();
            updateResults();
        });
    }

    // Funciones para actualizar filtros
    function updateAvailabilityFilters() {
        activeFilters.availability = [];
        document.querySelectorAll('.filter-checkbox input').forEach(checkbox => {
            if ((checkbox.name === FILTER_ABIERTOS_AHORA || checkbox.name === 'nuevo' || checkbox.name === 'gastosEnvioGratis') && checkbox.checked) {
                activeFilters.availability.push(checkbox.name);
            }
        });
    }

    function updateOffersFilters() {
        activeFilters.offers = [];
        document.querySelectorAll('.filter-checkbox input').forEach(checkbox => {
            if ((checkbox.name === 'ofertas' || checkbox.name === 'tarjetaSelllos') && checkbox.checked) {
                activeFilters.offers.push(checkbox.name);
            }
        });
    }

    function updateDietFilters() {
        activeFilters.diet = [];
        const halalCheckbox = document.querySelector('input[name="halal"]');
        if (halalCheckbox && halalCheckbox.checked) {
            activeFilters.diet.push('halal');
        }
    }

    function updateRatingFilters() {
        activeFilters.rating = [];
        document.querySelectorAll('.star-checkbox:checked').forEach(checkbox => {
            activeFilters.rating.push(parseInt(checkbox.value));
        });
    }

    // Determina si una tarjeta de restaurante cumple los filtros activos
    function cardMatchesFilters(card) {
        const name = (card.dataset.name || '').toLowerCase();
        const categories = (card.dataset.categories || '').toLowerCase().split(',');
        const rating = parseFloat(card.dataset.rating || '0');
        const isNuevo = card.dataset.nuevo === 'true';
        const minOrder = parseFloat(card.dataset.minOrder || '0');
        const freeDelivery = card.dataset.freeDelivery === 'true';

        // Filtrar por búsqueda de texto
        if (activeFilters.search && !name.includes(activeFilters.search)) {
            return false;
        }

        // Filtrar por categoría de pill
        if (activeFilters.category && activeFilters.category !== 'Ofertas') {
            if (!categories.includes(activeFilters.category.toLowerCase())) {
                return false;
            }
        }

        // Filtrar por "Nuevo"
        if (activeFilters.availability.includes('nuevo') && !isNuevo) {
            return false;
        }

        // Filtrar por gastos de envío gratis
        if (activeFilters.availability.includes('gastosEnvioGratis') && !freeDelivery) {
            return false;
        }

        // Filtrar por puntuación mínima (los restaurantes "Nuevo" con rating=0 no se excluyen)
        if (activeFilters.rating.length > 0) {
            const minRating = Math.max(...activeFilters.rating);
            if (rating > 0 && rating < minRating) {
                return false;
            }
        }

        // Filtrar por pedido mínimo
        if (activeFilters.minOrder === 'menos10' && minOrder > 10) {
            return false;
        }
        if (activeFilters.minOrder === 'menos15' && minOrder > 15) {
            return false;
        }

        return true;
    }

    // Función principal para actualizar resultados
    function updateResults() {
        let visibleCount = 0;
        const filterDescriptions = [];

        // Mostrar/ocultar tarjetas según los filtros
        restaurantCards.forEach(card => {
            if (cardMatchesFilters(card)) {
                card.style.display = '';
                visibleCount++;
            } else {
                card.style.display = 'none';
            }
        });

        // Construir descripciones de filtros activos
        if (activeFilters.search) {
            filterDescriptions.push(activeFilters.search);
        }
        if (activeFilters.category) {
            filterDescriptions.push(activeFilters.category);
        }
        if (activeFilters.availability.includes('nuevo')) {
            filterDescriptions.push('Nuevo');
        }
        if (activeFilters.availability.includes('gastosEnvioGratis')) {
            filterDescriptions.push('Gastos de envío gratis');
        }
        if (activeFilters.rating.length > 0) {
            filterDescriptions.push(`${Math.max(...activeFilters.rating)}+ estrellas`);
        }
        if (activeFilters.minOrder !== 'todos') {
            filterDescriptions.push(activeFilters.minOrder === 'menos10' ? 'Pedido mín. 10€ o menos' : 'Pedido mín. 15€ o menos');
        }
        if (activeFilters.offers.includes('ofertas')) {
            filterDescriptions.push('Con ofertas');
        }
        if (activeFilters.offers.includes('tarjetaSelllos')) {
            filterDescriptions.push('Tarjeta de sellos');
        }
        if (activeFilters.diet.includes('halal')) {
            filterDescriptions.push('Halal');
        }

        updateHeaderText(visibleCount, filterDescriptions);
    }

    function updateHeaderText(count, descriptions) {
        let text;

        if (descriptions.length === 0) {
            text = `${count} establecimientos disponibles`;
        } else if (descriptions.length === 1) {
            text = `${count} establecimientos coinciden con "${descriptions[0]}"`;
        } else {
            const descCopy = [...descriptions];
            const lastFilter = descCopy.pop();
            text = `${count} establecimientos con ${descCopy.join(', ')} y ${lastFilter}`;
        }

        resultsHeader.textContent = text;

        // Mostrar/ocultar botón de limpiar filtros
        if (clearFilterBtn) {
            if (hasActiveFilters()) {
                clearFilterBtn.style.display = 'block';
                clearFilterBtn.textContent = descriptions.length > 1 ? 'Eliminar todos los filtros' : 'Eliminar este filtro';
            } else {
                clearFilterBtn.style.display = 'none';
            }
        }
    }

    function hasActiveFilters() {
        // abiertosAhora es el estado por defecto, no cuenta como filtro activo
        const nonDefaultAvailability = activeFilters.availability.filter(a => a !== FILTER_ABIERTOS_AHORA);
        return activeFilters.search !== '' ||
               nonDefaultAvailability.length > 0 ||
               activeFilters.minOrder !== 'todos' ||
               activeFilters.rating.length > 0 ||
               activeFilters.offers.length > 0 ||
               activeFilters.diet.length > 0 ||
               activeFilters.category !== null;
    }

    function clearAllFilters() {
        // Limpiar el campo de búsqueda
        if (searchInput) {
            searchInput.value = '';
        }

        // Resetear checkboxes (mantener abiertosAhora marcado por defecto)
        document.querySelectorAll('.filter-checkbox input').forEach(checkbox => {
            checkbox.checked = checkbox.name === FILTER_ABIERTOS_AHORA;
        });

        // Resetear radio buttons a "todos"
        const todosRadio = document.querySelector('input[name="soporte"][value="todos"]');
        if (todosRadio) {
            todosRadio.checked = true;
        }

        // Desmarcar todas las estrellas
        document.querySelectorAll('.star-checkbox').forEach(checkbox => {
            checkbox.checked = false;
        });

        // Actualizar visualización de estrellas si la función existe
        if (typeof window.updateStarsDisplay === 'function') {
            setTimeout(() => window.updateStarsDisplay(), 50);
        }

        // Desactivar todas las categorías
        categoryPills.forEach(pill => {
            pill.classList.remove('active');
        });

        // Resetear estado de filtros
        activeFilters = {
            search: '',
            availability: [],
            minOrder: 'todos',
            rating: [],
            offers: [],
            diet: [],
            category: null
        };
    }
});

