import { resetPaging, nextPage } from './clubPaging.js';
import { setCity, setTopic, setSearch, getFilters } from './clubSearchFilter.js';

//베이스 api 주소
const CLUB_API_BASE = '/api/clubs';
// 요소가 배치될 컨테이너
const clubListTarget = document.querySelector('#clubList');
// 더 보기 버튼
const clubListMoreBtn = document.querySelector('#clubListMore');
// 지역 요소
const cities = document.querySelectorAll('.city');
// 주제 요소
const topics = document.querySelectorAll('.topic');
// 검색어 요소
const searchInput = document.querySelector('#searchInput');

clubListMoreBtn.onclick = () => clubListRender();
cities.forEach((cityLi) => {
  cityLi.onclick = (e) => changeCity(e.target.dataset.cityId);
});
topics.forEach((topicLi) => {
  topicLi.onclick = (e) => changeTopic(e.target.dataset.topicId);
});
searchInput.oninput = () => changeSearch(searchInput.value);

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
		<li class="club-card">
			<a href="/club/${club.clubId}">
				<img th:src="@{${club.imageUrl}}" alt="모임 대표 이미지">
				<p class="club-title">${club.name}</p>
				<p class="club-description">${club.description}</p>
				<p class="club-info">
					<span>${club.topicName}</span>
					<span>${club.cityName}</span>
				</p>
			</a>
		</li>
	`;

  return template.content.firstElementChild;
}

function changeCity(cityId) {
  console.log(cityId);
  setCity(cityId);
  resetPaging();
  clubListTarget.innerHTML = '';
  clubListMoreBtn.style.display = 'block';

  clubListRender();
}

function changeTopic(topicId) {
  setTopic(topicId);
  resetPaging();
  clubListTarget.innerHTML = '';
  clubListMoreBtn.style.display = 'block';

  clubListRender();
}

function changeSearch(search) {
  setSearch(search);
  resetPaging();
  clubListTarget.innerHTML = '';
  clubListMoreBtn.style.display = 'block';

  clubListRender();
}
