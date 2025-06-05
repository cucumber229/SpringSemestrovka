document.addEventListener('DOMContentLoaded', () => {
  const toggler = document.querySelector('.navbar-toggler');
  const menu = document.getElementById('navbarMenu');
  if (toggler && menu) {
    toggler.addEventListener('click', () => {
      menu.classList.toggle('show');
    });
  }
});
