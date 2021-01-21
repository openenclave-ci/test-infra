// Shared library config, check out common.groovy!
LINUX_VERSION=env.LINUX_VERSION?env.LINUX_VERSION:"Ubuntu-1804"
SHARED_LIBRARY="/config/jobs/test-infra-images/jenkins/common.groovy"

pipeline {
    options {
        timeout(time: 60, unit: 'MINUTES') 
    }
    agent { label "ACC-${LINUX_VERSION}" }

    stages {

        stage('Checkout'){
            steps{
                cleanWs()
                checkout scm
            }
        }

        stage('Launch base VM'){
            steps{
                script{
                    sh  """
                        echo 'gotta start somewhere'
                        """
                }
            }
        }
    }

    post ('Clean Up'){
        always{
            cleanWs()
        }
    }
}