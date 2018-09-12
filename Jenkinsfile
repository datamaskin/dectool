pipeline {
  agent any
  
  environment {
    //Use Pipeline Utility Steps plugin to read information from pom.xml into env variables
    ARTIFACTID = readMavenPom().getArtifactId()
    VERSION = readMavenPom().getVersion().replace("-SNAPSHOT", "")
  }
  
  // Poll git every hour (Monday-Friday) to see if any changes have been commited
  triggers { pollSCM('H * * * 1-5') }
  
  stages {
  
    stage('Set Version') {
      
      parallel {
        stage('Master') {
          when { branch 'master' }
          steps {
            // We run the special set-version script as it will re-use the previous tag version if the current commit contains [ci skip]
            sh '/git/set-version.sh ${VERSION}'
          }
        }
        stage('Other Branches') {
          when {
            not {
              branch 'master'
            }
          }
          steps {
            // We append the commit hash so that every release build is unique.  This prevents clashing with the master version which uses build number
            sh 'mvn versions:set -DnewVersion=${BRANCH_NAME}-${VERSION}-SNAPSHOT'
          }
        }
      }
    }
    
    stage('Build') {
      steps {
        sh 'echo Building ${BRANCH_NAME} branch'
        sh 'mvn clean compile package'
      }
    }
    
    // Every project should have a FindBugs stage, make sure your pom.xml is configured for FindBugs
    stage('Spotbugs') {
      steps {
        sh 'mvn verify'
        
        findbugs canComputeNew: false, canRunOnFailed: true, defaultEncoding: '', excludePattern: '', failedTotalHigh: '0', healthy: '', includePattern: '', isRankActivated: true, pattern: '**/spotbugsXml.xml', unHealthy: ''
        
        archiveArtifacts allowEmptyArchive: true, artifacts: '**/spotbugsXml.xml'
      }
    }
    
    // Every project should have a Dependency-Check stage.  These settings shouldn't need to be changed.
    stage('Dependency-Check') {
      steps {
        dependencyCheckAnalyzer datadir: '', hintsFile: '', includeCsvReports: false, includeHtmlReports: false, includeJsonReports: false, includeVulnReports: false, isAutoupdateDisabled: false, outdir: '', scanpath: '**/*.jar', skipOnScmChange: false, skipOnUpstreamChange: false, suppressionFile: '', zipExtensions: ''

        dependencyCheckPublisher canComputeNew: false, defaultEncoding: '', failedTotalHigh: '0', healthy: '0', pattern: '', thresholdLimit: 'high', unHealthy: '1'
        
        archiveArtifacts allowEmptyArchive: true, artifacts: '**/dependency-check-report.xml'
      }
    } 
    
    stage('Archiving') {
      // This is the final step that will save the build jar/war to jenkins and push it into maven.  Change *.jar to *.war for webapps
      parallel {
        
        stage('Archive Jenkins') {
          steps {
            // Archive in jenkins
            archiveArtifacts artifacts: '**/*.jar', fingerprint: true
          }
        }
        
        // It will only install into archiva if the latest commit message does not contain [ci skip]
        stage('Archive Master') {
          when { 
            branch 'master'
            changelog '^((?!ci skip).)*$'
          }
          steps {        
            // Installs locally and deploys to archiva
            sh 'mvn install deploy'
          }
        }
        
        // Always archive the branch snapshot build
        stage('Archive Branch') {
          when {
            not { branch 'master' }
          }
          steps {
              // Installs locally and deploys to archiva
              sh 'mvn install deploy'
          }          
        }
      }
    }
    
    // This will only tag the master branch when a new change is built.  Must be last step, otherwise the archive step will rebuild and upload with the new snapshot version
    // It will also only tag if the latest commit message does not contain [ci skip]
    stage('Tag') {
      when { 
        branch 'master'
        changelog '^((?!ci skip).)*$'
      }
      steps {
        sh 'git tag ${VERSION}'
        sh 'git push origin --tags'
        
        sh '/git/increment-pom-version.sh ${VERSION}'
        
        sh 'git commit -am "[ci skip] Incremented Version"'
        
        // commit is on a detached head, assign it to a temp branch, merge back to master, then delete the temp branch
        sh 'git branch temp-branch'
        sh 'git checkout master'
        sh 'git merge temp-branch'
        sh 'git branch -d temp-branch'
        sh 'git push origin master'
      }
    }   
  }
  
  post {
    failure {
      // If the build fails, send a notification to Slack
      slackSend "Failed to build ${env.JOB_NAME} ${env.BUILD_NUMBER} (<${env.BUILD_URL}|Open>)"
    }
  }
}