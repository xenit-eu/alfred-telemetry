pipeline {
    agent any

    stages {
        stage("Clean") {
            steps {
                sh "./gradlew clean -i"
            }
        }
        stage("Enterprise Integration Tests") {
            when {
                anyOf {
                    branch "master*"
                    changeRequest target: "master*"
                    changeRequest target: "release*"
                }
            }
            steps {
                sh "./gradlew integrationTest -Penterprise"
            }
        }
    }

    post {
        always {
            sh "./gradlew composeDownForced"
            script {
                junit '**/build/**/TEST-*.xml'
            }
        }
    }
}
