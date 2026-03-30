const form = document.querySelector('#editForm');
const deleteButton = document.querySelector('#deleteButton');
form.addEventListener('submit', update);
deleteButton.addEventListener('click', deleteMember);
async function update(e) {
  e.preventDefault();
  const formData = Object.fromEntries(new FormData(form));
  const res = await fetch(`${API_BASE}/me`, {
    method: 'PATCH',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(formData),
  });
  if (!res.ok) {
    const data = await res.json();
    const msg = `
    ${data.messageCode}
    ${data.defaultMessage}
    Code: ${data.code}
    `;
    alert(msg);
  } else {
    alert(window.i18n.alert.success);
    location.replace('/members/me');
  }
}

async function deleteMember() {
  const isConfirm = await Ui.confirm(window.i18n.edit.delete.text, {
    title: window.i18n.edit.delete.title,
    icon: 'warning',
    confirmText: window.i18n.edit.delete.btn,
    cancelText: window.i18n.edit.delete.cancel
  });
  if(!isConfirm){
    return;
  }
  const formData = Object.fromEntries(new FormData(form));
  const res = await fetch(`${API_BASE}/me`, {
    method: 'DELETE',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(formData),
  });
  if (!res.ok) {
    const data = await res.json();
    const msg = `
    ${data.messageCode}
    ${data.defaultMessage}
    Code: ${data.code}
    `;
    alert(msg);
  } else {
    alert(window.i18n.alert.success);
    location.replace('/auth/logout');
  }
}