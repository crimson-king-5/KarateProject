pipeline {
    agent any

    environment {
        CLIENT_ID     = credentials('Clien_ID')          // Jenkins credential
        CLIENT_SECRET = credentials('Client_Secret')     // Jenkins credential
        TEST_PLAN_KEY = 'POEI20252-531'
    }

    stages {
        stage('Generate Token') {
            steps {
                script {
                    def response = httpRequest(
                        httpMode: 'POST',
                        url: 'https://xray.cloud.getxray.app/api/v2/authenticate',
                        contentType: 'APPLICATION_JSON',
                        requestBody: """{
                            "client_id": "${CLIENT_ID}",
                            "client_secret": "${CLIENT_SECRET}"
                        }"""
                    )
                    writeFile file: 'token.txt', text: response.content.replaceAll('"', '')
                }
            }
        }

        stage('Download Features from Xray') {
            steps {
                script {
                    def token = readFile('token.txt').trim()
                    def response = httpRequest(
                        httpMode: 'GET',
                        url: "https://xray.cloud.getxray.app/api/v2/export/cucumber?keys=${TEST_PLAN_KEY}",
                        customHeaders: [[name: 'Authorization', value: "Bearer ${token}"]],
                        validResponseCodes: '200'
                    )

                    writeFile file: 'features.zip', text: response.content
                    bat 'powershell Expand-Archive -Path features.zip -DestinationPath src\\test\\resources\\features\\imported -Force'
                }
            }
        }

        stage('Merge Features') {
            steps {
                script {
                    def merged = new File('src/test/resources/features/merged.feature')
                    merged.text = "Feature: Test Xray\n\n"

                    def importedDir = new File('src/test/resources/features/imported')
                    importedDir.eachFileRecurse(FileType.FILES) { file ->
                        if (file.name.endsWith('.feature')) {
                            def content = file.text.replaceAll("(?i)^Feature:.*\\R", "")
                            merged.append(content + "\n")
                        }
                    }
                }
            }
        }

        stage('Run Tests') {
            steps {
                bat 'mvn test'
            }
        }

        stage('Send Results to Xray') {
            steps {
                script {
                    def token = readFile('token.txt').trim()
                    def resultJson = readFile('target/cucumber.json')

                    httpRequest(
                        httpMode: 'POST',
                        url: 'https://xray.cloud.getxray.app/api/v2/import/execution/cucumber',
                        customHeaders: [
                            [name: 'Authorization', value: "Bearer ${token}"],
                            [name: 'Content-Type', value: 'application/json']
                        ],
                        requestBody: resultJson
                    )
                }
            }
        }
    }
}