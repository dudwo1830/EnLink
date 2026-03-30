(function () {
  'use strict';

  const $ = (sel, parent = document) => parent.querySelector(sel);

  function getParam(name, defaultValue = '') {
    const url = new URL(location.href);
    return url.searchParams.get(name) ?? defaultValue;
  }

  function setParams(params) {
    const url = new URL(location.href);
    Object.entries(params).forEach(([k, v]) => {
      if (v === null || v === undefined || v === '') url.searchParams.delete(k);
      else url.searchParams.set(k, v);
    });
    history.replaceState({}, '', url.toString());
  }

  function fmtDate(isoLikeString) {
    // 서버에서 createdAt이 String으로 오는 구조라(예: "2026-02-21 13:22" / ISO 등) 최대한 안전하게 처리
    if (!isoLikeString) return '';
    // "2026-02-21T13:22:00" 또는 "2026-02-21 13:22:00" 대응
    const safe = isoLikeString.replace(' ', 'T');
    const d = new Date(safe);
    if (isNaN(d.getTime())) return isoLikeString;
    const yyyy = d.getFullYear();
    const mm = String(d.getMonth() + 1).padStart(2, '0');
    const dd = String(d.getDate()).padStart(2, '0');
    return `${yyyy}-${mm}-${dd}`;
  }

  function escapeHtml(text) {
    return String(text)
      .replaceAll('&', '&amp;')
      .replaceAll('<', '&lt;')
      .replaceAll('>', '&gt;')
      .replaceAll('"', '&quot;')
      .replaceAll("'", '&#039;');
  }

  // =========================
  // Post List
  // =========================
  async function initPostList(root) {
    const clubId = root.dataset.clubId;
    if (!clubId) return;

    // URL 파라미터 (없으면 기본)
    const searchType = getParam('searchType', 'all');
    const searchKeyword = getParam('searchKeyword', '');
    const page = Number(getParam('page', '0')) || 0;

    // 폼 초기값 반영
    const sel = $('#searchType');
    const input = $('#searchKeyword');
    if (sel) sel.value = searchType;
    if (input) input.value = searchKeyword;

    // fetch
    const url = new URL('/api/posts', location.origin);
    url.searchParams.set('clubId', clubId);
    url.searchParams.set('searchType', searchType);
    if (searchKeyword) url.searchParams.set('searchKeyword', searchKeyword);
    url.searchParams.set('page', String(page));

    const res = await fetch(url.toString());
    if (!res.ok) {
      alert('게시글 목록을 불러오지 못했습니다.');
      return;
    }
    const pageData = await res.json();

    renderPostCards(pageData.content || []);
    renderPagination(pageData, { clubId, searchType, searchKeyword });
  }

  function renderPostCards(posts) {
    const listEl = $('#postList');
    if (!listEl) return;

    if (!posts.length) {
      listEl.innerHTML = `
        <div class="post-card">
          <div class="post-card-title">게시글이 없습니다</div>
          <div class="meta">첫 글을 작성해보세요!</div>
        </div>
      `;
      return;
    }

    listEl.innerHTML = posts
      .map((post) => {
        const badge = post.isNotice
          ? `<span class="badge badge-notice">공지</span>`
          : '';
        const title = escapeHtml(post.title ?? '');
        const memberId = escapeHtml(post.memberId ?? '');
        const date = fmtDate(post.createdAt);

        return `
          <article class="post-card">
            <a href="/community/post/detail/${post.postId}">
              <div class="post-card-title">
                ${badge}
                <span>${title}</span>
              </div>
              <div class="meta">
                <span>작성자: ${memberId}</span>
                <span>작성일: ${date}</span>
              </div>
            </a>
          </article>
        `;
      })
      .join('');
  }

  function renderPagination(pageData, { clubId, searchType, searchKeyword }) {
    const pagEl = $('#pagination');
    if (!pagEl) return;

    const totalPages = pageData.totalPages ?? 0;
    const current = pageData.number ?? 0; // 0-based
    if (totalPages <= 1) {
      pagEl.innerHTML = '';
      return;
    }

    // ✅ 5개 단위 블록 페이징
    const blockSize = 5;

    // current가 0~4면 start=0, 5~9면 start=5 ...
    const start = Math.floor(current / blockSize) * blockSize;
    const end = Math.min(totalPages - 1, start + blockSize - 1);

    const makeBtn = (label, page, opts = {}) => {
      const active = opts.active ? 'active' : '';
      const disabled = opts.disabled ? 'disabled' : '';
      return `<button class="page-btn ${active}" ${disabled} data-page="${page}">${label}</button>`;
    };

    let html = '';

    // ✅ 블록 단위 "이전" (start-1로 이동)
    html += makeBtn('이전', start - 1, { disabled: start === 0 });

    // ✅ 5개만 렌더링
    for (let i = start; i <= end; i++) {
      html += makeBtn(String(i + 1), i, { active: i === current });
    }

    // ✅ 블록 단위 "다음" (end+1로 이동)
    html += makeBtn('다음', end + 1, { disabled: end >= totalPages - 1 });

    pagEl.innerHTML = html;

    // ⚠️ 중요: 매번 renderPagination 호출될 때마다 이벤트가 중복으로 붙는 문제 방지
    pagEl.onclick = (e) => {
      const btn = e.target.closest('button[data-page]');
      if (!btn) return;
      if (btn.disabled) return;

      const nextPage = Number(btn.dataset.page);

      const nextUrl = new URL(`/community/post/list/${clubId}`, location.origin);
      nextUrl.searchParams.set('searchType', searchType);
      if (searchKeyword) nextUrl.searchParams.set('searchKeyword', searchKeyword);
      nextUrl.searchParams.set('page', String(nextPage));
      location.href = nextUrl.toString();
    };
  }

  // =========================
  // Post Detail + Replies
  // =========================
  async function initPostDetail(root) {
    const postId = root.dataset.postId;
    if (!postId) return;

    const detailRes = await fetch(`/api/posts/detail/${postId}`);
    if (!detailRes.ok) {
      alert('게시글 정보를 불러오지 못했습니다.');
      return;
    }
    const post = await detailRes.json();

    $('#detailTitle').textContent = post.title ?? '';
    $('#detailMemberId').textContent = post.memberId ?? '';
    $('#detailCreatedAt').textContent = fmtDate(post.createdAt);

    // 공지 뱃지
    const badgeWrap = document.getElementById('detailBadges');
    if (badgeWrap) {
      badgeWrap.innerHTML = post.isNotice
        ? `<span class="badge badge-notice">공지</span>`
        : '';
    }

    // 이미지
    const imgWrap = $('#detailImageWrap');
    if (imgWrap) {
      imgWrap.innerHTML = post.imageUrl
        ? `<div class="post-image"><img src="/postImg/${post.imageUrl}" alt="post image"></div>`
        : '';
    }

    bindImageModal();

    // 내용(줄바꿈 유지)
    $('#detailContent').textContent = post.content ?? '';

    // 버튼 권한
    const editBtn = $('#editBtn');
    const deleteBtn = $('#deleteBtn');
    if (editBtn) editBtn.style.display = post.canEdit ? 'inline-flex' : 'none';
    if (deleteBtn)
      deleteBtn.style.display = post.canDelete ? 'inline-flex' : 'none';

    // 목록 버튼: clubId로 복귀
    const listLink = $('#listLink');
    if (listLink) listLink.href = `/community/post/list/${post.clubId}`;

    // 삭제
    if (deleteBtn) {
      deleteBtn.addEventListener('click', async () => {
        if (!confirm('정말 삭제할까요?')) return;
        const res = await fetch(`/api/posts/${postId}`, { method: 'DELETE' });
        if (res.ok) {
          alert('삭제되었습니다.');
          location.href = `/community/post/list/${post.clubId}`;
        } else {
          const msg = await res.text();
          alert(`삭제 실패: ${msg}`);
        }
      });
    }

    // 댓글
    await fetchReplies(postId);
    bindReplyCreate(postId);
  }

  async function fetchReplies(postId) {
    const res = await fetch(`/api/posts/${postId}/replies`);
    if (!res.ok) {
      $('#replyList').innerHTML =
        `<div class="reply-item">댓글을 불러오지 못했습니다.</div>`;
      return;
    }
    const replies = await res.json();
    $('#replyCount').textContent = String(replies.length);

    const list = $('#replyList');
    if (!replies.length) {
      list.innerHTML = `<div class="reply-item">아직 댓글이 없습니다.</div>`;
      return;
    }

    list.innerHTML = replies
      .map((r) => {
        const actions = r.canEdit
          ? `
            <div class="reply-actions">
              <button type="button" data-action="edit" data-id="${r.replyId}">수정</button>
              <button type="button" data-action="delete" data-id="${r.replyId}">삭제</button>
            </div>
          `
          : '';

        return `
          <div class="reply-item" data-reply-id="${r.replyId}">
            <div class="reply-top">
              <div>
                <strong>${escapeHtml(r.memberId ?? '')}</strong>
                <span> · ${fmtDate(r.createdAt)}</span>
              </div>
              ${actions}
            </div>
            <div class="reply-body">
              <div class="reply-text">${escapeHtml(r.content ?? '')}</div>
            </div>
          </div>
        `;
      })
      .join('');

    // 이벤트 위임(수정/삭제)
    list.addEventListener('click', async (e) => {
      const btn = e.target.closest('button[data-action]');
      if (!btn) return;

      const action = btn.dataset.action;
      const replyId = btn.dataset.id;

      if (action === 'delete') {
        if (!confirm('댓글을 삭제할까요?')) return;
        const del = await fetch(`/api/posts/replies/${replyId}`, {
          method: 'DELETE',
        });
        if (del.ok) {
          await fetchReplies(postId);
        } else {
          alert('삭제 실패');
        }
      }

      if (action === 'edit') {
        const item = e.target.closest('.reply-item');
        const textEl = $('.reply-text', item);
        const before = textEl ? textEl.textContent : '';
        const next = prompt('댓글 수정', before);
        if (next === null) return; // 취소
        if (!next.trim()) {
          alert('내용을 입력해주세요.');
          return;
        }

        const form = new URLSearchParams();
        form.set('content', next);

        const put = await fetch(`/api/posts/replies/${replyId}`, {
          method: 'PUT',
          headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
          body: form.toString(),
        });

        if (put.ok) {
          await fetchReplies(postId);
        } else {
          const msg = await put.text();
          alert(`수정 실패: ${msg}`);
        }
      }
    });
  }

  function bindReplyCreate(postId) {
    const form = $('#replyForm');
    if (!form) return;

    form.addEventListener('submit', async (e) => {
      e.preventDefault();
      const input = $('#replyContent');
      const content = input.value;

      if (!content.trim()) {
        alert('댓글 내용을 입력해주세요.');
        return;
      }

      const body = new URLSearchParams();
      body.set('content', content);

      const res = await fetch(`/api/posts/${postId}/replies`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: body.toString(),
      });

      if (res.ok) {
        input.value = '';
        await fetchReplies(postId);
      } else {
        const msg = await res.text();
        alert(`댓글 등록 실패: ${msg}`);
      }
    });
  }

  function bindImageModal() {
    const modal = document.getElementById('imgModal');
    const target = document.getElementById('imgModalTarget');
    if (!modal || !target) return;

    // 상세 이미지 클릭하면 열기 (이벤트 위임)
    document.addEventListener('click', (e) => {
      const img = e.target.closest('.post-image img');
      if (img) {
        target.src = img.getAttribute('src');
        modal.classList.add('open');
        modal.setAttribute('aria-hidden', 'false');
        return;
      }

      // 닫기 (백드롭/닫기 버튼)
      const close = e.target.closest("[data-close='true']");
      if (close && modal.classList.contains('open')) {
        modal.classList.remove('open');
        modal.setAttribute('aria-hidden', 'true');
        target.src = '';
      }
    });

    // ESC로 닫기
    document.addEventListener('keydown', (e) => {
      if (e.key === 'Escape' && modal.classList.contains('open')) {
        modal.classList.remove('open');
        modal.setAttribute('aria-hidden', 'true');
        target.src = '';
      }
    });
  }

  // =========================
  // Write / Edit
  // =========================
  function bindImagePreview(fileInputId, previewBoxId, previewImgId) {
    const input = document.getElementById(fileInputId);
    const box = document.getElementById(previewBoxId);
    const img = document.getElementById(previewImgId);

    if (!input || !box || !img) return;

    input.addEventListener('change', () => {
      const f = input.files && input.files[0];
      if (!f) {
        box.style.display = 'none';
        img.src = '';
        return;
      }
      const reader = new FileReader();
      reader.onload = (ev) => {
        img.src = ev.target.result;
        box.style.display = 'block';
      };
      reader.readAsDataURL(f);
    });
  }

  async function initPostWrite(root) {
    const clubId = root.dataset.clubId;
    if (!clubId) return;

    bindImagePreview('imageFile', 'imagePreviewBox', 'previewImg');

    const form = $('#writeForm');
    if (!form) return;

    form.addEventListener('submit', async (e) => {
      e.preventDefault();

      const title = $('#title').value;
      const content = $('#content').value;

      const isNoticeEl = $('#isNotice');
      const isNotice = isNoticeEl ? isNoticeEl.checked : false;

      if (!title.trim() || !content.trim()) {
        alert('제목과 내용을 입력해주세요.');
        return;
      }

      const fd = new FormData();
      fd.append('clubId', clubId);
      fd.append('title', title);
      fd.append('content', content);
      fd.append('isNotice', isNotice ? 'true' : 'false');

      const fileInput = $('#imageFile');
      if (fileInput && fileInput.files && fileInput.files[0]) {
        // DTO 필드명(image)과 맞춤
        fd.append('image', fileInput.files[0]);
      }

      const res = await fetch('/api/posts', { method: 'POST', body: fd });
      if (res.ok) {
        alert('등록되었습니다.');
        location.href = `/community/post/list/${clubId}`;
      } else {
        const msg = await res.text();
        alert(`등록 실패: ${msg}`);
      }
    });
  }

  async function initPostEdit(root) {
    const postId = root.dataset.postId;
    if (!postId) return;

    bindImagePreview('imageFile', 'imagePreviewBox', 'previewImg');

    // 기존 데이터 로드
    const res = await fetch(`/api/posts/detail/${postId}`);
    if (!res.ok) {
      alert('게시글 정보를 불러오지 못했습니다.');
      return;
    }
    const post = await res.json();

    $('#title').value = post.title ?? '';
    $('#content').value = post.content ?? '';

    // 기존 이미지가 있으면 미리 보여주기
    const box = $('#imagePreviewBox');
    const img = $('#previewImg');
    if (post.imageUrl && box && img) {
      img.src = `/postImg/${post.imageUrl}`;
      box.style.display = 'block';
    }

    // 공지 체크(권한 있는 사람만 체크박스가 렌더링될 수 있음)
    const isNoticeEl = $('#isNotice');
    if (isNoticeEl) isNoticeEl.checked = !!post.isNotice;

    // 제출
    const form = $('#editForm');
    form.addEventListener('submit', async (e) => {
      e.preventDefault();

      const title = $('#title').value;
      const content = $('#content').value;
      const isNotice = isNoticeEl ? isNoticeEl.checked : false;

      if (!title.trim() || !content.trim()) {
        alert('제목과 내용을 입력해주세요.');
        return;
      }

      const fd = new FormData();
      fd.append('title', title);
      fd.append('content', content);
      fd.append('isNotice', isNotice ? 'true' : 'false');

      const fileInput = $('#imageFile');
      if (fileInput && fileInput.files && fileInput.files[0]) {
        fd.append('image', fileInput.files[0]);
      }

      const put = await fetch(`/api/posts/${postId}`, {
        method: 'PUT',
        body: fd,
      });

      if (put.ok) {
        alert('수정되었습니다.');
        location.href = `/community/post/detail/${postId}`;
      } else {
        const msg = await put.text();
        alert(`수정 실패: ${msg}`);
      }
    });

    // 취소 -> 상세로
    const cancelBtn = $('#cancelBtn');
    if (cancelBtn) {
      cancelBtn.addEventListener('click', () => {
        location.href = `/community/post/detail/${postId}`;
      });
    }
  }

  // =========================
  // Boot
  // =========================
  document.addEventListener('DOMContentLoaded', () => {
    const root =
      document.querySelector("[data-page='post-list']") ||
      document.querySelector("[data-page='post-detail']") ||
      document.querySelector("[data-page='post-write']") ||
      document.querySelector("[data-page='post-edit']");
    if (!root) return;

    const page = root.dataset.page;
    if (page === 'post-list') initPostList(root);
    if (page === 'post-detail') initPostDetail(root);
    if (page === 'post-write') initPostWrite(root);
    if (page === 'post-edit') initPostEdit(root);
  });
})();
