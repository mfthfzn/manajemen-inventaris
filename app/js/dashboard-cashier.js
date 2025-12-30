import { getCookie } from "./cookie.js";

document.addEventListener("DOMContentLoaded", async function (event) {
  event.preventDefault();

  try {
    const role = getCookie("role");

    if (role != "CASHIER") {
      throw new Error("Role bukan Cashier");
    }
  } catch (error) {
    console.error("Error:", error);
    window.location.href = "../../../login.html";
  }

  let fullname = '';
  let names = getCookie("name").split("-");
  names.forEach(n => 
    fullname = fullname + " " + n
  )

  const heroWelcome = document.querySelector(".hero-welcome");
  heroWelcome.innerHTML = "Halo, " + fullname
});
