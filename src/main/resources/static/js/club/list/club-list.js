import { resetPaging, nextPage } from './club-paging.js';

import { setCity, setTopic, setSearch, setRegion, getFilters } from './club-search-filter.js';

<<<<<<< HEAD


const CLUB_API_BASE = '/api/clubs';



// 1. 요소 선택 (에러 방지를 위해 존재 여부 체크 필수)

const clubListTarget = document.querySelector('#clubList');

const newClubListTarget = document.querySelector('#newClubList');

const clubListMoreBtn = document.querySelector('#clubListMore');

const searchInput = document.querySelector('#searchInput');



const regionTarget = document.querySelector('.select-search.regions');

const cityTarget = document.querySelector('.select-search.cities');

const topicTarget = document.querySelector('.select-search.topics');



// 2. 이벤트 바인딩 (요소가 있을 때만 실행)

if (clubListMoreBtn) clubListMoreBtn.onclick = () => clubListRender();



if (searchInput) {

const handleSearchInput = debounce((value) => {

changeSearch(value);

}, 200);

searchInput.oninput = () => handleSearchInput(searchInput.value);

}



// 3. SearchSelect 초기화 (요소가 있을 때만)

if (regionTarget && cityTarget && topicTarget) {

const regionSelect = new SearchSelect(regionTarget);

const citySelect = new SearchSelect(cityTarget);

const topicSelect = new SearchSelect(topicTarget);



regionSelect.load(`/api/location/regions`, {

valueKey: 'regionId', labelKey: 'nameLocal', includeAll: true, allLabel: '도/시 전체',

});

citySelect.load(`/api/location/cities`, {

valueKey: 'cityId', labelKey: 'fullNameLocal', includeAll: true, allLabel: '지역 전체',

});

topicSelect.load(`/api/topics`, {

valueKey: 'topicId', labelKey: 'name', includeAll: true, allLabel: '주제 전체',

});



regionTarget.addEventListener('change', (e) => changeRegion(e.detail.value, citySelect));

cityTarget.addEventListener('change', (e) => changeCity(e.detail.value));

topicTarget.addEventListener('change', (e) => changeTopic(e.detail.value));

}



// 4. 초기 실행

if (newClubListTarget) loadHomeNewClubs(); // 홈 화면 전용

if (clubListTarget) clubListRender(); // 추천 리스트



// --- [기능 함수들] ---



function clubListRender() {

const filters = getFilters();

const page = nextPage();

fetchClubList({ filters, page }).then(renderClubList).catch(console.error);

}



function fetchClubList({ filters, page }) {

const params = new URLSearchParams();

Object.entries(filters).forEach(([key, value]) => {

if (value != null && value !== '') params.append(key, value);

});

params.append('page', page);

return fetch(`${CLUB_API_BASE}?${params.toString()}`).then(res => res.json());

}



function renderClubList(data) {

if (!clubListTarget) return;

data.content.forEach(club => clubListTarget.appendChild(makeClubElement(club)));

if (data.last && clubListMoreBtn) clubListMoreBtn.style.display = 'none';

}



/**

* ✨ 신규 모임 8개 로드

*/

async function loadHomeNewClubs() {

try {

const response = await fetch(`${CLUB_API_BASE}?size=5&sort=createdAt,desc`);

const data = await response.json();

if (newClubListTarget) {

newClubListTarget.innerHTML = '';

data.content.forEach(club => newClubListTarget.appendChild(makeClubElement(club)));

}

} catch (e) {

console.error("신규 모임 로드 실패:", e);

}

}



function makeClubElement(club) {

const template = document.createElement('template');



template.innerHTML = `

<div class="club-card">

<a href="/club/${club.clubId}">

<div class="card-img">

<img src="${club.imageUrl || '/images/default_club.jpg'}" alt="모임 이미지">

</div>

<div class="card-body">

<p class="club-title">${club.name}</p>

<p class="club-description">${club.description || ''}</p>

<p class="club-info">

<span>${club.topicName}</span>

<span>${club.cityName}</span>

</p>

</div>

</a>

</div>

`;

return template.content.firstElementChild;

}



// 필터 로직들 (기존과 동일하되 시티셀렉트 인자 추가)

