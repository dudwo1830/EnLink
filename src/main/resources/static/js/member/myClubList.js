/** 4. 가입 취소 함수 (팀장님 참고용 수정본) */
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