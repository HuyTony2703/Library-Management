const { app, BrowserWindow, Menu, shell } = require("electron");
const path = require("path");

let mainWindow;

app.setName("LibraDesk");

// Xóa menu mặc định: File, Edit, View, Window...
Menu.setApplicationMenu(null);

function createWindow() {
    mainWindow = new BrowserWindow({
        width: 1380,
        height: 860,
        minWidth: 1100,
        minHeight: 720,
        title: "LibraDesk",
        backgroundColor: "#eef2ff",
        show: false,
        autoHideMenuBar: true,
        webPreferences: {
            nodeIntegration: false,
            contextIsolation: true
        }
    });

    // Chặn trang web đổi title cửa sổ về "frontend"
    mainWindow.on("page-title-updated", (event) => {
        event.preventDefault();
        mainWindow.setTitle("LibraDesk");
    });

    const devServerUrl = process.env.ELECTRON_START_URL;

    if (devServerUrl) {
        mainWindow.loadURL(devServerUrl);
        // Tắt DevTools khi demo cho sạch
        // mainWindow.webContents.openDevTools();
    } else {
        mainWindow.loadFile(path.join(__dirname, "../dist/index.html"));
    }

    mainWindow.once("ready-to-show", () => {
        mainWindow.show();
    });

    mainWindow.webContents.setWindowOpenHandler(({ url }) => {
        shell.openExternal(url);
        return { action: "deny" };
    });
}

app.whenReady().then(() => {
    createWindow();

    app.on("activate", () => {
        if (BrowserWindow.getAllWindows().length === 0) {
            createWindow();
        }
    });
});

app.on("window-all-closed", () => {
    if (process.platform !== "darwin") {
        app.quit();
    }
});