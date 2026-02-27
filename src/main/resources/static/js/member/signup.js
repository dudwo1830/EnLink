const form = document.querySelector('#signupForm');
form.addEventListener('submit', signup);
async function signup(e) {
  e.preventDefault();
  const formData = Object.fromEntries(new FormData(form));
  const res = await fetch(`${API_BASE}`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(formData),
  });
  if (!res.ok) {
    const data = await res.json();
    let msg = '';
    if (data.errors) {
      data.errors.forEach((error) => {
        msg += 'Field: ' + error.field + '\n';
        msg += 'Message: ' + error.message + '\n';
      });
    } else {
      msg = `
      ${data.messageCode}
      `;
    }
    alert(msg);
  } else {
    alert('success');
    location.replace('/auth/login');
  }
}
