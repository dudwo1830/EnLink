let filters = {
  cityId: null,
  topicId: null,
  search: null,
  regionId: null,
};

export function setCity(cityId) {
  filters.cityId = cityId;
}

export function setTopic(topicId) {
  filters.topicId = topicId;
}

export function setSearch(keyword) {
  filters.search = keyword?.trim() || null;
}

export function setRegion(regionId) {
  filters.regionId = regionId;
}

export function getFilters() {
  return { ...filters };
}
