// Timeout configs
GLOBAL_TIMEOUT_MINUTES = 120
CTEST_TIMEOUT_SECONDS = 1200

// Pull Request Information
OE_PULL_NUMBER=env.OE_PULL_NUMBER?env.OE_PULL_NUMBER:"master"

// OS Version Configuration
LINUX_VERSION=env.LINUX_VERSION?env.LINUX_VERSION:"1604"

// Some Defaults
DOCKER_TAG=env.DOCKER_TAG?env.DOCKER_TAG:"latest"
PUSH=env.PUSH?env.PUSH:"true"
//String[] BUILD_TYPES = ['Debug', 'RelWithDebInfo', 'Release']
String[] BUILD_TYPES = ['Release']
//String[] SUPPORTED_REPOS = ['openenclave', 'oeedger8r-cpp']
String[] SUPPORTED_REPOS = ['oeedger8r-cpp']


// Some override for build configuration
EXTRA_CMAKE_ARGS=env.EXTRA_CMAKE_ARGS?env.EXTRA_CMAKE_ARGS:"-DLVI_MITIGATION=ControlFlow -DLVI_MITIGATION_SKIP_TESTS=OFF -DUSE_SNMALLOC=ON"

// Repo hardcoded
REPO="test-infra"

// Shared library config, check out common.groovy!
def SHARED_LIBRARY="/config/jobs/"+"${REPO}"+"/jenkins/images/common.groovy"

pipeline {
    environment {
        registry = "openenclave/ubuntu-1604"
        registryCredential = 'dockerhub_openenclave_id'
        dockerImage = ''
    }
    options {
        timeout(time: 60, unit: 'MINUTES') 
    }
    agent { label "ACC-${LINUX_VERSION}" }

    stages {
        stage('Test Image'){
            steps{
                script{

                    // BUILD IMAGE
                    cleanWs()
                    checkout scm
                    def dockerImage = docker.build("openenclave/ubuntu-${LINUX_VERSION}:${BUILD_NUMBER}",". -f images/ubuntu/${LINUX_VERSION}/Dockerfile")

                    docker.withRegistry('', 'dockerhub_openenclave_id') {
                        /* Push the container to the custom Registry */
                        dockerImage.push()
                    }
                }
            }
        }
    }
}