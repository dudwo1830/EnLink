let page = 0;

export function resetPaging() {
  page = 0;
}

export function nextPage() {
  return page++;
}
