package com.leetsync.stats.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.ScheduledEvent;
import com.leetsync.stats.service.AthenaQueryService;
import com.leetsync.stats.service.StatsCacheService;
import com.leetsync.stats.service.StatsCalculationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import software.amazon.awssdk.services.athena.model.ResultSet;
import software.amazon.awssdk.services.athena.model.Row;
import software.amazon.awssdk.services.athena.model.Datum;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class StatsHandlerTest {

    @Mock
    private AthenaQueryService athenaService;
    
    @Mock
    private StatsCacheService cacheService;
    
    @Mock
    private StatsCalculationService calculationService;
    
    @Mock
    private Context context;

    private StatsHandler handler;
    private ScheduledEvent event;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        handler = new StatsHandler(athenaService, cacheService, calculationService);
        event = new ScheduledEvent();
    }

    @Test
    void handleRequest_ProcessesActiveUsers() {
        // Mock table ready
        doNothing().when(athenaService).ensureTableReady(any());
        
        // Mock active users query
        ResultSet mockResultSet = createMockActiveUsersResultSet("user1", "user2");
        when(athenaService.getYesterdayActiveUsers(any(LocalDate.class))).thenReturn(mockResultSet);
        
        // Mock calculation service
        doNothing().when(calculationService).calculateAllStatsForUser(any(), any());

        String result = handler.handleRequest(event, context);

        assertNotNull(result);
        assertTrue(result.contains("Successfully processed stats for 2/2 users"));
        
        verify(athenaService).ensureTableReady(any());
        verify(athenaService).getYesterdayActiveUsers(any(LocalDate.class));
        verify(calculationService, times(2)).calculateAllStatsForUser(any(), any());
    }

    @Test
    void handleRequest_SkipsEmptyUsers() {
        // Mock table ready
        doNothing().when(athenaService).ensureTableReady(any());
        
        // Mock active users query with empty username
        ResultSet mockResultSet = createMockActiveUsersResultSet("user1", "", "user2");
        when(athenaService.getYesterdayActiveUsers(any(LocalDate.class))).thenReturn(mockResultSet);
        
        // Mock calculation service
        doNothing().when(calculationService).calculateAllStatsForUser(any(), any());

        String result = handler.handleRequest(event, context);

        assertNotNull(result);
        assertTrue(result.contains("Successfully processed stats for 2/3 users"));
        
        // Should only be called twice for valid usernames
        verify(calculationService, times(2)).calculateAllStatsForUser(any(), any());
    }

    @Test
    void handleRequest_ContinuesOnUserFailure() {
        // Mock table ready
        doNothing().when(athenaService).ensureTableReady(any());
        
        // Mock active users query
        ResultSet mockResultSet = createMockActiveUsersResultSet("user1", "user2");
        when(athenaService.getYesterdayActiveUsers(any(LocalDate.class))).thenReturn(mockResultSet);
        
        // Mock calculation service to fail for first user
        doThrow(new RuntimeException("Calculation failed"))
                .when(calculationService).calculateAllStatsForUser(eq("user1"), any());
        doNothing().when(calculationService).calculateAllStatsForUser(eq("user2"), any());

        String result = handler.handleRequest(event, context);

        assertNotNull(result);
        assertTrue(result.contains("Successfully processed stats for 1/2 users"));
        
        verify(calculationService, times(2)).calculateAllStatsForUser(any(), any());
    }

    @Test
    void processSpecificUser_Success() {
        // Mock table ready
        doNothing().when(athenaService).ensureTableReady(any());
        
        // Mock calculation service
        doNothing().when(calculationService).calculateAllStatsForUser(any(), any());

        String result = handler.processSpecificUser("testuser");

        assertNotNull(result);
        assertTrue(result.contains("Successfully processed stats for user: testuser"));
        
        verify(athenaService).ensureTableReady(any());
        verify(calculationService).calculateAllStatsForUser(eq("testuser"), any(LocalDate.class));
    }

    @Test
    void healthCheck_Success() {
        // Mock table ready
        doNothing().when(athenaService).ensureTableReady(any());
        
        // Mock cache service
        when(cacheService.getTotalStats("healthcheck-user")).thenReturn(null);

        String result = handler.healthCheck();

        assertEquals("Stats Lambda health check passed", result);
        
        verify(athenaService).ensureTableReady(any());
        verify(cacheService).getTotalStats("healthcheck-user");
    }

    private ResultSet createMockActiveUsersResultSet(String... usernames) {
        // Create header row
        Row headerRow = Row.builder()
                .data(Datum.builder().varCharValue("username").build())
                .build();
        
        // Create data rows
        List<Row> rows = Arrays.asList(headerRow);
        rows = new java.util.ArrayList<>(rows);
        
        for (String username : usernames) {
            Row dataRow = Row.builder()
                    .data(Datum.builder().varCharValue(username).build())
                    .build();
            rows.add(dataRow);
        }

        return ResultSet.builder()
                .rows(rows)
                .build();
    }
}