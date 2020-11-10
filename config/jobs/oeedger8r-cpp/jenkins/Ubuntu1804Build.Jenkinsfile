// Timeout configs
GLOBAL_TIMEOUT_MINUTES = 120
CTEST_TIMEOUT_SECONDS = 1200

// Pull Request Information
OE_PULL_NUMBER=env.OE_PULL_NUMBER?env.OE_PULL_NUMBER:"master"

// OS Version Configuration
LINUX_VERSION=env.LINUX_VERSION?env.LINUX_VERSION:"1804"

// Some Defaults
DOCKER_TAG=env.DOCKER_TAG?env.DOCKER_TAG:"latest"
BUILD_TYPE=env.BUILD_TYPE?env.BUILD_TYPE:"Release"

// Some override for build configuration
EXTRA_CMAKE_ARGS = env.EXTRA_CMAKE_ARGS?env.EXTRA_CMAKE_ARGS:""

// Repo hardcoded
REPO="oeedger8r-cpp"

// Shared library config, check out common.groovy!
SHARED_LIBRARY="/config/jobs/"+"${REPO}"+"/jenkins/common.groovy"

pipeline {
    options {
        timeout(time: 30, unit: 'MINUTES') 
    }
    agent { label "ACC-${LINUX_VERSION}" }
    stages {
        stage( 'Ubuntu 1804 Build') {
            steps {
                script {
                    //docker.image("openenclave/windows-${LINUX_VERSION}:${DOCKER_TAG}").inside {
                        cleanWs()
                        checkout scm
                        def runner = load pwd() + "${SHARED_LIBRARY}"
                        runner.checkout("${REPO}", "${OE_PULL_NUMBER}")
                        runner.cmakeBuild("${REPO}","${BUILD_TYPE}")
                    //}
                }
            }
        }
    }
}