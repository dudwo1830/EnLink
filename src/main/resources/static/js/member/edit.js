const form = document.querySelector('#editForm');
form.addEventListener('submit', signup);
function signup(e) {
  e.preventDefault();
  const data = Object.fromEntries(new FormData(form));
  fetch(`${API_BASE}/me`, {
    method: 'PATCH',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(data),
  }).then((res) => {
    console.log(res);
  });
}
