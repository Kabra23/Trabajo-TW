// Star Filter Interactivity
document.addEventListener('DOMContentLoaded', function() {
    const starWrappers = document.querySelectorAll('.star-filter-wrapper');

    // Agregar eventos de hover
    starWrappers.forEach((wrapper, index) => {
        const label = wrapper.querySelector('.star-label');

        // Hover: iluminar esta estrella y todas las anteriores
        label.addEventListener('mouseenter', function() {
            highlightStars(index);
        });

        // Mouse leave: restaurar al estado original
        wrapper.addEventListener('mouseleave', function() {
            resetStarsToChecked();
        });
    });

    // También escuchar cambios en los checkboxes
    const checkboxes = document.querySelectorAll('.star-checkbox');
    checkboxes.forEach(checkbox => {
        checkbox.addEventListener('change', function() {
            resetStarsToChecked();
        });
    });

    // Función para iluminar estrellas hasta el índice dado
    function highlightStars(upToIndex) {
        starWrappers.forEach((wrapper, index) => {
            const svg = wrapper.querySelector('.star-icon svg');
            if (index <= upToIndex) {
                svg.style.color = '#ff9800';
                svg.style.filter = 'drop-shadow(0 0 4px rgba(255, 152, 0, 0.4))';
                wrapper.querySelector('.star-label').style.backgroundColor = '#fff3e0';
            } else {
                svg.style.color = '#d0d0d0';
                svg.style.filter = 'none';
                wrapper.querySelector('.star-label').style.backgroundColor = 'transparent';
            }
        });
    }

    // Función para restaurar estrellas según estado checked
    function resetStarsToChecked() {
        starWrappers.forEach((wrapper) => {
            const checkbox = wrapper.querySelector('.star-checkbox');
            const svg = wrapper.querySelector('.star-icon svg');
            const label = wrapper.querySelector('.star-label');

            if (checkbox.checked) {
                svg.style.color = '#ff9800';
                svg.style.filter = 'drop-shadow(0 0 4px rgba(255, 152, 0, 0.4))';
                label.style.backgroundColor = '#fff3e0';
            } else {
                svg.style.color = '#d0d0d0';
                svg.style.filter = 'none';
                label.style.backgroundColor = 'transparent';
            }
        });
    }
});

