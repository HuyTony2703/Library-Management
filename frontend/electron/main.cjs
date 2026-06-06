const { app, BrowserWindow, Menu, shell, ipcMain } = require("electron");
const fs = require("fs");
const http = require("http");
const path = require("path");
const { spawn } = require("child_process");

let mainWindow;
let backendProcess;

const BACKEND_PORT = 8080;
const HEALTH_URL = `http://localhost:${BACKEND_PORT}/api/health`;

app.setName("LibraDesk");
Menu.setApplicationMenu(null);

function getConfigPath() {
    return path.join(app.getPath("userData"), "database-config.json");
}

function readDatabaseConfig() {
    try {
        const raw = fs.readFileSync(getConfigPath(), "utf8");
        return JSON.parse(raw);
    } catch {
        return null;
    }
}

function writeDatabaseConfig(config) {
    fs.mkdirSync(path.dirname(getConfigPath()), { recursive: true });
    fs.writeFileSync(getConfigPath(), JSON.stringify(config, null, 2), "utf8");
}

function buildJdbcUrl(config) {
    const host = config.host?.trim() || "localhost";
    const port = config.port?.trim() || "1433";
    const database = config.database?.trim() || "QuanLyThuVien";
    const authPart = config.authMode === "windows" ? ";integratedSecurity=true" : "";

    return `jdbc:sqlserver://${host}:${port};databaseName=${database};encrypt=true;trustServerCertificate=true${authPart}`;
}

function toBackendEnv(config) {
    const nativePath = config.nativePath || "";
    const env = {
        ...process.env,
        DB_URL: buildJdbcUrl(config)
    };

    if (nativePath) {
        env.PATH = `${nativePath}${path.delimiter}${env.PATH || ""}`;
    }

    if (config.authMode === "sql") {
        env.DB_USERNAME = config.username || "";
        env.DB_PASSWORD = config.password || "";
    } else {
        delete env.DB_USERNAME;
        delete env.DB_PASSWORD;
    }

    return env;
}

function requestHealth(timeoutMs = 1500) {
    return new Promise((resolve) => {
        const request = http.get(HEALTH_URL, { timeout: timeoutMs }, (response) => {
            response.resume();
            resolve(response.statusCode >= 200 && response.statusCode < 500);
        });

        request.on("timeout", () => {
            request.destroy();
            resolve(false);
        });
        request.on("error", () => resolve(false));
    });
}

async function waitForBackend(timeoutMs = 60000) {
    const startedAt = Date.now();

    while (Date.now() - startedAt < timeoutMs) {
        if (await requestHealth()) {
            return true;
        }

        await new Promise((resolve) => setTimeout(resolve, 1000));
    }

    return false;
}

function findBackendJar() {
    const candidates = [
        path.join(process.resourcesPath || "", "backend", "backend-0.0.1-SNAPSHOT.jar"),
        path.join(app.getAppPath(), "backend", "backend-0.0.1-SNAPSHOT.jar"),
        path.join(__dirname, "..", "..", "release", "backend-0.0.1-SNAPSHOT.jar"),
        path.join(__dirname, "..", "..", "backend", "target", "backend-0.0.1-SNAPSHOT.jar")
    ];

    return candidates.find((candidate) => candidate && fs.existsSync(candidate)) || null;
}

async function startBackend(config) {
    if (!config) {
        return {
            ok: false,
            message: "Chua co cau hinh SQL Server."
        };
    }

    if (await requestHealth()) {
        return { ok: true, alreadyRunning: true };
    }

    const jarFile = findBackendJar();

    if (!jarFile) {
        return {
            ok: false,
            message: "Khong tim thay file backend-0.0.1-SNAPSHOT.jar trong release hoac backend/target."
        };
    }

    const nativePath = path.dirname(jarFile);
    const javaArgs = [`-Djava.library.path=${nativePath}`, "-jar", jarFile];

    backendProcess = spawn("java", javaArgs, {
        cwd: path.dirname(jarFile),
        env: toBackendEnv({ ...config, nativePath }),
        windowsHide: true,
        stdio: "ignore"
    });

    backendProcess.on("exit", () => {
        backendProcess = null;
    });

    const ready = await waitForBackend();

    if (!ready) {
        return {
            ok: false,
            message: "Backend khong khoi dong duoc. Kiem tra Java, SQL Server va thong tin ket noi database."
        };
    }

    return { ok: true, alreadyRunning: false };
}

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
            preload: path.join(__dirname, "preload.cjs"),
            nodeIntegration: false,
            contextIsolation: true
        }
    });

    mainWindow.on("page-title-updated", (event) => {
        event.preventDefault();
        mainWindow.setTitle("LibraDesk");
    });

    const devServerUrl = process.env.ELECTRON_START_URL;

    if (devServerUrl) {
        mainWindow.loadURL(devServerUrl);
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

ipcMain.handle("database:get-config", () => readDatabaseConfig());
ipcMain.handle("database:save-config", (_event, config) => {
    writeDatabaseConfig(config);
    return readDatabaseConfig();
});
ipcMain.handle("backend:start", (_event, config) => startBackend(config || readDatabaseConfig()));
ipcMain.handle("backend:health", () => requestHealth());

app.whenReady().then(() => {
    createWindow();

    app.on("activate", () => {
        if (BrowserWindow.getAllWindows().length === 0) {
            createWindow();
        }
    });
});

app.on("before-quit", () => {
    if (backendProcess) {
        backendProcess.kill();
        backendProcess = null;
    }
});

app.on("window-all-closed", () => {
    if (process.platform !== "darwin") {
        app.quit();
    }
});
