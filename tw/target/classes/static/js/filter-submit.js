document.addEventListener('DOMContentLoaded', function() {
    const valoracionFilter = document.getElementById('valoracionFilter');
    if (!valoracionFilter) return;

    const checkboxes = valoracionFilter.querySelectorAll('.star-checkbox');

    checkboxes.forEach(checkbox => {
        checkbox.addEventListener('change', function() {
            const selectedValue = this.checked ? this.value : '';

            // Desmarcar otros checkboxes
            checkboxes.forEach(cb => {
                if (cb !== this) {
                    cb.checked = false;
                }
            });

            // Actualizar la URL y recargar
            const currentUrl = new URL(window.location.href);
            if (selectedValue) {
                currentUrl.searchParams.set('valoracion', selectedValue);
            } else {
                currentUrl.searchParams.delete('valoracion');
            }
            window.location.href = currentUrl.toString();
        });
    });
});

