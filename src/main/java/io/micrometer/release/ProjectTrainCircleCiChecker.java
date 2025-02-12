/**
 * Copyright 2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micrometer.release;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ProjectTrainCircleCiChecker {

    private static final Logger log = LoggerFactory.getLogger(ProjectTrainCircleCiChecker.class);

    private final String circleCiToken;

    private final String githubOrgRepo;

    private final HttpClient httpClient;

    private final ObjectMapper objectMapper;

    ProjectTrainCircleCiChecker(String circleCiToken, String githubOrgRepo, HttpClient httpClient,
            ObjectMapper objectMapper) {
        this.circleCiToken = circleCiToken;
        this.githubOrgRepo = githubOrgRepo;
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    boolean checkBuildStatus(String version) throws IOException, InterruptedException {
        log.info("Checking CircleCI status for version: [{}]", version);
        String tag = "v" + version;
        String apiUrl = "https://circleci.com/api/v2/project/github/" + githubOrgRepo + "/pipeline";
        int pageCount = 0;
        while (apiUrl != null && pageCount < 2) { // Limit to 2 pages - there shouldn't be
                                                  // more jobs to search against
            HttpRequest request = getCircleHttpRequest(apiUrl);
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            PipelineResponse pipelineResponse = objectMapper.readValue(response.body(), PipelineResponse.class);
            for (Pipeline pipeline : pipelineResponse.items()) {
                if (tag.equals(pipeline.vcs().tag())) {
                    return checkWorkflowStatus(pipeline.id());
                }
            }
            apiUrl = pipelineResponse.nextPageToken() != null
                    ? apiUrl + "?page-token=" + pipelineResponse.nextPageToken() : null;
            pageCount++;
        }
        throw new IllegalStateException("No CircleCI pipeline found for tag: " + tag);
    }

    private boolean checkWorkflowStatus(String pipelineId) throws IOException, InterruptedException {
        String workflowUrl = "https://circleci.com/api/v2/pipeline/" + pipelineId + "/workflow";
        HttpRequest request = getCircleHttpRequest(workflowUrl);
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        WorkflowResponse workflowResponse = objectMapper.readValue(response.body(), WorkflowResponse.class);
        for (Workflow workflow : workflowResponse.items()) {
            if (!"success".equalsIgnoreCase(workflow.status())) {
                if ("failed".equalsIgnoreCase(workflow.status())) {
                    throw new IllegalStateException("Workflow [" + workflow.name + "] failed!");
                }
                return false;
            }
        }
        return true;
    }

    private HttpRequest getCircleHttpRequest(String workflowUrl) {
        return HttpRequest.newBuilder().uri(URI.create(workflowUrl)).header("Circle-Token", circleCiToken).build();
    }

    public record VCSInfo(@JsonProperty("tag") String tag) {

    }

    public record Pipeline(@JsonProperty("id") String id, @JsonProperty("vcs") VCSInfo vcs) {

    }

    public record PipelineResponse(@JsonProperty("items") List<Pipeline> items,
            @JsonProperty("next_page_token") String nextPageToken) {

    }

    public record Workflow(@JsonProperty("name") String name, @JsonProperty("status") String status) {

    }

    public record WorkflowResponse(@JsonProperty("items") List<Workflow> items) {

    }

}
