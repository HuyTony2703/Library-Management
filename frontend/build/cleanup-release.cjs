const fs = require("fs");
const path = require("path");

const releaseDir = path.resolve(__dirname, "../../release");

if (!fs.existsSync(releaseDir)) {
  process.exit(0);
}

for (const entry of fs.readdirSync(releaseDir)) {
  const fullPath = path.join(releaseDir, entry);
  const stat = fs.statSync(fullPath);

  if (stat.isDirectory() && /^win.*-unpacked$/.test(entry)) {
    fs.rmSync(fullPath, { recursive: true, force: true });
  }
}
