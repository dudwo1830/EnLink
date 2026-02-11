const regionTarget = document.querySelector('.select-search.regions');
const cityTarget = document.querySelector('.select-search.cities');
const topicTarget = document.querySelector('.select-search.topics');

const regionSelect = new SearchSelect(regionTarget);
const citySelect = new SearchSelect(cityTarget);
const topicSelect = new SearchSelect(topicTarget);

regionSelect.load(`/api/location/regions`, {
  valueKey: 'regionId',
  labelKey: 'nameLocal',
  includeAll: true,
  allLabel: '도/시 선택',
  allValue: null
});
citySelect.load(`/api/location/cities`, {
  valueKey: 'cityId',
  labelKey: 'fullNameLocal',
  includeAll: true,
  allLabel: '구/군 선택',
  allValue: null
});
topicSelect.load(`/api/topics`, {
  valueKey: 'topicId',
  labelKey: 'name',
  includeAll: true,
  allLabel: '관심사 선택',
  allValue: null
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
  });
}