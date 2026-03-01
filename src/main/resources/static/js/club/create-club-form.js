const searchSelectContainer = document.querySelector('#createClubForm');
const regionTarget = searchSelectContainer.querySelector('.select-search.regions');
const cityTarget = searchSelectContainer.querySelector('.select-search.cities');
const topicTarget = searchSelectContainer.querySelector('.select-search.topics');

const regionSelect = new SearchSelect(regionTarget);
const citySelect = new SearchSelect(cityTarget);
const topicSelect = new SearchSelect(topicTarget);

regionSelect.load(`/api/location/regions`, {
  valueKey: 'regionId',
  labelKey: 'nameLocal',
  includeAll: true,
  allLabel: window.i18n.search.region,
});
citySelect.load(`/api/location/cities`, {
  valueKey: 'cityId',
  labelKey: 'fullNameLocal',
  includeAll: true,
  allLabel: window.i18n.search.city,
});
topicSelect.load(`/api/topics`, {
  valueKey: 'topicId',
  labelKey: 'name',
  includeAll: true,
  allLabel: window.i18n.search.topic,
});
regionTarget.addEventListener('change', (e) => {
  changeRegion(regionSelect.getValue());
});

function changeRegion(regionId) {
  const params = new URLSearchParams();
  if (regionId != null) {
    params.append('regionId', regionId);
  }
  citySelect.load(`/api/location/cities?${params.toString()}`, {
    valueKey: 'cityId',
    labelKey: 'fullNameLocal',
    includeAll: true,
    allLabel: window.i18n.search.city,
  });
}
