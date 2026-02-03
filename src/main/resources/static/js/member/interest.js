const TOPIC_API_BASE = '/api/topics';
const CITY_API_BASE = '/api/location';
const topicTarget = document.querySelector('#topicTarget');
const cityTarget = document.querySelector('#cityTarget');

topicRender();
regionRender();
document.querySelector('#citySearch').addEventListener('input', cityRender);

/**
 * 관심 주제 설정
 */
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

/**
 * 관심 지역 설정
 */
// 도/시, 현
function regionRender() {
  const regionList = cityTarget.querySelector('#regionList');
  fetch(`${CITY_API_BASE}/regions`)
    .then((res) => {
      if (res.ok) {
        return res.json();
      } else {
        alert(res.status);
      }
    })
    .then((data) => {
      regionList.innerHTML = '';
      data.forEach((region) => {
        regionList.appendChild(makeRegionElement(region));
      });
    });
}

function makeRegionElement(region) {
  const li = document.createElement('li');
  li.textContent = region.nameLocal;
  li.dataset.regionId = region.regionId;
  li.addEventListener('click', selectRegion);
  return li;
}
function selectRegion(ev) {
  const regionInput = cityTarget.querySelector('#regionInput');
  regionInput.value = ev.target.dataset.regionId;
  cityRender();
}

// 구/군, 시/정/마을
function cityRender() {
  const search = cityTarget.querySelector('#citySearch').value;
  const regionId = cityTarget.querySelector('#regionInput').value;
  const cityList = cityTarget.querySelector('#cityList');
  const params = new URLSearchParams();
  params.append('keyword', search);
  if (regionId !== '' && regionId != null) {
    params.append('regionId', regionId);
  }
  fetch(`${CITY_API_BASE}/cities?${params.toString()}`)
    .then((res) => {
      if (res.ok) {
        return res.json();
      } else {
        alert(res.status);
      }
    })
    .then((data) => {
      cityList.innerHTML = '';
      data.forEach((city) => {
        cityList.appendChild(makeCityElement(city));
      });
    });
}

function makeCityElement(city) {
  const li = document.createElement('li');
  li.textContent = city.nameLocal;
  li.dataset.cityId = city.cityId;
  li.addEventListener('click', selectCity);
  return li;
}
function selectCity(ev) {
  const currentCity = cityTarget.querySelector('#currentCity');
  const cityInput = cityTarget.querySelector('#cityInput');
  currentCity.textContent = ev.target.textContent;
  cityInput.value = ev.target.dataset.cityId;
}

function updateCity() {
  const cityId = cityTarget.querySelector('#cityInput').value;
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

// 최종 저장
async function save() {
  const topic = await updateTopic();
  const msg = `관심 주제 수정: ${topic ? '성공' : '실패'}`;
  const cityId = cityTarget.querySelector('#cityInput').value;
  if (cityId !== '') {
    const city = await updateCity();
    msg += `관심 지역 수정: ${city ? '성공' : '실패'}`;
  }
  alert(msg);
}
