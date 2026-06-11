export function notifyReaderNotificationsChanged(detail) {
    window.dispatchEvent(new CustomEvent("reader-notifications-changed", { detail }));
}
