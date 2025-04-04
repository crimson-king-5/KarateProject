pipeline {
    agent any

    environment {
        CLIENT_ID     = credentials('Clien_ID')
        CLIENT_SECRET = credentials('Client_Secret')
        TEST_PLAN_KEY = 'POEI20252-531'
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'main', url: 'https://github.com/crimson-king-5/KarateProject.git'
            }
        }

        stage('Generate Token') {
            steps {
                script {
                    bat """
                        curl -X POST https://xray.cloud.getxray.app/api/v2/authenticate ^
                        -H "Content-Type: application/json" ^
                        -d "{\\"client_id\\": \\"%CLIENT_ID%\\", \\"client_secret\\": \\"%CLIENT_SECRET%\\"}" ^
                        > token.txt
                    """
                }
            }
        }

        stage('Download Features from Xray') {
            steps {
                script {
                    bat """
                        set /p TOKEN=<token.txt
                        curl -X GET "https://xray.cloud.getxray.app/api/v2/export/cucumber?keys=%TEST_PLAN_KEY%" ^
                        -H "Authorization: Bearer %TOKEN%" ^
                        -o features.zip
                        powershell Expand-Archive -Path features.zip -DestinationPath src\\test\\resources\\features\\imported -Force
                    """
                }
            }
        }

        stage('Merge Features') {
            steps {
                bat 'type src\\test\\resources\\features\\imported\\*.feature > src\\test\\resources\\features\\merged.feature'
            }
        }

        stage('Run Tests') {
            steps {
                bat 'mvn test'
            }
        }

        stage('Merge JSON Reports') {
            steps {
                bat 'copy /b target\\cucumber*.json merged.json'
            }
        }

        stage('Send Results to Xray') {
            steps {
                script {
                    bat """
                        set /p TOKEN=<token.txt
                        curl -X POST https://xray.cloud.getxray.app/api/v2/import/execution/cucumber ^
                        -H "Content-Type: application/json" ^
                        -H "Authorization: Bearer %TOKEN%" ^
                        --data-binary "@merged.json"
                    """
                }
            }
        }
    }
}