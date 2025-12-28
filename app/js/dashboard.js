document.querySelectorAll(".menu-item").forEach((item) => {
  item.addEventListener("click", function () {
    item.forEach(menu => menu.classList.remove("active"));
    this.classList.add("active");
  });
});
