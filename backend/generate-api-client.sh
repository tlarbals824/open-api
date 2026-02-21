#!/bin/bash
set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
SWAGGER_JSON="$SCRIPT_DIR/swagger.json"
OUTPUT_DIR="$SCRIPT_DIR/generated-api-client"
PACKAGE_NAME="@ject-2-test/backend-api-client"

echo "=== 1. Backend clean build ==="
cd "$SCRIPT_DIR"
./gradlew clean compileKotlin -q

echo "=== 2. Starting backend server ==="
./gradlew bootRun -q &
BOOT_PID=$!
trap "kill -- -$BOOT_PID 2>/dev/null || kill $BOOT_PID 2>/dev/null; sleep 2" EXIT

for i in $(seq 1 30); do
  if curl -s http://localhost:8080/v3/api-docs > /dev/null 2>&1; then
    echo "Server started."
    break
  fi
  sleep 2
done

echo "=== 3. Extracting swagger.json ==="
curl -s http://localhost:8080/v3/api-docs | python3 -m json.tool > "$SWAGGER_JSON"
echo "Saved: $SWAGGER_JSON"

echo "=== 4. Stopping backend server ==="
# Kill gradle + java child processes
kill -- -$BOOT_PID 2>/dev/null || kill $BOOT_PID 2>/dev/null
# Also kill any leftover java process on port 8080
lsof -ti:8080 | xargs kill -9 2>/dev/null || true
sleep 2
trap - EXIT

echo "=== 5. Generating TypeScript API client ==="
rm -rf "$OUTPUT_DIR"
npx @openapitools/openapi-generator-cli generate \
  -i "$SWAGGER_JSON" \
  -g typescript-axios \
  -o "$OUTPUT_DIR" \
  -t "$SCRIPT_DIR/openapi-templates" \
  --additional-properties=supportsES6=true,withSeparateModelsAndApi=true,apiPackage=api,modelPackage=models \
  2>&1 | tail -5

echo "=== 6. Setting up npm package ==="
cat > "$OUTPUT_DIR/package.json" <<'PKGJSON'
{
  "name": "@ject-2-test/backend-api-client",
  "version": "VERSION_PLACEHOLDER",
  "description": "Backend API client generated from OpenAPI spec",
  "main": "dist/index.js",
  "types": "dist/index.d.ts",
  "files": ["dist"],
  "scripts": {
    "build": "tsc",
    "prepublishOnly": "npm run build"
  },
  "dependencies": {
    "axios": "^1.7.0"
  },
  "devDependencies": {
    "typescript": "^5.5.0"
  },
  "license": "MIT"
}
PKGJSON

cat > "$OUTPUT_DIR/tsconfig.json" <<'TSCONF'
{
  "compilerOptions": {
    "target": "ES2020",
    "module": "commonjs",
    "lib": ["ES2020", "DOM"],
    "declaration": true,
    "strict": true,
    "noImplicitAny": true,
    "strictNullChecks": true,
    "moduleResolution": "node",
    "esModuleInterop": true,
    "skipLibCheck": true,
    "forceConsistentCasingInFileNames": true,
    "outDir": "./dist",
    "rootDir": "."
  },
  "include": ["*.ts", "api/**/*.ts", "models/**/*.ts"],
  "exclude": ["node_modules", "dist"]
}
TSCONF

# Determine version: bump minor from current npm version, or use 1.0.0
CURRENT_VERSION=$(npm view "$PACKAGE_NAME" version 2>/dev/null || echo "0.0.0")
IFS='.' read -r MAJOR MINOR PATCH <<< "$CURRENT_VERSION"
NEW_VERSION="$MAJOR.$((MINOR + 1)).0"
sed -i '' "s/VERSION_PLACEHOLDER/$NEW_VERSION/" "$OUTPUT_DIR/package.json"
echo "Version: $CURRENT_VERSION -> $NEW_VERSION"

echo "=== 7. Installing dependencies & building ==="
cd "$OUTPUT_DIR"
npm install -q 2>&1
npm run build 2>&1

echo "=== 8. Publishing to npm ==="
npm publish --access public 2>&1

echo ""
echo "=== Done! Published $PACKAGE_NAME@$NEW_VERSION ==="
