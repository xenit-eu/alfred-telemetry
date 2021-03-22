pipeline {
    agent any

    stages {
        stage("Clean") {
            steps {
                sh "./gradlew clean -i"
            }
        }
        stage("Assemble") {
            steps {
                sh "./gradlew assemble -i"
            }
        }
        stage("Unit Tests") {
            steps {
                sh "./gradlew test -i"
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
