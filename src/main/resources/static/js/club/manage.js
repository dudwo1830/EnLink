/**
 * manage.js
 * 모임 관리 통합 스크립트 (가입신청, 멤버관리, 정보수정, 삭제/복구)
 */
// --- [공통] 서버 응답 처리 핸들러 ---
async function handleResponse(response, successMsg, callback) {
    const resText = await response.text();
    if (response.ok) {
        Swal.fire({
            title: '성공',
            text: successMsg || resText,
            icon: 'success',
            confirmButtonColor: '#3085d6'
        }).then(() => {
            // 💡 [핵심 추가] 만약 함수를 호출할 때 callback을 넘겨줬다면 그걸 우선 실행합니다.
            if (typeof callback === 'function') {
                callback();
                return; // 콜백 실행 후 종료
            }

            // 기존 로직 유지
            if (successMsg && successMsg.includes("수정")) {
                const clubId = window.location.pathname.split('/')[2];
                location.href = `/club/${clubId}`;
            } else {
                location.reload();
            }
        });
    } else {
        let errorMsg = resText;
        try {
            const errorJson = JSON.parse(resText);
            errorMsg = errorJson.defaultMessage || errorJson.message || resText;
        } catch (e) {
            console.error("JSON 파싱 에러:", e);
        }
        Swal.fire('오류 발생', errorMsg, 'error');
    }
}


// 1. 가입 신청 현황 (requests.html)

// 가입 승인
async function approveMember(clubId, memberId) {
    const result = await Swal.fire({
        title: '가입 승인',
        text: `${memberId} 님의 가입을 승인하시겠습니까?`,
        icon: 'question',
        showCancelButton: true,
        confirmButtonText: '승인',
        cancelButtonText: '취소'
    });

    if (result.isConfirmed) {
        const response = await fetch(`/api/club/${clubId}/manage/approve?memberId=${memberId}`, { method: 'POST' });
        handleResponse(response, "가입 승인이 완료되었습니다.");
    }
}

// 가입 거절
async function rejectMember(clubId, memberId) {
    const result = await Swal.fire({
        title: '가입 거절',
        text: `${memberId} 님의 가입 신청을 거절하시겠습니까?`,
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#d33',
        confirmButtonText: '거절',
        cancelButtonText: '취소'
    });

    if (result.isConfirmed) {
        const response = await fetch(`/api/club/${clubId}/manage/reject?memberId=${memberId}`, { method: 'POST' });
        handleResponse(response, "가입 거절 처리가 완료되었습니다.");
    }
}

// 답변 보기 모달
function openManageModal(memberId, memberName, answerText) {
    document.getElementById('modalTitle').innerText = `${memberName}(${memberId}) 님의 답변`;
    document.getElementById('modalAnswer').innerText = answerText && answerText !== 'null' ? answerText : "입력된 답변이 없습니다.";
    document.getElementById('manageModal').style.display = 'block';
}

function closeManageModal() {
    document.getElementById('manageModal').style.display = 'none';
}

// 상세 이력 모달
window.openHistoryModal = async function(buttonElement) {

    const memberId = buttonElement.getAttribute('data-member-id');
    const memberName = buttonElement.getAttribute('data-member-name');
    const clubId = buttonElement.getAttribute('data-club-id');

    document.getElementById('historyTitle').innerText = `${memberName} 님의 전체 이력`;
    const historyList = document.getElementById('historyList');
    historyList.innerHTML = '<tr><td colspan="3" style="text-align:center;">불러오는 중...</td></tr>';
    document.getElementById('historyModal').style.display = 'block';

    try {
        const response = await fetch(`/api/club/${clubId}/member/${memberId}/history`);
        if (!response.ok) throw new Error("데이터를 가져오지 못했습니다.");

        const data = await response.json();

        if (!data || data.length === 0) {
            historyList.innerHTML = '<tr><td colspan="3" style="text-align:center;">이력이 없습니다.</td></tr>';
        } else {
            historyList.innerHTML = data.map(h => {
                // 1. 날짜 가공 (T 제거 및 포맷팅)
                const formattedDate = h.createdAt.replace('T', ' ').substring(0, 16);

                // 2. 유형 한글화 (보기 좋게)
                const typeMap = {
                    'BANNED': '제명',
                    'EXIT': '탈퇴',
                    'ROLE_CHANGE': '권한변경',
                    'JOIN_APPROVE': '가입승인',
                    'JOIN_REQUEST': '가입신청'
                };
                const typeKorean = typeMap[h.actionType] || h.actionType;
                const badgeClass = h.actionType === 'BANNED' ? 'danger' : 'gray';

                return `
                    <tr>
                        <td>${formattedDate}</td>
                        <td><span class="badge-${badgeClass}">${typeKorean}</span></td>
                        <td style="text-align: left;">${h.description || '-'}</td>
                    </tr>
                `;
            }).join('');
        }
    } catch (error) {
        historyList.innerHTML = `<tr><td colspan="3" style="text-align:center; color:red;">${error.message}</td></tr>`;
    }
}

