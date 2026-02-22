(function () {
  "use strict";

  const $ = (sel, parent = document) => parent.querySelector(sel);

  function fmtDate(dateLike) {
    if (!dateLike) return "";
    const safe = String(dateLike).replace(" ", "T");
    const d = new Date(safe);
    if (isNaN(d.getTime())) return String(dateLike);

    const yyyy = d.getFullYear();
    const mm = String(d.getMonth() + 1).padStart(2, "0");
    const dd = String(d.getDate()).padStart(2, "0");
    return `${yyyy}-${mm}-${dd}`;
  }

  // state
  let clubId = null;
  let currentPage = 0;
  let isLoading = false;
  let lastPage = false;

  let detailModal = null;
  let uploadModal = null;
  let currentPhotoId = null;

  async function fetchJson(url, options = {}) {
    const res = await fetch(url, options);
    if (!res.ok) {
      const text = await res.text().catch(() => "");
      throw new Error(text || `HTTP ${res.status}`);
    }
    return res.json();
  }

  function renderCards(photos) {
    const grid = $("#galleryGrid");
    const empty = $("#emptyState");
    const moreBtn = $("#moreBtn");
    if (!grid || !empty || !moreBtn) return;

    if (currentPage === 0) grid.innerHTML = "";

    if (!photos.length && currentPage === 0) {
      empty.style.display = "block";
      moreBtn.style.display = "none";
      return;
    }
    empty.style.display = "none";

    const html = photos
      .map((p) => {
        const created = fmtDate(p.createdAt);
        const memberId = p.memberId ?? "";

        return `
          <article class="photo-card">
            <div class="photo-media">
              <img
                class="photo-thumb"
                src="/galleryImg/${p.imageUrl}"
                alt="gallery photo"
                data-photo-id="${p.photoId}"
                data-image-url="${p.imageUrl}"
                data-member-id="${memberId}"
                data-created-at="${p.createdAt ?? ""}"
              />
            </div>

            <div class="photo-meta">
              <span class="chip">${memberId}</span>
              <span class="chip muted">${created}</span>
            </div>
          </article>
        `;
      })
      .join("");

    grid.insertAdjacentHTML("beforeend", html);
  }

  async function loadMore() {
    if (isLoading || lastPage) return;
    isLoading = true;

    const moreBtn = $("#moreBtn");
    if (moreBtn) moreBtn.disabled = true;

    try {
      const page = await fetchJson(`/api/gallery/${clubId}?page=${currentPage}`);

      renderCards(page.content || []);

      lastPage = !!page.last;
      if (lastPage) {
        if (moreBtn) moreBtn.style.display = "none";
      } else {
        currentPage += 1;
        if (moreBtn) moreBtn.style.display = "inline-flex";
      }
    } catch (e) {
      alert(`사진을 불러오는 중 오류가 발생했습니다.\n${e.message}`);
    } finally {
      isLoading = false;
      if (moreBtn) moreBtn.disabled = false;
    }
  }

  function openDetailFromImg(imgEl) {
    const photoId = imgEl.dataset.photoId;
    const imageUrl = imgEl.dataset.imageUrl;
    const memberId = imgEl.dataset.memberId || "";
    const createdAt = imgEl.dataset.createdAt || "";

    currentPhotoId = photoId;

    $("#modalImg").src = `/galleryImg/${imageUrl}`;
    $("#modalInfo").innerHTML =
      `<strong>등록자:</strong> ${memberId}<br>` +
      `<strong>등록일:</strong> ${fmtDate(createdAt)}`;

    // 권한체크는 서버에서. 버튼은 노출해두고 실패 시 메시지
    const deleteBtn = $("#deleteBtn");
    if (deleteBtn) deleteBtn.style.display = "inline-flex";

    detailModal.show();
  }

  async function deleteCurrentPhoto() {
    if (!currentPhotoId) return;
    if (!confirm("정말 이 사진을 삭제하시겠습니까?")) return;

    try {
      const res = await fetch(`/api/gallery/${currentPhotoId}`, { method: "DELETE" });
      if (!res.ok) {
        const msg = await res.text();
        throw new Error(msg || "삭제 실패");
      }

      alert("사진이 삭제되었습니다.");
      detailModal.hide();

      // refresh
      currentPage = 0;
      lastPage = false;
      await loadMore();
    } catch (e) {
      alert(`삭제 실패: ${e.message}`);
    }
  }

  function bindUploadPreview() {
    const input = $("#imageFile");
    const box = $("#uploadPreview");
    const img = $("#previewImg");
    const filePickBtn = $("#filePickBtn");
    const fileName = $("#fileName");

    if (!input) return;

    // 버튼 클릭 -> 파일 선택창 열기
    if (filePickBtn) {
      filePickBtn.addEventListener("click", () => input.click());
    }

    input.addEventListener("change", () => {
      const f = input.files && input.files[0];

      // 파일명 표시
      if (fileName) fileName.textContent = f ? f.name : "선택된 파일 없음";

      // 미리보기
      if (!box || !img) return;
      if (!f) {
        box.style.display = "none";
        img.src = "";
        return;
      }
      const reader = new FileReader();
      reader.onload = (ev) => {
        img.src = ev.target.result;
        box.style.display = "block";
      };
      reader.readAsDataURL(f);
    });
  }

  async function uploadPhoto() {
    const input = $("#imageFile");
    const btn = $("#uploadBtn");
    const fileName = $("#fileName");

    if (!input || !input.files || !input.files[0]) {
      alert("파일을 선택해주세요.");
      return;
    }

    btn.disabled = true;

    try {
      const fd = new FormData();
      fd.append("image", input.files[0]);
      fd.append("clubId", clubId);

      const res = await fetch("/api/gallery/upload", { method: "POST", body: fd });
      if (!res.ok) {
        const msg = await res.text();
        throw new Error(msg || "업로드 실패");
      }

      alert("성공적으로 업로드되었습니다!");

      // reset + close
      uploadModal.hide();
      input.value = "";
      if (fileName) fileName.textContent = "선택된 파일 없음";
      if ($("#uploadPreview")) $("#uploadPreview").style.display = "none";
      if ($("#previewImg")) $("#previewImg").src = "";

      currentPage = 0;
      lastPage = false;
      await loadMore();
    } catch (e) {
      alert(`업로드 실패: ${e.message}`);
    } finally {
      btn.disabled = false;
    }
  }

  function boot() {
    const root = document.querySelector("[data-page='gallery-list']");
    if (!root) return;

    clubId = root.dataset.clubId;
    if (!clubId) {
      alert("clubId를 찾을 수 없습니다.");
      return;
    }

    detailModal = new bootstrap.Modal(document.getElementById("detailModal"));
    uploadModal = new bootstrap.Modal(document.getElementById("uploadModal"));

    // more
    const moreBtn = $("#moreBtn");
    if (moreBtn) moreBtn.addEventListener("click", loadMore);

    // click card (delegation)
    const grid = $("#galleryGrid");
    if (grid) {
      grid.addEventListener("click", (e) => {
        const img = e.target.closest(".photo-thumb");
        if (!img) return;
        openDetailFromImg(img);
      });
    }

    // delete
    const deleteBtn = $("#deleteBtn");
    if (deleteBtn) deleteBtn.addEventListener("click", deleteCurrentPhoto);

    // upload
    bindUploadPreview();
    const uploadBtn = $("#uploadBtn");
    if (uploadBtn) uploadBtn.addEventListener("click", uploadPhoto);

    // first
    loadMore();
  }

  document.addEventListener("DOMContentLoaded", boot);
})();