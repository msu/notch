# Publishing Notch to Maven Central

## How to publish

Make sure the private secrets repo is cloned (gitignored) into `publishing/` directory:

```sh
git clone git@github.com:msu/maven_keys.git publishing/maven_keys
```

For each release:

```sh
# 1. Build, sign, and stage the deployment.
mvn -s publishing/maven_keys/settings.xml clean deploy -Pcentral

# 2. Tag and push so release.yml builds the native installers.
git tag v0.1.1
git push origin v0.1.1
```

Artifact should be at `https://central.sonatype.com/artifact/edu.montana.cs.notch/notch` may take a few minutes.





## Details:

### 1. Account and namespace

- Authorization belongs to Sonatype account `OpenSource@montana.edu` (login in `maven_keys/sonatype-login.txt`)
- The `edu.montana` namespace is verified on `central.sonatype.com` (via a Portal issued key in a DNS TXT record). A verified namespace covers all sub-namespaces, so any `edu.montana.*` groupId (notch, pika-orm, etc.) publishes without additional setup.
  - More info at: [namespace registration](https://central.sonatype.org/register/namespace/), [setting the TXT record](https://central.sonatype.org/faq/how-to-set-txt-record/), and [publishing requirements](https://central.sonatype.org/publish/requirements/)


### 2. GPG release key

Every artifact must be signed with a key whose public half is on a public keyserver.

```sh
gpg --gen-key
gpg --keyserver keyserver.ubuntu.com --send-keys <KEY_ID>
gpg --keyserver keys.openpgp.org --send-keys <KEY_ID>
gpg --armor --export-secret-keys <KEY_ID> > release-key.asc   # private half goes into our maven_keys repo
```

### 3. Portal user token

* Generate a token at https://central.sonatype.com/usertoken

* Record both halves in `sonatype-token.txt` and in `settings.xml` with in the maven_keys repo. 
  * Note that generating a new token will revoke the old one.

### 4. The maven_keys secrets repo

```
maven_keys/
  release-key.asc      # armored, passphrase-encrypted GPG private key
  passphrase.txt       # the GPG passphrase (I dont know if I should have put it here but did)
  sonatype-login.txt   # Portal login
  sonatype-token.txt   # Portal user token (copy of what's in settings.xml)
  settings.xml         # ready-to-use Maven settings
```

Anyone with read access holds the signing key. Rotations of (GPG key, new passphrase, and new token) for new maintainers.

### 5. settings.xml

File in `maven_keys` this is what the  `-s` flag points to:

```xml
<settings>
    <servers>
        <server>
            <id>central</id>
            <username><!-- token username from sonatype-token.txt --></username>
            <password><!-- token password from sonatype-token.txt --></password>
        </server>
        <server>
            <id>gpg.passphrase</id>
            <passphrase><!-- contents of passphrase.txt --></passphrase>
        </server>
    </servers>
</settings>
```

The two `<id>` values:

* `central` must match the publishing plugin's `<publishingServerId>`
*  `gpg.passphrase` must match the gpg plugin's `<passphraseServerId>`.

**More on why:**

The gpg plugin looks up `gpg.passphrase` when signing runs at verify. The gpg plugin reads the passphrase out of settings.xml and feeds it to a gpg process that uses it to unlock the encrypted private key and then signs each artifact (writing a detached .asc signature file alongside it). This prevent the user from having to go through a interactive promt for signing hence the  `--pinentry-mode loopback` in the `pom.xml`.

The publishing plugin looks up `central` when the upload runs at deploy. Publishing plugin zips the staged artifacts and POSTs it to the Portal's API at central.sonatype.com. username/password pair looked up encoded into the Authorization header and verified by the maven site (central.sonatype.com).

### 6. pom.xml

**Project metadata**: `<description>`, `<url>`, `<licenses>`, `<developers>`, and `<scm>` are validation requirements.

**The `central` profile** (activated by `-Pcentral`) contains five plugin configurations:

- `maven-source-plugin` / `maven-javadoc-plugin` attach the sources and javadoc jars Central requires.
- `exec-maven-plugin` runs `gpg --batch --import` on the key file at the `validate` phase, so signing never fails for want of an imported key (see Details).
- `maven-gpg-plugin` signs everything at `verify`.
- `central-publishing-maven-plugin` bundles and uploads at `deploy`.





The `-s` flag tells Maven to use this file instead of the default ~/.m2/settings.xml so publishing doesn't need setup on any individual machine.

### `-Pcentral`

It produces the `target/notch.jar` uber-jar. The `central` profile layers on the publishing plugins and swaps the shaded uber-jar for the plain library jar so users get get dependencies through the pom instead of bundled copies.

### Publish (may need to change idk)

`<autoPublish>false</autoPublish>` in the pom means a successful `mvn deploy` only *stages* the upload. A person can review the validator output on the Portal and click **Publish** (or **Drop**).



Also Note that 

- `notch.shade.skip` for central only exists because notch's default build is a shaded uber-jar.
- The `git tag` / `release.yml` step builds notch's native installers and has nothing to do with Maven Central.