function closeHistoryModal() {
    document.getElementById('historyModal').style.display = 'none';
}


/**
 * 멤버 권한 변경 (OWNER 전용)
 */
window.updateRole = async function(selectElement) {
    const clubId = selectElement.getAttribute('data-club-id');
    const memberId = selectElement.getAttribute('data-member-id');
    const newRole = selectElement.value;
    const oldRole = selectElement.getAttribute('data-old-role') || selectElement.defaultValue;

    // 🚀 모임장 위임 여부에 따른 문구 차별화
    const isOwnerDelegation = (newRole === 'OWNER');
    const alertTitle = isOwnerDelegation ? '모임장 위임' : '권한 변경 확인';
    const alertText = isOwnerDelegation
        ? `[${memberId}]님에게 모임장 권한을 위임하시겠습니까?\n`
        : `[${memberId}]님의 권한을 [${newRole}](으)로 변경하시겠습니까?`;

    const result = await Swal.fire({
        title: alertTitle,
        text: alertText,
        icon: isOwnerDelegation ? 'warning' : 'question',
        showCancelButton: true,
        confirmButtonColor: isOwnerDelegation ? '#d33' : '#3085d6',
        cancelButtonColor: '#aaa',
        confirmButtonText: '확인',
        cancelButtonText: '취소'
    });

    if (result.isConfirmed) {
        const params = new URLSearchParams();
        params.append('memberId', memberId);
        params.append('newRole', newRole);

        try {
            const response = await fetch(`/api/club/${clubId}/manage/members/update-role`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: params
            });

            if (isOwnerDelegation) {
                // 💡 모임장을 위임했다면 성공 알림 후 상세 페이지로 쫓아냅니다.
                handleResponse(response, "모임장 권한 위임이 완료되었습니다.", () => {
                    location.href = `/club/${clubId}`;
                });
            } else {
                selectElement.setAttribute('data-old-role', newRole);
                handleResponse(response, "권한 변경이 완료되었습니다.");
            }
        } catch (error) {
            selectElement.value = oldRole;
            Swal.fire('오류', '통신 중 에러가 발생했습니다.', 'error');
        }
    } else {
        selectElement.value = oldRole;
    }
}

/** * 제명 모달 열기
 */
window.kickContext = window.kickContext || { clubId: null, memberId: null };

window.openKickModal = function(buttonElement) {
    const memberId = buttonElement.getAttribute('data-member-id');
    const memberName = buttonElement.getAttribute('data-member-name');
    const clubId = buttonElement.getAttribute('data-club-id');

    kickContext = { clubId, memberId };

    document.getElementById('kickTargetName').innerText = memberName;
    document.getElementById('kickModal').style.display = 'block';
}

/**
 * 제명 모달 닫기
 */
window.closeKickModal = function() {
    document.getElementById('kickModal').style.display = 'none';
    document.getElementById('kickEtcInput').value = '';
    document.getElementById('kickReasonSelect').value = '부적절한 게시물 게시';
    document.getElementById('kickEtcWrapper').style.display = 'none';
}

/**
 * 기타 사유 입력창 토글
 */
window.toggleKickEtcInput = function() {
    const select = document.getElementById('kickReasonSelect');
    const wrapper = document.getElementById('kickEtcWrapper');
    wrapper.style.display = (select.value === '기타') ? 'block' : 'none';
}


// 제명 최종 승인

window.submitKick = async function() {
    const select = document.getElementById('kickReasonSelect');
    let description = select.value;
    if (description === '기타') {
        description = document.getElementById('kickEtcInput').value.trim();
    }

    if (!description) {
        Swal.fire('알림', '제명 사유를 입력해주세요.', 'info');
        return;
    }

    const params = new URLSearchParams();
    params.append('memberId', kickContext.memberId);
    params.append('description', description);

    try {
        const response = await fetch(`/api/club/${kickContext.clubId}/manage/members/kick`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: params
        });

        closeKickModal();
        handleResponse(response, "멤버가 제명되었습니다.");
    } catch (error) {
        console.error('Error:', error);
        Swal.fire('오류', '제명 처리 중 서버 통신 오류가 발생했습니다.', 'error');
    }
}


/**
 * 모임 정보 수정 (최종 제출)
 */
