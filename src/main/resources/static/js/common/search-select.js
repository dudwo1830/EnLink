/**
 * input + ul-li 를 사용한 검색형 셀렉트 박스
 *
 * @class SearchSelect
 */
class SearchSelect {
  constructor(root) {
    // input, hidden input, ul-li 자식 요소를 가진 부모 요소
    this.root = root;
    // root 자식 요소 중 select-input 클래스를 가진 input 요소
    this.input = root.querySelector('.select-input');
    // root 자식 요소 중 select-hidden 클래스를 가진 input 요소
    this.hidden = root.querySelector('.select-hidden');
    // root 자식 요소 중 select-options 클래스를 가진 ul 요소
    this.options = root.querySelector('.select-options');

    // hidden의 name설정
    this.hidden.name = root.dataset.name;

    //onload
    this.isReady = false;
    this.readyCallbacks = [];

    // init
    this.bindEvents();
    this.close();
  }

  bindEvents() {
    this.input.addEventListener('focus', () => {
      this.open();
    });
    this.input.addEventListener('input', () => {
      this.filter(this.input.value);
    });
    this.options.addEventListener('click', (ev) => {
      this.select(ev.target);
    });

    //다른 곳 클릭 시 숨김
    document.addEventListener('click', (ev) => {
      if (!this.root.contains(ev.target)) {
        this.close();
      }
    });
  }

  getText() {
    return this.input.value;
  }
  getValue() {
    return this.hidden.value;
  }
  // 옵션 보이기
  open() {
    this.options.style.display = 'block';
  }

  // 옵션 숨기기
  close() {
    this.options.style.display = 'none';
  }

  //검색 필터
  filter(keyword) {
    // 대소문자 구분 안함
    const lower = keyword.toLowerCase();

    // keyword가 포함된 옵션만 보이기
    [...this.options.children].forEach((li) => {
      li.style.display = li.textContent.toLowerCase().includes(lower) ? 'block' : 'none';
    });
  }

  // 선택
  select(li) {
    this.input.value = li.textContent;
    this.hidden.value = li.dataset.value;
    this.close();

    // 커스텀 이벤트
    this.root.dispatchEvent(
      new CustomEvent('change', {
        detail: {
          value: li.dataset.value,
          label: li.textContent,
        },
      }),
    );
  }

  // json 데이터를 세팅
  setOptions(data, { valueKey, labelKey, includeAll, allLabel = '전체', allValue = '' } = {}) {
    // 기존 li 비우기
    this.options.innerHTML = '';

    // 전체 항목 추가
    if (includeAll) {
      const allLi = document.createElement('li');
      allLi.classList.add('options');
      allLi.dataset.value = allValue;
      allLi.textContent = allLabel;
      this.options.appendChild(allLi);
      // 기본값 처리
      this.setDefaultOption(allLabel, allValue);
    }

    data.forEach((item) => {
      const li = document.createElement('li');
      li.classList.add('options');
      li.dataset.value = item[valueKey];
      li.textContent = item[labelKey];
      this.options.appendChild(li);
    });

    this.isReady = true;
    this.readyCallbacks.forEach(cb => cb(this));
  }

  setDefaultOption(label = '전체', value = '') {
    this.input.value = label;
    this.hidden.value = value;
  }

  async load(url, mapping) {
    const res = await fetch(url);
    if (!res.ok) return;
    const data = await res.json();
    this.setOptions(data, mapping);
  }

  onReady(callback) {
    if (this.isReady) {
      callback(this);
    } else {
      this.readyCallbacks.push(callback);
    }
  }
}
