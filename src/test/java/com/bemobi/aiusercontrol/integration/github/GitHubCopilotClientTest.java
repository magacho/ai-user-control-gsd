package com.bemobi.aiusercontrol.integration.github;

import com.bemobi.aiusercontrol.dto.response.ToolAccountInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GitHubCopilotClientTest {

    @Mock
    private WebClient.Builder webClientBuilder;

    private GitHubCopilotClient gitHubCopilotClient;

    @BeforeEach
    void setUp() {
        gitHubCopilotClient = new GitHubCopilotClient(webClientBuilder);
    }

    @Test
    void testFetchSeats_parsesCreatedAtAndLastActivityAt() {
        // Given: Seat response with both date fields populated
        Map<String, Object> assignee = new HashMap<>();
        assignee.put("login", "alicegh");

        Map<String, Object> seat = new HashMap<>();
        seat.put("assignee", assignee);
        seat.put("created_at", "2024-08-03T18:00:00-06:00");
        seat.put("last_activity_at", "2025-01-15T10:30:00Z");

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("total_seats", 1);
        responseBody.put("seats", List.of(seat));

        mockWebClientResponse(responseBody);

        // When
        List<ToolAccountInfo> result = gitHubCopilotClient.fetchSeats("ghp-token", "bemobi-org");

        // Then: dates parsed correctly
        assertNotNull(result);
        assertEquals(1, result.size());

        ToolAccountInfo seatInfo = result.get(0);
        assertEquals("alicegh", seatInfo.getIdentifier());
        assertNull(seatInfo.getEmail()); // GitHub seats have null email

        // created_at: "2024-08-03T18:00:00-06:00" = 2024-08-04T00:00:00Z in UTC
        Instant expectedCreatedAt = OffsetDateTime.parse("2024-08-03T18:00:00-06:00").toInstant();
        assertEquals(expectedCreatedAt, seatInfo.getCreatedAtSource());

        // last_activity_at: "2025-01-15T10:30:00Z"
        Instant expectedLastActivity = Instant.parse("2025-01-15T10:30:00Z");
        assertEquals(expectedLastActivity, seatInfo.getLastActivityAt());
    }

    @Test
    void testFetchSeats_lastActivityAtNull_returnsNullInToolAccountInfo() {
        // Given: Seat with null last_activity_at (90-day retention, no activity data)
        Map<String, Object> assignee = new HashMap<>();
        assignee.put("login", "bobgh");

        Map<String, Object> seat = new HashMap<>();
        seat.put("assignee", assignee);
        seat.put("created_at", "2024-06-01T12:00:00Z");
        seat.put("last_activity_at", null);

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("total_seats", 1);
        responseBody.put("seats", List.of(seat));

        mockWebClientResponse(responseBody);

        // When
        List<ToolAccountInfo> result = gitHubCopilotClient.fetchSeats("ghp-token", "bemobi-org");

        // Then: createdAtSource parsed, lastActivityAt is null
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("bobgh", result.get(0).getIdentifier());
        assertNotNull(result.get(0).getCreatedAtSource());
        assertNull(result.get(0).getLastActivityAt());
    }

    @Test
    void testFetchSeats_timezoneOffset_parsedCorrectly() {
        // Given: created_at has timezone offset "-06:00"
        Map<String, Object> assignee = new HashMap<>();
        assignee.put("login", "charliegh");

        Map<String, Object> seat = new HashMap<>();
        seat.put("assignee", assignee);
        seat.put("created_at", "2024-08-03T18:00:00-06:00");
        seat.put("last_activity_at", "2025-02-10T14:30:00+03:00");

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("total_seats", 1);
        responseBody.put("seats", List.of(seat));

        mockWebClientResponse(responseBody);

        // When
        List<ToolAccountInfo> result = gitHubCopilotClient.fetchSeats("ghp-token", "bemobi-org");

        // Then: timezone offsets are converted to UTC Instant correctly
        assertNotNull(result);
        assertEquals(1, result.size());

        // -06:00 offset: 18:00 - (-06:00) = 2024-08-04T00:00:00Z
        Instant expectedCreated = OffsetDateTime.parse("2024-08-03T18:00:00-06:00").toInstant();
        assertEquals(expectedCreated, result.get(0).getCreatedAtSource());

        // +03:00 offset: 14:30 - (+03:00) = 2025-02-10T11:30:00Z
        Instant expectedActivity = OffsetDateTime.parse("2025-02-10T14:30:00+03:00").toInstant();
        assertEquals(expectedActivity, result.get(0).getLastActivityAt());
    }

    @Test
    void testFetchSeats_malformedDate_doesNotCrash_setsNull() {
        // Given: Seat with malformed date strings
        Map<String, Object> assignee = new HashMap<>();
        assignee.put("login", "davegh");

        Map<String, Object> seat = new HashMap<>();
        seat.put("assignee", assignee);
        seat.put("created_at", "not-a-date");
        seat.put("last_activity_at", "also-not-a-date");

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("total_seats", 1);
        responseBody.put("seats", List.of(seat));

        mockWebClientResponse(responseBody);

        // When: should not throw
        List<ToolAccountInfo> result = gitHubCopilotClient.fetchSeats("ghp-token", "bemobi-org");

        // Then: seat created with null dates (malformed dates logged as warning, not crash)
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("davegh", result.get(0).getIdentifier());
        assertNull(result.get(0).getCreatedAtSource());
        assertNull(result.get(0).getLastActivityAt());
    }

    @Test
    void testFetchSeats_nullApiToken_returnsEmptyList() {
        // When: null apiToken
        List<ToolAccountInfo> result = gitHubCopilotClient.fetchSeats(null, "bemobi-org");

        // Then: returns empty list without HTTP call
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(webClientBuilder, never()).baseUrl(anyString());
    }

    @Test
    void testFetchSeats_nullOrgName_returnsEmptyList() {
        // When: null orgName
        List<ToolAccountInfo> result = gitHubCopilotClient.fetchSeats("ghp-token", null);

        // Then: returns empty list without HTTP call
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(webClientBuilder, never()).baseUrl(anyString());
    }

    @Test
    void testFetchSeats_multipleSeats_allParsed() {
        // Given: Response with multiple seats
        Map<String, Object> assignee1 = new HashMap<>();
        assignee1.put("login", "user1");
        Map<String, Object> seat1 = new HashMap<>();
        seat1.put("assignee", assignee1);
        seat1.put("created_at", "2024-01-10T08:00:00Z");
        seat1.put("last_activity_at", "2025-02-25T16:00:00Z");

        Map<String, Object> assignee2 = new HashMap<>();
        assignee2.put("login", "user2");
        Map<String, Object> seat2 = new HashMap<>();
        seat2.put("assignee", assignee2);
        seat2.put("created_at", "2024-03-15T12:00:00Z");
        seat2.put("last_activity_at", null);

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("total_seats", 2);
        responseBody.put("seats", List.of(seat1, seat2));

        mockWebClientResponse(responseBody);

        // When
        List<ToolAccountInfo> result = gitHubCopilotClient.fetchSeats("ghp-token", "bemobi-org");

        // Then: Both seats parsed with their respective dates
        assertNotNull(result);
        assertEquals(2, result.size());

        assertEquals("user1", result.get(0).getIdentifier());
        assertNotNull(result.get(0).getCreatedAtSource());
        assertNotNull(result.get(0).getLastActivityAt());

        assertEquals("user2", result.get(1).getIdentifier());
        assertNotNull(result.get(1).getCreatedAtSource());
        assertNull(result.get(1).getLastActivityAt());
    }

    @SuppressWarnings("unchecked")
    private void mockWebClientResponse(Map<String, Object> responseBody) {
        WebClient webClient = mock(WebClient.class);
        WebClient.RequestHeadersUriSpec requestSpec = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec headersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        when(webClientBuilder.baseUrl(anyString())).thenReturn(webClientBuilder);
        when(webClientBuilder.defaultHeader(anyString(), anyString())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(webClient);
        when(webClient.get()).thenReturn(requestSpec);
        when(requestSpec.uri(anyString())).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class))).thenReturn(Mono.just(responseBody));
    }
}
