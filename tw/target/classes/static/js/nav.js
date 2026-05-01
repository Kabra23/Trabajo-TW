(function () {
    var btn  = document.querySelector('.nav-dropdown-btn');
    var menu = document.querySelector('.nav-dropdown-menu');
    if (!btn || !menu) return;

    btn.addEventListener('click', function (e) {
        e.stopPropagation();
        var opening = !menu.classList.contains('open');
        menu.classList.toggle('open', opening);
        btn.classList.toggle('open', opening);
    });

    document.addEventListener('click', function () {
        menu.classList.remove('open');
        btn.classList.remove('open');
    });

    menu.addEventListener('click', function (e) {
        e.stopPropagation();
    });
})();
