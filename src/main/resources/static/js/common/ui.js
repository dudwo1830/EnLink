const Ui = {
  alert(message, options = {}) {
    if (typeof Swal !== "undefined" && Swal.fire) {
      return Swal.fire({
        title: message.title,
        text: message.text,
        icon: options.icon || "info",
        confirmButtonText: options.confirmText || "확인"
      });
    }

    window.alert(message);
    return Promise.resolve();
  },

  confirm(message, options = {}) {
    if (typeof Swal !== "undefined" && Swal.fire) {
      return Swal.fire({
        title: options.title,
        text: message,
        icon: options.icon || "question",
        showCancelButton: true,
        confirmButtonText: options.confirmText || "확인",
        cancelButtonText: options.cancelText || "취소"
      }).then(result => result.isConfirmed);
    }

    return Promise.resolve(window.confirm(message));
  }
};