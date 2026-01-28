const API_BASE = '/topics';
const editButtons = document.querySelectorAll('button.editBtn');
const addButton = document.querySelector('#addBtn');

editButtons.forEach((editButton) => {
  editButton.addEventListener('click', updateTopic);
});
addButton.addEventListener('click', addTopic);

function updateTopic(ev) {
  const target = ev.target.closest('[data-topic-id]');
  const topicId = target.dataset.topicId;
  const name = target.querySelector('input').value;
  fetch(`${API_BASE}/${topicId}`, {
    method: 'PATCH',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      topicId: topicId,
      name: name,
    }),
  }).then((res) => {});
}

function addTopic() {
  const name = document.querySelector('#name').value;
  fetch(`${API_BASE}`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      name: name,
    }),
  }).then((res) => {
    if (res.ok) {
      location.reload();
    }
  });
}
