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

2.  **(If recreating) Remove the existing secret first:**
    ```bash
    gh secret remove DEBUG_KEYSTORE_BASE64 -R karlcc/Kiosk-Launcher
    ```

3.  **Generate the keystore file** with the following command. This creates a `debug.keystore` file in your current directory.
    ```bash
    # Remove existing keystore if it exists
    rm -f debug.keystore

    # Generate new keystore
    keytool -genkey -v -keystore debug.keystore -storepass android -alias androiddebugkey -keypass android -keyalg RSA -keysize 2048 -validity 10000 -dname "C=US, O=Android, CN=Android Debug"
    ```

4.  **Encode and create the repository secret.** First encode the keystore to base64, then set it as a secret:
    ```bash
    base64 -i debug.keystore | gh secret set DEBUG_KEYSTORE_BASE64 -R karlcc/Kiosk-Launcher
    ```

    **Note:** We manually base64-encode the keystore so it can be safely stored as a text secret. The workflow will decode it back to binary.

5.  **(Optional) Clean up.** The secret is now safely stored in GitHub, so you can delete the local keystore file.
    ```bash
    rm debug.keystore
    ```

---

Once these steps are complete, your `build-apk.yml` workflow will be able to sign debug builds with a consistent key. You can now safely delete the `.github/workflows/keystore.yml` file.
