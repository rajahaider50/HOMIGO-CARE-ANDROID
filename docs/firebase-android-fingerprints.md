# Firebase Android fingerprints

## Debug keystore received from Termux

These are non-secret certificate fingerprints for the Termux debug keystore:

- SHA-1: `DD:1C:60:CF:51:4B:61:D3:26:4A:31:07:19:82:96:61:BF:C3:E4:A2`
- SHA-256: `0A:1C:A8:AB:5C:56:28:7F:B6:3A:40:11:E4:03:91:1B:43:2B:F0:ED:6C:22:9B:1E:79:8C:C2:63:87:91:84:3A`

The production Firebase Android app has both fingerprints registered. The development app has SHA-256 registered; the Firebase MCP returned an error while adding the SHA-1, so add that one manually in Firebase Console if Google Sign-In testing requires it.

## Release keystore

Do not commit the `.jks` file or passwords. After creating the release keystore in Termux, send only its SHA-1 and SHA-256 fingerprints. Register release fingerprints separately from debug fingerprints in both Firebase Android apps.
