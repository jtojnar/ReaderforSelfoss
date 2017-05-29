# ReaderForSelfoss

[![CircleCI](https://circleci.com/gh/aminecmi/ReaderforSelfoss.svg?style=svg)](https://circleci.com/gh/aminecmi/ReaderforSelfoss)

This is the repo of [Reader For Selfoss](https://play.google.com/store/apps/details?id=apps.amine.bou.readerforselfoss&hl=en).

It's an RSS Reader for Android, that **only** works with [Selfoss](https://selfoss.aditu.de/)


## Build

You can directly import this project into IntellIJ/Android Studio.

You'll have to:

- [Create your own launcher icon](https://developer.android.com/studio/write/image-asset-studio.html#creating-launcher)

- Configure Fabric, or [remove it](https://docs.fabric.io/android/fabric/settings/removing.html#).
- Define the following in `res/values/strings.xml` or create `res/values/secrets.xml`

    - mercury: A [Mercury](https://mercury.postlight.com/web-parser/) web parser api key for the internal browser
    - feedback_email: An email to receive users  feedback.
    - source_url: an url to the source code, used in the settings
    - tracker_url: an url to the tracker, used in the settings

## Useful links

- [Check what changed](https://github.com/aminecmi/ReaderforSelfoss/blob/master/CHANGELOG.md)
- [See what I'm doing](https://github.com/aminecmi/ReaderforSelfoss/projects/1)
- [Create an issue, or request a new feature](https://github.com/aminecmi/ReaderforSelfoss/issues)