function changeRegion(regionId, citySelect) {

const params = new URLSearchParams();

if (regionId != null) params.append('regionId', regionId);

citySelect.load(`/api/location/cities?${params.toString()}`, {

valueKey: 'cityId', labelKey: regionId === '' ? 'fullNameLocal' : 'nameLocal', includeAll: true, allLabel: '지역 전체',

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

=======
const CLUB_API_BASE = '/api/clubs';
const RECOMMEND_API = '/api/club/recommend';

// --- [신규 모임 전용 페이징 변수] ---
let newClubPage = 0;
let isNewClubLast = false;

// --- [추천 모임 전용 슬라이싱 변수] ---
let recommendPage = 0;
const recommendSize = 5;
let cachedRecommendedClubs = [];

// 1. 요소 선택
const clubListTarget = document.querySelector('#clubList');
const newClubListTarget = document.querySelector('#newClubList');
const clubListMoreBtn = document.querySelector('#clubListMore');
const newClubMoreBtn = document.querySelector('#newClubMoreBtn');
const searchInput = document.querySelector('#searchInput');

// 2. 이벤트 바인딩
if (clubListMoreBtn) clubListMoreBtn.onclick = () => {
    const filters = getFilters();
    const hasFilter = filters.regionId || filters.cityId || filters.topicId || filters.search;

    if (hasFilter) {
        clubListRender();
    } else {
        loadRecommendedClubs();
    }
};
if (newClubMoreBtn) newClubMoreBtn.onclick = () => loadHomeNewClubs();

if (searchInput) {
    const handleSearchInput = debounce((value) => { changeSearch(value); }, 200);
    searchInput.oninput = () => handleSearchInput(searchInput.value);
}

// 3. SearchSelect 엔진 초기화
(() => {
    const regionTarget = document.querySelector('.select-search.regions');
    const cityTarget = document.querySelector('.select-search.cities');
    const topicTarget = document.querySelector('.select-search.topics');

    const regionSelect = (headerRegionSelect) ? headerRegionSelect : new SearchSelect(regionTarget);
    const citySelect = (headerCitySelect) ? headerCitySelect : new SearchSelect(cityTarget);
    const topicSelect = (headerTopicSelect) ? headerTopicSelect : new SearchSelect(topicTarget);

    if(!headerRegionSelect && !headerCitySelect && !headerTopicSelect){
        // 💡 주소 확인! 404 방지를 위해 /api/location/... 으로 수정
        regionSelect.load(`/api/location/regions`, {
            valueKey: 'regionId', labelKey: 'nameLocal', includeAll: true, allLabel: window.i18n.search.region,
        });
        citySelect.load(`/api/location/cities`, {
            valueKey: 'cityId', labelKey: 'fullNameLocal', includeAll: true, allLabel: window.i18n.search.city,
        });
        topicSelect.load(`/api/topics`, {
            valueKey: 'topicId', labelKey: 'name', includeAll: true, allLabel: window.i18n.search.topic,
        });

        regionTarget.addEventListener('change', (e) => changeRegion(e.detail.value, citySelect));
        cityTarget.addEventListener('change', (e) => changeCity(e.detail.value));
        topicTarget.addEventListener('change', (e) => changeTopic(e.detail.value));
    }else{
        regionTarget.addEventListener('change', resetAndRender);
        cityTarget.addEventListener('change', resetAndRender);
        topicTarget.addEventListener('change', resetAndRender);
    }
})();

// 4. 초기 로드
if (newClubListTarget) loadHomeNewClubs();
if (clubListTarget) loadRecommendedClubs();

// --- [기능 함수들] ---

async function loadRecommendedClubs() {
    try {
        if (recommendPage === 0) {
            const res = await fetch(RECOMMEND_API);
            if (!res.ok) throw new Error("추천 API 호출 실패");
            cachedRecommendedClubs = await res.json();
            if (clubListTarget) clubListTarget.innerHTML = '';

            if (cachedRecommendedClubs.length === 0) {
                clubListRender();
                return;
            }
        }

        const start = recommendPage * recommendSize;
        const end = start + recommendSize;
        const itemsToShow = cachedRecommendedClubs.slice(start, end);

        if (clubListTarget && itemsToShow.length > 0) {
            itemsToShow.forEach(club => clubListTarget.appendChild(makeClubElement(club)));
            recommendPage++;
        }

        if (clubListMoreBtn) {
            clubListMoreBtn.style.display = (end >= cachedRecommendedClubs.length) ? 'none' : 'block';
        }
    } catch (e) {
        console.error("추천 로드 실패:", e);
        if (recommendPage === 0) clubListRender();
    }
}

async function loadHomeNewClubs() {
    if (isNewClubLast) return;
    try {
        const params = new URLSearchParams();
        params.append('size', '5');
        params.append('page', newClubPage.toString());
        params.append('sort', 'createdAt,desc');

        const res = await fetch(`${CLUB_API_BASE}?${params.toString()}`);
        const data = await res.json();

        if (newClubListTarget) {
            data.content.forEach(club => newClubListTarget.appendChild(makeClubElement(club)));
        }
        isNewClubLast = data.last;
        newClubPage++;
        if (isNewClubLast && newClubMoreBtn) newClubMoreBtn.style.display = 'none';
    } catch (e) { console.error("신규 모임 로드 실패:", e); }
}

function clubListRender() {
    const filters = getFilters();
    const page = nextPage();

    const params = new URLSearchParams();
    Object.entries(filters).forEach(([key, value]) => {
        if (value != null && value !== '') params.append(key, value);
    });
    params.append('size', '5');
    params.append('page', page);

    fetch(`${CLUB_API_BASE}?${params.toString()}`)
        .then(res => res.json())
        .then(data => {
            if (!clubListTarget) return;
            data.content.forEach(club => clubListTarget.appendChild(makeClubElement(club)));
            if (clubListMoreBtn) clubListMoreBtn.style.display = data.last ? 'none' : 'block';
        }).catch(console.error);
>>>>>>> origin/develop
}



function resetAndRender() {
<<<<<<< HEAD

resetPaging();

if (clubListTarget) clubListTarget.innerHTML = '';

if (clubListMoreBtn) clubListMoreBtn.style.display = 'block';

clubListRender();

}



function debounce(fn, delay = 300) {

let timer = null;

return (...args) => {

clearTimeout(timer);

timer = setTimeout(() => fn(...args), delay);

};

=======
    resetPaging();
    if (clubListTarget) clubListTarget.innerHTML = '';
    clubListRender();
}

function changeRegion(regionId, citySelect) {
    const params = new URLSearchParams();
    if (regionId != null) params.append('regionId', regionId);
    citySelect.load(`/api/location/cities?${params.toString()}`, {
        valueKey: 'cityId',
        labelKey: regionId === '' ? 'fullNameLocal' : 'nameLocal',
        includeAll: true,
        allLabel: window.i18n.search.region
    });
    setRegion(regionId);
    changeCity(null);
}
function changeCity(cityId) { setCity(cityId); resetAndRender(); }
function changeTopic(topicId) { setTopic(topicId); resetAndRender(); }
function changeSearch(search) { setSearch(search); resetAndRender(); }
function debounce(fn, delay = 300) {
    let timer = null;
    return (...args) => { clearTimeout(timer); timer = setTimeout(() => fn(...args), delay); };
}

function makeClubElement(club) {
    const template = document.createElement('template');
    template.innerHTML = `
        <div class="club-card">
            <a href="/club/${club.clubId}">
                <div class="card-img">
                    <img src="${club.imageUrl || '/images/default_club.jpg'}" alt="모임 이미지">
                </div>
                <div class="card-body">
                    <p class="club-title">${club.name}</p>

                    <p class="club-description">${club.description || ''}</p>

                    <p class="tag-topic-inline">${club.topicName || '주제'}</p>

                    <div class="info-bottom-vertical">
                        <span class="location-tag">
                            <i class="bi bi-geo-alt"></i> ${club.cityName || '지역'}
                        </span>

                        <div class="member-info">
                            <i class="bi bi-people-fill"></i>
                            <span class="current">${club.currentMemberCount || 0}</span>
                            <span class="total">/ ${club.maxMember ? club.maxMember : (club.maxMemberCount || 0)}</span>
                        </div>
                    </div>
                </div>
            </a>
        </div>
    `;
    return template.content.firstElementChild;
>>>>>>> origin/develop
}