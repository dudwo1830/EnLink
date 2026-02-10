document.addEventListener('DOMContentLoaded', function() {
    const createForm = document.getElementById('createClubForm');

    if (createForm) {
        createForm.addEventListener('submit', function(e) {
            e.preventDefault();

            const submitBtn = this.querySelector('.btn-submit');
            submitBtn.disabled = true;

            const formData = new FormData(this);

            fetch('/api/club/create', {
                method: 'POST',
                body: formData
            })
            .then(async response => {
                // 💡 [수정] 응답 결과(ID 혹은 에러메시지)를 딱 한 번만 변수에 담습니다.
                const resultData = await response.text();

                if (response.ok) {
                    // ✅ 이미 위에서 읽은 resultData가 바로 newClubId입니다.
                    const newClubId = resultData;

                    Swal.fire({
                        title: '개설 완료!',
                        text: '새로운 모임이 성공적으로 만들어졌습니다.',
                        icon: 'success',
                        confirmButtonText: '확인',
                        buttonsStyling: false,
                        customClass: { confirmButton: 'btn-primary-custom' }
                    }).then((result) => {
                        if (result.isConfirmed) {
                            location.href = `/club/detail/${newClubId}`; // 💡 경로 확인 필요
                        }
                    });
                    return;
                }

                // ❌ 실패 케이스
                let errorMsg = resultData;
                try {
                    const errorJson = JSON.parse(resultData);
                    errorMsg = errorJson.defaultMessage || errorJson.message || resultData;
                } catch(e) { }

                Swal.fire({
                    title: '개설 실패',
                    text: errorMsg,
                    icon: 'warning',
                    confirmButtonText: '확인',
                    buttonsStyling: false,
                    customClass: { confirmButton: 'btn-primary-custom' }
                });
                submitBtn.disabled = false;
            })
            .catch(error => {
                console.error('Fetch Error:', error);
                Swal.fire('오류', '서버 통신 중 오류가 발생했습니다.', 'error');
                submitBtn.disabled = false;
            });
        });
    }
});