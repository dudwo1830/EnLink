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
        Swal.fire('알림', '가입 질문에 답해주세요.', 'info');
        return;
    }

    const response = await fetch(`/api/club/${clubId}/member/apply`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ answer: answer })
    });

    // 💡 핵심: 서버 응답을 처리하기 전에(혹은 후에) 기존 모달을 닫아줍니다.
    closeApplyModal();

    handleResponse(response, "신청 완료");
}

/** 4. 가입 취소 함수 */
async function cancelApply(clubId) {
    // 💡 confirm 대신 Swal.fire를 쓰면 더 팀장님 스타일이죠!
    if(!confirm("가입 신청을 취소하시겠습니까?")) return;

    try {
        const response = await fetch(`/api/club/${clubId}/member/cancel`, {
            method: 'POST'
        });

        if (response.ok) {
            // ✅ 핵심: 성공 알림 후 location.reload()를 호출하면
            // 현재 주소(예: /mypage/clubs?type=pending)를 다시 읽습니다.
            alert("가입 신청이 취소되었습니다.");
            location.reload();
        } else {
            const errorMsg = await response.text();
            alert("취소 실패: " + errorMsg);
        }
    } catch (error) {
        console.error("Error:", error);
        alert("서버 통신 중 오류가 발생했습니다.");
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

/** 6. 공통 응답 처리기 (이게 빠져있거나 괄호가 꼬이면 에러납니다!) */
async function handleResponse(response, successTitle) {
    const resText = await response.text();

    if (response.ok) {
        Swal.fire({
            title: successTitle,
            text: resText,
            icon: 'success'
        }).then(() => location.reload());
    } else {
        let errorMsg = resText;
        try {
            const errorJson = JSON.parse(resText);
            errorMsg = errorJson.defaultMessage || errorJson.message || resText;
        } catch(e) {}
        Swal.fire('오류 발생', errorMsg, 'warning');
    }
}