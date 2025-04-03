pipeline {
    agent any

    environment {
        CLIENT_ID = credentials('Clien_ID')
        CLIENT_SECRET = credentials('Client_Secret')
    }

    stages {
        stage('Generate Token') {
            steps {
                writeFile file: 'token.txt', text: ''
                script {
                    def response = httpRequest customHeaders: [
                        [name: 'Content-Type', value: 'application/json']
                    ],
                    httpMode: 'POST',
                    requestBody: "{\"client_id\": \"${CLIENT_ID}\", \"client_secret\": \"${CLIENT_SECRET}\"}",
                    url: 'https://xray.cloud.getxray.app/api/v2/authenticate'

                    writeFile file: 'token.txt', text: response.content.replaceAll('"', '')
                }
            }
        }

        stage('Download Features from Xray') {
            steps {
                script {
                    def token = readFile('token.txt').trim()
                    def download = httpRequest customHeaders: [
                        [name: 'Authorization', value: "Bearer ${token}"]
                    ],
                    httpMode: 'GET',
                    url: 'https://xray.cloud.getxray.app/api/v2/export/cucumber?keys=POEI20252-531'

                    writeFile file: 'features.zip', text: download.content
                    bat 'powershell Expand-Archive -Path features.zip -DestinationPath src\\test\\resources\\features\\imported -Force'
                }
            }
        }

       stage('Merge Features') {
           steps {
               bat '''
                   type src\\test\\resources\\features\\imported\\*.feature > src\\test\\resources\\features\\merged.feature
               '''
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
                    httpRequest customHeaders: [
                        [name: 'Authorization', value: "Bearer ${token}"],
                        [name: 'Content-Type', value: "application/json"]
                    ],
                    httpMode: 'POST',
                    requestBody: readFile('target/cucumber.json'),
                    url: 'https://xray.cloud.getxray.app/api/v2/import/execution/cucumber'
                }
            }
        }
    }
}