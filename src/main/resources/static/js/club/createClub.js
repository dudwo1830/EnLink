document.addEventListener('DOMContentLoaded', function() {

    const submitBtn = document.getElementById('submitBtn');
    const createForm = document.getElementById('createClubForm');
    const clubNameInput = document.getElementById("clubName");
    const feedback = document.getElementById("nameFeedback");

    let nameValid = false;
    let debounceTimer;

    // ✅ 모임명 중복 + 발리데이션 통합 체크
    clubNameInput.addEventListener("keyup", function () {
        const name = this.value.trim();

        clearTimeout(debounceTimer);

        debounceTimer = setTimeout(async () => {
            if (name.length < 2) {
                feedback.innerText = "2자 이상 입력하세요";
                feedback.className = "mt-2 small text-muted";
                nameValid = false;
                return;
            }

            try {
                const res = await fetch(`/api/club/check-name?name=${encodeURIComponent(name)}`);
                const data = await res.json();
                // data: { available: true/false, message: "..." }

                feedback.innerText = data.message;
                if (data.available) {
                    feedback.className = "mt-2 small text-success";
                    nameValid = true;
                } else {
                    feedback.className = "mt-2 small text-danger";
                    nameValid = false;
                }
            } catch (e) {
                console.error("중복/유효성 체크 실패:", e);
                feedback.innerText = "서버와 통신 중 문제가 발생했습니다";
                feedback.className = "mt-2 small text-danger";
                nameValid = false;
            }
        }, 400);
    });

    if (submitBtn) {
        submitBtn.addEventListener('click', async function() {
            if (!nameValid) {
                Swal.fire('확인 필요', '모임 이름을 확인해주세요.', 'warning');
                return;
            }

            if (this.disabled) return;
            this.disabled = true;

            const topicId = createForm.querySelector('input[name="topicId"]').value;
            const cityId = createForm.querySelector('input[name="cityId"]').value;

            if (!topicId) {
                Swal.fire('요청 오류', '관심사를 선택해 주세요', 'warning');
                this.disabled = false;
                return;
            }

            if (!cityId) {
                Swal.fire('요청 오류', '지역을 선택해 주세요', 'warning');
                this.disabled = false;
                return;
            }

            if (!createForm.checkValidity()) {
                createForm.reportValidity();
                this.disabled = false;
                return;
            }

            const formData = new FormData(createForm);

            try {
                const response = await fetch('/api/club/create', {
                    method: 'POST',
                    body: formData
                });
                const resultData = await response.json();

                if (response.ok) {
                    Swal.fire('개설 완료!', resultData.message, 'success')
                        .then(() => location.href = "/club/list");
                } else {
                    // 서버에서 전달한 발리데이션 메시지 활용
                    let msg = resultData.message || "모임 생성에 실패했습니다";
                    Swal.fire('개설 실패', msg, 'warning');
                    this.disabled = false;
                }
            } catch {
                Swal.fire('오류 발생', '서버 통신 오류', 'error');
                this.disabled = false;
            }
        });
    }

    // region/city select 처리
    const regionBox = createForm.querySelector(".select-search.regions");
    const cityBox = createForm.querySelector(".select-search.cities");

    if (regionBox && cityBox) {
        const regionHidden = regionBox.querySelector(".select-hidden");
        const cityInput = cityBox.querySelector(".select-input");
        const cityHidden = cityBox.querySelector(".select-hidden");
        const cityGuide = document.getElementById("cityGuide");

        cityInput.disabled = true;
        cityBox.classList.add("opacity-50");

        const observer = new MutationObserver(() => {
            if (regionHidden.value) {
                cityInput.disabled = false;
                cityBox.classList.remove("opacity-50");
                cityGuide.style.display = "none";
            } else {
                cityInput.disabled = true;
                cityHidden.value = "";
                cityInput.value = "";
                cityBox.classList.add("opacity-50");
                cityGuide.style.display = "block";
            }
        });

        observer.observe(regionHidden, { attributes: true, attributeFilter: ['value'] });
    }

});