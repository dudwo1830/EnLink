const TOPIC_API_BASE = '/api/topics';
const topicTarget = document.querySelector('#topicTarget');

topicRender();

function topicRender() {
  fetch(`${TOPIC_API_BASE}/me`)
    .then((res) => {
      console.log(res);
      if (res.ok) {
        return res.json();
      }
    })
    .then((data) => {
      topicTarget.innerHTML = '';
      data.forEach((topic) => {
        topicTarget.appendChild(makeTopicElement(topic));
      });
    });
}

function makeTopicElement(topic) {
  const template = document.createElement('template');
  template.innerHTML = `
	<p>
		<input type="checkbox" value="${topic.topicId}">
		<span>${topic.name}</span>
	</p>
	`;
  const checkbox = template.content.querySelector("input[type='checkbox']");
  checkbox.checked = topic.checked;
  return template.content.firstElementChild;
}

function updateTopic() {
  const topicIds = [...topicTarget.querySelectorAll('input[type=checkbox]:checked')].map((el) => el.value);
  fetch(`${API_BASE}/me/topics`, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      topicIds: topicIds,
    }),
  });
}
