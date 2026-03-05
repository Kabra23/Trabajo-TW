// Script de autocompletado de búsqueda de direcciones
document.addEventListener('DOMContentLoaded', function() {
    const addressInput = document.getElementById('address');
    const autocompleteResults = document.getElementById('autocomplete-results');
    const searchForm = document.getElementById('searchForm');

    // Base de datos de direcciones sugeridas (simulación)
    const addressDatabase = [
        'Calle Santa Eulalia 2, 06800 Mérida',
        'Avenida de Extremadura 10, 06800 Mérida',
        'Plaza de España 1, 06800 Mérida',
        'Calle John Lennon 5, 06800 Mérida',
        'Avenida Juan Carlos I 20, 06800 Mérida',
        'Calle Félix Valverde Lillo 3, 06800 Mérida',
        'Plaza de la Constitución 8, 06800 Mérida',
        'Calle Romero Leal 15, 06800 Mérida',
        'Avenida de Portugal 12, 06800 Mérida',
        'Calle Cabo Verde 7, 06800 Mérida',
        'Calle Reyes Huertas 4, 06800 Mérida',
        'Avenida Vía de la Plata 25, 06800 Mérida',
        'Calle Camilo José Cela 9, 06800 Mérida',
        'Plaza de Toros 1, 06800 Mérida',
        'Calle Santa Julia 6, 06800 Mérida',
        'Avenida José Fernández López 18, 06800 Mérida',
        'Calle Almendralejo 11, 06800 Mérida',
        'Paseo de Roma 3, 06800 Mérida',
        'Calle Adriano 14, 06800 Mérida',
        'Avenida Reina Sofía 22, 06800 Mérida',
        'Centro Universitario de Mérida, 06800 Mérida',
        'Campus Universitario, Calle Santa Teresa de Jornet, 06800 Mérida'
    ];

    let selectedIndex = -1;
    let currentResults = [];

    // Evento de entrada en el campo de búsqueda
    if (addressInput) {
        addressInput.addEventListener('input', function() {
            const query = this.value.trim().toLowerCase();

            if (query.length < 2) {
                hideAutocomplete();
                return;
            }

            // Filtrar direcciones que coincidan con la búsqueda
            currentResults = addressDatabase.filter(address =>
                address.toLowerCase().includes(query)
            );

            if (currentResults.length > 0) {
                showAutocomplete(currentResults, query);
            } else {
                hideAutocomplete();
            }
        });

        // Navegación con teclado
        addressInput.addEventListener('keydown', function(e) {
            const items = autocompleteResults.querySelectorAll('.autocomplete-item');

            if (e.key === 'ArrowDown') {
                e.preventDefault();
                selectedIndex = Math.min(selectedIndex + 1, items.length - 1);
                updateSelectedItem(items);
            } else if (e.key === 'ArrowUp') {
                e.preventDefault();
                selectedIndex = Math.max(selectedIndex - 1, -1);
                updateSelectedItem(items);
            } else if (e.key === 'Enter') {
                if (selectedIndex >= 0 && items[selectedIndex]) {
                    e.preventDefault();
                    selectAddress(currentResults[selectedIndex]);
                }
            } else if (e.key === 'Escape') {
                hideAutocomplete();
            }
        });

        // Cerrar autocompletado al hacer clic fuera
        document.addEventListener('click', function(e) {
            if (!addressInput.contains(e.target) && !autocompleteResults.contains(e.target)) {
                hideAutocomplete();
            }
        });
    }

    function showAutocomplete(results, query) {
        autocompleteResults.innerHTML = '';
        selectedIndex = -1;

        results.forEach((address, index) => {
            const item = document.createElement('div');
            item.className = 'autocomplete-item';

            // Resaltar el texto que coincide con la búsqueda
            const highlightedText = highlightMatch(address, query);
            item.innerHTML = `
                <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
                    <path d="M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7zm0 9.5c-1.38 0-2.5-1.12-2.5-2.5s1.12-2.5 2.5-2.5 2.5 1.12 2.5 2.5-1.12 2.5-2.5 2.5z"/>
                </svg>
                <span>${highlightedText}</span>
            `;

            item.addEventListener('click', function() {
                selectAddress(address);
            });

            item.addEventListener('mouseenter', function() {
                selectedIndex = index;
                updateSelectedItem(autocompleteResults.querySelectorAll('.autocomplete-item'));
            });

            autocompleteResults.appendChild(item);
        });

        autocompleteResults.classList.add('show');
    }

    function hideAutocomplete() {
        autocompleteResults.classList.remove('show');
        autocompleteResults.innerHTML = '';
        selectedIndex = -1;
    }

    function selectAddress(address) {
        addressInput.value = address;
        hideAutocomplete();
        addressInput.focus();
    }

    function updateSelectedItem(items) {
        items.forEach((item, index) => {
            if (index === selectedIndex) {
                item.classList.add('selected');
                item.scrollIntoView({ block: 'nearest' });
            } else {
                item.classList.remove('selected');
            }
        });

        // Actualizar el valor del input con el elemento seleccionado
        if (selectedIndex >= 0 && currentResults[selectedIndex]) {
            addressInput.value = currentResults[selectedIndex];
        }
    }

    function highlightMatch(text, query) {
        const regex = new RegExp(`(${escapeRegex(query)})`, 'gi');
        return text.replace(regex, '<strong>$1</strong>');
    }

    function escapeRegex(string) {
        return string.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
    }

    // Validación del formulario
    if (searchForm) {
        searchForm.addEventListener('submit', function(e) {
            const address = addressInput.value.trim();

            if (address.length < 5) {
                e.preventDefault();
                addressInput.focus();
                showError('Por favor, introduce una dirección válida');
                return false;
            }

            // Guardar la dirección en localStorage para mostrarla en la página de restaurantes
            localStorage.setItem('selectedAddress', address);
            console.log('Dirección guardada:', address);
        });
    }

    function showError(message) {
        // Crear o actualizar mensaje de error
        let errorMsg = addressInput.parentElement.querySelector('.error-message');

        if (!errorMsg) {
            errorMsg = document.createElement('div');
            errorMsg.className = 'error-message';
            addressInput.parentElement.appendChild(errorMsg);
        }

        errorMsg.textContent = message;
        errorMsg.style.display = 'block';
        addressInput.classList.add('error');

        setTimeout(() => {
            errorMsg.style.display = 'none';
            addressInput.classList.remove('error');
        }, 3000);
    }
});

