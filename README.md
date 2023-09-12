# Smoke Analytics

[![CI](https://github.com/feragusper/SmokeAnalytics/actions/workflows/deployment.yml/badge.svg?branch=master)](https://github.com/feragusper/SmokeAnalytics/actions/workflows/deployment.yml)

Android application to track smoking habits and run analytics on them.

Architecture
------------
Based on Clean Architecture. Using a MVI, Use Cases and Repositories.

Libraries included
-----------------

- Kotlin
- Architecture Components
- Kotlin Coroutines/Flow
- Hilt
- Retrofit and OkHttp
- Compose
- Navigation Component
- JUnit
- Mockito

CI
--
There's already a workflow available for downloading the last stable build at https://github.com/feragusper/SmokeAnalytics/actions

Getting Started
---------------

> :warning: **This project won't compile unless an GOOGLE_AUTH_SERVER_CLIENT_ID value is provided through local.properties or through env vars**

In order to build it locally, add `google.auth.server.client.id=\"GOOGLE_AUTH_SERVER_CLIENT_ID\"` to your local.properties

It requires java 17 to run.

- Use `./gradlew assemble` to build it, or run it in Android Studio.
- Use `./gradlew test` to run the unit test on your local host.

Support
-------
If you've found an error in this project, please file an issue: https://github.com/feragusper/SmokeAnalytics/issues

Patches are encouraged, and may be submitted by forking this project and submitting a pull request through GitHub.

Contribute
----------
Pull requests are welcome.

1. Fork it!
2. Create your feature branch: `git checkout -b my-new-feature`
3. Commit your changes: `git commit -am 'Add some feature'`
4. Push to the branch: `git push origin my-new-feature`
5. Submit a pull request :D
