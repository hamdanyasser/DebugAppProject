## Steps
- Add robust error handling in Profile sign-in to display the exact Google Sign-In status code and message.
- Validate the OAuth client id used for `requestIdToken` and block sign-in if it’s still a placeholder.
- Generate the debug SHA-1/SH‑256 with `./gradlew signingReport` for you to add in Firebase console.
- Rebuild and reinstall; test sign-in again.

## Expected Causes
- Missing/incorrect `default_web_client_id` from `google-services.json`.
- Debug SHA-1 not added to Firebase → token invalid → cancellation.
- Emulator accounts misconfiguration or network hiccup.

## Outcome
- Clear on-device messages for misconfiguration vs real cancellation.
- Provide SHA-1 so you can update Firebase; after updating JSON, sign-in succeeds without cancellation.