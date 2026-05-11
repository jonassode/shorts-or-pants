# shorts-or-pants

## Android CI builds

Pushes to `main` now produce a signed release APK in GitHub Actions. Configure these repository secrets for the Android workflow:

- `ANDROID_CI_KEYSTORE_BASE64`
- `ANDROID_CI_KEYSTORE_PASSWORD`
- `ANDROID_CI_KEY_ALIAS`
- `ANDROID_CI_KEY_PASSWORD`

The workflow sets `versionCode` from the GitHub Actions run number so each CI release build gets a higher version code than the last one.
