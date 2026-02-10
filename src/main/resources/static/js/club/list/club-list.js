import { resetPaging, nextPage } from './club-paging.js';
import { setCity, setTopic, setSearch, setRegion, getFilters } from './club-search-filter.js';

//베이스 api 주소
const CLUB_API_BASE = '/api/clubs';
// 요소가 배치될 컨테이너
const clubListTarget = document.querySelector('#clubList');
// 더 보기 버튼
const clubListMoreBtn = document.querySelector('#clubListMore');
clubListMoreBtn.onclick = () => clubListRender();
// 검색어 요소
const searchInput = document.querySelector('#searchInput');
searchInput.oninput = () => changeSearch(searchInput.value);
// 도/시 요소
const regionTarget = document.querySelector('.select-search.regions');
// 지역 요소
const cityTarget = document.querySelector('.select-search.cities');
// 주제 요소
const topicTarget = document.querySelector('.select-search.topics');

/* SearchSelect */
const regionSelect = new SearchSelect(regionTarget);
const citySelect = new SearchSelect(cityTarget);
const topicSelect = new SearchSelect(topicTarget);
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
topicSelect.load(`/api/topics`, {
  valueKey: 'topicId',
  labelKey: 'name',
  includeAll: true,
  allLabel: '주제 전체',
});
regionTarget.addEventListener('change', (e) => {
  changeRegion(e.detail.value);
});
cityTarget.addEventListener('change', (e) => {
  changeCity(e.detail.value);
});
topicTarget.addEventListener('change', (e) => {
  changeTopic(e.detail.value);
});

// 최초 1회 실행
clubListRender();

// 리스트 가져오기
function clubListRender() {
  const filters = getFilters();
  const page = nextPage();

  fetchClubList({ filters, page }).then(renderClubList).catch(console.error);
}

function fetchClubList({ filters, page }) {
  const params = new URLSearchParams();

  Object.entries(filters).forEach(([key, value]) => {
    console.log(key, value);
    if (value != null && value !== '') {
      params.append(key, value);
    }
  });

  params.append('page', page);

  return fetch(`${CLUB_API_BASE}?${params.toString()}`).then((res) => {
    if (!res.ok) throw new Error('API Error');
    return res.json();
  });
}

// 요소 생성 및 배치
function renderClubList(data) {
  data.content.forEach((club) => {
    clubListTarget.appendChild(makeClubElement(club));
  });

  if (data.last) {
    clubListMoreBtn.style.display = 'none';
  }
}
// 요소 생성 및 반환
function makeClubElement(club) {
  const template = document.createElement('template');
  template.innerHTML = `
		<div class="card">
			<a href="/club/${club.clubId}">
				<img th:src="@{${club.imageUrl}}" alt="모임 대표 이미지">
				<p class="club-title">${club.name}</p>
				<p class="club-description">${club.description}</p>
				<p class="club-info">
					<span>${club.topicName}</span>
					<span>${club.cityName}</span>
				</p>
			</a>
		</div>
	`;

  return template.content.firstElementChild;
}

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
  setRegion(regionId);
  changeCity(null);
}

function changeCity(cityId) {
  setCity(cityId);
  resetAndRender();
}

function changeTopic(topicId) {
  setTopic(topicId);
  resetAndRender();
}

function changeSearch(search) {
  setSearch(search);
  resetAndRender();
}

function resetAndRender() {
  resetPaging();
  clubListTarget.innerHTML = '';
  clubListMoreBtn.style.display = 'block';
  clubListRender();
}
