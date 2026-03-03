// Sistema de filtros dinámicos para restaurantes
document.addEventListener('DOMContentLoaded', function() {
    // Referencias a elementos
    const resultsHeader = document.querySelector('.results-header h2');
    const clearFilterBtn = document.querySelector('.filter-link');

    // Todos los filtros
    const categoryPills = document.querySelectorAll('.category-pill');

    // Estado de los filtros
    let activeFilters = {
        availability: [],
        minOrder: 'todos',
        rating: [],
        offers: [],
        diet: [],
        category: null
    };

    // Datos de ejemplo de restaurantes (esto vendría del backend en producción)
    const allRestaurants = [
        { name: 'The Sushi Mérida', category: 'Japonesa', rating: 0, nuevo: true },
        { name: 'Asador de Pollos Koki', category: 'Española', rating: 4.8 },
        { name: 'Aguacate Mexican And Caribbean Food', category: 'Mexicana', rating: 4.9 },
        { name: 'Cafetería CUMe', category: 'Cafetería', rating: 4.7 },
        { name: 'Pizza Express Mérida', category: 'Pizza', rating: 4.5 },
        { name: 'Burger House Mérida', category: 'Hamburguesas', rating: 4.6 }
    ];

    // Inicializar
    updateResults();

    // Event listeners para checkboxes de disponibilidad
    document.querySelectorAll('.filter-checkbox input').forEach(checkbox => {
        checkbox.addEventListener('change', function() {
            if (this.name === 'abiertosAhora' || this.name === 'nuevo' || this.name === 'gastosEnvioGratis') {
                updateAvailabilityFilters();
            } else if (this.name === 'ofertas' || this.name === 'tarjetaSelllos') {
                updateOffersFilters();
            } else if (this.name === 'halal') {
                updateDietFilters();
            }

            // Sincronizar con las pills de categoría
            if (this.name === 'ofertas') {
                const ofertaPill = Array.from(categoryPills).find(pill =>
                    pill.textContent.trim() === 'Ofertas'
                );
                if (ofertaPill) {
                    if (this.checked) {
                        categoryPills.forEach(p => p.classList.remove('active'));
                        ofertaPill.classList.add('active');
                        activeFilters.category = 'Ofertas';
                    } else {
                        ofertaPill.classList.remove('active');
                        if (activeFilters.category === 'Ofertas') {
                            activeFilters.category = null;
                        }
                    }
                }
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

                // Si es Ofertas, desmarcar el checkbox
                if (categoryName === 'Ofertas') {
                    const ofertasCheckbox = document.querySelector('input[name="ofertas"]');
                    if (ofertasCheckbox) {
                        ofertasCheckbox.checked = false;
                        updateOffersFilters();
                    }
                }
            } else {
                // Remover active de todas las pills
                categoryPills.forEach(p => p.classList.remove('active'));
                // Agregar active a la clickeada
                this.classList.add('active');
                // Actualizar filtro de categoría
                activeFilters.category = categoryName;

                // Si es Ofertas, marcar el checkbox
                if (categoryName === 'Ofertas') {
                    const ofertasCheckbox = document.querySelector('input[name="ofertas"]');
                    if (ofertasCheckbox) {
                        ofertasCheckbox.checked = true;
                        updateOffersFilters();
                    }
                }
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
            if ((checkbox.name === 'abiertosAhora' || checkbox.name === 'nuevo' || checkbox.name === 'gastosEnvioGratis') && checkbox.checked) {
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

    // Función principal para actualizar resultados
    function updateResults() {
        let filteredRestaurants = [...allRestaurants];
        let filterDescriptions = [];

        // Filtrar por categoría (pills)
        if (activeFilters.category) {
            filteredRestaurants = filteredRestaurants.filter(r =>
                r.category === activeFilters.category
            );
            filterDescriptions.push(activeFilters.category);
        }

        // Filtrar por disponibilidad
        if (activeFilters.availability.includes('nuevo')) {
            filteredRestaurants = filteredRestaurants.filter(r => r.nuevo === true);
            filterDescriptions.push('Nuevo');
        }

        if (activeFilters.availability.includes('gastosEnvioGratis')) {
            filterDescriptions.push('Gastos de envío gratis');
        }

        // Filtrar por puntuación
        if (activeFilters.rating.length > 0) {
            const maxRating = Math.max(...activeFilters.rating);
            filteredRestaurants = filteredRestaurants.filter(r =>
                r.rating >= maxRating || r.rating === 0
            );
            filterDescriptions.push(`${maxRating}+ estrellas`);
        }

        // Filtrar por pedido mínimo
        if (activeFilters.minOrder !== 'todos') {
            if (activeFilters.minOrder === 'menos10') {
                filterDescriptions.push('Pedido mín. 10€ o menos');
            } else if (activeFilters.minOrder === 'menos15') {
                filterDescriptions.push('Pedido mín. 15€ o menos');
            }
        }

        // Filtrar por ofertas (solo si no está ya incluido en categoría)
        if (activeFilters.offers.length > 0 && activeFilters.category !== 'Ofertas') {
            if (activeFilters.offers.includes('ofertas')) {
                filterDescriptions.push('Con ofertas');
            }
            if (activeFilters.offers.includes('tarjetaSelllos')) {
                filterDescriptions.push('Tarjeta de sellos');
            }
        }

        // Filtrar por dieta
        if (activeFilters.diet.length > 0) {
            if (activeFilters.diet.includes('halal')) {
                filterDescriptions.push('Halal');
            }
        }

        // Actualizar el texto del encabezado
        updateHeaderText(filteredRestaurants.length, filterDescriptions);

        // Aquí podrías actualizar la visualización de las tarjetas de restaurantes
        // Por ahora solo actualizamos el texto
    }

    function updateHeaderText(count, descriptions) {
        let text;

        if (descriptions.length === 0) {
            text = `${count} establecimientos disponibles`;
        } else if (descriptions.length === 1) {
            text = `${count} establecimientos coinciden con "${descriptions[0]}"`;
        } else {
            const lastFilter = descriptions.pop();
            text = `${count} establecimientos con ${descriptions.join(', ')} y ${lastFilter}`;
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
        return activeFilters.availability.length > 0 ||
               activeFilters.minOrder !== 'todos' ||
               activeFilters.rating.length > 0 ||
               activeFilters.offers.length > 0 ||
               activeFilters.diet.length > 0 ||
               activeFilters.category !== null;
    }

    function clearAllFilters() {
        // Desmarcar todos los checkboxes excepto "Abiertos ahora" si quieres que esté marcado por defecto
        document.querySelectorAll('.filter-checkbox input').forEach(checkbox => {
            checkbox.checked = false;
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
            availability: [],
            minOrder: 'todos',
            rating: [],
            offers: [],
            diet: [],
            category: null
        };
    }
});

