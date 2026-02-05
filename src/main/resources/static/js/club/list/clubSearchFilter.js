let filters = {
  cityId: null,
  topicId: null,
  search: null,
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

export function getFilters() {
  return { ...filters };
}
