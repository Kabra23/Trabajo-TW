// Star Filter Interactivity
document.addEventListener('DOMContentLoaded', function() {
    const starWrappers = document.querySelectorAll('.star-filter-wrapper');
    const checkboxes = document.querySelectorAll('.star-checkbox');

    // Agregar eventos de hover
    starWrappers.forEach((wrapper, index) => {
        const label = wrapper.querySelector('.star-label');

        // Hover: iluminar esta estrella y todas las anteriores
        label.addEventListener('mouseenter', function() {
            highlightStarsUpTo(index);
        });
    });

    // Mouse leave del contenedor: restaurar al estado seleccionado
    const filterContainer = document.querySelector('.star-rating-filter');
    if (filterContainer) {
        filterContainer.addEventListener('mouseleave', function() {
            updateStarsDisplay();
        });
    }

    // Escuchar clicks en los checkboxes
    checkboxes.forEach((checkbox, index) => {
        checkbox.addEventListener('change', function(event) {
            // Si el cambio no viene de una limpieza de filtros, manejar la selección
            if (!event.isTrusted || event.target === checkbox) {
                handleStarSelection(index);
            } else {
                // Solo actualizar visualización
                updateStarsDisplay();
            }
        });
    });

    // Función para manejar la selección de estrellas
    function handleStarSelection(selectedIndex) {
        const wasChecked = checkboxes[selectedIndex].checked;

        if (wasChecked) {
            // Si ya estaba marcada, marcar todas hasta este índice
            checkboxes.forEach((cb, i) => {
                cb.checked = i <= selectedIndex;
            });
        } else {
            // Si no estaba marcada, desmarcar todas
            checkboxes.forEach(cb => cb.checked = false);
        }

        // Actualizar visualización
        updateStarsDisplay();
    }

    // Función para iluminar estrellas hasta el índice dado (hover)
    function highlightStarsUpTo(upToIndex) {
        starWrappers.forEach((wrapper, index) => {
            const svg = wrapper.querySelector('.star-icon svg');
            if (index <= upToIndex) {
                svg.style.color = '#ffa726';
                svg.style.transform = 'scale(1.1)';
            } else {
                svg.style.color = '#d0d0d0';
                svg.style.transform = 'scale(1)';
            }
        });
    }

    // Función para actualizar la visualización según el estado checked
    function updateStarsDisplay() {
        starWrappers.forEach((wrapper, index) => {
            const checkbox = wrapper.querySelector('.star-checkbox');
            const svg = wrapper.querySelector('.star-icon svg');

            if (checkbox.checked) {
                svg.style.color = '#ffd700';
                svg.style.filter = 'drop-shadow(0 0 4px rgba(255, 215, 0, 0.5))';
                svg.style.transform = 'scale(1)';
            } else {
                svg.style.color = '#d0d0d0';
                svg.style.filter = 'none';
                svg.style.transform = 'scale(1)';
            }
        });
    }

    // Inicializar el estado al cargar
    updateStarsDisplay();

    // Exponer función para que otros scripts puedan actualizar las estrellas
    window.updateStarsDisplay = updateStarsDisplay;
});
