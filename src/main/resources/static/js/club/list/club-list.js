import { resetPaging, nextPage } from './club-paging.js';
import { setCity, setTopic, setSearch, setRegion, getFilters } from './club-search-filter.js';

const CLUB_API_BASE = '/api/clubs';

// --- [신규 모임 전용 페이징 변수] ---
let newClubPage = 0;
let isNewClubLast = false;

// 1. 요소 선택
const clubListTarget = document.querySelector('#clubList');      // 추천/검색 결과 영역
const newClubListTarget = document.querySelector('#newClubList'); // 신규 모임 영역
const clubListMoreBtn = document.querySelector('#clubListMore');  // 추천 더보기 버튼
const newClubMoreBtn = document.querySelector('#newClubMoreBtn'); // 신규 더보기 버튼
const searchInput = document.querySelector('#searchInput');

const regionTarget = document.querySelector('.select-search.regions');
const cityTarget = document.querySelector('.select-search.cities');
const topicTarget = document.querySelector('.select-search.topics');

// 2. 이벤트 바인딩
if (clubListMoreBtn) clubListMoreBtn.onclick = () => clubListRender();
if (newClubMoreBtn) newClubMoreBtn.onclick = () => loadHomeNewClubs();

if (searchInput) {
    const handleSearchInput = debounce((value) => { changeSearch(value); }, 200);
    searchInput.oninput = () => handleSearchInput(searchInput.value);
}

// 3. 영재 상의 SearchSelect 엔진 초기화
if (regionTarget && cityTarget && topicTarget) {
    const regionSelect = new SearchSelect(regionTarget);
    const citySelect = new SearchSelect(cityTarget);
    const topicSelect = new SearchSelect(topicTarget);

    // 💡 주소 확인! 404 방지를 위해 /api/location/... 으로 수정
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

// 4. 초기 로드
if (newClubListTarget) loadHomeNewClubs();
if (clubListTarget) clubListRender();

// --- [기능 함수들] ---

/**
 * 🆕 신규 모임 로드 로직 (검색 필터의 영향을 받지 않음)
 */
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

/**
 * 🔍 추천/검색 모임 렌더링 (영재 상의 필터와 연동됨)
 */
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
}

/**
 * ♻️ 필터 변경 시 리셋 (영재 상의 로직 유지)
 */
function resetAndRender() {
    resetPaging();
    if (clubListTarget) clubListTarget.innerHTML = ''; // 추천 리스트만 비움
    clubListRender();
}

// --- [기존 필터 유틸리티 함수 유지] ---
function changeRegion(regionId, citySelect) {
    const params = new URLSearchParams();
    if (regionId != null) params.append('regionId', regionId);
    citySelect.load(`/api/location/cities?${params.toString()}`, {
        valueKey: 'cityId', labelKey: regionId === '' ? 'fullNameLocal' : 'nameLocal', includeAll: true, allLabel: '지역 전체',
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