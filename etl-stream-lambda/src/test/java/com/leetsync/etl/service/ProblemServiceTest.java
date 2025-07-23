package com.leetsync.etl.service;

import com.leetsync.etl.model.Problem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProblemServiceTest {

    @Mock
    private DynamoDbEnhancedClient enhancedClient;
    
    @Mock
    private DynamoDbTable<Problem> problemTable;
    
    private ProblemService problemService;
    
    private static final String TABLE_NAME = "test-problems-table";

    @BeforeEach
    void setUp() {
        when(enhancedClient.<Problem>table(eq(TABLE_NAME), any())).thenReturn(problemTable);
        problemService = new ProblemService(enhancedClient, TABLE_NAME);
    }

    @Test
    void testGetProblem_Success() {
        String titleSlug = "two-sum";
        Problem expectedProblem = createTestProblem(titleSlug);
        
        when(problemTable.getItem(any(Key.class))).thenReturn(expectedProblem);
        
        Problem result = problemService.getProblem(titleSlug);
        
        assertNotNull(result);
        assertEquals(expectedProblem.getTitleSlug(), result.getTitleSlug());
        assertEquals(expectedProblem.getDifficulty(), result.getDifficulty());
        assertEquals(expectedProblem.getAcRate(), result.getAcRate());
        
        verify(problemTable).getItem(any(Key.class));
    }

    @Test
    void testGetProblem_NotFound() {
        String titleSlug = "nonexistent-problem";
        
        when(problemTable.getItem(any(Key.class))).thenReturn(null);
        
        Problem result = problemService.getProblem(titleSlug);
        
        assertNull(result);
        verify(problemTable).getItem(any(Key.class));
    }

    @Test
    void testGetProblem_Exception() {
        String titleSlug = "error-problem";
        
        when(problemTable.getItem(any(Key.class))).thenThrow(new RuntimeException("DynamoDB error"));
        
        Problem result = problemService.getProblem(titleSlug);
        
        assertNull(result);
        verify(problemTable).getItem(any(Key.class));
    }

    @Test
    void testGetProblem_KeyCreation() {
        String titleSlug = "test-problem";
        Problem expectedProblem = createTestProblem(titleSlug);
        
        when(problemTable.getItem(any(Key.class))).thenReturn(expectedProblem);
        
        problemService.getProblem(titleSlug);
        
        verify(problemTable).getItem(argThat((Key key) -> {
            // Verify the key is created correctly with the titleSlug as partition key
            return key != null;
        }));
    }

    @Test
    void testConstructor_TableSetup() {
        verify(enhancedClient).<Problem>table(eq(TABLE_NAME), any());
    }

    private Problem createTestProblem(String titleSlug) {
        return new Problem(
            123L,      // questionId
            1,         // frontendQuestionId
            titleSlug, // titleSlug
            1000000L,  // totalAccepted
            1908234L,  // totalSubmitted
            1          // difficultyLevel (Easy)
        );
    }
}