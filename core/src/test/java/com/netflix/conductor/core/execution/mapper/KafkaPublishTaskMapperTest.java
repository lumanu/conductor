/*
 * Copyright 2022 Netflix, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package com.netflix.conductor.core.execution.mapper;

import java.util.HashMap;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.netflix.conductor.common.metadata.tasks.TaskDef;
import com.netflix.conductor.common.metadata.tasks.TaskType;
import com.netflix.conductor.common.metadata.workflow.WorkflowDef;
import com.netflix.conductor.common.metadata.workflow.WorkflowTask;
import com.netflix.conductor.core.utils.IDGenerator;
import com.netflix.conductor.core.utils.ParametersUtils;
import com.netflix.conductor.dao.MetadataDAO;
import com.netflix.conductor.model.TaskModel;
import com.netflix.conductor.model.WorkflowModel;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class KafkaPublishTaskMapperTest {

    private KafkaPublishTaskMapper kafkaTaskMapper;

    @Rule public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() {
        ParametersUtils parametersUtils = mock(ParametersUtils.class);
        MetadataDAO metadataDAO = mock(MetadataDAO.class);
        kafkaTaskMapper = new KafkaPublishTaskMapper(parametersUtils, metadataDAO);
    }

    @Test
    public void getMappedTasks() {
        // Given
        WorkflowTask workflowTask = new WorkflowTask();
        workflowTask.setName("kafka_task");
        workflowTask.setType(TaskType.KAFKA_PUBLISH.name());
        workflowTask.setTaskDefinition(new TaskDef("kafka_task"));
        String taskId = IDGenerator.generate();
        String retriedTaskId = IDGenerator.generate();

        WorkflowModel workflow = new WorkflowModel();
        WorkflowDef workflowDef = new WorkflowDef();
        workflow.setWorkflowDefinition(workflowDef);

        TaskMapperContext taskMapperContext =
                TaskMapperContext.newBuilder()
                        .withWorkflowModel(workflow)
                        .withTaskDefinition(new TaskDef())
                        .withWorkflowTask(workflowTask)
                        .withTaskInput(new HashMap<>())
                        .withRetryCount(0)
                        .withRetryTaskId(retriedTaskId)
                        .withTaskId(taskId)
                        .build();

        // when
        List<TaskModel> mappedTasks = kafkaTaskMapper.getMappedTasks(taskMapperContext);

        // Then
        assertEquals(1, mappedTasks.size());
        assertEquals(TaskType.KAFKA_PUBLISH.name(), mappedTasks.get(0).getTaskType());
    }

    @Test
    public void getMappedTasks_WithoutTaskDef() {
        // Given
        WorkflowTask workflowTask = new WorkflowTask();
        workflowTask.setName("kafka_task");
        workflowTask.setType(TaskType.KAFKA_PUBLISH.name());
        String taskId = IDGenerator.generate();
        String retriedTaskId = IDGenerator.generate();

        WorkflowModel workflow = new WorkflowModel();
        WorkflowDef workflowDef = new WorkflowDef();
        workflow.setWorkflowDefinition(workflowDef);

        TaskDef taskdefinition = new TaskDef();
        String testExecutionNameSpace = "testExecutionNameSpace";
        taskdefinition.setExecutionNameSpace(testExecutionNameSpace);
        String testIsolationGroupId = "testIsolationGroupId";
        taskdefinition.setIsolationGroupId(testIsolationGroupId);
        TaskMapperContext taskMapperContext =
                TaskMapperContext.newBuilder()
                        .withWorkflowModel(workflow)
                        .withTaskDefinition(taskdefinition)
                        .withWorkflowTask(workflowTask)
                        .withTaskInput(new HashMap<>())
                        .withRetryCount(0)
                        .withRetryTaskId(retriedTaskId)
                        .withTaskId(taskId)
                        .build();

        // when
        List<TaskModel> mappedTasks = kafkaTaskMapper.getMappedTasks(taskMapperContext);

        // Then
        assertEquals(1, mappedTasks.size());
        assertEquals(TaskType.KAFKA_PUBLISH.name(), mappedTasks.get(0).getTaskType());
        assertEquals(testExecutionNameSpace, mappedTasks.get(0).getExecutionNameSpace());
        assertEquals(testIsolationGroupId, mappedTasks.get(0).getIsolationGroupId());
    }
}
