const headerSearchForm = document.querySelector('#headerSearchForm');
const headerRegionTarget = headerSearchForm.querySelector('.select-search.regions');
const headerCityTarget = headerSearchForm.querySelector('.select-search.cities');
const headerTopicTarget = headerSearchForm.querySelector('.select-search.topics');

const headerRegionSelect = new SearchSelect(headerRegionTarget);
const headerCitySelect = new SearchSelect(headerCityTarget);
const headerTopicSelect = new SearchSelect(headerTopicTarget);

headerRegionSelect.load(`/api/location/regions`, {
  valueKey: 'regionId',
  labelKey: 'nameLocal',
  includeAll: true,
  allLabel: window.i18n ? window.i18n.search.region : '도/시',
});
headerCitySelect.load(`/api/location/cities`, {
  valueKey: 'cityId',
  labelKey: 'fullNameLocal',
  includeAll: true,
  allLabel: window.i18n ? window.i18n.search.city : '구/군',
});
headerTopicSelect.load(`/api/topics`, {
  valueKey: 'topicId',
  labelKey: 'name',
  includeAll: true,
  allLabel: window.i18n ? window.i18n.search.topic : '관심사',
});
headerRegionTarget.addEventListener('change', (e) => {
  headerChangeRegion(headerRegionSelect.getValue());
});

function headerChangeRegion(regionId) {
  const params = new URLSearchParams();
  if (regionId != null) {
    params.append('regionId', regionId);
  }
  headerCitySelect.load(`/api/location/cities?${params.toString()}`, {
    valueKey: 'cityId',
    labelKey: 'fullNameLocal',
  });
}
(() => {
  const params = new URLSearchParams(window.location.search);
  const topicId = params.get('topicId');
  const regionId = params.get('regionId');
  const cityId = params.get('cityId');
  const searchInput = headerSearchForm.querySelector(`input[name=q]`);
  const q = params.get('q');
  if (topicId) {
    headerTopicSelect.onReady(() => {
      headerTopicSelect.select(headerTopicTarget.querySelector(`li[data-value='${topicId}']`));
    });
  }
  if (regionId) {
    headerRegionSelect.onReady(() => {
      headerRegionSelect.select(headerRegionTarget.querySelector(`li[data-value='${regionId}']`));
    });
  }
  if (cityId) {
    headerCitySelect.onReady(() => {
      headerCitySelect.select(headerCityTarget.querySelector(`li[data-value='${cityId}']`));
    });
  }
  if (q) {
    searchInput.value = q;
  }
})();
