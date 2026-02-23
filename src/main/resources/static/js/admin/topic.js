const API_BASE = '/api/topics';
const editButtons = document.querySelectorAll('button.editBtn');
const addButton = document.querySelector('#addBtn');

editButtons.forEach((editButton) => {
  editButton.addEventListener('click', updateTopic);
});
addButton.addEventListener('click', addTopic);

function updateTopic(ev) {
  const target = ev.target.closest('[data-topic-id]');
  const topicId = target.dataset.topicId;
  const nameKo = target.querySelector('#updateKo').value;
  const nameJa = target.querySelector('#updateJa').value;
  fetch(`${API_BASE}/${topicId}`, {
    method: 'PATCH',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      topicId: topicId,
      nameKo: nameKo,
      nameJa: nameJa
    }),
  }).then((res) => {});
}

function addTopic() {
  const nameKo = document.querySelector('#nameKo').value;
  const nameJa = document.querySelector('#nameJa').value;
  fetch(`${API_BASE}`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      nameKo: nameKo,
      nameJa: nameJa
    }),
  }).then((res) => {
    if (res.ok) {
      location.reload();
    }
  });
}
