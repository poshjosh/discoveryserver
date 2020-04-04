#!/usr/bin/env groovy
/**
 * https://github.com/poshjosh/discoveryserver
 * @see https://hub.docker.com/_/maven
 */
library(
    identifier: 'jenkins-shared-library@master',
    retriever: modernSCM(
        [
            $class: 'GitSCMSource',
            remote: 'https://github.com/poshjosh/jenkins-shared-library.git'
        ]
    )
)

pipelineForJavaSprintBoot(
        appPort : '8761',
        appEndpoint : '/actuator/health',
        mainClass : 'com.looseboxes.discoveryserver.ServiceRegistrationServer'
)
