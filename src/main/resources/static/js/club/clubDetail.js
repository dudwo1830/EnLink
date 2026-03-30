/** 1. 모달 제어 관련 함수들 */
function openApplyModal() {
    document.getElementById('applyModal').style.display = 'block';
}
function closeApplyModal() {
    document.getElementById('applyModal').style.display = 'none';
}
function openLeaveModal() {
    document.getElementById('leaveModal').style.display = 'block';
}
function closeLeaveModal() {
    document.getElementById('leaveModal').style.display = 'none';
}

/** 2. 기타 사유 입력창 토글 */
function toggleLeaveEtcInput() {
    const select = document.getElementById('leaveReasonSelect');
    const etcWrapper = document.getElementById('leaveEtcWrapper');
    if (etcWrapper) {
        etcWrapper.style.display = (select.value === '기타') ? 'block' : 'none';
    }
}

/** 3. 가입 신청 함수 */
async function submitApply(clubId) {
    const answer = document.getElementById('applyAnswer').value;
    if (!answer) {
        // 🚀 didOpen을 추가해서 알림창을 최상단 레이어로 올립니다.
        Swal.fire({
            title: '알림',
            text: '가입 질문에 답해주세요.',
            icon: 'info',
            didOpen: () => {
                Swal.getContainer().style.zIndex = "99999";
            }
        });
        return;
    }

    const response = await fetch(`/api/club/${clubId}/member/apply`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ answer: answer })
    });

    closeApplyModal();
    handleResponse(response, "신청 완료");
}

/** 4. 가입 취소 함수 */
async function cancelApply(clubId) {
    // confirm 대신 세련된 Swal로 교체
    const result = await Swal.fire({
        title: '신청 취소',
        text: "가입 신청을 취소하시겠습니까?",
        icon: 'question',
        showCancelButton: true,
        confirmButtonColor: '#6c63ff',
        cancelButtonColor: '#aaa',
        confirmButtonText: '네, 취소합니다',
        cancelButtonText: '아니오'
    });

    if (!result.isConfirmed) return;

    try {
        // 🚨 중요: 실제 컨트롤러의 API 주소가 /api/club/${clubId}/member/cancel 가 맞는지 꼭 확인!
        const response = await fetch(`/api/club/${clubId}/member/cancel`, {
            method: 'POST'
        });

        // 이미 만들어두신 공통 응답 처리기(handleResponse)를 활용합니다.
        handleResponse(response, "취소 완료");
    } catch (error) {
        console.error("Error:", error);
        Swal.fire('오류', '서버 통신 중 오류가 발생했습니다.', 'error');
    }
}

/** 5. 탈퇴 처리 함수 (수정됨) */
async function submitLeave(clubId) {
    const select = document.getElementById('leaveReasonSelect');
    let description = select.value;

    if (description === '기타') {
        description = document.getElementById('leaveEtcInput').value;
    }

    if (!description) {
        Swal.fire('알림', '탈퇴 사유를 입력해주세요.', 'info');
        return;
    }

    const response = await fetch(`/api/club/${clubId}/member/leave`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ description: description })
    });

    closeLeaveModal();

    handleResponse(response, "탈퇴 완료");
}

/** 6. 공통 응답 처리기 */
async function handleResponse(response, successTitle) {
    const resText = await response.text();

    if (response.ok) {
        Swal.fire({
            title: successTitle,
            text: resText,
            icon: 'success',
            didOpen: () => {
                Swal.getContainer().style.zIndex = "99999";
            }
        }).then(() => location.reload());
    } else {
        let errorMsg = resText;
        try {
            const errorJson = JSON.parse(resText);
            errorMsg = errorJson.defaultMessage || errorJson.message || resText;
        } catch(e) {}

        Swal.fire({
            title: '오류 발생',
            text: errorMsg,
            icon: 'warning',
            didOpen: () => {
                Swal.getContainer().style.zIndex = "99999";
            }
        });
    }
}