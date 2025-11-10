const visibilityLogo = document.querySelector(".visibility-logo");
const password = document.querySelector("#password");

visibilityLogo.addEventListener("click", function () {
  if (password.type == "password") {
    password.type = "text";
    visibilityLogo.src = "assets/show.svg";
  } else {
    password.type = "password";
    visibilityLogo.src = "assets/hide.svg";
  }
});

document
  .querySelector(".login-form")
  .addEventListener("submit", async function (event) {
    event.preventDefault();
    const messageError = document.querySelector(".message-error");

    messageError.textContent = "";

    const email = document.getElementById("email").value;
    const password = document.getElementById("password").value;

    try {
      const response = await fetch("http://localhost:8080/login", {
        method: "POST",
        headers: {
          "Content-Type": "application/x-www-form-urlencoded",
        },
        body: `email=${encodeURIComponent(email)}&password=${encodeURIComponent(
          password
        )}`,
        credentials: "include"
      });

      const data = await response.json();
      console.log("Response:", data);

      if (response.status === 200) {
        window.location.href = "dashboard-cashier.html";
      } else {
        messageError.textContent = data.message;
      }
    } catch (error) {
      console.error("Error:", error);
      messageError.textContent = "Terjadi kesalahan saat login!";
    }
  });
