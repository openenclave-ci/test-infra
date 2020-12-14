---
jobs:
  - script: |
        folder("oeedger8r-cpp") {
            displayName("eedger8r-cpp")
        }
        pipelineJob("oeedger8r-cpp/Ubuntu1804BuildClang-7) {
            description()
            keepDependencies(false)
            parameters {
                stringParam {
                    name("PULL_NUMBER")
                    defaultValue("master")
                    description("")
                    trim(true)
                }
                stringParam {
                    name("LINUX_VERSION")
                    defaultValue("1804")
                    description("")
                    trim(true)
                }
                stringParam {
                    name("COMPILER")
                    defaultValue("clang-7")
                    description("")
                    trim(true)
                }
            } 
            authenticationToken('<JENKINS_REMOTE_TRIGGER_TOKEN>')
            definition {
                cpsScm {
                    scm {
                        git {
                            remote {
                                github("openenclave/test-infra", "https")
                            }
                            branch('*/master')
                            extensions {
                                cleanBeforeCheckout()
                                cleanAfterCheckout()
                                cloneOptions {
                                    depth(1)
                                    honorRefspec(false)
                                    noTags(false)
                                    reference("")
                                    shallow(true)
                                    timeout(10)
                                }
                                pruneBranches()
                                submoduleOptions {
                                    disable(false)
                                    parentCredentials(false)
                                    recursive(true)
                                    reference("")
                                    timeout(10)
                                    tracking(false)
                                }
                                wipeOutWorkspace()
                            }
                        }
                    }
                    scriptPath("config/jobs/oeedger8r-cpp/jenkins/UbuntuBuild.Jenkinsfile")
                }
            }
            disabled(false)
            configure {
                it / 'properties' / 'jenkins.model.BuildDiscarderProperty' {
                    strategy {
                        'daysToKeep'('2')
                        'numToKeep'('100')
                        'artifactDaysToKeep'('-1')
                        'artifactNumToKeep'('-1')
                    }
                }
            }
        }
