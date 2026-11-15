function all() {
    pageNav();
    tooltip();
}

function pageNav() {

}

function tooltip() {
    const profileButton = document.getElementById("profile");
    const profileTooltip = document.getElementById("icon-tooltip");

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