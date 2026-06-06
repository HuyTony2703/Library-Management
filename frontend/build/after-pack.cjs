const fs = require("fs");
const path = require("path");

const KEEP_LOCALES = new Set(["en-US.pak", "vi.pak"]);

exports.default = async function afterPack(context) {
  const localesDir = path.join(context.appOutDir, "locales");

  if (!fs.existsSync(localesDir)) {
    return;
  }

  for (const entry of fs.readdirSync(localesDir)) {
    const fullPath = path.join(localesDir, entry);
    const stat = fs.statSync(fullPath);

    if (stat.isFile() && entry.endsWith(".pak") && !KEEP_LOCALES.has(entry)) {
      fs.rmSync(fullPath, { force: true });
    }
  }
};
