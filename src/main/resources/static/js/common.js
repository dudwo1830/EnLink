document.addEventListener('DOMContentLoaded', () => {
    console.log('Common JS Loaded');

    const messageInput = document.getElementById('serverMessage');
    const serverMessage = messageInput?.value;

    if (serverMessage && serverMessage.trim() !== "") {
        alert(serverMessage);

        messageInput.value = "";
    }
});
