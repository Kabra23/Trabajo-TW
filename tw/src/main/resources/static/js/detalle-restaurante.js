// Script para funcionalidad de detalle del restaurante
document.addEventListener('DOMContentLoaded', function() {

    // ========== PESTAÑAS DEL MENÚ ==========
    const tabButtons = document.querySelectorAll('.tab-btn');
    const dishCards  = document.querySelectorAll('.dish-card');
    let activeCategory = '';   // '' = mostrar todos

    tabButtons.forEach(function(button) {
        button.addEventListener('click', function() {
            // ── Actualizar clase activa ──
            tabButtons.forEach(function(btn) { btn.classList.remove('active'); });
            this.classList.add('active');

            // ── Determinar categoría seleccionada ──
            // El tab "Todos" tiene data-tab="__todos__" y data-category="".
            // Usamos data-tab para distinguirlo de las etiquetas reales.
            var dataTab      = this.getAttribute('data-tab')      || '';
            var dataCategory = this.getAttribute('data-category') || '';

            activeCategory = (dataTab === '__todos__') ? '' : (dataCategory || dataTab).toLowerCase().trim();

            applyDishFilters();
        });
    });

    // ========== BÚSQUEDA EN EL MENÚ ==========
    var searchInput = document.getElementById('searchDish');

    if (searchInput) {
        searchInput.addEventListener('input', function() {
            applyDishFilters();
        });
    }

    // ========== FILTROS COMBINADOS ==========
    function applyDishFilters() {
        var term = searchInput ? searchInput.value.toLowerCase().trim() : '';
        var visibleCount = 0;

        dishCards.forEach(function(card) {
            var dishCategory = (card.getAttribute('data-category') || '').toLowerCase().trim();
            var coincideCategoria = !activeCategory || dishCategory === activeCategory;

            var coincideBusqueda = true;
            if (term) {
                var titulo      = card.querySelector('h3')               ? card.querySelector('h3').textContent.toLowerCase()               : '';
                var descripcion = card.querySelector('.dish-description') ? card.querySelector('.dish-description').textContent.toLowerCase() : '';
                coincideBusqueda = titulo.includes(term) || descripcion.includes(term);
            }

            if (coincideCategoria && coincideBusqueda) {
                card.style.display = 'grid';
                visibleCount++;
            } else {
                card.style.display = 'none';
            }
        });

        updateProductCount(visibleCount);
    }

    // ========== CONTADOR DE PRODUCTOS ==========
    function updateProductCount(count) {
        var subtitle = document.querySelector('.section-subtitle');
        if (subtitle) {
            subtitle.textContent = count + ' plato' + (count !== 1 ? 's' : '');
        }
    }

    // ========== ANIMACIÓN DE ENTRADA EN PLATOS ==========
    // Inicializamos los platos como invisibles ANTES de observarlos,
    // para que la animación sea: invisible → visible (no al revés).
    dishCards.forEach(function(card) {
        card.style.opacity   = '0';
        card.style.transform = 'translateY(20px)';
    });

    var observer = new IntersectionObserver(function(entries) {
        entries.forEach(function(entry) {
            if (entry.isIntersecting) {
                entry.target.style.transition = 'opacity 0.5s ease, transform 0.5s ease';
                entry.target.style.opacity    = '1';
                entry.target.style.transform  = 'translateY(0)';
                observer.unobserve(entry.target);
            }
        });
    }, { threshold: 0.1 });

    dishCards.forEach(function(card) { observer.observe(card); });

    // Mostrar todos los platos al arrancar
    applyDishFilters();
});
