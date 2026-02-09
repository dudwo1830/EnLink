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
    changeCity(data.nameLocal);
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

const regionTarget = document.querySelector('.select-search.regions');
const cityTarget = document.querySelector('.select-search.cities');
const regionSelect = new SearchSelect(regionTarget);
const citySelect = new SearchSelect(cityTarget);
regionSelect.load(`/api/location/regions`, {
  valueKey: 'regionId',
  labelKey: 'nameLocal',
  includeAll: true,
  allLabel: '도/시 전체',
});
citySelect.load(`/api/location/cities`, {
  valueKey: 'cityId',
  labelKey: 'fullNameLocal',
  includeAll: true,
  allLabel: '지역 전체',
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
    labelKey: regionId === '' ? 'fullNameLocal' : 'nameLocal',
    includeAll: true,
    allLabel: '지역 전체',
  });
}

function changeCity(cityText) {
  document.querySelector('#currentCity').textContent = cityText;
}

// 최종 저장
async function save() {
  const cityId = citySelect.getValue();

  const topic = await updateTopic();
  let msg = `관심 주제 수정: ${topic ? '성공' : '실패'}`;
  if (cityId != null && cityId !== '') {
    const city = await updateCity();
    msg += `관심 지역 수정: ${city ? '성공' : '실패'}`;
  }
  alert(msg);
}

function updateTopic() {
  const topicIds = [...topicTarget.querySelectorAll('input[type=checkbox]:checked')].map((el) => el.value);
  return fetch(`${API_BASE}/me/topics`, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      topicIds: topicIds,
    }),
  }).then((res) => {
    return res.ok;
  });
}

function updateCity() {
  const cityId = citySelect.getValue();
  return fetch(`${API_BASE}/me/city`, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      cityId: cityId,
    }),
  }).then((res) => {
    return res.ok;
  });
}
