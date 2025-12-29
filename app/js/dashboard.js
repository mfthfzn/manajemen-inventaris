import { getCookie } from "./cookie.js";

document.addEventListener("DOMContentLoaded", async function (event) {
  event.preventDefault();

  try {
    const role = getCookie("role");

    if (role != "CASHIER") {
      window.location.href = "login.html";
    }
  } catch (error) {
    console.error("Error:", error);
    window.location.href = "login.html";
  }

  let fullname = '';
  let names = getCookie("name").split("-");
  names.forEach(n => 
    fullname = fullname + " " + n
  )
  let role = getCookie("role");

  const profileName = document.querySelector(".profile-name");
  const profileRole = document.querySelector(".profile-role");

  profileName.textContent = fullname;
  profileRole.textContent = role;
});
