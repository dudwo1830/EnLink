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
  const nameKo = target.querySelector('.update-ko').value;
  const nameJa = target.querySelector('.update-ja').value;
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
  }).then((res) => {
    target.classList.add('table-success-highlight');
    // 2. 2초 뒤에 하이라이트 제거
    setTimeout(() => {
        target.classList.remove('table-success-highlight');
    }, 2000);
  });
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
