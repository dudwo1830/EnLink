// 가입신청 관련 js
function openApplyModal() {
        document.getElementById('applyModal').style.display = 'block';
    }

    function closeApplyModal() {
        document.getElementById('applyModal').style.display = 'none';
    }

    window.onclick = function(event) {
        const modal = document.getElementById('applyModal');
        if (event.target == modal) {
            modal.style.display = "none";
        }
}

