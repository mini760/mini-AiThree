# Mini — Phase 1

Mini is an Android AI Agent designed to observe the screen and perform automated tasks securely using the Gemini API and Android Accessibility Services.

## Phase 1 Architecture
- **Agent Engine**: Coroutine-based state machine handling planning, execution, and verification.
- **Vision**: Uses `MediaProjection` for safe, leak-free screen capture.
- **Action System**: Strict JSON parsing and validation before execution.
- **Accessibility**: `AccessibilityController` executes gestures safely without retaining context.
- **Security**: API keys are injected via `local.properties` (dev) or GitHub Secrets (CI).

## Setup Instructions (Manual Creation)

1. **Create the Project**: Copy the provided file tree and code into your Android Studio project.
2. **API Key Configuration**:
   - Create a file named `local.properties` in the root directory.
   - Add your Gemini API key: `GEMINI_API_KEY=your_actual_api_key_here`
   - *Note: `local.properties` is in `.gitignore` and will not be committed.*
3. **GitHub Actions Setup**:
   - In your GitHub repository (`mini760/mini-AiThree`), go to Settings > Secrets and variables > Actions.
   - Add the following repository secrets for release signing:
     - `KEYSTORE_BASE64` (Base64 encoded `.jks` file)
     - `KEYSTORE_PASSWORD`
     - `KEY_ALIAS`
     - `KEY_PASSWORD`
     - `GEMINI_API_KEY` (For CI builds)

## Running the App
1. Build and install the app on an Android 14+ device.
2. Open the app and tap **Enable Accessibility**. Turn on "Mini AI Agent" in settings.
3. Tap **Enable Overlay Permission** and grant the permission.
4. Tap **Start Mini Overlay**. You will be prompted to allow Screen Capture.
5. The floating overlay will appear. Enter a task and tap **Run**.

## Security Warnings
- **NEVER** commit your `local.properties` or `.jks` files.
- The AI is not trusted. All actions pass through `ActionValidator` before execution.
- The app requires `FOREGROUND_SERVICE_MEDIA_PROJECTION` to capture the screen safely.

## Limitations (Phase 1)
- UI tree parsing via Accessibility nodes is deferred to Phase 2; Phase 1 relies on Vision (Gemini 1.5 Flash).
- Database logging is implemented in Room but UI history viewing is deferred.
