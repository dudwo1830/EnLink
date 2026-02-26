const topicUpdateBtn = document.querySelector('#topicUpdateBtn');
const cityUpdateBtn = document.querySelector('#cityUpdateBtn');
topicUpdateBtn.addEventListener('click', updateTopic);
cityUpdateBtn.addEventListener('click', updateCity);

const topicTarget = document.querySelector('#topicTarget');

topicRender();
// 회원이 설정한 지역
fetch(`/api/members/me/city`)
  .then((res) => {
    if (!res.ok) {
      throw res.json();
    }
    return res.json();
  })
  .then((data) => {
    changeCity(data.fullNameLocal);
  })
  .catch((err) => {
    console.log(err);
  });

// 회원의 설정을 포함한 주제 목록 조회
function topicRender() {
  fetch(`/api/members/me/topics`)
    .then((res) => {
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
  const checkboxId = `topic_${topic.topicId}`;

  template.innerHTML = `
  <div class="topic-chip-item d-inline-block me-2 mb-2">
    <input type="checkbox" class="btn-check" id="${checkboxId}" value="${topic.topicId}" autocomplete="off">
    <label class="btn btn-outline-primary rounded-pill px-3 py-1" for="${checkboxId}">
      <i class="bi bi-plus-lg me-1 small"></i>${topic.name}
    </label>
  </div>
  `;
  const checkbox = template.content.querySelector("input[type='checkbox']");
  checkbox.checked = topic.checked;
  return template.content.firstElementChild;
}

const searchSelectContainer = document.querySelector('.search-select-container');
const regionTarget = searchSelectContainer.querySelector('.select-search.regions');
const cityTarget = searchSelectContainer.querySelector('.select-search.cities');
const regionSelect = new SearchSelect(regionTarget);
const citySelect = new SearchSelect(cityTarget);
regionSelect.load(`/api/location/regions`, {
  valueKey: 'regionId',
  labelKey: 'nameLocal',
  includeAll: true,
  allLabel: window.i18n ? window.i18n.search.region : '도/시',
});
citySelect.load(`/api/location/cities`, {
  valueKey: 'cityId',
  labelKey: 'fullNameLocal',
});
regionTarget.addEventListener('change', (e) => {
  changeRegion(regionSelect.getValue());
});
cityTarget.addEventListener('change', (e) => {
  changeCity(citySelect.getText());
});

function changeRegion(regionId) {
  const params = new URLSearchParams();
  if (regionId != null) {
    params.append('regionId', regionId);
  }
  citySelect.load(`/api/location/cities?${params.toString()}`, {
    valueKey: 'cityId',
    labelKey: 'fullNameLocal',
  });
}

function changeCity(cityText) {
  document.querySelector('#currentCity').textContent = cityText;
}

// 최종 저장
// function save() {
//   const cityId = citySelect.getValue();
//   let result = updateTopic();
//   if (cityId != null && cityId !== '') {
//     result = result && updateCity();
//   }
//   if (result) {
//     alert('저장 되었습니다.');
//     location.reload();
//   } else {
//     alert('failed');
//   }
// }

async function updateTopic() {
  const topicIds = [...topicTarget.querySelectorAll('input[type=checkbox]:checked')].map((el) => el.value);
  const res = await fetch(`${API_BASE}/me/topics`, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      topicIds: topicIds,
    }),
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
    alert('saved');
  }
}

async function updateCity() {
  const cityId = citySelect.getValue();
  const res = await fetch(`${API_BASE}/me/city`, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      cityId: cityId,
    }),
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
    alert('saved');
  }
}
