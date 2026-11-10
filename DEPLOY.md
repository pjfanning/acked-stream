# Releasing

Releases go to Maven Central via the Sonatype Central Portal, using sbt's built-in support (sbt 1.11+).
Credentials for host `central.sonatype.com` must be configured (e.g. in `~/.sbt/1.0/credentials.sbt`, or the
`SONATYPE_USERNAME` / `SONATYPE_PASSWORD` environment variables), and your PGP key must be available to sbt-pgp.

1. Set the release version (no `-SNAPSHOT`) in `project/version.properties`.
2. Test, sign and stage, then upload and release:

   ```
   sbt +test
   sbt +publishSigned sonaRelease
   ```

   `+publishSigned` stages the signed artifacts in `target/sona-staging`; `sonaRelease` uploads the bundle to the
   Central Portal and releases it. Use `sonaUpload` instead to upload without releasing, then publish from
   https://central.sonatype.com/publishing.

3. Commit, tag and push:

   ```
   source project/version.properties
   git commit -am "release $version"
   git tag v$version
   git push origin main v$version
   ```

4. Bump `project/version.properties` to the next `-SNAPSHOT` version and push.