async function updateClubInfo(clubId) {
    if (!isNameChecked) {
        Swal.fire('알림', '모임 이름을 확인해주세요.', 'warning');
        return;
    }

    const form = document.getElementById('editClubForm');
    const formData = new FormData(form);

    const topicIdVal = form.querySelector('input[name="topicId"]').value;
    const cityIdVal = form.querySelector('input[name="cityId"]').value;

    if (!topicIdVal || !cityIdVal) {
        Swal.fire('알림', '관심사와 지역을 모두 선택해주세요.', 'info');
        return;
    }

    const confirmResult = await Swal.fire({
        title: '정보를 수정하시겠습니까?',
        icon: 'question', showCancelButton: true, confirmButtonText: '수정', cancelButtonText: '취소'
    });

    if (!confirmResult.isConfirmed) return;

    try {
        const response = await fetch(`/api/club/${clubId}/manage/edit`, {
            method: 'POST',
            body: formData
        });
        handleResponse(response, "수정 완료");
    } catch (e) {
        Swal.fire('오류', '서버 통신 중 문제가 발생했습니다.', 'error');
    }
}

document.addEventListener('DOMContentLoaded', () => {
    // 전역 변수
    window.isNameChecked = false;

    const clubNameInput = document.getElementById('clubName');
    const feedback = document.getElementById('nameFeedback');
    const clubIdInput = document.getElementsByName('clubId')[0];

    if (!clubNameInput || !clubIdInput) return;

    clubNameInput.addEventListener('blur', async function() {
        const name = this.value.trim();
        const clubId = clubIdInput.value;

        // 기존 이름과 같으면 체크하지 않음
        if (typeof currentName !== 'undefined' && name === currentName) {
            feedback.innerText = "";
            window.isNameChecked = true;
            return;
        }

        try {
            const response = await fetch(`/api/club/${clubId}/manage/check-name-edit?name=${encodeURIComponent(name)}&clubId=${clubId}`);
            const result = await response.json(); // { available: true/false, message: "..." }

            feedback.innerText = result.message;
            if (result.available) {
                feedback.className = "mt-2 small text-success";
                window.isNameChecked = true;
            } else {
                feedback.className = "mt-2 small text-danger";
                window.isNameChecked = false;
            }
        } catch (e) {
            console.error("중복/유효성 체크 실패:", e);
            feedback.innerText = "서버와 통신 중 문제가 발생했습니다.";
            feedback.className = "mt-2 small text-danger";
            window.isNameChecked = false;
        }
    });
});

//이미지 변경 (기본이미지 적용)
document.addEventListener('DOMContentLoaded', function() {
    const fileInput = document.getElementById('fileInput');
    const defaultCheck = document.getElementById('defaultImage');

    if (fileInput && defaultCheck) {
        // 파일을 선택하면 '기본 이미지로 변경' 체크를 해제
        fileInput.addEventListener('change', function() {
            if (this.value) defaultCheck.checked = false;
        });

        // '기본 이미지로 변경'을 체크하면 파일 선택을 초기화
        defaultCheck.addEventListener('change', function() {
            if (this.checked) fileInput.value = '';
        });
    }
});


// 4. 모임 삭제/복구 (clubDelete.html)
async function deleteClub(clubId) {
    const { isConfirmed } = await Swal.fire({
        title: '모임 삭제 요청',
        text: "삭제 요청 후 7일 이내에만 복구가 가능합니다. 진행하시겠습니까?",
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#d33',
        confirmButtonText: '삭제 요청',
        cancelButtonText: '취소'
    });

    if (isConfirmed) {
        const response = await fetch(`/api/club/${clubId}/manage/delete`, { method: 'POST' });
        handleResponse(response, "삭제 요청이 수락되었습니다.");
    }
}

async function restoreClub(clubId) {
    const response = await fetch(`/api/club/${clubId}/manage/restore`, { method: 'POST' });
    handleResponse(response, "모임이 정상적으로 복구되었습니다.", () => {
            location.reload();
        });
}

function hardDeleteClub(clubId) {
    Swal.fire({
        title: '정말 즉시 삭제하시겠습니까?',
        text: "이미지와 모든 데이터가 즉시 삭제되며, 복구할 수 없습니다!",
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#d33',
        cancelButtonColor: '#3085d6',
        confirmButtonText: '네, 영구 삭제합니다',
        cancelButtonText: '취소'
    }).then((result) => {
        if (result.isConfirmed) {
            // ✅ 수정된 부분: URL 구조를 컨트롤러 매핑과 일치시킵니다.
            fetch(`/api/club/${clubId}/manage/hard-delete`, { method: 'DELETE' })
                .then(res => {
                    if(res.ok) {
                        Swal.fire('삭제 완료', '모임이 완전히 사라졌습니다.', 'success')
                            .then(() => {
                                // 삭제 후에는 목록 페이지로 이동
                                location.href = '/club/list';
                            });
                    } else {
                        // 권한이 없거나 서버 에러일 경우
                        res.text().then(msg => Swal.fire('삭제 실패', msg, 'error'));
                    }
                })
                .catch(err => {
                    console.error('Fetch error:', err);
                    Swal.fire('에러', '네트워크 통신 중 오류가 발생했습니다.', 'error');
                });
        }
    });
}