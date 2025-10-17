# Manual Keystore Setup for GitHub Actions

This guide explains how to manually generate a debug keystore and set it as a repository secret using the GitHub CLI (`gh`) on your local machine.

### Prerequisites

1.  **Install GitHub CLI:** If you don't have it, follow the official installation instructions at [cli.github.com](https://cli.github.com/).
2.  **Authenticate:** Run `gh auth login` in your terminal and follow the prompts to log in to your GitHub account.

### Steps

1.  **Open your terminal** and navigate to the root of your project directory:
    ```bash
    cd /path/to/your/Kiosk-Launcher
    ```

2.  **Generate the keystore file** with the following command. This creates a `debug.keystore` file in your current directory.
    ```bash
    keytool -genkey -v -keystore debug.keystore -storepass android -alias androiddebugkey -keypass android -keyalg RSA -keysize 2048 -validity 10000 -dname "C=US, O=Android, CN=Android Debug"
    ```

3.  **Create the repository secret.** This command reads the `debug.keystore` file, encodes it, and sets it as a secret named `DEBUG_KEYSTORE_BASE64` in your GitHub repository.
    ```bash
    gh secret set DEBUG_KEYSTORE_BASE64 -R karlcc/Kiosk-Launcher < debug.keystore
    ```

4.  **(Optional) Clean up.** The secret is now safely stored in GitHub, so you can delete the local keystore file.
    ```bash
    rm debug.keystore
    ```

---

Once these steps are complete, your `build-apk.yml` workflow will be able to sign debug builds with a consistent key. You can now safely delete the `.github/workflows/keystore.yml` file.
