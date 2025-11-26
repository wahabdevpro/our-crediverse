## Docker image
The docker image used for this runner is available [here](https://github.com/myoung34/docker-github-actions-runner/)

## Access token
The `docker-compose` file uses an `ACCESS_TOKEN` to register the runner on github every time on startup. The current token is a `repo` level token. The recommendation is to create a dedicated user account on github for running the CI/CD pipelines of the various projects and create an `ACCESS_TOKEN` with scope set to either `admin:org` or `admin:enterprise`.
