const { contextBridge, ipcRenderer } = require("electron");

contextBridge.exposeInMainWorld("electronAPI", {
    isDesktop: true,
    platform: process.platform,
    getDatabaseConfig: () => ipcRenderer.invoke("database:get-config"),
    saveDatabaseConfig: (config) => ipcRenderer.invoke("database:save-config", config),
    startBackend: (config) => ipcRenderer.invoke("backend:start", config),
    checkBackendHealth: () => ipcRenderer.invoke("backend:health")
});
