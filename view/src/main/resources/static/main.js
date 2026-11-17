function all() {
    pageNav();
    tooltip();
}

function pageNav() {
    const links = document.querySelectorAll(".nav-links a");

    links.forEach((link) => {
        if (link.pathname === location.pathname) {
            link.setAttribute("aria-current", "page");
        } else {
            link.removeAttribute("aria-current");
        }
    });
}

function tooltip() {
    const profileButton = document.getElementById("profile");
    const profileTooltip = document.getElementById("icon-tooltip");
    if (!profileButton || !profileTooltip) return;

    function setTooltipOpen(isOpen) {
        profileTooltip.hidden = !isOpen;
        profileButton.setAttribute("aria-expanded", String(isOpen));
    }

    profileButton.addEventListener("click", () => {
        setTooltipOpen(profileTooltip.hidden);
    });

    document.addEventListener("click", (event) => {
        if (!event.target.closest(".profile-menu")) {
            setTooltipOpen(false);
        }
    });

    document.addEventListener("keydown", (event) => {
        if (event.key === "Escape") {
            setTooltipOpen(false);
        }
    });
}

all();