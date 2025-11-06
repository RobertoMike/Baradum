# Release Process

This document describes how to release new versions of Baradum to Maven Central.

## Prerequisites

Before you can publish to Maven Central, you need to set up the following secrets in your GitHub repository:

### Required Secrets

1. **GPG Keys for Signing**
   - `GPG_PRIVATE_KEY`: Your GPG private key in ASCII-armored format
   - `GPG_SECRET_KEY_PASSWORD`: The passphrase for your GPG key
   - `GPG_SHORT_KEY`: The short key ID (last 8 characters of your key ID)

2. **Maven Central Credentials**
   - `OSSRH_USERNAME`: Your Maven Central username (user token username from central.sonatype.com)
   - `OSSRH_TOKEN`: Your Maven Central password (user token password from central.sonatype.com)

### Setting Up Maven Central Access

As of July 2024, Maven Central uses a new publishing portal. You need to:

1. Go to https://central.sonatype.com/
2. Sign in with your Sonatype account
3. Generate a user token (Account → Generate User Token)
4. Use the token username as `OSSRH_USERNAME`
5. Use the token password as `OSSRH_TOKEN`

### Setting Up GPG Keys

If you don't have a GPG key yet:

```bash
# Generate a new GPG key
gpg --full-generate-key

# List your keys to get the key ID
gpg --list-secret-keys --keyid-format=short

# Export your private key in ASCII format
gpg --armor --export-secret-keys YOUR_KEY_ID > private-key.asc

# Export your public key
gpg --armor --export YOUR_KEY_ID > public-key.asc

# Upload your public key to a key server
gpg --keyserver keyserver.ubuntu.com --send-keys YOUR_KEY_ID
```

## Creating a Release

The release process is automated via GitHub Actions. To trigger a release:

### 1. Release All Modules (Baradum + Apache Tomcat)

Create a GitHub release with a tag that contains `-all`:

```bash
git tag -a v2.1.3-all -m "Release version 2.1.3 for all modules"
git push origin v2.1.3-all
```

Then create a GitHub release from this tag.

### 2. Release Baradum Only

Create a GitHub release with a tag that contains `-baradum`:

```bash
git tag -a v2.1.3-baradum -m "Release version 2.1.3 for Baradum module"
git push origin v2.1.3-baradum
```

### 3. Release Apache Tomcat Module Only

Create a GitHub release with a tag that contains `-apache-tomcat`:

```bash
git tag -a v2.0.4-apache-tomcat -m "Release version 2.0.4 for Apache Tomcat module"
git push origin v2.0.4-apache-tomcat
```

## What Happens During Release

When you create a GitHub release:

1. **Build**: The code is compiled and tested
2. **Sign**: Artifacts are signed with your GPG key using Gradle's signing plugin
3. **Stage**: Signed artifacts are published to a local staging directory
4. **Deploy**: JReleaser bundles and uploads the artifacts to Maven Central Portal
5. **Publish**: Maven Central automatically validates and publishes the artifacts

## Troubleshooting

### Check the Workflow Logs

If a release fails, check the GitHub Actions workflow logs:
- Go to your repository on GitHub
- Click on "Actions"
- Find the failed workflow run
- Check the logs for errors

### Common Issues

1. **GPG Signing Fails**
   - Verify `GPG_PRIVATE_KEY` and `GPG_SECRET_KEY_PASSWORD` are correct
   - Ensure the key hasn't expired

2. **Maven Central Authentication Fails**
   - Verify `OSSRH_USERNAME` and `OSSRH_TOKEN` are from central.sonatype.com
   - Make sure you're using a user token, not your account password

3. **Artifacts Already Exist**
   - Maven Central doesn't allow overwriting released versions
   - You need to increment the version number in `build.gradle.kts`

## Version Management

Before creating a release, update the version numbers:

- Main module: `build.gradle.kts` → `version = "x.y.z"`
- Apache Tomcat module: `apache-tomcat/build.gradle.kts` → `version = "x.y.z"`

## Migration from Old OSSRH

The old workflow used the OSSRH staging repository (s01.oss.sonatype.org) which was deprecated in July 2024. 

The new workflow uses:
- **Central Portal API**: https://central.sonatype.com/api/v1/publisher
- **JReleaser**: Handles bundling and uploading artifacts
- **Simplified Process**: No manual staging/promotion required

Your existing GPG keys and Maven Central account continue to work with the new system.
