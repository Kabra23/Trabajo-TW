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
        checkbox.addEventListener('change', function() {
            handleStarSelection(index);
        });
    });

    // Función para manejar la selección de estrellas
    function handleStarSelection(selectedIndex) {
        // Desmarcar todos primero
        checkboxes.forEach(cb => cb.checked = false);

        // Marcar desde 0 hasta el índice seleccionado
        for (let i = 0; i <= selectedIndex; i++) {
            checkboxes[i].checked = true;
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
});

