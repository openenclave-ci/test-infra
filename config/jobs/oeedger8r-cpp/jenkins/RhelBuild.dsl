pipelineJob("Rhel8Build") {
	description()
	keepDependencies(false)
	parameters {
		stringParam("PULL_NUMBER", "master", "")
		booleanParam("TEST_INFRA", true, "")
		stringParam("BUILD_TYPE", "Release", "")
		stringParam("EXTRA_CMAKE_ARGS", "", "")
	}
	definition {
		cpsScm {
			scm {
				git {
					remote {
						github("openenclave-ci/test-infra", "https")
					}
					branch("origin/pr/\${PULL_NUMBER}")
					branch("origin/master")
					branch("*/master")
					extensions {
						wipeOutWorkspace()
					}
				}
			}
			scriptPath("config/jobs/oeedger8r-cpp/jenkins/Rhel8Build.Jenkinsfile")
		}
	}
	disabled(false)
	configure {
		it / 'properties' / 'jenkins.model.BuildDiscarderProperty' {
			strategy {
				'daysToKeep'('2')
				'numToKeep'('10')
				'artifactDaysToKeep'('-1')
				'artifactNumToKeep'('-1')
			}
		}
	}
}
