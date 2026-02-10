document.addEventListener('DOMContentLoaded', function() {
    const submitBtn = document.getElementById('submitBtn');
    const createForm = document.getElementById('createClubForm');

    if (submitBtn) {
        submitBtn.addEventListener('click', function() {
            // 1. 중복 클릭 방지 (즉시 잠금)
            if (this.disabled) return;
            this.disabled = true;

            // 2. 유효성 검사
            if (!createForm.checkValidity()) {
                createForm.reportValidity();
                this.disabled = false;
                return;
            }

            const formData = new FormData(createForm);

            // 3. AJAX 요청
            fetch('/api/club/create', {
                method: 'POST',
                body: formData
            })
            .then(async response => {
                const resultData = await response.json();
                if (response.ok) {
                    Swal.fire({
                        title: '개설 완료!',
                        text: resultData.message,
                        icon: 'success',
                        confirmButtonText: '확인'
                    }).then(() => {
                        location.href = "/club/list";
                    });
                } else {
                    Swal.fire('개설 실패', resultData.message || '알 수 없는 오류', 'warning');
                    this.disabled = false;
                }
            })
            .catch(error => {
                console.error('Error:', error);
                Swal.fire('오류 발생', '서버와의 통신 중 문제가 발생했습니다.', 'error');
                this.disabled = false;
            });
        });
    }
});