const fs = require("fs");
const path = require("path");

const KEEP_LOCALES = new Set(["en-US.pak", "vi.pak"]);
const OPTIONAL_RUNTIME_FILES = [
  "LICENSES.chromium.html",
  "vk_swiftshader.dll",
  "vulkan-1.dll",
  "dxcompiler.dll",
  "dxil.dll"
];

exports.default = async function afterPack(context) {
  const localesDir = path.join(context.appOutDir, "locales");
  const backendSourceDir = path.join(context.packager.projectDir, "..", "release", "backend");
  const backendTargetDir = path.join(context.appOutDir, "resources", "backend");
  const backendJarSource = path.join(context.packager.projectDir, "..", "release", "backend-0.0.1-SNAPSHOT.jar");
  const backendJarTarget = path.join(backendTargetDir, "backend-0.0.1-SNAPSHOT.jar");

  if (fs.existsSync(localesDir)) {
    for (const entry of fs.readdirSync(localesDir)) {
      const fullPath = path.join(localesDir, entry);
      const stat = fs.statSync(fullPath);

      if (stat.isFile() && entry.endsWith(".pak") && !KEEP_LOCALES.has(entry)) {
        fs.rmSync(fullPath, { force: true });
      }
    }
  }

  for (const entry of OPTIONAL_RUNTIME_FILES) {
    fs.rmSync(path.join(context.appOutDir, entry), { force: true });
  }

  if (fs.existsSync(backendSourceDir)) {
    fs.cpSync(backendSourceDir, backendTargetDir, { recursive: true });
  }

  if (fs.existsSync(backendJarSource)) {
    fs.mkdirSync(backendTargetDir, { recursive: true });
    fs.copyFileSync(backendJarSource, backendJarTarget);
  }
};
