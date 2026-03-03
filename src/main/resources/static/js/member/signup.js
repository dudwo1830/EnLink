const form = document.querySelector('#signupForm');
form.addEventListener('submit', update);

async function update(e) {
  e.preventDefault();

  // 1. 이전 에러 메시지 및 클래스 초기화
  clearErrors();

  const formData = Object.fromEntries(new FormData(form));

  try {
    const res = await fetch(`${API_BASE}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(formData),
    });

    if (!res.ok) {
      const data = await res.json();
      if (data.errors) {
        data.errors.forEach((error) => {
          showError(error.field, error.message);
        });
      }
    } else {
      alert(window.i18n.alert.success);
      location.replace('/auth/login');
    }
  } catch (error) {
    console.error("Signup Error:", error);
    alert(window.i18n.alert.error);
  }
}

function showError(fieldName, message) {
  const input = form.querySelector(`[name="${fieldName}"]`);
  if (input) {
    input.classList.add('is-invalid');
    const feedback = input.parentElement.querySelector('.invalid-feedback');
    if (feedback) {
      feedback.textContent = message;
    }
    input.scrollIntoView({
      block: 'center'
    });
    input.focus();
  }
}

function clearErrors() {
  const invalidInputs = form.querySelectorAll('.is-invalid');
  invalidInputs.forEach((input) => {
    input.classList.remove('is-invalid');
  });

  const feedbacks = form.querySelectorAll('.invalid-feedback');
  feedbacks.forEach((fb) => {
    fb.textContent = '';
  });
}